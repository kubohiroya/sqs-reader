/**
 *  ConfigHandlerFactoryImpl.java
 
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
 
 Created on 2007/07/31
 Author hiroya
 */

package net.sqs2.omr.session.init;

import java.net.MalformedURLException;

import net.sqs2.omr.model.Config;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.util.FileResourceID;


public class SourceDirectoryConfigurationFactoryImpl implements SourceDirectoryConfigurationFactory {

	public SourceDirectoryConfigurationFactoryImpl() {
	}

	public SourceDirectoryConfiguration create(FileResourceID configFileResourceID, Config config) throws MalformedURLException,ConfigSchemeException {
		return new SourceDirectoryConfiguration(configFileResourceID, config);
	}

}
