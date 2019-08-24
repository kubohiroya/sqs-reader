/**
 * 
 */
package net.sqs2.omr.app.deskew;

import java.awt.Polygon;
import java.awt.geom.Point2D;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.OMRProcessorSource;
import net.sqs2.omr.model.SourceConfig;
import net.sqs2.omr.model.ValidationConfig;

public class PageSequenceValidator extends Validator{
	
	public PageSequenceValidator(OMRProcessorSource source,
			DeskewedImageSource deskewedImageSource,
			Point2D[] centerPoints,
			int[] areaSizes){
		super(source, deskewedImageSource, centerPoints, areaSizes);
		
	}
	
	public void validate()throws OMRProcessorException{
		SourceConfig sourceConfig = this.source.getConfiguration().getConfig().getPrimarySourceConfig();
		FormMaster formMaster = this.source.getFormMaster(); 
		ValidationConfig validationConfig = sourceConfig.getFrameConfig().getValidationConfig();
		if (validationConfig != null && validationConfig.isCheckEvenOdd() &&
			formMaster.getFooterLeftRectangle() != null && formMaster.getFooterRightRectangle() != null){
			validatePageSequence();
		}
	}
	
	private void validatePageSequence() throws OMRProcessorException{
		FormMaster formMaster = source.getFormMaster();
		int pageIndex = source.getProcessingPageIndex();
		Polygon footerAreaLeft = deskewedImageSource.createRectPolygon(formMaster.getFooterLeftRectangle());
		Polygon footerAreaRight = deskewedImageSource.createRectPolygon(formMaster.getFooterRightRectangle());
		int left = deskewedImageSource.calcMarkAreaDensity(formMaster.getFooterLeftRectangle());
		int right = deskewedImageSource.calcMarkAreaDensity(formMaster.getFooterRightRectangle());

		if (pageIndex == -1 || formMaster.getNumPages() == 1) {
			return;
		}
		
		if (pageIndex % 2 == 1) {
			if (left < right) {
				return;
			}
		} else {
			if (left > right) {
				return;
			}
		}

		throw new OMRProcessorException(new PageSequenceInvalidErrorModel(
				 footerAreaLeft, left, footerAreaRight, right));
				
	}

}