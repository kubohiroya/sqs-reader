package net.sqs2.omr.session.exec;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sqs2.lang.GroupThreadFactory;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.session.daemon.ServerDispatcher;
import net.sqs2.omr.session.service.AbstractOMRPageTask;

public class OMRPageTaskExecutorDaemon<T extends OMRPageTask, D extends ServerDispatcher> {
	
	public static final int TASK_EXECUTOR_THREAD_PRIORIY = Thread.NORM_PRIORITY;
	
	private  GroupThreadFactory executorGroupThreadFactory;
	private ScheduledExecutorService executorThreadPool;
	private Future<?>[] executorFutures = null;

	private ArrayBlockingQueue<AbstractOMRPageTask<T,D>> preExecutionTaskQueue;
	private ArrayBlockingQueue<AbstractOMRPageTask<T,D>> postExecutionTaskQueue;

	int numExecutorThreads;

	boolean isRunning = false;
	
	public OMRPageTaskExecutorDaemon() {
		this.numExecutorThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
		this.preExecutionTaskQueue = new ArrayBlockingQueue<AbstractOMRPageTask<T,D>>(this.numExecutorThreads);
		this.postExecutionTaskQueue = new ArrayBlockingQueue<AbstractOMRPageTask<T,D>>(this.numExecutorThreads);
		start();
	}

	void start() {
		this.isRunning = true;
		if(this.executorGroupThreadFactory == null){
			this.executorGroupThreadFactory = new GroupThreadFactory("OMRPageTaskExecutorDaemon", TASK_EXECUTOR_THREAD_PRIORIY, true);
		}
		if(this.executorThreadPool == null){
			this.executorThreadPool = Executors.newScheduledThreadPool(this.numExecutorThreads, executorGroupThreadFactory);
		}
		if (this.executorFutures == null) {
			this.executorFutures = new Future[this.numExecutorThreads];
			Runnable runnable = new Runnable(){
				public void run() {
					try {
						AbstractOMRPageTask<T,D> executable = preExecutionTaskQueue.poll(100, TimeUnit.MILLISECONDS);
						if (executable == null) {
							return;
						}
						executable.execute();
						boolean offer = false;
						do {
							try {
								offer = postExecutionTaskQueue.offer(executable, 100, TimeUnit.MILLISECONDS);
							} catch (InterruptedException ignore) {
							} catch (NullPointerException ignore) {
							}
						} while (OMRPageTaskExecutorDaemon.this.isRunning && !offer);

					} catch (InterruptedException ignore) {
					} catch (RuntimeException ex) {
						ex.printStackTrace();
						/*
					} catch (NoSuchObjectException ignore) {
					} catch (RemoteException ignore) {
						// ignore exception from RMI connection handling. we will drop
						// the uploading task but never stop uploading thread itself.
						// ignore.printStackTrace();*/
					}finally{
						Thread.yield();
					}
				}
			};
			for (int i = 0; i < this.numExecutorThreads; i++) {
				this.executorFutures[i] = this.executorThreadPool.scheduleWithFixedDelay(runnable, 1000 * i / this.numExecutorThreads, 1, TimeUnit.MILLISECONDS);
			}
		}
	}

	public void shutdown() {
		this.isRunning = false;
		this.postExecutionTaskQueue.clear();
		this.preExecutionTaskQueue.clear();

		if (this.executorFutures != null) {
			for (int i = 0; i < this.executorFutures.length; i++) {
				this.executorFutures[i].cancel(true);
			}
			this.executorFutures = null;
		}

		if (this.executorThreadPool != null) {
			this.executorThreadPool.shutdown();
			this.executorThreadPool = null;
		}
	}

	public BlockingQueue<AbstractOMRPageTask<T,D>> getPreExecutionTaskQueue() {
		return this.preExecutionTaskQueue;
	}

	public BlockingQueue<AbstractOMRPageTask<T,D>> getPostExecutionTaskQueue() {
		return this.postExecutionTaskQueue;
	}
}
