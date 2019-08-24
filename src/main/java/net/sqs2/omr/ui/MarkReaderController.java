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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.session.service.ErrorPageImageFilesZipArchiver;
import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.omr.session.service.MarkReaderSessionManager;
import net.sqs2.omr.ui.swing.SamplingImageCanvas;
import net.sqs2.omr.ui.util.Observer;
import net.sqs2.swing.FileDropTargetDecorator;
import net.sqs2.util.FileUtil;

class MarkReaderController {
	
	static final Border DEFAULT_BORDER = new EmptyBorder(2,2,2,2);
	static final Border DROP_TARGET_HIGHLIGHT_BORDER = BorderFactory.createLineBorder(Color.green, 2);
	
	final MarkReaderPanel view;
	MarkReaderModel markReaderModel;

	MarkReaderController(MarkReaderPanel view) {
		this.view = view;
	}

	public void bind(final MarkReaderModel markReaderModel) {
		this.markReaderModel = markReaderModel;

		bindTitleUpdater();
		bindFileDropActionListener();		
		bindFileMenuListener();
		bindEditMenuListener();

		bindMenuStateListener();
	}

	private void bindFileMenuListener() {
		bindOpenMenuActionListener();
		bindSaveMenuActionListener();
		bindExportMenuActionListener();
		bindClearMenuActionListener();
		bindCloseMenuActionListener();
		bindQuitMenuActionListener();
	}


