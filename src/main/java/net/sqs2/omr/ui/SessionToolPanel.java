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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.omr.ui.swing.ButtonGroupPanel;
import net.sqs2.omr.ui.swing.ImageIconUtil;
import net.sqs2.omr.ui.util.ObservableObject;
import net.sqs2.omr.ui.util.Observer;

class SessionToolPanel extends JPanel implements Observer<Float>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ButtonGroupPanel buttonGroup;
	JTextField pathField;

	ExportControlPanel exportControlPanel;
	JPopupMenu startPopupMenu;
	JMenuItem cleanStartPopUpMenuItem;
	
	SessionToolPanel(MarkReaderSession session){

		File file = session.getSourceDirectoryRootFile();
		final ObservableObject<Float> progressRate = session.getProgressRate();
		
		progressRate.bind(this);
		
		setLayout(new BorderLayout());

		pathField = new JTextField(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g;
				g2.setColor(new Color(0,200,0,30));
				float rate = progressRate.getObject().floatValue();
				Insets insets = getBorder().getBorderInsets(this);
				int w = getWidth() - insets.left - insets.right; // - getInsets().left - getInsets().right;// -  insets.left - insets.right;
				int h = getHeight() - insets.top- insets.bottom; // this.getInsets().top - this.getInsets().bottom;//
				if(0 <= rate){
					g2.fillRect(insets.left, insets.top, (int)(w * rate), h);
				}else{
					rate *= -1;
					g2.clipRect(insets.left, insets.top, w, h);
					g2.fillRect(insets.left + (int)(w * rate * 2), insets.top, w, h);
					g2.fillRect(insets.left + (int)(w * (rate-1.0) * 2), insets.top, w, h);
				}
			}
		};
		
		pathField.setEditable(false);
		pathField.setDragEnabled(true);
		pathField.setTransferHandler(null);
		pathField.setToolTipText(file.getAbsolutePath());
		pathField.setText(file.getAbsolutePath());
		pathField.setFocusable(true);

		cleanStartPopUpMenuItem = new JMenuItem("CleanStart");
		
		startPopupMenu = new JPopupMenu();
		startPopupMenu.add(cleanStartPopUpMenuItem);
		
		buttonGroup = new ButtonGroupPanel(
				new String[]{"Start", "Stop"},
				new Icon[]{
						ImageIconUtil.createImageIcon("class:icon/circleright32.png"),
						ImageIconUtil.createImageIcon("class:icon/stop32.png")
						},
						new boolean[]{true, false},
						new JPopupMenu[]{startPopupMenu, null}
				);

		exportControlPanel = new ExportControlPanel(session);
		add(buttonGroup, BorderLayout.WEST);
		add(pathField, BorderLayout.CENTER);
		add(exportControlPanel, BorderLayout.EAST);
	}

	@Override
	public void update(Float m) {
		pathField.repaint();
	}
}