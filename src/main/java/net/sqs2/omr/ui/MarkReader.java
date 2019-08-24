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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import net.sqs2.net.ClassURLStreamHandlerFactory;
import net.sqs2.omr.app.MarkReaderApp;
import net.sqs2.omr.app.MarkReaderNoGUILauncher;
import net.sqs2.swing.process.RemoteWindowPopupDecorator;

public class MarkReader {

	static MarkReaderModel model;
	static MarkReaderPanel view;
	static MarkReaderController controller;

	public MarkReader() throws Exception{
		
		model = new MarkReaderModel(new MarkReaderApp(7345));

		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				try{
					view = new MarkReaderPanel(model);
					RemoteWindowPopupDecorator decorator = new RemoteWindowPopupDecorator();
					if(! decorator.activate(view.getFrame(), 1099)){
						System.exit(0);
					}
										
					view.getFrame().setPreferredSize(new Dimension(850,850));
					view.show();

					controller = new MarkReaderController(view);
					controller.bind(model);
					
					decorator.shutdown();

					//File file = new File("/Users/hiroya/Desktop/test");
					//new RemoveResultFoldersCommand(file).call();
					//controller.openDirectoryASync(file);
					
				}catch(Exception ex){
					ex.printStackTrace();
					System.exit(0);
				}
			}
		});
	}
	
	public void open(String[] filenames)throws IOException{
		model.app.open(filenames);
	}

	/**
	 * 
	 * @param second -1 wait forever
	 */
	public void waitUntilAllSessionStopped(){
		model.app.waitUntilAllSessionStopped();
	}

	// scenario 1: gui, no default folder () 
	// scenario 2: gui, default folder (arg0 arg1 arg2)
	// scenario 3: no gui, default folder(--nogui arg0 arg1 arg2)
	// scenario 4: no gui, daemon (--nogui)
	
	static final Option COMMAND_LINE_OPTION_OF_NOGUI_MODE = OptionBuilder.withArgName("n").hasOptionalArgs().withDescription("no GUI mode").withLongOpt("nogui").create("n");
	
    public static void main(String args[]) throws Exception{
		URL.setURLStreamHandlerFactory(ClassURLStreamHandlerFactory.getSingleton());

		if(args.length == 0){
			// gui with no opened folders 
			new MarkReader();
    		
    	}else{
    		Options options = new Options();

    		options.addOption(COMMAND_LINE_OPTION_OF_NOGUI_MODE);

    		CommandLineParser parser = new PosixParser();
    		CommandLine commandLine = parser.parse(options, args);

    		if(commandLine.hasOption(COMMAND_LINE_OPTION_OF_NOGUI_MODE.getArgName())){
    			if(commandLine.getArgs().length == 0){
    				// daemon mode, with no opend folders
    				new MarkReaderNoGUILauncher();// FIXME! this is not a daemon
    			}else{
    				// no gui with opened folders
    				MarkReaderNoGUILauncher launcher = new MarkReaderNoGUILauncher();
    				launcher.open(commandLine.getArgs());
    				launcher.waitUntilAllSessionStopped();
    				System.exit(0);
    			}
    		}else{
    			//gui with opened folders.
    			final MarkReader markReader = new MarkReader();
    			for(String filename: commandLine.getArgs()){
    				final File file = new File(filename);
    				if(file.isDirectory()){
    					SwingUtilities.invokeLater(new Runnable(){
    						public void run(){
    							try{
    								markReader.controller.openDirectory(file);
    							}catch(IOException ex){
    								ex.printStackTrace();
    							}
    						}
    					});
    				}
    			}
    		}
    	}
	}
}
