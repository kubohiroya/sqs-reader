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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sqs2.lang.GroupThreadFactory;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.ui.util.ObservableObject;

import org.apache.commons.collections15.map.LRUMap;

public class SamplingImageCanvas extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final double ON_THE_FLY_SCALING_THRESHOLD = 1.0;

	public ObservableObject<OMRImage> area;
	public ObservableObject<Float> scaleObject;
	public JScrollPane scrollPane;
	public JPanel viewportSourcePanel;
	//public FrameRate frameRate = new FrameRate();
	
	private Future<Image> future = null;
	private LRUMap<ImageCacheKey, Image> cache = new LRUMap<ImageCacheKey, Image>(4);
	private ExecutorService singleThreadExecutorService = Executors.newSingleThreadExecutor(new GroupThreadFactory("ImageCanvas", Thread.MAX_PRIORITY, true));

	class ImageCacheKey{
		PageID pageID;
		Float scale;
		long timestamp;
		 ImageCacheKey(PageID pageID, Float scale, long timestamp){
			 this.pageID = pageID;
			 this.scale = scale;
			 this.timestamp = timestamp;
		 }
		 
		 public int hashCode(){
			 return this.pageID.hashCode() + this.scale.hashCode() & (int)timestamp;
		 }

		 public boolean equals(Object o){
			 if(o == this){
				 return true;
			 }
			 if(!(o instanceof ImageCacheKey) || o == null){
				 return false;
			 }
			 ImageCacheKey e = (ImageCacheKey)o; 
			 return scale.equals(e.scale) && pageID.equals(e.pageID) && timestamp == e.timestamp; 
		 }
	}
	
	public SamplingImageCanvas(ObservableObject<OMRImage> area, ObservableObject<Float> scaleObject, Dimension size) {
		this.area = area;
		this.scaleObject = scaleObject;
		this.setFocusable(true);
	}
	
	@Override
	public void setPreferredSize(Dimension size){
		super.setPreferredSize(size);
		if(this.viewportSourcePanel != null){
			int w, h;
			if(scrollPane != null && scrollPane.getVisibleRect() != null){
				Rectangle visibleRect = scrollPane.getViewport().getVisibleRect();
				w = (int)Math.max(visibleRect.getWidth(), size.getWidth());
				h = (int)Math.max(visibleRect.getHeight(), size.getHeight());
			}else{
				w = (int)size.getWidth();
				h = (int)size.getHeight();
			}
			this.viewportSourcePanel.setPreferredSize(new Dimension(w, h));
			this.viewportSourcePanel.revalidate();
		}
	}

	@Override
	public void paintComponent(Graphics _g) {
		Graphics2D g = (Graphics2D)_g;
		
		if(area.getObject() == null || area.getObject().getSourceImage() == null){
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			return;
		}

		float scale = scaleObject.getObject();

		BufferedImage image = area.getObject().getSourceImage();

		if(image == null){
			//frameRate.count();
			return;
		}
		
		AffineTransform savedTransform = g.getTransform();
		AffineTransform at = (AffineTransform)savedTransform.clone();
		at.scale(scale, scale);
		g.setTransform(at);

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		
		paintCanvas(g);
		
		g.setTransform(savedTransform);
		//frameRate.count();
	}
	
	public void clearCache(){
		this.cache.clear();
	}
	
	
	public void paintCanvas(Graphics2D g){
		boolean FINE_IMAGE_MODE = true;
		
		if(FINE_IMAGE_MODE){
			float scale = scaleObject.getObject().floatValue(); 
			if (scale <= ON_THE_FLY_SCALING_THRESHOLD) {
				drawOnTheFlyScaledImage(g);
			} else {
				drawImage(g);
			}
		}else{
			drawImage(g);
		}
	}
	
	private void drawImage(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		BufferedImage image = area.getObject().getSourceImage();
		g.drawImage(image, 0, 0, null);
	}
	
	private void drawOnTheFlyScaledImage(Graphics _g) {
		
		final float scale = scaleObject.getObject();;
		Graphics2D g = (Graphics2D)_g;
		Image image = getCachedImage(new ImageCacheKey(area.getObject().getPageID(), scale, area.getObject().getUpdateTimeStamp()));	
		if (image == null) {

			drawImage(g);
			
			if (this.future != null) {
				this.future.cancel(true);
			}

			Callable<Image> task = new Callable<Image>() {
				public Image call() {
					ImageCacheKey key = new ImageCacheKey(area.getObject().getPageID(), scale, area.getObject().getUpdateTimeStamp());
					Image image = getCachedImage(key);
					if (image != null) {
						return image;
					}
					image = createScaledImage(area.getObject().getSourceImage());
					putCachedImage(key, image);
					repaint();
					return image;
				}
			};


			this.future = this.singleThreadExecutorService.submit(task);
			return;
		}
		
		AffineTransform at = g.getTransform(); 
		g.scale(1/scale, 1/scale);
		g.drawImage(image, 0, 0, null);
		g.setTransform(at);
	}

	private synchronized Image getCachedImage(ImageCacheKey key) {
		return this.cache.get(key);
	}

	@SuppressWarnings("unused")
	private synchronized boolean containsCachedImage(ImageCacheKey key) {
		return this.cache.containsKey(key);
	}

	private synchronized void putCachedImage(ImageCacheKey key, Image image) {
		this.cache.put(key, image);
	}

	private Image createScaledImage(BufferedImage image) {
		float scale = this.scaleObject.getObject();
		int tWidth = (int) (image.getWidth() * scale);
		int tHeight = (int) (image.getHeight() * scale);

		Image scaledImage = image.getScaledInstance(tWidth, tHeight, Image.SCALE_AREA_AVERAGING);
		MediaTracker mediaTracker = new MediaTracker(this);
		mediaTracker.addImage(scaledImage, 0);

		try {
			mediaTracker.waitForID(0);

			PixelGrabber pixelGrabber = new PixelGrabber(scaledImage, 0, 0, -1, -1, false);
			pixelGrabber.grabPixels();
			ColorModel cm = pixelGrabber.getColorModel();

			final int w = pixelGrabber.getWidth();
			final int h = pixelGrabber.getHeight();
			WritableRaster raster = cm.createCompatibleWritableRaster(w, h);
			BufferedImage renderedImage = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(),
					new Hashtable<Object, Object>());
			renderedImage.getRaster().setDataElements(0, 0, w, h, pixelGrabber.getPixels());
			scaledImage.flush();
			return renderedImage;

		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private JPanel createViewportSourcePane(JComponent c){
		viewportSourcePanel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0,0,0,0);
		gbc.fill = GridBagConstraints.BOTH;
		viewportSourcePanel.setLayout(gbl);
		viewportSourcePanel.add(c);
		return viewportSourcePanel;
	}
	
	private JScrollPane createSurroundingScrollPane(JComponent c){
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(c);
		return scrollPane;
	}
	
	private JPanel createSurroundingPane(JComponent p){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(p, BorderLayout.CENTER);
		panel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				new EtchedBorder()));
		panel.setPreferredSize(new Dimension(800, 80));
		return panel; 
	}

	public JScrollPane getScrollPane(){
		return scrollPane;
	}

	public void updateSize(){
		float scale = scaleObject.getObject();
		if(area.getObject() == null || area.getObject().getSourceImage() == null){
			setPreferredSize(new Dimension(800,80));
			revalidate();
			return;
		}
		int width = area.getObject().getSourceImage().getWidth(null);
		int height = area.getObject().getSourceImage().getHeight(null);
		
		if(scale == 0.0f){
			scale = area.getObject().getDefaultScale();
			scaleObject.setObject(scale);
		}
		Dimension sourceSize = new Dimension((int) (width * scale), (int) (height * scale));
		setPreferredSize(sourceSize);
		this.revalidate();
	}

	public JPanel createSurroundingPane(){
		JPanel panel = createSurroundingPane(createSurroundingScrollPane(createViewportSourcePane(this)));
		updateSize();
		return panel;
	}
}