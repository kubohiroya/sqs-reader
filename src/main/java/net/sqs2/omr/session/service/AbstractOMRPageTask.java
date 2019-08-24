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

import java.rmi.RemoteException;

import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.session.daemon.ServerDispatcher;

public abstract class AbstractOMRPageTask<T extends OMRPageTask, D extends ServerDispatcher>{

	protected T task;
	protected D dispatcher;

	public AbstractOMRPageTask(T task, D dispatcher){
		this.task = task;
		this.dispatcher = dispatcher;
	}

	public T getOMRPageTask() {
		return this.task;
	}

	public D getDispatcher() {
		return this.dispatcher;
	}
	
	public void emit() throws RemoteException{
		long key = getDispatcher().getKey();
		long sessionID = this.getOMRPageTask().getSessionID();
		dispatcher.getServer().submitPageTask(key, sessionID, this.getOMRPageTask());
	}
	
	public abstract void execute();

}