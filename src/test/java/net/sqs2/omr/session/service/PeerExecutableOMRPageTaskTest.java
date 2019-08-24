package net.sqs2.omr.session.service;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import net.sqs2.image.ImageFactory;
import net.sqs2.net.ClassURLStreamHandlerFactory;
import net.sqs2.omr.app.SessionTestHelper;
import net.sqs2.omr.app.TestFolderManager;
import net.sqs2.omr.app.deskew.AssertShape;
import net.sqs2.omr.app.deskew.DeskewGuideAreaPair;
import net.sqs2.omr.app.deskew.DeskewGuideAreaPairFactory;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.master.FormMasterException;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.DeskewGuideAreaConfig;
import net.sqs2.omr.model.FormAreaResult;
import net.sqs2.omr.model.MarkAreaResult;
import net.sqs2.omr.model.MarkReaderConstants;
import net.sqs2.omr.model.OMRPageTask;
import net.sqs2.omr.model.OMRProcessorErrorModel;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.OMRProcessorResult;
import net.sqs2.omr.model.PageAreaResult;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.PageTask;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.model.SourceConfig;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.session.daemon.SessionSourceServerDispatcher;
import net.sqs2.omr.session.init.SessionSourceInitException;
import net.sqs2.omr.session.init.SessionSourceInitService;
import net.sqs2.omr.session.server.RemoteSessionSourceServer;
import net.sqs2.omr.session.server.SessionSourceServerDispatcherImpl;
import net.sqs2.omr.session.server.SessionSourceServerImpl;
import net.sqs2.util.FileResourceID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class PeerExecutableOMRPageTaskTest {

    static final File[] SOURCE_DIRECTORY_ARRAY = new File[] {
    		/*
        TestFolderManager.getFolder("test0"),
        TestFolderManager.getFolder("test1"),
        TestFolderManager.getFolder("test2"),
        TestFolderManager.getFolder("test3"),
        */
        //TestFolderManager.getFolder("test4")
    };

	static final long KEY = 0L;
	
	static SessionSourceServerDispatcher sessionSourceServerDispatcher;
	
	public PeerExecutableOMRPageTaskTest (){}
	
	@BeforeClass
	public static void beforeClass()throws Exception{
		try{
			URL.setURLStreamHandlerFactory(ClassURLStreamHandlerFactory.getSingleton());
		}catch(Error ignore){}
		
		for(int i  = 0; i < SOURCE_DIRECTORY_ARRAY.length; i++) {
			File sourceDirectory = SOURCE_DIRECTORY_ARRAY[i];
			SessionSource sessionSource = SessionSourceManager.createInstance(sourceDirectory);
			new SessionSourceInitService(sessionSource).call();
			SessionSourceManager.putInstance(sessionSource);
		}
		RemoteSessionSourceServer sessionSourceServer = SessionSourceServerImpl.createInstance(KEY, MarkReaderConstants.CLIENT_TIMEOUT_SEC);
		sessionSourceServerDispatcher = new SessionSourceServerDispatcherImpl(sessionSourceServer, null, KEY);
	}

	@AfterClass
	public static void afterClass()throws Exception{
	    SessionTestHelper.closeSessionSources();
		sessionSourceServerDispatcher.close();
	}

	private void assertMarkValues(FormAreaResult formAreaResult, String id, int width, int height, float density, float delta)throws IOException{
		assertEquals(id, formAreaResult.getID());
		BufferedImage markarea1 = ImageFactory.createImage(formAreaResult.getImageType(), formAreaResult.getImageByteArray());
		assertEquals( markarea1.getWidth(), width);
		assertEquals(markarea1.getHeight(), height);
		assertEquals(((MarkAreaResult)formAreaResult).getDensity(), density , delta);
	}
	
	private DeskewGuideAreaPair createDeskewGuideAreaPair(File sourceDirectoryRoot, String imageFilePath)throws ConfigSchemeException, IOException, FormMasterException, OMRProcessorException{
		SessionSource sessionSource = SessionSourceManager.getInstance(sourceDirectoryRoot);
		File imageFile = new File(sessionSource.getRootDirectory().getAbsoluteFile(), imageFilePath);
		FileResourceID imageFileResourceID = new FileResourceID(imageFilePath, imageFile.lastModified());
		int indexInFile = 0;
		int numPagesInFile = 1;

		BufferedImage image = ImageFactory.createImage(imageFile, indexInFile);
		PageID pageID = new PageID(imageFileResourceID, indexInFile, numPagesInFile);
		
		SourceDirectory sourceDirectory = sessionSource.getSourceDirectory(pageID);

		SourceConfig sourceConfig = sourceDirectory.getConfiguration().getConfig().getSourceConfig(imageFilePath, indexInFile);
		DeskewGuideAreaConfig deskewGuideAreaConfig = sourceConfig.getFrameConfig().getDeskewGuideAreaConfig();
		FormMaster formMaster = sourceDirectory.getCurrentFormMaster(); 
		
		DeskewGuideAreaPairFactory deskewGuideAreaPairFactory = new DeskewGuideAreaPairFactory(deskewGuideAreaConfig,
				formMaster,
				image, 
				pageID);
		
		return deskewGuideAreaPairFactory.create();
	}
	
	private OMRPageTask execute(
			File sourceDirectoryRootDirectory,
			String formMassterFilePath, 
			String configFilePath,
			String imageFilePath, 
			int indexofPageInImageFile, 
			int numPagesInImageFile,
			int processingPageIndex) throws RemoteException, IOException, OMRProcessorException, SessionSourceInitException, FormMasterException, SessionStopException, ConfigSchemeException {

		SessionSource sessionSource = SessionSourceManager.getInstance(sourceDirectoryRootDirectory);
		
		File sourceDirectoryRoot = sessionSource.getRootDirectory();
		File imageFile = new File(sourceDirectoryRoot, imageFilePath);
		File masterFile = new File(sourceDirectoryRoot, formMassterFilePath);
		File configFile = new File(sourceDirectoryRoot, configFilePath);

		FileResourceID formMasterFileResourceID = new FileResourceID(formMassterFilePath,masterFile.lastModified());
		FileResourceID configFileResourceID = new FileResourceID(configFilePath, configFile.lastModified());
		FileResourceID imageFileResourceID = new FileResourceID(imageFilePath, imageFile.lastModified());

		PageID pageID = new PageID(imageFileResourceID, indexofPageInImageFile, numPagesInImageFile);
		
		OMRPageTask pageTask = new OMRPageTask(sessionSource.getSessionID(),
				pageID, 
				configFileResourceID, 
				formMasterFileResourceID, 
				processingPageIndex);

		PeerExecutableOMRPageTask task = new PeerExecutableOMRPageTask(pageTask, sessionSourceServerDispatcher);
		task.execute();
		
		return pageTask;
	}

	
	//@Test
	public synchronized void testExecuteOnTest0()throws Exception{

		String formMasterFilePath = "form.pdf";
		String configFilePath = AppConstants.RESULT_DIRNAME+File.separatorChar+"config.xml";
		String imageFilePath = "a001.tif";
		int processingPageIndex = 0;
		int indexInFile = 0;
		int numPagesInFile = 1;
		
		DeskewGuideAreaPair pair = createDeskewGuideAreaPair(SOURCE_DIRECTORY_ARRAY[0], imageFilePath);
		pair.getDeskewGuideCenterPoints();
		
		
		OMRPageTask pageTask = execute(SOURCE_DIRECTORY_ARRAY[0], formMasterFilePath, configFilePath, imageFilePath,
				indexInFile, numPagesInFile, processingPageIndex);
		
		OMRProcessorResult result = pageTask.getResult();
		OMRProcessorErrorModel errorModel = pageTask.getErrorModel();
		assertNull(errorModel);
		
		Point2D[] deskewGuideCenterPoints = result.getDeskewGuideCenterPoints();
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[0], new Point2D.Float(220.1f, 81.0f), 3,4);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[1], new Point2D.Float(1015.6f, 88.0f),3,4);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[2], new Point2D.Float(194.2f,1648.0f),3,4);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[3], new Point2D.Float(986.0f,1655.0f),3,4);
		
		List<PageAreaResult> pageAreaResultList = result.getPageAreaResultList();
		assertEquals(pageAreaResultList.size(), 2);
	}

	@Test
	public  void testDummy(){
		assertEquals(true, true);
	}
	
	//@Test
	public synchronized void testExecuteOnTest3()throws Exception{

		String masterPath = "form.pdf";
		String configPath = AppConstants.RESULT_DIRNAME+File.separatorChar+"config.xml";

		String imageFilePath = "001.png";
		int processingPageIndex = 0;
		int indexInFile = 0;
		int numPagesInFile = 1;
		
		DeskewGuideAreaPair pair = createDeskewGuideAreaPair(SOURCE_DIRECTORY_ARRAY[3], imageFilePath);
		pair.getDeskewGuideCenterPoints();
		
		OMRPageTask pageTask = execute(SOURCE_DIRECTORY_ARRAY[3], masterPath, configPath, imageFilePath,
				indexInFile, numPagesInFile, processingPageIndex);
		
		OMRProcessorResult result = pageTask.getResult();
		OMRProcessorErrorModel errorModel = pageTask.getErrorModel();
		assertNull(errorModel);

		Point2D[] deskewGuideCenterPoints = result.getDeskewGuideCenterPoints();
		
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[0], new Point2D.Float(111.34f,52.6f), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[1], new Point2D.Float(501.56f,62.699997f),1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[2], new Point2D.Float(91.65f,794.86664f),1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[3], new Point2D.Float(480.08f, 804.9667f),1,3);
		
		List<PageAreaResult> pageAreaResultList = result.getPageAreaResultList();
		assertEquals(pageAreaResultList.size(), 2);
	}
	
	//@Test
	public void testExecuteOnA001()throws Exception{
		
		String formMasterFilePath = "form.pdf";
		String configFilePath = AppConstants.RESULT_DIRNAME+File.separatorChar+"config.xml";
		String imageFilePath = "a001.tif";
		int indexOfPageInImageFile = 0;
		int numPagesInImageFile = 1;
		
		int processingPageIndex = 0;
		
		DeskewGuideAreaPair pair = createDeskewGuideAreaPair(SOURCE_DIRECTORY_ARRAY[0], imageFilePath);
		pair.getDeskewGuideCenterPoints();
		Point2D[] deskewGuideCenterPoints0 = pair.getDeskewGuideCenterPoints();
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints0[0], new Point2D.Float(220.1f,81), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints0[1], new Point2D.Float(1015.6f,88), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints0[2], new Point2D.Float(194.2f,1648),1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints0[3], new Point2D.Float(986,1655),1,3);
		
		
		PageTask pageTask = execute(SOURCE_DIRECTORY_ARRAY[0], formMasterFilePath, configFilePath, imageFilePath, 
				indexOfPageInImageFile, numPagesInImageFile,
				processingPageIndex);
		
		OMRProcessorResult result = pageTask.getResult();
		OMRProcessorErrorModel errorModel = pageTask.getErrorModel();
		assertNull(errorModel);
		
		Point2D[] deskewGuideCenterPoints = result.getDeskewGuideCenterPoints();
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[0], new Point2D.Float(220.1f,81), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[1], new Point2D.Float(1015.6f,88), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[2], new Point2D.Float(194.2f,1648), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[3], new Point2D.Float(986.0f,1655), 1,3);
		
		List<PageAreaResult> pageAreaResultList = result.getPageAreaResultList();
		assertEquals(pageAreaResultList.size(), 2);
		
		FormAreaResult formAreaResult0 = (FormAreaResult)pageAreaResultList.get(0);
		assertEquals(formAreaResult0.getID(), "textarea1");
		
		BufferedImage textarea1 = ImageFactory.createImage(formAreaResult0.getImageType(), formAreaResult0.getImageByteArray());
		assertEquals(textarea1.getWidth(), 454);
		assertEquals(textarea1.getHeight(), 84);
		
		FormAreaResult formAreaResult1 = (FormAreaResult)pageAreaResultList.get(1);
		assertEquals(formAreaResult1.getID(), "textarea2");
		
		BufferedImage textarea2 = ImageFactory.createImage(formAreaResult1.getImageType(), formAreaResult1.getImageByteArray());
		assertEquals(textarea2.getWidth(), 454);
		assertEquals(textarea2.getHeight(), 84);

	}

	//@Test
	public void testExecuteOnA002()throws Exception{

		String formMasterFilePath = "form.pdf";
		String configFilePath = AppConstants.RESULT_DIRNAME+File.separatorChar+"config.xml";
		
		String imageFilePath = "a002.tif";
		int indexOfPageInImageFile = 0;
		int numPagesInImageFile = 1;
		int processingPageIndex = 1;
		
		DeskewGuideAreaPair pair = createDeskewGuideAreaPair(SOURCE_DIRECTORY_ARRAY[0], imageFilePath);
		pair.getDeskewGuideCenterPoints();
		
		PageTask pageTask = execute(SOURCE_DIRECTORY_ARRAY[0], formMasterFilePath, configFilePath, imageFilePath, indexOfPageInImageFile, numPagesInImageFile, processingPageIndex);
		
		OMRProcessorResult pageTaskResult = pageTask.getResult();
		assertNotNull(pageTaskResult);
		
		Point2D[] deskewGuideCenterPoints = pageTaskResult.getDeskewGuideCenterPoints();
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[0], new Point2D.Float(223.8f,88), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[1], new Point2D.Float(1015.6f,88), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[2], new Point2D.Float(220.1f,1648), 1,3);
		AssertShape.assertEqualsPoint2D(deskewGuideCenterPoints[3], new Point2D.Float(1011.9f,1648), 1,3);
		
		List<PageAreaResult> pageAreaResultsList = pageTaskResult.getPageAreaResultList();
		assertEquals(76, pageAreaResultsList.size()); // this image contains 76 answer fields
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(0), "mark3/0", 13, 24, 1.0f, 0.1f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(1), "mark3/1", 13, 24, 1.0f, 0.1f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(2), "mark3/2", 13, 24, 0.5f, 0.2f); // marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(3), "mark3/3", 13, 24, 1.0f, 0.1f); // not marked
	}

	/*
	@Test
	public void testOMRLogicOnTest4()throws Exception{
		
		String formMasterFilePath = "form.pdf";
		String configFilePath = AppConstants.RESULT_DIRNAME+File.separatorChar+"config.xml";
		
		String imageFilePath = "04.tif";
		int indexOfPageInImageFile = 0;
		int numPagesInImageFile = 1;
		int processingPageIndex = 1;
		long sessionID = 4L;
		
		PageTask pageTask = executeOMRTask(sessionID, formMasterFilePath, configFilePath, imageFilePath, indexOfPageInImageFile, numPagesInImageFile,
				processingPageIndex);
		PageTaskResult result = pageTask.getPageTaskResult();
																																			
		PageTaskErrorModel errorModel = pageTask.getErrorModel();
		assertNull(errorModel);	
		
		PageTaskResult pageTaskResult = pageTask.getPageTaskResult();
		assertNotNull(pageTaskResult);
		
		Point2D[] deskewGuideCenterPoints = pageTaskResult.getDeskewGuideCenterPoints();
		assertEquals(deskewGuideCenterPoints[0], new Point(207,84));
		assertEquals(deskewGuideCenterPoints[1], new Point(1013,84));
		assertEquals(deskewGuideCenterPoints[2], new Point(207,1678));
		assertEquals(deskewGuideCenterPoints[3], new Point(1013,1678));
		
		List<PageAreaResult> pageAreaResultsList = pageTaskResult.getPageAreaResultList();
		assertEquals(18, pageAreaResultsList.size()); // this image contains 18 answer fields
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(0), "mark2/0", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(1), "mark2/1", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(2), "mark2/2", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(3), "mark2/3", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(4), "mark2/4", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(5), "mark2/5", 13, 24, 1.0f); // not marked

		assertMarkValues((FormAreaResult)pageAreaResultsList.get(6), "mark3/0", 13, 24, 0.6901960968971252f); // marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(7), "mark3/1", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(8), "mark3/2", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(9), "mark3/3", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(10), "mark3/4", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(11), "mark3/5", 13, 24, 1.0f); // not marked

		assertMarkValues((FormAreaResult)pageAreaResultsList.get(12), "mark4/0", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(13), "mark4/1", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(14), "mark4/2", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(15), "mark4/3", 13, 24, 1.0f); // not marked
		assertMarkValues((FormAreaResult)pageAreaResultsList.get(16), "mark4/4", 13, 24, 1.0f); // not marked

		assertEquals(pageAreaResultsList.get(17).getID(), "textarea5");
	}
	 */
}
