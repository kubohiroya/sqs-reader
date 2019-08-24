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
import java.awt.geom.Point2D;

public abstract class GElement  {

	double scaleX ;
	double scaleY;
	Point2D position;

	protected GElement(Point2D position){
		this(1.0, 1.0, position);
	}
	
	protected GElement(double scaleX, double scaleY, Point2D position){
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.position = position;
	}
	/*
	public GElement(Shape shape, Point2D position, double sx, double sy, Color pathColor, Stroke pathStroke, Color fillColor, Color xorColor){
		this(shape, position, sx, sy, pathColor, pathStroke, fillColor);
		this.xorColor = xorColor;
	}
	
	public GElement(Shape shape, Point2D position, double sx, double sy, Color pathColor, Stroke pathStroke, Color fillColor){
		this(shape, null, null, position, pathColor, pathStroke, fillColor);
		this.scaleX = sx;
		this.scaleY = sy;
	}
	
	public GElement(Font font, String message, Point2D position, Color pathColor, Stroke pathStroke, Color fillColor){
		this(null, font, message, position, pathColor, pathStroke, fillColor);
	}
	
	public GElement(Shape shape, Font font, String message, Point2D position, Color pathColor, Stroke pathStroke, Color fillColor){
		this.shape = shape;
		this.font = font;
		this.message = message;
		this.position = position;
		this.pathColor = pathColor;
		this.pathStroke = pathStroke;
		this.fillColor = fillColor;
	}*/
	
	public double getScaleX(){
		return scaleX;
	}
	
	public double getScaleY(){
		return scaleY;
	}
	
	public Point2D getPosition() {
		return position;
	}
	
	public abstract void draw(Graphics2D g);

}
