package net.sqs2.omr.model;

import static org.testng.Assert.assertEquals;
import net.sqs2.util.FileResourceID;

import org.testng.annotations.Test;


public class PageTaskExceptionTest {
	@Test
	public void testCompareTo() {
		FileResourceID fileResourceID1 = new FileResourceID("/tmp", 0L);
		FileResourceID fileResourceID2 = new FileResourceID("/tmp", 0L);

		assertEquals((fileResourceID1.compareTo(fileResourceID2) == 0), true);

		PageID pageID1 = new PageID(fileResourceID1, 0, 1);
		PageID pageID2 = new PageID(fileResourceID2, 0, 1);

		assertEquals((pageID1.compareTo(pageID2) == 0), true);

		OMRProcessorException taskException1 = new OMRProcessorException(new OMRProcessorErrorModel(pageID1));
		OMRProcessorException taskException2 = new OMRProcessorException(new OMRProcessorErrorModel(pageID2));

		assertEquals((taskException1.compareTo(taskException2) == 0), true);
	}
}
