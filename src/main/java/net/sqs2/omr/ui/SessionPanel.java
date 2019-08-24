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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class SessionPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	SessionModel sessionModel;

	SourceDirectoryPanel sourceDirectoryPanel;
	ContentPanel contentPanel;

	SessionPanel(SessionModel sessionModel) {
		this.sessionModel = sessionModel;

		sourceDirectoryPanel = new SourceDirectoryPanel(sessionModel);
		contentPanel = new ContentPanel(sessionModel);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, sourceDirectoryPanel, contentPanel);
		splitPane.setResizeWeight(0.5);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
	}
	
	public SessionModel getSessionModel(){
		return this.sessionModel;
	}

	public ContentPanel getContentPanel(){
		return contentPanel;
	}
	
	public SourceDirectoryPanel getSourceDirectoryPanel(){
		return sourceDirectoryPanel;
	}

}
