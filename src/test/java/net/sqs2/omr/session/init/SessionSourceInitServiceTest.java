package net.sqs2.omr.session.init;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import net.sqs2.event.EventSource;
import net.sqs2.net.ClassURLStreamHandlerFactory;
import net.sqs2.omr.app.SessionTestHelper;
import net.sqs2.omr.app.TestFolderManager;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.master.FormMasterException;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.session.service.SessionStopException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SessionSourceInitServiceTest {

    static File testDirectory1 = TestFolderManager.getFolder("test1"); 
    static File testDirectory2 = TestFolderManager.getFolder("test2");
    static File testDirectory3 = TestFolderManager.getFolder("test3");
    static File testDirectory5 = TestFolderManager.getFolder("test5");
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			URL.setURLStreamHandlerFactory(ClassURLStreamHandlerFactory.getSingleton());
			
		} catch (Error ex) {
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	    SessionTestHelper.closeSessionSources();
        
	}
	
	private SessionSource createAndInitializeSessionSource(File rootDirectory) throws IOException, OMRProcessorException,
		SessionSourceInitException, FormMasterException, SessionStopException, ConfigSchemeException, SessionException {
		SessionSource sessionSource = SessionSourceManager.createInstance(rootDirectory);
		SessionSourceInitService sessionSourceInitService = new SessionSourceInitService(sessionSource, 
				new EventSource<SessionSourceInitEvent> (){
			@Override
			public void fireEvent(SessionSourceInitEvent e) {
				super.fireEvent(e);
			}
		}, false
		);
		sessionSourceInitService.call();
		SessionSourceManager.putInstance(sessionSource);
		return sessionSource;
	}

	@Test
	public void testOnDirectory1(){
		File rootDirectory = testDirectory1.getAbsoluteFile();
		SessionSource sessionSource = null;
		try{
			sessionSource = createAndInitializeSessionSource(rootDirectory);
			
			FormMaster master = sessionSource.getFormMaster(0);

			assertTrue(sessionSource.getRootDirectory().isDirectory());
			assertEquals(sessionSource.getNumFormMasters(), 1);

			Set<SourceDirectory> sourceDirectorySet = sessionSource.getSourceDirectoryRootTreeSet(master);
			
			for(SourceDirectory sourceDirectory: sourceDirectorySet){
				assertEquals(sourceDirectory.getNumChildSourceDirectories(), 0);
				assertEquals(sourceDirectory.getDescendentSourceDirectoryList().size(), 0);
			
				assertEquals(sourceDirectory.getNumPageIDs(), 22);
				assertEquals(sourceDirectory.getNumPageIDsRecursive(), 22);
			
				assertEquals(sourceDirectory.getRelativePath(), "");
				assertEquals(sourceDirectory.getSourceDirectoryRootFile(), rootDirectory);
			}
			
			/*
			PageTaskHolder pageTaskHolder = new PageTaskHolder();
			EventSource<SessionSourceScanEvent> eventSource = new EventSource<SessionSourceScanEvent> (){
				@Override
				public void fireEvent(SessionSourceScanEvent e) {
					super.fireEvent(e);
				}
			};
			SessionSourceScannerTaskProducer producer = new SessionSourceScannerTaskProducer(sessionSource,
					eventSource,
					pageTaskHolder);
			producer.run();
			assertEquals(pageTaskHolder.getNumRemoteLeasedTasks(), 0);
			assertEquals(pageTaskHolder.getNumPreparedTasks(), 22);
			*/
			return;
		}catch(SessionException ex){
			ex.printStackTrace();
		}catch(SessionSourceInitException ex){
			ex.printStackTrace();
		}catch(ConfigSchemeException ex){
			ex.printStackTrace();
		}catch(FormMasterException ex){
			ex.printStackTrace();
		}catch(OMRProcessorException ex){
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}finally{
			if(sessionSource != null){
				try{
				    SessionTestHelper.closeSession(sessionSource);
					
				}catch(IOException ignore){}
			}
		}
		fail();
	}

	@Test
	public void testOnDirectory2(){
		
		File rootDirectory = testDirectory2.getAbsoluteFile();
		SessionSource sessionSource = null;
		try{
			sessionSource = createAndInitializeSessionSource(rootDirectory);
			
			FormMaster master = sessionSource.getFormMaster(0);

			assertTrue(sessionSource.getRootDirectory().isDirectory());
			assertEquals(sessionSource.getNumFormMasters(), 1);

			Set<SourceDirectory> sourceDirectorySet = sessionSource.getSourceDirectoryRootTreeSet(master);
			
			assertEquals(sourceDirectorySet.size(), 1);
			
			for(SourceDirectory sourceDirectory: sourceDirectorySet){
				assertEquals(sourceDirectory.getNumChildSourceDirectories(), 3);
				assertEquals(sourceDirectory.getDescendentSourceDirectoryList().size(), 3);
			
				assertEquals(sourceDirectory.getNumPageIDs(), 0);
				assertEquals(sourceDirectory.getNumPageIDsRecursive(), 50);
			
				assertEquals(sourceDirectory.getRelativePath(), "");
				assertEquals(sourceDirectory.getSourceDirectoryRootFile(), rootDirectory);
				
				SourceDirectory subSourceDirectory0 = sourceDirectory.getChildSourceDirectoryList().get(0);
				assertEquals(subSourceDirectory0.getRelativePath(), "A");
				assertEquals(subSourceDirectory0.getNumChildSourceDirectories(), 0);
				assertEquals(subSourceDirectory0.getNumPageIDs(), 30);
				
				SourceDirectory subSourceDirectory1 = sourceDirectory.getChildSourceDirectoryList().get(1);
				assertEquals(subSourceDirectory1.getRelativePath(), "B");
				assertEquals(subSourceDirectory1.getNumChildSourceDirectories(), 0);
				assertEquals(subSourceDirectory1.getNumPageIDs(), 10);
				
				SourceDirectory subSourceDirectory2 = sourceDirectory.getChildSourceDirectoryList().get(2);
				assertEquals(subSourceDirectory2.getRelativePath(), "C");
				assertEquals(subSourceDirectory2.getNumChildSourceDirectories(), 0);
				assertEquals(subSourceDirectory2.getNumPageIDs(), 10);
			}
			
			return;
		}catch(SessionException ex){
			ex.printStackTrace();
		}catch(SessionSourceInitException ex){
			ex.printStackTrace();
		}catch(ConfigSchemeException ex){
			ex.printStackTrace();
		}catch(FormMasterException ex){
			ex.printStackTrace();
		}catch(OMRProcessorException ex){
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}finally{
			if(sessionSource != null){
				try{
					sessionSource.close();
				}catch(IOException ignore){}
			}
		}
		fail();
	}

	@Test
	public void testOnDirectory3WithNewFormat(){
		
		File rootDirectory = testDirectory3.getAbsoluteFile();
		SessionSource sessionSource = null;
		try{
			sessionSource = createAndInitializeSessionSource(rootDirectory);
			

			assertTrue(sessionSource.getRootDirectory().isDirectory());
			assertEquals(1, sessionSource.getNumFormMasters());
			
			FormMaster master = sessionSource.getFormMaster(0);
			
			Set<SourceDirectory> rootSourceDirectorySet = sessionSource.getSourceDirectoryRootTreeSet(master);
			for(SourceDirectory rootSourceDirectory: rootSourceDirectorySet){
				assertEquals(rootSourceDirectory.getSourceDirectoryRootFile(), rootDirectory);
				assertEquals(rootSourceDirectory.getRelativePath(), "");
				assertEquals(rootSourceDirectory.getNumChildSourceDirectories(), 0);
				assertEquals(rootSourceDirectory.getDescendentSourceDirectoryList().size(), 0);
				assertEquals(rootSourceDirectory.getNumPageIDs(), 1);
				assertEquals(rootSourceDirectory.getNumPageIDsRecursive(), 1);
			}
			return;
		}catch(FormMasterException ex){
			ex.printStackTrace();
		}catch(SessionException ex){
			ex.printStackTrace();
		}catch(SessionSourceInitException ex){
			ex.printStackTrace();
		}catch(ConfigSchemeException ex){
			ex.printStackTrace();
		}catch(OMRProcessorException ex){
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}finally{
			try{
				if(sessionSource != null){
					sessionSource.close();
				}
			}catch(IOException ignore){
				ignore.printStackTrace();
			}
		}
		fail();
	}
	
	@Test
	public void testOnDirectory5(){
		
		File rootDirectory = testDirectory5.getAbsoluteFile();
		SessionSource sessionSource = null;
		try{
			sessionSource = createAndInitializeSessionSource(rootDirectory);
			
			assertTrue(sessionSource.getRootDirectory().isDirectory());
			assertEquals(1, sessionSource.getNumFormMasters());
			
			FormMaster master = sessionSource.getFormMaster(0);
			
			Set<SourceDirectory> rootSourceDirectorySet = sessionSource.getSourceDirectoryRootTreeSet(master);
			for(SourceDirectory rootSourceDirectory: rootSourceDirectorySet){
				assertEquals(rootSourceDirectory.getSourceDirectoryRootFile(), rootDirectory);
				assertEquals(rootSourceDirectory.getRelativePath(), "");
				assertEquals(rootSourceDirectory.getNumChildSourceDirectories(), 0);
				assertEquals(rootSourceDirectory.getDescendentSourceDirectoryList().size(), 0);
				assertEquals(rootSourceDirectory.getNumPageIDs(), 4);
				assertEquals(rootSourceDirectory.getNumPageIDsRecursive(), 4);
				assertEquals(rootSourceDirectory.getCurrentFormMaster().getFileResourceID().getRelativePath(), "pi-classreview2011a-gairon.pdf");
				assertEquals(rootSourceDirectory.getPageID(0).getFileResourceID().getRelativePath(), "001.tif");
				assertEquals(rootSourceDirectory.getPageID(1).getFileResourceID().getRelativePath(), "002.tif");
				assertEquals(rootSourceDirectory.getPageID(2).getFileResourceID().getRelativePath(), "313.tif");
				assertEquals(rootSourceDirectory.getPageID(3).getFileResourceID().getRelativePath(), "314.tif");
			}
			return;
		}catch(FormMasterException ex){
			ex.printStackTrace();
		}catch(SessionException ex){
			ex.printStackTrace();
		}catch(SessionSourceInitException ex){
			ex.printStackTrace();
		}catch(ConfigSchemeException ex){
			ex.printStackTrace();
		}catch(OMRProcessorException ex){
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}finally{
			try{
				if(sessionSource != null){
					sessionSource.close();
				}
			}catch(IOException ignore){
				ignore.printStackTrace();
			}
		}
		fail();
	}
	
	/**
	 * 	case: no pdf warning must be tested.
	 */

	/**
	 * 	case: no image warning must be tested.
	 */

	/**
	 * 	case: non-writable directory warning must be tested.
	 */
	
}
