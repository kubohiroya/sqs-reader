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
import java.util.HashMap;
import java.util.Map;

public class GGroup extends GElement {
	
	Map<String,GElement> map;

	public GGroup(Point2D position) {
		super(position);
		this.map = new HashMap<String,GElement>(); 
	}

	public Map<String,GElement> getMap(){
		return this.map;
}
		
	public boolean contains(String key){
		return this.map.containsKey(key);
}
	
	public void remove(String key){
		this.map.remove(key);
	}
		
	public GElement get(String key){
		return this.map.get(key);
	}
	
	public void put(String key, GElement gelement){
		this.map.put(key, gelement);
	}
	
	public void clear(){
		this.map.clear();
	}
	
	public void draw(Graphics2D g){
		AffineTransform t = g.getTransform();
		g.scale(scaleX, scaleY);
		if(position != null){
			g.translate(position.getX(), position.getY());
		}

		for(GElement gelem: map.values()){
			gelem.draw(g);
		}
		
		g.setTransform(t);
	}
}
