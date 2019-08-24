package net.sqs2.omr.app.deskew;

import java.awt.geom.Point2D;

public class DeskewGuideAreaPair {

	DeskewGuideArea header, footer;
	
	public DeskewGuideAreaPair(DeskewGuideArea header, DeskewGuideArea footer) {
		this.header = header;
		this.footer = footer;
	}

	public DeskewGuideArea getHeader() {
		return header;
	}

	public DeskewGuideArea getFooter() {
		return footer;
	}

	public Point2D[] getDeskewGuideCenterPoints(){
		return new Point2D[]{
				this.header.getExtractedDeskewGuidePair().getLeft().getCenterPoint(),
				this.header.getExtractedDeskewGuidePair().getRight().getCenterPoint(),
				this.footer.getExtractedDeskewGuidePair().getLeft().getCenterPoint(),
				this.footer.getExtractedDeskewGuidePair().getRight().getCenterPoint(),
		}; 
	}

	public int[] getDeskewGuideAreaSizes(){
		return new int[]{
				this.header.getExtractedDeskewGuidePair().getLeft().getAreaSize(),
				this.header.getExtractedDeskewGuidePair().getRight().getAreaSize(),
				this.footer.getExtractedDeskewGuidePair().getLeft().getAreaSize(),
				this.footer.getExtractedDeskewGuidePair().getRight().getAreaSize(),
		}; 
	}

}
