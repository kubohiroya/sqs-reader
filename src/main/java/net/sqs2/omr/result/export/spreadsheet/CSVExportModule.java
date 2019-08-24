/**
 * CSVExportModule.java

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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.Answer;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.MarkAreaAnswer;
import net.sqs2.omr.model.MarkAreaAnswerItem;
import net.sqs2.omr.model.OMRProcessorErrorMessages;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.TextAreaAnswer;
import net.sqs2.omr.result.export.SpreadSheetExportUtil;
import net.sqs2.omr.session.traverse.PageEvent;
import net.sqs2.omr.session.traverse.QuestionEvent;
import net.sqs2.omr.session.traverse.QuestionItemEvent;
import net.sqs2.omr.session.traverse.RowEvent;
import net.sqs2.omr.session.traverse.RowGroupEvent;
import net.sqs2.omr.session.traverse.SpreadSheetEvent;
import net.sqs2.util.StringUtil;

import org.apache.commons.collections15.multimap.MultiHashMap;

public class CSVExportModule extends AbstractExportModule {

	public static class Param{
		String encoding;
		String suffix;
		String columnSeparator;
		String itemSeparator;
		boolean verbosePrintPage;
		boolean verbosePrintQID;
		public Param(String encoding, String suffix, String columnSeparator, String itemSeparator, boolean verbosePrintPage, boolean verbosePrintQID){
			this.encoding = encoding;
			this.suffix = suffix;
			this.columnSeparator = columnSeparator;
			this.itemSeparator = itemSeparator;
			this.verbosePrintPage = verbosePrintPage;
			this.verbosePrintQID = verbosePrintQID;
		}
		public String getEncoding() {
			return encoding;
		}
		public String getSuffix() {
			return suffix;
		}
		public String getColumnSeparator() {
			return columnSeparator;
		}
		public String getItemSeparator() {
			return itemSeparator;
		}
		public boolean isVerbosePrintPage() {
			return verbosePrintPage;
		}
		public boolean isVerbosePrintQID() {
			return verbosePrintQID;
		}
	}
	
	
	PrintWriter exportingCsvWriter;
	PrintWriter csvWriter;

	Param param;
	
	public CSVExportModule(Param param) {
		this.param = param;
	}

	public CSVExportModule(Param param, PrintWriter exportingCsvWriter) {
		this(param);
		this.exportingCsvWriter = exportingCsvWriter;
	}

	
	@Override
	public void startSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		try {
			if(this.exportingCsvWriter != null){
				this.csvWriter = this.exportingCsvWriter;
			}else{
				File resultDirectory = new File(spreadSheetEvent.getSpreadSheet().getSourceDirectory().getDirectory().getAbsoluteFile(), AppConstants.RESULT_DIRNAME);
				resultDirectory.mkdirs();
				File csvFile = SpreadSheetExportUtil.createSpreadSheetFile(spreadSheetEvent, param.suffix);
				this.csvWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(csvFile)), param.encoding));
			}
			printCSVHeaderRow(this.csvWriter, spreadSheetEvent.getFormMaster());
			this.csvWriter.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void startRowGroup(RowGroupEvent rowGroupEvent) {
	}

	@Override
	public void startRow(RowEvent rowEvent) {
		int rowIndexInThisRowGroup = rowEvent.getRowGroupEvent().getRowIndexBase() + rowEvent.getIndex() + 1;
		this.csvWriter.print(rowIndexInThisRowGroup);
		this.csvWriter.print(param.columnSeparator);
		this.csvWriter.print(rowEvent.getRowGroupEvent().getSourceDirectory().getRelativePath());
		this.csvWriter.print(param.columnSeparator);
		this.csvWriter.print(rowEvent.createRowMemberFilenames(','));
		this.csvWriter.print(param.columnSeparator);

		for (PageID pageID : rowEvent.getPageIDList()) {
			Collection<OMRProcessorErrorModel> errorModelList = rowEvent.getTaskErrorModelMultiHashMap(pageID);
			if(errorModelList != null){
	            final StringBuffer sb = new StringBuffer();
	            for (OMRProcessorErrorModel errorModel : errorModelList) {
					if (sb.length()!=0) sb.append(" + ");
				    sb.append(pageID.getFileResourceID().getRelativePath() + "=" + OMRProcessorErrorMessages.get(errorModel));
				}
				this.csvWriter.print(sb.toString());
			}
		}
	}

	@Override
	public void startPage(PageEvent rowEvent) {
	}

	@Override
	public void startQuestion(QuestionEvent questionEvent) {
		MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap = questionEvent.getRowEvent().getTaskErrorModelMultiHashMap();
		//int numPageIDsWithTaskErrors = taskErrorModelMap.size();
		FormArea primaryFormArea = questionEvent.getPrimaryFormArea();
		questionEvent.getPrimaryFormArea();
	
		Answer answer = questionEvent.getAnswer();
		if (primaryFormArea.isSelectSingle()) {
			writeSelectSingleAnswer(questionEvent, taskErrorModelMap, answer);
		} else if (primaryFormArea.isSelectMultiple()) {
			writeSelectMultipleAnswer(questionEvent, taskErrorModelMap, answer);
		} else if (primaryFormArea.isTextArea()) {
			writeTextAreaAnswer(taskErrorModelMap, answer);
		}
	}

	@Override
	public void startQuestionItem(QuestionItemEvent questionItemEvent) {
		// do nothing
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
		this.csvWriter.print("\n");
	}

	@Override
	public void endRowGroup(RowGroupEvent rowGroupEvent) {
		// do nothing
	}

	@Override
	public void endSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		this.csvWriter.close();
	}

	

	private void writeHeaderItemLabelRow(PrintWriter csvWriter, FormMaster master) {
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
	
		String prevQID = null;
	
		for (FormArea area : master.getFormAreaList()) {
			if (area.isTextArea()) {
				csvWriter.print(param.columnSeparator);
				continue;
			}
			switch (area.getTypeCode()) {
			case FormArea.SELECT_MULTIPLE:
				csvWriter.print(param.columnSeparator);
				csvWriter.print(StringUtil.escapeTabSeparatedValues(area.getItemLabel()));
				break;
			case FormArea.SELECT_SINGLE:
				if (area.getQID().equals(prevQID)) {
					csvWriter.print(param.itemSeparator);
				} else {
					csvWriter.print(param.columnSeparator);
				}
				prevQID = area.getQID();
				csvWriter.print(StringUtil.escapeTabSeparatedValues(area.getItemLabel()));
				break;
			}
		}
		csvWriter.println();
	}

	private void writeHeaderHintsRow(PrintWriter csvWriter, FormMaster master) {
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		for (FormArea area : master.getFormAreaList()) {
			if (area.getTypeCode() == FormArea.SELECT_SINGLE && 0 < area.getItemIndex()) {
				continue;
			}
			csvWriter.print(param.columnSeparator);
			if (!param.verbosePrintQID && area.isMarkArea() && 0 < area.getItemIndex()) {
				continue;
			}
			csvWriter.print(StringUtil.escapeTabSeparatedValues(StringUtil.join(area.getHints(), "")));
		}
		csvWriter.println();
	}

	private void writeHeaderTypeRow(PrintWriter csvWriter, FormMaster master) {
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		for (FormArea area : master.getFormAreaList()) {
			if (area.getTypeCode() == FormArea.SELECT_SINGLE && 0 < area.getItemIndex()) {
				continue;
			}
			csvWriter.print(param.columnSeparator);
			if (! param.verbosePrintQID && area.isMarkArea() && 0 < area.getItemIndex()) {
				continue;
			}
			csvWriter.print(area.getType());
		}
		csvWriter.println();
	}

	private void writeHeaderQIDRow(PrintWriter csvWriter, FormMaster master) {
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		for (FormArea area : master.getFormAreaList()) {
			if (area.getTypeCode() == FormArea.SELECT_SINGLE && 0 < area.getItemIndex()) {
				continue;
			}
			csvWriter.print(param.columnSeparator);
			if (! param.verbosePrintQID && area.isMarkArea() && 0 < area.getItemIndex()) {
				continue;
			}
			csvWriter.print(area.getQID());
		}
		csvWriter.println();
	}

	private void writeHeaderPageRow(PrintWriter csvWriter, FormMaster master) {
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		for (FormArea area : master.getFormAreaList()) {
			if (area.getTypeCode() == FormArea.SELECT_SINGLE && 0 < area.getItemIndex()) {
				continue;
			}
	
			csvWriter.print(param.columnSeparator);
	
			if (! param.verbosePrintPage && area.isMarkArea() && 0 < area.getItemIndex()) {
				continue;
			}
			csvWriter.print(area.getPage());
		}
		csvWriter.println();
	}

	private void writeHeaderItemValueRow(PrintWriter csvWriter, FormMaster master) {
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		csvWriter.print(param.columnSeparator);
		String prevQID = null;
	
		for (FormArea area : master.getFormAreaList()) {
			if (area.isTextArea()) {
				csvWriter.print(param.columnSeparator);
				continue;
			}
			switch (area.getTypeCode()) {
			case FormArea.SELECT_MULTIPLE:
				csvWriter.print(param.columnSeparator);
				csvWriter.print(StringUtil.escapeTabSeparatedValues(area.getItemValue()));
				break;
			case FormArea.SELECT_SINGLE:
				if (area.getQID().equals(prevQID)) {
					csvWriter.print(param.itemSeparator);
				} else {
					csvWriter.print(param.columnSeparator);
				}
				prevQID = area.getQID();
				csvWriter.print(StringUtil.escapeTabSeparatedValues(area.getItemValue()));
				break;
			}
		}
		csvWriter.println();
	}

	private void printCSVHeaderRow(PrintWriter csvWriter, FormMaster master) {
		writeHeaderPageRow(csvWriter, master);
		writeHeaderQIDRow(csvWriter, master);
		writeHeaderTypeRow(csvWriter, master);
		writeHeaderHintsRow(csvWriter, master);
		writeHeaderItemLabelRow(csvWriter, master);
		writeHeaderItemValueRow(csvWriter, master);
	}

	private void writeSelectSingleAnswer(QuestionEvent questionEvent, MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap, Answer answer) {
		this.csvWriter.print(param.columnSeparator);
		if (!checkWriteAnswerPrecondition(taskErrorModelMap, answer)) return;
		final String value = getSingleValue(questionEvent, ((MarkAreaAnswer) answer));
        this.csvWriter.print(StringUtil.escapeTabSeparatedValues(value));
	}

	private void writeTextAreaAnswer(MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap, Answer answer) {
		this.csvWriter.print(param.columnSeparator);
		if (!checkWriteAnswerPrecondition(taskErrorModelMap, answer)) return;
        String value = ((TextAreaAnswer) answer).getValue();
		if (value != null) {
			this.csvWriter.print(StringUtil.escapeTabSeparatedValues(value));
		}
	}

	private void writeSelectMultipleAnswer(final QuestionEvent questionEvent, final MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMap, final Answer answer) {
	    if (!checkWriteAnswerPrecondition(taskErrorModelMap, answer)) return;
        int size = questionEvent.getFormAreaList().size();
		for (int itemIndex = 0; itemIndex < size; itemIndex++) {
			this.csvWriter.print(param.columnSeparator);
			MarkAreaAnswerItem answerItem = ((MarkAreaAnswer) answer).getMarkAreaAnswerItem(itemIndex);

			if (answerItem.isManualMode()) {
				if (answerItem.isManualSelected()) {
					this.csvWriter.print("1");
				} else {
					this.csvWriter.print("0");
				}
			} else {
				if (answerItem.getDensity() < this.densityThreshold) {
					this.csvWriter.print("1");
				} else {
					this.csvWriter.print("0");
				}
			}
		}
	}
}
