package net.sqs2.omr.model;

import net.sqs2.omr.app.deskew.DeskewGuideMissingErrorModel;
import net.sqs2.omr.app.deskew.PageFrameDistortionErrorModel;
import net.sqs2.omr.app.deskew.PageImageErrorModel;
import net.sqs2.omr.app.deskew.PageSequenceInvalidErrorModel;
import net.sqs2.omr.app.deskew.PageSourceErrorModel;
import net.sqs2.omr.app.deskew.PageUpsideDownErrorModel;
import net.sqs2.omr.base.Messages;

public class OMRProcessorErrorMessages {
	public static String get(OMRProcessorErrorModel model){
		if(model instanceof OMRProcessorErrorModel){
			if(model instanceof PageImageErrorModel){
				if(model instanceof DeskewGuideMissingErrorModel){
					return Messages.SESSION_ERROR_DESKEWGUIDEMISSING;
				}else if(model instanceof PageImageErrorModel){
					if(model instanceof PageFrameDistortionErrorModel){
						return Messages.SESSION_ERROR_PAGEFRAMEDISTORTION;
					}else if(model instanceof PageSequenceInvalidErrorModel){
						return Messages.SESSION_ERROR_PAGESEQUENCEINVALID;
					}else if(model instanceof PageUpsideDownErrorModel){
						return Messages.SESSION_ERROR_PAGEUPSIDEDOWN;
					}
				}else if(model instanceof PageSourceErrorModel){
					return "(PageSourceErrorModel)";
				}
			}else if(model instanceof DeskewGuideExtractionErrorModel){
				return Messages.SESSION_ERROR_DESKEWGUIDEMISSING;
			}
		}
		if(model == null){
			return "(null)";
		}else{
			return "(unknown error type:"+ model.getClass().getSimpleName() +")";
		}
	}
}
