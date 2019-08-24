/*
 Copyright 2011 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2011/12/03

 */
package net.sqs2.omr.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import net.sqs2.geom.ProjectionTranslator;
import net.sqs2.image.ImageFactory;
import net.sqs2.image.ImageUtil;
import net.sqs2.omr.app.deskew.DeskewGuide;
import net.sqs2.omr.app.deskew.DeskewGuideArea;
import net.sqs2.omr.app.deskew.DeskewGuideAreaBitmap;
import net.sqs2.omr.app.deskew.DeskewGuideCandidate;
import net.sqs2.omr.app.deskew.DeskewGuideCandidateFinder;
import net.sqs2.omr.app.deskew.PageFrameDistortionErrorModel;
import net.sqs2.omr.app.deskew.PageImageErrorModel;
import net.sqs2.omr.app.deskew.PageSequenceInvalidErrorModel;
import net.sqs2.omr.app.deskew.PageUpsideDownErrorModel;
import net.sqs2.omr.master.FormArea;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.DeskewGuideExtractionErrorModel;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorErrorMessages;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.OMRProcessorResult;
import net.sqs2.omr.model.OMRProcessorSource;
import net.sqs2.omr.model.PageAreaResult;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.omr.session.exec.OMRProcessor;
import net.sqs2.omr.session.service.LocalOMRProcessorSourceFactory;
import net.sqs2.omr.session.service.PageAreaResultListProcessor;
import net.sqs2.omr.ui.swing.ColorManager;
import net.sqs2.omr.ui.swing.DottedLineStrokeManager;
import net.sqs2.omr.ui.swing.GElement;
import net.sqs2.omr.ui.swing.GShape;
import net.sqs2.omr.ui.swing.GText;
import net.sqs2.omr.ui.swing.OMRImage;
import net.sqs2.omr.ui.util.ObservableObject;
import net.sqs2.util.FileResourceID;
import net.sqs2.util.FileUtil;

public class OMRImageModel{

	public static float DEFAULT_PAGE_IMAGE_SCALE = 0.25f;
	public static float DEFAULT_DESKEW_IMAGE_SCALE = 1.0f;
	static Color orange = new Color(255,128, 0, 128);
	static Color red = new Color(255,0, 0, 128);
	static Color yellow = new Color(255,255, 0, 128);
	static Color cyan = new Color(0,255, 255, 128);
	ObservableObject<OMRImage> headerArea;
	ObservableObject<OMRImage> footerArea;
	ObservableObject<OMRImage> previewArea;
	ObservableObject<Float> deskewScale;
	ObservableObject<Float> previewScale;

	private OMRImage headerImage = new OMRImage(null, null, -1L );
	private OMRImage footerImage = new OMRImage(null, null,  -1L);
	private OMRImage previewImage = new OMRImage(null, null,  -1L);
	
	private List<OMRProcessorErrorModel> headerErrorModels = new ArrayList<OMRProcessorErrorModel>();
	private List<OMRProcessorErrorModel> footerErrorModels = new ArrayList<OMRProcessorErrorModel>();
	private List<OMRProcessorErrorModel> previewErrorModels = new ArrayList<OMRProcessorErrorModel>();

	SwingWorker<String,String> worker = null;

	OMRImageModel(){
		this.headerArea = new ObservableObject<OMRImage>(headerImage);
		this.footerArea = new ObservableObject<OMRImage>(footerImage);
		this.previewArea = new ObservableObject<OMRImage>(previewImage);
		this.deskewScale = new ObservableObject<Float>(0f);
		this.previewScale = new ObservableObject<Float>(0f);
	}		

	public ObservableObject<Float> getDeskewScale(){
		return this.deskewScale;
	}

	public ObservableObject<Float> getPreviewScale(){
		return previewScale;
	}

	public float getDefaultDeskewScaleValue(){
		return this.headerImage.getDefaultScale();
	}

	public float getDefaultPreviewScaleValue(){
		return this.previewImage.getDefaultScale();
	}

	public float getDeskewScaleValue(){
		float ret = this.deskewScale.getObject();
		if(ret == 0){
			return this.headerImage.getDefaultScale();
		}else{
			return ret;
		}
	}

	public Float getPreviewScaleValue(){
		float ret = this.previewScale.getObject();
		if(ret == 0){
			return this.previewImage.getDefaultScale();
		}else{
			return ret;
		}
	}

	public ObservableObject<OMRImage> getHeaderArea() {
		return this.headerArea;
	}

	public ObservableObject<OMRImage> getFooterArea() {
		return this.footerArea;
	}

