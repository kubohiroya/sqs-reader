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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableUtil{
	public static final Color evenColor = new Color(240,240,255);
	
	public static Component decorateEvenOddTableRowBgColor(Component renderer,
			JTable table, int row, int column){
	if(table.isRowSelected(row)){
		renderer.setForeground(table.getSelectionForeground());
		renderer.setBackground(table.getSelectionBackground());
	}else{
		renderer.setForeground(table.getForeground());
		renderer.setBackground((row%2==0)?evenColor:table.getBackground());
	}
	return renderer;
}
	
	public static JTableHeader createToolTipEnabledTableHeader(TableColumnModel tableColumnModel, final String[] messages){
		return new JTableHeader(tableColumnModel){
			private static final long serialVersionUID = 1L;
			@Override
			public String getToolTipText(MouseEvent e){
				int column = columnAtPoint(e.getPoint());
				return messages[column];
			}
		};
	}
	
	public static TableColumnModel createTableColumnModel(String[] columnNames){
		TableColumnModel columnModel = new DefaultTableColumnModel();
		for(int i=0; i < columnNames.length; i++){
			TableColumn c = new TableColumn(i);
			c.setHeaderValue(columnNames[i]);
			columnModel.addColumn(c);
		}
		return columnModel;
	}

}