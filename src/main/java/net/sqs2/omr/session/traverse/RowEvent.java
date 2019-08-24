package net.sqs2.omr.session.traverse;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.Row;
import net.sqs2.util.StringUtil;

import org.apache.commons.collections15.multimap.MultiHashMap;

public class RowEvent extends TraverseEvent {

	RowGroupEvent rowGroupEvent;
	int rowIndex;
	Row row;
	List<PageID> pageIDList;
	MultiHashMap<PageID, OMRProcessorErrorModel> taskErrorModelMultiHashMap;

	public RowEvent(RowGroupEvent sourceDirectoryEvent, int numEvents) {
		this.rowGroupEvent = sourceDirectoryEvent;
		this.numEvents = numEvents;
	}

	public RowGroupEvent getRowGroupEvent() {
		return this.rowGroupEvent;
	}
	
	@Override public String toString(){
		return row.getID()+","+pageIDList.toString();
	}

	public void setRow(Row row) {
		this.row = row;
	}

	public Row getRow() {
		return this.row;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public int getRowIndex() {
		return this.rowIndex;
	}

	public void putTaskErrorModelMultiHashMap(PageID pageID, OMRProcessorErrorModel errorModel) {
		this.taskErrorModelMultiHashMap.put(pageID, errorModel);
	}

	public Collection<OMRProcessorErrorModel> getTaskErrorModelMultiHashMap(PageID pageID) {
		if(this.taskErrorModelMultiHashMap == null){
			return Collections.EMPTY_LIST;
		}else{
			return this.taskErrorModelMultiHashMap.get(pageID);
		}
	}

	public int getNumTaskErrorModels(PageID pageID) {
		Collection<OMRProcessorErrorModel> taskErrorModels = this.taskErrorModelMultiHashMap.get(pageID);
		return (taskErrorModels == null)? 0 : taskErrorModels.size();
	}

	public int getNumPageIDs() {
		return this.taskErrorModelMultiHashMap.size();
	}

	public MultiHashMap<PageID, OMRProcessorErrorModel> getTaskErrorModelMultiHashMap() {
		return this.taskErrorModelMultiHashMap;
	}

	public void setTaskErrorModelMultiHashMap(MultiHashMap<PageID, OMRProcessorErrorModel> map) {
		this.taskErrorModelMultiHashMap = map;
	}

	public List<PageID> getPageIDList() {
		return this.pageIDList;
	}

	public void setPageIDList(List<PageID> pageIDList) {
		this.pageIDList = pageIDList;
	}

	public String createRowMemberFilenames(char itemSeparator) {
		boolean separator = false;
		int numPages = this.rowGroupEvent.getFormMaster().getNumPages();
		StringBuilder filenames = new StringBuilder();
		for (int pageIndex = 0; pageIndex < numPages; pageIndex++) {
			PageID pageID = this.pageIDList.get(pageIndex);
			if (separator) {
				filenames.append(itemSeparator);
			} else {
				separator = true;
			}
			filenames.append(StringUtil.escapeTabSeparatedValues(new File(pageID.getFileResourceID().getRelativePath())
					.getName()));
		}
		return filenames.toString();
	}
}
