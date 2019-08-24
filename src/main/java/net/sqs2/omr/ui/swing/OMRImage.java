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
package net.sqs2.omr.ui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sqs2.omr.model.PageID;
import net.sqs2.omr.ui.OMRImageRenderer;

public class OMRImage {
	
	public static final Color BACKGROUND_COLOR = Color.white;
		
	private static final String SELECTING_NAME = "selecting";
	private static final String SELECTED_NAME = "selected";
	private static final String SELECTED_BOX_NAME = "selectedBox";
	
	private static final String SELECTING_RECT_NAME = "rect";
	private static final String SELECTED_IMAGE_NAME = "image";
	
	private static final GGroup SELECTING_GROUP = new GGroup(new Point2D.Double(0,0));

	private float defaultScale;
	
	private PageID pageID;
	private BufferedImage sourceImage;
	private List<GElement> gelemList;
	private Map<String,GElement> gelemMap;
	private long initialTimeStamp;
	private long updateTimeStamp;
	private final GGroup selectedGroup = new GGroup(new Point2D.Double(0,0));	
	private final GGroup selectedBoxGroup = new GGroup(new Point2D.Double(0,0));	
	
	public OMRImage(PageID pageID, BufferedImage sourceImage, long timeStamp) {
		this.pageID = pageID;
		this.sourceImage = sourceImage;
		this.gelemList = new ArrayList<GElement>();
		this.gelemMap = new HashMap<String,GElement>();
		this.initialTimeStamp = timeStamp;
		this.updateTimeStamp = timeStamp;
		initialize();
	}
	
	public void initialize(){
		this.gelemMap.put(SELECTING_NAME, SELECTING_GROUP);
		this.gelemMap.put(SELECTED_NAME, selectedGroup);
		selectedGroup.getMap().put(SELECTED_BOX_NAME, selectedBoxGroup);
	}
	
	public float getDefaultScale(){
		return defaultScale;
	}
	
	public void setDefaultScale(float defaultScale){
		this.defaultScale = defaultScale;
	}
	
	public PageID getPageID(){
		return this.pageID;
	}
	
	public void setPageID(PageID pageID){
		this.pageID = pageID;
	}
	
	public BufferedImage getSourceImage(){
		return sourceImage;
	}

	public long getUpdateTimeStamp(){
		return this.updateTimeStamp;
	}
	
	public GGroup getSelectedGroup(){
		return this.selectedGroup;
	}
	
	public void moveSelectedObject(int dx, int dy){
		for(GElement gelem: selectedGroup.getMap().values()){
			Point2D p = new Point2D.Double(gelem.getPosition().getX()+dx, gelem.getPosition().getY()+dy);
			gelem.getPosition().setLocation(p);
		}
	}

	private Shape createSelectAllRectangle(double scale){
		double w = sourceImage.getWidth() / scale;
		double h = sourceImage.getHeight() / scale;
		return new Rectangle2D.Double(0, 0, w, h );
	}

	public void selectAll(double scale){
		Shape selectAllRectangle = createSelectAllRectangle(scale);
		final GElement gelem = createDottedLineStrokeShape(selectAllRectangle);
		SELECTING_GROUP.put(SELECTING_RECT_NAME, gelem);
	}
	
	public BufferedImage copy(){
		GShape selectedRectangle = ((GShape)SELECTING_GROUP.get(SELECTING_RECT_NAME));
		if(selectedRectangle == null){
			return null;
		}
		Rectangle rect = selectedRectangle.getBounds();
		if(rect.width == 0 || rect.height == 0){
			return null;
		}
		Shape shape = ((GShape)SELECTING_GROUP.get(SELECTING_RECT_NAME)).getShape();
		return cropImageWithShape(sourceImage.getSubimage(rect.x, rect.y, rect.width, rect.height), shape);
	}
	
	public BufferedImage cut(double scale){
		GShape selectedRectangle = ((GShape)SELECTING_GROUP.get(SELECTING_RECT_NAME));
		if(selectedRectangle == null){
			return null;
		}
		Rectangle rect = selectedRectangle.getBounds();
		if(rect.width == 0 || rect.height == 0){
			return null;
		}
		Shape shape = ((GShape)SELECTING_GROUP.get(SELECTING_RECT_NAME)).getShape();
		BufferedImage copiedImage = cropImageWithShape(sourceImage.getSubimage(rect.x, rect.y, rect.width, rect.height), shape);
		
		Graphics2D g = (Graphics2D)sourceImage.getGraphics();
		g.scale(1.0/ selectedRectangle.getScaleX() / scale, 1.0 / selectedRectangle.getScaleY() / scale);
		g.translate(selectedRectangle.getPosition().getX() / scale, selectedRectangle.getPosition().getY() / scale);
		g.setColor(BACKGROUND_COLOR);
		g.fill(shape);
		g.dispose();

		setSourcemageChanged();
		
		return copiedImage;
	}
	
