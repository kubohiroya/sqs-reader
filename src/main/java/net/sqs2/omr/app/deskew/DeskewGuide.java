package net.sqs2.omr.app.deskew;

import java.awt.geom.Point2D;

public class DeskewGuide implements Comparable<DeskewGuide>{
	
	Point2D centerPoint;
	int areaSize;
	
	DeskewGuide(Point2D centerPoint, int area){
		this.centerPoint = centerPoint;
		this.areaSize = area;
	}
	public Point2D getCenterPoint() {
		return centerPoint;
	}
	public int getAreaSize() {
		return areaSize;
	}
	
	public void addOffset(double horizontal, double vertical){
		centerPoint = new Point2D.Double(centerPoint.getX() + horizontal, centerPoint.getY()+vertical);
	}
	
	@Override
	public String toString(){
		return centerPoint.toString()+":"+areaSize;
	}
	
	@Override
	public int compareTo(DeskewGuide o) {
		return this.areaSize - o.areaSize;
	}
}