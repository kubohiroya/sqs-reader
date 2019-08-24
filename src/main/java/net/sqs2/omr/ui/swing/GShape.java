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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class GShape extends GDrawable implements Shape{
	Shape shape;
	Stroke pathStroke;
	Color fillColor;
	Color xorColor;

	public GShape(Shape shape, Point2D position, double sx, double sy, Color pathColor, Stroke pathStroke, Color fillColor){
		this(shape, position, sx, sy, pathColor, pathStroke, fillColor, null);
	}
	
	public GShape(Shape shape, Point2D position, double sx, double sy, Color pathColor, Stroke pathStroke, Color fillColor, Color xorColor){
		super(sx, sy, position, pathColor);
		this.shape = shape;
		this.pathStroke = pathStroke;
		this.fillColor = fillColor;
		this.xorColor = xorColor;
	}

	public void setShape(Shape shape){
		this.shape = shape;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public void setPathStroke(Stroke pathStroke){
		this.pathStroke = pathStroke;
	}

	public Stroke getPathStroke() {
		return pathStroke;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public Color getXORColor(){
		return xorColor;
	}

	@Override
	public boolean contains(Point2D p) {
		if(shape == null){
			return false;
		}
		return shape.contains(p);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		if(shape == null){
			return false;
		}
		return shape.contains(r);
	}

	@Override
	public boolean contains(double x, double y) {
		if(shape == null){
			return false;
		}
		return shape.contains(x, y);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		if(shape == null){
			return false;
		}
		return shape.contains(x, y, w, h);
	}

	@Override
	public Rectangle getBounds() {
		if(shape == null){
			return null;
		}
		return shape.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		if(shape == null){
			return null;
		}
		return shape.getBounds2D();
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		if(shape == null){
			return null;
		}
		return shape.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		if(shape == null){
			return null;
		}
		return shape.getPathIterator(at, flatness);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		if(shape == null){
			return false;
		}
		return shape.intersects(r);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		if(shape == null){
			return false;
		}
		return shape.intersects(x, y, w, h);
	}

	public void draw(Graphics2D g){
		if(shape == null){
			return;
		}
		
		Stroke prevStroke = g.getStroke();
		Color prevColor = g.getColor();
		AffineTransform t = g.getTransform();
		
		if(pathStroke != null){
			g.setStroke(pathStroke);
		}
		if(xorColor != null){
			g.setXORMode(xorColor);
		}

		g.setColor(color);
		
		g.translate(position.getX(), position.getY());
		g.scale(scaleX, scaleY);
		g.draw(shape);
		
		g.setTransform(t);
		g.setColor(prevColor);
		g.setStroke(prevStroke);
	}

}
