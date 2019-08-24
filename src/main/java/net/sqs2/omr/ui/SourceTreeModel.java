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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.ui.util.TreeValues;
import net.sqs2.util.StringUtil;

class SourceTreeModel{
	
	public static final String ROOT_NODE_NAME = "すべて";

	DefaultTreeModel treeModel;
	DefaultMutableTreeNode rootNode;

	Map<SourceDirectory,TreeNode> sourceDirectoryToTreeNodeMap = new HashMap<SourceDirectory,TreeNode>();

	TreeValues numErrors = new TreeValues();
	TreeValues numSuccess = new TreeValues();
	TreeValues numTasks = new TreeValues();
	TreeValues numPageIDs = new TreeValues();

	boolean hasInitialized = false;
	
	SourceTreeModel(){
		this.rootNode = new DefaultMutableTreeNode(ROOT_NODE_NAME);
		this.treeModel = new DefaultTreeModel(this.rootNode);
		this.hasInitialized = false;
	}

	public void initialize(SessionSource sessionSource){
		for(FormMaster formMaster: sessionSource.getFormMasters()){
			Set<SourceDirectory> sourceDirectorySet = sessionSource.getSourceDirectoryRootTreeSet(formMaster);

			DefaultMutableTreeNode masterNode = new DefaultMutableTreeNode(formMaster);
			this.rootNode.add(masterNode);
			this.treeModel.nodeStructureChanged(this.rootNode);

			for(SourceDirectory sourceDirectory: sourceDirectorySet){
				initializeTreeNodes(masterNode, sourceDirectory);
			}
		}
		this.hasInitialized = true;
	}
	
	private void initializeTreeNodes(DefaultMutableTreeNode parentNode, SourceDirectory sourceDirectory) {
		DefaultMutableTreeNode sourceDirectoryNode = new DefaultMutableTreeNode(sourceDirectory); 
		parentNode.add(sourceDirectoryNode);
		this.sourceDirectoryToTreeNodeMap.put(sourceDirectory, sourceDirectoryNode);
		this.treeModel.nodeChanged(sourceDirectoryNode);
		this.treeModel.nodeStructureChanged(sourceDirectoryNode);
		this.numPageIDs.setValue(sourceDirectoryNode, sourceDirectory.getNumPageIDs());
		Collection<SourceDirectory> childSourceDirectoryList = sourceDirectory.getChildSourceDirectoryList();
		if(childSourceDirectoryList == null){
			return;
		}
		for(SourceDirectory childSourceDirectory: childSourceDirectoryList){
			if(childSourceDirectory.getCurrentFormMaster() == sourceDirectory.getCurrentFormMaster()){
				initializeTreeNodes(sourceDirectoryNode, childSourceDirectory);
			}
		}
	}

	public TreePath getTreePath(SourceDirectory sourceDirectory){
		if(sourceDirectory == null){
			return null;
		}
		
		if(hasInitialized == false){
			throw new RuntimeException("Not Initialized:"+this);
		}
		
		FormMaster master = sourceDirectory.getCurrentFormMaster();
		
		int numChildren = this.rootNode.getChildCount();
		
		for(int index = 0; index < numChildren; index++){
			DefaultMutableTreeNode topLevelNode = (DefaultMutableTreeNode)this.rootNode.getChildAt(index);
			if(topLevelNode.getUserObject().equals(master)){
				List<String> path = StringUtil.split(sourceDirectory.getRelativePath(), File.separatorChar);
				path.add(0, ""); // add node represents SourceDirectory Root
				TreeNode treeNode = this.sourceDirectoryToTreeNodeMap.get(sourceDirectory);
				return new TreePath(this.treeModel.getPathToRoot(treeNode));
			}
		}
		throw new IllegalArgumentException("["+sourceDirectory+"]");
	}

	public void valueForPathChanged(TreePath treePath){
		// update Node Title for JTree
		do {
			Object nodeValue = ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
			this.treeModel.valueForPathChanged(treePath, nodeValue);
			treePath = treePath.getParentPath();
		} while (treePath != null);
	}

	public void clear(){
		this.sourceDirectoryToTreeNodeMap.clear();
		this.rootNode.removeAllChildren();
		this.numErrors.clear();
		this.numSuccess.clear();
		this.numTasks.clear();
		this.numPageIDs.clear();
	}
}