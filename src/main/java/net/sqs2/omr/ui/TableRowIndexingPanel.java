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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;

class TableRowIndexingPanel extends JPanel{
	
	private static final long serialVersionUID = 1L;
	
	JTable table;
	int topMargin;
	int bottomMargin;
	int focusedRowIndex;
	
	TableRowIndexingPanel(final JTable table, final int width, final int topMargin, final int bottomMargin){
		this.table = table;
		this.topMargin = topMargin;
		this.bottomMargin =  bottomMargin;
		this.focusedRowIndex = -1;

		this.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(table.getRowCount() == 0){
					return;
				}
				double unitHeight = getUnitHeight();
				int rowIndex = (int)((e.getY() - topMargin)/unitHeight);
				if(0 <= rowIndex && rowIndex < table.getRowCount()){
					table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
					Rectangle rect = table.getCellRect(rowIndex, 0, true);
					table.scrollRectToVisible(rect);
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				focusedRowIndex = -1;
				repaint();
			}

		});
		
		this.addMouseMotionListener(new MouseAdapter(){
			@Override
			public void mouseMoved(MouseEvent e) {
				if(table.getRowCount() == 0){
					return;
				}
				double unitHeight = getUnitHeight();
				focusedRowIndex = (int) ((e.getY() - topMargin)/unitHeight);
				if(focusedRowIndex < 0 || table.getRowCount() <= focusedRowIndex){
					focusedRowIndex = -1;
				}
				repaint();
			}

		});
	}
	
	@Override
	public void paintComponent(Graphics _g){
		Graphics2D g = (Graphics2D)_g;
		g.setColor(UIManager.getColor("Panel.background"));
		//g.fillRect(0, topMargin, getWidth(), getHeight() - topMargin - bottomMargin);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.lightGray);
		g.drawRect(0, topMargin, getWidth(), getHeight() - topMargin - bottomMargin);
		
		RenderingHints rh = g.getRenderingHints ();
		rh.put (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints (rh);
		
		if(table.getRowCount() == 0){
			return;
		}

		double unitHeight = getUnitHeight();
		
		for(int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++){

			final int modelRowIndex = table.convertRowIndexToModel(rowIndex);
			Rectangle2D rect = new Rectangle2D.Double(1, (topMargin + rowIndex * unitHeight), getWidth() - 2, (Math.max(1, unitHeight - 1)));

			Object value = table.getModel().getValueAt(modelRowIndex,SourceDirectoryPanel.RESULT_MESSAGE_COLUMN_INDEX);
			if(value != null){
				if(value.equals(SourceDirectoryPanel.PROCESS_SUCCEED_MESSAGE)){
					g.setColor(Colors.PROCESS_SUCCEED_COLOR);
				}else if(value.equals(SourceDirectoryPanel.PROCESS_WAIT_MESSAGE)){
					g.setColor(Colors.PROCESS_WAIT_COLOR);
				}else{
					g.setColor(Colors.PROCESS_ERROR_COLOR);
				}
				g.fill(rect);
			}else{
			}
			if(rowIndex == focusedRowIndex){														
				g.setColor(Color.white);
				g.draw(rect);
			}
		}
	}

	private double getUnitHeight() {
		int numRows = table.getRowCount();
		Rectangle tableRowVisibleRect = table.getCellRect(numRows - 1, 0, true);
		double height = Math.min(getHeight() - topMargin - bottomMargin, tableRowVisibleRect.getMaxY());
		return height / numRows;
	}
	
}