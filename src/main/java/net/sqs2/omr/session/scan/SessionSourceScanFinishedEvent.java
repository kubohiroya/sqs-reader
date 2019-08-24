package net.sqs2.omr.session.scan;

import net.sqs2.omr.model.SessionSource;

public class SessionSourceScanFinishedEvent extends SessionSourceScanEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SessionSourceScanFinishedEvent(SessionSource sessionSource) {
		super(sessionSource);
	}

}
