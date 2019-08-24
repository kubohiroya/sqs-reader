/*

 SessionConfigUtil.java

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

 */
package net.sqs2.omr.session.init;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.util.JarExtender;
import net.sqs2.util.FileResourceID;
import net.sqs2.util.FileUtil;

public class ConfigFileUtil{

	public static FileResourceID createConfigurationTemplateFile(SourceDirectory sourceDirectory) {
		
		String configPath;
		
		if(0 < sourceDirectory.getRelativePath().length()){
			configPath = sourceDirectory.getRelativePath() + File.separatorChar + AppConstants.RESULT_DIRNAME + File.separatorChar + AppConstants.SOURCE_CONFIG_FILENAME;
		}else{
			configPath = AppConstants.RESULT_DIRNAME + File.separatorChar + AppConstants.SOURCE_CONFIG_FILENAME;
		}
		
		File configFile = new File(sourceDirectory.getSourceDirectoryRootFile(), configPath);
		if (! configFile.exists()) {
			File resultDir = new File(sourceDirectory.getDirectory(), AppConstants.RESULT_DIRNAME);
			new JarExtender().extend(new String[] { AppConstants.SOURCE_CONFIG_FILENAME }, resultDir);
		}
		return new FileResourceID(configPath, configFile.lastModified());	
	}
	
	public static void createConfigFileIfNotExists(File sourceDirectoryRootFile) throws MalformedURLException {
		File configFile = new File(new File(sourceDirectoryRootFile, AppConstants.RESULT_DIRNAME),
				AppConstants.SOURCE_CONFIG_FILENAME);
		createConfigFile(configFile);
	}

	public static void createConfigFile(File configFile) throws MalformedURLException {
		if (!configFile.exists()) {
			InputStream in = null;
			OutputStream out = null; 
			try {
				in = createDefaultConfigFileInputStream();
				out = new BufferedOutputStream(new FileOutputStream(configFile));
				FileUtil.pipe(in, out);
			} catch (IOException ignore) {
			}finally{
				try{
					if(in != null){
						in.close();
					}
					if(out != null){
						out.close();
					}
				} catch (IOException ignore) {}
			}
		}
	}

	private static InputStream createDefaultConfigFileInputStream() throws IOException {
		if (AppConstants.USER_CUSTOMIZED_DEFAULT_CONFIG_FILE.exists()) {
			return new BufferedInputStream(new FileInputStream(
					AppConstants.USER_CUSTOMIZED_DEFAULT_CONFIG_FILE));
		} else {
			return ConfigFileUtil.class.getClassLoader().getResourceAsStream(
					AppConstants.SOURCE_CONFIG_FILENAME);
		}
	}
}