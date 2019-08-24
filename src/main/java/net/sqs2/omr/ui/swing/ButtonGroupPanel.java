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

import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;


public class ButtonGroupPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Map<String,MenuToggleButton> map;
	MenuToggleButton[] button;  
	ButtonGroup group;
	public ButtonGroupPanel(String[] text, Icon[] icon, boolean[] selected, JPopupMenu[] popup){
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		map = new HashMap<String,MenuToggleButton>();
		group = new ButtonGroup();
		button = new MenuToggleButton[text.length];
		for(int i=0; i< text.length; i++){
			MenuToggleButton b = new MenuToggleButton(text[i], icon[i], selected[i], popup[i]);
			group.add(b);
			button[i] = b;
			add(b);
			map.put(text[i], b);
		}
	}

	public MenuToggleButton[] getButtons(){
		return button;
	}
	
	public MenuToggleButton get(String name){
		return map.get(name);
	}
}