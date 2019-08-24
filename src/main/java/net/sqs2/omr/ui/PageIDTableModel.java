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
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.sqs2.omr.model.Config;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.OMRProcessorErrorMessages;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.OMRProcessorResult;
import net.sqs2.omr.model.PageAreaResult;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.PageTaskAccessor;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceConfig;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.result.export.textarea.FormAreaImageWriter;
import net.sqs2.util.FileUtil;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;

public class PageIDTableModel extends AbstractTableModel implements TableModel{
	
	private static final long serialVersionUID = 1;
	public static final String[] COLUMN_NAMES = new String[]{"SourceDirectory", "Filename", "M", "ID", "P", "Status"};
	public static final String[] COLUMN_TOOLTIP_TEXT = new String[]{"SourceDirectory", "Filename", "Page in MultiPageFile", "Sample ID In this SourceDirectory", "Page of this Form", "MarkRead Status"};
	public static final int[] COLUMN_WIDTH_RATIO = new int[]{100,80,40,40,40,200};
	
	SessionModel sessionModel;
	SourceDirectory sourceDirectory;
		
	PageIDTableModel(SessionModel sessionModel, SourceDirectory sourceDirectory){
		this.sessionModel = sessionModel;
		this.sourceDirectory = sourceDirectory;
	}
	
	public SourceDirectory getSourceDirectory(){
		return this.sourceDirectory;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}
	
	@Override
	public int getRowCount() {
		if(sourceDirectory == null){
			return 0;
		}
		int rowCount = sourceDirectory.getNumPageIDs();
		return rowCount;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex){
		case 0:
		case 1:
			return String.class;
		case 2:
		case 3:
		case 4:
			return Integer.class;
		case 5:
			return String.class;
		}
		return super.getColumnClass(columnIndex);
	}
	
	public void clear(){
		this.sourceDirectory = null;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(sourceDirectory == null){
			return null;
		}

		PageID pageID = sourceDirectory.getPageIDList().get(rowIndex);
		int numPagesOfMaster = sourceDirectory.getCurrentFormMaster().getNumPages();
		
		switch(columnIndex){
		case 0:
			return sourceDirectory.getRelativePath();
		case 1:
			return FileUtil.getName(pageID.getFileResourceID().getRelativePath());
		case 2:
			return new Integer(pageID.getIndexInFile() + 1);
		case 3:
			return new Integer(rowIndex / numPagesOfMaster + 1);
		case 4:
			return new Integer(rowIndex % numPagesOfMaster + 1);
		case 5:
			try{
				SessionSource sessionSource = sessionModel.session.getSessionSource();
				if(sessionSource == null || sourceDirectory == null || sourceDirectory.getConfiguration() == null){
					return null;
				}
				PageTaskAccessor pageTaskAccessor = sessionSource.getContentAccessor().getPageTaskAccessor();
				int pageIndex = rowIndex % numPagesOfMaster;
				PageTask pageTask = pageTaskAccessor.get(pageID, pageIndex);
				Config config = sourceDirectory.getConfiguration().getConfig();
				SourceConfig sourceConfig = config.getSourceConfig(pageID.getFileResourceID().getRelativePath(), pageID.getIndexInFile());
				
				float threshold = sourceConfig.getMarkRecognitionConfig().getMarkRecognitionDensityThreshold();
				Bag<String> numMarkedBag = new HashBag<String>();
				
				if(pageTask != null){
					OMRProcessorResult pageTaskResult = pageTask.getResult();
					if(pageTaskResult != null){
						
						List<PageAreaResult> pageAreaResultList = pageTaskResult.getPageAreaResultList();

						new FormAreaImageWriter();
						
						/*
							FormAreaResult formAreaResult = (FormAreaResult)pageAreaResult;
							double density = formAreaResult.getDensity();
							if(density < threshold){
								numMarkedBag.add(pageAreaResult.getID());
						*/
						
						return SourceDirectoryPanel.PROCESS_SUCCEED_MESSAGE;
					}
					OMRProcessorErrorModel errorModel = pageTask.getErrorModel();
					if(errorModel != null){
						return OMRProcessorErrorMessages.get(errorModel);
					}
				}
				return SourceDirectoryPanel.PROCESS_WAIT_MESSAGE;
		} catch (ConfigSchemeException e) {
			return "ERROR: ConfigSchemeException";
		}catch(IOException ignore){
			return "ERROR: IOException";
			}
		}
		return null;
	}
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		}
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(sourceDirectory == null){
			return;
		}
		switch(columnIndex){
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			break;
		case 5:
			/*
			 PageID pageID = sourceDirectory.getPageIDList().get(rowIndex);
			 TODO: set cell value at PageIDTableModel
		{
			String message = (aValue instanceof String)? ((String)aValue) : aValue.toString();
			sessionModel.session.getResultMessageMap().put(pageID, message);
		}*/
			break;
		}
	}
}
