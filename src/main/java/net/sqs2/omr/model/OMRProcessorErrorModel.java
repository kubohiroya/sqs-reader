package net.sqs2.omr.model;

import java.io.Serializable;

public class OMRProcessorErrorModel implements Serializable, Comparable<OMRProcessorErrorModel>{

	private static final long serialVersionUID = 1L;
	protected PageID pageID;
	protected String errorMessage;
	
	public OMRProcessorErrorModel(){}
	
	public OMRProcessorErrorModel(PageID pageID) {
		this.pageID = pageID;
	}

	public OMRProcessorErrorModel(PageID pageID, String errorMessage) {
		this(pageID);
		this.errorMessage = errorMessage;
	}
	
	public PageID getPageID(){
		return pageID;
	}
	
	public int compareTo(OMRProcessorErrorModel m) {
		return this.pageID.compareTo(m.pageID);
	}
	
	/*
	@Override
	public int hashCode() {
		return this.pageID.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		try {
			OMRProcessorErrorModel m = (OMRProcessorErrorModel) o;
			return this.pageID.equals(m.pageID);
		} catch (Exception ex) {
			return false;
		}
	}

*/

}