package net.sqs2.omr.model;

import java.io.Serializable;

public class OMRProcessorException extends Exception implements Comparable<OMRProcessorException>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected OMRProcessorErrorModel errorModel;

	public OMRProcessorException(OMRProcessorErrorModel errorModel) {
		this.errorModel = errorModel;
	}

	public OMRProcessorErrorModel getErrorModel() {
		return this.errorModel;
	}

	@Override
	public boolean equals(Object o) {
		try {
			return errorModel.equals(((OMRProcessorException)o).getErrorModel());
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.errorModel.hashCode();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[" + this.errorModel+"]";
	}

	public int compareTo(OMRProcessorException o) {
		return errorModel.compareTo(((OMRProcessorException)o).getErrorModel());
	}

}
