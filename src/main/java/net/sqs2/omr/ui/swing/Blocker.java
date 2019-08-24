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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

public class Blocker extends JComponent implements MouseInputListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Cursor prevCursor;
	private KeyEventDispatcher dispatcher;

	public Blocker() {
		super();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.dispatcher = new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent ke) {
				return true;
			}
		};
	}

	public void block() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this.dispatcher);
		this.prevCursor = this.getCursor();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.setVisible(true);
	}

	public void unBlock() {
		this.setCursor(this.prevCursor);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this.dispatcher);
		this.setVisible(false);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.white);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,	0.7f));
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		e.consume();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		e.consume();
	}
}
