/*
  Copyright 2011 KUBO Hiroya (hiroya@cuc.ac.jp).
  
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2011/12/03

 */
package net.sqs2.omr.session.service;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.omr.util.FileContents;
import net.sqs2.util.FileResourceID;

public interface LocalSessionSourceServer extends Remote{
	
	public boolean existsRunningLocalSessions()throws RemoteException;
	
	public long ping(long key) throws RemoteException;

	public PageTask leaseTask(long key) throws RemoteException;

	public FormMaster getFormMaster(long key, long sessionID, FileResourceID fileResourceID) throws RemoteException;

	public SourceDirectoryConfiguration getConfigration(long key, long sessionID, FileResourceID fileResourceID) throws RemoteException;

	public FileContents getFileContentByteArray(long key, long sessionID, FileResourceID fileResourceID) throws RemoteException, IOException;

	public void submitPageTask(long key, long sessionID, PageTask pageTask) throws RemoteException;
}
