package net.sqs2.omr.session.scan;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourcePhase;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.session.service.SessionStopException;
import net.sqs2.util.FileResourceID;

public abstract class PageHandlingSessionSourceScanner extends SessionSourceScanner {

	public PageHandlingSessionSourceScanner(SessionSource sessionSource) {
		super(sessionSource);
	}

	protected int scanMasters() throws SessionStopException, IOException {
		int numPages = 0;
		AbstractSessionSourceScannerWorker worker = createWorker();
		
		
		for (int formMasterIndex = 0; formMasterIndex < this.sessionSource.getNumFormMasters(); formMasterIndex++) {
			FormMaster formMaster = this.sessionSource.getFormMaster(formMasterIndex);
			numPages = formMaster.getNumPages();

			Set<SourceDirectory> sourceDirectorySet = this.sessionSource.getSourceDirectoryRootTreeSet(formMaster);
			for(SourceDirectory sourceDirectory: sourceDirectorySet){
				numPages += scanSourceDirectories(worker, numPages, sourceDirectory);
			}
		}
 
		worker.finishScanning();
		return numPages;
	}

	private int scanSourceDirectories(AbstractSessionSourceScannerWorker worker, int numPages, SourceDirectory sourceDirectory) throws SessionStopException {

		int pageIDIndex = 0;
		try{
			worker.startScanningSourceDirectory(sourceDirectory);
			Collection<SourceDirectory> sourceDirectoryList = sourceDirectory.getChildSourceDirectoryList();
			if(sourceDirectoryList != null){
				for(SourceDirectory childSourceDirectory: sourceDirectoryList){
					if(childSourceDirectory.getCurrentFormMaster().equals(sourceDirectory.getCurrentFormMaster())){
						pageIDIndex += scanSourceDirectories(worker, numPages, childSourceDirectory);
					}
				}
			}

			List<PageID> pageIDList = sourceDirectory.getPageIDList();
			if (pageIDList == null) {
				return pageIDIndex;
			}

			for (PageID pageID : pageIDList) {
				int rowIndex = pageIDIndex;
				int pageIndex = (pageIDIndex % numPages);

				if (this.sessionSource.getSessionSourcePhase().hasStopped()) {
					throw new SessionStopException(this.sessionSource.getRootDirectory());
				}
				sessionSource.putRowIndex(sourceDirectory.getRelativePath(), pageID, rowIndex);
				FileResourceID formMasterFileResourceID = sourceDirectory.getCurrentFormMaster().getFileResourceID();
				worker.work(sourceDirectory, pageID, formMasterFileResourceID, pageIndex);
				pageIDIndex++;
			}
			
		}finally{
				//Logger.getLogger(getClass().getName()).info("scanning done : " + sourceDirectory.getRelativePath() + " " + pageIDIndex);
		}
		return pageIDIndex;
	}

}
