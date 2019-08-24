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
import java.util.logging.Logger;

import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.OMRProcessorSource;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.session.daemon.SessionSourceServerDispatcher;
import net.sqs2.omr.session.exec.OMRProcessor;
import net.sqs2.sound.SoundManager;


public class PeerExecutableOMRPageTask extends AbstractOMRPageTask<OMRPageTask, SessionSourceServerDispatcher> {
	
	public PeerExecutableOMRPageTask(OMRPageTask task, SessionSourceServerDispatcher dispatcher) {
		super(task, dispatcher);
	}
	
	@Override
	public void execute() {
		long baseTimeStamp = System.currentTimeMillis();
		long laptime = baseTimeStamp;
		try{

			OMRProcessorSource omrProcessorSource = new NetworkOMRProcessorSourceFactory(task, dispatcher).call();
			
			new OMRProcessor(task, omrProcessorSource).call();
			
			laptime = System.currentTimeMillis() - baseTimeStamp;
		}catch(ConfigSchemeException e){
			task.setErrorModel(new OMRProcessorErrorModel(task.getPageID(), e.getLocalizedMessage()));
		}catch(IOException e){
			task.setErrorModel(new OMRProcessorErrorModel(task.getPageID(), e.getLocalizedMessage()));}
		
		if(task.getResult() != null){
			logSucceed(laptime, task);
		}else{
			logFailure(task);
		}
	}
	
	static protected void logSucceed(long laptime, PageTask pageTask) {
		Logger.getLogger("executor").info("[[Process OK in " + laptime + " msec]]\t" + pageTask);
		SoundManager.getInstance().play(AppConstants.TASK_EXECUTION_SOUND_FILENAME);
	}
	
	static protected void logFailure(PageTask pageTask) {
		Logger.getLogger("executor").info("[[Process NG]]\t" + pageTask+"\tresult="+pageTask.getResult()+"\terror="+pageTask.getErrorModel());
		SoundManager.getInstance().play(AppConstants.TASK_EXECUTION_ERROR_SOUND_FILENAME);
	}


}