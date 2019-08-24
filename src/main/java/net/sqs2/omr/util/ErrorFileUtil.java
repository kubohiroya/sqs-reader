package net.sqs2.omr.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ErrorFileUtil {
	
	static void writeErrorFileNameList(File baseDir, File zipFile, List<File> errorFileList) throws UnsupportedEncodingException, FileNotFoundException {
		PrintWriter w = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(zipFile)), "UTF-8")); 
		for(File file: errorFileList){
			if(baseDir != null){
				w.print(baseDir.getName());
			}
			w.print(File.separatorChar);
			w.print(file.getParent());
			w.println();
		}
		w.close();
	}

}
