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

import javax.swing.tree.TreeNode;

import net.sqs2.omr.model.SourceDirectory;

class SourceDirectoryTreeNodeTitleFactory{
	
	public static String getTitle(TreeNode node, SourceTreeModel model, SourceDirectory sourceDirectory){
		return getTitle(node, model, sourceDirectory.getDirectory().getAbsoluteFile().getName());
	}
	
	public static String getTitle(TreeNode node, SourceTreeModel model, String name) {
		
		int numErrors = 0;
		int numSuccess = 0;
		int numTasks = 0;
		int numPageIDs = 0;

		numErrors = model.numErrors.getValueTotal(node);
		numSuccess = model.numSuccess.getValueTotal(node);
		numTasks = model.numTasks.getValueTotal(node);
		numPageIDs = model.numPageIDs.getValueTotal(node);
		
		StringBuilder nodeContent = new StringBuilder();
		nodeContent.append(name);
		
		String base = (numTasks == numPageIDs)? Integer.toString(numTasks) : Integer.toString(numTasks)+'/'+Integer.toString(numPageIDs); 
		
		if(0 < numErrors){
			nodeContent.append(" <").append(numErrors).append(">").append(numSuccess).append(" (").append(base).append(")");
		}else if(0 < numSuccess){
			nodeContent.append(" ").append(numSuccess).append(" (").append(base).append(")");
		}else if(0 < numTasks){
			nodeContent.append(" ").append(numTasks).append("(").append(base).append(")");
		}else if(0 < numPageIDs){
			nodeContent.append(" (").append(base).append(")");
		}

		return nodeContent.toString();
	}
}