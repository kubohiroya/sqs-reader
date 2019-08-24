package net.sqs2.omr.session.exec;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import net.sqs2.image.ImageFactory;
import net.sqs2.omr.app.deskew.DeskewGuideAreaPair;
import net.sqs2.omr.app.deskew.DeskewGuideAreaPairFactory;
import net.sqs2.omr.app.deskew.DeskewGuideValidator;
import net.sqs2.omr.app.deskew.DeskewedImageSource;
import net.sqs2.omr.app.deskew.PageSequenceValidator;
import net.sqs2.omr.app.deskew.PageUpsideDownValidator;
import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.BarcodeAreaResult;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.FormAreaResult;
import net.sqs2.omr.model.FrameConfig;
import net.sqs2.omr.model.MarkAreaResult;
import net.sqs2.omr.model.MarkRecognitionConfig;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.OMRProcessorResult;
import net.sqs2.omr.model.OMRProcessorSource;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SourceConfig;
import net.sqs2.omr.model.TextAreaResult;
import net.sqs2.omr.model.ValidationConfig;
import net.sqs2.omr.session.constants.MarkAreaPreviewImageConstants;
import net.sqs2.omr.session.constants.TextAreaPreviewImageConstants;

public class OMRProcessor implements Callable<Void>{
	OMRPageTask task;

	OMRProcessorSource omrProcessorSource;

	DeskewGuideAreaPair deskewGuideAreaPair;
	OMRProcessorResult omrProcessorResult;

	public OMRProcessor(OMRPageTask task, OMRProcessorSource omrProcessorSource){
		this.task  = task;
		this.omrProcessorSource = omrProcessorSource;
	}

	public DeskewGuideAreaPair getDeskewGuideAreaPair (){
		return this.deskewGuideAreaPair;
	}
	
	public OMRProcessorResult getOMRProcessorResult(){
		return this.omrProcessorResult;
	}
	
	public Void call(){
		try{
			
			process();
			
			this.task.setResult(omrProcessorResult);
			
		} catch (OMRProcessorException ex) {
			this.task.setErrorModel(ex.getErrorModel());
		} catch (ConfigSchemeException ex){
			this.task.setErrorModel(createOMRProcessorErrorModel(this.task.getPageID(), ex.getLocalizedMessage()));
		} catch (IOException ex){
			this.task.setErrorModel(createOMRProcessorErrorModel(this.task.getPageID(), ex.getLocalizedMessage()));
		}
		return null;
	}
	
	void process() throws ConfigSchemeException, IOException, OMRProcessorException{

		FrameConfig frameConfig = this.omrProcessorSource.getSourceConfig().getFrameConfig();
		FormMaster formMaster = this.omrProcessorSource.getFormMaster();
		PageID pageID = this.omrProcessorSource.getPageID();
		BufferedImage pageImage = this.omrProcessorSource.getPageImage();

		this.deskewGuideAreaPair = new DeskewGuideAreaPairFactory(frameConfig.getDeskewGuideAreaConfig(),
				formMaster,
				pageImage, pageID).create();
				
		ValidationConfig validationConfig = frameConfig.getValidationConfig();
		new DeskewGuideValidator(validationConfig, deskewGuideAreaPair).validate();
		
		Point2D[] extractedDeskewGuideCenterPoints = deskewGuideAreaPair.getDeskewGuideCenterPoints();
		int[] extractedDeskewGuideAreaSizes = deskewGuideAreaPair.getDeskewGuideAreaSizes();

		DeskewedImageSource deskewedImageSource = new DeskewedImageSource(omrProcessorSource.getPageImage(),
				omrProcessorSource.getFormMaster().getDeskewGuideCenterPoints(), extractedDeskewGuideCenterPoints,
				0x00ffffff);
		
		PageUpsideDownValidator pageUpsideDownValidator = new PageUpsideDownValidator(omrProcessorSource, deskewedImageSource, extractedDeskewGuideCenterPoints, extractedDeskewGuideAreaSizes);
		pageUpsideDownValidator.validate();
		
		PageSequenceValidator pageSequenceValidator = new PageSequenceValidator(omrProcessorSource, deskewedImageSource, extractedDeskewGuideCenterPoints, extractedDeskewGuideAreaSizes);
		pageSequenceValidator.validate();

		this.omrProcessorResult = createOMRProcessorResult(omrProcessorSource, deskewedImageSource);
	}
	
