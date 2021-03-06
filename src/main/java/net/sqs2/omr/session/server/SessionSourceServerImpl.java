/*

 SessionServiceImpl.java


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
package net.sqs2.omr.session.server;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.sqs2.lang.GroupThreadFactory;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.ContentAccessor;
import net.sqs2.omr.model.FormMasterAccessor;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.PageTaskHolder;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.omr.session.service.MarkReaderSessionManager;
import net.sqs2.omr.util.FileContents;
import net.sqs2.store.ObjectStore.ObjectStoreException;
import net.sqs2.util.FileResourceID;

public class SessionSourceServerImpl extends UnicastRemoteObject implements RemoteSessionSourceServer, Serializable {
	private static final long serialVersionUID = 0L;
	private long key;

	private Map<String, ClientInfo> clientInfoMap = null;
	transient private ScheduledExecutorService clientConnectionManagerService = Executors.newScheduledThreadPool(1, new GroupThreadFactory("clientConnectionManagerService", Thread.MIN_PRIORITY, true));
	transient private Future<?> clientConnectionManager;
	
	transient private static SessionSourceServerImpl singleton = null;

	class ClientInfo {
		String host;
		int numLease = 0;
		int numSubmit = 0;
		long lastMessageTime = 0L;

		ClientInfo() {
			updateMessageTime();
		}

		public int getNumLease() {
			return this.numLease;
		}

		public int getNumSubmit() {
			return this.numSubmit;
		}

		public void incrementNumLease() {
			this.numLease++;
		}

		public void incrementNumSubmit() {
			this.numSubmit++;
		}

		public void updateMessageTime() {
			this.lastMessageTime = System.currentTimeMillis();
		}

		public long lastMessageTime() {
			return this.lastMessageTime;
		}

		public boolean isOlderThan(long time) {
			return this.lastMessageTime < time;
		}
	}

	public static SessionSourceServerImpl getInstance() throws RemoteException {
		if(singleton == null){
			throw new RuntimeException("SessionSourceServerImpl is not initialized.");
		}
		return singleton;
	}
	
	public static RemoteSessionSourceServer createInstance(long key, final int remoteClientTimeout) throws RemoteException {
		return new SessionSourceServerImpl(key, remoteClientTimeout); 
	}
	
	public static void shutdown(){
		singleton.clientInfoMap.clear();
		singleton.clientConnectionManagerService.shutdown();
		singleton = null;
	}

	private SessionSourceServerImpl(long key, final int remoteClientTimeout) throws RemoteException {
		super();
		singleton = this;	
		this.key = key;
		Logger.getLogger("session").info("Start SessionServiceImpl");
		this.clientInfoMap = Collections.synchronizedSortedMap(new TreeMap<String, ClientInfo>());
		this.clientConnectionManager = this.clientConnectionManagerService.scheduleWithFixedDelay(
				new Runnable() {
					public void run() {
						long time = System.currentTimeMillis() - remoteClientTimeout * 1000;
						for (Map.Entry<String, ClientInfo> entry : SessionSourceServerImpl.this.clientInfoMap.entrySet()) {
							String clientHost = entry.getKey();
							if (entry.getValue().isOlderThan(time)) {
								Logger.getLogger(getClass().getName()).warning("disconnected: " + clientHost);
								SessionSourceServerImpl.this.clientInfoMap.remove(clientHost);
							}
						}

					}
				}, 10, 10, TimeUnit.SECONDS);
	}

	public void close() {
		this.clientConnectionManager.cancel(true);
		this.clientConnectionManagerService.shutdown();
	}

	public long ping(long key) throws RemoteException {
		checkKey(key);
		return System.currentTimeMillis();
	}

	private void checkKey(long key) throws RemoteException {
		if (this.key != key) {
			Logger.getLogger(getClass().getName()).warning(key + " != " + this.key);
			throw new RemoteException("Invalid Key");
		}
	}

	public synchronized PageTask leaseTask(long key) throws RemoteException {
		checkKey(key);
		try {
			String clientHost = null;
			try {
				clientHost = RemoteServer.getClientHost();
				ClientInfo clientInfo = getClientInfo(clientHost);
				clientInfo.incrementNumLease();
			} catch (ServerNotActiveException ex) {
			}

			for (MarkReaderSession session : MarkReaderSessionManager.getSessions()) {
				long sessionID = session.getSessionID();
				PageTaskHolder taskHolder = MarkReaderSessionManager.get(sessionID).getTaskHolder();

				if (! session.isRunning()){
					continue;
				}
				PageTask task = taskHolder.leaseTask(100);
				if (task != null) {
					task.setLeased();
					if (clientHost == null) {
						MarkReaderSessionManager.get(sessionID).getTaskHolder().addLeaseLocalTask(task);
					} else {
						MarkReaderSessionManager.get(sessionID).getTaskHolder().addLeaseRemoteTask(task);
					}
				}
				return task;
			}
			return null;
		} catch (InterruptedException ex) {
			return null;
		}
	}

	public void submitPageTask(long key, long sessionID, PageTask task) throws RemoteException {
		checkKey(key);
		try {
			String clientHost = RemoteServer.getClientHost();
			ClientInfo clientInfo = getClientInfo(clientHost);
			clientInfo.incrementNumSubmit();
			clientInfo.updateMessageTime();
		} catch (ServerNotActiveException ex) {
		}
		MarkReaderSessionManager.get(sessionID).getTaskHolder().submitTask(task);
	}

	private ClientInfo getClientInfo(String clientHost) {
		ClientInfo clientInfo = this.clientInfoMap.get(clientHost);
		if (clientInfo == null) {
			clientInfo = new ClientInfo();
			this.clientInfoMap.put(clientHost, clientInfo);
		}
		return clientInfo;
	}

	public FormMaster getFormMaster(long key, long sessionID, FileResourceID fileResourceID) throws RemoteException {
		checkKey(key);
		SessionSource sessionSource = SessionSourceManager.getInstance(sessionID);
		ContentAccessor contentAccessor = sessionSource.getContentAccessor();
		FormMasterAccessor formMasterAccessor = contentAccessor.getFormMasterAccessor();
		try{
			return formMasterAccessor.get(FormMaster.createKey(fileResourceID.getRelativePath(), fileResourceID.getLastModified()));
		}catch(ObjectStoreException ex){
			throw new RuntimeException(ex);
		}
	}

	public SourceDirectoryConfiguration getConfigration(long key, long sessionID, FileResourceID fileResourceID) throws RemoteException {
		checkKey(key);
		try{
			return SessionSourceManager.getInstance(sessionID).getConfiguration(fileResourceID);
		}catch(IOException ex){
			throw new RuntimeException(ex);
		}catch(ConfigSchemeException ex){
			throw new RuntimeException(ex);
		}
	}

	public FileContents getFileContentByteArray(long key, long sessionID, FileResourceID fileResourceID) throws RemoteException,IOException {
		checkKey(key);
		FileContents fileContentBytes = SessionSourceManager.getInstance(sessionID).getContentAccessor()
				.getFileContentCache().get(fileResourceID);
		return fileContentBytes;
	}

	public long getLastModified(long key, long sessionID, String relativePath) throws RemoteException {
		checkKey(key);
		return SessionSourceManager.getInstance(sessionID).getContentAccessor().getFileContentCache()
				.getLastModified(relativePath);
	}

	public int countRemoteTaskTracker() throws RemoteException {
		return this.clientInfoMap.size();
	}

	public void removeRemoteTaskTracker(String clientHost) throws RemoteException {
		this.clientInfoMap.remove(clientHost);
	}

	public boolean existsRunningLocalSessions() {
		for (MarkReaderSession session : MarkReaderSessionManager.getSessions()) {
			if (session.isRunning()) {
				return true;
			}
		}
		return false;
	}

}
