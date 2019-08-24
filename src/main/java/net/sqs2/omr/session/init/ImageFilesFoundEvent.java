package net.sqs2.omr.session.init;

import java.io.File;

import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;


public class ImageFilesFoundEvent extends SessionSourceInitDirectoryEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int numAddedImages;
	File sourceDirectory;

	public ImageFilesFoundEvent(Object source, SessionSource sessionSource, SourceDirectory sourceDirectory, int numAddedImages) {
		super(source, sessionSource, sourceDirectory, SessionSourceInitDirectoryEvent.INFO);
		this.numAddedImages = numAddedImages;
	}

	public int getNumAddedImages() {
		return numAddedImages;
	}

}
