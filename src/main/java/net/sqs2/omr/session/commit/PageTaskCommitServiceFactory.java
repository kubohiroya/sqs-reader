package net.sqs2.omr.session.commit;

import java.io.File;
import java.io.IOException;

import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.session.service.MarkReaderSession;

public interface PageTaskCommitServiceFactory {

	public abstract AbstractPageTaskCommitService create(File sourceDirectoryRoot, MarkReaderSession session) throws OMRProcessorException, IOException;

}
