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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

public class MenuArrowIcon implements Icon {
	  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
	    Graphics2D g2 = (Graphics2D)g;
	    g2.setPaint(Color.BLACK);
	    g2.translate(x,y);
	    g2.drawLine( 2, 3, 6, 3 );
	    g2.drawLine( 3, 4, 5, 4 );
	    g2.drawLine( 4, 5, 4, 5 );
	    g2.translate(-x,-y);
	  }
	  @Override public int getIconWidth()  { return 9; }
	  @Override public int getIconHeight() { return 9; }
}