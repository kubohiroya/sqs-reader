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
package net.sqs2.omr.ui.util;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.TreeNode;

public class TreeValues{
	Map<TreeNode,Integer> valueMap;
	Map<TreeNode,Integer> valueTotalMap;
	
	public TreeValues(){
		this.valueMap = new HashMap<TreeNode,Integer>();
		this.valueTotalMap = new HashMap<TreeNode,Integer>();
	}
	
	public void clear(){
		this.valueMap.clear();
		this.valueTotalMap.clear();
	}
	
	public int getValueTotal(TreeNode node){
		Integer valueTotal = valueTotalMap.get(node);
		if(valueTotal != null){
			return valueTotal.intValue();
		}
		int total = getValue(node);
		for(int index = node.getChildCount() - 1; 0 <= index ; index--){
			TreeNode childNode = node.getChildAt(index);
			total += getValueTotal(childNode);
		}
		valueTotalMap.put(node, total);
		return total;
	}

	public int getValue(TreeNode node){
		Integer value = valueMap.get(node);
		return (value == null) ? 0: value.intValue();
	}
	
	public void setValue(TreeNode node, int value){
		valueMap.put(node, value);
		valueTotalMap.put(node, null);
		for(TreeNode parentNode = node.getParent(); parentNode != null; parentNode =  parentNode.getParent()){
			valueTotalMap.put(parentNode, null);
		}
	}

	public void incrementValue(TreeNode node){
		int value = getValue(node);
		valueMap.put(node, value+1);
		valueTotalMap.put(node, null);
		for(TreeNode parentNode = node.getParent(); parentNode != null; parentNode =  parentNode.getParent()){
			valueTotalMap.put(parentNode, null);
		}
	}
}
