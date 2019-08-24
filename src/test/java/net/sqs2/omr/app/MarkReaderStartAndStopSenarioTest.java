package net.sqs2.omr.app;

import org.testng.annotations.Test;

public class MarkReaderStartAndStopSenarioTest extends MarkReaderAppTest {

	@Test
	public void testStartCloseAndStart() throws Exception {
		
		synchronized (this) {
			startAndCloseSession(sourceDirectoryRoot1, new Runnable() {
				public void run() {
					try {
						startAndCloseSession(sourceDirectoryRoot1,
								new Runnable() {
									public void run() {
										synchronized (MarkReaderStartAndStopSenarioTest.this) {
											MarkReaderStartAndStopSenarioTest.this.notify();						
										}
									}
								});
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			try{
				wait();
			}catch(InterruptedException ignore){}
		}

	}
}
