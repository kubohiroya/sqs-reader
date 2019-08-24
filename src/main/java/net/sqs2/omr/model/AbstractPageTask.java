package net.sqs2.omr.model;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import net.sqs2.util.FileResourceID;

public class AbstractPageTask implements Serializable {

	private static final long serialVersionUID = 2L;

	protected Ticket ticket;
	protected long sessionID = 0L;

	protected FileResourceID configFileResourceID;
	protected OMRProcessorResult result = null;
	
	protected OMRProcessorErrorModel errorModel = null;

	public AbstractPageTask() {
		this.ticket = new Ticket();
	}

	public AbstractPageTask(long sessionID) {
		this();
		this.sessionID = sessionID;
	}

	public AbstractPageTask(FileResourceID configFileResourceID, long sessionID) {
		this(sessionID);
		this.configFileResourceID = configFileResourceID;
	}

	public long getSessionID() {
		return this.sessionID;
	}

	public FileResourceID getConfigFileResourceID() {
		return this.configFileResourceID;
	}

	public OMRProcessorResult getResult() {
		return this.result;
	}

	public OMRProcessorErrorModel getErrorModel() {
		return this.errorModel;
	}

	public void setErrorModel(OMRProcessorErrorModel errorModel) {
		this.errorModel = errorModel;
	}

	public void setResult(OMRProcessorResult result) {
		this.result = result;	
	}
	
	public void setLeased() {
		this.ticket.setLeased();
	}
	
	public long getDelay(TimeUnit unit){
		return this.ticket.getDelay(unit);
	}
}
