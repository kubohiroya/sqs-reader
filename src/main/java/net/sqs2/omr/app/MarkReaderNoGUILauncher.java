/*

 MarkReaderCommandLineLauncher.java

 Copyright 2007 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on Apr 7, 2007

 */
package net.sqs2.omr.app;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import net.sqs2.net.ClassURLStreamHandlerFactory;

public class MarkReaderNoGUILauncher {

	 MarkReaderApp markReaderApp;
		
	public MarkReaderNoGUILauncher() throws UnknownHostException, IOException, InterruptedException {
		boolean isLocalTaskExecutorEnabled = true;
		markReaderApp = new MarkReaderApp(1099, isLocalTaskExecutorEnabled);
	}

	public void open(String[] args) throws IOException {
		markReaderApp.open(args);
	}

	public void waitUntilAllSessionStopped() throws IOException {
		markReaderApp.waitUntilAllSessionStopped();
	}

	public static void main(String[] args) throws Exception {

		try {
			URL.setURLStreamHandlerFactory(ClassURLStreamHandlerFactory.getSingleton());
		} catch (Error ex) {
			ex.printStackTrace();
		}
		
		MarkReaderNoGUILauncher  noGUILauncher = new MarkReaderNoGUILauncher ();
		noGUILauncher.open(args);
		noGUILauncher.waitUntilAllSessionStopped();
		System.exit(0);
	}
}
