/**
 *  SessionSourceInitializer.java

 Copyright 2007 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2007/03/10
 Author hiroya
 */
package net.sqs2.omr.session.init;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import net.sqs2.event.EventSource;
import net.sqs2.image.ImageFactory;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.master.FormMasterException;
import net.sqs2.omr.master.FormMasterFactory;
import net.sqs2.omr.master.FormMasterUtil;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.CacheConstants;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.FormMasterAccessor;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.omr.util.NameBasedComparableFile;
import net.sqs2.util.FileResourceID;
import net.sqs2.util.PathUtil;

public class SessionSourceInitializer implements Callable<Integer>{
	private SessionSource sessionSource;
	
	private FormMasterFactory formMasterFactory = null;
	private boolean enableSearchFormMasterFromAncestorDirectory = false;
	private EventSource<SessionSourceInitEvent> eventSource;

	static final boolean MULTIPAGE_TIFF_ENABLED = false;
	static final boolean PDF_IMAGE_BUNDLE_ENABLED = false;
	static final boolean NUM_OF_IMAGES_PAR_ROW_CHECK_ENABLED = false;
	
	public SessionSourceInitializer(SessionSource sessionSource,
			EventSource<SessionSourceInitEvent> eventSource, 
			FormMasterFactory formMasterFactory,
			boolean enableSearchPageMasterFromAncestorDirectory) throws IOException {
		this.sessionSource = sessionSource;
		this.formMasterFactory = formMasterFactory;
		this.eventSource = eventSource;
		this.enableSearchFormMasterFromAncestorDirectory = enableSearchPageMasterFromAncestorDirectory;
	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName()+":"+sessionSource.toString();
	}
	
	public Integer call() throws IOException, SessionException, FormMasterException, ConfigSchemeException, SessionSourceInitException{
		File rootDirectory = this.sessionSource.getRootDirectory();

		FormMaster ancestorFormMaster = (this.enableSearchFormMasterFromAncestorDirectory)? getPageMasterFromAncestorDirectory(sessionSource, rootDirectory.getParentFile()) : null;
		
		int ret = new SourceDirectoryInitializer(sessionSource, null, "", ancestorFormMaster).call();
		return ret;
	}
	
	private FormMaster getPageMasterFromAncestorDirectory(SessionSource sessionSource, File directory) throws IOException {
		if (directory == null) {
			// found no master file.
			return null;
		}
		
		List<FormMaster> masterList = new ArrayList<FormMaster>();
		for (File file : directory.listFiles()) {
			String filename = file.getName().toLowerCase();
			if (filename.endsWith(".pdf") || filename.endsWith(".sqm")) {
				FormMaster master = createFormMaster(sessionSource, file);
				if (master != null) {
					masterList.add(master);
				}
			}
		}
		switch(masterList.size()){
		case 0:
			return getPageMasterFromAncestorDirectory(sessionSource, directory.getParentFile());
		case 1:
			return masterList.get(0); // found one master file.
		default:
			return null; // found more than one masters.
		}
	}

