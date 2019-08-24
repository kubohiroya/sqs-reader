package net.sqs2.omr.result.tree;

import java.util.ArrayList;
import java.util.List;

import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.result.tree.PathInfoParser.PageIDListPathItem;
import net.sqs2.omr.result.tree.PathInfoParser.PageIDPathItem;
import net.sqs2.omr.result.tree.PathInfoParser.QuestionItemPathItem;
import net.sqs2.omr.result.tree.PathInfoParser.QuestionPathItem;
import net.sqs2.omr.result.tree.PathInfoParser.SourceDirectoryListPathItem;
import net.sqs2.omr.util.URLSafeRLEBase64;

public class SourceDirectoryListPathInfoParser extends SessionPathInfoParser {

	public SourceDirectoryListPathInfoParser(String pathInfo){
		super(pathInfo);
		
	}
	
	public SourceDirectoryListPathItem parse(){
		throw new RuntimeException("not implemented yet."); 
		/*
		int formMasterIndex = URLSafeRLEBase64.decodeToInt(pathInfoArray[2]);
		FormMaster formMaster = sessionSource.getFormMaster(formMasterIndex);
		if(paramDepth < 4){
			throw new IllegalArgumentException(pathInfo);	
		}
		int sourceDirectoryIndex = URLSafeRLEBase64.decodeToInt(pathInfoArray[3]);
		if(paramDepth == 4){
			return new SourceDirectoryListPathItem(pathInfo, sessionID, formMaster);
		}else{
			SourceDirectory masterLocalSourceDirectoryRoot = sessionSource.getSourceDirectoryRoot(formMaster).getSourceDirectoryWithAbsoluteIndex(sourceDirectoryIndex);
			return parse(4, pathInfo, pathInfoArray, sessionID, masterLocalSourceDirectoryRoot);
		}*/
	}

	@SuppressWarnings("unused")
	private SourceDirectoryListPathItem parse(int pathInfoArrayIndexOffset, String pathInfo, String[] pathInfoArray, long sessionID, SourceDirectory sourceDirectory){
		if(pathInfoArrayIndexOffset < pathInfoArray.length){
			int index = URLSafeRLEBase64.decodeToInt(pathInfoArray[pathInfoArrayIndexOffset]);
			int numChildSourceDirectories = sourceDirectory.getNumChildSourceDirectories();
			if(index < numChildSourceDirectories){
				SourceDirectory childSourceDirectory = sourceDirectory.getChildSourceDirectory(index);
				if(childSourceDirectory.getCurrentFormMaster().equals(sourceDirectory.getCurrentFormMaster())){
					if(pathInfoArrayIndexOffset+1 < pathInfoArray.length){
						return parse(pathInfoArrayIndexOffset+1, pathInfo, pathInfoArray, sessionID, childSourceDirectory);
					}else{
						return new SourceDirectoryListPathItem(pathInfo, sessionID, sourceDirectory);			
					}
				}
			}else if(index < numChildSourceDirectories + sourceDirectory.getNumPageIDs()){
				int rowIndex = index - numChildSourceDirectories;
				int numPages = sourceDirectory.getCurrentFormMaster().getNumPages();
				int start = rowIndex * numPages;
				int end = start + numPages;
				List<PageID> pageIDList = new ArrayList<PageID>(end - start + 1);
				for(int i = start; i <= end; i++){
					PageID pageID = sourceDirectory.getPageID(i);
					pageIDList.add(pageID);
				}
				if(pathInfoArrayIndexOffset+1 < pathInfoArray.length){
					if(true/* type="image" */){
						int imageIndex = URLSafeRLEBase64.decodeToInt(pathInfoArray[pathInfoArrayIndexOffset+1]);
						PageID pageID = pageIDList.get(imageIndex);
						return new PageIDPathItem(pathInfo, sessionID, sourceDirectory, rowIndex, imageIndex, pageID);
					}else{/* type="question" */
						int questionIndex = URLSafeRLEBase64.decodeToInt(pathInfoArray[pathInfoArrayIndexOffset+1]);
						if(pathInfoArrayIndexOffset+2 < pathInfoArray.length){
							int questionItemIndex = URLSafeRLEBase64.decodeToInt(pathInfoArray[pathInfoArrayIndexOffset+2]);
							return new QuestionItemPathItem(pathInfo, sessionID, sourceDirectory, rowIndex, questionIndex,
									questionItemIndex);
						}else{
							return new QuestionPathItem(pathInfo, sessionID, sourceDirectory, rowIndex, questionIndex);
						}
					}
				}else{
					return new PageIDListPathItem(pathInfo, sessionID, sourceDirectory, rowIndex, pageIDList);
				}
			}
		}
		return null;
		//throw new IllegalArgumentException(StringUtil.join(pathInfoArray, "/"));
	}
}
