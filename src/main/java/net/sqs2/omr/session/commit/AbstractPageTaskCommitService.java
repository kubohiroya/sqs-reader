package net.sqs2.omr.session.commit;

import java.io.IOException;
import java.util.concurrent.Callable;

import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.SessionSourcePhase;
import net.sqs2.omr.session.service.MarkReaderSession;

public abstract class AbstractPageTaskCommitService implements PageTaskCommitService, Callable<Integer> {

	protected MarkReaderSession markReaderSession;
	protected int numCommitedPageTask;
	
	public AbstractPageTaskCommitService(MarkReaderSession markReaderSession) throws IOException {
		this.markReaderSession = markReaderSession;
		this.numCommitedPageTask = 0;
	}

	public Integer call() throws IOException {
		while (true) {
			PageTask task = this.markReaderSession.getTaskHolder().pollSubmittedTask();
			if (task != null) {
				commit(task);
				numCommitedPageTask++;
				this.markReaderSession.notifyStoringTask(task);
			} else if (this.canFinish()) {
				break;
			} else {
				// no submitted tasks and still remains leased tasks, wait 200 msec.
				try {
					Thread.sleep(200);
				} catch (InterruptedException ignore) {
				}
				continue;
			}
			//Logger.getLogger(getClass().getName()).info(this.markReaderSession.toString());
		}
		return this.numCommitedPageTask;
	}

	public boolean canFinish() throws IOException {
		SessionSourcePhase.Phase phase = markReaderSession.getSessionSourcePhase().getScanningPhase();
		boolean hasScanned =  phase == SessionSourcePhase.Phase.done;
		boolean failInScanning =  phase == SessionSourcePhase.Phase.fail;
		boolean stopInScanning =  phase == SessionSourcePhase.Phase.stop;
		
		boolean taskHolderIsEmpty = this.markReaderSession.getTaskHolder().isEmpty();
		if ((hasScanned && taskHolderIsEmpty) || failInScanning || stopInScanning){
			return true;
		}
		return false;
	}

	public abstract void commit(PageTask task) throws IOException;
	
}
