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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class GImage extends GElement {

	BufferedImage image;
	
	protected GImage(double scaleX, double scaleY, Point2D position, BufferedImage image) {
		super(scaleX, scaleY, position);
		this.image  = image;
	}

	public GImage(Point2D position, BufferedImage image) {
		super(position);
		this.image  = image;
	}

	public BufferedImage getImage(){
		return this.image;
	}
	
	public boolean contains(double x, double y){
		double _x = x * scaleX;
		double _y = y * scaleY;
		return this.position.getX() <= _x && _x < this.position.getX()+this.image.getWidth() &&
				this.position.getY() <= _y && _y < this.position.getY()+this.image.getHeight(); 
	}
	
	public void draw(Graphics2D g){
		AffineTransform t = g.getTransform();
		g.scale(scaleX, scaleY);
		g.translate(position.getX(), position.getY());
		g.drawImage(image, 0, 0, null);
		g.setTransform(t);
	}
	
}
