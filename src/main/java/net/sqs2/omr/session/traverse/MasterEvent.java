package net.sqs2.omr.session.traverse;

import net.sqs2.omr.master.FormMaster;

public class MasterEvent extends TraverseEvent {
	SessionSourceEvent sessionSourceEvent;
	FormMaster master;

	public MasterEvent(SessionSourceEvent sessionSourceEvent, FormMaster master, int numEvents) {
		this.sessionSourceEvent = sessionSourceEvent;
		this.master = master;
		this.numEvents = numEvents;
	}

	public MasterEvent(SessionSourceEvent sessionSourceEvent, int numEvents) {
		this(sessionSourceEvent, null, numEvents);
	}

	public SessionSourceEvent getSessionSourceEvent() {
		return this.sessionSourceEvent;
	}

	public void setFormMaster(FormMaster master) {
		this.master = master;
	}

	public FormMaster getFormMaster() {
		return this.master;
	}

}
