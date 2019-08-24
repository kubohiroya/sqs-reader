package net.sqs2.omr.session.init;

public class SessionSourceInitException extends Exception {

	private static final long serialVersionUID = 1L;

	SessionSourceInitEvent sessionSourceInitEvent;
	
	public SessionSourceInitException(SessionSourceInitEvent sessionSourceInitEvent) {
		this.sessionSourceInitEvent = sessionSourceInitEvent;
	}

	public SessionSourceInitEvent getSessionSourceInitEvent(){
		return sessionSourceInitEvent;
	}
	
	@Override
	public String getMessage(){
		return this.sessionSourceInitEvent.toString();
	}

}
