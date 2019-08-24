/*

 PageTaskAccessor.java

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

 Created on 2007/01/11

 */
package net.sqs2.omr.model;

import java.io.File;
import java.io.IOException;

import net.sqs2.store.ObjectStore.ObjectStoreException;

public class PageTaskAccessor extends AbstractObjectAccessor{

	public static PageTaskAccessor createInstance(File sourceDirectoryRoot)throws IOException{
		return new PageTaskAccessor(sourceDirectoryRoot);
	}
	
	public PageTaskAccessor(File sourceDirectoryRoot) throws ObjectStoreException {
		super(sourceDirectoryRoot, "pageTask");
	}

	public PageTask get(String key) throws ObjectStoreException{
		return (PageTask) this.dba.get(key);
	}

	public PageTask get(PageID pageID, int pageIndex) throws ObjectStoreException{
		return (PageTask) this.dba.get(OMRPageTask.createID(pageID, pageIndex));
	}

	public void put(PageTask pageTask)  throws ObjectStoreException{
		this.dba.put(pageTask.getID(), pageTask);
	}
}
