/*

 PageTaskCommitRowGenerateSerivce.java

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

 Created on 2007/01/11

 */
package net.sqs2.omr.session.commit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.Answer;
import net.sqs2.omr.model.ContentAccessor;
import net.sqs2.omr.model.FormAreaResult;
import net.sqs2.omr.model.MarkAreaAnswer;
import net.sqs2.omr.model.MarkAreaAnswerItem;
import net.sqs2.omr.model.MarkAreaResult;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorResult;
import net.sqs2.omr.model.PageAreaResult;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.PageTaskAccessor;
import net.sqs2.omr.model.Row;
import net.sqs2.omr.model.RowAccessor;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.model.TextAreaAnswer;
import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.store.ObjectStore.ObjectStoreException;
import net.sqs2.util.FileResourceID;
import net.sqs2.util.FileUtil;

public class PageTaskCommitRowGenerateService extends AbstractPageTaskCommitService {

	private PageTaskAccessor taskAccessor;

	public PageTaskCommitRowGenerateService(MarkReaderSession session) throws IOException {
		super(session);
	}

	public void setup(File sourceDirectoryRoot) throws IOException {
		SessionSource sessionSource = markReaderSession.getSessionSource();
		ContentAccessor contentAccessor = sessionSource.getContentAccessor();
		this.taskAccessor = contentAccessor.getPageTaskAccessor(); 
	}
	
	@Override
	public void commit(PageTask task) throws IOException{
		this.taskAccessor.put(task);
		setupRow((OMRPageTask)task);
	}

	private void setupRow(OMRPageTask pageTask) throws IOException{
		SessionSource sessionSource = SessionSourceManager.getInstance(pageTask.getSessionID());
		ContentAccessor sessionSourceAccessor = sessionSource.getContentAccessor();
		FormMaster master = null;
		FileResourceID masterFileResourceID = pageTask.getFormMasterFileResourceID();
		
		OMRProcessorResult pageTaskResult = pageTask.getResult();
		
		if(pageTaskResult != null && pageTaskResult.getMasterFileResourceID() != null){
			masterFileResourceID = pageTaskResult.getMasterFileResourceID();
		}
		
		master = (FormMaster) sessionSource.getContentAccessor().getFormMasterAccessor().get(masterFileResourceID);
		
		File pageIDPath = new File(pageTask.getPageID().getFileResourceID().getRelativePath());
		String parentPath = pageIDPath.getParent();
		if (parentPath == null) {
			parentPath = "";
		}
		
		RowAccessor rowAccessor = sessionSourceAccessor.getRowAccessor();
		
		String pagePath = pageTask.getPageID().getFileResourceID().getRelativePath();
		String dirPath = FileUtil.getDirpath(pagePath, File.separatorChar);
		int rowIndex = sessionSource.getRowIndex(dirPath, pageTask.getPageID()) / master.getNumPages();
		Row row = getRow(rowAccessor, parentPath, rowIndex, master);
		if (pageTask.getErrorModel() != null) {
			row.addErrorModel(pageTask.getErrorModel());
		}else{
			setupAnswers(row, pageTask, rowAccessor, master, rowIndex, pageIDPath);
			row.clearTaskErrorModelMap();
		}
		rowAccessor.put(row);
	}

	private void setupAnswers(Row row, OMRPageTask task, RowAccessor rowAccessor, FormMaster master, int rowIndex, File pageIDPath) {
		OMRProcessorResult taskResult = task.getResult();

		List<PageAreaResult> pageAreaResultList = taskResult.getPageAreaResultList();
		List<FormArea> formAreaList = master.getFormAreaListByPageIndex(task.getProcessingPageIndex());

		for (FormArea formArea : formAreaList) {
			FormAreaResult formAreaResult = (FormAreaResult) pageAreaResultList.get(formArea.getAreaIndexInPage());
			int itemIndex = formArea.getItemIndex();
			int questionIndex = formArea.getQuestionIndex();
			Answer answer = row.getAnswer(questionIndex);
			if (formArea.isMarkArea()) {
				if (answer == null) {
					int numItems = master.getFormAreaList(questionIndex).size();
					answer = new MarkAreaAnswer(formArea.isSelectMultiple(), numItems);
					row.setAnswer(questionIndex, answer);
				}
				float density = ((MarkAreaResult)formAreaResult).getDensity();
				MarkAreaAnswerItem answerItem = new MarkAreaAnswerItem(itemIndex, density);
				((MarkAreaAnswer) answer).setMarkAreaAnswerItem(answerItem);
			} else if (formArea.isTextArea()) {
				row.setAnswer(questionIndex, new TextAreaAnswer());
			}
		}
	}

	private Row getRow(RowAccessor rowAccessor, String parentPath, int rowIndex, FormMaster master) throws ObjectStoreException{
		Row row;
		try{
			row = (Row) rowAccessor.get(master.getRelativePath(), parentPath, rowIndex);
			if (row == null || row.getNumAnswers() != master.getNumQuestions()){
				row = createRow(parentPath, rowIndex, master);
			}
		}catch(ObjectStoreException ex){
			row = createRow(parentPath, rowIndex, master);
		}
		return row;
	}

	private Row createRow(String parentPath, int rowIndex, FormMaster master) {
		return new Row(master.getRelativePath(), 
				parentPath, 
				rowIndex, 
				master.getNumQuestions());
	}
}
