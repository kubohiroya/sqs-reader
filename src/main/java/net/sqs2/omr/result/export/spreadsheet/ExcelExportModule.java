/**
 * ExcelExportModule.java

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

package net.sqs2.omr.result.export.spreadsheet;

import java.awt.image.BufferedImage;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.Answer;
import net.sqs2.omr.model.MarkAreaAnswer;
import net.sqs2.omr.model.MarkAreaAnswerItem;
import net.sqs2.omr.model.MarkRecognitionConfig;
import net.sqs2.omr.model.OMRProcessorErrorMessages;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.Row;
import net.sqs2.omr.model.SourceConfig;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.model.TextAreaAnswer;
import net.sqs2.omr.result.export.MarkAreaAnswerValueUtil;
import net.sqs2.omr.result.export.SpreadSheetExportUtil;
import net.sqs2.omr.session.traverse.MasterEvent;
import net.sqs2.omr.session.traverse.PageEvent;
import net.sqs2.omr.session.traverse.QuestionEvent;
import net.sqs2.omr.session.traverse.QuestionItemEvent;
import net.sqs2.omr.session.traverse.RowEvent;
import net.sqs2.omr.session.traverse.RowGroupEvent;
import net.sqs2.omr.session.traverse.SessionSourceEvent;
import net.sqs2.omr.session.traverse.SourceDirectoryEvent;
import net.sqs2.omr.session.traverse.SpreadSheetEvent;
import net.sqs2.omr.session.traverse.SpreadSheetTraverseEventListener;
import net.sqs2.spreadsheet.SpreadSheetWorkbook;
import net.sqs2.util.StringUtil;

import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


public class ExcelExportModule implements SpreadSheetTraverseEventListener{

	public static final String XLSX_SUFFIX = "xlsx";
	public static final int XLSX_MODE = 1;
	
	
	private static final short NUM_HEADER_ROWS = 6;
	private static final short NUM_HEADER_COLUMNS = 4;
	private static final char ITEM_SEPARATOR_CHAR = ',';

	
	private SpreadSheetWorkbook spreadSheetWorkbook;
	private short columnIndext = 0;
	private int rowIndex = 0;

	private float densityThreshold;
	private float doubleMarkSuppressionThreshold;
	private float noMarkSuppressionThreshold;

	XSSFObjectFactory spreadSheetObjectFactory = null;

	private OutputStream xlsOutputStream;
	
	public ExcelExportModule() {
	}

	public ExcelExportModule(OutputStream xlsOutputStream) {
		this.xlsOutputStream = xlsOutputStream;
	}

	public static String createRowMemberFilenames(int rowIndex, int numPages, List<PageID> pageIDList) {
		boolean separator = false;
		StringBuilder filenames = new StringBuilder();
		for (int pageIndex = 0; pageIndex < numPages; pageIndex++) {
			PageID pageID = pageIDList.get(rowIndex * numPages + pageIndex);
			if (separator) {
				filenames.append(ITEM_SEPARATOR_CHAR);
			} else {
				separator = true;
			}
			filenames.append(StringUtil.escapeTabSeparatedValues(new File(pageID.getFileResourceID().getRelativePath())
					.getName()));
		}
		return filenames.toString();
	}

	public String getSuffix(){
		return this.spreadSheetObjectFactory.getSuffix();
	}

	@Override
	public void startSessionSource(SessionSourceEvent sessionSourceEvent) {
		// do nothing
	}

	@Override
	public void startMaster(MasterEvent masterEvent) {
		// do nothing
	}

	@Override
	public void startSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
		MarkRecognitionConfig recognitionConfig = ((SourceConfig)sourceDirectoryEvent.getSourceDirectory()
				.getConfiguration().getConfig().getPrimarySourceConfig()).getMarkRecognitionConfig();
		this.densityThreshold = recognitionConfig.getMarkRecognitionDensityThreshold();
		this.doubleMarkSuppressionThreshold = recognitionConfig.getDoubleMarkErrorSuppressionThreshold();
		this.noMarkSuppressionThreshold = recognitionConfig.getNoMarkErrorSuppressionThreshold();
	}
	
	@Override
	public void startSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		this.spreadSheetObjectFactory = new XSSFObjectFactory();
		this.spreadSheetWorkbook = new SpreadSheetWorkbook(this.spreadSheetObjectFactory.createWorkbook());
		this.spreadSheetWorkbook.getWorkbook().createSheet("Sheet1");
		
		setNorthHeaderCellValues(spreadSheetEvent.getFormMaster());
		this.rowIndex = 0;
	}
	
	@Override
	public void startRowGroup(RowGroupEvent sourceDirectoryEvent) {
	}

	@Override
	public void startRow(RowEvent rowEvent) {
		SourceDirectory sourceDirectory = rowEvent.getRowGroupEvent().getSourceDirectory();
		MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMultiHashMap = rowEvent.getTaskErrorModelMultiHashMap();

		Cell[] cellArray = new Cell[NUM_HEADER_COLUMNS];
		
		for (short columnIndex = 0; columnIndex < NUM_HEADER_COLUMNS; columnIndex++) {
			Sheet sheet = this.spreadSheetWorkbook.getWorkbook().getSheetAt(0);
			cellArray[columnIndex] = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), rowEvent.getRowIndex()+NUM_HEADER_ROWS, columnIndex);
		}
 
		
		int rowIndexInThisRowGroup = rowEvent.getRowGroupEvent().getRowIndexBase() + rowEvent.getIndex() + 1;
		cellArray[0].setCellValue(rowIndexInThisRowGroup);
		String path = sourceDirectory.getRelativePath();
		cellArray[1].setCellValue(createRichTextString(path));
		String fileNames = rowEvent.createRowMemberFilenames(',');
		cellArray[2].setCellValue(createRichTextString(fileNames));
		if (taskErrorModelMultiHashMap != null) {
			setExceptionCell(taskErrorModelMultiHashMap, cellArray);
		}
		this.columnIndext = 0;
	}

	@Override
	public void startPage(PageEvent rowEvent) {
	}

	@Override
	public void startQuestion(QuestionEvent questionEvent) {
		int rowIndex = questionEvent.getRowEvent().getRowIndex();
		int columnIndex = questionEvent.getQuestionIndex();
		List<FormArea> formAreaList = questionEvent.getFormMaster().getFormAreaList(columnIndex);
		FormArea primaryFormArea = formAreaList.get(0);
		Row row = questionEvent.getRowEvent().getRow();
		if (row == null) {
			return;
		}
		Answer answer = row.getAnswer(columnIndex);
		if (answer == null) {
			setCellValueAsNull(rowIndex);
			return;
		}
		if (primaryFormArea.isSelectSingle()) {
			setCellValueAsSelectSingle(rowIndex, formAreaList, (MarkAreaAnswer) answer, this.densityThreshold,
					this.doubleMarkSuppressionThreshold,
					this.noMarkSuppressionThreshold);
		} else if (primaryFormArea.isSelectMultiple()) {
			setCellValueAsSelectMultiple(rowIndex, (MarkAreaAnswer) answer, this.densityThreshold);
		} else if (primaryFormArea.isTextArea()) {
			setCellValueAsTextArea(rowIndex, questionEvent.getRowEvent().getRowGroupEvent().getParentSourceDirectory().getDirectory(), (TextAreaAnswer) answer);

		}
	}

	@Override
	public void startQuestionItem(QuestionItemEvent questionEvent) {
	}

	
	@Override
	public void endQuestionItem(QuestionItemEvent questionItemEvent) {
		// do nothing
	}

	@Override
	public void endQuestion(QuestionEvent questionEvent) {
		// do nothing
	}

	@Override
	public void endPage(PageEvent pageEvent) {
		// do nothing
	}

	@Override
	public void endRow(RowEvent rowEvent) {
		this.rowIndex++;
	}

	@Override
	public void endRowGroup(RowGroupEvent rowGroupEvent) {
		// do nothing
	}

	@Override
	public void endSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		try {
			OutputStream xlsOutputStream = null;
			if(this.xlsOutputStream != null){
				xlsOutputStream = this.xlsOutputStream;
			}else{
				File xlsFile = SpreadSheetExportUtil.createSpreadSheetImgFile(spreadSheetEvent, getSuffix());
				xlsOutputStream = new BufferedOutputStream(new FileOutputStream(xlsFile));
			}
			this.spreadSheetWorkbook.writeTo(xlsOutputStream);
			xlsOutputStream.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void endSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
		// do nothing
	}

	@Override
	public void endMaster(MasterEvent masterEvent) {
		// do nothing
	}

	@Override
	public void endSessionSource(SessionSourceEvent sessionSourceEvent) {
		// do nothing
	}
	
	private SpreadSheetObjectFactory createSpreadSheetObjectFactory(){
		return new XSSFObjectFactory();
	}

	private void setExceptionCell(MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMultiHashMap, Cell[] cellArray) {
		String errorMessage = null;
		for (PageID pageID : taskErrorModelMultiHashMap.keySet()) {
			if(pageID == null){
				continue;
			}
			for (OMRProcessorErrorModel pageTaskErrorModel : taskErrorModelMultiHashMap.get(pageID)) {
				if (errorMessage == null) {
					errorMessage = "";
				} else {
					errorMessage += "\n";
				}
				errorMessage += pageID.getFileResourceID().getRelativePath() + "=" + OMRProcessorErrorMessages.get(pageTaskErrorModel);
			}
		}
		Cell errorCell = cellArray[3];
		errorCell.setCellValue(createRichTextString(errorMessage));
		errorCell.setCellStyle(this.spreadSheetObjectFactory.getErrorCellStyle(this.spreadSheetWorkbook));
	}

	private short getNumQIDColumns(List<FormArea> formAreaList) {
		FormArea primaryFormArea = formAreaList.get(0);
		short numQIDColumns = 0;
		if (primaryFormArea.isSelectMultiple()) {
			numQIDColumns = (short) formAreaList.size();
		} else {
			numQIDColumns = 1;
		}
		return numQIDColumns;
	}

	private void setNorthHeaderCellValues(FormMaster master) {
		int columnIndex = NUM_HEADER_COLUMNS;
		for (String qid : master.getQIDSet()) {
			List<FormArea> formAreaList = master.getFormAreaList(qid);
			if (formAreaList == null || formAreaList.size() == 0) {
				continue;
			}
			FormArea primaryFormArea = formAreaList.get(0);
			if (primaryFormArea.isSelectSingle() || primaryFormArea.isTextArea()) {
				setHeaderCellValuesAsQuestion(columnIndex, primaryFormArea);
				setHeaderCellValuesAsSelectSingle(columnIndex, formAreaList, primaryFormArea);
				columnIndex++;
			} else if (primaryFormArea.isSelectMultiple()) {
				for (FormArea formArea : formAreaList) {
					setHeaderCellValuesAsQuestion(columnIndex, primaryFormArea);
					setHeaderCellValuesAsSelectMultiple(columnIndex, formArea);
					columnIndex++;
				}
			}
		}
	}

	private void setHeaderCellValuesAsQuestion(int columnIndex, FormArea primaryFormArea) {
		Sheet sheet = this.spreadSheetWorkbook.getWorkbook().getSheetAt(0);
		
		Cell pageCell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), 0, columnIndex) ;
		RichTextString pageString = createRichTextString(Integer.toString(primaryFormArea.getPage()));
		pageCell.setCellValue(pageString);
	
		Cell qidCell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), 1, columnIndex) ;
		RichTextString qidString = createRichTextString(primaryFormArea.getQID());
		qidCell.setCellValue(qidString);
	
		Cell typeCell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), 2, columnIndex) ;
		RichTextString typeString = createRichTextString(primaryFormArea.getType());
		typeCell.setCellValue(typeString);
	
		Cell hintsCell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), 3, columnIndex) ;
		RichTextString hintsString = createRichTextString(StringUtil.join(primaryFormArea.getHints(), " "));
		hintsCell.setCellValue(hintsString);
	}

	private void setHeaderCellValuesAsSelectMultiple(int columnIndex, FormArea formArea) {
		Sheet sheet = this.spreadSheetWorkbook.getWorkbook().getSheetAt(0);
		
		Cell itemLabelCell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), 4, columnIndex) ;
		RichTextString itemLabelString = createRichTextString(formArea.getItemLabel());
		itemLabelCell.setCellValue(itemLabelString);
	
		Cell itemValueCell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), 5, columnIndex) ;
		RichTextString itemValueString = createRichTextString(formArea.getItemValue());
		itemValueCell.setCellValue(itemValueString);
	}

	private void setHeaderCellValuesAsSelectSingle(int columnIndex, List<FormArea> formAreaList, FormArea primaryFormArea) {
		if (primaryFormArea.isSelectSingle()) {
			StringBuilder itemLabelString = null;
			StringBuilder itemValueString = null;
			
			for (FormArea formArea : formAreaList) {
				if (itemLabelString == null) {
					itemLabelString = new StringBuilder();
					itemValueString = new StringBuilder();
				} else {
					itemLabelString.append(ITEM_SEPARATOR_CHAR);
					itemValueString.append(ITEM_SEPARATOR_CHAR);
				}
				itemLabelString.append(formArea.getItemLabel());
				itemValueString.append(formArea.getItemValue());
			}
			if (itemLabelString != null) {
				Sheet sheet = this.spreadSheetWorkbook.getWorkbook().getSheetAt(0);
				
				Cell itemLabelCell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), 4, columnIndex) ;
				Cell itemValueCell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), 5, columnIndex) ;
				itemLabelCell.setCellValue(createRichTextString(itemLabelString.toString()));
				itemValueCell.setCellValue(createRichTextString(itemValueString.toString()));
			}
		}
	}

	private void setCellValue(Cell cell, String value) {
		try {
			double num = Double.parseDouble(value);
			cell.setCellValue(num);
		} catch (NullPointerException ex) {
			cell.setCellValue(createRichTextString(value));
		} catch (NumberFormatException ex) {
			cell.setCellValue(createRichTextString(value));
		}
	}

	private void setCellValueAsSelectSingle(int rowIndex, List<FormArea> formAreaList, MarkAreaAnswer markAreaAnswer, float densityThreshold,
			float doubleMarkErrorSuppressionThreshold,
			float noMarkErrorSuppressionThreshold) {
		Cell cell = getBodyCell(rowIndex);
		CellStyle style = this.spreadSheetObjectFactory.getSelectSingleCellStyle(this.spreadSheetWorkbook);
		String value = MarkAreaAnswerValueUtil.createSelectSingleMarkAreaAnswerValueString(densityThreshold,
				doubleMarkErrorSuppressionThreshold, 
				noMarkErrorSuppressionThreshold, markAreaAnswer, formAreaList, ITEM_SEPARATOR_CHAR);
		if ("".equals(value)) {
			style = this.spreadSheetObjectFactory.getNoAnswerCellStyle(this.spreadSheetWorkbook);
		} else if (0 <= value.indexOf(ITEM_SEPARATOR_CHAR)) {
			style = this.spreadSheetObjectFactory.getMultipleAnswersCellStyle(this.spreadSheetWorkbook);
		}
	
		cell.setCellStyle(style);
		setCellValue(cell, value);
		this.columnIndext++;
	}

	private void setCellValueAsSelectMultiple(int rowIndex, MarkAreaAnswer answer, float densityThreshold) {
		MarkAreaAnswerItem[] markAreaAnswerItemArray = answer.getMarkAreaAnswerItemArray();
		for (MarkAreaAnswerItem markAreaAnswerItem : markAreaAnswerItemArray) {
			Cell cell = getBodyCell(rowIndex);
			cell.setCellStyle(this.spreadSheetObjectFactory.getSelectMultipleCellStyle(this.spreadSheetWorkbook));
			if (markAreaAnswerItem.isSelectMultiSelected(answer, densityThreshold)) {
				setCellValue(cell, "1");
			} else {
				setCellValue(cell, "0");
			}
			this.columnIndext++;
		}
	}

	private void setCellValueAsTextArea(int rowIndex, File rootDirectory, TextAreaAnswer answer) {
		System.out.println("###"+rootDirectory.getAbsolutePath()+":"+rowIndex+":"+answer.getTextAreaImageFilePath());;
        if(rootDirectory == null) {
            throw new IllegalArgumentException("rootDirectory is null");
        }
        if(answer.getTextAreaImageFilePath() == null) {
            //throw new IllegalArgumentException("answer.getAreaImageFilePath() is null");
        }else{	
			Cell cell = getBodyCell(rowIndex);
			//String value = answer.getValue();
			//cell.setCellStyle(this.spreadSheetObjectFactory.getTextAreaCellStyle(this.spreadSheetWorkbook));
			//setCellValue(cell, value);
			try{
				File areaImageFile = new File(rootDirectory, answer.getTextAreaImageFilePath());
				ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
				BufferedImage img = ImageIO.read(areaImageFile);
				String extension = FilenameUtils.getExtension(areaImageFile.getName());
				ImageIO.write(img,  extension, byteArrayOut);
				org.apache.poi.ss.usermodel.Row row = cell.getRow();
				row.setHeight((short)img.getHeight());
				Workbook wb = this.spreadSheetWorkbook.getWorkbook();
				Sheet sheet = wb.getSheetAt(0);
				Drawing drawing = sheet.createDrawingPatriarch();
				ClientAnchor anchor = drawing.createAnchor(0, 0, img.getWidth() - 1, img.getHeight() - 1, cell.getColumnIndex(), rowIndex, 0, 0);
	            anchor.setAnchorType(0);
	            int picIndex = wb.addPicture(byteArrayOut.toByteArray(), Workbook.PICTURE_TYPE_PNG);
	            Picture pic = drawing.createPicture(anchor, picIndex);
	
			}catch(IOException e){
				e.printStackTrace();
			}		
        }
		this.columnIndext++;
	}

	private void setCellValueAsNull(int rowIndex){
		Cell cell = getBodyCell(rowIndex);
		cell.setCellStyle(this.spreadSheetObjectFactory.getErrorCellStyle(this.spreadSheetWorkbook));
		setCellValue(cell, "");
		this.columnIndext++;
	}
	
	private RichTextString createRichTextString(String value){
		return this.spreadSheetObjectFactory.createRichTextString(value);	
	}

	private Cell getBodyCell(int rowIndex) {
		Sheet sheet = this.spreadSheetWorkbook.getWorkbook().getSheetAt(0);
		Cell cell = this.spreadSheetWorkbook.getCell(sheet.getSheetName(), this.rowIndex + NUM_HEADER_ROWS, this.columnIndext + NUM_HEADER_COLUMNS);
		return cell;
	}

}
