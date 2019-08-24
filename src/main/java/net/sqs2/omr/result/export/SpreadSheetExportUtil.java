/**
 * SpreadSheetExportModule.java

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

package net.sqs2.omr.result.export;

import java.io.File;
import java.io.IOException;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.session.traverse.SpreadSheetEvent;
import net.sqs2.util.FileUtil;

public abstract class SpreadSheetExportUtil{

	public static File createSpreadSheetFile(SpreadSheetEvent spreadSheetEvent, String suffix) throws IOException {
		return createSpreadSheetFile(spreadSheetEvent.getFormMaster(), spreadSheetEvent.getSpreadSheet().getSourceDirectory(), suffix);
	}

	public static File createSpreadSheetImgFile(SpreadSheetEvent spreadSheetEvent, String suffix) throws IOException {
		return createSpreadSheetImgFile(spreadSheetEvent.getFormMaster(), spreadSheetEvent.getSpreadSheet().getSourceDirectory(), suffix);
	}

	public static File createSpreadSheetFile(FormMaster formMaster, SourceDirectory sourceDirectory, String suffix) throws IOException {
		String masterName = new File(formMaster.getFileResourceID().getRelativePath()).getName();
		File masterFile = new File(new File(sourceDirectory.getDirectory(), AppConstants.RESULT_DIRNAME), masterName);
		return new File(FileUtil.getSuffixReplacedFilePath(masterFile, suffix));
	}
	
	public static File createSpreadSheetImgFile(FormMaster formMaster, SourceDirectory sourceDirectory, String suffix) throws IOException {
		String masterName = new File(formMaster.getFileResourceID().getRelativePath()).getName();
		File masterFile = new File(new File(sourceDirectory.getDirectory(), AppConstants.RESULT_DIRNAME), masterName+"_img");
		return new File(FileUtil.getSuffixReplacedFilePath(masterFile, suffix));
	}

	public static File createSpreadSheetFile(SessionSource sessionSource, String suffix) throws IOException {
		File resultDir = new File(sessionSource.getRootDirectory(), AppConstants.RESULT_DIRNAME);
		File rootSpreadSheetFile =  new File(resultDir, sessionSource.getRootDirectory().getName()+"."+suffix);
		return rootSpreadSheetFile;
	}

}
