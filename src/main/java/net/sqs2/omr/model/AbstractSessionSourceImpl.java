package net.sqs2.omr.model;

import java.io.File;


public class AbstractSessionSourceImpl implements AbstractSessionSource {
	protected long sessionID;
	protected File rootDirectory;
	protected SessionSourcePhase sessionSourcePhase;

	public AbstractSessionSourceImpl(long sessionID, File rootDirectory){
		this.sessionID = sessionID;
		this.rootDirectory = rootDirectory;
		this.sessionSourcePhase = new SessionSourcePhase();
	}

	/* (non-Javadoc)
	 * @see net.sqs2.omr.session.source.IA#getSessionID()
	 */
	public long getSessionID() {
		return this.sessionID;
	}

	/* (non-Javadoc)
	 * @see net.sqs2.omr.session.source.IA#getRootDirectory()
	 */
	public File getRootDirectory() {
		return this.rootDirectory;
	}

	/* (non-Javadoc)
	 * @see net.sqs2.omr.session.source.IA#getSessionSourceState()
	 */
	public SessionSourcePhase getSessionSourcePhase() {
		return this.sessionSourcePhase;
	}
}
