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

import static net.sqs2.omr.ui.swing.TableColumnsUtil.adjustTableHeaderColumnRatio;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sqs2.event.EventListener;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.OMRProcessorResult;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourcePhase;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.result.export.SpreadSheetExportUtil;
import net.sqs2.omr.result.export.spreadsheet.ExcelExportModule;
import net.sqs2.omr.session.commit.PageTaskCommittedEvent;
import net.sqs2.omr.session.init.ImageFilesFoundEvent;
import net.sqs2.omr.session.init.InvalidFormMasterFoundErrorEvent;
import net.sqs2.omr.session.init.InvalidNumImagesErrorEvent;
import net.sqs2.omr.session.init.NoFormMasterFoundErrorEvent;
import net.sqs2.omr.session.init.SessionSourceInitDirectoryEvent;
import net.sqs2.omr.session.init.SessionSourceInitErrorEvent;
import net.sqs2.omr.session.init.SessionSourceInitEvent;
import net.sqs2.omr.session.scan.PageTaskProducedEvent;
import net.sqs2.omr.session.scan.SessionSourceScanEvent;
import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.omr.session.service.SessionEvent;
import net.sqs2.omr.sound.SessionFanfare;
import net.sqs2.omr.ui.swing.ButtonGroupPanel;
import net.sqs2.omr.ui.swing.MenuToggleButton;
import net.sqs2.omr.ui.util.TreeValues;

class SessionController {
	
	SessionPanel sessionView;

	SessionController(SessionPanel sessionView) {
		this.sessionView = sessionView;
	}
	
	public SessionPanel getSessionVIew(){
		return sessionView;
	}