	public void paste(double scale, Point2D point, BufferedImage copiedImage){
		Point2D startPoint = null;
		if(point != null){
			double x = point.getX() / scale;
			double y = point.getY() / scale;
			startPoint = new Point2D.Double(x, y);
		}else{
			int x = ( sourceImage.getWidth() - copiedImage.getWidth())/2;
			int y = ( sourceImage.getHeight() - copiedImage.getHeight())/2;
			startPoint = new Point2D.Double(x, y);
		}
		SELECTING_GROUP.clear();
		selectedGroup.put(SELECTED_IMAGE_NAME, new GImage(startPoint, copiedImage));
		Shape rect = new Rectangle2D.Double(0.0, 0.0, copiedImage.getWidth(), copiedImage.getHeight());
		Stroke pathStroke = null; // FIXME1 
		selectedBoxGroup.put(SELECTED_BOX_NAME, new GShape(rect, startPoint, 1.0, 1.0, Color.BLUE, pathStroke, null));
	}
	
	public static void clearSelectingRectangle(){
		SELECTING_GROUP.remove(SELECTING_RECT_NAME);
		//SELECTING_GROUP.clear();
	}


	public void setSelectingRectangle(Rectangle2D rectangle){
		GShape shape = createDottedLineStrokeShape(rectangle);
		SELECTING_GROUP.put(SELECTING_RECT_NAME, shape);
	}
	
	public GShape createDottedLineStrokeShape(Shape shape) {
		Point2D position = new Point2D.Double(0, 0);
		Color pathColor = Color.black;
		Stroke pathStroke = DottedLineStrokeManager.get(0);
		Color fillColor = null;
		GShape gshape = new GShape(shape, position, 1.0, 1.0, pathColor, pathStroke, fillColor, Color.white);
		return gshape;
	}

	public void updatePathStrokeAnimation(int selectionStrokeIndex){
		for(GElement gl: SELECTING_GROUP.getMap().values()){
			if(gl instanceof GShape){
				((GShape)gl).setPathStroke(DottedLineStrokeManager.get(selectionStrokeIndex));
			}
		}
	}
	
	public void add(GElement gelem){
		this.gelemList.add(gelem);
	}
	
	public GElement get(int index){
		return this.gelemList.get(index);
	}
	
	public void put(String key, GElement gelem){
		this.gelemMap.put(key, gelem);
	}
	
	public GElement get(String key){
		return this.gelemMap.get(key);
	}
	
	public void remove(String key){
		this.gelemMap.remove(key);
	}
	
	public void clear(){
		this.selectedGroup.clear();
		SELECTING_GROUP.clear();
		this.gelemList.clear();
	}
	
	public void dispose(){
		this.clear();	
		if(this.sourceImage != null){
			this.sourceImage.flush();
		}
	}
	
	public void setSourcemageChanged(){
		this.updateTimeStamp = System.currentTimeMillis();
	}
	
	public List<GElement> getGelemList() {
		return gelemList;
	}

	public Map<String, GElement> getGelemMap() {
		return gelemMap;
	}

	public int hashCode(){
		return (int)updateTimeStamp;
	}
	
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof OMRImage) || o == null){
			return false;
		}
		OMRImage e = (OMRImage)o;
		return (e.sourceImage.equals(sourceImage)) && e.pageID.equals(pageID) && e.gelemList.equals(gelemList) && e.gelemMap.equals(gelemMap) && initialTimeStamp == e.initialTimeStamp && updateTimeStamp == e.updateTimeStamp;
	}
	
	private BufferedImage cropImageWithShape(BufferedImage image, Shape shape){
		// FIXME: cropImage, setTransparency
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		newImage.getGraphics().drawImage(image, 0, 0, null);
		return newImage;
	}
	
	public boolean hasSourceImageChanged(){
		return initialTimeStamp != updateTimeStamp;
	}
	
	public void rasterizeSelectedGElements() {
		if(! selectedGroup.getMap().isEmpty()){
			OMRImageRenderer.renderSelectedGroup(this);
			setSourcemageChanged();
			selectedGroup.clear();
		}
	}


}