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

import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class TableColumnsUtil {
	public static void adjustTableHeaderColumnRatio(JTable table, int[] list){
		int total = table.getSize().width;
		double ratio = 1.0 * total / getRatioTotal(list);
		int numColumns = table.getColumnModel().getColumnCount();
		for(int i = 0; i < numColumns - 1; i++){
			TableColumn col = table.getColumnModel().getColumn(i);
			int colWidth =(int)( list[i] * ratio);
			col.setPreferredWidth(colWidth);
			total -= colWidth;
		}
		table.getColumnModel().getColumn(numColumns - 1).setPreferredWidth(total);
		table.revalidate();
	}
	
	public static void adjustTableColumnRatio(JTable table, int[] list){
		int numColumns = table.getColumnModel().getColumnCount();
		int total = table.getSize().width;
		double ratio = 1.0 * total / getRatioTotal(list);
		for(int i = 0; i < numColumns - 1; i++){
			TableColumn col = table.getColumnModel().getColumn(i);
			int colWidth =(int)( list[i] * ratio);
			col.setMaxWidth(colWidth);
			total -= colWidth;
		}
		table.getColumnModel().getColumn(numColumns - 1).setPreferredWidth(total);
		table.revalidate();
	}
	
	private static int getRatioTotal(int[] list){
		int total = 0;
		for(int a : list){
			total += a;
		}
		return total;
	}
}