	private FormMaster createFormMaster(SessionSource sessionSource, File file) {

		FormMaster master = null;
		try {
			String path = PathUtil.getRelativePath(file, sessionSource.getRootDirectory(), File.separatorChar);
			//System.out.println("************ PATH" + file.getAbsolutePath());
			FormMasterAccessor formMasterAccessor = sessionSource.getContentAccessor().getFormMasterAccessor();
			FormMaster cachedMaster = null;

			try {
				cachedMaster = formMasterAccessor.get(FormMaster.createKey(path, file.lastModified()));
			} catch (Error ignore) {
				ignore.printStackTrace();
			}
			
			if (cachedMaster != null && cachedMaster.getLastModified() == file.lastModified()) {
				return cachedMaster;
			}

			master = this.formMasterFactory.create(sessionSource.getRootDirectory(), path);
			if (master == null) {
				throw new FormMasterException(file);
			}
			formMasterAccessor.put(master);
			if(eventSource != null){
				eventSource.fireEvent(new FormMasterFoundEvent(this, sessionSource, master));
			}
			return master;
		} catch (FormMasterException ex) {
			if(eventSource != null){
				eventSource.fireEvent(new InvalidFormMasterFoundErrorEvent(this, sessionSource, ex.getFile(), ex.getMessage()));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private class SourceDirectoryInitializer{
		
		final SessionSource sessionSource;
		final SourceDirectory parentSourceDirectory; 
		final String relativePath;
		final FormMaster ancestorFormMaster; 
		
		List<FormMaster> newFormMasterList = new ArrayList<FormMaster>();
		List<File> imageFileList = new ArrayList<File>();
		List<File> childDirectoryList = new ArrayList<File>();
	
		SourceDirectoryInitializer(final SessionSource sessionSource,
				final SourceDirectory parentSourceDirectory, 
				final String relativePath,
				final FormMaster ancestorFormMaster){
			this.sessionSource = sessionSource;
			this.parentSourceDirectory = parentSourceDirectory;
			this.relativePath = relativePath;
			this.ancestorFormMaster = ancestorFormMaster;
		}
		
		/**
		 * @return number of Image files found in this initialization of this SessionSource
		 */
		public int call() throws IOException, ConfigSchemeException,SessionSourceInitException,FormMasterException,SessionSourceInitException, SessionException{
	
			File[] items = new File(sessionSource.getRootDirectory(), relativePath).listFiles();
			if (items == null || 0 == items.length) {
				return 0;
			}
			
			init(items);
			
			FormMaster formMaster = selectCurrentFormMaster(ancestorFormMaster, newFormMasterList);
			
			if(formMaster == null){
				return 0;
			}else{
				return work(formMaster, imageFileList, childDirectoryList);
			}
		}
		
		void init(File[] items) throws IOException,SessionException{
			
			File[] files = NameBasedComparableFile.createSortedFileArray(items);
		
			for (File file : files) {
				String filename = file.getName();
				String lowerFilename = filename.toLowerCase();
				if (lowerFilename.endsWith(".pdf")) {
					int numSQSMasterPages = FormMasterUtil.getNumPages(file);
					if(0 < numSQSMasterPages){
						FormMaster newFormMaster = createFormMaster(sessionSource, file);
						if (newFormMaster != null) {
							newFormMasterList.add(newFormMaster);
						}
					}
				} else if (filename.endsWith(".sqm")) {
					FormMaster newFormMaster = createFormMaster(sessionSource, file);
					if (newFormMaster != null) {
						newFormMasterList.add(newFormMaster);
					}
				}else if (file.isFile() && ImageFactory.isSupportedFileName(filename)) {
					imageFileList.add(file);
				} else if (file.isDirectory() && ! isIgnorableDirectory(file)) {
					childDirectoryList.add(file);
				}
			
				if (sessionSource.getSessionSourcePhase().hasStopped()) {
					throw new SessionException(sessionSource.getRootDirectory());
				}
			}
		}
		
		private boolean isIgnorableDirectory(File file) {
			return file.getName().endsWith(AppConstants.RESULT_DIRNAME)
					|| new File(file, CacheConstants.CACHE_ROOT_DIRNAME).exists()
					|| (new File(file, "TEXTAREA").exists() && new File(file, "CHART").exists()) 
					|| file.getName().startsWith(".");
		}
		
		FormMaster selectCurrentFormMaster(FormMaster ancestorFormMaster, List<FormMaster> newFormMasterList)throws SessionSourceInitException{
			FormMaster newFormMaster = null;
	
			if(newFormMasterList.size() == 1){
				newFormMaster = newFormMasterList.get(0);
			}else if(1 < newFormMasterList.size()){
				throw new SessionSourceInitException(new MultipleFormMastersFoundErrorEvent(this, sessionSource, newFormMasterList));
			}
			
			if(ancestorFormMaster == null && newFormMaster == null && 0 != imageFileList.size()){
				throw new SessionSourceInitException(new NoFormMasterFoundErrorEvent(this, sessionSource));
			}
		
			return (newFormMaster != null)? newFormMaster : ancestorFormMaster;
		}
		/**
		 * 
		 * @param formMaster
		 * @param imageFileList
		 * @param childDirectoryList
		 * @return number of Image files found in this initialization of this SessionSource
		 * @throws IOException
		 * @throws ConfigSchemeException
		 * @throws SessionSourceInitException
		 * @throws FormMasterException
		 * @throws SessionException
		 */
		int work(FormMaster formMaster, List<File> imageFileList, List<File> childDirectoryList) throws IOException,ConfigSchemeException,SessionSourceInitException,FormMasterException,SessionException{			
	
			SourceDirectory sourceDirectory = new SourceDirectory(parentSourceDirectory, sessionSource.getRootDirectory(), relativePath, formMaster);
			
			Logger.getLogger("init").info("+ ("+sessionSource.getRootDirectory()+":"+relativePath+":"+formMaster+")");
	
			try{
	
				if(eventSource != null){
					eventSource.fireEvent(new SessionSourceInitDirectoryEvent(this, sessionSource, sourceDirectory, SessionSourceInitDirectoryEvent.STARTED));
				}
			
				int numAddingImageFiles = 0;
				String relativePathWithSeparator = (0 == relativePath.length()) ? "" : relativePath + File.separatorChar;
			
				for (File childDirectory : childDirectoryList) {
					String childDirectoryRelativePath = relativePathWithSeparator + childDirectory.getName();
				
					numAddingImageFiles += new SourceDirectoryInitializer(sessionSource, sourceDirectory, childDirectoryRelativePath, formMaster).call();
				
					if (sessionSource.getSessionSourcePhase().hasStopped()) {
						throw new SessionException(sessionSource.getRootDirectory());
					}
				}
			
				if(0 == imageFileList.size() + numAddingImageFiles){
					return 0;
				}
			
				initializeSourceDirectory(sourceDirectory, formMaster);
	
			}finally{
				if(eventSource != null){
					if(! imageFileList.isEmpty()){
						eventSource.fireEvent(new ImageFilesFoundEvent(this, sessionSource, sourceDirectory, imageFileList.size()));
					}
					eventSource.fireEvent(new SessionSourceInitDirectoryEvent(this, sessionSource, sourceDirectory, SessionSourceInitDirectoryEvent.DONE));
				}
				Logger.getLogger("init").info("- ("+sessionSource.getRootDirectory()+":"+relativePath+":"+formMaster+")");
			}
			
			return imageFileList.size();
		}

		private void initializeSourceDirectory(SourceDirectory sourceDirectory, FormMaster formMaster) throws ConfigSchemeException, IOException {
			for(File imageFile: imageFileList){
				sourceDirectory.addImageFile(imageFile);
			}
		
			if(parentSourceDirectory != null){
				parentSourceDirectory.addChildSourceDirectory(sourceDirectory);
			}

			if(parentSourceDirectory == null || newFormMasterList.size() == 1){
				sessionSource.addSourceDirectoryRoot(formMaster, sourceDirectory);
			}else{
				parentSourceDirectory.addChildSourceDirectory(sourceDirectory);
			}

			sessionSource.getContentAccessor().putSourceDirectory(sourceDirectory.getRelativePath(), sourceDirectory);
			setupConfiguration(sourceDirectory);
		}
		
		private void setupConfiguration(SourceDirectory sourceDirectory) throws ConfigSchemeException, IOException{
			FileResourceID configFileResourceID = ConfigFileUtil.createConfigurationTemplateFile(sourceDirectory);
			SourceDirectoryConfiguration configuration = ConfigManager.createSourceDirectoryConfiguration(sourceDirectory, configFileResourceID);
			sourceDirectory.setConfiguration(configuration);
			sessionSource.putConfiguration(configuration);
		}
	}
}