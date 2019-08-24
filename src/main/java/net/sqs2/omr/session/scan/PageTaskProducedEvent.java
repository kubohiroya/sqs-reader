/*
 * 

  PageTaskSourceProducerMonitor.java

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
 */
package net.sqs2.omr.session.scan;

import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.SessionSource;

public class PageTaskProducedEvent extends SessionSourceScanEvent{

	public static final int NEW_TASK = 0;
	public static final int COMPLETED_TASK = 1;
	public static final int ERROR_TASK_EXECUTION_REQUIRED =21;
	public static final int ERROR_TASK_RESERVED = 3;
	public static final int IGNORABLE_TASK = 4;
	
	private static final long serialVersionUID = 1L;
	PageTask pageTask;
	int status;
	
	public PageTaskProducedEvent(SessionSource source, PageTask pageTask, int status){
		super(source);
		this.pageTask = pageTask;
		this.status = status;
	}

	public PageTask getPageTask() {
		return pageTask;
	}
	
	public int getStatus(){
		return this.status;
	}
}
