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

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import net.sqs2.omr.ui.swing.OMRImage;
import net.sqs2.omr.ui.util.ObservableObject;

class OMRImageModelPainter{
	
	int penShapeIndex = 0;
	int penSizeIndex = 0;
	int penColorIndex = 0;
	
	PageContentModel pageContentModel;
	
	OMRImageModelPainter(PageContentModel pageContentModel){
		this.pageContentModel = pageContentModel;
	}

	void flipColor(ObservableObject<OMRImage> observable, float scale, Point p) {
		OMRImage b = observable.getObject();
		if (b == null) {
			return;
		}
		if (b.getSourceImage() == null) {
			return;
		}
		int x = (int) (p.getX() / scale);
		int y = (int) (p.getY() / scale);
		int argb = b.getSourceImage().getRGB(x, y);
		int black = 0;
		int white = 0x00ffffff;
		int color = (argb == 0) ? white : black;

		try{
			b.getSourceImage().setRGB(x, y, color);
			b.setSourcemageChanged();
		}catch(Exception ignore){}
	}
	
	private void draw(OMRImage b, int size, Color color, int x, int y) {
		for(int j = y - size; j <= y + size; j++){
			for(int i = x - size; i <= x + size; i++){
				if(penShapeIndex==0){
					if( (x-i)*(x-i) + (y-j)*(y-j) <= size * size ){
						try{
							b.getSourceImage().setRGB(i, j, color.getRGB());
						}catch(Exception ignore){}
					}
				}else{
					try{
						b.getSourceImage().setRGB(i, j, color.getRGB());
					}catch(Exception ignore){}
				}
			}
		}
		b.setSourcemageChanged();
	}
	
	private void draw(OMRImage b, int size, Color color, double x, double y) {
		draw(b, size, color, (int)x, (int)y);
	}
	
	void penUp(MouseEvent e, float scale, ObservableObject<OMRImage> observable){
			OMRImage b = observable.getObject();
			if(pageContentModel != null){
				pageContentModel.setPrevCursorDrawPosition(null);
			}
			if (b == null) {
				return;
			}
	}
	
	void drawPoint(MouseEvent e, float scale, ObservableObject<OMRImage> observable) {
			OMRImage b = observable.getObject();
			if (b == null || b.getSourceImage() == null) {
				return;
			}
			int size = PageContentCursors.PEN_SIZE_ARRAY[penSizeIndex];
			Color color = (penColorIndex == 0)? Color.black : Color.white;
			int x = (int) (e.getX() / scale);
			int y = (int) (e.getY() / scale);

			draw(b, size, color, x, y);
			pageContentModel.setPrevCursorDrawPosition(new Point(x, y));
			
			b.setSourcemageChanged();
	}
	
	synchronized void drawLine(MouseEvent e, float scale,
			ObservableObject<OMRImage> observable) {
			OMRImage b = observable.getObject();
			if (b == null || b.getSourceImage() == null) {
				return;
			}
			int x = (int) (e.getX() / scale);
			int y = (int) (e.getY() / scale);

			int size = PageContentCursors.PEN_SIZE_ARRAY[penSizeIndex];
			Color color = (penColorIndex == 0)? Color.black : Color.white;
			Point prevCursorPosition = pageContentModel.getPrevCursorDrawPosition();
			if(prevCursorPosition == null){
				draw(b, size, color, x, y);	
			}else{
				float px = (float)prevCursorPosition.getX();
				float py = (float)prevCursorPosition.getY();
				float dx = (float)(x - px);
				float dy = (float)(y - py);
				float adx = Math.abs(dx) ;
				float ady = Math.abs(dy) ;
				
				if(0 < adx || 0 < ady){
					if(ady <= adx){
						if(x <= px){
							for(float ix = x; ix <= px; ix++){
								float iy = y - dy * (ix - x)/adx;
								draw(b, size, color, ix, iy);
							}	
						}else{
							for(float ix = px; ix <= x; ix++){
								float iy = py + dy * (ix - px)/adx;
								draw(b, size, color, ix, iy);
							}	
						}
					}else{
						if(y <= py){
							for(float iy = y; iy <= py; iy++){
								float ix = x - dx * (iy - y)/ady;
								draw(b, size, color, ix, iy);
							}
						}else{
							for(float iy = py; iy <= y; iy++){
								float ix = px + dx * (iy - py)/ady;
								draw(b, size, color, ix, iy);
							}	
						}
					}
				}
			}
			pageContentModel.setPrevCursorDrawPosition(new Point(x, y));

			b.setSourcemageChanged();
	}
	
}