package net.sqs2.omr.session.init;

import net.sqs2.omr.model.SessionSource;

public class SessionSourceInitErrorEvent extends SessionSourceInitEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SessionSourceInitErrorEvent(Object source, SessionSource sessionSource) {
		super(source, sessionSource);
	}

}
