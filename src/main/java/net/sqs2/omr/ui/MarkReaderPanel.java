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
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import net.sqs2.omr.ui.swing.Blocker;
import net.sqs2.omr.ui.swing.View;

class MarkReaderPanel extends View {
	public static final String DRAG_AND_DROP_PROMPT_MESSAGE = "Drag and Drop Your SourceDirectory into Here!";
	public static MarkReaderPanel singleton = null;
	
	MarkReaderModel model;
	JTabbedPane tabbedPane;
	
	JMenuItem openMenuItem;
	JMenuItem saveMenuItem;
	JMenuItem exportMenuItem;
	JMenuItem clearMenuItem;
	JMenuItem closeMenuItem;
	JMenuItem quitMenuItem;
	
	JMenuItem undoMenuItem;
	JMenuItem redoMenuItem;
	JMenuItem cutMenuItem;
	JMenuItem copyMenuItem;
	JMenuItem pasteMenuItem;
	JMenuItem selectAllMenuItem;
	
	Blocker blocker;
	
	MarkReaderPanel(final MarkReaderModel model) {
		this.model = model;
		if(MarkReaderPanel.singleton == null){
			MarkReaderPanel.singleton = this;
		}else{
			throw new RuntimeException("error: cannot instantiate duplicated object");
		}
		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		
		this.tabbedPane = new JTabbedPane(){
			private static final long serialVersionUID = 0L; 
			@Override
			public void paintComponent(Graphics g){
				if(this.getComponentCount() == 0){
					int w = getWidth();
					int h = getHeight();
					String label = DRAG_AND_DROP_PROMPT_MESSAGE;
					g.drawString(label, w/2 - 100, h/2);
				}else{
					super.paintComponent(g);
				}
			}
		};

		this.content = new JPanel();
		content.setLayout(new BorderLayout());
		content.add(tabbedPane, BorderLayout.CENTER);
	
		frame = new JFrame() ;
		blocker = new Blocker();
		frame.setGlassPane(blocker);
		frame.setTitle(this.model.getApplicationName()+" "+this.model.getVersion());
		JMenuBar menuBar = createJMenuBar(frame);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		frame.add(content);
	}
	
	public static MarkReaderPanel getSingleton(){
		return singleton;
	}
	
	private JMenuBar createJMenuBar(final JFrame frame) {
		JMenuBar menuBar = new JMenuBar();
		initFileMenu(frame, menuBar);
		initEditMenu(frame, menuBar);
		initHelpMenu(frame, menuBar);
		return menuBar;
	}

	private void initFileMenu(final JFrame frame, JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		initOpenMenuItem(frame, fileMenu);
		fileMenu.addSeparator();
		initSaveMenuItem(fileMenu);
		fileMenu.addSeparator();
		initExportMenuItem(frame, fileMenu);
		fileMenu.addSeparator();
		initClearMenuItem(frame, fileMenu);
		initCloseMenuItem(frame, fileMenu);
		fileMenu.addSeparator();
		initQuiteMenuItem(frame, fileMenu);
		saveMenuItem.setEnabled(false);
		menuBar.add(fileMenu);
	}
	
	private void initEditMenu(final JFrame frame, JMenuBar menuBar){
		JMenu editMenu = new JMenu("Edit");
		initUndoMenuItem(frame, editMenu);
		initRedoMenuItem(frame, editMenu);
		editMenu.addSeparator();
		initCutMenuItem(frame, editMenu);
		initCopyMenuItem(frame, editMenu);
		initPasteMenuItem(frame, editMenu);
		initSelectAllMenuItem(frame, editMenu);
		undoMenuItem.setEnabled(false);// FIXME! remove before implementation
		redoMenuItem.setEnabled(false);// FIXME! remove before implementation
		cutMenuItem.setEnabled(false);
		copyMenuItem.setEnabled(false);
		pasteMenuItem.setEnabled(false);
		menuBar.add(editMenu);
	}
	
	private void initHelpMenu(final JFrame frame, JMenuBar menuBar){
		JMenu helpMenu = new JMenu("Help");
		// TODO: about, WebSite
		// TODO: License Information, dependency list
		
		JMenuItem aboutMenuItem = new JMenuItem("About...");
		helpMenu.add(aboutMenuItem);
		
		menuBar.add(helpMenu);
		menuBar.add(helpMenu);
	}
	
	private void initOpenMenuItem(final JFrame frame, JMenu fileMenu) {
		openMenuItem = new JMenuItem("Open Directory...");
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_DOWN_MASK));
		fileMenu.add(openMenuItem);
	}

	private void initSaveMenuItem(final JMenu fileMenu) {
		saveMenuItem = new JMenuItem("Save Current Editing Image");
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK));
		fileMenu.add(saveMenuItem);
	}

	private void initExportMenuItem(final JFrame frame, JMenu fileMenu) {
		exportMenuItem = new JMenuItem("Export...");
		exportMenuItem.setToolTipText("Export ErrorFiles in a Zip File...");
		fileMenu.add(exportMenuItem);
	}

	private void initClearMenuItem(final JFrame frame, JMenu fileMenu) {
		clearMenuItem = new JMenuItem("Clear");
		fileMenu.add(clearMenuItem);
	}

	private void initCloseMenuItem(final JFrame frame, JMenu fileMenu) {
		closeMenuItem = new JMenuItem("Close");
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_DOWN_MASK));
		fileMenu.add(closeMenuItem);
	}

	private void initUndoMenuItem(final JFrame frame, JMenu editMenu) {
		undoMenuItem = new JMenuItem("Undo");
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK));
		editMenu.add(undoMenuItem);		
	}

	private void initRedoMenuItem(final JFrame frame, JMenu editMenu) {
		redoMenuItem = new JMenuItem("Redo");
		redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.META_DOWN_MASK));
		editMenu.add(redoMenuItem);
	}

	private void initCutMenuItem(final JFrame frame, JMenu editMenu) {
		cutMenuItem = new JMenuItem("Cut");
		cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_DOWN_MASK));
		editMenu.add(cutMenuItem);
	}

	private void initCopyMenuItem(final JFrame frame, JMenu editMenu) {
		copyMenuItem = new JMenuItem("Copy");
		copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK));
		editMenu.add(copyMenuItem);
	}

	private void initPasteMenuItem(final JFrame frame, JMenu editMenu) {
		pasteMenuItem = new JMenuItem("Paste");
		pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_DOWN_MASK));
		editMenu.add(pasteMenuItem);
	}

	private void initSelectAllMenuItem(final JFrame frame, JMenu editMenu) {
		selectAllMenuItem = new JMenuItem("Select All");
		selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.META_DOWN_MASK));
		editMenu.add(selectAllMenuItem);
	}

	private void initQuiteMenuItem(final JFrame frame, JMenu fileMenu) {
		quitMenuItem = new JMenuItem("Quit");		
		if(System.getProperty("os.name").startsWith("Mac OS X")){
			quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.META_DOWN_MASK));
		}else{
			quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		}
		fileMenu.add(quitMenuItem);
	}
	
}