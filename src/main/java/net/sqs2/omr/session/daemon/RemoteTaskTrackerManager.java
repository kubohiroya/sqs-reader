/**
 * 
 */
package net.sqs2.omr.session.daemon;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.sqs2.lang.GroupThreadFactory;
import net.sqs2.omr.model.OMRPageTask;

class RemoteTaskTrackerManager <T extends OMRPageTask, D extends ServerDispatcher>{
	Map<String, TaskTracker<T,D>> taskTrackerMap;
	ScheduledExecutorService executorService;
	
	RemoteTaskTrackerManager(Map<String, TaskTracker<T,D>> remoteTaskTrackerMap) {
		this(remoteTaskTrackerMap, 60);
	}

	RemoteTaskTrackerManager(Map<String, TaskTracker<T,D>> remoteTaskTrackerMap, int executionDelayInSec) {
		this.taskTrackerMap = remoteTaskTrackerMap;
		executorService = Executors.newSingleThreadScheduledExecutor(new GroupThreadFactory("RemoteExecutor", Thread.NORM_PRIORITY, true));
		executorService.scheduleWithFixedDelay(new Runnable() {
					public void run() {
						cleanupConnections();
					}
				}, executionDelayInSec, executionDelayInSec, TimeUnit.SECONDS);
	}

	private void cleanupConnections() {
		for (Map.Entry<String, TaskTracker<T,D>> e : this.taskTrackerMap.entrySet()) {
			String uri = e.getKey();
			TaskTracker<T,D> taskTracker = e.getValue();
			if (taskTracker.isConnected() == false) {
				this.taskTrackerMap.remove(uri);
				Logger.getLogger("executor").info("Remove old taskTracker=" + uri);
			}
		}
	}
	
	public void shutdown(){
		executorService.shutdown();
	}
}
