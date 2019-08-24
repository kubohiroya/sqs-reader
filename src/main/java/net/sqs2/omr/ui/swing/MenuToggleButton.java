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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class MenuToggleButton extends JToggleButton {
	private static final long serialVersionUID = 1L;
	private static final Icon MENU_ARROW_ICON = new MenuArrowIcon();
	  public MenuToggleButton() {
	    this("", null);
	  }
	  public MenuToggleButton(Icon icon) {
	    this("", icon);
	  }
	  public MenuToggleButton(String text) {
		  this(text, null);
	  }
	  public MenuToggleButton(String text, Icon icon) {
		  this(text, icon, false, null);
	  }
	  
	  int serial = 0;
	  boolean popupPrompt;
	  
	  public MenuToggleButton(final String text, Icon icon, boolean selected, final JPopupMenu pop) {
	    super();
	    this.pop = pop;

	    setText(text);
	    setFocusable(false);
	    setIcon(icon);
	    if(pop!=null){
	    	setMargin(new Insets(2, 4, 2, 6+MENU_ARROW_ICON.getIconWidth()));
	    	addMouseListener(new MouseAdapter(){
	    		@Override
		    	public void mousePressed(MouseEvent ev){
		    		MenuToggleButton b = MenuToggleButton.this;
			    	Dimension dim = b.getSize();
			    	Insets ins = b.getInsets();
		    		if(ev.getX() <= dim.width-ins.right+4){
		    			listener.actionPerformed(new ActionEvent(b, serial++, text));
		    		}else{
		    			pop.show(b, ins.right, b.getHeight() - ins.bottom);
		    		}
		    	}

		    	@Override
		    	public void mouseExited(MouseEvent ev){
	    			popupPrompt = false;
	    			repaint();
		    	}
		    });
	    	addMouseMotionListener(new MouseAdapter(){
		    	public void mouseMoved(MouseEvent ev){
		    		MenuToggleButton b = MenuToggleButton.this;
			    	Dimension dim = b.getSize();
			    	Insets ins = b.getInsets();
		    		if(dim.width-ins.right+4 <= ev.getX() && ev.getX() <= dim.width &&
		    				0 <= ev.getY() && ev.getY() <= dim.getHeight()){
		    			popupPrompt = true;
		    		}else{
		    			popupPrompt = false;
		    		}
		    		b.repaint();
		    	}
		    });
	    }
	    setSelected(selected);
	  }
	  
	  protected JPopupMenu pop;
	  public void setPopupMenu(final JPopupMenu pop) {
	    this.pop = pop;
	    pop.addPopupMenuListener(new PopupMenuListener() {
	      @Override public void popupMenuCanceled(PopupMenuEvent e) {
	    	  popupPrompt = false;
	    	  repaint();
	      }
	      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
	      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	        setSelected(false);
	        popupPrompt = false;
	        repaint();
	      }
	    });
	    pop.addMouseMotionListener(new MouseMotionListener(){
			@Override
			public void mouseDragged(MouseEvent e) {
			}
			@Override
			public void mouseMoved(MouseEvent e) {
			}
	    });
	  }
	  
	  ActionListener listener;
	  @Override public void addActionListener(ActionListener listener){
		  if(this.pop != null){
			  this.listener = listener;
		  }else{
			  super.addActionListener(listener);
		  }
	  }
	  
	  @Override protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    if(this.pop != null){
	    	Dimension dim = getSize();
	    	Insets ins = getInsets();
	    	int x = dim.width-ins.right+5;
	    	int y = ins.top+(dim.height-ins.top-ins.bottom-MENU_ARROW_ICON.getIconHeight())/2;

	    	if(popupPrompt){
	    		g.setColor(Color.gray);
	    		g.drawRoundRect(x, ins.top, ins.right - 10, dim.height - ins.top-ins.bottom, 5, 5);
	    	}
	    	MENU_ARROW_ICON.paintIcon(this, g, x+2, y);
	    	
	    }
	  }
	}
