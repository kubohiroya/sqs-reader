package net.sqs2.omr.session.commit;

import java.util.EventObject;

import net.sqs2.omr.model.PageTask;

public class PageTaskCommittedEvent extends EventObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PageTask pageTask;
		
	public PageTaskCommittedEvent(Object source, PageTask pageTask) {
		super(source);
		this.pageTask = pageTask;
	}

	public PageTask getPageTask() {
		return pageTask;
	}

}
