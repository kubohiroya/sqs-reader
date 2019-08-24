package net.sqs2.omr.app;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import net.sqs2.event.EventListener;
import net.sqs2.net.ClassURLStreamHandlerFactory;
import net.sqs2.omr.model.SessionSourcePhase;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.session.scan.PageTaskProducedEvent;
import net.sqs2.omr.session.scan.SessionSourceScanEvent;
import net.sqs2.omr.session.service.MarkReaderSession;
import net.sqs2.omr.session.service.SessionEvent;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class MarkReaderAppTest {

    public final File sourceDirectoryRoot0 = TestFolderManager.getFolder( "test0");
    public final File sourceDirectoryRoot1 = TestFolderManager.getFolder( "test1");
    public final File sourceDirectoryRoot2 = TestFolderManager.getFolder( "test2");
    public final File sourceDirectoryRoot3 = TestFolderManager.getFolder( "test3");
	public final File sourceDirectoryRoot4 = TestFolderManager.getFolder( "test4");
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try{
			URL.setURLStreamHandlerFactory(ClassURLStreamHandlerFactory.getSingleton());
		} catch (Error ignore) {
		}
	}
	
	@AfterClass
	public static void afterClass()throws Exception{
	    SessionTestHelper.closeSessionSources();
        
	}

	protected synchronized void startAndCloseSession(final File sourceDirectoryRoot, final Runnable callback) throws IOException, InterruptedException,
		ExecutionException {
		final MarkReaderApp app = new MarkReaderApp(1099);
		MarkReaderSession session = app.createSession(sourceDirectoryRoot, false);
		
		session.getSessionSourceScanEventSource().addListener(new EventListener<SessionSourceScanEvent>() {
			@Override
			public void eventFired(SessionSourceScanEvent event) {
				if(event instanceof PageTaskProducedEvent){
					PageTaskProducedEvent ev = (PageTaskProducedEvent) event;
					System.err.println(ev.getPageTask().getID());
				}
			}
		});
		
		session.getSessionEventSource().addListener(new EventListener<SessionEvent>() {
			@Override
			public void eventFired(SessionEvent event) {
				if(event.getPhase().getSessionRunningPhase() == SessionSourcePhase.Phase.done){
					app.closeSessionSource(sourceDirectoryRoot);
					app.shutdown();
					
					if(callback != null){
						callback.run();
					}
					synchronized(MarkReaderAppTest.this){
						MarkReaderAppTest.this.notify();
					}
				}
			}
		});
		session.startSession(false);
		try{
			wait();
		}catch(InterruptedException ignore){}
	}

}
