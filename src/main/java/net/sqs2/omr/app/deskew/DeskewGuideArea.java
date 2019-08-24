package net.sqs2.omr.app.deskew;

import java.util.List;

import net.sqs2.omr.model.OMRProcessorException;

public class DeskewGuideArea{
	
	protected DeskewGuideCandidateFinder deskewGuideCandidateFinder;
	protected DeskewGuideAreaBitmap deskewGuideAreaBitmap;
	protected List<DeskewGuide> deskewGuideCandidateList;
	protected DeskewGuidePair extractedDeskewGuidePair;
	protected OMRProcessorException exception = null;
		
	public DeskewGuideArea() {
	}
	
	public DeskewGuideCandidateFinder getDeskewGuideCandidateFinder(){
		return deskewGuideCandidateFinder;
	}

	public void setDeskewGuideCandidateFinder(DeskewGuideCandidateFinder deskewGuideCandidateFinder){
		this.deskewGuideCandidateFinder = deskewGuideCandidateFinder;
	}

	public void setDeskewGuideAreaBitmap(DeskewGuideAreaBitmap deskewGuideAreaBitmap) {
		this.deskewGuideAreaBitmap = deskewGuideAreaBitmap;
	}

	public void setDeskewGuideCandidateList(
			List<DeskewGuide> deskewGuideCandidateList) {
		this.deskewGuideCandidateList = deskewGuideCandidateList;
	}

	public void setExtractedDeskewGuidePair(DeskewGuidePair extractedDeskewGuidePair) {
		this.extractedDeskewGuidePair = extractedDeskewGuidePair;
	}

	public void setException(OMRProcessorException exception) {
		this.exception = exception;
	}

	public List<DeskewGuide> getDeskewGuideCandidateList() {
		return deskewGuideCandidateList;
	}

	public DeskewGuideAreaBitmap getDeskewGuideAreaBitmap() {
		return deskewGuideAreaBitmap;
	}

	public DeskewGuidePair getExtractedDeskewGuidePair() {
		return extractedDeskewGuidePair;
	}
	
	public DeskewGuide getLeft() {
		return extractedDeskewGuidePair.getLeft();
	}
	
	public DeskewGuide getRight() {
		return extractedDeskewGuidePair.getRight();
	}
	
	public OMRProcessorException getOMRProcessorException(){
		return exception;
	}
}