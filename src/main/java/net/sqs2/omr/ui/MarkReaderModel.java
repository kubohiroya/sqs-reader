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
package net.sqs2.omr.ui;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import net.sqs2.omr.app.MarkReaderApp;
import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.omr.ui.util.ObservableObject;

// ************* Model ***************** //
public class MarkReaderModel{
	
	public static long sessionID = 0L;
	public static final String APPNAME = "MarkReader";
	public static final String VERSION = "2.1.3";

	MarkReaderApp app;

	ArrayList<SessionModel> sessionModelList;
	
	ObservableObject<MarkReaderMenuState> menuState;
	
	public MarkReaderModel(MarkReaderApp app) throws UnknownHostException, IOException{
		this.app = app;
		this.sessionModelList = new ArrayList<SessionModel>();
		this.menuState = new ObservableObject<MarkReaderMenuState>(new MarkReaderMenuState());
	}
	
	public String getApplicationName(){
		return APPNAME;
	}
	
	public String getVersion(){
		return VERSION;
	}
	
	public ObservableObject<MarkReaderMenuState> getMenuState(){
		return menuState;
	}
	
	public SessionModel createSessionModel(File rootDirectory, boolean enableSearchPageMasterFromAncestorDirectory)throws IOException{
		MarkReaderSession session = app.createSession(rootDirectory, enableSearchPageMasterFromAncestorDirectory);
		SessionModel sessionModel = new SessionModel(this,session);
		this.sessionModelList.add(sessionModel);
		return sessionModel; 
	}
	
	public void removeSessionModel(SessionModel sessionModel){
		this.sessionModelList.remove(sessionModel);
	}
	
	public SessionModel getPredefinedSessionModel(File sourceDirectoryRoot) {
		for (SessionModel sessionModel : this.sessionModelList) {
			File tabRootDirectory = sessionModel.getMarkReaderSession().getSourceDirectoryRootFile();
			if (sourceDirectoryRoot.getAbsoluteFile().equals(
					tabRootDirectory.getAbsoluteFile())) {
				return sessionModel;
			}
		}
		return null;
	}

}