	public ObservableObject<OMRImage> getPreviewArea() {
		return this.previewArea;
	}

	public void setEmptyPageContent() {
		getHeaderArea().setObject(null);
		getHeaderArea().update();
		getFooterArea().setObject(null);
		getFooterArea().update();
		getPreviewArea().setObject(null);
		getPreviewArea().update();
	}

	Rectangle2D createRectangleOnCanvas(Point2D _a, Point2D _b, double scale){
		BufferedImage image = this.previewArea.getObject().getSourceImage();
		double canvasWidth = image.getWidth();
		double canvasHeight = image.getHeight();
		Point2D a = new Point2D.Double(_a.getX()/scale, _a.getY()/scale);
		Point2D b = new Point2D.Double(_b.getX()/scale, _b.getY()/scale);
		double ax = Math.max(0, Math.min(a.getX(), b.getX()));
		double ay = Math.max(0, Math.min(a.getY(), b.getY()));
		double bx = Math.min(canvasWidth - 1, Math.max(a.getX(), b.getX()));
		double by = Math.min(canvasHeight - 1,  Math.max(a.getY(), b.getY()));
		double w = Math.abs(bx - ax);
		double h = Math.abs(by - ay);
		return new Rectangle2D.Double(ax, ay, w, h);
	}

	public void updatePageContent(final long sessionID, final SourceDirectory sourceDirectory, final int pageIDTableModelRowIndex,
														Rectangle headerVisibleRect, Rectangle footerVisibleRect, Rectangle previewVisibleRect){
		doUpdatePageContent(sessionID, sourceDirectory, pageIDTableModelRowIndex, headerVisibleRect, footerVisibleRect, previewVisibleRect);
	}

