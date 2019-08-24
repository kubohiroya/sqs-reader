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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import net.sqs2.omr.model.PageID;

public class UndoableOMRImage extends OMRImage {
	
	UndoManager um;
	UndoableEditSupport us;

	public UndoableOMRImage(PageID pageID, BufferedImage image, long timeStamp) {
		super(pageID, image, timeStamp);
	}
	
	protected void initUndoManager(){
		um = new UndoManager();
		us = new UndoableEditSupport();
		us.addUndoableEditListener(um);
	}

	public class UndoAction extends AbstractAction{
		private static final long serialVersionUID = 1L;
		protected UndoManager um;
		public UndoAction(UndoManager um){
			super("Undo");
			putValue(MNEMONIC_KEY, 'U');
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
			this.um = um;
		}
		
		@Override
		public boolean isEnabled(){
			return um.canUndo();
		}

		@Override
		public void actionPerformed(ActionEvent ev){
			if(um.canUndo()){
				um.undo();
			}
		}
	} 

	public class RedoAction extends AbstractAction{
		private static final long serialVersionUID = 1L;
		protected UndoManager um;
		public RedoAction(UndoManager um){
			super("Redo");
			putValue(MNEMONIC_KEY, 'R');
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
			this.um = um;
		}
		
		@Override
		public boolean isEnabled(){
			return um.canRedo();
		}

		@Override
		public void actionPerformed(ActionEvent ev){
			if(um.canRedo()){
				um.redo();
			}
		}
	}

	
	//FIXME! this is sample impl
	protected class CutEdit extends AbstractUndoableEdit{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected Object oldValue;
		protected Object newValue;
		protected int row, column;

		protected CutEdit(){
			
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
		//	UndoableOMRImage.super.setValueAt(oldValue, row, column);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			//	UndoableOMRImage.super.setValueAt(newValue, row, column);
		}

		@Override
		public void die() {
			super.die();
			oldValue = null;
			newValue = null;
		}
	}
}
