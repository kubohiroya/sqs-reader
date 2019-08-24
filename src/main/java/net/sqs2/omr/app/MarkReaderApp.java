/*

 MarkReaderApp.java

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

 Created on 2007/01/11

 */
package net.sqs2.omr.app;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.sqs2.event.EventListener;
import net.sqs2.net.RMIRegistryUtil;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.CacheConstants;
import net.sqs2.omr.model.MarkReaderConstants;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.SessionSourcePhase;
import net.sqs2.omr.session.daemon.RemoteExecutorManager;
import net.sqs2.omr.session.daemon.RemoteTaskTracker;
import net.sqs2.omr.session.daemon.SessionSourceServerDispatcher;
import net.sqs2.omr.session.daemon.TaskTracker;
import net.sqs2.omr.session.exec.OMRPageTaskExecutorDaemon;
import net.sqs2.omr.session.server.RemoteSessionSourceServer;
import net.sqs2.omr.session.server.SessionSourceServerDispatcherImpl;
import net.sqs2.omr.session.server.SessionSourceServerImpl;
import net.sqs2.omr.session.service.AbstractOMRPageTask;
import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.omr.session.service.MarkReaderSessionManager;
import net.sqs2.omr.session.service.PeerExecutableOMRPageTask;
import net.sqs2.omr.session.service.SessionEvent;

public class MarkReaderApp{
	
	private int rmiPort;
	private int numUsingProcessors;
	private SessionSourceServerDispatcher localDispatcher;
	private OMRPageTaskExecutorDaemon<OMRPageTask, SessionSourceServerDispatcher> taskExecutorDaemon;
	private RemoteTaskTracker<OMRPageTask, SessionSourceServerDispatcher> remoteTaskTracker;
	private TaskTracker<OMRPageTask, SessionSourceServerDispatcher> localTaskTracker;
		
	public static final String SESSION_SERVICE_PATH = '/'+AppConstants.APP_ID + '/'+ MarkReaderConstants.SESSION_SERVICE_NAME;

	public class PageTaskTracker extends TaskTracker<OMRPageTask, SessionSourceServerDispatcher>{
		
		public PageTaskTracker(SessionSourceServerDispatcher dispatcher){
			super(dispatcher);
		}
		
		@Override
		protected AbstractOMRPageTask<OMRPageTask, SessionSourceServerDispatcher> pollPreExecutionTask()throws RemoteException, InterruptedException{
			OMRPageTask pageTask = (OMRPageTask)this.dispatcher.getServer().leaseTask(getKey());
			if(pageTask == null){
				return null;
			}
			return new PeerExecutableOMRPageTask(pageTask, this.getDispatcher());
		}

		@Override
		protected void execute(AbstractOMRPageTask<OMRPageTask, SessionSourceServerDispatcher> executable) throws RemoteException, InterruptedException{
			taskExecutorDaemon.getPreExecutionTaskQueue().put(executable);
		}

		@Override
		protected AbstractOMRPageTask<OMRPageTask, SessionSourceServerDispatcher> pollPostExecutionTask()
				throws InterruptedException {
			return taskExecutorDaemon.getPostExecutionTaskQueue().poll(100, TimeUnit.MILLISECONDS);
		}
		
	}
	
	public MarkReaderApp(int rmiPort) throws UnknownHostException, IOException {
		this(rmiPort, true);
	}
	
	public MarkReaderApp(int rmiPort, boolean isLocalTaskExecutionEnabled) throws UnknownHostException, IOException {
		this.rmiPort = rmiPort;
		this.numUsingProcessors = Math.max(1, Math.min(1, Runtime.getRuntime().availableProcessors() - 1));
		long key = new Random().nextLong();
		Logger.getLogger(getClass().getName()).info("MarkReaderController key=" + key);

		System.setProperty("java.rmi.server.hostname", getHostAddress());
		
		RemoteSessionSourceServer sessionSourceServer = SessionSourceServerImpl.createInstance(key, MarkReaderConstants.CLIENT_TIMEOUT_SEC);
		this.localDispatcher = new SessionSourceServerDispatcherImpl(sessionSourceServer, null, key);
		this.taskExecutorDaemon = new OMRPageTaskExecutorDaemon<OMRPageTask, SessionSourceServerDispatcher>();
		
		this.remoteTaskTracker = createRemoteTaskTracker(this.rmiPort);
		
		if(isLocalTaskExecutionEnabled){
			this.localTaskTracker = createLocalTaskTracker(this.localDispatcher);
		}
	}

	private TaskTracker<OMRPageTask, SessionSourceServerDispatcher> createLocalTaskTracker(SessionSourceServerDispatcher localDispatcher) {
		return new PageTaskTracker(localDispatcher);
	}

	private RemoteTaskTracker<OMRPageTask, SessionSourceServerDispatcher> createRemoteTaskTracker(int rmiPort) {
		return new RemoteTaskTracker<OMRPageTask, SessionSourceServerDispatcher>(rmiPort, MarkReaderApp.SESSION_SERVICE_PATH){
			@Override
			public RemoteExecutorManager<OMRPageTask, SessionSourceServerDispatcher> createRemoteExecutorManager() {
				return new RemoteExecutorManager<OMRPageTask, SessionSourceServerDispatcher>(numUsingProcessors){
					public TaskTracker<OMRPageTask, SessionSourceServerDispatcher>
							createTaskTracker(RemoteSessionSourceServer remoteSessionServer,
							long remoteKey){
						SessionSourceServerDispatcher remoteDispacher = new SessionSourceServerDispatcherImpl(null, remoteSessionServer, remoteKey);
						return new PageTaskTracker(remoteDispacher);
					}
				};
			}
		};
	}	