	OMRProcessorErrorModel createOMRProcessorErrorModel(PageID pageID, String errorMessage) {
		return new OMRProcessorErrorModel(pageID, errorMessage);
	}

	OMRProcessorResult createOMRProcessorResult(OMRProcessorSource source,
			DeskewedImageSource deskewedImageSource
			)throws IOException, ConfigSchemeException {
		String formAreaImageFormat = AppConstants.FORMAREA_IMAGE_FORMAT;
		FormMaster formMaster = source.getFormMaster();
		int processingPageIndex = source.getProcessingPageIndex();
		List<FormArea> formAreaList = formMaster.getFormAreaListByPageIndex(processingPageIndex);
		SourceConfig sourceConfig = source.getConfiguration().getConfig().getPrimarySourceConfig();
		
		OMRProcessorResult pageTaskResult = new OMRProcessorResult(deskewedImageSource.getDeskewImagePoints(),
				deskewedImageSource.createRectPolygon(formMaster.getFooterLeftRectangle()),
				deskewedImageSource.createRectPolygon(formMaster.getFooterRightRectangle()),
				deskewedImageSource.calcMarkAreaDensity(formMaster.getFooterLeftRectangle()),
				deskewedImageSource.calcMarkAreaDensity(formMaster.getFooterRightRectangle()),
				deskewedImageSource.getProjectionTranslator()
				);
	
		for (FormArea formArea : formAreaList) {
			if (formArea.isMarkArea()) {
				updateMarkAreaResult(pageTaskResult, deskewedImageSource, formAreaImageFormat,
						sourceConfig.getMarkRecognitionConfig(), formArea);
			} else if (formArea.isTextArea()) {
				updateTextAreaResult(pageTaskResult, deskewedImageSource, formAreaImageFormat, 
						formArea);
			} else if (formArea.isBarcode()) {
				updateBarcodeAreaResult(pageTaskResult, deskewedImageSource, formAreaImageFormat, formArea);
			} else {
				throw new RuntimeException("Unknown type of formArea:" + formArea);
			}
		}
		return pageTaskResult;
	}
	
	void updateTextAreaResult(OMRProcessorResult result,
			DeskewedImageSource deskewedImageSource, String formAreaImageFormat,
			FormArea formArea) throws IOException {
		Rectangle textAreaRectangle = createFormAraeRectangleWithMargin(formArea.getRect(), 0f, 0f,
				TextAreaPreviewImageConstants.HORIZONTAL_MARGIN,
				TextAreaPreviewImageConstants.HORIZONTAL_MARGIN, 1.0f);
		BufferedImage image = deskewedImageSource.cropImage(textAreaRectangle);
		result.addPageAreaResult(createTextAreaResult(formArea.getID(), formAreaImageFormat,
				ImageFactory.writeImage(formAreaImageFormat, image), ""));
	}

