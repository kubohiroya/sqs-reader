package net.sqs2.omr.result.path;

import java.util.List;
import java.util.Set;

import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.result.model.SpreadSheetItemList;

public class MultiSpreadSheetSelection extends SingleMasterSelection {
	//LinkedList<SourceDirectory> flattenSelectedSourceDirectoryList = new LinkedList<SourceDirectory>();
	
	@SuppressWarnings("unused")
	public MultiSpreadSheetSelection(MultiSpreadSheetPath multiSpreadSheetPath) {
		super(multiSpreadSheetPath);
		
		Set<SourceDirectory> sourceDirectorySet = sessionSource.getSourceDirectoryRootTreeSet(formMaster);

		for(int flattenSourceDirectoryIndex: multiSpreadSheetPath.getFlattenSourceDirectorySelection().getSelectedIndexTreeSet()){
		//	flattenSelectedSourceDirectoryList.add(sourceDirectory.getSourceDirectoryWithAbsoluteIndex(flattenSourceDirectoryIndex));
		}
	}
	
	public MultiSpreadSheetPath getMultiSpreadSheetPath(){
		return (MultiSpreadSheetPath)masterPath;
	}

	public List<SourceDirectory> getFlattenSelectedSourceDirectoryList() {
		throw new RuntimeException("not implemented yet."); 
		//return null;//flattenSelectedSourceDirectoryList;
	}
	
	public String getTitle(){
		/*
		if(title == null){
			if(flattenSelectedSourceDirectoryList.size() == 1){
				return title = flattenSelectedSourceDirectoryList.get(0).getDirectory().getName();
			}else{
				return super.getTitle();
			}
		}else{
			return title;
		}*/
		throw new RuntimeException("not implemented yet."); 
	}

	public SpreadSheetItemList createFlattenSelectedSpreadSheetItemList(){
	/*
		SpreadSheetItemList spreadSheetItemList = new SpreadSheetItemList();
		for(SourceDirectory sourceDirectory: flattenSelectedSourceDirectoryList){
			spreadSheetItemList.add(new SpreadSheetItem(sourceDirectory));	
		}
		return spreadSheetItemList;
		*/
		throw new RuntimeException("not implemented yet."); 
	}
}
