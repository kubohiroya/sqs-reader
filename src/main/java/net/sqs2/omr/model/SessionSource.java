package net.sqs2.omr.model;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.store.ObjectStore.ObjectStoreException;
import net.sqs2.util.FileResourceID;
import net.sqs2.util.FileUtil;


public class SessionSource extends AbstractSessionSourceImpl {

	private ContentAccessor contentAccessor;
	private ContentIndexer contentIndexer;
		
	SessionSource(long sessionID, File rootDirectory) throws IOException{
		super(sessionID, rootDirectory);
		this.contentIndexer = new ContentIndexer();
		this.contentAccessor = new ContentAccessorImpl(this.rootDirectory);
	}
	
	public void clearCache() throws ObjectStoreException{
		FormMasterAccessor formMasterAccessor = contentAccessor.getFormMasterAccessor();
		formMasterAccessor.delete();
		PageTaskAccessor taskAccessor = contentAccessor.getPageTaskAccessor();
		taskAccessor.delete();
	}
	
	public void removeResultDirectories() throws IOException{
		removeResultDirectories(rootDirectory);
	}
	
	private static void removeResultDirectories(File targetDirectory) throws IOException{
		for (File targetSubFile : targetDirectory.listFiles()) {
			if (targetSubFile.isDirectory()) {
				removeResultDirectories(targetSubFile);
			}
		}
		
		File resultDirectory = new File(targetDirectory, AppConstants.RESULT_DIRNAME);
		if (! resultDirectory.exists()) {
			return;
		}
		for(int i = 1; i <= 10 && resultDirectory.exists(); i++) {
			FileUtil.deleteDirectory(resultDirectory);
			try{
				Thread.sleep(100*i);
			}catch(InterruptedException ignore){}
		}
		if (resultDirectory.exists()) {
			throw new RuntimeException("Error on remove: "+resultDirectory.getAbsolutePath());
		}
	}
	
	public void close() throws IOException{
		this.contentAccessor.close();
		this.contentIndexer.clear();
	}

	public ContentAccessor getContentAccessor() {
		return this.contentAccessor;
	}

	public void addSourceDirectoryRoot(FormMaster formMaster, SourceDirectory sourceDirectoryRoot){
		this.contentIndexer.addSourceDirectoryRoot(formMaster, sourceDirectoryRoot);
	}

	public TreeSet<SourceDirectory> getSourceDirectoryRootTreeSet(FormMaster formMaster){
		return this.contentIndexer.getSourceDirectoryRootTreeSet(formMaster);
	}
	
	public SourceDirectory getSourceDirectory(final PageID pageID){
		String pageIDPath = pageID.getFileResourceID().getRelativePath();
		int lastSeparatorIndex = pageIDPath.lastIndexOf(File.separatorChar);
		if(lastSeparatorIndex == -1){
			return contentAccessor.getSourceDirectory("");
		}else{
			String sourceDirectoryPath = pageIDPath.substring(0, lastSeparatorIndex);
			return contentAccessor.getSourceDirectory(sourceDirectoryPath);
		}
	}

	public Set<FormMaster> getFormMasters(){
		return contentIndexer.getFormMasters();
	}

	public FormMaster getFormMaster(int index){
		return contentIndexer.getFormMaster(index);
	}
	
	public int getNumFormMasters(){
		return contentIndexer.getNumFormMasters();
	}
	
	public int getRowIndex(String sourceDirectoryRelativePath, PageID pageID){
		return this.contentIndexer.getRowIndex(sourceDirectoryRelativePath, pageID);
	}
	
	public void putRowIndex(String sourceDirectoryRelativePath, PageID pageID, int rowIndex){
		this.contentIndexer.putRowIndex(sourceDirectoryRelativePath, pageID, rowIndex);
	}
	
	public SourceDirectoryConfiguration getConfiguration(FileResourceID fileResourceID) throws IOException,ConfigSchemeException {
		return this.contentIndexer.getConfiguration(fileResourceID);
	}

	public void putConfiguration(SourceDirectoryConfiguration sourceDirectoryConfiguration) {
		this.contentIndexer.putConfiguration(sourceDirectoryConfiguration);
	}
	
}
