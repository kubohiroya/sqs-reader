/**
 * AbstractTaskTracker.java

 Copyright 2007 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2007/01/31
 Author hiroya
 */

package net.sqs2.omr.session.daemon;

import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.sqs2.lang.GroupThreadFactory;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.session.service.AbstractOMRPageTask;

public abstract class TaskTracker<T extends OMRPageTask, D extends ServerDispatcher>{

	public static final int EXECUTOR_THREAD_PRIORIY = Thread.NORM_PRIORITY - 1;
	public static boolean DEBUG_CLUSTER_MODE = false;

	protected D dispatcher;
	private boolean isConnected = false;
	
	int numTrackerThreads;
	int numEmitterThreads;

	private GroupThreadFactory trackerGroupThreadFactory;
	private ScheduledExecutorService trackerThreadPool;
	private Future<?>[] trackerFutures = null;

	private ScheduledExecutorService emitterThreadPool;
	private Future<?>[] emitterFutures = null;

	int idleTime = 500;

	public TaskTracker(D dispatcher) {
		
		this.dispatcher = dispatcher;
		this.numTrackerThreads = 1;
		this.numEmitterThreads = 1;
		
		start();
	}
	
	public D getDispatcher(){
		return this.dispatcher;
	}

	public void start() {
		this.trackerGroupThreadFactory = new GroupThreadFactory("TaskTracker"+System.currentTimeMillis(), EXECUTOR_THREAD_PRIORIY, true);
		
		setConnected(true);

		if (this.trackerThreadPool == null) {
			this.trackerThreadPool = Executors.newScheduledThreadPool(this.numTrackerThreads, this.trackerGroupThreadFactory);
		}
		if (this.emitterThreadPool == null) {
			this.emitterThreadPool = Executors.newScheduledThreadPool(this.numEmitterThreads, this.trackerGroupThreadFactory);
		}
		
		if (this.trackerFutures == null) {
			this.trackerFutures = new Future[numTrackerThreads];
			Runnable tracker = new Runnable(){
				public void run() {
					
					if(idleTime < 1000){
						idleTime += 10;
					}else{
						idleTime = 1000;
					}
					
					try {
						if (dispatcher.isRemote()) {
							// remote session
							if (TaskTracker.DEBUG_CLUSTER_MODE) {
								Logger.getLogger(getClass().getName()).info("execute remote task in debug mode");
							} else if (dispatcher.getLocalServer() != null
									&& dispatcher.getLocalServer().existsRunningLocalSessions()) {
								sleep(idleTime);
								return;
							}
							
						} else {
							// local session
							if (! dispatcher.hasPublished()) {
								sleep(idleTime);
								return;
							}
						}
						if ( trackAndExecuteTask() ) {
							//execute task
							idleTime = 0;
							return;
						}
					} catch (RemoteException ex) {
						// ignore.printStackTrace();
						Logger.getLogger("executor").warning("RemoteSession closed.");
						stop();
					} finally{
						Thread.yield();
					}
				}
			};
			for (int i = 0; i < numTrackerThreads; i++) {
				this.trackerFutures[i] = this.trackerThreadPool.scheduleWithFixedDelay(tracker, 100 * i / numTrackerThreads, 1, TimeUnit.MILLISECONDS);
			}
		}
		
		if (this.emitterFutures == null) {
			this.emitterFutures = new Future[this.numEmitterThreads];
			Runnable emitter = new Runnable(){
				public void run() {
					emit();
				}
			};
			for (int i = 0; i < this.numEmitterThreads; i++) {
				this.emitterFutures[i] = this.emitterThreadPool.scheduleWithFixedDelay(emitter, 100 * i / this.numEmitterThreads, 1, TimeUnit.MILLISECONDS);
			}
		}
	}
	
	public void stop() {
		if (this.trackerFutures != null) {
			for (int i = 0; i < this.trackerFutures.length; i++) {
				if (this.trackerFutures[i] != null) {
					this.trackerFutures[i].cancel(true);
				}
			}
			this.trackerFutures = null;
		}
		if (this.trackerThreadPool != null) {
			this.trackerThreadPool.shutdown();
			this.trackerThreadPool = null;
		}
		
		if (this.emitterFutures != null) {
			for (int i = 0; i < this.emitterFutures.length; i++) {
				this.emitterFutures[i].cancel(true);
			}
			this.emitterFutures = null;
		}

		if (this.emitterThreadPool != null) {
			this.emitterThreadPool.shutdown();
			this.emitterThreadPool = null;
		}
		
		this.trackerGroupThreadFactory.destroy();
	}

	public void shutdown() {
		stop();
		setConnected(false);
		if (this.dispatcher != null) {
			this.dispatcher.close();
			this.dispatcher = null;
		}
	}
	
	public long getKey() {
		return this.dispatcher.getKey();
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public boolean isConnected() {
		try {
			this.dispatcher.getServer().ping(this.dispatcher.getKey());
			return this.isConnected;
		} catch (RemoteException ignore) {
			stop();
			return false;
		}
	}
	
	private void sleep(int msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException ignore) {
		}
	}
	
	private boolean trackAndExecuteTask() throws RemoteException {
		try {
			AbstractOMRPageTask<T,D> executable = pollPreExecutionTask();
			if (executable == null) {
				return false;
			}
		
			execute(executable);
		
			return true;
			
		} catch (InterruptedException ignore) {
			// ignore
		} catch (ClassCastException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		}finally{
			Thread.yield();
		}
		return false;
	}

	private void emit() {
		try {
			AbstractOMRPageTask<T,D> executable = null;
			executable = pollPostExecutionTask();
			if (executable == null) {
				return;
			}
			executable.emit();
		} catch (InterruptedException ignore) {
		} catch (RemoteException ignore) {
			// ignore exception in uploading. we will drop the uploading
			// task but never stop uploading thread itself.
		}finally{
			Thread.yield();
		}
	}

	abstract protected AbstractOMRPageTask<T,D> pollPreExecutionTask()throws RemoteException, InterruptedException;	
	abstract protected void execute(AbstractOMRPageTask<T,D> executable)throws RemoteException, InterruptedException;
	abstract protected AbstractOMRPageTask<T,D> pollPostExecutionTask() throws InterruptedException;
}

