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

import java.awt.Component;
import java.awt.Graphics;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class LayeredIcon extends ImageIcon{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Icon subicon;
	
	public LayeredIcon(URL url, Icon subicon){
		super(url);
		this.subicon = subicon;
	}
	
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		subicon.paintIcon(c, g, x, y);
		super.paintIcon(c, g, x, y);
	}
}