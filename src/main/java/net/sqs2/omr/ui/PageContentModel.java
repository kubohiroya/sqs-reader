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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import net.sqs2.omr.ui.swing.GElement;
import net.sqs2.omr.ui.swing.GImage;
import net.sqs2.omr.ui.swing.GShape;
import net.sqs2.omr.ui.swing.OMRImage;

class PageContentModel{
	
	public static final Point2D O_POINT = new Point2D.Double(0, 0);

	private ScheduledExecutorService strokeAnimationUpdater = Executors.newScheduledThreadPool(1);
	
	private Future<?> strokeAnimationFuture; // FIXME: this future shall be canceled in the end of application process.
	
	private Point initialMousePosition = null;
	private Point prevCursorSelectPosition = null;
	private Point prevCursorDrawPosition = null;
	private boolean isSelectedShapeDagging = false;
	private BufferedImage copiedImage = null;

	private OMRImageModel omrImageModel;
	 
	PageContentModel(){
		this.omrImageModel = new OMRImageModel();
		
		this.strokeAnimationFuture = strokeAnimationUpdater.scheduleWithFixedDelay(new Runnable(){
			private int strokeAnimationFrameIndex = 0;
			public void run(){
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							OMRImage omrImage = omrImageModel.previewArea.getObject();
							if(omrImage == null){
								return;
							}
							omrImageModel.previewArea.getObject().updatePathStrokeAnimation(strokeAnimationFrameIndex++);
							omrImageModel.previewArea.update();
						}
					});
			}
		}, 0, 3000L, TimeUnit.SECONDS);

	}
	
	public void clear(){
		this.initialMousePosition = null;
		this.prevCursorSelectPosition = null;
		this.isSelectedShapeDagging = false;
		OMRImage.clearSelectingRectangle();
		this.omrImageModel.setEmptyPageContent();
	}
	
	public void stopStrokeAnimation(){
		this.strokeAnimationFuture.cancel(true);
	}

	public OMRImageModel getOMRImageModel(){
		return omrImageModel;
	}
	
	public void setSelectedShapeDagging(boolean isSelectedShapeDagging){
		this.isSelectedShapeDagging = isSelectedShapeDagging;
	}
	
	public boolean isSelectedShapeDagging(){
		return this.isSelectedShapeDagging;
	}
	
	public void setPrevCursorSelectPosition(Point prevCursorPosition){
		this.prevCursorSelectPosition = prevCursorPosition;
	}
	public void setPrevCursorDrawPosition(Point prevCursorPosition){
		this.prevCursorDrawPosition = prevCursorPosition;
	}
	
	public Point getPrevCursorSelectPosition(){
		return this.prevCursorSelectPosition;
	}
	
	public Point getPrevCursorDrawPosition(){
		return this.prevCursorDrawPosition;
	}

	public void mouseSelectionStarted(Point mousePosition){
		this.initialMousePosition = mousePosition;
		this.prevCursorSelectPosition = mousePosition;
		this.clearSelecting();
	}
	
	public void finishDrawing(){
		this.prevCursorSelectPosition = null;
	}
	
	public void mouseSelectionUpdate(Point mousePosition){
		double scale = this.omrImageModel.getPreviewScaleValue();
		if(this.initialMousePosition == null){
			return;
		}
		final Rectangle2D rectangle = this.omrImageModel.createRectangleOnCanvas(this.initialMousePosition, mousePosition, scale);
		omrImageModel.previewArea.getObject().setSelectingRectangle(rectangle);
		omrImageModel.previewArea.update();
	}
	
	public void mouseSelectionEnded(){
		this.initialMousePosition = null;
	}

	/*
	public void startSelectionStrokeAnimationUpdater() {
		clearSelection();
	}*/
	
	void clearSelecting(){
		OMRImage.clearSelectingRectangle();
	}
		
	private void move(final int dx, final int dy){
		OMRImage omrImage = omrImageModel.previewArea.getObject();
		if(omrImage == null){
			return;
		}
		omrImage.moveSelectedObject(dx, dy);
		omrImageModel.previewArea.update();
	}
	
	public boolean checkFlagSelectedShapeDrag(Point p) {
		OMRImage omrImage = omrImageModel.previewArea.getObject();
		if(omrImage == null){
			return false;
		}
		float scale = omrImageModel.getPreviewScaleValue();
		setSelectedShapeDagging(false);
		double x = p.getX() / scale;
		double y = p.getY() / scale;
		for(GElement ge : omrImage.getSelectedGroup().getMap().values()){
			if(ge instanceof GImage){
				if(((GImage)ge).contains(x, y)){
					setSelectedShapeDagging(true);
					return true;
				}
			}
		}
		return false;
	}

	public void up(){
		move(0, -1);
	}
	
	public void down(){
		move(0, 1);
	}

	public void left(){
		move(-1, 0);
	}

	public void right(){
		move(1, 0);
	}
	
	public void selectAll(){
		double scale = omrImageModel.previewScale.getObject().doubleValue();
		omrImageModel.previewArea.getObject().selectAll(scale);
	}
	
	public void copy(){
		System.err.println("COPY");
		copiedImage = omrImageModel.previewArea.getObject().copy();
	}

	public void cut(){
		System.err.println("CUT");
		float scale = omrImageModel.previewScale.getObject();
		copiedImage = omrImageModel.previewArea.getObject().cut(scale);
	}

	public void paste(){
		if(copiedImage == null){
			return;
		}
		System.err.println("PASTE");
		double scale = omrImageModel.previewScale.getObject();
		omrImageModel.previewArea.getObject().paste(scale, prevCursorSelectPosition, copiedImage);
	}

	public GShape selectGElement(Point2D p){
		for(GElement gelem: omrImageModel.previewArea.getObject().getGelemList()){
			if(gelem instanceof GShape){
				GShape gshape = (GShape) gelem;
				if(gshape.contains(p)){
					return gshape;
				}
			}
		}
		return null;
	}
	
	public List<GShape> selectGElements(Rectangle2D rect){
		List<GShape> ret = new ArrayList<GShape>();  
		for(GElement gelem: omrImageModel.previewArea.getObject().getGelemList()){
			if(gelem instanceof GShape){
				GShape gshape = (GShape) gelem;
				if(gshape.getShape().intersects(rect)){
					ret.add(gshape);
				}
			}
		}
		return ret;
	}

	public void focusGElement(GElement g){
		throw new RuntimeException("NOT_IMPLEMENTED");
	}
	
	public void initialize(){
		OMRImage omrImage = omrImageModel.previewArea.getObject();
		if(omrImage != null){
			omrImage.initialize();
		}
		initialMousePosition = null;
	}
	
}
