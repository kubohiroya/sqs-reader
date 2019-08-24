package net.sqs2.omr.model;

import java.io.Serializable;

public class TextAreaResult extends FormAreaResult implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	protected String  stringValue;
	
	public TextAreaResult(String id, String imageType, byte[] imageByteArray, String stringValue) {
		super(id, imageType, imageByteArray);
		this.stringValue = stringValue;
	}

	public String getStringValue() {
		return this.stringValue;
	}

}
