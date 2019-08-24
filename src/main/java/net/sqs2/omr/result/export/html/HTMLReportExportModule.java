/**
 * HTMLReportExporter.java

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

package net.sqs2.omr.result.export.html;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import net.sqs2.omr.base.Messages;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.result.export.ResultDirectoryUtil;
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
import net.sqs2.omr.util.JarExtender;
import net.sqs2.template.TemplateLoader;
import net.sqs2.util.PathUtil;
import net.sqs2.xml.PrefixResolverImpl;

import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;

import freemarker.template.Template;

public class HTMLReportExportModule implements SpreadSheetTraverseEventListener{

	String skinName;
	String csvFilenameSuffix;
	String excelFilenameSuffix;

	public HTMLReportExportModule(String skinName, String csvFilenameSuffix, String excelFilenameSuffix) {
		this.skinName = skinName;
		this.csvFilenameSuffix = csvFilenameSuffix;
		this.excelFilenameSuffix = excelFilenameSuffix;
	}
	
	@Override
	public void startSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
		// do nothing
		
	}

	@Override
	public void startMaster(MasterEvent masterEvent) {
		// do nothing
		
	}

	@Override
	public void startSessionSource(SessionSourceEvent sessionSourceEvent) {
		// do nothing
		
	}

	@Override
	public void startSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		try{
			exportReport(spreadSheetEvent, this.skinName, this.csvFilenameSuffix, this.excelFilenameSuffix);
		}catch(IOException ex){
			ex.printStackTrace();
		}
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
	public void endPage(PageEvent pageEvent) {
		// do nothing
		
	}

	@Override
	public void endQuestion(QuestionEvent questionEvent) {
		// do nothing
		
	}

	@Override
	public void endQuestionItem(QuestionItemEvent questionItemEvent) {
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
	public void endSpreadSheet(SpreadSheetEvent spreadSheetEvent) {
		// do nothing
		
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

	public void exportReport(SpreadSheetEvent spreadSheetEvent, String skinName, String csvFilenameSuffix, String excelFilenameSuffix) throws IOException {
	
		SourceDirectoryEvent sourceDirectoryEvent = spreadSheetEvent.getSourceDirectoryEvent();
		File sourceDirectoryFile = sourceDirectoryEvent.getSourceDirectory().getDirectory();
		File resultDirectoryFile = new File(sourceDirectoryFile, AppConstants.RESULT_DIRNAME);
	
		File resultDirectoryIndexFile = new File(resultDirectoryFile, "index.html");
	
		new JarExtender().extend(new String[] { "css/"+skinName+".css" }, resultDirectoryFile);
	
		PrintWriter resultDirectoryIndexWriter = ResultDirectoryUtil.createPrintWriter(resultDirectoryIndexFile);
	
		try {
			Map<String, Object> map = new HashMap<String, Object>();
	
			registTitle(spreadSheetEvent.getFormMaster(), map);
			File csvFileName = SpreadSheetExportUtil.createSpreadSheetFile(spreadSheetEvent,
					csvFilenameSuffix);
			File xlsFileName = SpreadSheetExportUtil.createSpreadSheetFile(spreadSheetEvent,
					excelFilenameSuffix);
					
			String csvFilePath = PathUtil.getRelativePath(csvFileName, resultDirectoryFile, File.separatorChar);
			String xlsFilePath = PathUtil.getRelativePath(xlsFileName, resultDirectoryFile, File.separatorChar);
			
			map.put("path", spreadSheetEvent.getSpreadSheet().getSourceDirectory().getRelativePath());			
			map.put("csvFilePath", csvFilePath);
			map.put("xlsFilePath", xlsFilePath);
			map.put("resultFolderName", AppConstants.RESULT_DIRNAME);
			if(sourceDirectoryEvent.getSourceDirectory().getChildSourceDirectoryList() != null){
				map.put("numChildSourceDirectories", sourceDirectoryEvent.getSourceDirectory().getChildSourceDirectoryList().size());
			}else{
				map.put("numChildSourceDirectories", 0);
			}
			map.put("showSubFolders", Messages.RESULT_DIRECTORYINDEX_SUBFOLDERS_LABEL);
			map.put("sourceDirectory", sourceDirectoryEvent.getSourceDirectory());
			
			TemplateLoader loader = new TemplateLoader(AppConstants.USER_CUSTOMIZED_CONFIG_DIR,
					"ftl", skinName);
			Template resultDirectoryIndexTemplate = loader.getTemplate("index.ftl", "UTF-8");
			registDirectoryIndexParameters(map);
			resultDirectoryIndexTemplate.process(map, resultDirectoryIndexWriter);
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
		resultDirectoryIndexWriter.close();
	}

	public static void registTitle(FormMaster master, Map<String, Object> map) {
		Document document = master.getPageMasterMetadata().getSourceDocument();
		try {
			if (document != null) {
				PrefixResolver prefixResolver = new PrefixResolverImpl(document.getDocumentElement());
				String title = null;
				XObject xobj = XPathAPI.eval(document.getDocumentElement(),
						"/xhtml2:html/xhtml2:head/xhtml2:title", prefixResolver);
				title = xobj.str();
				map.put("title", title);
				return;
			}
		} catch (TransformerException ignore) {
		}
		map.put("title", "");
	}

	public String getSkinName(){
		return skinName;
	}

	private void registDirectoryIndexParameters(Map<String, Object> map){
		for(String key: new String[]{
				"folderPrefixLabel",
				"contentsOfResultLabel",
				"listOfSpreadSheetsLabel",
				"xlsSpreadSheetLabel",
				"csvSpreadSheetLabel",
				"listOfResultsLabel",
				"listOfFreeAnswersLabel",
				"listOfStatisticsLabel"
		}){
			map.put(key, Messages._("result.directoryIndex."+key));
		}
		map.put("skin", AppConstants.GROUP_ID);
	}
}
