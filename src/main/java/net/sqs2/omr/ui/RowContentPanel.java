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

import javax.swing.JPanel;

import net.sqs2.omr.model.PageID;

class RowContentPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	//SessionModel sessionModel;
	RowContentModel rowContentModel;
	
	RowContentPanel(RowContentModel rowContentModel){
		this.rowContentModel = rowContentModel;
		setLayout(new BorderLayout());
	}
	
	public void setModel(RowContentModel rowContentModel){
		this.rowContentModel = rowContentModel;
		/*
		removeAll();
		setLayout(new GridLayout(1, this.rowContentModel.omrCanvasList.size()));
		for(OMRImageCanvas c: this.rowContentModel.omrCanvasList){
			add(c);
		}*/
	}
	
	public void paintComponent(Graphics g){
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(rowContentModel.pageIDArray != null){
			g.setColor(Color.black);
			for(int i=0; i < rowContentModel.pageIDArray.length; i++){
				PageID pageID = rowContentModel.pageIDArray[i];
				g.drawString("content row panel: "+pageID.toString(), 100, 100+i * 20);
			}
			
		}
	}

}
