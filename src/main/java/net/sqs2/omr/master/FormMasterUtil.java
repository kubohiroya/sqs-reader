package net.sqs2.omr.master;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import net.sqs2.image.ImageFactory;

public class FormMasterUtil {
	public static int getNumPages(File file)throws IOException{
		InputStream in = null;
		try{
			in = new BufferedInputStream(new FileInputStream(file)); 
			HashMap<?,?> map = ImageFactory.getMetadataMap("pdf", in);
			String producer = (String)map.get("Producer");
			if(producer != null && producer.startsWith("SQS")){
				return ((Integer)map.get("NumberOfPages")).intValue();
			}else{
				return 0;
			}
		}finally{
			if(in != null){
				in.close();
			}
		}
	}

}
