package net.sqs2.omr.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.util.FileResourceID;

import org.apache.commons.collections15.map.ListOrderedMap;

public class ContentIndexer {
	
	private ListOrderedMap<FormMaster,TreeSet<SourceDirectory>> sourceDirectoryRootMap = new ListOrderedMap<FormMaster,TreeSet<SourceDirectory>>();

	private Map<FileResourceID, SourceDirectoryConfiguration> sourceDirectoryConfigurationRegistry = new HashMap<FileResourceID, SourceDirectoryConfiguration>();

	private Map<String, Map<PageID,Integer>> rowIndexMap = new HashMap<String, Map<PageID,Integer>>(); 
	
	public ContentIndexer(){}
	
	public void clear(){
		sourceDirectoryRootMap.clear();
		sourceDirectoryConfigurationRegistry.clear();
		rowIndexMap.clear();
	}
	
	public void addSourceDirectoryRoot(FormMaster formMaster, SourceDirectory sourceDirectoryRoot){		
		TreeSet<SourceDirectory> sourceDirectoryList = sourceDirectoryRootMap.get(formMaster);
		if(sourceDirectoryList == null){
			sourceDirectoryList = new TreeSet<SourceDirectory>();
			sourceDirectoryRootMap.put(formMaster, sourceDirectoryList);
		}
		sourceDirectoryList.add(sourceDirectoryRoot);
	}

	public TreeSet<SourceDirectory> getSourceDirectoryRootTreeSet(final FormMaster formMaster){
		return sourceDirectoryRootMap.get(formMaster);
	}
	
	public Set<FormMaster> getFormMasters(){
		return sourceDirectoryRootMap.keySet();
	}
	
	public FormMaster getFormMaster(int index){
		return sourceDirectoryRootMap.get(index);
	}
	
	public int getNumFormMasters(){
		return sourceDirectoryRootMap.size();
	}

	public SourceDirectoryConfiguration getConfiguration(FileResourceID fileResourceID) {
		SourceDirectoryConfiguration sourceDirectoryConfiguration = this.sourceDirectoryConfigurationRegistry.get(fileResourceID);
		if(sourceDirectoryConfiguration == null){
			throw new IllegalArgumentException("config.xml not found:"+fileResourceID.toString());
		}else{
			return sourceDirectoryConfiguration;
		}
	}
	
	public int getRowIndex(String sourceDirectoryRelativePath, PageID pageID){
		Map<PageID,Integer> rowIndexTableMap = this.rowIndexMap.get(sourceDirectoryRelativePath);
		if(rowIndexTableMap == null){
			throw new IllegalArgumentException("undefined sourceDirectory:"+sourceDirectoryRelativePath+":"+pageID);
		}
		Integer value = rowIndexTableMap.get(pageID);
		if(value == null || value == -1){
			throw new IllegalArgumentException("undefined pageID:"+sourceDirectoryRelativePath+" : "+pageID);
		}else{
			return value.intValue();
		}
	}
	
	public void putRowIndex(String sourceDirectoryRelativePath, PageID pageID, int rowIndex){
		Map<PageID,Integer> rowIndexTableMap = this.rowIndexMap.get(sourceDirectoryRelativePath);
		if(rowIndexTableMap == null){
			rowIndexTableMap = new HashMap<PageID, Integer>();
			this.rowIndexMap.put(sourceDirectoryRelativePath, rowIndexTableMap);
		}
		rowIndexTableMap.put(pageID, rowIndex);
	}

	public void putConfiguration(SourceDirectoryConfiguration sourceDirectoryConfiguration) {
		this.sourceDirectoryConfigurationRegistry.put(sourceDirectoryConfiguration.getConfigFileResourceID(), sourceDirectoryConfiguration);
	}
}
