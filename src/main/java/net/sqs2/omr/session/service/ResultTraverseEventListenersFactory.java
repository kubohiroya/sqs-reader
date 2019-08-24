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

package net.sqs2.omr.session.service;

import java.util.ArrayList;
import java.util.List;

import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.MarkReaderConfiguration;
import net.sqs2.omr.result.export.chart.ChartExportModule;
import net.sqs2.omr.result.export.html.HTMLReportExportModule;
import net.sqs2.omr.result.export.spreadsheet.CSVExportModule;
import net.sqs2.omr.result.export.spreadsheet.ExcelExportModule;
import net.sqs2.omr.result.export.spreadsheet.ExcelExportModule;
import net.sqs2.omr.result.export.textarea.TextAreaExportModule;
import net.sqs2.omr.result.export.xml.XMLExportModule;
import net.sqs2.omr.session.traverse.DummyExportModule;
import net.sqs2.omr.session.traverse.MarkReadStatusRetriever;
import net.sqs2.omr.session.traverse.SpreadSheetTraverseEventListener;

public class ResultTraverseEventListenersFactory{
	
	public enum ModuleSetType{DEFAULT, USER_CONFIG, DEBUG}
	
	protected ModuleSetType moduleSetType = null;
	
	
	CSVExportModule.Param csvExportModuleParam;
	
	public ResultTraverseEventListenersFactory(ModuleSetType moduleSetType){
		this.moduleSetType = moduleSetType;
		this.csvExportModuleParam = createCSVExportModuleParam();
	}
	
	public List<SpreadSheetTraverseEventListener> create() {
		if(moduleSetType == null){
			throw new IllegalArgumentException();
		}
		switch(moduleSetType){
		case DEFAULT:
			return createDefaultExportModules();
		case USER_CONFIG:
			return createUserConfiguredExportModules();
		case DEBUG:
			return createDebugingExportModules();
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private static CSVExportModule.Param createCSVExportModuleParam(){
		final String encoding = "x-UTF-16LE-BOM";
		final String filenameSuffix = "csv";
		final String columnSeparator = "\t";
		final String itemSeparator = ",";
		final boolean verbosePrintPage = true;
		final boolean verbosePrintQID = true;
		return new CSVExportModule.Param(encoding, filenameSuffix, columnSeparator, itemSeparator, verbosePrintPage, verbosePrintQID);
	}
	
	protected List<SpreadSheetTraverseEventListener> createBaseExportModules() {
		List<SpreadSheetTraverseEventListener> listeners = new ArrayList<SpreadSheetTraverseEventListener>();
		listeners.add(new MarkReadStatusRetriever());
		return listeners;
	}
	
	protected List<SpreadSheetTraverseEventListener> createDefaultExportModules() {
		List<SpreadSheetTraverseEventListener> listeners = createBaseExportModules();
		// listeners.add(new DummyExportModule());
		listeners.add(new XMLExportModule());
		listeners.add(new CSVExportModule(csvExportModuleParam));
		listeners.add(new ExcelExportModule());
		listeners.add(new HTMLReportExportModule(AppConstants.SKIN_ID, csvExportModuleParam.getSuffix(), "xlsx"));
		listeners.add(new ChartExportModule(AppConstants.SKIN_ID));
		listeners.add(new TextAreaExportModule(AppConstants.SKIN_ID));
		listeners.add(new ExcelExportModule());
		return listeners;
	}
	
	protected List<SpreadSheetTraverseEventListener> createDebugingExportModules() {
		List<SpreadSheetTraverseEventListener> listeners = createBaseExportModules();
		listeners.add(new DummyExportModule());
		return listeners; 
	}

	protected List<SpreadSheetTraverseEventListener> createUserConfiguredExportModules() {
		List<SpreadSheetTraverseEventListener> listeners = createBaseExportModules();

		if ( MarkReaderConfiguration.isEnabled(MarkReaderConfiguration.KEY_SPREADSHEET)) {
			listeners.add(new CSVExportModule(csvExportModuleParam));
			listeners.add(new ExcelExportModule());
		}
		if ( MarkReaderConfiguration.isEnabled(MarkReaderConfiguration.KEY_TEXTAREA)
				|| MarkReaderConfiguration.isEnabled(MarkReaderConfiguration.KEY_CHART)) {
			listeners.add(new HTMLReportExportModule(AppConstants.SKIN_ID, csvExportModuleParam.getSuffix(), "xlsx"));
		
			if ( MarkReaderConfiguration.isEnabled(MarkReaderConfiguration.KEY_CHART)) {
				listeners.add(new ChartExportModule(AppConstants.SKIN_ID));
			}
			if ( MarkReaderConfiguration.isEnabled(MarkReaderConfiguration.KEY_TEXTAREA)) {
				listeners.add(new TextAreaExportModule(AppConstants.SKIN_ID));
			}
			
			MarkReadStatusRetriever markReadStatusRetriever = new MarkReadStatusRetriever();
			listeners.add(markReadStatusRetriever);

		}
		return listeners; 
	}

}