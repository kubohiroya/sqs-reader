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
package net.sqs2.omr.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.sqs2.util.FileResourceID;
import net.sqs2.util.FileUtil;

public class FileContentsCache extends Cache<String,FileContents> {

	File rootDirectory;

	public FileContentsCache(File rootDirectory, int cacheSize) {
		super(cacheSize);
		this.rootDirectory = rootDirectory;
	}

	public FileContents get(FileResourceID fileResourceID) throws IOException {
		FileContents fileContents;
		synchronized (this.map) {
			fileContents = this.map.get(fileResourceID.getRelativePath());
			if (fileContents == null || fileContents.getLastModified() != fileResourceID.getLastModified()) {
				byte[] bytes = createByteArray(fileResourceID.getRelativePath());
				long lastModified = getLastModified(fileResourceID.getRelativePath());
				fileContents = new FileContents(bytes, lastModified);
				this.map.put(fileResourceID.getRelativePath(), fileContents);
			}
		}
		return fileContents;
	}
	
	public void delete(String relativePath){
		this.map.remove(relativePath);
	}

	public long getLastModified(String relativePath) {
		File file = new File(this.rootDirectory.getAbsolutePath(), relativePath);
		return file.lastModified();
	}

	private byte[] createByteArray(String relativePath) throws IOException {
		BufferedInputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			File file = new File(this.rootDirectory, relativePath);
			in = new BufferedInputStream(new FileInputStream(file));
			out = new ByteArrayOutputStream();
			FileUtil.connect(in, out);
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
		return out.toByteArray();
	}
}
