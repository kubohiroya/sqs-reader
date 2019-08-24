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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sqs2.omr.ui.swing.GroupLayoutUtil;

public class NodeInfoPanel extends JPanel {
	
	public class Field extends JTextField{
		private static final long serialVersionUID = 0L;
		 public Field(String name){
			 super(name);
			 setEditable(false);
		 }
	} 
	
	private static final long serialVersionUID = 1L;
	SessionModel sessionModel;
	Field pathField;
	Field statusField;
	
	NodeInfoPanel(SessionModel sessionModel){
		this.sessionModel = sessionModel;
		
		DefaultMutableTreeNode node = sessionModel.sourceTreeModel.rootNode;
		String sourceDirectoryRootPath = sessionModel.getMarkReaderSession().getSourceDirectoryRootFile().getAbsolutePath();
		String status = SourceDirectoryTreeNodeTitleFactory.getTitle(node, sessionModel.sourceTreeModel, "");

		pathField = new Field(sourceDirectoryRootPath);
		statusField = new Field(status);
		
		JPanel p1 = new JPanel();
		
		GroupLayoutUtil.layout(p1, 
				new JLabel[]{new JLabel("Path:"), new JLabel("Status:")},
				new JComponent[]{pathField, statusField},
				true);

		p1.setBorder(BorderFactory.createTitledBorder("Node Information"));
		
		setLayout(new BorderLayout());
		add(p1);

	}
	
}
