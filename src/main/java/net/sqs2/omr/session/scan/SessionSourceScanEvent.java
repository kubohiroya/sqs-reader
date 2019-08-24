package net.sqs2.omr.session.scan;

import net.sqs2.omr.model.SessionSource;

public class SessionSourceScanEvent extends java.util.EventObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SessionSourceScanEvent(SessionSource sessionSource){
		super(sessionSource);
	}

}
