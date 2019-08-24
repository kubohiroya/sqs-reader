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
package net.sqs2.omr.tools;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sqs2.swing.FileDropTargetDecorator;

public class SwapFilenamesGUILauncher{
	public static void main(String[] args){
		SwapFilenamesGUILauncher app = new SwapFilenamesGUILauncher();
		new FileDropTargetDecorator(app.panel){
			public void drop(File[] file) {
				try{
					SwapFilenames.swapFilenames(file);
				}catch(IOException ex){
					ex.printStackTrace();
				}
			}
		};
	}
	
	JPanel panel;
	public SwapFilenamesGUILauncher(){
		this.panel = new JPanel(); 
		this.panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.panel.add(new JLabel("Drop a folder or files Here!"));
		JFrame frame = new JFrame();
		frame.setTitle("SwapFilenames");
		frame.add(this.panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
