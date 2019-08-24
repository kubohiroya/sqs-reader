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

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sqs2.omr.base.Messages;

public class NoPDFSourceFolderWarningPrompter {

	JPanel parent;
	boolean returnValue = false;

	public NoPDFSourceFolderWarningPrompter(JPanel parent) {
		this.parent = parent;
	}

	public synchronized boolean prompt(final File sourceDirectoryRoot) {
		if (this.parent == null) {
			throw new RuntimeException("parent is null");
		}
		this.returnValue = JOptionPane.showConfirmDialog(this.parent,
				Messages.SESSION_ERROR_NOPDFSOURCEFOLDER + "\n" + sourceDirectoryRoot.getAbsolutePath(),
				"Warning", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
		return this.returnValue;
	}
}