/**

 SourceDirectory.java

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

 Created on 2007/01/11

 */
package net.sqs2.omr.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sqs2.image.ImageFactory;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.util.FileResourceID;

import org.apache.commons.io.FilenameUtils;

public class SourceDirectory implements Serializable, Comparable<SourceDirectory> {

	private static final long serialVersionUID = 1L;

	private File sourceDirectoryRootFile;
	private String sourceDirectoryPath;
	private SourceDirectoryConfiguration sourceDirectoryConfiguration;
	
	private SourceDirectory parent;
	
	private List<PageID> pageIDList;
	private	List<SourceDirectory> childSourceDirectoryList;
	
	private FormMaster formMaster;
	
	private Map<PageID,Integer> indexToPageIDMap; 
	private Map<SourceDirectory,Integer> indexToSourceDirectoryMap; 
	
	private int numPageIDsRecursive = -1;
	private int numDescendentSourceDirectories = -1;
	
	public SourceDirectory(SourceDirectory parent, File sourceDirectoryRootFile, String sourceDirectoryPath, FormMaster formMaster) {
		this.parent = parent;
		this.sourceDirectoryRootFile = sourceDirectoryRootFile;
		this.sourceDirectoryPath = sourceDirectoryPath;
		this.formMaster = formMaster;
		
		this.pageIDList = new ArrayList<PageID>();
		this.indexToPageIDMap = new HashMap<PageID,Integer>();
		this.indexToSourceDirectoryMap = new HashMap<SourceDirectory,Integer>();
		this.childSourceDirectoryList = new LinkedList<SourceDirectory>();
	}
	
	public SourceDirectory getParent(){
		return this.parent;
	}

	@Override
	public String toString() {
		return "SourceDirectory(" + this.sourceDirectoryPath + ")";
	}

	public boolean isRoot() {
		return this.sourceDirectoryPath.equals("");
	}

	public boolean isLeaf() {
		return this.childSourceDirectoryList.size() == 0;
	}

	public File getSourceDirectoryRootFile() {
		return this.sourceDirectoryRootFile;
	}

	public File getDirectory() {
		return new File(this.sourceDirectoryRootFile.getAbsolutePath() + File.separator + this.sourceDirectoryPath);
	}

	public String getRelativePath() {
		return this.sourceDirectoryPath;
	}

	public PageID getPageID(int index) {
		return this.pageIDList.get(index);
	}

	public void addPageID(PageID pageID) {
		this.pageIDList.add(pageID);
	}

	public List<PageID> getPageIDList() {
		return this.pageIDList;
	}

	public int getNumPageIDs() {
		return pageIDList.size();
	}

	public SourceDirectoryConfiguration getConfiguration() {
		return this.sourceDirectoryConfiguration;
	}

	public void setConfiguration(SourceDirectoryConfiguration sourceDirectoryConfiguration) {
		this.sourceDirectoryConfiguration = sourceDirectoryConfiguration;
	}

	public FormMaster getCurrentFormMaster() {
		return this.formMaster;
	}
	
	public void setFormMaster(FormMaster formMaster) {
		this.formMaster = formMaster;
	}

	public void close() {
		this.pageIDList.clear();
		this.pageIDList = null;
		this.childSourceDirectoryList.clear();
		this.formMaster = null;
		this.indexToPageIDMap.clear();
		this.indexToPageIDMap = null;
		this.indexToSourceDirectoryMap.clear();
		this.indexToSourceDirectoryMap = null;
	}

	public SourceDirectory getChildSourceDirectory(int index) {
		return this.childSourceDirectoryList.get(index);
	}

	public int getNumChildSourceDirectories() {
		return this.childSourceDirectoryList.size();
	}

	public List<SourceDirectory> getChildSourceDirectoryList() {
		return this.childSourceDirectoryList;
	}
	
	public int getNumPageIDsRecursive() {
		if(numPageIDsRecursive != -1){
			return numPageIDsRecursive;
		}
		int num = pageIDList.size();
		for(SourceDirectory sourceDirectory: this.childSourceDirectoryList){
			num += sourceDirectory.getNumPageIDsRecursive();
		}
		return numPageIDsRecursive = num;
	}
	
	public int getNumDescendentSourceDirectories() {
		if(numDescendentSourceDirectories != -1){
			return numDescendentSourceDirectories;
		}
		int num = childSourceDirectoryList.size();
		for(SourceDirectory sourceDirectory: this.childSourceDirectoryList){
			num += sourceDirectory.getNumDescendentSourceDirectories();
		}
		return numDescendentSourceDirectories = num;
	}

