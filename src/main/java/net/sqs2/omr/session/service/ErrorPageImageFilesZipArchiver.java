package net.sqs2.omr.session.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.util.ZipArchiveUtil;
import net.sqs2.store.ObjectStore.ObjectStoreException;

public class ErrorPageImageFilesZipArchiver {
	
	SessionSource sessionSource;
	
	public ErrorPageImageFilesZipArchiver(SessionSource sessionSource){
		this.sessionSource = sessionSource;
	}

	private void setupErrorFileList(List<File> errorFileList, SourceDirectory sourceDirectory){
		for(SourceDirectory childSourceDirectory: sourceDirectory.getChildSourceDirectoryList()){
			if(childSourceDirectory.getCurrentFormMaster() == sourceDirectory.getCurrentFormMaster()){
				setupErrorFileList(errorFileList, childSourceDirectory);
			}
		}
		int numPages = sourceDirectory.getCurrentFormMaster().getNumPages();
		int index = 0;
		for(PageID pageID:sourceDirectory.getPageIDList()){
			if(hasError(pageID, index % numPages)){
				errorFileList.add(new File(pageID.getFileResourceID().getRelativePath()));
			}
			index ++;
		}
	}
	
	private boolean hasError(PageID pageID, int pageIndex){
		try{
			PageTask pageTask = sessionSource.getContentAccessor().getPageTaskAccessor().get(pageID, pageIndex);
			OMRProcessorErrorModel errorModel = pageTask.getErrorModel();
			if(errorModel != null){
				return true;
			}
		}catch(ObjectStoreException ignore){
		}
		return false;
	}
	
	public File exportZipFileTo(File zipFile)throws IOException{
		List<File> errorFileList = new ArrayList<File>(); 
		for(FormMaster formMaster: sessionSource.getFormMasters()){
			for(SourceDirectory sourceDirectoryRoot: sessionSource.getSourceDirectoryRootTreeSet(formMaster)){
				setupErrorFileList(errorFileList, sourceDirectoryRoot);
			}
		}
		
		File baseDir = this.sessionSource.getRootDirectory();
		return ZipArchiveUtil.encode(errorFileList, zipFile, baseDir, false);
	}
}
