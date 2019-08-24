/**
 * TraverseEventGenerator.java

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.PageTaskAccessor;
import net.sqs2.omr.model.Row;
import net.sqs2.omr.model.RowAccessor;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourcePhase;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.model.SpreadSheet;
import net.sqs2.store.ObjectStore.ObjectStoreException;

public class TraverseEventGenerator{
	
	SessionSource sessionSource = null;
	RowAccessor rowAccessor = null;
	PageTaskAccessor taskAccessor = null;
	ArrayList<SpreadSheetTraverseEventListener> listeners;

	SpreadSheetTraverseEventFilter filter;

	public TraverseEventGenerator(SessionSource sessionSource) throws IOException{
		this.sessionSource = sessionSource;
		this.rowAccessor = sessionSource.getContentAccessor().getRowAccessor();
		this.taskAccessor = sessionSource.getContentAccessor().getPageTaskAccessor();
		this.listeners = new ArrayList<SpreadSheetTraverseEventListener>();		
	}

	public TraverseEventGenerator(SessionSource sessionSource, SpreadSheetTraverseEventFilter filter) throws IOException{
		this(sessionSource);
		this.filter = filter;
	}
	
	public void addTraverseEvetListener(SpreadSheetTraverseEventListener listener) {
		this.listeners.add(listener);
	}
	
	public void addTraverseEventListeners(List<SpreadSheetTraverseEventListener> listeners) {
		this.listeners.addAll(listeners);
	}
	
	public Void call() throws TraverseStopException {
		SessionSourceEvent sessionSourceEvent = new SessionSourceEvent(this.sessionSource.getSessionID());
		sessionSourceEvent.setStart();
		sessionSourceEvent.setIndex(0);
		startSessionSource(sessionSourceEvent);
		traverseSessionSource(sessionSourceEvent);
		sessionSourceEvent.setEnd();
		endSessionSource(sessionSourceEvent);
		return null;
	}

	public void stop() {
		this.sessionSource.getSessionSourcePhase().setSessionRunningPhase(SessionSourcePhase.Phase.stop);
	}

	
	protected void traverseSessionSource(SessionSourceEvent sessionSourceEvent) throws TraverseStopException {
		Logger.getLogger(getClass().getName()).info(" + TraverseSessionSource: " + sessionSourceEvent.getSessionID());

		//Collection<FormMaster> formMasters = this.sessionSource.getFormMasterList();
		MasterEvent masterEvent = new MasterEvent(sessionSourceEvent, this.sessionSource.getNumFormMasters());
		
		int masterIndex = 0;
		for(FormMaster formMaster: this.sessionSource.getFormMasters()){
			masterEvent.setStart();
			masterEvent.setIndex(masterIndex++);
			masterEvent.setFormMaster((FormMaster) formMaster);
			if (this.filter != null && !this.filter.accept(masterEvent)) {
				return;
			}
			startMaster(masterEvent);
			traverseMaster(masterEvent, formMaster);
			masterEvent.setEnd();
			endMaster(masterEvent);
			masterIndex++;
		}
		Logger.getLogger(getClass().getName()).info(" - TraverseSessionSource: " + sessionSourceEvent.getSessionID());
	}

	protected void traverseMaster(MasterEvent masterEvent, FormMaster formMaster) throws TraverseStopException {
		Logger.getLogger(getClass().getName()).info(" + TraverseMaster: " + formMaster);
		Set<SourceDirectory> sourceDirectorySet = this.sessionSource.getSourceDirectoryRootTreeSet(formMaster);
		
		SourceDirectoryEvent sourceDirectoryEvent = new SourceDirectoryEvent(masterEvent, formMaster, 1);
		int sourceDirectoryIndex = 0;
		for(SourceDirectory sourceDirectory: sourceDirectorySet){
			sourceDirectoryEvent.setIndex(sourceDirectoryIndex++);
			sourceDirectoryEvent.setSourceDirectory(sourceDirectory);
			sourceDirectoryEvent.setStart();
			startSourceDirectory(sourceDirectoryEvent);
			Logger.getLogger(getClass().getName()).info(" ++ Traverse SourceDirectory: " + sourceDirectory);
			traverseSourceDirectory(sourceDirectoryEvent, formMaster);
			Logger.getLogger(getClass().getName()).info(" -- Traverse SourceDirectory: " + sourceDirectory);
			sourceDirectoryEvent.setEnd();
			endSourceDirectory(sourceDirectoryEvent);
		}
		Logger.getLogger(getClass().getName()).info(" - TraverseMaster: " + formMaster);
	}
	
	protected void traverseSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent, FormMaster master) throws TraverseStopException {
		SpreadSheetEvent spreadSheetEvent = new SpreadSheetEvent(sourceDirectoryEvent, master, 1);
		long sessionID = sourceDirectoryEvent.getMasterEvent().getSessionSourceEvent().getSessionID();
		SpreadSheet spreadSheet = new SpreadSheet(sessionID, master, sourceDirectoryEvent.getSourceDirectory());
		spreadSheetEvent.setStart();
		spreadSheetEvent.setIndex(0);
		spreadSheetEvent.setSpreadSheet(spreadSheet);

		if (this.filter != null && !this.filter.accept(spreadSheetEvent)) {
			return;
		}

		startSpreadSheet(spreadSheetEvent);
		traverseSpreadSheet(spreadSheetEvent, master);

		spreadSheetEvent.setEnd();
		endSpreadSheet(spreadSheetEvent);
	}

	protected void traverseSpreadSheet(SpreadSheetEvent spreadSheetEvent, FormMaster master) throws TraverseStopException {
		SourceDirectory targetSourceDirectory = spreadSheetEvent.getSpreadSheet().getSourceDirectory();
		Collection<SourceDirectory> sourceDirectoryList = targetSourceDirectory.getDescendentSourceDirectoryList();
		RowGroupEvent rowGroupEvent = new RowGroupEvent(spreadSheetEvent, master, 
				spreadSheetEvent.getSpreadSheet(), sourceDirectoryList.size() + 1);
		rowGroupEvent.setBaseRowGroup(false);

		int rowIndexBase = 0;		
		int sourceDirectoryIndex = 0;
		for (SourceDirectory sourceDirectory:  sourceDirectoryList) {
			rowGroupEvent.setStart();
			rowGroupEvent.setIndex(sourceDirectoryIndex);
			rowGroupEvent.setParentSourceDirectory(targetSourceDirectory);
			rowGroupEvent.setSourceDirectory(sourceDirectory);
			rowGroupEvent.setRowIndexBase(rowIndexBase);
			if (this.filter != null && !this.filter.accept(rowGroupEvent)) {
				sourceDirectoryIndex++;
				continue;
			}
			startRowGroup(rowGroupEvent);
			traverseRowGroup(rowGroupEvent, rowIndexBase, master);
			rowGroupEvent.setEnd();
			endRowGroup(rowGroupEvent);
			rowIndexBase += sourceDirectory.getNumPageIDs() / master.getNumPages();
			sourceDirectoryIndex++;
		}
		
		rowGroupEvent.setIndex(sourceDirectoryList.size());
		rowGroupEvent.setBaseRowGroup(true);
		rowGroupEvent.setRowIndexBase(rowIndexBase);
		rowGroupEvent.setSourceDirectory(targetSourceDirectory);
		rowGroupEvent.setParentSourceDirectory(targetSourceDirectory);
		rowGroupEvent.setStart();
		if (this.filter == null || this.filter.accept(rowGroupEvent)) {
			startRowGroup(rowGroupEvent);
			traverseRowGroup(rowGroupEvent, rowIndexBase, master);
			rowGroupEvent.setEnd();
			endRowGroup(rowGroupEvent);
		}
	}

	protected void traverseRowGroup(RowGroupEvent rowGroupEvent, int rowIndexBase, FormMaster master) throws TraverseStopException{
		SourceDirectory sourceDirectory = rowGroupEvent.getSourceDirectory();
		int numPages = master.getNumPages();

		int numRows = sourceDirectory.getPageIDList().size() / numPages;
		
		RowEvent rowEvent = new RowEvent(rowGroupEvent, numRows);
		String masterPath = master.getRelativePath();
		String sourceDirectoryPath = sourceDirectory.getRelativePath();
		for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
			try {
				rowEvent.setStart();
				Row row = (Row) this.rowAccessor.get(masterPath, sourceDirectoryPath, rowIndex);
				if(row == null){
					throw new RuntimeException("row is null :master="+masterPath+", sourceDirectory="+sourceDirectoryPath+", rowIndex"+rowIndex);
				}
				rowEvent.setRow(row);
				rowEvent.setRowIndex(rowIndex);
				List<PageID> pageIDList = new ArrayList<PageID>();
				for(int pageIndex = 0; pageIndex < numPages; pageIndex++){
					pageIDList.add(sourceDirectory.getPageIDList().get(rowIndex * numPages + pageIndex));
				}
				rowEvent.setPageIDList(pageIDList);
				
				if(row != null){
					rowEvent.setTaskErrorModelMultiHashMap(row.getTaskErrorModelMultiHashMap());
				}
				
				if (this.filter != null && !this.filter.accept(rowEvent)) {
					return;
				}

				startRow(rowEvent);
				traverseRow(rowEvent, master);
				rowEvent.setEnd();
				endRow(rowEvent);
			
			} catch (ObjectStoreException ex) {
				throw new TraverseStopException(ex);
			}
		}
	}

	protected void traverseRow(RowEvent rowEvent, FormMaster master) throws TraverseStopException {
		int numColumns = master.getNumQuestions();
		PageEvent pageEvent = new PageEvent(rowEvent, master);
		QuestionEvent questionEvent = new QuestionEvent(rowEvent, master);
		int prevPageIndex = -1;

		List<PageID> pageIDList = rowEvent.getPageIDList();

		try{
			for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
				List<FormArea> formAreaList = master.getFormAreaList(columnIndex);
				int pageIndex = formAreaList.get(0).getPageIndex();
				if (prevPageIndex != pageIndex) {

					if (prevPageIndex != -1) {
						pageEvent.setEnd();
						endPage(pageEvent);
					}

					pageEvent.setStart();
					PageID pageID = pageIDList.get(pageIndex);

					PageTask pageTask = this.taskAccessor.get(pageID, pageIndex);
					pageEvent.setPageTask(pageTask);
					pageEvent.setPageIndex(pageIndex);

					if (this.filter != null && !this.filter.accept(pageEvent)) {
						return;
					}
					startPage(pageEvent);
				}
				questionEvent.setStart();
				questionEvent.setIndex(columnIndex);
				questionEvent.setQuestionIndex(columnIndex);
				questionEvent.setFormAreaList(formAreaList);
				if (this.filter != null && !this.filter.accept(questionEvent)) {
					return;
				}

				startQuestion(questionEvent);
				traverseQuestion(pageEvent, questionEvent, master, formAreaList);
				questionEvent.setEnd();
				endQuestion(questionEvent);

				prevPageIndex = pageIndex;
			}
		}catch(ObjectStoreException ex){
			ex.printStackTrace();
			return;
		}
		
		if (prevPageIndex != -1) {
			pageEvent.setEnd();
			endPage(pageEvent);
		}

	}

	protected void traverseQuestion(PageEvent pageEvent, QuestionEvent questionEvent, FormMaster master, List<FormArea> formAreaList) {
		int numItems = formAreaList.size();
		QuestionItemEvent questionItemEvent = new QuestionItemEvent(questionEvent);
		for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
			questionItemEvent.setStart();
			questionItemEvent.setPageEvent(pageEvent);
			questionItemEvent.setItemIndex(itemIndex);
			FormArea formArea = formAreaList.get(itemIndex);
			questionItemEvent.setFormArea(formArea);
			startQuestionItem(questionItemEvent);
			traverseQuestionItem(questionItemEvent, master, formAreaList.get(itemIndex));
			questionItemEvent.setEnd();
			endQuestionItem(questionItemEvent);
		}
	}

	private boolean hasStopped(){
		return this.sessionSource.getSessionSourcePhase().hasStopped();
	}
	
	protected void traverseQuestionItem(QuestionItemEvent questionItemEvent, FormMaster master, FormArea formArea) {
		// do nothing
	}

	protected void startSessionSource(SessionSourceEvent sessionSourceEvent) {
		for (TraverseEventListener listener : this.listeners) {
			listener.startSessionSource(sessionSourceEvent);
		}
	}

	protected void endSessionSource(SessionSourceEvent sessionSourceEvent) {
		for (TraverseEventListener listener : this.listeners) {
			listener.endSessionSource(sessionSourceEvent);
		}
	}

	protected void startMaster(MasterEvent masterEvent) {
		for (TraverseEventListener listener : this.listeners) {
			listener.startMaster(masterEvent);
		}
	}

	protected void endMaster(MasterEvent masterEvent) {
		for (TraverseEventListener listener : this.listeners) {
			listener.endMaster(masterEvent);
		}
	}

	protected void startSourceDirectory(SourceDirectoryEvent spreadSheetEvent) {
		for (TraverseEventListener listener : this.listeners) {
			listener.startSourceDirectory(spreadSheetEvent);
		}
	}

	protected void endSourceDirectory(SourceDirectoryEvent spreadSheetEvent) {
		for (TraverseEventListener listener : this.listeners) {
			listener.endSourceDirectory(spreadSheetEvent);
		}
	}

	protected void startSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.startSpreadSheet(spreadSheetEvent);
		}
	}

	protected void endSpreadSheet(SpreadSheetEvent spreadSheetEvent) throws TraverseStopException {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.endSpreadSheet(spreadSheetEvent);
		}
		if (hasStopped()) {
			throw new TraverseStopException();
		}
	}

	protected void startRowGroup(RowGroupEvent rowGroupEvent) {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.startRowGroup(rowGroupEvent);
		}
	}

	protected void endRowGroup(RowGroupEvent sourceDirectoryEvent) throws TraverseStopException {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.endRowGroup(sourceDirectoryEvent);
		}
		if (hasStopped()) {
			throw new TraverseStopException();
		}
	}

	protected void startRow(RowEvent rowEvent) {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.startRow(rowEvent);
		}
	}

	protected void endRow(RowEvent rowEvent) throws TraverseStopException {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.endRow(rowEvent);
		}
		if (hasStopped()) {
			throw new TraverseStopException();
		}
	}

	protected void startPage(PageEvent pageEvent) {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.startPage(pageEvent);
		}
	}

	protected void endPage(PageEvent pageEvent) throws TraverseStopException {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.endPage(pageEvent);
		}
		if (hasStopped()) {
			throw new TraverseStopException();
		}
	}

	protected void startQuestion(QuestionEvent questionEvent) {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.startQuestion(questionEvent);
		}
	}

	protected void endQuestion(QuestionEvent questionEvent) {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.endQuestion(questionEvent);
		}
	}

	protected void startQuestionItem(QuestionItemEvent questionItemEvent) {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.startQuestionItem(questionItemEvent);
		}
	}

	protected void endQuestionItem(QuestionItemEvent questionItemEvent) {
		for (SpreadSheetTraverseEventListener listener : this.listeners) {
			listener.endQuestionItem(questionItemEvent);
		}
	}

}
