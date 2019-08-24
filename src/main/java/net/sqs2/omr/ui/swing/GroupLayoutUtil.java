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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GroupLayoutUtil{
public static void layout(JPanel p1, JLabel[] labels, JComponent[] components, boolean gap) {
	GroupLayout layout = new GroupLayout(p1);
	p1.setLayout(layout);
	if(gap){
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
	}

	GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
	
	GroupLayout.ParallelGroup pg1 =  layout.createParallelGroup();
	for(JLabel label: labels){
		pg1.addComponent(label);
	}
	hGroup.addGroup(pg1);

	GroupLayout.ParallelGroup pg2 = layout.createParallelGroup();
	for(JComponent comp: components){
		pg2.addComponent(comp);
	}
	hGroup.addGroup(pg2);
	
	layout.setHorizontalGroup(hGroup);

	GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
	for(int i = 0; i < labels.length; i++){
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(labels[i]).addComponent(components[i]));
	}
	layout.setVerticalGroup(vGroup);
}
}