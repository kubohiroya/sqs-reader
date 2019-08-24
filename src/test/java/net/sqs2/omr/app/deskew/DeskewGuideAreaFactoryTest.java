package net.sqs2.omr.app.deskew;

import static org.testng.Assert.assertEquals;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import net.sqs2.net.ClassURLStreamHandlerFactory;
import net.sqs2.omr.app.SessionTestHelper;
import net.sqs2.omr.app.TestFolderManager;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.master.FormMasterException;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.DeskewGuideAreaConfig;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.PageID;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.model.SourceConfig;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.session.init.SessionSourceInitService;
import net.sqs2.util.FileResourceID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DeskewGuideAreaFactoryTest {
	
    static File sourceDirectoryRoot;
    static SessionSource sessionSource;
	
	static{
		setUpURLStreamHandlerFactory();
	}
	
	public DeskewGuideAreaFactoryTest (){
		if(sourceDirectoryRoot == null){
			sourceDirectoryRoot = TestFolderManager.getFolder("test3");
		}
	} 
	
	public static void setUpURLStreamHandlerFactory(){	
		try{
			URL.setURLStreamHandlerFactory(ClassURLStreamHandlerFactory.getSingleton());
		}catch(Error ignore){
		}
	}
	
	@BeforeClass
	public static void setUpSessionSource()throws Exception{	
		 sessionSource = SessionSourceManager.createInstance(sourceDirectoryRoot);
		 new SessionSourceInitService(sessionSource).call();
		 SessionSourceManager.putInstance(sessionSource);
	}
	
	@AfterClass
	public static void closeSource()throws Exception{	
	    SessionTestHelper.closeSessionSources();
        
	}
	
	private DeskewGuideAreaPair createDeskewGuideAreaPair(String imageFilePath)throws ConfigSchemeException, IOException, FormMasterException, OMRProcessorException{
		File imageFile = new File(sourceDirectoryRoot, imageFilePath);
		FileResourceID imageFileResourceID = new FileResourceID(imageFilePath, imageFile.lastModified());
		int indexInFile = 0;
		int numPagesInFile = 1;

		BufferedImage image = ImageIO.read(imageFile);
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
	
	@Test
	public void testOnImage001()throws Exception{

			DeskewGuideAreaPair deskewGuideAreaPair = createDeskewGuideAreaPair("001.png");

			assertEquals(deskewGuideAreaPair.getHeader().getLeft().getAreaSize(), 54);//21
			assertEquals(deskewGuideAreaPair.getHeader().getRight().getAreaSize(), 50);//27
			assertEquals(deskewGuideAreaPair.getFooter().getLeft().getAreaSize(), 30);//15
			assertEquals(deskewGuideAreaPair.getFooter().getRight().getAreaSize(), 54);//28
			
			//Point2D[] deskewGuideCenterPointsExpected = new Point2D[]{new Point2D.Float(112, 52),new Point(501, 57), new Point(90, 797), new Point(480, 807)};
			Point2D[] deskewGuideCenterPointsExpected = new Point2D[]{new Point2D.Float(111, 52),new Point(501, 62), new Point(91, 791), new Point(480, 801)};
			Point2D[] deskewGuideCenterPointsAssumed = deskewGuideAreaPair.getDeskewGuideCenterPoints();
			for(int i=0; i<4;i++){
				Point2D actual = deskewGuideCenterPointsAssumed[i];
				Point2D expected = deskewGuideCenterPointsExpected[i];
				AssertShape.assertEqualsPoint2D(actual, expected, 1.0, 1.0);
			}
	}
	
}
