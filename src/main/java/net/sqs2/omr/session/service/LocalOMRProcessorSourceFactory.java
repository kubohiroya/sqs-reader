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

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorSource;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.util.FileResourceID;

public class LocalOMRProcessorSourceFactory extends AbstractOMRProcessorSourceFactory{

	SessionSource sessionSource;
	
	public LocalOMRProcessorSourceFactory(PageTask task){
		super(task);
		this.sessionSource = SessionSourceManager.getInstance(task.getSessionID());
	}
			
	public OMRProcessorSource call() throws ConfigSchemeException, IOException {
		OMRPageTask omrTask = (OMRPageTask)task;
		PageID pageID = omrTask.getPageID();
		FileResourceID formMasterFileResourceID = omrTask.getFormMasterFileResourceID();
		FormMaster formMaster = sessionSource.getContentAccessor().getFormMasterAccessor().get(formMasterFileResourceID);
		int processingPageIndex = omrTask.getProcessingPageIndex();
		
		FileResourceID configFileResourceID = omrTask.getConfigFileResourceID();
		SourceDirectoryConfiguration sourceDirectoryConfiguration = sessionSource.getConfiguration(configFileResourceID);
		byte[] imageByteArray = sessionSource.getContentAccessor().getFileContentCache().get(pageID.getFileResourceID()).getBytes();
		return new OMRProcessorSource(pageID, imageByteArray, sourceDirectoryConfiguration, formMaster, processingPageIndex);
	}
}