	synchronized void doUpdatePageContent(final long sessionID, final SourceDirectory sourceDirectory, final int rowIndex,
			final Rectangle headerVisibleRect, final Rectangle footerVisibleRect, final Rectangle previewVisibleRect) {

		if(worker != null && ! worker.isDone()){
			worker.cancel(true);
		}

		worker = new SwingWorker<String, String>(){

			OMRProcessorResult result = null;
			OMRProcessorErrorModel errorModel = null;
			PageID pageID = null;

			OMRProcessor omrProcessor = null;

			SourceDirectoryConfiguration configuration;
			boolean[] previewMonochromeBitmap ;
			int pageIndex;
			int w, h;

			@Override
			public synchronized String doInBackground(){

				if (rowIndex == -1) {
					setEmptyPageContent();
					return "setEmptyPageContent";
				}

				pageID = sourceDirectory.getPageID(rowIndex);
				pageIndex = rowIndex % sourceDirectory.getCurrentFormMaster().getNumPages();

				configuration = sourceDirectory.getConfiguration();

				FileResourceID formMasterFileResourceID = sourceDirectory.getCurrentFormMaster().getFileResourceID();
				FileResourceID configFileResourceID = configuration.getConfigFileResourceID();

				OMRPageTask pageTask = new OMRPageTask(sessionID,
						pageID,
						configFileResourceID, 
						formMasterFileResourceID, 
						pageIndex);

				OMRProcessorSource omrProcessorSource = null;
				
				try {
					omrProcessorSource = new LocalOMRProcessorSourceFactory(pageTask).call();
				} catch (ConfigSchemeException e) {
					e.printStackTrace();
					return "ConfigSchemeException";
				} catch (IOException e) {
					String message = e.getLocalizedMessage();
					if(message != null){
						drawErrorMessage(getHeaderArea().getObject(),
								getFooterArea().getObject(),
								getPreviewArea().getObject(), message, null);
					}
					return "IOException";
				}

				w = omrProcessorSource.getPageImage().getWidth();
				h = omrProcessorSource.getPageImage().getHeight();

				previewMonochromeBitmap = MonochromeBitmapFactory.create(omrProcessorSource, w, h);	

				omrProcessor = new OMRProcessor(pageTask, omrProcessorSource);
				omrProcessor.call();

				result = pageTask.getResult();
				errorModel = pageTask.getErrorModel();
/*
				if(false){
					try{
						PageTaskAccessor pageTaskAccessor = MarkReaderSessions.get(sessionID).getSessionSource().getContentAccessor().getPageTaskAccessor();
						PageTask storedTask = pageTaskAccessor.get(pageID, pageIndex);
						result = storedTask.getResult();
					}catch(IOException ex){
						ex.printStackTrace();
						return "IOException";
					}
				}*/

				return null;
			}

			@Override
			public synchronized  void done(){

				DeskewGuideArea header = omrProcessor.getDeskewGuideAreaPair().getHeader();
				DeskewGuideArea footer = omrProcessor.getDeskewGuideAreaPair().getFooter();
				DeskewGuideAreaBitmap headerDeskewGuideArea = header.getDeskewGuideAreaBitmap();
				DeskewGuideAreaBitmap footerDeskewGuideArea = footer.getDeskewGuideAreaBitmap();

				OMRImage headerOMRImage, footerOMRImage, previewOMRImage;

				headerOMRImage = updateOMRImage(getHeaderArea(), pageID,
						headerDeskewGuideArea.getBitmap(),
						headerDeskewGuideArea.getBitmapWidth(),
						headerDeskewGuideArea.getBitmapHeight(), false);

				footerOMRImage = updateOMRImage(getFooterArea(), pageID,
						footerDeskewGuideArea.getBitmap(),
						footerDeskewGuideArea.getBitmapWidth(),
						footerDeskewGuideArea.getBitmapHeight(), true);

				previewOMRImage = updateOMRImage(getPreviewArea(), pageID, previewMonochromeBitmap, w, h, false);

				float preferredPreviewScale = (float) Math.min(1.0 * previewVisibleRect.width / w, 1.0 * previewVisibleRect.height / h);
				float preferredDeskewScale = (float) Math.min(1.0 * headerVisibleRect.width / headerDeskewGuideArea.getBitmapWidth(),
																					 1.0 * headerVisibleRect.height / headerDeskewGuideArea.getBitmapHeight());
				previewScale.setObject(preferredPreviewScale);
				deskewScale.setObject(preferredDeskewScale);
				previewOMRImage.setDefaultScale(preferredPreviewScale);
				headerOMRImage.setDefaultScale(preferredDeskewScale);
				footerOMRImage.setDefaultScale(preferredDeskewScale);
				previewScale.update();
				deskewScale.update();
				
				if(result != null){
					
					// Succeed in OMR Processing!
					
					drawOMRPage(sourceDirectory,
							headerDeskewGuideArea, footerDeskewGuideArea,
							headerOMRImage, footerOMRImage, previewOMRImage);
					
				}else if(errorModel instanceof DeskewGuideExtractionErrorModel){
					
					DeskewGuideExtractionErrorModel errorModel = (DeskewGuideExtractionErrorModel)this.errorModel;
					List<DeskewGuide> headerDeskewGuideList = header.getDeskewGuideCandidateList();
					 List<DeskewGuide> footerDeskewGuideList = footer.getDeskewGuideCandidateList();
					 if(headerDeskewGuideList == null || headerDeskewGuideList.size() < 2){
						 DeskewGuideCandidateFinder finder = header.getDeskewGuideCandidateFinder();
							float[] hueColorArray = ColorManager.fibonacciHueColor(finder.getDeskewGuideCandidates().length);
							int index = 0;
							for(DeskewGuideCandidate d : finder.getDeskewGuideCandidates()){
								drawDeskewGuideCanddiateShape(headerOMRImage, previewOMRImage, d, new Color(hueColorArray[index++], 0.5f, 1.0f, 0.5f));
							}
					}else{
						drawDeskewGuideShape(headerOMRImage, previewOMRImage, errorModel.getHeaderErrorModel(), headerDeskewGuideList);
					}
					 if(footerDeskewGuideList == null || footerDeskewGuideList.size() < 2){
						 DeskewGuideCandidateFinder finder = footer.getDeskewGuideCandidateFinder();
							float[] hueColorArray = ColorManager.fibonacciHueColor(finder.getDeskewGuideCandidates().length);
							int index = 0;
							for(DeskewGuideCandidate d : finder.getDeskewGuideCandidates()){
								drawDeskewGuideCanddiateShape(footerOMRImage, previewOMRImage, d, new Color(hueColorArray[index++], 0.5f, 1.0f, 0.5f));
						 }
					 }else{
						drawDeskewGuideShape(footerOMRImage, previewOMRImage, errorModel.getFooterErrorModel(), footerDeskewGuideList);
					}
					 
					// String errorMessage = "ERROR: Deskew Guide Missing: ";
					// addErrorMessage(headerOMRImage, footerOMRImage, previewOMRImage, errorMessage, errorModel);
					 
				}else{
						drawSemanticErrorStatus(headerDeskewGuideArea, footerDeskewGuideArea, 
								headerOMRImage, footerOMRImage, previewOMRImage);
				}
					
				getHeaderArea().update();
				getFooterArea().update();
				getPreviewArea().update();
			}

			private void drawSemanticErrorStatus(
					DeskewGuideAreaBitmap headerDeskewGuideArea,
					DeskewGuideAreaBitmap footerDeskewGuideArea,
					OMRImage headerOMRImage, OMRImage footerOMRImage,
					OMRImage previewOMRImage) {
				if(errorModel instanceof PageImageErrorModel && result != null){
					Point2D[] corners = result.getDeskewGuideCenterPoints();
					if(errorModel instanceof PageFrameDistortionErrorModel){
						PageFrameDistortionErrorModel pageFrameDistortionErrorModel = (PageFrameDistortionErrorModel) errorModel;
						String errorMessage = "ERROR: Form Image Error :"+pageFrameDistortionErrorModel .getErrorType();
						drawErrorMessage(headerOMRImage, footerOMRImage, previewOMRImage, errorMessage, errorModel);
						drawDeskewGuide(headerOMRImage, footerOMRImage, previewOMRImage, corners, headerDeskewGuideArea, footerDeskewGuideArea);

					}else if(errorModel instanceof PageSequenceInvalidErrorModel){
						PageSequenceInvalidErrorModel pageSequenceInvalidErrorModel = (PageSequenceInvalidErrorModel) errorModel;

						String errorMessage = "ERROR: Page Sequence Invalid";
						drawErrorMessage(headerOMRImage, footerOMRImage, previewOMRImage, errorMessage, errorModel);
						drawDeskewGuide(headerOMRImage, footerOMRImage, previewOMRImage, corners, headerDeskewGuideArea, footerDeskewGuideArea);

						Polygon leftArea = pageSequenceInvalidErrorModel.getLeftFooterArea();
						Polygon rightArea = pageSequenceInvalidErrorModel.getRightFooterArea();
						int leftValue = pageSequenceInvalidErrorModel.getLeftValue();
						int rightValue = pageSequenceInvalidErrorModel.getRightValue();
						drawFooterDecorations(footerOMRImage, previewOMRImage, leftArea, rightArea, leftValue, rightValue, headerDeskewGuideArea);
						
					}else if(errorModel instanceof PageUpsideDownErrorModel){
						//	PageUpsideDownErrorModel pageUpsideDownErrorModel = (PageUpsideDownErrorModel) errorModel;
						String errorMessage = "ERROR: Page Upside Down";
						drawErrorMessage(headerOMRImage, footerOMRImage, previewOMRImage, errorMessage, errorModel);
						drawDeskewGuide(headerOMRImage, footerOMRImage, previewOMRImage, corners, headerDeskewGuideArea, footerDeskewGuideArea);
					}
				}else{
					String errorMessage = "ERROR:"+OMRProcessorErrorMessages.get(errorModel);
					drawErrorMessage(headerOMRImage, footerOMRImage, previewOMRImage, errorMessage, errorModel);
				}
			}

			private void drawOMRPage(
					final SourceDirectory sourceDirectory,
					DeskewGuideAreaBitmap headerDeskewGuideArea,
					DeskewGuideAreaBitmap footerDeskewGuideArea,
					OMRImage headerOMRImage, OMRImage footerOMRImage,
					OMRImage previewOMRImage) {
				Polygon leftArea = result.getLeftArea();
				Polygon rightArea = result.getRightArea();
				int leftValue = result.getLeftAreaValue();
				int rightValue = result.getRightAreaValue();
				float threshold = configuration.getConfig().getPrimarySourceConfig().getMarkRecognitionConfig().getMarkRecognitionDensityThreshold();
				Point2D[] corners = result.getDeskewGuideCenterPoints();

				drawDeskewGuide(headerOMRImage, footerOMRImage, previewOMRImage, corners, headerDeskewGuideArea, footerDeskewGuideArea);

				drawFooterDecorations(footerOMRImage, previewOMRImage, leftArea, rightArea, leftValue, rightValue, footerDeskewGuideArea);
				
				drawFormArea(previewOMRImage, result.getPageAreaResultList(), result.getProjectionTranslator(), 
						sourceDirectory.getCurrentFormMaster(), pageIndex, 
						threshold);
			}
			
			private void drawDeskewGuideCanddiateShape(OMRImage deskewOMRImage, OMRImage previewOMRImage, DeskewGuideCandidate deskewGuideCandidate, Color color){
				if(deskewGuideCandidate.getAreaSize() <= 1){
					return;
				}
				Point2D p = new Point2D.Float(deskewGuideCandidate.getGX(), deskewGuideCandidate.getGY());
				float w = deskewGuideCandidate.getBoundingBoxWidth();
				float h = deskewGuideCandidate.getBoundingBoxHeight();
				
				Rectangle2D rect = new Rectangle2D.Double(p.getX() - w / 2, p.getY() - h / 2, w + 1, h + 1);
				GElement boundingBoxGElem = new GShape(rect,
						PageContentModel.O_POINT, 1.0, 1.0,
						color,
						new BasicStroke(0.5f),
						color
						);
				deskewOMRImage.add(boundingBoxGElem);
				previewOMRImage.add(boundingBoxGElem);

				/*
				GElement deskewGuideCandidateLabelGElem = new GText(Font.getFont("seril"), Integer.toString(areaSize),
						p,
						Color.magenta
						);
				deskewOMRImage.add(deskewGuideCandidateLabelGElem);
				 */
			}

			private void drawDeskewGuideShape(OMRImage deskewOMRImage,
					OMRImage previewOMRImage, OMRProcessorErrorModel errorModel,
					List<DeskewGuide> list) {
				for(DeskewGuide deskewGuideCandidate : list){
					Point2D p = deskewGuideCandidate.getCenterPoint();
					int areaSize = deskewGuideCandidate.getAreaSize();
					GElement deskewGuideCandidateLabelGElem = new GText(Font.getFont("seril"), Integer.toString(areaSize),
							p,
							Color.magenta
							);
					deskewOMRImage.add(deskewGuideCandidateLabelGElem);
				}
			}
		};

		worker.execute();
	}

