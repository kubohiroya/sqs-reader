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

import java.awt.CardLayout;

import javax.swing.JPanel;


class ContentPanel extends JPanel{
	
	private static final String MODE_NAME_ENTIRE_PAGE = Messages.getString("ContentPanel.EntirePage"); //$NON-NLS-1$
	private static final String MODE_NAME_HEADER_FOOTER = Messages.getString("ContentPanel.HeaderFooter"); //$NON-NLS-1$
	private static final String CONTENT_NAME_PAGE_CONTENT = Messages.getString("ContentPanel.PageContent"); //$NON-NLS-1$
	private static final String CONTENT_NAME_ROW_CONTENT = Messages.getString("ContentPanel.RowContent"); //$NON-NLS-1$

	private static final long serialVersionUID = 1L;
	
	public static final String[] MODE_NAMES = new String[]{MODE_NAME_ENTIRE_PAGE, MODE_NAME_HEADER_FOOTER};
	public static final String[] CONTENT_NAMES = new String[]{CONTENT_NAME_PAGE_CONTENT, CONTENT_NAME_ROW_CONTENT};
	public static final int DEFAULT_MODE = 0;

	SessionModel sessionModel;
	
	PageContentPanel pageContentPanel;
	RowContentPanel rowContentPanel; 
	CardLayout contentCardLayout;
	
	ContentPanel(SessionModel sessionModel){
		this.sessionModel = sessionModel;
		this.rowContentPanel = new RowContentPanel(sessionModel.getRowContentModel());
		this.pageContentPanel = new PageContentPanel(sessionModel.getPageContentModel());

		this.contentCardLayout = new CardLayout();
		setLayout(this.contentCardLayout);
		add(CONTENT_NAMES[0], this.pageContentPanel);
		add(CONTENT_NAMES[1], this.rowContentPanel);
		contentCardLayout.show(this, CONTENT_NAMES[0]);
	}

	public SessionModel getSessionModel(){
		return this.sessionModel;
	}
	
	public PageContentModel getPageContentModel(){
		return sessionModel.getPageContentModel();
	}

}

