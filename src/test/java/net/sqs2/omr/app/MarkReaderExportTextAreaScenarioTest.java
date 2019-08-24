package net.sqs2.omr.app;

import static org.testng.Assert.assertEquals;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.io.File;

import net.sqs2.omr.base.Messages;
import net.sqs2.omr.model.AppConstants;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.xml.XMLUtil;
import net.sqs2.xml.XPathSelector;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MarkReaderExportTextAreaScenarioTest extends MarkReaderAppTest{

	static final String LIST_OF_FREE_ANSWERS_LABEL = Messages._("result.directoryIndex.listOfFreeAnswersLabel");
	static final String RESULT_DIRNAME = AppConstants.RESULT_DIRNAME;
	static final String ROW_NUMBER_PREFIX_LABEL = Messages._("result.textAreaIndex.rowNumberPrefixLabel");
	static final String ROW_NUMBER_SUFFIX_LABEL = Messages._("result.textAreaIndex.rowNumberSuffixLabel");
	static final String FOLDER_LABEL = Messages._("result.directoryIndex.folderPrefixLabel");
	
	public MarkReaderExportTextAreaScenarioTest() {
		super();
	}

	class ExpectedStringValueInHTML{
		Element documentElement;
		XPathSelector xpath;
		
		ExpectedStringValueInHTML(File file, String rootNodeTest)throws Exception{
			Document document = XMLUtil.createDocumentBuilder().parse(file);
			documentElement = (Element)document.getDocumentElement();			
			xpath = new XPathSelector(document, rootNodeTest);
		}
		
		String getValue(String targetElementTest)throws Exception{
			Element elem = xpath.selectSingleNode(documentElement, targetElementTest);
			return elem.getTextContent();
		}

		String getValue(String targetElementTest, String targetAttributeName)throws Exception{
			Element elem = ((Element)xpath.selectSingleNode(documentElement, targetElementTest));
			return elem.getAttribute(targetAttributeName);
		}
	}
	
	@Test
	public void testTextAreaExportScenario0() {
		try{
			File targetSourceDirectoryRoot = sourceDirectoryRoot0;
			startAndCloseSession(targetSourceDirectoryRoot, null);
			
			File resultDirectory = new File(targetSourceDirectoryRoot, AppConstants.RESULT_DIRNAME);
			File textareaDirectory = new File(resultDirectory, "TEXTAREA");			
			File textareaDirectoryIndexFile = new File(textareaDirectory, "index.html");
			File row0Directory = new File(textareaDirectory, "0");
			File row0DirectoryIndexFile = new File(row0Directory, "index.html");
			File row0Page0File = new File(row0Directory, "0.html");
			
			assertTrue(textareaDirectory.isDirectory());
			assertTrue(textareaDirectoryIndexFile.exists());

			ExpectedStringValueInHTML test1 = new ExpectedStringValueInHTML(textareaDirectoryIndexFile, "xhtml");
			assertEquals(test1.getValue("/xhtml:html/xhtml:head/xhtml:title"), "SQS 実習帖（09年7月実施）::"+LIST_OF_FREE_ANSWERS_LABEL);
			assertEquals(test1.getValue("/xhtml:html/xhtml:body/xhtml:div/xhtml:div/xhtml:ul/xhtml:li[1]/xhtml:a", "href"), "0/index.html");
			
			assertTrue(row0DirectoryIndexFile.exists());
			
			ExpectedStringValueInHTML test2 = new ExpectedStringValueInHTML(row0DirectoryIndexFile, "xhtml");
			assertEquals(test2.getValue("/xhtml:html/xhtml:head/xhtml:title"), ":(1):"+LIST_OF_FREE_ANSWERS_LABEL);
			assertEquals(test2.getValue("/xhtml:html/xhtml:body/xhtml:div/xhtml:div/xhtml:ul/xhtml:li[1]/xhtml:img", "src"), "0.png");
			
			assertTrue(row0Page0File.exists());
			
			ExpectedStringValueInHTML test3 = new ExpectedStringValueInHTML(row0Page0File, "xhtml");
			assertEquals(test3.getValue("/xhtml:html/xhtml:head/xhtml:title"), ":(1):["+ROW_NUMBER_PREFIX_LABEL+"1"+ROW_NUMBER_SUFFIX_LABEL+"-"+ROW_NUMBER_PREFIX_LABEL+"2"+ROW_NUMBER_SUFFIX_LABEL+
					"](Page 1 of 1):"+LIST_OF_FREE_ANSWERS_LABEL);
			assertEquals(test3.getValue("/xhtml:html/xhtml:body/xhtml:div/xhtml:div/xhtml:ul/xhtml:li[1]/xhtml:img", "src"), "0.png");
			
			SessionTestHelper.closeSessionSources();
	        
		}catch(Exception ex){
			ex.printStackTrace();
			fail();
		}
	}

	/* ------------------------ */
	
	@Test
	public void testTextAreaExportScenario2() {
		try{
			File targetSourceDirectoryRoot = sourceDirectoryRoot2;
			startAndCloseSession(targetSourceDirectoryRoot, null);
			
			File resultDirectory = new File(targetSourceDirectoryRoot, AppConstants.RESULT_DIRNAME);			
			File textareaDirectory = new File(resultDirectory, "TEXTAREA");			
			File textareaDirectoryIndexFile = new File(textareaDirectory, "index.html");
			File row15Directory = new File(textareaDirectory, "15");
			File row15DirectoryIndexFile = new File(row15Directory, "index.html");
			File row15Page0File = new File(row15Directory, "0.html");
			
			assertTrue(textareaDirectory.isDirectory());

			assertTrue(textareaDirectoryIndexFile.exists());
			
			ExpectedStringValueInHTML test1 = new ExpectedStringValueInHTML(textareaDirectoryIndexFile, "xhtml");
			assertEquals(test1.getValue("/xhtml:html/xhtml:head/xhtml:title"), 
					"::"+LIST_OF_FREE_ANSWERS_LABEL);
			assertEquals(test1.getValue("/xhtml:html/xhtml:body/xhtml:div/xhtml:div/xhtml:ul/xhtml:li[1]/xhtml:a", "href"), 
					"15/index.html");
			
			assertTrue(row15DirectoryIndexFile.exists());
			
			ExpectedStringValueInHTML test2 = new ExpectedStringValueInHTML(row15DirectoryIndexFile, "xhtml");
			assertEquals(test2.getValue("/xhtml:html/xhtml:head/xhtml:title"),
					":(7):"+LIST_OF_FREE_ANSWERS_LABEL);
			assertEquals(test2.getValue("/xhtml:html/xhtml:body/xhtml:div/xhtml:div/xhtml:ul/xhtml:li[1]/xhtml:a", "href"), 
					"../../../A/"+RESULT_DIRNAME+"/TEXTAREA/15/index.html");
			assertTrue(row15Page0File.exists());
			
			ExpectedStringValueInHTML test3 = new ExpectedStringValueInHTML(row15Page0File, "xhtml");
			assertEquals(test3.getValue("/xhtml:html/xhtml:head/xhtml:title"),
					FOLDER_LABEL+"C:(7):["+ROW_NUMBER_PREFIX_LABEL+"1"+ROW_NUMBER_SUFFIX_LABEL+"-"+ROW_NUMBER_PREFIX_LABEL+"5"+ROW_NUMBER_SUFFIX_LABEL+
					"](Page 1 of 3):"+LIST_OF_FREE_ANSWERS_LABEL);
			assertEquals(test3.getValue("/xhtml:html/xhtml:body/xhtml:div/xhtml:div/xhtml:ul/xhtml:li[1]/xhtml:img", "src"), "20.png");

			SessionTestHelper.closeSessionSources();
	        	
		}catch(Exception ex){
			ex.printStackTrace();
			fail();
		}
	}
}
