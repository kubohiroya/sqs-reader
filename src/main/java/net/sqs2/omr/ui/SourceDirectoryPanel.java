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
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.ui.swing.TableUtil;

class SourceDirectoryPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public static final String[] COMPONENT_NAME = { "Page", "PageSet", "Info" };

	public static final String PROCESS_SUCCEED_MESSAGE = "OK";// Process Succeed
	public static final String PROCESS_WAIT_MESSAGE = "";
	public static final int RESULT_MESSAGE_COLUMN_INDEX = 5;

	JTree tree;

	SessionToolPanel toolBar;
	JTabbedPane tabbedPane;

	JTable pageIDTable;
	JTable resultTable;
	NodeInfoPanel infoPanel;

	JScrollPane pageIDTableScrollPane;
	TableRowIndexingPanel pageIDRowIndexingPanel;

	JScrollPane resultTableScrollPane;
	TableRowIndexingPanel resultRowIndexingPanel;

	JSplitPane splitPane;

	TableColumnModel pageIDTableColumnModel = TableUtil.createTableColumnModel(PageIDTableModel.COLUMN_NAMES);
	JTableHeader pageIDTableHeader = TableUtil.createToolTipEnabledTableHeader(pageIDTableColumnModel, PageIDTableModel.COLUMN_TOOLTIP_TEXT);
	TableColumnModel resultTableColumnModel = TableUtil.createTableColumnModel(ResultTableModel.COLUMN_NAMES);
	JTableHeader resultTableHeader = TableUtil.createToolTipEnabledTableHeader(	resultTableColumnModel, ResultTableModel.COLUMN_TOOLTIP_TEXT);

	SourceDirectoryPanel(final SessionModel model) {
		this.tree = new JTree();
		this.tree.setRootVisible(true);

		DefaultTreeCellRenderer renderer = new SourceDirectoryTreeCellRenderer(model);

		this.tree.setCellRenderer(renderer);
		this.tree.setModel(model.sourceTreeModel.treeModel);
		this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		JScrollPane treeScrollPane = new JScrollPane(this.tree);

		for (int rowIndex = 1; rowIndex < this.tree.getRowCount(); rowIndex++) {
			TreePath p = this.tree.getPathForRow(rowIndex);
			Object o = ((DefaultMutableTreeNode) p.getLastPathComponent()).getUserObject();
			if (o instanceof SourceDirectory) {
				this.tree.setSelectionRow(rowIndex++);
				break;
			}
		}

		this.tree.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				TreePath mouseOverPath = tree.getClosestPathForLocation(e.getX(), e.getY());
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) mouseOverPath.getLastPathComponent();
				if (node.getUserObject() instanceof SourceDirectory) {
					String title = SourceDirectoryTreeNodeTitleFactory.getTitle(node, model.sourceTreeModel, (SourceDirectory) node.getUserObject());
					tree.setToolTipText(title);
				}
			}

		});

		this.pageIDTable = new JTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component prepareRenderer(TableCellRenderer renderer,
					int row, int column) {
				try{
					Component c = super.prepareRenderer(renderer, row, column);
					return TableUtil.decorateEvenOddTableRowBgColor(c, this, row,
						column);
				}catch(Exception ignore){
					return null;
				}
			}
		};

		this.pageIDTable.setRowSelectionAllowed(true);
		this.pageIDTable.setAutoCreateRowSorter(true);
		this.pageIDTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.pageIDTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.pageIDTable.setModel(model.getPageIDTableModel());
		this.pageIDTable.setColumnModel(this.pageIDTableColumnModel);
		this.pageIDTable.setTableHeader(this.pageIDTableHeader);
		this.pageIDTable.setDragEnabled(true);

		DragGestureListener dgl = new DragGestureListener() {
			@Override
			public void dragGestureRecognized(DragGestureEvent dge) {
				int selectedRow = pageIDTable.getSelectedRow();
				if (selectedRow == -1) {
					return;
				}
				if (dge.getTriggerEvent().isConsumed()) {
					return;
				}
				String root = model.getMarkReaderSession()
						.getSourceDirectoryRootFile().getAbsolutePath();
				String path = (String) pageIDTable.getValueAt(selectedRow, 0);
				String file = (String) pageIDTable.getValueAt(selectedRow, 1);
				final File tmpfile = new File(new File(root, path), file);

				Transferable tran = new Transferable() {
					public Object getTransferData(DataFlavor flavor)
							throws UnsupportedFlavorException {
						if (DataFlavor.javaFileListFlavor.equals(flavor)) {
							ArrayList<File> al = new ArrayList<File>(1);
							al.add(tmpfile);
							return al;
						}
						throw new UnsupportedFlavorException(flavor);
					}

					public DataFlavor[] getTransferDataFlavors() {
						return new DataFlavor[] { DataFlavor.javaFileListFlavor };
					}

					public boolean isDataFlavorSupported(DataFlavor flavor) {
						return DataFlavor.javaFileListFlavor.equals(flavor);
					}
				};

				try {
					dge.startDrag(DragSource.DefaultLinkDrop, tran, null);
				} catch (InvalidDnDOperationException ignore) {
				}
			}
		};
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this.pageIDTable, DnDConstants.ACTION_LINK, dgl);

		model.getPageIDTableModel().fireTableDataChanged();

		this.pageIDTableScrollPane = new JScrollPane(this.pageIDTable);

		this.resultTable = new JTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				return TableUtil.decorateEvenOddTableRowBgColor(c, this, row, column);
			}
		};

		this.resultTable.setRowSelectionAllowed(true);
		this.resultTable.setAutoCreateRowSorter(true);
		this.resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.resultTable.setColumnModel(resultTableColumnModel);
		this.resultTable.setTableHeader(resultTableHeader);
		this.resultTable.setModel(model.getResultTableModel());

		this.resultTableScrollPane = new JScrollPane(resultTable);

		this.pageIDRowIndexingPanel = new TableRowIndexingPanel(pageIDTable, 15, 17, 2);
		this.resultRowIndexingPanel = new TableRowIndexingPanel(resultTable, 15, 17, 2);

		JPanel pageIDPanel = new JPanel();
		pageIDPanel.setLayout(new BorderLayout());
		pageIDPanel.add(this.pageIDTableScrollPane, BorderLayout.CENTER);
		pageIDPanel.add(this.pageIDRowIndexingPanel, BorderLayout.EAST);

		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout());
		resultPanel.add(resultTableScrollPane, BorderLayout.CENTER);
		resultPanel.add(resultRowIndexingPanel, BorderLayout.EAST);

		this.infoPanel = new NodeInfoPanel(model);

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.add(COMPONENT_NAME[0], pageIDPanel);
		this.tabbedPane.add(COMPONENT_NAME[1], resultPanel);
		this.tabbedPane.add(COMPONENT_NAME[2], this.infoPanel);

		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,	treeScrollPane, this.tabbedPane);
		this.splitPane.setResizeWeight(0.5);

		this.toolBar = new SessionToolPanel(model.getMarkReaderSession());

		setLayout(new BorderLayout());
		add(this.toolBar, BorderLayout.NORTH);
		add(this.splitPane, BorderLayout.CENTER);
		setBorder(new EmptyBorder(3, 3, 3, 3));
	}

	public void modifySplitPosition() {
		int tableComponentWidth = this.tabbedPane.getSelectedComponent().getWidth();
		int treeComponentWidth = this.tree.getWidth();
		double value = 1.0 * treeComponentWidth / (tableComponentWidth + treeComponentWidth);
		this.splitPane.setResizeWeight(value);
	}

	public void expandAll() {
		for (int rowIndex = 0; rowIndex < this.tree.getRowCount(); rowIndex++) {
			this.tree.expandRow(rowIndex);
		}
		selectFirstEditableNode();
	}

	public void selectFirstEditableNode() {
		for (int rowIndex = 0; rowIndex < this.tree.getRowCount(); rowIndex++) {
			TreePath path = this.tree.getPathForRow(rowIndex);
			DefaultMutableTreeNode node = ((DefaultMutableTreeNode) path.getLastPathComponent());
			if (node.isLeaf()) {
				Object obj = node.getUserObject();
				if (obj instanceof SourceDirectory) {
					this.tree.setSelectionPath(path);
					break;
				}
			}
		}
	}

}