	private void drawFooterDecorations(OMRImage footerOMRImage, OMRImage previewOMRImage, 
			Polygon leftArea, Polygon rightArea, int leftValue, int rightValue, DeskewGuideAreaBitmap deskewGuideExtractor){

		double offset = (previewOMRImage.getSourceImage().getHeight() - deskewGuideExtractor.getDeskewGuideAreaHeight() - deskewGuideExtractor.getFooterVerticalMargin());
		Polygon[] areas = new Polygon[]{leftArea, rightArea};
		double sx = deskewGuideExtractor.getScaleX();
		double sy = deskewGuideExtractor.getScaleY();
		int[] values = new int[]{leftValue, rightValue};
		int padding = 3;

		synchronized(previewOMRImage.getGelemList()){
			for(int i = 0; i < 2; i++){ 
				if(areas[i] == null){
					continue;
				}
				GElement areaRectElem = new GShape(areas[i],
						PageContentModel.O_POINT,
						1.0, 1.0,
						Color.red,
						new BasicStroke(1.0f),
						Color.red
						);
				previewOMRImage.add(areaRectElem);				

				if(0 <= values[i]){
					String label = String.valueOf(values[i]);
					GElement areaLabelGElem = new GText(Font.getFont("seril"), label,
							new Point2D.Double(areas[i].getBounds().getMinX() + padding, areas[i].getBounds().getMaxY() - 1 * padding),
							Color.red
							);
					previewOMRImage.add(areaLabelGElem);
				}
			}
		}

		synchronized(footerOMRImage.getGelemList()){
			for(int i = 0; i < 2; i++){ 
				if(areas[i] == null){
					continue;
				}
				Point2D p = new Point2D.Double( - deskewGuideExtractor.getHorizontalMargin()  * sx,  - offset * sy);
				GElement footerAreaRectGElem = new GShape(areas[i],
						p,
						sx, sy,
						Color.red,
						new BasicStroke(2.0f),
						Color.red
						);
				footerOMRImage.add(footerAreaRectGElem);

				if(0 <= values[i]){
					String label = String.valueOf(values[i]);
					GElement footerAreaLabelGElem = new GText(Font.getFont("seril"), label,
							new Point2D.Double(areas[i].getBounds().getMinX() + padding - p.getX(), areas[i].getBounds().getMaxY() - p.getY() - 1 * padding),
							Color.red
							);
					footerOMRImage.add(footerAreaLabelGElem);
				}
			}
		}
	}