	void updateMarkAreaResult(OMRProcessorResult result,
			DeskewedImageSource deskewedImageSource, String formAreaImageFormat,
			MarkRecognitionConfig config, 
			FormArea formArea) throws IOException, ConfigSchemeException {
		BufferedImage previewImage = deskewedImageSource.cropImage(createFormAreaPreviewRectangleWithMargin(formArea.getRect(), config.getResolutionScale()));
		int value = -1;
		
		Rectangle markAreaRectangle = createFormAraeRectangleWithMargin(formArea.getRect(),
				config.getHorizontalTrim(), config.getVerticalTrim(), 
				config.getHorizontalMargin(), config.getVerticalMargin(), config.getResolutionScale());
		if (MarkRecognitionConfig.VERTICAL_SLICES_AVERAGE_DENSITY.equals(config.getAlgorithm())){
			value = deskewedImageSource.calcMarkAreaDensityWithVerticalSlices(markAreaRectangle, config.getMarkRecognitionDensityThreshold(), config.getResolutionScale());
		} else if (MarkRecognitionConfig.CONVOLUTION5x3_AVERAGE_DENSITY.equals(config.getAlgorithm())) {
			value = deskewedImageSource.calcConvolution5x3AverageMarkAreaDensity(markAreaRectangle, config.getNoMarkErrorSuppressionThreshold(), config.getResolutionScale());
		} else if (MarkRecognitionConfig.CONVOLUTION5x3_AVERAGE_DENSITY_WITH_DEBUGOUT.equals(config.getAlgorithm())) {
			value = deskewedImageSource.calcConvolution5x3AverageMarkAreaDensityWithDebugOut(markAreaRectangle, config.getNoMarkErrorSuppressionThreshold(), config.getResolutionScale());
		} else {
			throw new ConfigSchemeException("Not supported algorithm:"+config.getAlgorithm());
		}
		result.addPageAreaResult(createMarkAreaResult(formArea.getID(), formAreaImageFormat,
				ImageFactory.writeImage(formAreaImageFormat, previewImage), value / 255.0f));
	}

	FormAreaResult createMarkAreaResult(String id, String imageType, byte[] imageByteArray, float density) {
		return new MarkAreaResult(id, imageType, imageByteArray, density);
	}

	 TextAreaResult createTextAreaResult(String id, String imageType, byte[] imageByteArray, String stringValue) {
		return new TextAreaResult(id, imageType, imageByteArray, stringValue);
	}

	FormAreaResult createBarcodeAreaResult(String id, String imageType, byte[] imageByteArray, String stringValue) {
		return new BarcodeAreaResult(id, imageType, imageByteArray, stringValue);
	}

	Rectangle createFormAraeRectangleWithMargin(Rectangle rect, float horizontalTrim, float verticalTrim, float horizontalMargin, float verticalMargin, float marginScale) {
		return new Rectangle((int)(rect.x + (horizontalTrim - horizontalMargin) * marginScale), (int)(rect.y + (verticalTrim - verticalMargin) * marginScale), 
				(int)(rect.getWidth() + (horizontalTrim + horizontalMargin * 2) * marginScale), (int)(rect.getHeight() + (verticalTrim + verticalMargin * 2) * marginScale));
	}

	Rectangle createFormAreaPreviewRectangleWithMargin(Rectangle rect, float marginScale) {
		return createFormAraeRectangleWithMargin(rect, 0f, 0f,
				MarkAreaPreviewImageConstants.HORIZONTAL_MARGIN,
				MarkAreaPreviewImageConstants.VERTICAL_MARGIN, marginScale);
	}

	void updateBarcodeAreaResult(OMRProcessorResult result,
			DeskewedImageSource deskewedImageSource, String formAreaImageFormat,
			FormArea formArea) throws IOException {
		Rectangle barcodeAreaRectangle = createFormAraeRectangleWithMargin(formArea.getRect(), 0f, 0f,
				TextAreaPreviewImageConstants.HORIZONTAL_MARGIN,
				TextAreaPreviewImageConstants.HORIZONTAL_MARGIN, 1.0f);
		BufferedImage image = deskewedImageSource.cropImage(barcodeAreaRectangle);
		
		String barcodeDataStringValue = "";//TODO: set decoded value
		
		result.addPageAreaResult(createBarcodeAreaResult(formArea.getID(), formAreaImageFormat,
				ImageFactory.writeImage(formAreaImageFormat, image), barcodeDataStringValue));
	}

}