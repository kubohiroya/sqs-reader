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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import net.sqs2.image.ImageFactory;

import org.apache.commons.lang.SystemUtils;

class PageContentCursors{
	public static final String[] SHAPE_NAME_ARRAY = {"Oval","Square"};
	public static final String[] COLOR_NAME_ARRAY = {"Black","White"};
	public static final int [] PEN_SIZE_ARRAY = new int[]{1, 2, 5, 15};
	public static final Cursor PEN_CURSOR[] = new Cursor[SHAPE_NAME_ARRAY.length*COLOR_NAME_ARRAY.length*PEN_SIZE_ARRAY.length];
	public static final ImageIcon PEN_ICON[] = new ImageIcon[SHAPE_NAME_ARRAY.length*COLOR_NAME_ARRAY.length*PEN_SIZE_ARRAY.length];
	public static final String[] CURSOR_NAMES = new String[SHAPE_NAME_ARRAY.length*COLOR_NAME_ARRAY.length*PEN_SIZE_ARRAY.length];
	public static Cursor MOVE_CURSOR = null;
	
	static{
		createCustomCursors();
		if(SystemUtils.IS_OS_MAC_OSX){
			InputStream in = null;
			try{
				in = new URL("class:icon/move.gif").openStream();
				BufferedImage cursorImage = ImageFactory.createImage("gif", in);
				int cursorSize = cursorImage.getHeight();
				MOVE_CURSOR = 
						Toolkit.getDefaultToolkit().createCustomCursor(cursorImage,
								new Point(cursorSize/2,cursorSize/2), "MOVE");
			}catch(IOException ignore){
			}finally{
				if(in != null){
					try{
						in.close();
					}catch(IOException ignore){}
				}
			}
		}
	}
	
	public static void createCustomCursors(){
		for(int shapeIndex = 0; shapeIndex < SHAPE_NAME_ARRAY.length; shapeIndex++){
			for(int colorIndex = 0; colorIndex < COLOR_NAME_ARRAY.length; colorIndex++){
				for(int sizeIndex = 0; sizeIndex < PEN_SIZE_ARRAY.length; sizeIndex++){
					int r = PEN_SIZE_ARRAY[sizeIndex];
					int l = r * 2 + 1;
					BufferedImage iconImage = new BufferedImage(l , l, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics2D iconG= iconImage.createGraphics();
					iconG.setPaint(Color.DARK_GRAY);
				
					iconG.setStroke(new BasicStroke(2.0f));
					if(shapeIndex == 0){
						if(colorIndex == 0){
							iconG.fillOval(1, 1, l-2, l-2);
						}else{
							iconG.drawOval(1, 1, l-2, l-2);
						}
					}else{
						if(colorIndex == 0){
							iconG.fillRect(1, 1, l-2, l-2);
						}else{
							iconG.drawRect(1, 1, l-2, l-2);
						}
					}
					iconG.dispose();
					PEN_ICON[shapeIndex*8+colorIndex*4+sizeIndex] = new ImageIcon(iconImage);
					
					int cursorSize = PEN_SIZE_ARRAY [PEN_SIZE_ARRAY.length-1]*2+1;
					BufferedImage cursorImage = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics2D cursorG = cursorImage.createGraphics();
					cursorG.setPaint(Color.DARK_GRAY);
					cursorG.drawImage(iconImage, (cursorSize-l)/2, (cursorSize-l)/2, null);
					if(sizeIndex < 3){
						cursorG.drawLine(0,cursorSize/2, 4, cursorSize/2);
						cursorG.drawLine(cursorSize - 1, cursorSize/2, cursorSize - 5, cursorSize/2);
						cursorG.drawLine(cursorSize/2, 0, cursorSize/2, 4);
						cursorG.drawLine(cursorSize/2, cursorSize - 1, cursorSize/2, cursorSize - 5);
					}
					cursorG.dispose();
					PEN_CURSOR[shapeIndex*8+colorIndex*4+sizeIndex] = 
							Toolkit.getDefaultToolkit().createCustomCursor(cursorImage,
									new Point(cursorSize/2,cursorSize/2), 
									SHAPE_NAME_ARRAY[shapeIndex] + COLOR_NAME_ARRAY[colorIndex]+l);
					CURSOR_NAMES[shapeIndex*SHAPE_NAME_ARRAY.length*PEN_SIZE_ARRAY.length + colorIndex*PEN_SIZE_ARRAY.length + sizeIndex] = SHAPE_NAME_ARRAY[shapeIndex]+COLOR_NAME_ARRAY[colorIndex]+"-"+(sizeIndex+1);
				}
			}
		}
	}
	
}