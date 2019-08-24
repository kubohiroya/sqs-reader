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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class GText extends GDrawable {
	Font font;
	String text;

	public GText(Font font, String text, Point2D position, Color color){
		super(position, color);
		this.font = font;
		this.text= text;
	}

	public Font getFont() {
		return font;
	}

	public String getText() {
		return text;
	}

	public void draw(Graphics2D g){
		double factor = 1.0;
		Color prevColor = g.getColor();
		AffineTransform t = g.getTransform();
		Font prevFont = g.getFont();

		g.setColor(color);
		g.scale(factor/scaleX, factor/scaleY);
		g.translate(position.getX(), position.getY());		
		
		g.setFont(font);
		g.drawString(text, 0, 0);
		
		g.setFont(prevFont);
		g.setTransform(t);
		g.setColor(prevColor);
	}

}
