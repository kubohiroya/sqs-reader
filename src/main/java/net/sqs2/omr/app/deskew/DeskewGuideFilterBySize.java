package net.sqs2.omr.app.deskew;

import java.util.List;

import net.sqs2.omr.model.OMRProcessorException;

public class DeskewGuideFilterBySize extends DeskewGuideFilter{
	
	@Override
	public DeskewGuidePair filter(List<DeskewGuide> deskewGuideList, int imageWidth, int imageHeight, int headerOrFooter)throws OMRProcessorException{
		int len = deskewGuideList.size();
		DeskewGuide s1 = deskewGuideList.get(len- 1);
		DeskewGuide s2 = deskewGuideList.get(len - 2);
		
		if(s1.getCenterPoint().getX() < s2.getCenterPoint().getX()){
			return new DeskewGuidePair(s1, s2);
		}else{
			return new DeskewGuidePair(s2, s1);
		}
	}
}