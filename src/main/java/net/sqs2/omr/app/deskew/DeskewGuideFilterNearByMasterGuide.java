package net.sqs2.omr.app.deskew;

import java.awt.geom.Point2D;
import java.util.List;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.OMRProcessorException;


public class DeskewGuideFilterNearByMasterGuide extends DeskewGuideFilter{
	
	FormMaster formMaster;
	
	public DeskewGuideFilterNearByMasterGuide(FormMaster formMaster){
		this.formMaster = formMaster;
	}
	
	@Override
	public DeskewGuidePair filter(List<DeskewGuide> deskewGuideList, int imageWidth, int imageHeight, int headerOrFooter)throws OMRProcessorException{
		
		Point2D[] masterGuideCenterPoints = formMaster.getDeskewGuideCenterPoints();
		
		DeskewGuide[] extractedDeskewGuides = new DeskewGuide[2];
		
		double[] max = new double[]{Double.MIN_VALUE,Double.MIN_VALUE};
		for(DeskewGuide d : deskewGuideList){
			for(int leftOrRight = 0; leftOrRight < 2; leftOrRight++){
				final int ADHOC_VERTICAL_TRIM_VALUE = 18; 
				Point2D masterGuideCenterPoint = masterGuideCenterPoints[headerOrFooter * 2 + leftOrRight];
				double dx = d.getCenterPoint().getX() - masterGuideCenterPoint.getX() * imageWidth / formMaster.getWidth();
				double dy = d.getCenterPoint().getY() - (masterGuideCenterPoint.getY() +ADHOC_VERTICAL_TRIM_VALUE) * imageHeight / formMaster.getHeight();
				double distance = dx*dx+dy*dy;
				double value = 1 / distance;
				DeskewGuide extractedDeskewGuide = extractedDeskewGuides[leftOrRight];
				if(extractedDeskewGuide == null || (extractedDeskewGuide != null && max[leftOrRight] <= value)){
					extractedDeskewGuides[leftOrRight] = d;
					max[leftOrRight] = value;
				}
			}
		} 
		if(extractedDeskewGuides[0] == extractedDeskewGuides[1]){
			throw new OMRProcessorException(new DeskewGuideMissingErrorModel());
		}
		return new DeskewGuidePair(extractedDeskewGuides[0], extractedDeskewGuides[1]);
	}
}
