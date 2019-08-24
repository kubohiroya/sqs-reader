package net.sqs2.omr.app.deskew;

import java.io.Serializable;

public class PageSourceErrorModel extends PageImageErrorModel  implements Serializable{
	private static final long serialVersionUID = 0L;
	float x, y;

	PageSourceErrorModel(){}
	
	public PageSourceErrorModel(float x, float y) {
		this.x = x;
		this.y = y;
	}
	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}
}