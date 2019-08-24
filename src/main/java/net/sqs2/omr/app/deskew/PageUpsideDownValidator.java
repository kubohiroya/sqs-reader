/**
 * 
 */
package net.sqs2.omr.app.deskew;

import java.awt.geom.Point2D;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.master.PageMaster;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.OMRProcessorSource;
import net.sqs2.omr.model.ValidationConfig;
import net.sqs2.util.VersionTag;

public class PageUpsideDownValidator extends Validator{
	
	public PageUpsideDownValidator(OMRProcessorSource source,
			DeskewedImageSource deskewedImageSource,
			Point2D[] centerPoints,
			int[] areaSizes){
		super(source, deskewedImageSource, centerPoints, areaSizes);
	}

	public void validate() throws OMRProcessorException {
		ValidationConfig validationConfig = source.getConfiguration().getConfig().getPrimarySourceConfig().getFrameConfig().getValidationConfig();
		FormMaster formMaster = source.getFormMaster();
		if(new VersionTag(formMaster.getVersion()).isNewerThan(new VersionTag("2.1.1"))){
			validateUpsideDown();
		}else{
			if (validationConfig != null && validationConfig.isCheckUpsideDown() &&
					formMaster.getHeaderCheckArea() != null && formMaster.getFooterCheckArea() != null) {
				validateUpsideDown(formMaster, deskewedImageSource);
			}
		}
	}
	
	private void validateUpsideDown() throws OMRProcessorException {
		int headerDensity = areaSizes[0]+areaSizes[1];
		int footerDensity = areaSizes[2]+areaSizes[3];
		if (headerDensity > footerDensity) {
			return;
		}

		throw new OMRProcessorException(createPageUpsideDownErrorModel(headerDensity, footerDensity));
	}

	private void validateUpsideDown(PageMaster pageMaster, DeskewedImageSource pageSource) throws OMRProcessorException{
		int headerDensity = pageSource.calcMarkAreaDensity(pageMaster.getHeaderCheckArea());
		int footerDensity = pageSource.calcMarkAreaDensity(pageMaster.getFooterCheckArea());
		if (headerDensity < footerDensity) {
			return;
		}

		throw new OMRProcessorException(createPageUpsideDownErrorModel(headerDensity, footerDensity));
	}

	private PageUpsideDownErrorModel createPageUpsideDownErrorModel(
			int headerDensity, int footerDensity) {
		return new PageUpsideDownErrorModel( 
				this.source.getFormMaster().getHeaderCheckArea(),
				this.source.getFormMaster().getFooterCheckArea(), 
				headerDensity, footerDensity);
	}


}