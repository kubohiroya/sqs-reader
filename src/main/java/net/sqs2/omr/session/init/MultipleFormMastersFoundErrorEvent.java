package net.sqs2.omr.session.init;

import java.util.List;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.SessionSource;

public class MultipleFormMastersFoundErrorEvent extends SessionSourceInitErrorEvent {

	private static final long serialVersionUID = 1L;
	List<FormMaster> formMasters;
	
	public MultipleFormMastersFoundErrorEvent(Object source, SessionSource sessionSource, List<FormMaster> formMasters) {
		super(source, sessionSource);
		this.formMasters = formMasters;
	}
}