	private void bindTitleUpdater() {
		
		this.view.tabbedPane.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(final ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
					int selectedIndex = tabbedPane.getSelectedIndex();
					String title = markReaderModel.getApplicationName()+" "+markReaderModel.getVersion() + ((0 <= selectedIndex)? (": "+tabbedPane.getTitleAt(selectedIndex)): "");
					view.getFrame().setTitle(title);
				}});
			}});
	}

	private void bindOpenMenuActionListener() {
		this.view.openMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				SwingUtilities.invokeLater(new Runnable() {public void run() {
						JFileChooser jfc = new JFileChooser();
						jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						jfc.setMultiSelectionEnabled(true);
						jfc.setFileFilter(new FileFilter() {
							@Override
							public boolean accept(File f) {
								return f.isDirectory() && f.canRead();
							}

							@Override
							public String getDescription() {
								return "SourceFolder";
							}
						});

						int result = jfc.showOpenDialog(view.getFrame());
						if (result == JFileChooser.APPROVE_OPTION) {
							final File sourceDirectoryRoot = jfc.getSelectedFile();
							try{
								openDirectory(sourceDirectoryRoot);
							}catch(IOException ignore){
								ignore.printStackTrace();
								JOptionPane.showMessageDialog(view.getFrame(),"ERROR on SourceDirectory:" + ignore.getMessage());
							}

						}
				}});
			}
		});
	}

	private void bindSaveMenuActionListener() {
		this.view.saveMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					SessionPanel p = ((SessionPanel) view.tabbedPane.getSelectedComponent());
					MarkReaderSession session = p.sessionModel.getMarkReaderSession();
					if (session != null) {
							p.contentPanel.pageContentPanel.getActionMap().get("save").actionPerformed(new ActionEvent(this, 0, "save"));
					}
				}});
			}
		});
	}


	private void bindExportMenuActionListener() {
		this.view.exportMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				SwingUtilities.invokeLater(new Runnable() {public void run() {
						JFileChooser jfc = new JFileChooser();
						jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						jfc.setMultiSelectionEnabled(false);
						jfc.setFileFilter(new FileFilter() {
							@Override
							public boolean accept(File f) {
								return f.getName().toLowerCase().endsWith(".zip");
							}

							@Override
							public String getDescription() {
								return "zip file";
							}
						});

						int result = jfc.showSaveDialog(view.getFrame());
						if (result == JFileChooser.APPROVE_OPTION) {
							File zipFile = jfc.getSelectedFile();
							try{
								SessionPanel sessionView = ((SessionPanel) view.tabbedPane.getSelectedComponent());
								MarkReaderSession session = sessionView.sessionModel.getMarkReaderSession();
								if (session != null) {
									zipFile = new ErrorPageImageFilesZipArchiver(session.getSessionSource()).exportZipFileTo(zipFile);
									JOptionPane.showMessageDialog(view.getFrame(),"Export ErrorFiles in Zip file:" + zipFile.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
								}
							}catch(IOException ignore){
								ignore.printStackTrace();
								JOptionPane.showMessageDialog(view.getFrame(),"ERROR:" + ignore.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
							}
						}else{
						}
				}});
			}
		});
	}

	private void bindClearMenuActionListener() {
		this.view.clearMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("actionPerformed:clearMenuItem");
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					closeSession("Confirm Clear SourceDirectory:", true);
				}});
			}
		});
	}
	
	private void bindCloseMenuActionListener() {
		this.view.closeMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					closeSession("Confirm Close SourceDirectory:", false);
				}});
			}
		});
	}

	private void closeSession(String message, boolean clearCache){
		SessionPanel p = ((SessionPanel) view.tabbedPane.getSelectedComponent());
		MarkReaderSession session = p.sessionModel.getMarkReaderSession();
		if (session != null) {
			File root = session.getSourceDirectoryRootFile();
			int result = JOptionPane.showConfirmDialog(view.getFrame(),message + root);
			if(result == JOptionPane.YES_OPTION){
				if(session.isRunning()){
					session.stopSession();
				}
				session.closeSessionSource(clearCache);
				markReaderModel.sessionModelList.remove(p.sessionModel);
				view.tabbedPane.remove(p);
			}
		}
	}

	private void bindQuitMenuActionListener() {
		this.view.quitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
						view.getFrame().dispose();
				}});
				
				List<Long> sessionIDList = new ArrayList<Long>(SessionSourceManager.getSessionIDList().size());
				sessionIDList.addAll(SessionSourceManager.getSessionIDList());
				for( long sessionID: sessionIDList) {
					MarkReaderSession session = MarkReaderSessionManager.get(sessionID);
					if (session != null) {
						if(session.isRunning()){
							return;
						}
						session.closeSessionSource(false);
					}
				}
				markReaderModel.app.shutdown();
				System.exit(0);
			}
		});
	}

	private void bindFileDropActionListener() {
		new FileDropTargetDecorator(this.view.getFrame().getContentPane()) {
			@Override
			public void dragEnter(final DropTargetDragEvent e) {

				if((e.getDropTargetContext().getComponent()).getClass().getName().endsWith("JTable")){
					view.getContent().setBorder(DEFAULT_BORDER);
					e.rejectDrag();
					return;
				}
				
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					view.getContent().setBorder(DROP_TARGET_HIGHLIGHT_BORDER);
			}});
			}

			@Override
			public void dragExit(final DropTargetEvent e) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					view.getContent().setBorder(DEFAULT_BORDER);
				}});
			}
			
			@Override
			public void dropActionChanged(DropTargetDragEvent e) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					view.getContent().setBorder(DEFAULT_BORDER);
				}});
			}

			@Override
			public void drop(DropTargetDropEvent ev) {
				view.getContent().setBorder(DEFAULT_BORDER);
				super.drop(ev);
			}
			
			@Override
			public void drop(final File[] files) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
						for (File sourceDirectoryRoot : files) {
							try{
								if(sourceDirectoryRoot.isDirectory()){
									openDirectory(sourceDirectoryRoot);
								}
							}catch(IOException ignore){
								ignore.printStackTrace();
								JOptionPane.showMessageDialog(view.getFrame(),"ERROR on SourceDirectory:" + ignore.getMessage());
							}finally{
								view.getContent().setBorder(DEFAULT_BORDER);
							}
						}
				}});
			}
		};
		
		view.getContent().addMouseListener(new MouseAdapter(){
			@Override
			public void mouseExited(MouseEvent e) {
				view.getContent().setBorder(DEFAULT_BORDER);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				view.getContent().setBorder(DEFAULT_BORDER);
			}
		});
	}

	private void bindEditMenuListener() {
		this.view.selectAllMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SamplingImageCanvas canvas = ((SessionPanel) view.tabbedPane.getSelectedComponent()).contentPanel.pageContentPanel.previewCanvas;
				ActionMap actionMap = canvas.getActionMap();
				Action action = actionMap.get("selectAll");
				action.actionPerformed(new ActionEvent(canvas, e.getID(), "selectAll"));
				/*
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						SessionPanel sessionView = ((SessionPanel) view.tabbedPane.getSelectedComponent());
						final PageContentModel contentModel = sessionView.getSessionModel().getPageContentModel();
						contentModel.selectAll();
					}
				});
				*/
			}
		});
		
		this.view.copyMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SamplingImageCanvas canvas = ((SessionPanel) view.tabbedPane.getSelectedComponent()).contentPanel.pageContentPanel.previewCanvas;
				ActionMap actionMap = canvas.getActionMap();
				Action action = actionMap.get("copy");
				action.actionPerformed(new ActionEvent(canvas, e.getID(), "copy"));
				/*
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					SessionPanel sessionView = ((SessionPanel) view.tabbedPane.getSelectedComponent());
					PageContentModel contentModel = sessionView.getSessionModel().getPageContentModel();
					contentModel.copy();
				}});
				*/
			}
		});

		this.view.cutMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SamplingImageCanvas canvas = ((SessionPanel) view.tabbedPane.getSelectedComponent()).contentPanel.pageContentPanel.previewCanvas;
				ActionMap actionMap = canvas.getActionMap();
				Action action = actionMap.get("cut");
				action.actionPerformed(new ActionEvent(canvas, e.getID(), "cut"));
				/*
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					SessionPanel sessionView = ((SessionPanel) view.tabbedPane.getSelectedComponent());
					PageContentModel contentModel = sessionView.getSessionModel().getPageContentModel();
					contentModel.cut();
				}});*/
			}
		});

		this.view.pasteMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SamplingImageCanvas canvas = ((SessionPanel) view.tabbedPane.getSelectedComponent()).contentPanel.pageContentPanel.previewCanvas;
				ActionMap actionMap = canvas.getActionMap();
				Action action = actionMap.get("cut");
				action.actionPerformed(new ActionEvent(canvas, e.getID(), "cut"));
				/*
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					SessionPanel sessionView = ((SessionPanel) view.tabbedPane.getSelectedComponent());
					PageContentModel contentModel = sessionView.getSessionModel().getPageContentModel();
					contentModel.paste();
					sessionView.getContentPanel().pageContentPanel.previewCanvas.repaint();
				}});
				*/
			}
		});
	}
	
	private void bindMenuStateListener(){
		this.markReaderModel.getMenuState().bind(new Observer<MarkReaderMenuState>(){

			@Override
			public void update(MarkReaderMenuState m) {
				view.saveMenuItem.setEnabled(m.isSaveButtonEnabled());
				view.cutMenuItem.setEnabled(m.isCutCopyButtonEnabled());
				view.copyMenuItem.setEnabled(m.isCutCopyButtonEnabled());
				view.pasteMenuItem.setEnabled(m.isPasteButtonEnabled());
			}
			
		});
	}

	// ******************************************************************************

	private SessionPanel createSessionViewController(SessionModel sessionModel) {
		final SessionPanel sessionView = new SessionPanel(sessionModel);
		final SessionController sessionController = new SessionController(sessionView);
		sessionController.bind(sessionModel);
		return sessionView;
	}

	public synchronized void openDirectory(final File sourceDirectoryRoot) throws IOException{

		final SessionModel preDefinedSessionModel = this.markReaderModel.getPredefinedSessionModel(sourceDirectoryRoot);

		if (preDefinedSessionModel == null) {
			boolean enableSearchPageMasterFromAncestorDirectory =this.markReaderModel.getMenuState().getObject().isSearchPageMasterFromAncesterDirectoryIsEnabled(); 
			final SessionModel newSessionModel = this.markReaderModel.createSessionModel(sourceDirectoryRoot, enableSearchPageMasterFromAncestorDirectory); 

			
			SessionPanel sessionView = createSessionViewController(newSessionModel);
			String path = sourceDirectoryRoot.getAbsolutePath();
			final String title = FileUtil.getName(path);
			final String tip = path;
			final Icon icon = null;
			
			sessionView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			view.tabbedPane.addTab(title, icon, sessionView, tip);
			view.tabbedPane.setSelectedComponent(sessionView);
			newSessionModel.startSession();

		} else {

			SessionPanel sessionView = getSessionView(sourceDirectoryRoot);
			if(sessionView == null){
				return;
			}
			view.tabbedPane.setSelectedComponent(sessionView);
			SourceDirectoryPanel sourceDirectorySelectorPanel = sessionView.sourceDirectoryPanel;
			sourceDirectorySelectorPanel.tree.clearSelection();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)sourceDirectorySelectorPanel.tree.getModel().getRoot();
			node.removeAllChildren();
			sourceDirectorySelectorPanel.pageIDTable.clearSelection();
			sourceDirectorySelectorPanel.resultTable.clearSelection();
			
			preDefinedSessionModel.startSession();
		}
	}
	
	private SessionPanel getSessionView(File sourceDirectoryRoot){
		for(Component c : view.tabbedPane.getComponents()){
			SessionPanel sessionView = (SessionPanel)c;
			File file = sessionView.sessionModel.getMarkReaderSession().getSourceDirectoryRootFile();
			if(file.equals(sourceDirectoryRoot)){
				return sessionView;
			}
		}
		return null;
	}

}