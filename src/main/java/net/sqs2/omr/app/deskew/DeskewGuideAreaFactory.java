package net.sqs2.omr.app.deskew;

import java.awt.image.BufferedImage;
import java.util.List;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.DeskewGuideAreaConfig;
import net.sqs2.omr.model.OMRProcessorException;
import net.sqs2.omr.model.PageID;

public class DeskewGuideAreaFactory{
	DeskewGuideAreaConfig guideAreaConfig;
	FormMaster master;
	BufferedImage image;
	PageID pageID;
	int type;
	
	public DeskewGuideAreaFactory(DeskewGuideAreaConfig guideAreaConfig, FormMaster master, BufferedImage image, PageID pageID, int type){
		this.guideAreaConfig = guideAreaConfig;
		this.master = master;
		this.image = image;
		this.pageID = pageID;
		this.type = type;
	}

	DeskewGuideAreaBitmap createBitmap(){
		return new DeskewGuideAreaBitmap(guideAreaConfig, image, 300, 30, type);
	}
	
	DeskewGuideCandidateFinder createDeskewGuideCandidateFinder(DeskewGuideAreaBitmap bitmap)throws OMRProcessorException{
		return new DeskewGuideCandidateFinder(bitmap);
	}
	
	List<DeskewGuide> createDeskewGuideCandidates(DeskewGuideCandidateFinder finder)throws OMRProcessorException{
		return finder.find();
	}
	
	DeskewGuidePair createDeskewGuidePair(List<DeskewGuide> deskewGuideCandidateList, int imageWidth, int imageHeight, int type)throws OMRProcessorException{
		return new DeskewGuideFilterNearByMasterGuide(this.master).filter(deskewGuideCandidateList, imageWidth, imageHeight, type);
	}
	
	public DeskewGuideArea create(){
		DeskewGuideArea area = new DeskewGuideArea();
		area.setDeskewGuideAreaBitmap(createBitmap());
			
		try{
			area.setDeskewGuideCandidateFinder(createDeskewGuideCandidateFinder(area.getDeskewGuideAreaBitmap()));
			
			area.setDeskewGuideCandidateList(createDeskewGuideCandidates(area.getDeskewGuideCandidateFinder()));
		}catch(OMRProcessorException ex){
			area.setException(ex);
			return area;
		}
			
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		try{
			List<DeskewGuide> deskewGuideCandidateList = area.getDeskewGuideCandidateList();
			if(deskewGuideCandidateList.size() == 0){
				area.setException(new OMRProcessorException(new DeskewGuideMissingErrorModel()));
				area.setExtractedDeskewGuidePair(new DeskewGuidePair(null, null));
				return area;
			}else if(deskewGuideCandidateList.size() == 1){
				area.setException(new OMRProcessorException(new DeskewGuideMissingErrorModel()));
				DeskewGuide d = deskewGuideCandidateList.get(0);
				if(d.getCenterPoint().getX() < imageWidth / 2){
					area.setExtractedDeskewGuidePair(new DeskewGuidePair(d, null));
				}else{
					area.setExtractedDeskewGuidePair(new DeskewGuidePair(null, d));
				}
				return area;
			}
			DeskewGuidePair deskewGuidePair = createDeskewGuidePair(deskewGuideCandidateList, imageWidth, imageHeight, type);
			
			if(type == 1){
				deskewGuidePair.getLeft().addOffset(0, -3);
				deskewGuidePair.getRight().addOffset(0, -3);
			}
			
			area.setExtractedDeskewGuidePair(deskewGuidePair);
			if(deskewGuidePair.getLeft() == null || deskewGuidePair.getRight() == null){
				area.setException(new OMRProcessorException(new DeskewGuideMissingErrorModel()));
			}else{
				area.setException(null);
			}
		}catch(OMRProcessorException e){
			area.setException(e);
		}
		return area;
	}
}