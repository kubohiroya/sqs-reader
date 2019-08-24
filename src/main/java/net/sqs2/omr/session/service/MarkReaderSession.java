/*

 MarkReaderSession.java


 Copyright 2009 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

package net.sqs2.omr.session.service;

import java.io.File;

import java.io.IOException;
import java.util.EventObject;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sqs2.event.EventSource;
import net.sqs2.lang.GroupThreadFactory;
import net.sqs2.omr.master.FormMasterException;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.PageTaskHolder;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourcePhase;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.session.commit.PageTaskCommitRowGenerateService;
import net.sqs2.omr.session.commit.PageTaskCommittedEvent;
import net.sqs2.omr.session.daemon.RemoteTaskTracker;
import net.sqs2.omr.session.daemon.SessionSourceServerDispatcher;
import net.sqs2.omr.session.init.SessionSourceInitEvent;
import net.sqs2.omr.session.init.SessionSourceInitException;
import net.sqs2.omr.session.init.SessionSourceInitService;
import net.sqs2.omr.session.scan.SessionSourceScanEvent;
import net.sqs2.omr.session.scan.SessionSourceScannerTaskProducer;
import net.sqs2.omr.ui.util.ObservableObject;
import net.sqs2.store.ObjectStore.ObjectStoreException;

public class MarkReaderSession{

	private ExecutorService executorService;
	private Future<?> sessionFuture;
	private SessionDaemons sessionDaemons;
	private SessionSource sessionSource = null;
	private SessionSourceServerDispatcher sessionSourceServerDispatcher;

	private RemoteTaskTracker<OMRPageTask, SessionSourceServerDispatcher> taskTracker;
	
	private File sourceDirectoryRootFile;
	boolean enableSearchPageMasterFromAncestorDirectory = false;

	private PageTaskHolder pageTaskHolder;
	
	private EventSource<SessionEvent> sessionEventSource;
	private EventSource<SessionSourceInitEvent> sessionSourceInitializationEventSource;
	private EventSource<SessionSourceScanEvent> sessionSourceScanEventSource;
	private EventSource<PageTaskCommittedEvent> pageTaskCommitedEventSource;
	private EventSource<EventObject> sessionErrorEventSource;
	//private EventSource<ResultEvent> resultEventSource = new EventSource<ResultEvent>();
	
	protected PageTaskCommitRowGenerateService pageTaskCommitService;

	long timeStarted;
	long timeFinished;

	ObservableObject<Float> progressRate = new ObservableObject<Float>(-0.1f);
	
	public MarkReaderSession(File sourceDirectoryRoot,
			boolean enableSearchPageMasterFromAncestorDirectory,
			RemoteTaskTracker<OMRPageTask, SessionSourceServerDispatcher> taskTracker,
			SessionSourceServerDispatcher sessionSourceServerDispatcher)
			throws IOException {

		this.sessionEventSource = new EventSource<SessionEvent>();
		this.sessionSourceInitializationEventSource = new EventSource<SessionSourceInitEvent>();
		this.sessionSourceScanEventSource = new EventSource<SessionSourceScanEvent>();
		this.sessionErrorEventSource = new EventSource<EventObject>();
		this.pageTaskCommitedEventSource = new EventSource<PageTaskCommittedEvent>();
		
		this.sessionSource = SessionSourceManager.createInstance(sourceDirectoryRoot);
		
		this.sourceDirectoryRootFile = sourceDirectoryRoot;
		this.enableSearchPageMasterFromAncestorDirectory = enableSearchPageMasterFromAncestorDirectory;

		this.taskTracker = taskTracker;
		this.sessionSourceServerDispatcher = sessionSourceServerDispatcher;
		this.pageTaskHolder = new PageTaskHolder();
		this.pageTaskCommitService = new PageTaskCommitRowGenerateService(this);
		
		this.timeStarted = -1;
		this.timeFinished = -1;
	}
	
	public File getSourceDirectoryRootFile() {
		return this.sourceDirectoryRootFile;
	}

	public SessionSource getSessionSource() {
		return this.sessionSource;
	}

	public long getSessionID() {
		return this.sessionSource.getSessionID();
	}

	public long getKey() {
		return this.sessionSourceServerDispatcher.getKey();
	}

	public PageTaskHolder getTaskHolder() {
		return this.pageTaskHolder;
	}

	public EventSource<SessionSourceInitEvent> getSessionSourceInitializationEventSource(){
		return this.sessionSourceInitializationEventSource;
	}

	public EventSource<SessionSourceScanEvent> getSessionSourceScanEventSource() {
		return this.sessionSourceScanEventSource;
	}

	public EventSource<PageTaskCommittedEvent> getPageTaskCommitedEventSource() {
		return this.pageTaskCommitedEventSource;
	}

	public EventSource<SessionEvent> getSessionEventSource(){
		return this.sessionEventSource;
	}

	public EventSource<EventObject> getSessionErrorEventSource(){
		return this.sessionErrorEventSource;
	}

	public long getTimeStarted() {
		return timeStarted;
	}

	public long getTimeFinished() {
		return timeFinished;
	}

	public ObservableObject<Float> getProgressRate(){
		return progressRate;
	}

	public void setSessionSourcePhase(int categoryIndex, SessionSourcePhase.Phase phase){
		SessionSourcePhase sessionSourcePhase = this.sessionSource.getSessionSourcePhase();
		sessionSourcePhase.setPhase(categoryIndex, phase);
		this.sessionEventSource.fireEvent(new SessionEvent(this.sessionSource.getSessionID(), sessionSourcePhase));
	}
	
	public SessionSourcePhase getSessionSourcePhase(){
		if(this.sessionSource != null){
			return this.sessionSource.getSessionSourcePhase();
		}
		return null;
	}

	public boolean isRunning(){
		if(this.sessionSource == null){
			return false;
		}
		return this.sessionSource.getSessionSourcePhase().getSessionRunningPhase() == SessionSourcePhase.Phase.doing;
	}

	public synchronized void startSession(boolean isDaemon){
		setSessionSourcePhase(SessionSourcePhase.SESSION_RUNNING_CATEGORY_INDEX, SessionSourcePhase.Phase.doing);
		this.sessionDaemons = new SessionDaemons(this.sessionSource.getSessionID(), this.taskTracker, this.sessionSourceServerDispatcher, this.pageTaskHolder);
		this.sessionDaemons.start(isDaemon);
		this.executorService = Executors.newCachedThreadPool(new GroupThreadFactory("MarkReaderSession", Thread.NORM_PRIORITY, true));
		this.sessionFuture = executorService.submit(new MarkReaderSessionWorker());// async execution call 
	}

	public synchronized void stopSession() {
		closeSession();
		setSessionSourcePhase(SessionSourcePhase.SESSION_RUNNING_CATEGORY_INDEX, SessionSourcePhase.Phase.stop);
	}
	
	public synchronized void finishSession() throws IOException{
		closeSession();
		setSessionSourcePhase(SessionSourcePhase.SESSION_RUNNING_CATEGORY_INDEX, SessionSourcePhase.Phase.done);
	}
	
	public synchronized void clear()throws ObjectStoreException{
		if(this.sessionSource != null){
			this.sessionSource.clearCache();
		}
		this.pageTaskHolder.clear();
	}

	public synchronized void closeSessionSource(boolean clearResult) {
		try{
			if(this.sessionSource != null){
				SessionSourceManager.close(this.sessionSource);
				this.sessionSource.getSessionSourcePhase().reset();
				if(clearResult){
					this.sessionSource.removeResultDirectories();
				}
				MarkReaderSessionManager.remove(this.sourceDirectoryRootFile);
				this.executorService.shutdown();
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	public synchronized void notifyStoringTask(PageTask pageTask) {
		this.pageTaskCommitedEventSource.fireEvent(new PageTaskCommittedEvent(this, pageTask));
	}

	private void initializeSessionSource(boolean enableSearchPageMasterFromAncestorDirectory) throws IOException, OMRProcessorException,
			SessionSourceInitException, FormMasterException, SessionStopException,
			InterruptedException, ExecutionException {
		setSessionSourcePhase(SessionSourcePhase.INITIALIZING_CATEGORY_INDEX, SessionSourcePhase.Phase.doing);
		SessionSourceInitService sessionSourceInitializeCommand = new SessionSourceInitService(sessionSource, sessionSourceInitializationEventSource, enableSearchPageMasterFromAncestorDirectory);
		Future<Integer> future = this.sessionDaemons.executeSessionSourceInit(sessionSourceInitializeCommand);
		int numOfImagesFound = future.get();
		Logger.getLogger(this.getClass().getName()).info("NumOfImagesFound: "+numOfImagesFound);
		setSessionSourcePhase(SessionSourcePhase.INITIALIZING_CATEGORY_INDEX, SessionSourcePhase.Phase.done);
		SessionSourceManager.putInstance(this.sessionSource);
	}

	private void scanSessionSource() throws IOException, InterruptedException, ExecutionException {
		setSessionSourcePhase(SessionSourcePhase.SCANNING_CATEGORY_INDEX, SessionSourcePhase.Phase.doing);
		SessionSourceScannerTaskProducer taskProducer = new SessionSourceScannerTaskProducer(this.sessionSource, this.sessionSourceScanEventSource, this.pageTaskHolder);
		Future<Integer> sourceScanFuture = this.sessionDaemons.executeSessionSourceScanning(taskProducer);
		sourceScanFuture.get();
		setSessionSourcePhase(SessionSourcePhase.SCANNING_CATEGORY_INDEX, SessionSourcePhase.Phase.done);
		sessionSourceServerDispatcher.publish();
	}

	private int receiveAllPageTaskResults() throws IOException, InterruptedException, ExecutionException {
		this.pageTaskCommitService.setup(this.sourceDirectoryRootFile);
		Future<Integer> taskCommitFuture = this.sessionDaemons.executePageTaskCommitService(this.pageTaskCommitService); 
		return taskCommitFuture.get();
	}

	private void traverseResult() throws IOException, InterruptedException, ExecutionException {
		setSessionSourcePhase(SessionSourcePhase.EXPORTING_CATEGORY_INDEX, SessionSourcePhase.Phase.doing);
		ResultTraverseCommand resultTraverseService = new ResultTraverseCommand(this.sessionSource);
		Future<?> resultTraverseFuture = this.sessionDaemons.executeResultTraverseWorkerService(resultTraverseService);
		resultTraverseFuture.get();
		setSessionSourcePhase(SessionSourcePhase.EXPORTING_CATEGORY_INDEX, SessionSourcePhase.Phase.done);
	}

	private void closeSession() {
		this.pageTaskHolder.stop();
		if(this.sessionDaemons != null){
			this.sessionDaemons.stop();
			this.sessionDaemons.close();
			this.sessionDaemons = null;
		}
		if (this.sessionFuture != null) {
			this.sessionFuture.cancel(true);
		}
	}

	class MarkReaderSessionWorker implements Runnable{
		
		MarkReaderSessionWorker(){
		}
		
		public void run() {
			try {
				try{
					
					timeStarted =  System.currentTimeMillis();
					initializeSessionSource(enableSearchPageMasterFromAncestorDirectory);
					
					scanSessionSource();
					
					receiveAllPageTaskResults();
				
					traverseResult();
					
					timeFinished =  System.currentTimeMillis();
					finishSession();
					
					return;
					
				} catch (ExecutionException ex) {
					throw ex.getCause();
				}
				
			} catch (SessionSourceInitException e) {
				sessionSourceInitializationEventSource.fireEvent(e.getSessionSourceInitEvent());
				e.printStackTrace();
			} catch (SessionStopException e) {
				e.printStackTrace();
			} catch (CancellationException ignore) {
			} catch (IOException ignore) {
				ignore.printStackTrace();
			} catch (InterruptedException ignore) {
				ignore.printStackTrace();
			} catch (OMRProcessorException ignore) {
				ignore.printStackTrace();
			} catch (Throwable ignore) {
				ignore.printStackTrace();
			}
		
			stopSession();
		
			return;
		}
	}
}