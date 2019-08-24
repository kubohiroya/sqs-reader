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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sqs2.omr.ui.swing.OMRImageCanvas;
import net.sqs2.omr.ui.swing.SamplingImageCanvas;

class PageContentPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	
	private PageContentModel pageContentModel;
	
	JTabbedPane tabbedPanel;
	PageContentToolPanel toolPanel; 
	JPanel deskewPanel;
	JPanel previewPanel;
	SamplingImageCanvas headerCanvas;
	SamplingImageCanvas footerCanvas;
	SamplingImageCanvas previewCanvas;

	PageContentPanel (PageContentModel pageContentModel){
		this.pageContentModel = pageContentModel;
		this.toolPanel = new PageContentToolPanel(pageContentModel);
		this.tabbedPanel = createContentImageTabbedPanel();
		setLayout(new BorderLayout());
		add(tabbedPanel, BorderLayout.CENTER);
		add(toolPanel, BorderLayout.SOUTH);
	}
	
	private JTabbedPane createContentImageTabbedPanel(){
		
		OMRImageModel omrImageModel = pageContentModel.getOMRImageModel();
		BufferedImage headerImage = omrImageModel.getHeaderArea().getObject().getSourceImage();
		BufferedImage footerImage = omrImageModel.getFooterArea().getObject().getSourceImage();
		BufferedImage previewImage = omrImageModel.getPreviewArea().getObject().getSourceImage();
		Dimension headerPanelSize = createPanelSize(headerImage, omrImageModel.getDeskewScale().getObject());
		Dimension footerPanelSize = createPanelSize(footerImage, omrImageModel.getDeskewScale().getObject());
		Dimension previewPanelSize = createPanelSize(previewImage, omrImageModel.getPreviewScale().getObject());
		
		headerCanvas = new OMRImageCanvas(omrImageModel.getHeaderArea(), omrImageModel.getDeskewScale(), headerPanelSize);
		footerCanvas = new OMRImageCanvas(omrImageModel.getFooterArea(), omrImageModel.getDeskewScale(), footerPanelSize);
		previewCanvas = new OMRImageCanvas(omrImageModel.getPreviewArea(), omrImageModel.getPreviewScale(), previewPanelSize);
		
		deskewPanel = new JPanel();
		deskewPanel.setLayout(new BoxLayout(deskewPanel, BoxLayout.Y_AXIS));
		deskewPanel.add(headerCanvas.createSurroundingPane());
		deskewPanel.add(footerCanvas.createSurroundingPane());
		
		previewPanel = new JPanel();
		previewPanel.setLayout(new BorderLayout());
		previewPanel.add(previewCanvas.createSurroundingPane(), BorderLayout.CENTER);
		
		final JTabbedPane contentBitmapTabbedPanel = new JTabbedPane();
		contentBitmapTabbedPanel.setTabPlacement(JTabbedPane.BOTTOM);
		contentBitmapTabbedPanel.add(ContentPanel.MODE_NAMES[0], previewPanel);
		contentBitmapTabbedPanel.add(ContentPanel.MODE_NAMES[1], deskewPanel);
		
		contentBitmapTabbedPanel.addComponentListener(new ComponentAdapter(){

			@Override
			public void componentResized(ComponentEvent e) {
			}

		});
		
		return contentBitmapTabbedPanel;
	}
	
	public JTabbedPane getTabbedPane(){
		return tabbedPanel;
	}
	
	private static Dimension createPanelSize(BufferedImage image, float scale) {
		if(image == null){
			return new Dimension(0, 0);
		}else{
			int w = image.getWidth();
			int h = image.getHeight();
			return new Dimension((int) (w * scale), (int) (h * scale));
		}
	}

}
