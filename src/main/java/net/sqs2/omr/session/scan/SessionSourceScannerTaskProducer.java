/**
 *  SessionSourceScannerTaskProducer.java

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
package net.sqs2.omr.session.scan;

import java.io.IOException;
import java.util.Calendar;

import net.sqs2.event.EventSource;
import net.sqs2.omr.model.MarkReaderConstants;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.OMRProcessorResult;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.PageTaskAccessor;
import net.sqs2.omr.model.PageTaskHolder;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.session.service.SessionStopException;
import net.sqs2.store.ObjectStore.ObjectStoreException;
import net.sqs2.util.FileResourceID;

public class SessionSourceScannerTaskProducer extends PageHandlingSessionSourceScanner{

	private EventSource<SessionSourceScanEvent> eventSource;
	private PageTaskHolder pageTaskHolder;

	public SessionSourceScannerTaskProducer(SessionSource sessionSource,
			EventSource<SessionSourceScanEvent> eventSource,
			PageTaskHolder pageTaskHolder) throws IOException {
		super(sessionSource);
		this.eventSource = eventSource;
		this.pageTaskHolder = pageTaskHolder;
	}

	@Override
	public AbstractSessionSourceScannerWorker createWorker() throws IOException {
		return new SessionSourceScannerWorker(MarkReaderConstants.SESSION_SOURCE_NEWFILE_IGNORE_SEC_THRESHOLD_IN_SEC);
	}

	protected class SessionSourceScannerWorker extends
			AbstractSessionSourceScannerWorker {

		private long now;
		private long newFileIgnoreSecThreshold;

		private PageTaskAccessor taskAccessor;

		SessionSourceScannerWorker(long newFileIgnoreSecThreshold) throws IOException {
			
			this.now = Calendar.getInstance().getTimeInMillis();
			this.newFileIgnoreSecThreshold = newFileIgnoreSecThreshold;
			this.taskAccessor = sessionSource.getContentAccessor().getPageTaskAccessor();
		}

		@Override
		void work(SourceDirectory sourceDirectory, PageID pageID, FileResourceID formMasterFileResourceID, int pageIndex)throws SessionStopException {
			
			FileResourceID configFileResourceID = sourceDirectory.getConfiguration().getConfigFileResourceID();
			
			OMRPageTask pageTask = new OMRPageTask(sessionSource.getSessionID(), 
					pageID, configFileResourceID,
					formMasterFileResourceID, pageIndex);

			try {
				PageTask preparedToExecTask = check(pageTask);
				if (preparedToExecTask != null) {
					//this.taskHolder.incrementNumTargetTasks(1);
					this.taskAccessor.put(preparedToExecTask);
					pageTaskHolder.addPreparedToExecTask(preparedToExecTask);
				}
			} catch (ObjectStoreException ex) {
				ex.printStackTrace();
				throw new SessionStopException(sessionSource.getRootDirectory());
			}
		}

		@Override
		void startScanningSourceDirectory(SourceDirectory sourceDirectory) {
		}

		private PageTask check(PageTask task) throws SessionStopException {
			
			if (isConcurrentFileModificationDetected(task)) {
				eventSource.fireEvent(new PageTaskProducedEvent(sessionSource, task, PageTaskProducedEvent.IGNORABLE_TASK));
				return null;
			}

			PageTask storedTask = null;
			try {
				storedTask = this.taskAccessor.get(task.toString());
			} catch (Exception ignore) {
			}

			if (storedTask == null) {
				eventSource.fireEvent(new PageTaskProducedEvent(sessionSource, task, PageTaskProducedEvent.NEW_TASK));
				return task;
			}
			
			OMRProcessorResult pageTaskResult = storedTask.getResult();
					
			if(pageTaskResult != null){
				eventSource.fireEvent(new PageTaskProducedEvent(sessionSource, task, PageTaskProducedEvent.COMPLETED_TASK));
				return null;
			}
			
			OMRProcessorErrorModel pageTaskErrorModel = storedTask.getErrorModel();
			
			if(pageTaskErrorModel == null){
				eventSource.fireEvent(new PageTaskProducedEvent(sessionSource, task, PageTaskProducedEvent.NEW_TASK));
				return task;
			}
			
			if (task.getPageID().getFileResourceID().getLastModified() == storedTask.getPageID().getFileResourceID().getLastModified()) {
				eventSource.fireEvent(new PageTaskProducedEvent(sessionSource, storedTask, PageTaskProducedEvent.ERROR_TASK_RESERVED));
				return null;
			}
			
			
			eventSource.fireEvent(new PageTaskProducedEvent(sessionSource, storedTask, PageTaskProducedEvent.ERROR_TASK_EXECUTION_REQUIRED));
			return task;
		}

		private boolean isConcurrentFileModificationDetected(PageTask task) {
			return this.newFileIgnoreSecThreshold != -1 && (this.now - task.getPageID().getFileResourceID().getLastModified() <= this.newFileIgnoreSecThreshold);
		}

		@Override
		void finishScanning() {
			eventSource.fireEvent(new SessionSourceScanFinishedEvent(sessionSource));
		}
	}
}
