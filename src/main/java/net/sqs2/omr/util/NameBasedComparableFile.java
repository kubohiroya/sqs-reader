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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class NameBasedComparableFile implements Comparable<NameBasedComparableFile> {
	private File file;

	private NameBasedComparableFile(File file) {
		this.file = file;
	}

	private File getFile() {
		return this.file;
	}

	public int compareTo(NameBasedComparableFile o) {
		try {
			String path = o.getFile().getCanonicalFile().getAbsolutePath();
			return this.file.getCanonicalFile().getAbsolutePath().compareTo(path);
		} catch (IOException ignore) {
			return 0;
		}
	}

	public static File[] createSortedFileArray(File[] items) {
		NameBasedComparableFile[] files = new NameBasedComparableFile[items.length];
		for (int i = 0; i < items.length; i++) {
			files[i] = new NameBasedComparableFile(items[i]);
		}
		Arrays.sort(files);
		File[] ret = new File[items.length];
		for (int i = 0; i < items.length; i++) {
			ret[i] = files[i].getFile();
		}
		return ret;
	}
}
