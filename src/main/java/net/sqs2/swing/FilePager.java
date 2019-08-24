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

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;

import net.sqs2.util.StringUtil;

public class FilePager {

	JFrame frame;
	JScrollBar scrollBar;
	JTextArea textarea;

	int maxRows = 100;

	public static void main(String[] args) {
		new FilePager();
	}

	class FileContent {
		FileContent(File file) {

		}
	}

	FileContent fileContent;

	FilePager() {
		this.textarea = new JTextArea();

		// JViewport viewport = new JViewport();
		// viewport.add(textarea);

		JScrollPane scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.scrollBar = scrollPane.getVerticalScrollBar(); // 縦のスクロールバーを取得
		scrollPane.getViewport().add(this.textarea);

		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setLayout(new BorderLayout());
		this.frame.add(scrollPane, BorderLayout.CENTER);
		this.frame.setSize(400, 300);
		this.frame.setVisible(true);
	}

	int getLineEndOffset(int offset) {
		return this.textarea.getText().indexOf("\n", offset);
	}

	int getLineCount() {
		return StringUtil.count(this.textarea.getText(), '\n');
	}

	void append(String line) {
		if (getLineCount() > this.maxRows) {
			try {
				int len = getLineEndOffset(0);
				this.textarea.getDocument().remove(0, len);
			} catch (BadLocationException exp) {
			}
		}

		try {
			this.textarea.setCaretPosition(this.textarea.getText().length());
			this.textarea.append(line + "\n");
		} catch (IllegalArgumentException exp) {

		}

		this.scrollBar.setValue(this.scrollBar.getMaximum());
	}

	public void setMaxRows(int rows) {
		this.maxRows = rows;
	}

	public int getMaxRows() {
		return this.maxRows;
	}

}
