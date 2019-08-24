package net.sqs2.omr.model;

import java.util.List;

import net.sqs2.store.ObjectStore.ObjectStoreException;

public class OMRProcessorErrorMessageUtil{
	public static String createErrorMessage(RowAccessor rowAccessor, PageTaskAccessor pageTaskAccessor, SourceDirectory sourceDirectory, List<PageID> pageIDList, int numPagesOfMaster, int rowIndex){
		StringBuilder ret = new StringBuilder();
		int base = rowIndex * numPagesOfMaster;
		try{
			Row row = rowAccessor.get(sourceDirectory.getCurrentFormMaster().getRelativePath(), sourceDirectory.getRelativePath(), rowIndex);
			if(row == null){
				ret.append("Row=null");
			}

			for(int i = 0; i < numPagesOfMaster; i++){
				if(0 < i){
					ret.append(' ');
				}
				PageID pageID = pageIDList.get(base + i);
				PageTask pageTask = pageTaskAccessor.get(pageID, i);
				if(pageTask == null){
					ret.append("PageTask=null");
					continue;
				}
				OMRProcessorErrorModel errorModel = pageTask.getErrorModel();
				if(errorModel == null){
					continue;
				}
				ret.append("Page"+(i+1)+"=");
				ret.append(OMRProcessorErrorMessages.get(errorModel));

		}
		}catch(ObjectStoreException ex){
			ret.append("ObjectStoreException(row)");
		}
		String message = ret.toString().trim();
		if(message.length() == 0){
			return "OK";
		}else{
			return message;
		}
	}
}