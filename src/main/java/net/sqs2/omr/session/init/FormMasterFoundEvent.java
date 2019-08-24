package net.sqs2.omr.session.init;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.SessionSource;

public class FormMasterFoundEvent extends SessionSourceInitEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	FormMaster master;
	public FormMasterFoundEvent(Object source, SessionSource sessionSource, FormMaster master) {
		super(source, sessionSource);
		this.master = master;
	}
	public FormMaster getMaster() {
		return master;
	}

}
