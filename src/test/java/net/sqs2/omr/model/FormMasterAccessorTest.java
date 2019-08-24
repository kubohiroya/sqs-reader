package net.sqs2.omr.model;


import static org.testng.Assert.assertEquals;

import java.io.File;

import net.sqs2.omr.app.SessionTestHelper;
import net.sqs2.omr.app.TestFolderManager;
import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.master.sqm.PDFAttachmentFormMasterFactory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class FormMasterAccessorTest {
	
    static File sourceDirectoryRoot = TestFolderManager.getFolder("test3");
    static String masterFilePath = "form.pdf"; 
	
	@Test
	public void testPutAndGet()throws Exception{
		FormMasterAccessor formMasterAccessor;
		FormMaster formMaster, formMasterFromStorage;
		
		formMasterAccessor = FormMasterAccessor.getInstance(sourceDirectoryRoot);
		formMaster = new PDFAttachmentFormMasterFactory().create(sourceDirectoryRoot, masterFilePath);
		formMasterAccessor.put(formMaster);
		formMasterFromStorage = formMasterAccessor.get(FormMaster.createKey(formMaster.getFileResourceID()));
		
		assertEquals(formMaster, formMasterFromStorage);
		assertEquals(formMaster.getFormAreaList().size(), formMasterFromStorage.getFormAreaList().size());
		formMasterAccessor.delete();

		formMasterAccessor = FormMasterAccessor.getInstance(sourceDirectoryRoot);
		formMaster = new PDFAttachmentFormMasterFactory().create(sourceDirectoryRoot, masterFilePath);
		formMasterAccessor.put(formMaster);
		
		formMasterFromStorage = formMasterAccessor.get(formMaster.getFileResourceID());
		
		assertEquals(formMaster, formMasterFromStorage);
		assertEquals(formMaster.getFormAreaList().size(), formMasterFromStorage.getFormAreaList().size());
		formMasterAccessor.delete();
}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	    SessionTestHelper.closeSessionSources();
        
	}

}
