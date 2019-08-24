/**
 * SpreadSheetExportEventAdapter.java

 Copyright 2009 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Author hiroya
 */

package net.sqs2.omr.session.traverse;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.Answer;
import net.sqs2.omr.model.MarkAreaAnswer;
import net.sqs2.omr.model.MarkAreaAnswerItem;
import net.sqs2.omr.model.MarkRecognitionConfig;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.Row;
import net.sqs2.omr.model.SourceConfig;

public final class MarkReadStatusRetriever extends TraverseEventAdapter implements SpreadSheetTraverseEventListener {

	public class MarkReadStatus{
		private float densityThreshold;
		private float doubleMarkErrorSuppressionThreshold;
		private float noMarkErrorSuppressionThreshold;
		
		private int[][] valueTotalMatrix;
		private int[] numNoValues;
		private int[] numMultipleValues;
		private long[] targetLastModifiedArray;
		public float getDensityThreshold() {
			return densityThreshold;
		}
		public float getDoubleMarkErrorSuppressionThreshold() {
			return doubleMarkErrorSuppressionThreshold;
		}
		public float getNoMarkErrorSuppressionThreshold() {
			return noMarkErrorSuppressionThreshold;
		}
		public int[][] getValueTotalMatrix() {
			return valueTotalMatrix;
		}
		public int[] getNumNoValues() {
			return numNoValues;
		}
		public int[] getNumMultipleValues() {
			return numMultipleValues;
		}
		public long[] getTargetLastModifiedArray() {
			return targetLastModifiedArray;
		}
		
	}

	private static final Map<Long,MarkReadStatus> map = new HashMap<Long,MarkReadStatus>();
		
	
	@Override
	public void startSessionSource(SessionSourceEvent sessionSourceEvent) {
		map.put(sessionSourceEvent.getSessionID(), new MarkReadStatus());
	}