	public void bind(final SessionModel sessionModel) {
		
		bindToolBarButtonActionListener(sessionModel);

		bindSessionEventListener();
		bindSessionSourceInitEventListener();
		bindSessionSourceScanEventListener();
		bindPageTaskCommittedEventListener();
		
		// comment out : reserved for future implementation
		// bindTreeModelListener(sessionModel);
		
		bindTreeSelectionListener(sessionModel);

		bindLocatorPanelUpdator();
		
		ContentPanel contentView = sessionView.getContentPanel();
		PageContentModel contentModel = contentView.getPageContentModel();
		OMRImageModelPainter painter = new OMRImageModelPainter(contentModel); 

		new PageContentController(contentView, painter).bind();
		
		bindTableRowSelectionListener(painter);
		
//		bindResultEventListener();
		
		this.sessionView.sourceDirectoryPanel.tabbedPane.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(final ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable() {public void run() {
					JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
					int selectedIndex = tabbedPane.getSelectedIndex();
					if(selectedIndex < 2){
						ContentPanel contentView = sessionView.contentPanel;
						contentView.contentCardLayout.show(contentView, ContentPanel.CONTENT_NAMES[selectedIndex]);
					}
				}});
			}
		});
		
		this.sessionView.sourceDirectoryPanel.toolBar.exportControlPanel.openFolderButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				File root = sessionView.sessionModel.getMarkReaderSession().getSourceDirectoryRootFile();
				try{
					File resultDirectory = new File(root, AppConstants.RESULT_DIRNAME);
					Desktop.getDesktop().open(resultDirectory);
				}catch(IOException ignore){}
			}
		});
		
		this.sessionView.sourceDirectoryPanel.toolBar.exportControlPanel.spreadSheetBrowseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SessionModel sessionModel = sessionView.sessionModel;
				SessionSource sessionSource = sessionModel.getMarkReaderSession().getSessionSource();
				try{
					SourceDirectory sourceDirectory = sessionModel.getSelectedSourceDirectory();
					File spreadSheetFile = null;
					if(sourceDirectory == null){
						spreadSheetFile = SpreadSheetExportUtil.createSpreadSheetFile(sessionSource, ExcelExportModule.XLSX_SUFFIX);
					}else{
						FormMaster formMaster = sourceDirectory.getCurrentFormMaster();
						spreadSheetFile = SpreadSheetExportUtil.createSpreadSheetFile(formMaster, sourceDirectory, ExcelExportModule.XLSX_SUFFIX);
					}
					Desktop.getDesktop().open(spreadSheetFile);
				}catch(IOException ignore){}
			}
		});
	}

	private void bindToolBarButtonActionListener(final SessionModel sessionModel) {
		final ButtonGroupPanel toolBarButtonGroup = this.sessionView.sourceDirectoryPanel.toolBar.buttonGroup;
		for (MenuToggleButton button: toolBarButtonGroup.getButtons()) {
			button.addActionListener(new ActionListener() {
				@Override public void actionPerformed(final ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override public void run() {
							fireSessionAction(sessionModel, ((MenuToggleButton)e.getSource()).getText());
						}
					});
				}
			});
		}
		
		this.sessionView.sourceDirectoryPanel.toolBar.cleanStartPopUpMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				MarkReaderSession session = sessionModel.getMarkReaderSession();
				try {
					System.out.println("CleanStart");
					session.clear();
					session.closeSessionSource(true);
				} catch (IOException iex) {
					iex.printStackTrace();
				}
				fireSessionAction(sessionModel, "Start");
			}
		});
	}

	@SuppressWarnings("unused")
	private void bindTreeModelListener(final SessionModel sessionModel) {
		final JTree tree = this.sessionView.sourceDirectoryPanel.tree;
		tree.getModel().addTreeModelListener(new TreeModelListener(){

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				//System.out.println("changed "+e.getTreePath().toString());
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
			}

			@Override
			public void treeStructureChanged(final TreeModelEvent e) {
				SwingUtilities.invokeLater(new Runnable(){public void run(){
					tree.expandPath(e.getTreePath());
				}});
			}
		});
	}
	

	private void bindLocatorPanelUpdator() {

		this.sessionView.sessionModel.getPageIDTableModel().addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent e) {
				sessionView.sourceDirectoryPanel.pageIDRowIndexingPanel.repaint();
			}						
		});
		this.sessionView.sourceDirectoryPanel.pageIDTable.getTableHeader().addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent ev){
				sessionView.sourceDirectoryPanel.pageIDRowIndexingPanel.repaint();
			}
		});

		this.sessionView.sessionModel.getResultTableModel().addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent e) {
				sessionView.sourceDirectoryPanel.resultRowIndexingPanel.repaint();
			}
		});
		this.sessionView.sourceDirectoryPanel.resultTable.getTableHeader().addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent ev){
				sessionView.sourceDirectoryPanel.resultRowIndexingPanel.repaint();
			}
		});
	}

	private void bindTreeSelectionListener(final SessionModel sessionModel) {
		final JTree tree = this.sessionView.sourceDirectoryPanel.tree;
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						
						sessionView.sessionModel.pageContentModel.clear();
						sessionView.contentPanel.repaint();

						final TreePath selectedPath = e.getPath();
						final JTable pageIDTable = sessionView.sourceDirectoryPanel.pageIDTable;						
						final JTable resultTable = sessionView.sourceDirectoryPanel.resultTable;
						SourceDirectory sourceDirectory = getSelectedSourceDirectory(selectedPath);
						if(sourceDirectory == null){
							sessionModel.setSelectedSourceDirectory(sourceDirectory);
							pageIDTable.setModel(sessionView.sessionModel.defaultPageIDTableModel);
							resultTable.setModel(sessionView.sessionModel.defaultResultTableModel);
							sessionView.sessionModel.defaultPageIDTableModel.fireTableDataChanged();
							sessionView.sessionModel.defaultResultTableModel.fireTableDataChanged();
							sessionView.sourceDirectoryPanel.pageIDRowIndexingPanel.repaint();
							sessionView.sourceDirectoryPanel.resultRowIndexingPanel.repaint();
							File rootFile = sessionView.getSessionModel().getMarkReaderSession().getSourceDirectoryRootFile();
							String masterPath = new File(rootFile, selectedPath.getLastPathComponent().toString()).getAbsolutePath();
							sessionView.sourceDirectoryPanel.infoPanel.pathField.setText(masterPath);
							return;
						}
						sessionView.getSessionModel().setSelectedSourceDirectory(sourceDirectory);
						final PageIDTableModel pageIDTableModel = sessionModel.sourceDirectoryToPageIDTableModelMap.get(sourceDirectory);
						sessionView.getSessionModel().setPageIDTableModel(pageIDTableModel);
						final ResultTableModel resultTableModel = sessionModel.sourceDirectoryToResultTableModelMap.get(sourceDirectory); 
						sessionView.getSessionModel().setResultTableModel(resultTableModel);
						if(pageIDTableModel != null){
							pageIDTable.setModel(pageIDTableModel);
							pageIDTableModel.fireTableDataChanged();
							adjustTableHeaderColumnRatio(pageIDTable, PageIDTableModel.COLUMN_WIDTH_RATIO);
						}
						if(resultTableModel != null){
							resultTable.setModel(resultTableModel);
							resultTableModel.fireTableDataChanged();
							adjustTableHeaderColumnRatio(resultTable, ResultTableModel.COLUMN_WIDTH_RATIO);
						}
						
						sessionView.sourceDirectoryPanel.infoPanel.pathField.setText(sourceDirectory.getDirectory().getAbsolutePath());
						/*
						TableRowSorter<?> pageIDableRowSorter = new TableRowSorter<AbstractTableModel>(pageIDTableModel);
						pageIDableRowSorter.setMaxSortKeys(3);
						pageIDTable.setRowSorter(pageIDableRowSorter);

						TableRowSorter<?> resultRowSorter = new TableRowSorter<AbstractTableModel>(resultTableModel);
						resultRowSorter.setMaxSortKeys(3);
						pageIDTable.setRowSorter(resultRowSorter);
						 */
						sessionView.sourceDirectoryPanel.pageIDRowIndexingPanel.repaint();
						sessionView.sourceDirectoryPanel.resultRowIndexingPanel.repaint();
					}
				});
			}
		});	
	}
	
	int value = 0;
	int total = 0;
	
	private void bindSessionEventListener() {
		MarkReaderSession session = sessionView.getSessionModel().getMarkReaderSession();
		session.getSessionEventSource().addListener(new EventListener<SessionEvent>() {
					@Override
					public void eventFired(final SessionEvent event) {
						if(event.getPhase() == null || event.getPhase().getSessionRunningPhase() == SessionSourcePhase.Phase.notYet) {
							Thread.dumpStack();
							return;
						}else if(event.getPhase().getInitializingPhase() == SessionSourcePhase.Phase.doing) {
							SwingUtilities.invokeLater(new Runnable() {public void run() {
									//view.tabbedPane.setSelectedComponent(sessionView);
									ButtonGroupPanel bg = sessionView.sourceDirectoryPanel.toolBar.buttonGroup;
									bg.get("Start").setSelected(true);
									bg.get("Stop").setSelected(false);
									ExportControlPanel exportControlPanel = sessionView.sourceDirectoryPanel.toolBar.exportControlPanel;
									exportControlPanel.setEnabled(false);
									sessionView.sessionModel.getMarkReaderSession().getProgressRate().setObject(-1f);
									sessionView.sessionModel.getMarkReaderSession().getProgressRate().update();
							}});
							try{
								new SessionFanfare().startFanfare();
							}catch(Exception ignore){
							}
						}else if(event.getPhase().getInitializingPhase() == SessionSourcePhase.Phase.done) {
							
							sessionView.getSessionModel().initialize();
							
							SwingUtilities.invokeLater(new Runnable() {public void run() {
									sessionView.sourceDirectoryPanel.expandAll();
									sessionView.sourceDirectoryPanel.revalidate();
									sessionView.sessionModel.updateProgressRate();
							}});
						}
						if(event.getPhase().hasStopped()) {
							SwingUtilities.invokeLater(new Runnable() {public void run() {
								setStartableButtonState();
								sessionView.sessionModel.stopProgressBarUpdator();
								sessionView.sessionModel.getMarkReaderSession().getProgressRate().update();
							}});
						}
						switch(event.getPhase().getSessionRunningPhase()){
							case stop:
								SwingUtilities.invokeLater(new Runnable() {public void run() {
									setStartableButtonState();
									sessionView.sessionModel.stopProgressBarUpdator();
									sessionView.sessionModel.getMarkReaderSession().getProgressRate().setObject(0.0f);
									sessionView.sessionModel.getMarkReaderSession().getProgressRate().update();
								}});
								break;
							case done:
								SwingUtilities.invokeLater(new Runnable() {public void run() {
									setStartableButtonState();
									setExportableButtonState();
									sessionView.sessionModel.stopProgressBarUpdator();
									sessionView.sessionModel.getMarkReaderSession().getProgressRate().setObject(1.0f);
									sessionView.sessionModel.getMarkReaderSession().getProgressRate().update();
									ExportControlPanel p = sessionView.sourceDirectoryPanel.toolBar.exportControlPanel;
									p.cardLayout.show(p, ExportControlPanel.COMPONENT_NAMES[1]);
								}});
								try{
									new SessionFanfare().finishFanfare();
								}catch(Exception ignore){
								}
								break;
							case fail:
								SwingUtilities.invokeLater(new Runnable() {public void run() {
									setStartableButtonState();
									setExportableButtonState();
									sessionView.sessionModel.stopProgressBarUpdator();
									sessionView.sessionModel.getMarkReaderSession().getProgressRate().setObject(1.0f);
									sessionView.sessionModel.getMarkReaderSession().getProgressRate().update();
									ExportControlPanel p = sessionView.sourceDirectoryPanel.toolBar.exportControlPanel;
									p.cardLayout.show(p, ExportControlPanel.COMPONENT_NAMES[1]);
								}});
								try{
									new SessionFanfare().failFanfare();
								}catch(Exception ignore){
								}
								break;
								
						}
					}

					private void setStartableButtonState() {
						SwingUtilities.invokeLater(new Runnable() {public void run() {
								sessionView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								ButtonGroupPanel bg = sessionView.sourceDirectoryPanel.toolBar.buttonGroup;
								bg.get("Start").setSelected(false);
								bg.get("Stop").setSelected(true);
						}});
					}

					private void setExportableButtonState() {
						SwingUtilities.invokeLater(new Runnable() {public void run() {
							ExportControlPanel p = sessionView.sourceDirectoryPanel.toolBar.exportControlPanel;
							p.setEnabled(true);
						}});
					}
});
	}

	private void bindSessionSourceInitEventListener() {
		MarkReaderSession session = sessionView.getSessionModel().getMarkReaderSession();
		session.getSessionSourceInitializationEventSource().addListener(
				new EventListener<SessionSourceInitEvent>() {
					@Override
					public void eventFired(SessionSourceInitEvent ev) {
						if (ev instanceof SessionSourceInitDirectoryEvent) {
							if(ev instanceof ImageFilesFoundEvent){
								//TODO: notify found image
								//ImageFilesFoundEvent e = (ImageFilesFoundEvent)ev;
								//SourceDirectory sourceDirectory = e.getSourceDirectory();
								//int numAddedImages = e.getNumAddedImages();
							}else{ 
								SessionSourceInitDirectoryEvent e = (SessionSourceInitDirectoryEvent) ev;
								switch (e.getPhase()) {
								case SessionSourceInitDirectoryEvent.STARTED:
									break;
								case SessionSourceInitDirectoryEvent.DONE:
									break;
								}
							}
						}else if(ev instanceof SessionSourceInitErrorEvent){
							if(ev instanceof InvalidFormMasterFoundErrorEvent){
								JOptionPane.showConfirmDialog(sessionView, new Object[]{
										"Invalid form master found",
										((InvalidFormMasterFoundErrorEvent) ev).getFile().getAbsolutePath()
										}, "ERROR", JOptionPane.OK_OPTION);
							}else if(ev instanceof InvalidNumImagesErrorEvent){
								JOptionPane.showConfirmDialog(sessionView, new Object[]{
										"Invalid number of images found",
										ev.getSessionSource().getRootDirectory().getAbsolutePath(),
										((InvalidNumImagesErrorEvent) ev).getSourceDirectory().getDirectory().getPath(),
										((InvalidNumImagesErrorEvent) ev).getNumImages()
										}, "ERROR", JOptionPane.OK_OPTION);
							}else if(ev instanceof NoFormMasterFoundErrorEvent){
								JOptionPane.showConfirmDialog(sessionView, new Object[]{
										"No valid form master found",
										((NoFormMasterFoundErrorEvent) ev).getSessionSource().getRootDirectory().getAbsolutePath()
										}, "ERROR", JOptionPane.OK_OPTION);
							}else{
							}
						}
					}
				});
	}

	private void bindSessionSourceScanEventListener() {
		MarkReaderSession session = sessionView.getSessionModel().getMarkReaderSession();
		session.getSessionSourceScanEventSource().addListener(
				new EventListener<SessionSourceScanEvent>() {
					@Override
					public void eventFired(SessionSourceScanEvent ev) {
						SessionSource sessionSource = (SessionSource) ev.getSource();
						if (ev instanceof PageTaskProducedEvent) {

								PageTaskProducedEvent e = (PageTaskProducedEvent) ev;
								OMRPageTask pageTask = (OMRPageTask) e.getPageTask();

								SourceDirectory sourceDirectory = sessionSource.getSourceDirectory(pageTask.getPageID());

								final SessionModel sessionModel = sessionView.getSessionModel();
								TreeValues tasksTreeValues = sessionModel.sourceTreeModel.numTasks;
								final TreePath treePath = sessionModel.sourceTreeModel.getTreePath(sourceDirectory);
								if(treePath == null){
									return;
								}
								
								switch(e.getStatus()){
								case PageTaskProducedEvent.NEW_TASK:
								case PageTaskProducedEvent.ERROR_TASK_EXECUTION_REQUIRED:
									incrementValue(tasksTreeValues, treePath);
									break;
								case PageTaskProducedEvent.ERROR_TASK_RESERVED:
									TreeValues errorTreeValues = sessionModel.sourceTreeModel.numErrors;
									incrementValue(errorTreeValues, treePath);
									break;
								}
								
								SwingUtilities.invokeLater(new Runnable(){public void run(){
									TreePath path = treePath;
									do {
										Object nodeValue = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
										try{
											sessionModel.sourceTreeModel.treeModel.valueForPathChanged(path,nodeValue);
										}catch(Exception ignore){}
										path = path.getParentPath();
									} while (path != null);
								}});
						}else{
							
						}
					}
				});
	}

	private void bindPageTaskCommittedEventListener() {
		MarkReaderSession session = sessionView.getSessionModel().getMarkReaderSession();
		session.getPageTaskCommitedEventSource().addListener(new EventListener<PageTaskCommittedEvent>() {
					@Override
					public void eventFired(final PageTaskCommittedEvent ev) {
						SwingUtilities.invokeLater(new Runnable(){ public void run(){

								OMRPageTask pageTask = (OMRPageTask) ev.getPageTask();
								SessionModel sessionModel = sessionView.getSessionModel();
								SessionSource sessionSource = sessionModel.getMarkReaderSession().getSessionSource();
								SourceDirectory sourceDirectory = sessionSource.getSourceDirectory(pageTask.getPageID());
								FormMaster formMaster = sourceDirectory.getCurrentFormMaster();
								int numPages = formMaster.getNumPages();
								OMRProcessorErrorModel pageTaskErrorModel = pageTask.getErrorModel();
								OMRProcessorResult pageTaskResult = pageTask.getResult();

								TreePath treePath = sessionView.getSessionModel().sourceTreeModel.getTreePath(sourceDirectory);

								if (pageTaskErrorModel == null && pageTaskResult != null) {
									//Logger.getAnonymousLogger().info("PageTask OK:" + pageTask);
									TreeValues successTreeValues = sessionModel.sourceTreeModel.numSuccess;
									incrementValue(successTreeValues, treePath);
								} else {
									//Logger.getAnonymousLogger().info("PageTask ERROR:" + pageTask);
									TreeValues errorTreeValues = sessionModel.sourceTreeModel.numErrors;
									incrementValue(errorTreeValues, treePath);
								}
								sessionModel.sourceTreeModel.valueForPathChanged(treePath);

								JTable pageIDTable = sessionView.sourceDirectoryPanel.pageIDTable;
								PageIDTableModel pageIDTableModel = (PageIDTableModel)pageIDTable.getModel();
								ResultTableModel resultTableModel = (ResultTableModel)sessionView.sourceDirectoryPanel.resultTable.getModel();

								if(sourceDirectory == pageIDTableModel.getSourceDirectory() && sourceDirectory == resultTableModel.getSourceDirectory()){

										int rowIndex = sessionSource.getRowIndex(sourceDirectory.getRelativePath(), pageTask.getPageID());
										if(rowIndex == -1){
											throw new RuntimeException();
										}

										pageIDTableModel.	fireTableCellUpdated(rowIndex, SourceDirectoryPanel.RESULT_MESSAGE_COLUMN_INDEX);
										resultTableModel.fireTableCellUpdated(rowIndex / numPages, ResultTableModel.RESULT_MESSAGE_COLUMN_INDEX);
										sessionView.sourceDirectoryPanel.pageIDRowIndexingPanel.repaint();
										sessionView.sourceDirectoryPanel.resultRowIndexingPanel.repaint();

								}
								sessionView.sessionModel.updateProgressRate();
						}
						});
					}
				});
	}


	abstract class TableRowSelectAction implements ListSelectionListener{
		JTable pageIDTable;
		JTable resultTable;
		SessionModel sessionModel;
		OMRImageModelPainter painter;
		
		TableRowSelectAction(JTable pageIDTable, JTable resultTable, SessionModel sessionModel, OMRImageModelPainter painter){
				this.pageIDTable = pageIDTable;
				this.resultTable = resultTable;
				this.sessionModel = sessionModel;
				this.painter = painter;
		}
	}
	
	class PageIDTableRowSelectAction extends TableRowSelectAction{

		PageIDTableRowSelectAction(JTable pageIDTable, JTable resultTable, SessionModel sessionModel, OMRImageModelPainter painter){
			super(pageIDTable, resultTable, sessionModel, painter);
		}
		
		@Override
		public void valueChanged(final ListSelectionEvent ev) {
			if(ev.getValueIsAdjusting()){
				return;			
			}
			int selectedRowIndex = pageIDTable.getSelectedRow();
			if(selectedRowIndex == -1){
				return;
			}
			final long sessionID = sessionModel.getMarkReaderSession().getSessionID();
			final SourceDirectory sourceDirectory = sessionModel.getPageIDTableModel().getSourceDirectory();
			final int numPages = sourceDirectory.getCurrentFormMaster().getNumPages();
			final int pageIDTableViewRowIndex = selectedRowIndex;
			final int pageIDTableModelRowIndex = pageIDTable.convertRowIndexToModel(pageIDTableViewRowIndex);
			final int resultTableModelRowIndex = pageIDTableModelRowIndex / numPages;
			final int resultTableViewRowIndex = resultTable.convertRowIndexToView(resultTableModelRowIndex);
			final Rectangle rect = resultTable.getCellRect(resultTableViewRowIndex, 0, true);
			
			sessionView.contentPanel.pageContentPanel.toolPanel.saveButton.setEnabled(false);
			sessionModel.getMarkReaderModel().getMenuState().getObject().setSaveButtonEnabled(false);
			sessionModel.getMarkReaderModel().getMenuState().update();
			
			DefaultListSelectionModel listSelectionModel = (DefaultListSelectionModel)ev.getSource();
			if(listSelectionModel != pageIDTable.getSelectionModel()){
				return;
			}
			
			if(pageIDTableViewRowIndex == -1){
				sessionView.sessionModel.pageContentModel.getOMRImageModel().setEmptyPageContent();
				return;
			}

			resultTable.getSelectionModel().setValueIsAdjusting(true);
			resultTable.getSelectionModel().setSelectionInterval(resultTableViewRowIndex, resultTableViewRowIndex);
			resultTable.getSelectionModel().setValueIsAdjusting(false);
			resultTable.scrollRectToVisible(rect);
			
			Rectangle headerVisibleRect = sessionView.contentPanel.pageContentPanel.headerCanvas.getVisibleRect();
			Rectangle footerVisibleRect = sessionView.contentPanel.pageContentPanel.footerCanvas.getVisibleRect();
			Rectangle previewVisibleRect = sessionView.contentPanel.pageContentPanel.previewCanvas.getScrollPane().getVisibleRect();
			
			sessionView.contentPanel.pageContentPanel.previewCanvas.clearCache();
			sessionView.contentPanel.pageContentPanel.headerCanvas.clearCache();
			sessionView.contentPanel.pageContentPanel.footerCanvas.clearCache();
			
			sessionView.sessionModel.pageContentModel.initialize();
			
			PageContentModel pageContentModel = sessionModel.pageContentModel;
			pageContentModel.getOMRImageModel().updatePageContent(sessionID, sourceDirectory, pageIDTableModelRowIndex, headerVisibleRect, footerVisibleRect, previewVisibleRect);

		}
	}

	class ResultTableRowSelectAction extends TableRowSelectAction{
		
		ResultTableRowSelectAction( JTable pageIDTable, JTable resultTable, SessionModel sessionModel, OMRImageModelPainter painter){
			super( pageIDTable, resultTable, sessionModel, painter);
		}
		
		@Override
		public void valueChanged(final ListSelectionEvent ev) {
			if(ev.getValueIsAdjusting()){
				return;
			}
			int selectedRowIndex = resultTable.getSelectedRow();
			if(selectedRowIndex == -1){
				return;
			}
			final int resultTableViewRowIndex = selectedRowIndex;
			final long sessionID = sessionModel.getMarkReaderSession().getSessionID();
			final SourceDirectory sourceDirectory = sessionModel.getResultTableModel().getSourceDirectory();
			final int numPages = sourceDirectory.getCurrentFormMaster().getNumPages();
			final int resultTableModelRowIndex = resultTable.convertRowIndexToModel(resultTableViewRowIndex);
			final int pageIDTableModelRowIndex = resultTableModelRowIndex * numPages;
			final int pageIDTableViewRowIndex = pageIDTable.convertRowIndexToView(pageIDTableModelRowIndex);
			final Rectangle rect = pageIDTable.getCellRect(pageIDTableViewRowIndex, 0, true);

			if(sessionView.getSourceDirectoryPanel().tabbedPane.getSelectedIndex() != 1){
				return;
			}
			
			DefaultListSelectionModel listSelectionModel = (DefaultListSelectionModel)ev.getSource();
			if(listSelectionModel != resultTable.getSelectionModel()){
				return;
			}
			
			PageContentModel pageContentModel = sessionModel.pageContentModel;
			
			if(resultTableViewRowIndex == -1){
				pageContentModel.getOMRImageModel().setEmptyPageContent();
				return;
			}

			pageIDTable.getSelectionModel().setValueIsAdjusting(true);
			pageIDTable.getSelectionModel().setSelectionInterval(pageIDTableViewRowIndex, pageIDTableViewRowIndex);
			pageIDTable.getSelectionModel().setValueIsAdjusting(false);				
			pageIDTable.scrollRectToVisible(rect);

			pageContentModel.getOMRImageModel().updateRowContents(numPages, sessionID, sourceDirectory, resultTableViewRowIndex);

			RowContentModel rowContentModel = sessionModel.rowContentModel;
			PageID[] pageIDArray = new PageID[numPages];
			for(int i=0; i<numPages; i++){
				pageIDArray[i] = sourceDirectory.getPageID(resultTableModelRowIndex * numPages + i);
			}
			rowContentModel.setPageIDArray(pageIDArray);
		}
	}

	void bindTableRowSelectionListener(final OMRImageModelPainter painter) {
		final SessionModel sessionModel = this.sessionView.getSessionModel();
		JTable pageIDTable = this.sessionView.sourceDirectoryPanel.pageIDTable;
		JTable resultTable = this.sessionView.sourceDirectoryPanel.resultTable;
		pageIDTable.getSelectionModel().addListSelectionListener(new PageIDTableRowSelectAction(pageIDTable, resultTable, sessionModel, painter));
		resultTable.getSelectionModel().addListSelectionListener(new ResultTableRowSelectAction(pageIDTable, resultTable, sessionModel, painter));
	}
	
	private static SourceDirectory getSelectedSourceDirectory(TreePath path) {
		if(path == null){
			return null;
		}
		try {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			return (SourceDirectory) node.getUserObject();
		} catch (ClassCastException ex) {
			return null;
		}
	}
	
	private static void fireSessionAction(SessionModel sessionModel, String name) {
		if (name.equals("Start")) {
			sessionModel.getMarkReaderSession().startSession(true);
		} else if (name.equals("Stop")) {
			sessionModel.getMarkReaderSession().stopSession();
		}
	}
	
	private static void incrementValue(TreeValues treeValues, TreePath treePath) {
		if(treePath == null){
			return;
		}
		TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
		treeValues.incrementValue(treeNode);
	}
}