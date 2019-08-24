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
import java.util.concurrent.Callable;

import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.session.traverse.TraverseEventGenerator;
import net.sqs2.omr.session.traverse.TraverseStopException;

public class ResultTraverseCommand implements Callable<Void>{
	
	SessionSource sessionSource;
	
	public ResultTraverseCommand(SessionSource sessionSource){
		this.sessionSource = sessionSource;
	}
	
	public Void call()throws SessionStopException{
		try{
			TraverseEventGenerator generator = new TraverseEventGenerator(this.sessionSource);
			generator.addTraverseEventListeners( new ResultTraverseEventListenersFactory(ResultTraverseEventListenersFactory.ModuleSetType.DEFAULT).create());
			generator.call();
		}catch(TraverseStopException ex){
			throw new SessionStopException(this.sessionSource.getRootDirectory(), ex);
		}catch(IOException ex){
			throw new SessionStopException(this.sessionSource.getRootDirectory(), ex);
		}
		return null;
	}
}