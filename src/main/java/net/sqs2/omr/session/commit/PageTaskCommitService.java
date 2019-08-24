package net.sqs2.omr.session.commit;

import java.io.File;
import java.io.IOException;

import net.sqs2.omr.model.PageTask;

public interface PageTaskCommitService{
	public void setup(File sourceDirectoryRoot) throws IOException;
	public void commit(PageTask task) throws IOException;
}
