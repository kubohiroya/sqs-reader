/*

 OMRPageTask.java

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
package net.sqs2.omr.model;

import java.io.Serializable;
import java.util.concurrent.Delayed;

import net.sqs2.util.FileResourceID;

public class OMRPageTask extends PageTask implements Serializable{

	private static final long serialVersionUID = 7L;

	protected FileResourceID formMasterFileResourceID;
	protected int processingPageIndex;

	public OMRPageTask() {
	}

	public OMRPageTask(long sessionID, 
			PageID pageID,
			FileResourceID configFileResourceID, 
			FileResourceID formMasterFileResourceID, 
			int processingPageIndex) {
		super(sessionID, pageID, configFileResourceID);
		this.formMasterFileResourceID = formMasterFileResourceID;
		this.processingPageIndex = processingPageIndex;
		this.id = createID();
	}
	
	public String createID(){
		return createID(this.pageID, this.processingPageIndex);
	}

	public static String createID(PageID pageID, int processingPageIndex) {
		return processingPageIndex + "\t" + pageID.createID();
	}

	@Override
	public boolean equals(Object o) {
		try {
			OMRPageTask task = (OMRPageTask) o;
			return this.id.equals(task.id)
					&& this.formMasterFileResourceID.equals(task.formMasterFileResourceID)
					&& this.configFileResourceID.equals(task.configFileResourceID)
					&& this.processingPageIndex == task.processingPageIndex && this.pageID.equals(task.pageID);
		} catch (ClassCastException ignore) {
		}
		return false;
	}

	@Override
	public int compareTo(Delayed o) {
		try {
			OMRPageTask task = (OMRPageTask) o;
			int diff = 0;
			if(task == null){
				return -1;
			}
			if (this.id.equals(task.id)) {
				return 0;
			}
			if ((diff = this.formMasterFileResourceID.compareTo(task.formMasterFileResourceID)) != 0) {
				return diff;
			}
			if ((diff = this.configFileResourceID.compareTo(task.configFileResourceID)) != 0) {
				return diff;
			}
			if ((diff = this.processingPageIndex - task.processingPageIndex) != 0) {
				return diff;
			}
			if ((diff = this.pageID.compareTo(task.pageID)) != 0) {
				return diff;
			}
		} catch (ClassCastException ignore) {
		}
		return 1;
	}
	
	public int getProcessingPageIndex() {
		return this.processingPageIndex;
	}


	public FileResourceID getFormMasterFileResourceID(){
		return this.formMasterFileResourceID;
	}

	public void setFormMasterFileResourceID(FileResourceID formMasterFileResourceID){
		this.formMasterFileResourceID = formMasterFileResourceID;
	}

}
