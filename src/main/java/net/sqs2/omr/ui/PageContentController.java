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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoManager;

import net.sqs2.omr.model.ContentAccessor;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.ui.swing.OMRImage;
import net.sqs2.omr.ui.swing.SamplingImageCanvas;
import net.sqs2.omr.ui.util.ObservableObject;
import net.sqs2.omr.ui.util.Observer;

import org.apache.commons.lang.SystemUtils;

class PageContentController{

	ContentPanel contentPanel;
	OMRImageModelPainter builder;

	UndoManager undoManager = new UndoManager();

	PageContentController(ContentPanel contentPanel, OMRImageModelPainter builder){
		this.contentPanel = contentPanel;
		this.builder = builder;
	}

	public void bind(){
		bindFocusAction();
		bindSelectingTabActionListener();
		bindToolActions();

		bindDeskewAreaUpdater();
		bindPreviewAreaUpdater();

		bindDeskewScaleUpdater();
		bindPreviewScaleUpdater();

		bindDrawActions();
		bindSelectActions();
		bindScalingSliderChangeListener();
		bindEditActions();
		bindDrawingPenSelector();

		bindUndoManager();
	}

	private void bindUndoManager(){
		contentPanel.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				boolean mask = (SystemUtils.IS_OS_MAC_OSX && e.isMetaDown()) || e.isControlDown();
				if(! mask){
					return;
				}
				switch (e.getKeyCode()) {
				case KeyEvent.VK_Z:
					if (undoManager.canUndo()) {
						undoManager.undo();
						e.consume();
					}
					break;
				case KeyEvent.VK_Y:	
					if (undoManager.canRedo()) {
						undoManager.redo();
						e.consume();
					}
					break;
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});
	}

	private void bindSelectingTabActionListener() {
		ChangeListener imageSelectorListener = new ChangeListener() {
			@Override public void stateChanged(final ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
					JTabbedPane tabbedPane = ((JTabbedPane)e.getSource());
					int selectedTabIndex = tabbedPane.getSelectedIndex();
					PageContentToolPanel toolPanel = contentPanel.pageContentPanel.toolPanel;
					toolPanel.scaleSliderCardLayout.show(toolPanel.scaleSliderCardPanel, tabbedPane.getTitleAt(selectedTabIndex));
				}});
			}
		};
		contentPanel.pageContentPanel.getTabbedPane().addChangeListener(imageSelectorListener);
	}

	private void bindScalingSliderChangeListener() {
		final PageContentModel contentModel = contentPanel.getPageContentModel();
		final PageContentToolPanel contentToolPanel = contentPanel.pageContentPanel.toolPanel;
		OMRImageModel omrImageModel = contentModel.getOMRImageModel(); 
		ScaleSliderChangeListener previewScaleSliderChangeListener = new ScaleSliderChangeListener(omrImageModel.getPreviewScale());
		ScaleSliderChangeListener deskewScaleSliderChangeListener = new ScaleSliderChangeListener(omrImageModel.getDeskewScale());
		contentToolPanel.previewScaleSlider.addChangeListener(previewScaleSliderChangeListener);
		contentToolPanel.deskewScaleSlider.addChangeListener(deskewScaleSliderChangeListener);
	}

	private void bindPreviewScaleUpdater() {
		contentPanel.getPageContentModel().getOMRImageModel().getPreviewScale().bind(new Observer<Float>() {
			public void update(Float scale) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					contentPanel.pageContentPanel.previewCanvas.updateSize();
					contentPanel.pageContentPanel.previewCanvas.repaint();
				}});
			}
		});
	}

	private void bindDeskewScaleUpdater() {
		contentPanel.getPageContentModel().getOMRImageModel().getDeskewScale().bind(new Observer<Float>() {
			public void update(Float scale) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					contentPanel.pageContentPanel.headerCanvas.updateSize();
					contentPanel.pageContentPanel.footerCanvas.updateSize();
					contentPanel.pageContentPanel.headerCanvas.repaint();
					contentPanel.pageContentPanel.footerCanvas.repaint();
				}});
			}
		});
	}

	private void bindPreviewAreaUpdater() {
		contentPanel.getPageContentModel().getOMRImageModel().getPreviewArea().bind(new Observer<OMRImage>() {
			public void update(OMRImage preview) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					contentPanel.pageContentPanel.previewCanvas.updateSize();
					contentPanel.pageContentPanel.previewCanvas.repaint();
				}});
			}
		});
	}

	private void bindDeskewAreaUpdater() {
		contentPanel.getPageContentModel().getOMRImageModel().getHeaderArea().bind(new Observer<OMRImage>() {
			public void update(OMRImage header) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					contentPanel.pageContentPanel.headerCanvas.updateSize();
					contentPanel.pageContentPanel.headerCanvas.repaint();
				}});
			}
		});
		contentPanel.getPageContentModel().getOMRImageModel().getFooterArea().bind(new Observer<OMRImage>() {
			public void update(OMRImage footer) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					contentPanel.pageContentPanel.footerCanvas.updateSize();
					contentPanel.pageContentPanel.footerCanvas.repaint();
				}});
			}
		});
	}

	private void bindToolActions(){
		final PageContentToolPanel contentToolPanel = contentPanel.pageContentPanel.toolPanel;
		final SamplingImageCanvas previewCanvas = contentPanel.pageContentPanel.previewCanvas;
		contentToolPanel.selectButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
					previewCanvas.area.getObject().rasterizeSelectedGElements();
					contentToolPanel.drawButton.setSelected(false);
					previewCanvas.setCursor(Cursor.getDefaultCursor());
					previewCanvas.repaint();
				}});
			}
		});

		contentToolPanel.drawButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
					previewCanvas.area.getObject().rasterizeSelectedGElements();
					contentToolPanel.selectButton.setSelected(false);
					contentToolPanel.drawButton.setSelected(true);
					contentPanel.getPageContentModel().clearSelecting();
					previewCanvas.setCursor(getDrawingPenCursor(contentToolPanel));
					previewCanvas.repaint();
				}});
			}

			private Cursor getDrawingPenCursor(final PageContentToolPanel contentToolPanel) {
				ButtonModel buttonModel = contentToolPanel.group.getSelection();
				int index = 0;
				for(int i=0; i < contentToolPanel.drawMenuItem.length; i++){
					if(buttonModel == contentToolPanel.drawMenuItem[i].getModel()){
						index = i;
					}
				}
				return PageContentCursors.PEN_CURSOR[index];
			}
		});

		contentToolPanel.saveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				save();
			}
		});
	}

	private void save(){
		final SamplingImageCanvas previewCanvas = contentPanel.pageContentPanel.previewCanvas;
		final PageContentModel contentModel = contentPanel.getPageContentModel();
		final PageID pageID = contentModel.getOMRImageModel().getPreviewArea().getObject().getPageID();
		final File imageFile = new File(contentPanel.sessionModel.getMarkReaderSession().getSourceDirectoryRootFile(),
				pageID.getFileResourceID().getRelativePath());

		setSaveButtonEnabled(false);

		SwingWorker<String,String>worker = new SwingWorker<String,String>(){
			String message = null;
			@Override
			protected String doInBackground() throws Exception {
				previewCanvas.area.getObject().rasterizeSelectedGElements();
				previewCanvas.repaint();
				this.message = contentModel.getOMRImageModel().saveImage(imageFile);
				SessionSource sessionSource = contentPanel.getSessionModel().getMarkReaderSession().getSessionSource();
				ContentAccessor accessor = sessionSource.getContentAccessor();
				accessor.getFileContentCache().clear();
				return this.message;
			}

			@Override
			public void done(){
				if(this.message != null){
					JOptionPane.showConfirmDialog(contentPanel.getTopLevelAncestor(), "ERROR:"+this.message, "ERROR", JOptionPane.ERROR_MESSAGE);
				}else{
					//JOptionPane.showMessageDialog(contentPanel.getTopLevelAncestor(), "INFO: save image succeed:\n" + imageFile.getAbsolutePath() , "INFO", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		};

		worker.execute();

	}

	private void bindDrawingPenSelector() {
		int menuItemIndex = 0;
		final PageContentToolPanel toolPanel = contentPanel.pageContentPanel.toolPanel;
		final SamplingImageCanvas previewCanvas = contentPanel.pageContentPanel.previewCanvas;
		for(JMenuItem drawMenuItem : toolPanel.drawMenuItem){
			final int index = menuItemIndex;
			final int penSizeIndex = menuItemIndex % PageContentCursors.PEN_SIZE_ARRAY.length;
			final int penColorIndex =  (menuItemIndex & 4) >> 2;
				final int penShapeIndex = (menuItemIndex & 8) >> 3;
				drawMenuItem.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent ev){
						final Cursor cursor = PageContentCursors.PEN_CURSOR[index];
						builder.penShapeIndex = penShapeIndex;
						builder.penSizeIndex = penSizeIndex;
						builder.penColorIndex = penColorIndex;
						SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
							toolPanel.selectButton.setSelected(false);
							toolPanel.drawButton.setSelected(true);
							previewCanvas.setCursor(cursor);
						}});
					}
				});
				menuItemIndex++;
		}
	}

	private void bindSelectActions() {
		final PageContentModel pageContentModel = contentPanel.getPageContentModel();
		final PageContentPanel pageContentPanel = contentPanel.pageContentPanel;
		final SamplingImageCanvas previewCanvas = pageContentPanel.previewCanvas;
		final PageContentToolPanel toolPanel = pageContentPanel.toolPanel;

		previewCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable(){public void run(){
					if(toolPanel.selectButton.isSelected()){
						pageContentModel.checkFlagSelectedShapeDrag(e.getPoint());
						if(pageContentModel.isSelectedShapeDagging() == false){
							if(previewCanvas.area.getObject() == null || previewCanvas.area.getObject().getSourceImage() == null){
								return;
							}
							previewCanvas.area.getObject().rasterizeSelectedGElements();
							pageContentModel.mouseSelectionStarted(e.getPoint());
							pageContentModel.setPrevCursorSelectPosition(e.getPoint());
							setSaveButtonEnabled(true);
							previewCanvas.repaint();
						}else{
							previewCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							pageContentModel.setPrevCursorSelectPosition(e.getPoint());
						}
					}
				}});
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable(){public void run(){
					if(toolPanel.selectButton.isSelected()){
						if(pageContentModel.isSelectedShapeDagging()){
							pageContentModel.setSelectedShapeDagging(false);
							previewCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}else{
							pageContentModel.mouseSelectionEnded();
							setCutCopyButtonEnabled(true);
						}
					}
				}});
			}
		});

		previewCanvas.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						if(toolPanel.selectButton.isSelected()){	
							if(pageContentModel.isSelectedShapeDagging()){
								Point prev = pageContentModel.getPrevCursorSelectPosition();
								float scale = pageContentModel.getOMRImageModel().getPreviewScale().getObject().floatValue();
								int dx = (int)((e.getPoint().x - prev.x) / scale);
								int dy = (int)((e.getPoint().y - prev.y) / scale);
								previewCanvas.area.getObject().moveSelectedObject(dx, dy);
								pageContentModel.setPrevCursorSelectPosition(e.getPoint());
								setSaveButtonEnabled(true);
								previewCanvas.repaint();
							}else{
								pageContentModel.mouseSelectionUpdate(e.getPoint());
								setSaveButtonEnabled(true);
								previewCanvas.repaint();
							}
						}
					}
				});
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					if(toolPanel.selectButton.isSelected()){
						if(pageContentModel.checkFlagSelectedShapeDrag(e.getPoint())){
							if(SystemUtils.IS_OS_MAC_OSX){
								previewCanvas.setCursor(PageContentCursors.MOVE_CURSOR);
							}else{
								previewCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
							}
						}else{
							previewCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}});
			}
		});
	}

	private void bindFocusAction() {
		final SamplingImageCanvas previewCanvas = contentPanel.pageContentPanel.previewCanvas;
		previewCanvas.area.bind(new Observer<OMRImage>() {
			public void update(OMRImage preview) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					previewCanvas.updateSize();
					previewCanvas.repaint();
				}});
			}
		});

		MouseAdapter previewCanvasRequestFocusAction = new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent ev){
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					previewCanvas.requestFocusInWindow();
				}});
			}
		};

		previewCanvas.addMouseListener(previewCanvasRequestFocusAction);
		previewCanvas.scrollPane.getViewport().addMouseListener(previewCanvasRequestFocusAction);
		previewCanvas.viewportSourcePanel.addMouseListener(previewCanvasRequestFocusAction);
	}

	private void bindEditActions() {
		final PageContentPanel pageContentPanel = contentPanel.pageContentPanel;
		final SamplingImageCanvas previewCanvas = pageContentPanel.previewCanvas;
		InputMap inputMap = previewCanvas.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = previewCanvas.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK), "save");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
		actionMap.put("save", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), "copy");
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy");
		actionMap.put("copy", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					contentPanel.getPageContentModel().copy();
					setPasteButtonEnabled(true);
					previewCanvas.repaint();
					//BufferedImage image =contentPanel.getPageContentModel(). getPreviewArea().getObject().sourceImage;
				}});
			}
		});

		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), "cut");
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "cut");
		actionMap.put("cut", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					contentPanel.getPageContentModel().cut();
					setSaveButtonEnabled(true);
					previewCanvas.repaint();
				}});
			}
		});

		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), "paste");
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "paste");
		actionMap.put("paste", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					contentPanel.getPageContentModel().paste();
					setSaveButtonEnabled(true);
					previewCanvas.repaint();
				}});
			}
		});

		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK), "selectAll");
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), "selectAll");
		actionMap.put("selectAll", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("SELECT ALL");
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					contentPanel.getPageContentModel().selectAll();
					setCutCopyButtonEnabled(true);
					previewCanvas.repaint();
				}});
			}
		});

		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_DOWN_MASK), "undo");
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
		actionMap.put("undo", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("UNDO");

				// TODO: implement UNDO action

				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					// TODO: update content
					setSaveButtonEnabled(true);
				}});
			}
		});

		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_DOWN_MASK), "redo");
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo");
		actionMap.put("redo", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("REDO");

				// TODO: implement REDO action

				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					// TODO: update content
					setSaveButtonEnabled(true);
				}});
			}
		});

		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		actionMap.put("up", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					contentPanel.getPageContentModel().up();
					setSaveButtonEnabled(true);
					previewCanvas.repaint();
				}});
			}
		});
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		actionMap.put("down", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					contentPanel.getPageContentModel().down();
					setSaveButtonEnabled(true);
					previewCanvas.repaint();
				}});
			}
		});
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
		actionMap.put("left", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					contentPanel.getPageContentModel().left();
					setSaveButtonEnabled(true);
					previewCanvas.repaint();
				}});
			}
		});
		inputMap.put(	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		actionMap.put("right", 
				new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					contentPanel.getPageContentModel().right();
					setSaveButtonEnabled(true);
					previewCanvas.repaint();
				}});
			}
		});			
	}

	private void bindDrawActions() {
		final PageContentModel contentModel = contentPanel.getPageContentModel();
		final PageContentPanel pageContentPanel = contentPanel.pageContentPanel;

		pageContentPanel.previewCanvas.addMouseListener(new MouseAdapter() {

			// drawPoint

			@Override
			public void mousePressed(final MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					if(pageContentPanel.toolPanel.drawButton.isSelected()){
						ObservableObject<OMRImage> observable = contentModel.getOMRImageModel().getPreviewArea();
						float scale = contentModel.getOMRImageModel().getPreviewScale().getObject().floatValue();
						builder.drawPoint(e, scale, observable);
						contentModel.setPrevCursorSelectPosition(e.getPoint());
						pageContentPanel.toolPanel.saveButton.setEnabled(true);
						contentPanel.pageContentPanel.previewCanvas.repaint();
					}
				}});
			}

			// penUp

			@Override
			public void mouseReleased(final MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					if(pageContentPanel.toolPanel.drawButton.isSelected()){
						float scale = contentModel.getOMRImageModel().getPreviewScale().getObject().floatValue();
						ObservableObject<OMRImage> omrImage = contentModel.getOMRImageModel().getPreviewArea();
						builder.penUp(e, scale, omrImage);
					}
					contentModel.setPrevCursorSelectPosition(null);
				}});
			}
		});

		pageContentPanel.previewCanvas.addMouseMotionListener(new MouseAdapter() {

			// drawLine

			@Override
			public void mouseDragged(final MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable(){@Override public void run(){
					if(pageContentPanel.toolPanel.drawButton.isSelected()){
						ObservableObject<OMRImage> omrImage = contentModel.getOMRImageModel().getPreviewArea();
						float scale = contentModel.getOMRImageModel().getPreviewScale().getObject().floatValue();
						builder.drawLine(e, scale, omrImage);
						setSaveButtonEnabled(true);
						contentPanel.pageContentPanel.previewCanvas.repaint();
					}
				}});
			}
		});
	}

	private void setPasteButtonEnabled(boolean enabled){
		contentPanel.sessionModel.markReaderModel.getMenuState().getObject().setPasteButtonEnabled(enabled);
		contentPanel.sessionModel.markReaderModel.getMenuState().update();
	}

	private void setCutCopyButtonEnabled(boolean enabled){
		contentPanel.sessionModel.markReaderModel.getMenuState().getObject().setCutCopyButtonEnabled(enabled);
		contentPanel.sessionModel.markReaderModel.getMenuState().update();
	}

	private void setSaveButtonEnabled(boolean enabled){
		final PageContentToolPanel contentToolPanel = contentPanel.pageContentPanel.toolPanel;
		contentToolPanel.saveButton.setEnabled(enabled);
		contentPanel.sessionModel.markReaderModel.getMenuState().getObject().setSaveButtonEnabled(enabled);
		contentPanel.sessionModel.markReaderModel.getMenuState().update();
	}

}
