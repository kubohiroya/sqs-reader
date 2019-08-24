package net.sqs2.omr.app.deskew;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sqs2.image.ImageSilhouetteExtract;
import net.sqs2.omr.model.OMRProcessorException;

import org.apache.commons.collections15.list.TreeList;

public class DeskewGuideCandidateFinder{
	
	DeskewGuideAreaBitmap deskewGuideAreaBitmap;
	DeskewGuideCandidate[] deskewGuideCandidates;
	
	public DeskewGuideCandidateFinder(DeskewGuideAreaBitmap deskewGuideArea){
		this.deskewGuideAreaBitmap = deskewGuideArea;
	}
	
	public List<DeskewGuide> find()throws OMRProcessorException {
		 deskewGuideCandidates = createDeskewGuideCandidates();
		List<DeskewGuide> deskewGuideList = new TreeList<DeskewGuide>();

		double guideSize = 21;//A3 page size: 13, A4 page size: 18 (actual deskewGuide size of pdf scale) 
		double gw = this.deskewGuideAreaBitmap.bitmapWidth * guideSize / (595.0 - (1.0-2*deskewGuideAreaBitmap.config.getHorizontalMargin()));//595: A4 paper width
		double gh = this.deskewGuideAreaBitmap.bitmapHeight * guideSize / (842.0 * deskewGuideAreaBitmap.config.getHeight());//842: A4 paper height
		Arrays.sort(deskewGuideCandidates);
		for(DeskewGuideCandidate c : deskewGuideCandidates){
			float boundingBoxAreaSize = c.getBoundingBoxWidth()*c.getBoundingBoxHeight();
				
			if( checkValueInRange(c, "areaSize", boundingBoxAreaSize * 0.6, c.areaSize, gw * gh * 1.4 ) && 
				checkValueInRange(c, "boundingBoxWidth", gw * 0.35, c.getBoundingBoxWidth(), gw * 1.3) &&
				checkValueInRange(c, "boundingBoxHeight", gh * 0.7, c.getBoundingBoxHeight(), gh * 1.3)){
				deskewGuideList.add(new DeskewGuide(
						new Point2D.Float(deskewGuideAreaBitmap.translateX(c.getGX()), 
														deskewGuideAreaBitmap.translateY(c.getGY())), c.areaSize));
				//System.out.println("+ "+c+"\tareaSize="+c.areaSize+"\tw="+c.getBoundingBoxWidth()+"\th="+c.getBoundingBoxHeight());
			}else if(1 < c.areaSize){
				//System.out.println("- "+c+"\tareaSize="+c.areaSize+"\tw="+c.getBoundingBoxWidth()+"\th="+c.getBoundingBoxHeight());
			}else{
			}
		}
		
		Collections.sort(deskewGuideList);
		return deskewGuideList;
	}
	
	public DeskewGuideCandidate[]  getDeskewGuideCandidates(){
		if(deskewGuideCandidates == null){
			throw new RuntimeException("DeskewGuideCandidateFinter#find() must be called before calling getDeskewGuideCandidates()");
		}
		return deskewGuideCandidates;
	}

	private boolean checkValueInRange(DeskewGuideCandidate c, String type, double min, double value, double max){
		boolean lesser = (min <= value);
		boolean greater = (value <= max);
		boolean result = lesser && greater;
		if(lesser == false){
			c.putFilterReason(type, "-", "["+value+"]<"+min);
		}
		if(greater == false){
			c.putFilterReason(type, "+", max+"<["+value+"]");
		}
		return result; 
	}
	
	private DeskewGuideCandidate[] createDeskewGuideCandidates() throws OMRProcessorException {

		ImageSilhouetteExtract ise = new ImageSilhouetteExtract(this.deskewGuideAreaBitmap.getBitmap(), this.deskewGuideAreaBitmap.getBitmapWidth(), this.deskewGuideAreaBitmap.getBitmapHeight());
		int[] silhouetteIndexArray = ise.getSilhouetteIndexArray();

		int[] areaArray = ise.getAreaArray();
		DeskewGuideCandidate[] deskewGuideCandidate = new DeskewGuideCandidate[areaArray.length];

		for(int silhouetteIndex = 0; silhouetteIndex < areaArray.length; silhouetteIndex++){
			deskewGuideCandidate[silhouetteIndex] = new DeskewGuideCandidate(areaArray[silhouetteIndex]);
		}
		
		int pixelIndex = this.deskewGuideAreaBitmap.bitmapWidth * this.deskewGuideAreaBitmap.bitmapHeight - 1;
		for (int y = this.deskewGuideAreaBitmap.bitmapHeight - 1; 0 <= y; y--) {
			for (int x = this.deskewGuideAreaBitmap.bitmapWidth - 1; 0 <= x; x--) {
				int silhouetteIndex = silhouetteIndexArray[pixelIndex];
				DeskewGuideCandidate c = deskewGuideCandidate[silhouetteIndex]; 
				if (1 < c.areaSize) {
					c.minX = Math.min(c.minX, x);
					c.maxX = Math.max(c.maxX, x);
					c.minY = Math.min(c.minY, y);
					c.maxY = Math.max(c.maxY, y);
					c.gxTotal += x; 
					c.gyTotal += y; 
				}else{
					c.areaSize = 0;
				}
				pixelIndex--;
			}
		}
		
		return deskewGuideCandidate;
	}
	
}