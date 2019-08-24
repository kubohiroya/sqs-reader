package net.sqs2.omr.session.init;

import net.sqs2.omr.model.SessionSource;

public class NoFormMasterFoundErrorEvent extends SessionSourceInitErrorEvent {

	private static final long serialVersionUID = 1L;
	
	public NoFormMasterFoundErrorEvent(Object source, SessionSource sessionSource) {
		super(source, sessionSource);
	}
	
	@Override
	public String toString(){
		return this.getClass().getName()+":"+sessionSource.getRootDirectory().getAbsolutePath();
	}
}
