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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.tree.TreeNode;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.session.service.MarkReaderSession;

public class SessionModel{

	MarkReaderSession session;

	MarkReaderModel markReaderModel;
	private PageIDTableModel pageIDTableModel;
	private ResultTableModel resultTableModel;
	SourceTreeModel sourceTreeModel;
	PageContentModel pageContentModel;
	RowContentModel rowContentModel;
	
	Map<Integer,SourceDirectory> treeRowIDtoSourceDirectoryMap = new TreeMap<Integer,SourceDirectory>();
	Map<SourceDirectory,PageIDTableModel> sourceDirectoryToPageIDTableModelMap = new HashMap<SourceDirectory,PageIDTableModel>();
	Map<SourceDirectory,ResultTableModel> sourceDirectoryToResultTableModelMap = new HashMap<SourceDirectory,ResultTableModel>();
	
	Map<PageID,String> pageIndexMap = new HashMap<PageID,String>();

	SourceDirectory selectedSourceDirectory;
	
	public PageIDTableModel defaultPageIDTableModel;
	public ResultTableModel defaultResultTableModel;
	
	private Future<?> future;
	
	SessionModel(MarkReaderModel markReaderModel, MarkReaderSession session){
		this.markReaderModel = markReaderModel;
		this.session = session;
		this.sourceTreeModel = new SourceTreeModel();
		this.pageIDTableModel = new PageIDTableModel(this, null);
		this.resultTableModel = new ResultTableModel(this, null);
		this.pageContentModel = new PageContentModel();
		this.rowContentModel = new RowContentModel();
		this.defaultPageIDTableModel = new PageIDTableModel(this, null);
		this.defaultResultTableModel = new ResultTableModel(this, null);
	}
	
	public MarkReaderModel getMarkReaderModel(){
		return this.markReaderModel;
	}
	
	public PageContentModel getPageContentModel(){
		return this.pageContentModel;
	}
	
	public RowContentModel getRowContentModel(){
		return this.rowContentModel;
	}
	
	public MarkReaderSession getMarkReaderSession(){
		return this.session;
	}
	
	public void startSession()throws IOException{
		this.session.startSession(true);
	}
	
	public void updateProgressRate(){
		TreeNode rootNode = (TreeNode) this.sourceTreeModel.rootNode;
		int numErrors = this.sourceTreeModel.numErrors.getValueTotal(rootNode);
		int numSuccess = this.sourceTreeModel.numSuccess.getValueTotal(rootNode);
		int numTasks = this.sourceTreeModel.numTasks.getValueTotal(rootNode);
		if(0 < numTasks){
			session.getProgressRate().setObject( 1f * (numErrors + numSuccess) / numTasks );
			session.getProgressRate().update();
		}
	}
	
	public void initialize() {
		SessionSource sessionSource = this.session.getSessionSource();
		if(sessionSource == null){
			return;
		}
		
		clear();

		future = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable(){
			public void run(){
				if(session.getProgressRate() == null){
					return;
				}
				float prevValue = session.getProgressRate().getObject();
				if(prevValue < 0){
					float nextValue = prevValue - 0.01f;
					if(prevValue < -1.0){
						nextValue = - 0.01f;
					}
					session.getProgressRate().setObject(nextValue);
					session.getProgressRate().update();
				}
			}
		}, 0L, 20L, TimeUnit.MILLISECONDS);

		
		this.sourceTreeModel.initialize(sessionSource);
		
		for(int formMasterIndex = 0; formMasterIndex < sessionSource.getNumFormMasters(); formMasterIndex++){
			FormMaster formMaster = sessionSource.getFormMaster(formMasterIndex);
			Set<SourceDirectory> sourceDirectorySet = sessionSource.getSourceDirectoryRootTreeSet(formMaster);
			for(SourceDirectory sourceDirectory: sourceDirectorySet){
				int rowIndex = 0;
				this.treeRowIDtoSourceDirectoryMap.put(rowIndex, sourceDirectory);
				initializePageIDTableModel(sourceDirectory, rowIndex);
				initializeResultTableModel(sourceDirectory, rowIndex);
			}
		}
	}

	private void initializeResultTableModel(SourceDirectory sourceDirectory, int rowIndex) {
		this.resultTableModel = new ResultTableModel(this, sourceDirectory);
		this.sourceDirectoryToResultTableModelMap.put(sourceDirectory, this.resultTableModel);
		if(0 < sourceDirectory.getNumChildSourceDirectories()){
			for(SourceDirectory childSourceDirectory: sourceDirectory.getChildSourceDirectoryList()){
				initializeResultTableModel(childSourceDirectory, rowIndex);
			}
		}
	}
	
	private int initializePageIDTableModel(SourceDirectory sourceDirectory, int rowIndex) {
		this.pageIDTableModel = new PageIDTableModel(this, sourceDirectory);
		this.sourceDirectoryToPageIDTableModelMap.put(sourceDirectory, this.pageIDTableModel);

		if(0 < sourceDirectory.getNumChildSourceDirectories()){
			for(SourceDirectory childSourceDirectory: sourceDirectory.getChildSourceDirectoryList()){
				rowIndex += initializePageIDTableModel(childSourceDirectory, rowIndex);
			}
		}

		int numPages = sourceDirectory.getCurrentFormMaster().getNumPages();
		int index = 0;
		for(PageID pageID: sourceDirectory.getPageIDList()){
			this.pageIndexMap.put(pageID, String.valueOf(index % numPages + 1));
			index ++;
		}
		rowIndex += sourceDirectory.getNumPageIDs();
		return rowIndex;
	}
	
	public void stopProgressBarUpdator() {
		if(future != null){
			future.cancel(true);
		}
	}

	public void stop() {
		this.pageContentModel.stopStrokeAnimation();
		stopProgressBarUpdator();
	}
	
	private void clear() {
		this.treeRowIDtoSourceDirectoryMap.clear();
		this.sourceDirectoryToPageIDTableModelMap.clear();
		this.sourceDirectoryToResultTableModelMap.clear();
		this.pageIndexMap.clear();
		this.sourceTreeModel.clear();
		this.pageIDTableModel.clear();
		this.resultTableModel.clear();
	}

	public PageIDTableModel getPageIDTableModel() {
		return pageIDTableModel;
	}

	public void setPageIDTableModel(PageIDTableModel pageIDTableModel) {
		this.pageIDTableModel = pageIDTableModel;
	}

	public ResultTableModel getResultTableModel() {
		return resultTableModel;
	}

	public void setResultTableModel(ResultTableModel resultTableModel) {
		this.resultTableModel = resultTableModel;
	}

	public SourceDirectory getSelectedSourceDirectory() {
		return selectedSourceDirectory;
	}

	public void setSelectedSourceDirectory(SourceDirectory selectedSourceDirectory) {
		this.selectedSourceDirectory = selectedSourceDirectory;
	}

}
