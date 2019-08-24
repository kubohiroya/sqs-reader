package net.sqs2.omr.session.scan;

import java.io.IOException;
import java.util.concurrent.Callable;

import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.session.service.SessionStopException;
import net.sqs2.util.FileResourceID;

public abstract class SessionSourceScanner  implements Callable<Integer>{

	protected SessionSource sessionSource;

	protected static abstract class AbstractSessionSourceScannerWorker {
		abstract void startScanningSourceDirectory(SourceDirectory sourceDirectory);
	
		abstract void work(SourceDirectory sourceDirectory, PageID pageID, FileResourceID formMasterFileResourceID, int pageIndex) throws SessionStopException;
	
		abstract void finishScanning();
	}

	protected abstract AbstractSessionSourceScannerWorker createWorker() throws IOException;

	public SessionSourceScanner(SessionSource sessionSource) {
		this.sessionSource = sessionSource;
	}

	public Integer call() {
		try {
			return scanMasters();
		} catch (SessionStopException ignore) {
			return 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	
	protected abstract int scanMasters() throws SessionStopException, IOException;

}