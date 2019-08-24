package net.sqs2.omr.session.init;

import static org.testng.Assert.assertEquals;

import java.io.File;

import net.sqs2.omr.app.TestFolderManager;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.master.FormMasterFactory;

import org.testng.annotations.Test;


public class MergedFormMasterFactoryTest {
	
	FormMasterFactory formMasterFactory = new MultiSourceFormMasterFactory();

	@Test
	public void createFormMasterTestOnTest0()throws Exception{
	    File sourceDirectoryRoot = TestFolderManager.getFolder("test0");
        String formMasterFilePath = "form.pdf";

		FormMaster formMaster = formMasterFactory.create(sourceDirectoryRoot, formMasterFilePath);
		assertEquals(78, formMaster.getFormAreaList().size());
	}

}
