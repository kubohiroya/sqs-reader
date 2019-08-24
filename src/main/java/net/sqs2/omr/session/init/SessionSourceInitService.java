/**
 * 
 */
package net.sqs2.omr.session.init;

import java.io.IOException;
import java.util.concurrent.Callable;

import net.sqs2.event.EventSource;
import net.sqs2.omr.master.FormMasterException;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.session.service.SessionStopException;

public class SessionSourceInitService implements Callable<Integer>{

	SessionSourceInitializer sessionSourceInitializer;
	EventSource<SessionSourceInitEvent> eventSource;

	public SessionSourceInitService(SessionSource sessionSource)
	throws IOException, SessionSourceInitException, FormMasterException, SessionStopException {
		this(sessionSource, null, false);
	}

	public SessionSourceInitService(SessionSource sessionSource, 
			EventSource<SessionSourceInitEvent> eventSource,
			boolean enableSearchPageMasterFromAncestorDirectory)
	throws IOException, SessionSourceInitException, FormMasterException, SessionStopException {
		this.sessionSourceInitializer = new SessionSourceInitializer(sessionSource,
				eventSource, new MultiSourceFormMasterFactory(),
				enableSearchPageMasterFromAncestorDirectory);
	}

	/**
	 * @return number of Image files found in this initialization of this SessionSource
	 */
	public Integer call()throws SessionException, FormMasterException, ConfigSchemeException, SessionSourceInitException, SessionStopException, IOException{		
		if(this.sessionSourceInitializer == null){
			throw new RuntimeException("SessionSourceFactory cannot reused.");
		}
		int numAddedImages = this.sessionSourceInitializer.call();
		this.sessionSourceInitializer = null;
		return numAddedImages;
	}

}