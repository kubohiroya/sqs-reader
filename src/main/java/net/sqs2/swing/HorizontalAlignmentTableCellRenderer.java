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
package net.sqs2.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class HorizontalAlignmentTableCellRenderer implements TableCellRenderer {
	private final TableCellRenderer renderer;
	int alignment;

	public HorizontalAlignmentTableCellRenderer(TableCellRenderer renderer, int alignment) {
		this.renderer = renderer;
		this.alignment = alignment;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = this.renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
				column);
		if (c instanceof JLabel) {
			initLabel((JLabel) c, row, value.toString());
		}
		return c;
	}

	private void initLabel(JLabel l, int row, String value) {
		l.setHorizontalAlignment(this.alignment);
		l.setToolTipText(value);
	}
}
