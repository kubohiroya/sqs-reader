/**
 *  SessionTaskDaemons.java

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

package net.sqs2.omr.session.service;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sqs2.lang.GroupThreadFactory;
import net.sqs2.net.MulticastNetworkConnection;
import net.sqs2.net.RMIRegistryMulticastAdvertisingService;
import net.sqs2.omr.model.MarkReaderConfiguration;
import net.sqs2.omr.model.MarkReaderConstants;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.PageTaskHolder;
import net.sqs2.omr.session.daemon.RemoteTaskTracker;
import net.sqs2.omr.session.daemon.SessionSourceServerDispatcher;
import net.sqs2.omr.session.server.LocalPageTaskRecycleService;
import net.sqs2.omr.session.server.RemotePageTaskRecycleService;
import net.sqs2.sound.SoundManager;

public class SessionDaemons {

	//public static final int TASK_PRODUCER_EXEC_INTERVAL_IN_MILLIS = 10000;// msec
	public static final int TASK_CONSUMER_EXEC_INTERVAL_IN_MILLIS = 5;// msec
	public static final int RECYCLE_INTERVAL_IN_SEC = 31; // sec

	private GroupThreadFactory daemonThreadFactory;
	private GroupThreadFactory sessionThreadFactory;

	private ScheduledExecutorService remoteTaskRecycleService;
	private ScheduledExecutorService localTaskRecycleService;

	private ScheduledExecutorService sessionSourceInitService;
	private ScheduledExecutorService sessionSourceScanService;
	private ScheduledExecutorService pageTaskCommitService;
	private ScheduledExecutorService resultTraverseWorkerService;

	private Future<?> localTaskRecycleFuture;
	private Future<?> remoteTaskRecycleFuture;
	private Future<Integer> sessionSourceInitFuture;
	private Future<Integer> sessionSourceScanFuture;
	private Future<Integer> pageTaskCommitFuture;
	private Future<?> resultTraverseWorkerFuture;

	private RMIRegistryMulticastAdvertisingService advertisingService = null;
	private SessionSourceServerDispatcher dispatcher;
	private PageTaskHolder pageTaskHolder;
	private String rmiBindingName;

	public SessionDaemons(
			long sessionID,
			RemoteTaskTracker<OMRPageTask, SessionSourceServerDispatcher> taskTracker,
			SessionSourceServerDispatcher dispatcher,
			PageTaskHolder pageTaskHolder) {
	
		this.daemonThreadFactory = new GroupThreadFactory("SessionDaemons", Thread.MIN_PRIORITY, true);
		 
		this.advertisingService = createAdvertisingService(sessionID, taskTracker, dispatcher.getKey());
		this.dispatcher = dispatcher;
		
		this.remoteTaskRecycleService = Executors.newScheduledThreadPool(1,	daemonThreadFactory);
		this.localTaskRecycleService = Executors.newScheduledThreadPool(1,	daemonThreadFactory);
		this.pageTaskHolder = pageTaskHolder;
		this.rmiBindingName = taskTracker.getRMIBindingName();
		startTaskRecycleThread();
	}

	private void startTaskRecycleThread() {
		this.remoteTaskRecycleFuture = this.remoteTaskRecycleService.scheduleWithFixedDelay(new RemotePageTaskRecycleService(this.pageTaskHolder), 
				RECYCLE_INTERVAL_IN_SEC, RECYCLE_INTERVAL_IN_SEC, TimeUnit.SECONDS);

		this.localTaskRecycleFuture = this.localTaskRecycleService.scheduleWithFixedDelay(new LocalPageTaskRecycleService(this.pageTaskHolder),
				RECYCLE_INTERVAL_IN_SEC + 1, RECYCLE_INTERVAL_IN_SEC, TimeUnit.SECONDS);
	}

	public void start(boolean isDaemon) {
		
		sessionThreadFactory = new GroupThreadFactory("MarkReaderSessionThread", Thread.NORM_PRIORITY - 1, isDaemon);

		this.sessionSourceInitService = Executors.newScheduledThreadPool(1, sessionThreadFactory);
		this.sessionSourceScanService = Executors.newScheduledThreadPool(1, sessionThreadFactory);
		this.pageTaskCommitService = Executors.newScheduledThreadPool(1, sessionThreadFactory);
		this.resultTraverseWorkerService = Executors.newScheduledThreadPool(1, sessionThreadFactory);
		startAdvertisement();
	}

	public Future<Integer> executeSessionSourceInit(Callable<Integer> sessionSourceFactoryCommand) {
		this.sessionSourceInitFuture = this.sessionSourceInitService.submit(sessionSourceFactoryCommand);
		return this.sessionSourceInitFuture;
	}

	public Future<Integer> executeSessionSourceScanning(Callable<Integer> task) {
		this.sessionSourceScanFuture = this.sessionSourceScanService.submit(task);
		return this.sessionSourceScanFuture;
	}

	public Future<Integer> executePageTaskCommitService(Callable<Integer> task) {
		this.pageTaskCommitFuture = this.pageTaskCommitService.submit(task);
		return this.pageTaskCommitFuture;
	}

	public Future<?> executeResultTraverseWorkerService(Callable<Void> task) {
		this.resultTraverseWorkerFuture = this.resultTraverseWorkerService.submit(task);
		return this.resultTraverseWorkerFuture;
	}

	public void stop() {
		stopAdvertisement();

		stopFuture(this.sessionSourceInitFuture);
		this.sessionSourceInitFuture = null;
		stopFuture(this.sessionSourceScanFuture);
		this.sessionSourceScanFuture = null;
		stopFuture(this.pageTaskCommitFuture);
		this.pageTaskCommitFuture = null;
		stopFuture(this.resultTraverseWorkerFuture);
		this.resultTraverseWorkerFuture = null;

	}

	private void stopFuture(Future<?> future) {
		if (future != null) {
			future.cancel(true);
		}
	}

	public void close() {
		stopFuture(this.localTaskRecycleFuture);
		this.localTaskRecycleFuture = null;
		stopFuture(this.remoteTaskRecycleFuture);
		this.remoteTaskRecycleFuture = null;

		this.localTaskRecycleService.shutdown();
		this.remoteTaskRecycleService.shutdown();

		this.sessionSourceInitService.shutdown();
		this.sessionSourceScanService.shutdown();
		this.pageTaskCommitService.shutdown();
		this.resultTraverseWorkerService.shutdown();
		finishAdvertisement();
	}

	private RMIRegistryMulticastAdvertisingService createAdvertisingService(
			long sessionID,
			RemoteTaskTracker<OMRPageTask, SessionSourceServerDispatcher> taskTracker,
			long key) {
		MulticastNetworkConnection multicastNetworkConnection = taskTracker.getMulticastNetworkConnection();
		int rmiPort = taskTracker.getRMIPort();
		if (multicastNetworkConnection != null) {
			try {
				RMIRegistryMulticastAdvertisingService svc = new RMIRegistryMulticastAdvertisingService(
						multicastNetworkConnection,
						key,
						sessionID,
						MarkReaderConstants.ADVERTISE_SERVICE_THREAD_PRIORITY,
						rmiPort,
						MarkReaderConstants.SESSION_SOURCE_ADVERTISE_DELAY_IN_SEC) {

					@Override
					public void logAdvertisement() {
						//SoundManager.getInstance().play("pi78.wav");
					}
				};
				return svc;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public String getRMIBindingName() {
		return rmiBindingName;
	}

	private void startAdvertisement() {
		try {
			if (this.advertisingService != null && MarkReaderConfiguration.isEnabled(MarkReaderConfiguration.KEY_PARALLEL)) {
				String bindingName = getRMIBindingName();
				this.advertisingService.startAdvertising(dispatcher.getLocalServer(), bindingName);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void stopAdvertisement() {
		if (this.advertisingService != null) {
			this.advertisingService.stopAdvertising(rmiBindingName);
			this.advertisingService = null;
		}
	}

	private void finishAdvertisement() {
		stopAdvertisement();
	}
}
