package net.sqs2.omr.session.init;

import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;

public class SessionSourceInitDirectoryEvent extends SessionSourceInitEvent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SourceDirectory sourceDirectory;
	
	public static final int STARTED = 0;
	public static final int DONE = 1;
	public static final int STOPPED = 2;
	public static final int INFO = 8;
	
	int phase;

	public SessionSourceInitDirectoryEvent(Object source, SessionSource sessionSource, SourceDirectory sourceDirectory, int phase){
		super(source, sessionSource);
		this.sourceDirectory = sourceDirectory;
		this.phase = phase;
	}
	
	public SourceDirectory getSourceDirectory() {
		return sourceDirectory;
	}

	public int getPhase(){
		return phase;
	}
}
