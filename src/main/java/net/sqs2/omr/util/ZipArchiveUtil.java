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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;


public class ZipArchiveUtil{
	static byte[] buf = new byte[4096];
	
	public static File encode(List<File> targetFileList, File zipFile, File baseDir, boolean isIncludingRootDir) throws IOException{
		try{
			zipFile = addZipSuffix(zipFile);
			File[] targetFiles = new File[targetFileList.size()];
			targetFiles = targetFileList.toArray(targetFiles);
			return encode(targetFiles, zipFile, baseDir, isIncludingRootDir);

		}catch(CompressorException ex){
			throw new IOException(ex);
		}
	}

	private static File encode(File[] targetFiles, File zipFile, File baseDir, boolean isIncludingRootDir) throws IOException,CompressorException{
		OutputStream out = new BufferedOutputStream(new FileOutputStream(zipFile));
		ZipArchiveOutputStream zos = new ZipArchiveOutputStream(out);
		zos.setEncoding("Windows-31J");
		try{
			encode(zos, targetFiles, baseDir, isIncludingRootDir);
		}finally{
			zos.finish();
			zos.flush();
			zos.close();
		}
		return zipFile;
	}
	
	private static File addZipSuffix(File zipFile) {
		if(! zipFile.getName().toLowerCase().endsWith(".zip")){
			zipFile = new File(zipFile.getParentFile(), zipFile.getName()+".zip");
		}
		return zipFile;
	}
	
	private static void encode(ZipArchiveOutputStream zos, File[] files, File baseDir, boolean isIncludingBaseDir)throws IOException,CompressorException{
		for(File f: files){
		
			String filename = (isIncludingBaseDir)? baseDir.getName()+'/' : "";
			filename += f.getPath().replace('\\', '/');

			if(f.isDirectory()){
				ZipArchiveEntry entry = new ZipArchiveEntry(filename+'/');
				zos.putArchiveEntry(entry);
				zos.closeArchiveEntry();
				encode(zos, f.listFiles(), baseDir, isIncludingBaseDir);
			}else{
				
				ZipArchiveEntry entry = new ZipArchiveEntry(filename);				
				zos.putArchiveEntry(entry);
				
				InputStream is = new BufferedInputStream(new FileInputStream(new File(baseDir, f.getPath())));
				while(true){
					int len = is.read(buf);
					if (len < 0){
						break;
					}
					zos.write(buf, 0, len);
				}
				is.close();
				
				zos.closeArchiveEntry();
			}
		}
	}
	
}