	@Override
	public void startSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
		MarkRecognitionConfig config = ((SourceConfig)sourceDirectoryEvent.getSourceDirectory().getConfiguration().getConfig().getPrimarySourceConfig()).getMarkRecognitionConfig();
		MarkReadStatus status = map.get(sourceDirectoryEvent.getMasterEvent().getSessionSourceEvent().getSessionID());
		status.densityThreshold = config.getMarkRecognitionDensityThreshold();
		status.doubleMarkErrorSuppressionThreshold = config.getDoubleMarkErrorSuppressionThreshold();
		status.noMarkErrorSuppressionThreshold = config.getNoMarkErrorSuppressionThreshold();
	}

	@Override
	public void startSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		FormMaster master = spreadSheetEvent.getFormMaster();
		MarkReadStatus status = map.get(spreadSheetEvent.getSourceDirectoryEvent().getMasterEvent().getSessionSourceEvent().getSessionID());
		status.valueTotalMatrix = new int[master.getNumQuestions()][];
		status.numNoValues = new int[master.getNumQuestions()];
		status.numMultipleValues = new int[master.getNumQuestions()];
		status.targetLastModifiedArray = new long[master.getNumQuestions()];
	
		for (int columnIndex = 0; columnIndex < master.getNumQuestions(); columnIndex++) {
			List<FormArea> formAreaList = master.getFormAreaList(columnIndex);
			FormArea formArea = formAreaList.get(0);
			if (formArea.isSelectSingle() || formArea.isSelectMultiple()) {
				status.valueTotalMatrix[columnIndex] = new int[formAreaList.size()];
			}
		}
	}

	@Override
	public void startRowGroup(RowGroupEvent rowGroupEvent) {
	}

	@Override
	public void startRow(RowEvent rowEvent) {
	}

	@Override
	public void startPage(PageEvent pageEvent) {
	}

	@Override
	public void startQuestion(QuestionEvent questionEvent) {
		List<FormArea> formAreaList = questionEvent.getFormAreaList();
		FormArea formArea = formAreaList.get(0);
	
		Row row = questionEvent.getRowEvent().getRow();
		if (row == null) {
			return;
		}
		Answer answer = row.getAnswer(questionEvent.getQuestionIndex());
		if (answer == null) {
			return;	
		}
		
		MarkReadStatus status = map.get(questionEvent.getRowEvent().getRowGroupEvent().getSpreadSheetEvent().getSourceDirectoryEvent().getMasterEvent().getSessionSourceEvent().getSessionID());
		
		if (formArea.isSelectSingle()) {
			int numSelectedItemIndex = -1;
			int numSelected = 0;
	
			List<MarkAreaAnswerItem> markAreaAnswerItemList =
				((MarkAreaAnswer) answer).createMarkAreaAnswerItemSet().getMarkedAnswerItems(status.densityThreshold,
						status.doubleMarkErrorSuppressionThreshold, status.noMarkErrorSuppressionThreshold);
			
			for (MarkAreaAnswerItem markAreaAnswerItem : markAreaAnswerItemList){ 
				numSelectedItemIndex = markAreaAnswerItem.getItemIndex();
				numSelected++;
			}
	
			if (numSelected == 0) {
				status.numNoValues[formArea.getQuestionIndex()]++;
			} else if (numSelected == 1) {
				status.valueTotalMatrix[formArea.getQuestionIndex()][numSelectedItemIndex]++;
			} else {
				status.numMultipleValues[formArea.getQuestionIndex()]++;
			}
	
		} else if (formArea.isSelectMultiple()) {
			for (int itemIndex = 0; itemIndex < formAreaList.size(); itemIndex++) {
				MarkAreaAnswerItem markAreaAnswerItem = ((MarkAreaAnswer) answer).getMarkAreaAnswerItem(itemIndex);
				if (markAreaAnswerItem.isSelectMultiSelected(((MarkAreaAnswer) answer), status.densityThreshold)) {
					status.valueTotalMatrix[formArea.getQuestionIndex()][itemIndex]++;
				}
			}
		}
	}

	@Override
	public void startQuestionItem(QuestionItemEvent questionItemEvent) {
		FormArea formArea = questionItemEvent.getFormArea();
		MarkReadStatus status = map.get(questionItemEvent.getQuestionEvent().getRowEvent().getRowGroupEvent().getSpreadSheetEvent().getSourceDirectoryEvent().getMasterEvent().getSessionSourceEvent().getSessionID());
		if (formArea.isTextArea()) {
		} else if (formArea.isMarkArea()) {
			PageTask pageTask = questionItemEvent.getPageEvent().getPageTask();
			long targetFileLastModified = pageTask.getPageID().getFileResourceID().getLastModified();
			long configHandlerLastModified = pageTask.getConfigFileResourceID().getLastModified();
			long masterFileLastModified = questionItemEvent.getQuestionEvent().getFormMaster().getLastModified();
			long prevTargetLastModified = status.targetLastModifiedArray[formArea.getQuestionIndex()];
	
			status.targetLastModifiedArray[formArea.getQuestionIndex()] = max(max(targetFileLastModified,
					configHandlerLastModified), max(masterFileLastModified, prevTargetLastModified));
		}
	}

	@Override
	public void endQuestionItem(QuestionItemEvent questionItemEvent) {
	}

	@Override
	public void endQuestion(QuestionEvent questionEvent) {
	}

	@Override
	public void endPage(PageEvent pageEvent) {
	}

	@Override
	public void endRow(RowEvent rowEvent) {
	}

	@Override
	public void endRowGroup(RowGroupEvent rowGroupEvent) {
	}

	@Override
	public void endSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
	}

	@Override
	public void endSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
	}
	
	public static MarkReadStatus getMarkReadStatus(long sessionID){
		return map.get(sessionID);
	}

	private long max(long a, long b) {
		if (a <= b) {
			return b;
		} else {
			return a;
		}
	}

}