	private String getHostAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ex) {
			return "127.0.0.1";
		}
	}

	public int getRMIPort() {
		return this.rmiPort;
	}

	private void unexportSessionService() {
		try {
			SessionSourceServerImpl sessionSourceServer = SessionSourceServerImpl.getInstance();
			if (sessionSourceServer != null) {
				sessionSourceServer.close();
				UnicastRemoteObject.unexportObject(sessionSourceServer, false);
			}
		} catch (RemoteException ignore) {
		}
	}
	
	BlockingQueue<Boolean> queue;
	
	public void open(String[] args) throws IOException{
		 this.queue = (0 < args.length)? new ArrayBlockingQueue<Boolean>(args.length) : null;
		 
		for (String filename: args) {
			final File sourceDirectoryRoot = new File(filename);
			boolean enableSearchPageMasterFromAncestorDirectory = false;
			final MarkReaderSession session = createSession(sourceDirectoryRoot, enableSearchPageMasterFromAncestorDirectory);
			
			session.getSessionEventSource().addListener(new EventListener<SessionEvent>() {
				@Override
				public void eventFired(SessionEvent event) {
					SessionSourcePhase.Phase phase = session.getSessionSourcePhase().getSessionRunningPhase();
					if(phase == SessionSourcePhase.Phase.done || phase == SessionSourcePhase.Phase.stop || phase == SessionSourcePhase.Phase.fail){
						MarkReaderSessionManager.remove(sourceDirectoryRoot);
						queue.add(Boolean.TRUE);
					}
				}
			});
			session.startSession(true);		
		}
	}
	
	public void waitUntilAllSessionStopped(){
		try{
			while(0 < (MarkReaderSessionManager.getSessions().size())){
				while(queue.poll(1, TimeUnit.SECONDS) == null){
				}
			}
		}catch(InterruptedException ignore){
		}
	} 


	public synchronized MarkReaderSession createSession(File sourceDirectoryRoot, boolean enableSearchPageMasterFromAncestorDirectory ) throws IOException{
		if (sourceDirectoryRoot == null || ! sourceDirectoryRoot.isDirectory()){
			throw new IOException("DIRECTORY IO ERROR in: " + sourceDirectoryRoot);
		}
		if(sourceDirectoryRoot.getName().endsWith(CacheConstants.CACHE_ROOT_DIRNAME)){
			throw new IOException("DIRECTORY INVALID:"+sourceDirectoryRoot);
		}
		if(MarkReaderSessionManager.contains(sourceDirectoryRoot)){
			throw new IOException("DIRECTORY ALREADY EXISTS:"+sourceDirectoryRoot);
		}
		
		return MarkReaderSessionManager.create(sourceDirectoryRoot, enableSearchPageMasterFromAncestorDirectory,
				this.remoteTaskTracker, this.localDispatcher);
	}

	public void clearSession(File sourceDirectoryRoot) throws IOException{
		MarkReaderSession session = MarkReaderSessionManager.get(sourceDirectoryRoot);
		if(session == null){
			return;
		}
		session.clear();
	}

	public synchronized void stopSession(File sourceDirectoryRoot){
		MarkReaderSession session = MarkReaderSessionManager.get(sourceDirectoryRoot);
		if(session != null){
			session.stopSession();
		}
	}

	public synchronized void closeSessionSource(File sourceDirectoryRoot) {
		MarkReaderSession session = MarkReaderSessionManager.get(sourceDirectoryRoot);
		if(session != null){
			session.closeSessionSource(false);
		}
	}

	public synchronized void shutdown() {
		try {
			if (RMIRegistryUtil.unexport(this.remoteTaskTracker.getRMIPort(), this.remoteTaskTracker.getRMIBindingName())) {
				//Logger.getLogger("net").log(Level.WARNING, "RMI registry disabled.");
			}
			
		}catch(UnknownHostException ex){
			ex.printStackTrace();
			Logger.getLogger(getClass().getName()).warning(ex.getMessage());
		} catch (RemoteException ex) {
			ex.printStackTrace();
			Logger.getLogger(getClass().getName()).warning(ex.getMessage());
		} catch (SocketException ex) {
			//ex.printStackTrace();
			Logger.getLogger(getClass().getName()).warning(ex.getMessage());
		}
		
		Collection<MarkReaderSession> sessions = MarkReaderSessionManager.getSessions();
		Iterator<MarkReaderSession> sessionsIterator = sessions.iterator();
		MarkReaderSession[] sessionsArray = new MarkReaderSession[sessions.size()];

		for (int i = 0; i < sessions.size(); i++) {
			sessionsArray[i] = sessionsIterator.next();
		}
		for (MarkReaderSession session : sessionsArray) {
			session.stopSession();
			session.closeSessionSource(false);
		}
		this.remoteTaskTracker.shutdown();
		if(this.localTaskTracker != null){
			this.localTaskTracker.stop();
			this.localTaskTracker.shutdown();
		}
		unexportSessionService();
		SessionSourceServerImpl.shutdown();
		
		this.taskExecutorDaemon.shutdown(); 
	}

}
