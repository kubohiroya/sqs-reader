/**
 *  Row.java

 Copyright 2007 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2007/03/13
 Author hiroya
 */
package net.sqs2.omr.model;

import java.io.Serializable;

import org.apache.commons.collections15.multimap.MultiHashMap;

public class Row extends AbstractRow implements Serializable {
	private static final long serialVersionUID = 6;

	Answer[] answer;
	MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap;

	public Row() {
	}

	public Row(String masterPath, String sourceDirectoryPath, int rowIndex, int numQuestions) {
		super(masterPath, sourceDirectoryPath, rowIndex);
		this.answer = new Answer[numQuestions];
	}

	public void setAnswer(int questionIndex, Answer answer) {
		this.answer[questionIndex] = answer;
	}

	public Answer getAnswer(int questionIndex) {
		return this.answer[questionIndex];
	}

	public int getNumAnswers() {
		return this.answer.length;
	}

	public void addErrorModel(OMRProcessorErrorModel taskErrorModel) {
		if (this.taskErrorModelMap == null) {
			this.taskErrorModelMap = new MultiHashMap<PageID, OMRProcessorErrorModel>();
		}
		if (!this.taskErrorModelMap.containsKey(taskErrorModel)) {
			this.taskErrorModelMap.put(taskErrorModel.getPageID(), taskErrorModel);
		}
	}

	public MultiHashMap<PageID, OMRProcessorErrorModel> getTaskErrorModelMultiHashMap() {
		return this.taskErrorModelMap;
	}

	public void clearTaskErrorModelMap() {
		this.taskErrorModelMap = null;
	}
}
