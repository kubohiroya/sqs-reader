package net.sqs2.omr.app.deskew;

import java.awt.image.BufferedImage;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.DeskewGuideAreaConfig;
import net.sqs2.omr.model.PageID;

public class DeskewGuideAreaPairFactory {
	
	DeskewGuideAreaConfig deskewGuideAreaConfig;
	FormMaster formMaster;
	BufferedImage pageImage;
	PageID pageID;
	
	public DeskewGuideAreaPairFactory(DeskewGuideAreaConfig deskewGuideAreaConfig,
			FormMaster formMaster,
			BufferedImage pageImage,
			PageID pageID){
		this.deskewGuideAreaConfig = deskewGuideAreaConfig;
		this.formMaster = formMaster;
		this.pageImage = pageImage;
		this.pageID = pageID;
	}
	
	public DeskewGuideAreaPair create(){
		
		DeskewGuideArea header = new DeskewGuideAreaFactory(deskewGuideAreaConfig, formMaster, pageImage, pageID, DeskewGuideAreaBitmap.HEADER_AREA_TYPE).create();

		DeskewGuideArea footer = new DeskewGuideAreaFactory(deskewGuideAreaConfig, formMaster, pageImage, pageID, DeskewGuideAreaBitmap.FOOTER_AREA_TYPE).create();
		
		return new DeskewGuideAreaPair(header, footer); 
	}
}