	private void drawDeskewGuide(OMRImage headerOMRImage,
			OMRImage footerOMRImage, OMRImage previewOMRImage,
			Point2D[] corners,
			DeskewGuideAreaBitmap deskewGuideHeaderCandidateFinder,
			DeskewGuideAreaBitmap deskewGuideFooterCandidateFinder) {
		drawDeskewGuide(headerOMRImage,
				footerOMRImage, previewOMRImage,
				deskewGuideHeaderCandidateFinder,
				deskewGuideFooterCandidateFinder,
				corners, null);
	}

	private void drawDeskewGuide(OMRImage headerOMRImage,
			OMRImage footerOMRImage, OMRImage previewOMRImage,
			DeskewGuideAreaBitmap deskewGuideHeaderCandidateFinder,
			DeskewGuideAreaBitmap deskewGuideFooterCandidateFinder,
			Point2D[] corners,
			PageFrameDistortionErrorModel pageFrameDistortionErrorModel) {

		int rectSize = 140;
		int padding = 5;

		double hsx = deskewGuideHeaderCandidateFinder.getScaleX();
		double hsy = deskewGuideHeaderCandidateFinder.getScaleY();
		double fsx = deskewGuideFooterCandidateFinder.getScaleX();
		double fsy = deskewGuideFooterCandidateFinder.getScaleY();

		drawDeskewGuide(previewOMRImage, corners,
				rectSize, padding, pageFrameDistortionErrorModel);

		Rectangle2D deskewGuideHeaderRectScaled = new Rectangle2D.Double(rectSize/-2 * hsx , rectSize/-2 * hsy, rectSize * hsx, rectSize * hsy);
		Rectangle2D deskewGuideFooterRectScaled = new Rectangle2D.Double(rectSize/-2 * fsx , rectSize/-2 * fsy, rectSize * fsx, rectSize * fsy);

		Color color;
		Stroke stroke;
		if(pageFrameDistortionErrorModel == null){
			color = Color.red;
			stroke = new BasicStroke(3.0f);
		}else{
			color = Color.magenta;
			stroke = new BasicStroke(10.0f);
		}

		//header
		synchronized(headerOMRImage.getGelemList()){
			for(int i = 0; i < 2; i++){
				Point2D corner = new Point2D.Double((corners[i].getX() - deskewGuideHeaderCandidateFinder.getHorizontalMargin() ) * hsx, 
						(corners[i].getY() - deskewGuideHeaderCandidateFinder.getHeaderVerticalMargin()) * hsy);
				GElement cornerRectGElem = new GShape(deskewGuideHeaderRectScaled,
						corner,
						1.0, 1.0,
						color,
						stroke,
						color
						);

				GElement cornerLabelGElem = new GText(Font.getFont("seril"), String.valueOf(i),
						new Point2D.Double(corner.getX() + padding, corner.getY() - padding),
						color
						);

				headerOMRImage.add(cornerRectGElem);
				headerOMRImage.add(cornerLabelGElem);
			}
		}

		//footer
		synchronized(footerOMRImage.getGelemList()){
			double offset = (previewOMRImage.getSourceImage().getHeight() - deskewGuideFooterCandidateFinder.getDeskewGuideAreaHeight() - deskewGuideFooterCandidateFinder.getFooterVerticalMargin());
			for(int i = 2; i < 4; i++){ 
				Point2D corner = new Point2D.Double((corners[i].getX() - deskewGuideFooterCandidateFinder.getHorizontalMargin() ) * fsx,
						(corners[i].getY() - offset) * fsy);
				GElement cornerRectGElem = new GShape(deskewGuideFooterRectScaled,
						corner, 1.0, 1.0,
						color,
						stroke,
						color
						);

				GElement cornerLabelGElem = new GText(Font.getFont("seril"), String.valueOf(i),
						new Point2D.Double(corner.getX() + padding, corner.getY() - padding),
						color);
				footerOMRImage.add(cornerRectGElem);
				footerOMRImage.add(cornerLabelGElem);
			}
		}
	}

