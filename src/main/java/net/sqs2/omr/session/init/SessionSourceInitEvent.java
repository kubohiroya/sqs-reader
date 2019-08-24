package net.sqs2.omr.session.init;

import java.util.EventObject;

import net.sqs2.omr.model.SessionSource;

public class SessionSourceInitEvent extends EventObject {

	/**
	 * 
	 */
	SessionSource sessionSource;
	private static final long serialVersionUID = 1L;

	public SessionSourceInitEvent(Object source, SessionSource sessionSource) {
		super(source);
		this.sessionSource = sessionSource;
	}

	public SessionSource getSessionSource() {
		return sessionSource;
	}

}
