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

import java.awt.Dimension;
import java.awt.Graphics2D;

import net.sqs2.omr.ui.util.ObservableObject;
import net.sqs2.omr.ui.util.Observer;

public class OMRImageCanvas extends SamplingImageCanvas implements Observer<OMRImage>{

	private static final long serialVersionUID = 1L;

	public OMRImageCanvas(ObservableObject<OMRImage> area,
			ObservableObject<Float> scaleObject, Dimension size) {
		super(area, scaleObject, size);
		//scaleObject.bind(this);
		area.bind(this);
	}
	
	public void update(OMRImage omrImage){
		repaint();
	}

	@Override
	public void paintCanvas(Graphics2D g) {
		
		super.paintCanvas(g);
		
		if(this.area == null || this.area.getObject() == null){
			return;
		}
		
		for(GElement gElem: this.area.getObject().getGelemMap().values()){
			gElem.draw(g);
		}
		
		for(GElement gElem: this.area.getObject().getGelemList()){
			gElem.draw(g);
		}

	}
}