	private void drawDeskewGuide(OMRImage previewOMRImage,
			Point2D[] corners, int rectSize, int padding, PageFrameDistortionErrorModel pageFrameDistortionErrorModel) {
		Rectangle2D deskewGuideRect = new Rectangle2D.Float(rectSize/-2, rectSize/-2, rectSize, rectSize);

		Color color;
		Stroke stroke;
		if(pageFrameDistortionErrorModel == null){
			color = Color.red;
			stroke = new BasicStroke(3.0f);
		}else{
			color = Color.magenta;
			stroke = new BasicStroke(10.0f);
		}

		synchronized(previewOMRImage.getGelemList()){
			for(int i = 0; i < 4; i++){
				GElement cornerRectGElem = new GShape(deskewGuideRect,
						corners[i], 1.0, 1.0,
						color,
						stroke,
						color
						);
				GElement cornerLabelGElem = new GText(Font.getFont("seril"), String.valueOf(i),
						new Point2D.Double(corners[i].getX() - rectSize/2 + padding , corners[i].getY() + rectSize/2 - padding),
						color);
				previewOMRImage.add(cornerRectGElem);
				previewOMRImage.add(cornerLabelGElem);
			}
		}
	}
	
	

	private void drawFormArea(OMRImage previewOMRImage, List<PageAreaResult> pageAreaResultList, ProjectionTranslator pt, FormMaster master, int pageIndex, float threshold) {
		synchronized(previewOMRImage.getGelemList()){
	
			new OMRImageDrawingPageFormAreaProcessor(master, pageIndex, threshold, pageAreaResultList, previewOMRImage, pt).run();
			
		}
	}

