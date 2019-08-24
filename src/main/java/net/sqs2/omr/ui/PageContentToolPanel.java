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

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import net.sqs2.omr.ui.swing.ButtonGroupPanel;
import net.sqs2.omr.ui.swing.ImageIconUtil;
import net.sqs2.omr.ui.swing.MenuToggleButton;

class PageContentToolPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	ScaleSlider previewScaleSlider;
	ScaleSlider deskewScaleSlider;
	JPanel scaleSliderCardPanel;

	MenuToggleButton selectButton;
	MenuToggleButton drawButton;

	JToggleButton saveButton;
	CardLayout scaleSliderCardLayout;
	ButtonGroupPanel buttonGroup;
	ButtonGroup group;
	JPopupMenu drawPopupMenu;
	
	JMenuItem[] drawMenuItem;
		
	PageContentToolPanel(PageContentModel pageContentModel){
		
		previewScaleSlider = new ScaleSlider();
		deskewScaleSlider = new ScaleSlider();
		
		scaleSliderCardPanel = new JPanel();
		scaleSliderCardLayout = new CardLayout();
		scaleSliderCardPanel.setLayout(scaleSliderCardLayout);
		scaleSliderCardPanel.add(ContentPanel.MODE_NAMES[0], previewScaleSlider);
		scaleSliderCardPanel.add(ContentPanel.MODE_NAMES[1], deskewScaleSlider);
		scaleSliderCardLayout.show(scaleSliderCardPanel, ContentPanel.MODE_NAMES[ContentPanel.DEFAULT_MODE]);
	
		setBorder(new EmptyBorder(0,3,3,3));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	
		drawPopupMenu = new JPopupMenu(); 

		drawPopupMenu.setLayout(new GridLayout(2,4));
		
		drawMenuItem = new JMenuItem[16];
		group = new ButtonGroup();
		for(int i=0; i<drawMenuItem.length; i++){
			drawMenuItem[i] = new JRadioButtonMenuItem();
			drawMenuItem[i].setIcon(PageContentCursors.PEN_ICON[i]);
			drawMenuItem[i].setActionCommand("Pen-"+i);
			group.add(drawMenuItem[i]);
			drawPopupMenu.add(drawMenuItem[i]);
		}
		drawMenuItem[0].setSelected(true);
		try{
			
			buttonGroup = new ButtonGroupPanel(new String[]{"Select", "Draw"},
					new ImageIcon[]{ImageIconUtil.createImageIcon("class:icon/arrow32.png"),
					ImageIconUtil.createImageIcon("class:icon/pencil32.png"),
					ImageIconUtil.createImageIcon("class:icon/plus32.png")
					},
					new boolean[]{true, false, false},
					new JPopupMenu[]{null, drawPopupMenu, null}
					);
			selectButton = buttonGroup.get("Select");
			drawButton = buttonGroup.get("Draw");
			
			saveButton = new JToggleButton(new ImageIcon(new URL("class:icon/check32.png")));
			saveButton.setText("SaveImage");
			saveButton.setEnabled(false);
			
			add(buttonGroup);
			add(scaleSliderCardPanel);
			add(saveButton);
		}catch(MalformedURLException ex){
			ex.printStackTrace();
		}
	}
}