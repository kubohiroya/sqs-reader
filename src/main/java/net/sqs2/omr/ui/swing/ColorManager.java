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
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ColorManager {
	
	static float G = (float) ((1+Math.sqrt(5))/2.0);

	public static float[] fibonacciHueColor(int length){
		float[] ret = new float[length];
		float h = 0f;
		for(int i = 0; i < length; i++){
			h = (h + (1/G ))%1;
			ret[i] = h;
		}
		return ret;
	} 
	
	public static void main(String args[]){

		JPanel p = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics _g){
				Graphics2D g = (Graphics2D) _g;

				//g.scale(80.0, 20.0);
				//int index = 0;
				
				float h = 0f;
				
				for(int y = 0; y < 10; y++){
					for(int x = 0; x < 10; x++){
						h = (h + (1/G ))%1;
						g.setColor(Color.getHSBColor(h, 0.5f, 1.0f));
						int px = x * 80;
						int py = y * 12;
						g.fillRect(px,  py, 80, 12);
						g.setColor(Color.black);
						g.drawString(String.valueOf(h), px, py+12);
						//index++;
					}
				}
			}
		};
		p.setPreferredSize(new Dimension(800,200));
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(p);
		frame.pack();
		frame.setVisible(true);


	}
}