	public class OMRImageDrawingPageFormAreaProcessor extends PageAreaResultListProcessor{
		OMRImage previewOMRImage;
		ProjectionTranslator pt;

		public OMRImageDrawingPageFormAreaProcessor(FormMaster formMaster, int pageIndex, float threshold, List<PageAreaResult> pageAreaResultList, OMRImage previewOMRImage, ProjectionTranslator pt){
			super(formMaster, pageIndex, threshold, pageAreaResultList);
			this.previewOMRImage = previewOMRImage;
			this.pt = pt;
		}
		
		 public void processSelectSingleQuestion(FormArea firstFormArea, FormArea lastFormArea, int numMarks){
			drawQuestionBoundingBox(
					firstFormArea, lastFormArea, numMarks,
					previewOMRImage, pt);
		}
		 
		 public void processSelectSingleQuestionItem(FormArea formArea, float density){
				Color color = drawMarkAreaDensityLabel(previewOMRImage, pt, threshold, formArea, density);
				drawFormArea(formArea, color, previewOMRImage, pt);
		 }

	}
	
	private Color drawMarkAreaDensityLabel(OMRImage previewOMRImage,
			ProjectionTranslator pt, double threshold, FormArea formArea,
			double density) {
		Color color = (density < threshold) ? red: yellow;
		GElement densityLabelGElem = new GText(Font.getFont("seril"), new DecimalFormat("#.##").format(density),
				pt.getPoint((int)formArea.getRect().getX(), (int) formArea.getRect().getMaxY()+13),
				color.darker()
				);
		previewOMRImage.add(densityLabelGElem);
		return color;
	}

	private void drawFormArea(FormArea formArea, Color color,
			OMRImage previewOMRImage, ProjectionTranslator pt) {
		double margin = 3; 
		Rectangle2D formRectWithMargin = new Rectangle2D.Double(formArea.getRect().getX() - margin,
				formArea.getRect().getY() - margin,
				formArea.getRect().getWidth() + margin*2,
				formArea.getRect().getHeight() + margin*2);
		
		GElement formRectGElem = new GShape(pt.createRectPolygon(formRectWithMargin),
				PageContentModel.O_POINT, 1.0, 1.0,
				color,
				new BasicStroke(3.0f),
				color
				);
		
		previewOMRImage.add(formRectGElem);
	}


	private void drawQuestionBoundingBox(
			FormArea firstFormArea, FormArea lastFormArea,
			int numMarks, OMRImage previewOMRImage, ProjectionTranslator pt) {
			Color color = (2 <= numMarks) ? orange:Color.yellow;
			int horizontalMargin = 12;
			int verticalMargin = 6;
			double w = lastFormArea.getRect().getMaxX() - firstFormArea.getRect().getMinX() + horizontalMargin * 2;
			double h = lastFormArea.getRect().getMaxY() - firstFormArea.getRect().getMinY() + verticalMargin * 2;
			Rectangle2D boundingBox = new Rectangle2D.Double(firstFormArea.getRect().getMinX() - horizontalMargin, firstFormArea.getRect().getMinY() - verticalMargin, w, h);
			GElement boundingBoxRectGElem = new GShape(pt.createRectPolygon(boundingBox),
					PageContentModel.O_POINT, 1.0, 1.0,
					color,
					DottedLineStrokeManager.get(0),
					color
					);
			previewOMRImage.add(boundingBoxRectGElem);
	}

	private void drawErrorMessage(
			OMRImage headerOMRImage,
			OMRImage footerOMRImage,
			OMRImage previewOMRImage,
			String errorMessage,
			OMRProcessorErrorModel errorModel) {
		
		if(headerOMRImage != null){
			addErrorMessage(headerOMRImage, headerErrorModels, errorModel, errorMessage);
		}
		if(footerOMRImage != null){
			addErrorMessage(footerOMRImage, footerErrorModels, errorModel, errorMessage);
		}
		if(previewOMRImage != null){
			addErrorMessage(previewOMRImage, previewErrorModels, errorModel, errorMessage);
		}
	}
	
	private void addErrorMessage(OMRImage omrImage, List<OMRProcessorErrorModel> errorModelList, OMRProcessorErrorModel errorModel, String errorMessage){
		errorModelList.add(errorModel);
		omrImage.add(new GText(Font.getFont("serif"), 
					errorMessage, 
					new Point2D.Float(32f, 32f * errorModelList.size()),
					Color.red));
	}
	
