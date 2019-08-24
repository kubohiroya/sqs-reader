package net.sqs2.omr.app.deskew;

import java.util.List;

import net.sqs2.omr.model.OMRProcessorException;

public abstract class DeskewGuideFilter{
	public abstract DeskewGuidePair filter(List<DeskewGuide> deskewGuideList, int pageImageWidth, int pageImageHeight, int headerOrFooter)throws OMRProcessorException;
}