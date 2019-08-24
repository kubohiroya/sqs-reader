/*
dddd  Copyright 2011 KUBO Hiroya (hiroya@cuc.ac.jp).
  
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

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.sqs2.omr.model.OMRProcessorErrorMessageUtil;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.PageTaskAccessor;
import net.sqs2.omr.model.RowAccessor;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.util.FileUtil;

public class ResultTableModel extends AbstractTableModel implements TableModel{
	
	private static final long serialVersionUID = 1;
	public static final String[] COLUMN_NAMES = new String[]{"SourceDirectory", "Filenames", "ID", "#N/A", "#Multi", "Status"};
	public static final String[] COLUMN_TOOLTIP_TEXT = new String[]{"SourceDirectory", "Filenames", "Row ID", "Number of No Answer Errors", "Number of Multiple Answer Errors", "Row Status"};
	public static final int[] COLUMN_WIDTH_RATIO = new int[]{100,200,30,30,30, 200};

	public static final int RESULT_MESSAGE_COLUMN_INDEX = 5;
	
	SessionModel sessionModel;
	SourceDirectory sourceDirectory;
		
	ResultTableModel(SessionModel sessionModel, SourceDirectory sourceDirectory){
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
	
	public void clear(){
		this.sourceDirectory = null;
	}
	
	@Override
	public int getRowCount() {
		if(sourceDirectory == null){
			return 0;
		}
		return sourceDirectory.getNumPageIDs() / sourceDirectory.getCurrentFormMaster().getNumPages();
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex){
		case 0:
		case 1:
		case 5:
			return String.class;
		case 2:
		case 3:
		case 4:
			return Integer.class;
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(sourceDirectory == null){
			return null;
		}

		List<PageID> pageIDList = sourceDirectory.getPageIDList();
		int numPagesOfMaster = sourceDirectory.getCurrentFormMaster().getNumPages();
				
		switch(columnIndex){
		case 0:
			return sourceDirectory.getRelativePath();
		case 1:
			return createFileNames(pageIDList, numPagesOfMaster, rowIndex);
		case 5:
			return createErrorMessages(pageIDList, numPagesOfMaster, rowIndex);
		case 2:
			return new Integer(rowIndex);
		case 3:
			return new Integer(0);//FIXME to set Num of N/A error items
		case 4:
			return new Integer(0);//FIXME to set Num of Multi error items
		}
		return null;
	}

	private String createFileNames(List<PageID> pageIDList, int numPagesOfMaster, int rowIndex) {
		int base = rowIndex * numPagesOfMaster;
		StringBuilder ret = new StringBuilder();
		for(int i = 0; i < numPagesOfMaster; i++){
			if(0 < i){
				ret.append(' ');
			}
			ret.append(FileUtil.getName(pageIDList.get(base+i).getFileResourceID().getRelativePath()));
		}
		return ret.toString();
	}
	
	private String createErrorMessages(List<PageID> pageIDList, int numPagesOfMaster, int rowIndex) {
		RowAccessor rowAccessor = sessionModel.getMarkReaderSession().getSessionSource().getContentAccessor().getRowAccessor();
		PageTaskAccessor pageTaskAccessor = sessionModel.getMarkReaderSession().getSessionSource().getContentAccessor().getPageTaskAccessor();
		return OMRProcessorErrorMessageUtil.createErrorMessage(rowAccessor, pageTaskAccessor, sourceDirectory, pageIDList, numPagesOfMaster, rowIndex);
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
		/*
		List<PageID> pageIDList = sourceDirectory.getPageIDList();
		int numPagesOfMaster = sourceDirectory.getCurrentFormMaster().getNumPages();
		int base = rowIndex / numPagesOfMaster;
		 */
		switch(columnIndex){
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			break;
		}
	}
}