	abstract static class BitmapSourceBooleanArrayBuilder implements Runnable {
		int w, y;
		BitmapSourceBooleanArrayBuilder(int w, int y) {
			this.w = w;
			this.y = y;
		}
	}

	public static class MonochromeBitmapFactory{

		static final Future<?>[] bitmapBuilderFutures = new Future[Runtime.getRuntime().availableProcessors()];
		static final ExecutorService bitmapBuilderExecuterService = Executors.newFixedThreadPool(bitmapBuilderFutures.length);

		public static boolean[] create(final OMRProcessorSource omrProcessorSource, final int w, final int h) {
			final boolean[] previewMonochromeBitmap = new boolean[w * h];
			for (int i = 0; i < bitmapBuilderFutures.length; i++) {
				bitmapBuilderFutures[i] = bitmapBuilderExecuterService.submit(new BitmapSourceBooleanArrayBuilder(w, i * h / bitmapBuilderFutures.length) {
					public void run() {
						int index = y * w;
						for (int _y = y; _y < y + h / bitmapBuilderFutures.length; _y++) {
							for (int x = 0; x < w; x++) {
								previewMonochromeBitmap[index] =
										ImageUtil.rgb2gray(omrProcessorSource.getPageImage().getRGB(x, _y)) < 128;
								index++;
							}
						}
					}
				});
			}
			for (Future<?> f : bitmapBuilderFutures) {
				try {
					f.get();
				} catch (InterruptedException ignore) {
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return previewMonochromeBitmap;
		}

	}

	private OMRImage updateOMRImage(
			ObservableObject<OMRImage> observable, PageID pageID,
			boolean[] srcBitmap, int w, int h, boolean translateUpsideDown) {

		OMRImage omrImage = observable.getObject();
		BufferedImage newBufferedImage = null;

		if (omrImage != null && srcBitmap == null) {
			// dispose current image
			omrImage.dispose();
		}

		if (srcBitmap != null && omrImage != null
				&& omrImage.getSourceImage() != null
				&& omrImage.getSourceImage().getWidth() == w
				&& omrImage.getSourceImage().getHeight() == h) {
			newBufferedImage = omrImage.getSourceImage();
			omrImage.clear();
			omrImage.setPageID(pageID);
		}else{

			if (w == 0 || h == 0){
				return omrImage;
			}

			if (omrImage != null) {
				omrImage.dispose();
			}

			byte[] mono = new byte[]{0,(byte)255};
			IndexColorModel cmMono = new IndexColorModel(1,2,mono,mono,mono);
			newBufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY, cmMono);
			omrImage = new OMRImage(pageID, newBufferedImage, System.currentTimeMillis());
			observable.setObject(omrImage);
		}

		if (srcBitmap == null) {
			return omrImage;
		}

		if (translateUpsideDown) {
			for (int i = 0; i < srcBitmap.length; i++) {
				int x = i % w;
				int y = i / w;
				if (srcBitmap[x + (h - y - 1) * w]) {
					newBufferedImage.setRGB(x, y, 0x00000000);
				} else {
					newBufferedImage.setRGB(x, y, 0x00ffffff);
				}
			}
		} else {
			for (int i = 0; i < srcBitmap.length; i++) {
				int x = i % w;
				int y = i / w;
				if (srcBitmap[i]) {
					newBufferedImage.setRGB(x, y, 0x00000000);
				} else {
					newBufferedImage.setRGB(x, y, 0x00ffffff);
				}
			}
		}
		return omrImage;
	}

	void updateRowContents(final int numPages, final long sessionID, final SourceDirectory sourceDirectory, final int resultTableViewRowIndex){
		for(int i= 0; i < numPages; i++){
			updateRowContent(sessionID, sourceDirectory, resultTableViewRowIndex * numPages + i);
		}
	}

	private void updateRowContent(final long sessionID, final SourceDirectory sourceDirectory, int rowIndexArray) {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	public String saveImage(File imageFile)throws IOException{

		BufferedImage image = OMRImageRenderer.renderSelectedGroup(this.previewArea.getObject());

		String imageType = FileUtil.getSuffix(imageFile).toLowerCase();
		if(imageType.equals("tif")){
			imageType = "tiff";
		}
		else if(imageType.equals("jpg")){
			imageType = "jpeg";
		}

		try{
			ImageFactory.writeImage(imageType, image, imageFile);
			image.flush();

		}catch(IOException ex){
			return ex.getLocalizedMessage(); 
		}
		return null;
	}

}