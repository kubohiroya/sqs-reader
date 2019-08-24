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

import java.awt.Dimension;
import java.awt.Font;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

class ScaleSlider extends JSlider {
	private static final long serialVersionUID = 1L;

	private static final int SCALE = 7;

	class ScaleSliderJLabel extends JLabel{
		private static final long serialVersionUID = 1L;
		ScaleSliderJLabel(String label){
			super(label);
		}
	}
	
	ScaleSlider() {
		super(0, SCALE, 0);
		setPreferredSize(new Dimension(256, 36));
		setMajorTickSpacing(1);
		setSnapToTicks(true);
		setPaintTicks(true);
		
		Font font = new Font(Font.SERIF, Font.PLAIN, 10);
		
		Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
		JLabel [] labels = new JLabel[SCALE+1];
		
		labels[0] = new JLabel("<html>Fit</html>");
		labels[1] = new JLabel("<html>1/8</html>");
		labels[2] = new JLabel("<html>1/4</html>");
		labels[3] = new JLabel("<html>1/2</html>");
		labels[4] = new JLabel("<html>1</html>");
		labels[5] = new JLabel("<html>2</html>");
		labels[6] = new JLabel("<html>4</html>");
		labels[7] = new JLabel("<html>8</html>");
		
		for(int i=0; i < SCALE + 1; i++){
			labels[i].setFont(font);
			labelTable.put(i, labels[i]);
		}
		setLabelTable(labelTable);
		setPaintLabels(true);
	}
	
	public float getScale() {
		return valueToScale(getValue());
	}
	
	static private int scaleToValue(float scale){
		if (scale <= 1.0f / 8) {
			return 1;
		} else if (scale <= 1.0f / 4) {
			return 2;
		} else if (scale <= 1.0f / 2) {
			return 3;
		} else if (scale <= 1.0f) {
			return 4;
		} else if (scale <= 2.0f) {
			return 5;
		} else if (scale <= 4.0f) {
			return 6;
		} else if (scale <= 8.0f) {
			return 7;
		}
		return 0;
	}
	
	static public float valueToScale(int value) {
		switch(value) {
		case 1:
			return 1.0f / 8;
		case 2:
			return 1.0f / 4;
		case 3:
			return 1.0f / 2;
		case 4:
			return 1.0f;
		case 5:
			return 2.0f;
		case 6:
			return 4.0f;
		case 7:
			return 8.0f;
		}
		return 0;
	}
}