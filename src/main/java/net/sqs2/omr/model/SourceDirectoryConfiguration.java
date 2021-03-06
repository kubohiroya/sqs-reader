/*

 SourceDirectoryConfiguration.java
 
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

import java.io.Serializable;
import java.net.MalformedURLException;

import net.sqs2.util.FileResourceID;

public class SourceDirectoryConfiguration implements Serializable{

	private static final long serialVersionUID = 0L;

	private FileResourceID configFileResourceID;
	private Config config;

	public SourceDirectoryConfiguration(FileResourceID configFileResourceID, Config config) throws MalformedURLException,ConfigSchemeException{
		this.configFileResourceID = configFileResourceID;
		this.config = config;		
	}

	public Config getConfig() {
		return this.config;
	}

	public FileResourceID getConfigFileResourceID() {
		return this.configFileResourceID;
	}

	public long lastModified() {
		return this.configFileResourceID.getLastModified();
	}

	public String getConfigFilePath() {
		return this.configFileResourceID.getRelativePath();
	}

	@Override
	public String toString() {
		return "[SourceDirectoryConfig:" + this.getConfigFileResourceID().getRelativePath()+"]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SourceDirectoryConfiguration) {
			SourceDirectoryConfiguration o = (SourceDirectoryConfiguration) obj;
			return this.configFileResourceID.equals(o.configFileResourceID);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.configFileResourceID.hashCode();
	}
}
