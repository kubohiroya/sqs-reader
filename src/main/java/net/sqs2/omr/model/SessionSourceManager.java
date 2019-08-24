package net.sqs2.omr.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections15.map.ListOrderedMap;

public class SessionSourceManager {

	private static ListOrderedMap<Long, SessionSource> sessionIDToSessionSourceMap = new ListOrderedMap<Long, SessionSource>();
	private static Map<File, SessionSource> sourceDirectoryRootToSessionSourceMap = new HashMap<File, SessionSource>();
	private static long sessionID = 0L;

	public static synchronized SessionSource createInstance(File rootDirectory) throws IOException{
		return new SessionSource(sessionID++, rootDirectory);
	}
	
	public static synchronized SessionSource getInstance(long sessionID) {
		SessionSource sessionSource = sessionIDToSessionSourceMap.get(sessionID);
		if(sessionSource == null){
			throw new NoSuchElementException("SessionID:"+sessionID);
		}
		return sessionSource;
	}
	
	public static synchronized SessionSource getInstance(File rootDirectory) throws IOException{
		return sourceDirectoryRootToSessionSourceMap.get(rootDirectory);
	}

	public static synchronized void putInstance(SessionSource sessionSource) throws IOException{
		sessionIDToSessionSourceMap.put(sessionSource.getSessionID(), sessionSource);
		sourceDirectoryRootToSessionSourceMap.put(sessionSource.getRootDirectory(), sessionSource);
	}

	public static void close(long sessionID) throws IOException{
		SessionSource sessionSource = getInstance(sessionID); 
		if(sessionSource == null){
			throw new RuntimeException("no such session ID:"+sessionID);
		}
		close(sessionSource);
	}
	
	public static void close(SessionSource sessionSource) throws IOException{
		sessionSource.close();
		sessionIDToSessionSourceMap.remove(sessionSource.getSessionID());
		sourceDirectoryRootToSessionSourceMap.remove(sessionSource.getRootDirectory());
	}

	public static void closeAll(boolean clear) throws IOException{
		for(SessionSource sessionSource: sessionIDToSessionSourceMap.values()){
			sessionSource.close();
			if(clear){
				sessionSource.removeResultDirectories();
			}
		}
		sessionIDToSessionSourceMap.clear();
		sourceDirectoryRootToSessionSourceMap.clear();
	}

	public static List<Long> getSessionIDList(){
		return sessionIDToSessionSourceMap.asList();
	}
	
}
