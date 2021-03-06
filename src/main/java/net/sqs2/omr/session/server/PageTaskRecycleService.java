/*

 PageTaskRecycleTask.java

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
package net.sqs2.omr.session.server;

import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.PageTaskHolder;

abstract class PageTaskRecycleService implements Runnable{
	
	PageTaskHolder pageTaskHolder;
	PageTaskRecycleService(PageTaskHolder pageTaskHolder){
		this.pageTaskHolder = pageTaskHolder;
	}
	
	abstract PageTask getExpiredPageTask()throws InterruptedException;
	
	public void run() {
		try {
			PageTask task;
			while ((task = getExpiredPageTask()) != null) {
				this.pageTaskHolder.addPreparedToExecTask(task);
			}
		} catch (InterruptedException ignore) {
		}
	}
}