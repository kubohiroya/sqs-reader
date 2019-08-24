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

import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.util.FileUtil;

class SourceDirectoryTreeCellRenderer extends DefaultTreeCellRenderer{
	
	private static final long serialVersionUID = 1L;
	
	public static final Font parentNodeFont = new Font(Font.SANS_SERIF, Font.ITALIC, 12);
	public static final Font nodeFont = new Font(Font.SERIF, Font.PLAIN, 12);
	public Font defaultFont;
	SessionModel sessionModel;
	static JFileChooser chooser = new JFileChooser();
	
	SourceDirectoryTreeCellRenderer(SessionModel sessionModel){
		this.sessionModel = sessionModel;
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object object, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Icon icon;
		String name = null;
		DefaultMutableTreeNode node = ((DefaultMutableTreeNode)object);
		Object value = (object == null)?null:node.getUserObject();

		int numErrors = sessionModel.sourceTreeModel.numErrors.getValueTotal(node);
		int numSuccess = sessionModel.sourceTreeModel.numSuccess.getValueTotal(node);
		//int numTasks = sessionModel.sourceTreeModel.numTasks.getValueTotal(node);
		int numPageIDs = sessionModel.sourceTreeModel.numPageIDs.getValueTotal(node);
		int numPageIDsCurrent = sessionModel.sourceTreeModel.numPageIDs.getValue(node);

		if(defaultFont == null){
			defaultFont = getFont();
		}else{
			setFont(defaultFont);
		}
		
		if(numPageIDs != numPageIDsCurrent){
			setFont(parentNodeFont);
		}else{
			setFont(nodeFont);
		}
		
		if(0 < numErrors){
			this.setTextNonSelectionColor(Colors.ERROR_NODE_COLOR);
		}else if(numPageIDs == numSuccess && 0 < numSuccess && numErrors == 0){
			this.setTextNonSelectionColor(Colors.NO_ERROR_NODE_COLOR);
		}else if(numPageIDs == numSuccess + numErrors){
			this.setTextNonSelectionColor(Colors.AVAILABLE_NODE_COLOR);
		}else if((numSuccess + numErrors) > 0){
			this.setTextNonSelectionColor(Colors.ACTIVE_NODE_COLOR);
		}else{
			this.setTextNonSelectionColor(Colors.UNAVAILABLE_NODE_COLOR);
		}
		
		if(value instanceof SourceDirectory){
			leaf = false;
			SourceDirectory sourceDirectory = (SourceDirectory)value;
			if(sourceDirectory.getRelativePath().equals("")){
				icon = javax.swing.plaf.metal.MetalIconFactory.getFileChooserHomeFolderIcon();
			}else{
				icon = chooser.getIcon(new File(sessionModel.getMarkReaderSession().getSourceDirectoryRootFile(), sourceDirectory.getRelativePath()));
			}
			
			String nodeName = sourceDirectory.getDirectory().getAbsoluteFile().getName();		
			name =  nodeName;
						
		}else if(value instanceof FormMaster){
			String path = ((FormMaster)value).getFileResourceID().getRelativePath();
			icon = chooser.getIcon(new File(sessionModel.getMarkReaderSession().getSourceDirectoryRootFile(), path));
			name = FileUtil.getName(path);

		}else if(value instanceof String){
			icon = javax.swing.plaf.metal.MetalIconFactory.getTreeComputerIcon();
			name = (String)value;
			
		}else if(value == null){
			icon = javax.swing.plaf.metal.MetalIconFactory.getTreeLeafIcon();
			name = object.getClass().getName();
		}else{
			icon = javax.swing.plaf.metal.MetalIconFactory.getTreeLeafIcon();
			name = "-"+value.getClass().getName();
		}
		
		this.setIcon(icon);
		this.setOpenIcon(icon);
		this.setClosedIcon(icon);
		String treeNodeText = SourceDirectoryTreeNodeTitleFactory.getTitle(node, sessionModel.sourceTreeModel, name);
		super.getTreeCellRendererComponent(tree, treeNodeText, sel, expanded, leaf, row, hasFocus);
		return this;
	}
}