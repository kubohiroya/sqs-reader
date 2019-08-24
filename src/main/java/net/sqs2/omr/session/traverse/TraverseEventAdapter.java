/**
 * ExportEventAdapter.java

 Copyright 2009 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Author hiroya
 */

package net.sqs2.omr.session.traverse;


public abstract class TraverseEventAdapter implements TraverseEventListener {

	public TraverseEventAdapter() {
		super();
	}

	public void startSessionSource(SessionSourceEvent sessionSourceEvent) {
	}

	public void startMaster(MasterEvent masterEvent) {
	}

	public void startSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent) {
	}

	public void endSourceDirectory(SourceDirectoryEvent spreadSheetEvent) {
	}

	public void endMaster(MasterEvent masterEvent) {
	}

	public void endSessionSource(SessionSourceEvent sessionSourceEvent) {
	}

}
