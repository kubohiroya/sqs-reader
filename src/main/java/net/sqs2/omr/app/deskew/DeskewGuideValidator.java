package net.sqs2.omr.app.deskew;

import java.awt.geom.Point2D;

import net.sqs2.omr.model.DeskewGuideExtractionErrorModel;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.ValidationConfig;

public class DeskewGuideValidator{
	
	ValidationConfig validationConfig;
	DeskewGuideAreaPair deskewGuideAreaPair;
	
	public DeskewGuideValidator(ValidationConfig validationConfig, DeskewGuideAreaPair deskewGuideAreaPair){
		this.validationConfig = validationConfig;
		this.deskewGuideAreaPair = deskewGuideAreaPair;
	}
	
	public void validate()throws OMRProcessorException{
		
		OMRProcessorException headerException = deskewGuideAreaPair.getHeader().getOMRProcessorException();
		OMRProcessorException footerException = deskewGuideAreaPair.getFooter().getOMRProcessorException();
		
		if(headerException == null && footerException == null){
			try{
				Point2D[] extractedDeskewGuideCenterPoints = deskewGuideAreaPair.getDeskewGuideCenterPoints();
				validatePageFrameDistortion(extractedDeskewGuideCenterPoints);
			}catch(OMRProcessorException ex){
				throw ex;
			}
		}else{
			throw new OMRProcessorException(
					new DeskewGuideExtractionErrorModel(
							(headerException!=null)? headerException.getErrorModel() : null,
							(footerException!=null)? footerException.getErrorModel() : null
							)
					);
		}
	}
	
	private void validatePageFrameDistortion(Point2D[] deskewGuideCenterPoints) throws OMRProcessorException {
		
		double h1 = deskewGuideCenterPoints[0].distance(deskewGuideCenterPoints[1]);
		double h2 = deskewGuideCenterPoints[2].distance(deskewGuideCenterPoints[3]);
		double h12max = Math.max(h1, h2);
	
		double v1 = deskewGuideCenterPoints[0].distance(deskewGuideCenterPoints[2]);
		double v2 = deskewGuideCenterPoints[1].distance(deskewGuideCenterPoints[3]);
		double v12max = Math.max(v1, v2);
		
		if(h12max == 0 || v12max == 0){
			return;
		}
		
		double dh = Math.min(h1, h2) / h12max;
		double dv = Math.min(v1, v2) / v12max;
		
		if (dh < 1 - validationConfig.getHorizontalDistortion()) {
			throw new OMRProcessorException(new PageFrameDistortionErrorModel(
					PageFrameDistortionErrorModel.HORIZONTAL_ERROR_TYPE, (float) dh));
		}
		if (dv < 1 - validationConfig.getVerticalDistortion()) {
			throw new OMRProcessorException(new PageFrameDistortionErrorModel(
					PageFrameDistortionErrorModel.VERTICAL_ERRORR_TYPE, (float) dv));
		}
	}
	
}