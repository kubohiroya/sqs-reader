/**
 * ChartExportModule.java

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

package net.sqs2.omr.result.export.chart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.model.MarkReaderConfiguration;
import net.sqs2.omr.result.export.ResultDirectoryUtil;
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
import freemarker.template.TemplateException;

public class ChartExportModule implements SpreadSheetTraverseEventListener{
	String skinName;

	public ChartExportModule(String skinName) {
		this.skinName = skinName;
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
	public void startSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent){
		File chartDirectoryFile = ResultDirectoryUtil.createResultSubDirectory(sourceDirectoryEvent.getSourceDirectory(), "CHART");
		chartDirectoryFile.mkdirs();
	}

	@Override
	public void startSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		// do nothing
	}

	@Override
	public void startRowGroup(RowGroupEvent rowGroupEvent) {
		// do nothing
	}

	@Override
	public void startRow(RowEvent rowEvent) {
		// do nothing
	}

	@Override
	public void startPage(PageEvent pageEvent) {
		// do nothing
	}

	@Override
	public void startQuestion(QuestionEvent questionEvent) {
		// do nothing
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
		// do nothing
	}

	@Override
	public void endRowGroup(RowGroupEvent rowGroupEvent) {
		// do nothing
	}

	@Override
	public void endSpreadSheet(SpreadSheetEvent spreadSheetEvent){
		SourceDirectoryEvent sourceDirectoryEvent = spreadSheetEvent.getSourceDirectoryEvent();
		File chartDirectoryFile = ResultDirectoryUtil.createResultSubDirectory(sourceDirectoryEvent.getSourceDirectory(), "CHART");
		try{
			long sessionID = spreadSheetEvent.getSourceDirectoryEvent().getMasterEvent().getSessionSourceEvent().getSessionID();
			ChartHTMLWriter chartExportModule = new ChartHTMLWriter(sessionID, chartDirectoryFile, skinName);
			
			for (Map.Entry<String, ArrayList<FormArea>> entry : sourceDirectoryEvent.getFormMaster()
					.getFormAreaListEntrySet()) {
				List<FormArea> formAreaList = entry.getValue();
				FormArea primaryFormArea = formAreaList.get(0);
				if (primaryFormArea.isSelectSingle()) {
					chartExportModule.drawPieChartImage(formAreaList, spreadSheetEvent.getSpreadSheet());
				} else if (primaryFormArea.isSelectMultiple()) {
					chartExportModule.drawBarChartImage(formAreaList, spreadSheetEvent.getSpreadSheet());
				}
			}
			chartExportModule.writeChartIndexHTMLFile(chartDirectoryFile, spreadSheetEvent.getSpreadSheet(),
					MarkReaderConfiguration.isEnabled(MarkReaderConfiguration.KEY_CHARTIMAGE));
	
		}catch(TemplateException ex){
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void endSessionSource(SessionSourceEvent sessionSourceEvent) {
		// do nothing
	}

	@Override
	public void endMaster(MasterEvent masterEvent) {
		// do nothing
	}

	@Override
	public void endSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
		// do nothing
	}

}
