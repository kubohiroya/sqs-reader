package net.sqs2.omr.app.deskew;

import java.util.HashMap;
import java.util.Map;

public class DeskewGuideCandidate implements Comparable<DeskewGuideCandidate>{
	
	int minX = Integer.MAX_VALUE;
	int maxX = Integer.MIN_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxY = Integer.MIN_VALUE;
	int gxTotal = 0;
	int gyTotal = 0;
	int gx;
	int gy;
	int areaSize;
	
	Map<String,String> reasonMap;
	
	public DeskewGuideCandidate(int areaSize){
		this.areaSize = areaSize;
		this.reasonMap = new HashMap<String,String>();
	}
	
	public int getAreaSize(){
		return areaSize;
	}
	
	public float getGX(){
		return (0 < this.areaSize)? this.gxTotal / this.areaSize : -1;
	}
	
	public float getGY(){
		return (0 < this.areaSize)? this.gyTotal / this.areaSize : -1;
	}
	
	public float getBoundingBoxWidth(){
		return this.maxX - this.minX + 1;
	}
	public float getBoundingBoxHeight(){
		return this.maxY - this.minY + 1;
	}
	
	public String toString(){
			return this.getClass().getSimpleName()+"("+getGX()+','+getGY()+'='+areaSize+")";
	}
	
	public void putFilterReason(String type, String subtype, String reason){
		reasonMap.put(type+":"+subtype, reason);
	}
	
	public Map<String,String> getFilterReasonMap(){
		return reasonMap;
	}

	@Override
	public int compareTo(DeskewGuideCandidate o) {
		return areaSize - o.areaSize;
	}
}