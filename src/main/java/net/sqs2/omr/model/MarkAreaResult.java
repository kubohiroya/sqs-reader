package net.sqs2.omr.model;

import java.io.Serializable;

public class MarkAreaResult extends FormAreaResult implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	protected float density;
	
	public MarkAreaResult(String id, String imageType, byte[] imageByteArray, float density) {
		super(id, imageType, imageByteArray);
		this.density = density;
	}

	public float getDensity() {
		return this.density;
	}

}