	public SourceDirectory getSourceDirectoryWithAbsoluteIndex(int absoluteIndex){
		for(SourceDirectory childSourceDirectory: this.childSourceDirectoryList){
			int numSourceDirectoriesTotalInThisChild = childSourceDirectory.getNumDescendentSourceDirectories();
			if(absoluteIndex < numSourceDirectoriesTotalInThisChild){
				return childSourceDirectory.getChildSourceDirectory(absoluteIndex);
			}else{
				absoluteIndex -= numSourceDirectoriesTotalInThisChild;
			}
		}
		
		int numPageIDsInThisSourceDirectory = childSourceDirectoryList.size();
		if(absoluteIndex < numPageIDsInThisSourceDirectory){
			return childSourceDirectoryList.get(absoluteIndex);
		}else{
			throw new IllegalArgumentException();
		}
	}

	public PageID getPageIDWithAbsoluteIndex(int absoluteIndex){
		for(SourceDirectory childSourceDirectory: this.childSourceDirectoryList){
			int numPageIDsTotalInThisChild = childSourceDirectory.getNumPageIDsRecursive();
			if(absoluteIndex < numPageIDsTotalInThisChild){
				return childSourceDirectory.getPageIDWithAbsoluteIndex(absoluteIndex);
			}else{
				absoluteIndex -= numPageIDsTotalInThisChild;
			}
		}
		
		int numPageIDsInThisSourceDirectory = pageIDList.size();
		if(absoluteIndex < numPageIDsInThisSourceDirectory){
			return pageIDList.get(absoluteIndex);
		}else{
			throw new IllegalArgumentException();
		}
	}

	public Collection<SourceDirectory> getDescendentSourceDirectoryList() {
		return getDescendentSourceDirectoryList(new ArrayList<SourceDirectory>(),
				this.childSourceDirectoryList);
	}
	
	/**
	 * 
	 * @param descendent
	 * @param children
	 * @return depth first list of SourceDirectories.
	 */
	private Collection<SourceDirectory> getDescendentSourceDirectoryList(Collection<SourceDirectory> descendent, Collection<SourceDirectory> children) {
		if (children != null) {
			for (SourceDirectory sourceDirectory : children) {
				getDescendentSourceDirectoryList(descendent, sourceDirectory.getChildSourceDirectoryList());
				descendent.add(sourceDirectory);
			}
		}
		return descendent;
	}

	@Override
	public int hashCode() {
		return this.sourceDirectoryPath.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof SourceDirectory) {
			SourceDirectory sourceDirectory = (SourceDirectory) o;
			return sourceDirectory.getDirectory().equals(this.getDirectory());
		} else {
			return false;
		}
	}

	public void addChildSourceDirectory(SourceDirectory aChild) {
		if(! this.childSourceDirectoryList.contains(aChild)){
			this.indexToSourceDirectoryMap.put(aChild, this.childSourceDirectoryList.size());
			this.childSourceDirectoryList.add(aChild);
		}
	}

	public void addImageFile(File file) {
		int numPages = 0;

		try{
			if (FilenameUtils.getExtension(file.getName()).equals(".mtiff")) {
				numPages = ImageFactory.getNumPages("mtiff", file);
			}else if(file.getName().toLowerCase().endsWith(".pdf")){
				numPages = ImageFactory.getNumPages("pdf", file);
			}else{
				numPages = 1;
			}
		}catch(IOException ex){
			// ignore unreadable file
			return;
		}
		
		String relativePath = file.getAbsolutePath().substring(this.sourceDirectoryRootFile.getAbsolutePath().length() + 1);
		for (int i = 0; i < numPages; i++) {
			PageID pageID = new PageID(new FileResourceID(relativePath, file.lastModified()), i, numPages);
			pageIDList.add(pageID);
			indexToPageIDMap.put(pageID, i);
		}
	}

	@Override
	public int compareTo(SourceDirectory o) {
		String path1 = o.sourceDirectoryRootFile.getAbsolutePath()+File.separatorChar+o.sourceDirectoryPath;
		String path2 = sourceDirectoryRootFile.getAbsolutePath()+File.separatorChar+sourceDirectoryPath;
		return path2.compareTo(path1);
	}

}
