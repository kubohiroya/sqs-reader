/**
 * 
 */
package net.sqs2.omr.model;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.sqs2.image.ImageFactory;
import net.sqs2.omr.master.FormMaster;

public class OMRProcessorSource{
	PageID pageID;
	BufferedImage image;
	SourceDirectoryConfiguration configuration;
	FormMaster formMaster;
	int processingPageIndex;

	public OMRProcessorSource(PageID pageID, byte[] imageByteArray, SourceDirectoryConfiguration configuration,
			FormMaster formMaster, int processingPageIndex)throws IOException{
		this.pageID = pageID;
		this.image = createBufferedImage(imageByteArray);
		this.configuration = configuration;
		this.formMaster = formMaster;
		this.processingPageIndex = processingPageIndex;
	}
	
	public PageID getPageID(){
		return this.pageID;
	}

	public BufferedImage getPageImage(){
		return image;
	}

	public SourceDirectoryConfiguration getConfiguration() {
		return configuration;
	}

	public int getProcessingPageIndex() {
		return processingPageIndex;
	}

	public FormMaster getFormMaster() {
		return formMaster;
	}	
	
	public SourceConfig getSourceConfig() throws ConfigSchemeException {
		ConfigImpl config = (ConfigImpl) this.configuration.getConfig();
		SourceConfig sourceConfig = null;
		sourceConfig = config.getSourceConfig(pageID.getFileResourceID().getRelativePath());
		return sourceConfig;
	}

	private BufferedImage createBufferedImage(byte[] imageByteArray) throws IOException {
		String type = pageID.getExtension();
		if (imageByteArray== null) {
			throw new RuntimeException(pageID.toString());
		}
		BufferedImage pageImage = ImageFactory.createImage(type, imageByteArray, pageID.getIndexInFile());
		return pageImage;
	}


}