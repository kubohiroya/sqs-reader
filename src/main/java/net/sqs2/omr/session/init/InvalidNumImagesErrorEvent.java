package net.sqs2.omr.session.init;

import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectory;


public class InvalidNumImagesErrorEvent extends SessionSourceInitErrorEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SourceDirectory sourceDirectory;
	int numImages;

	public InvalidNumImagesErrorEvent(Object source, SessionSource sessionSource, SourceDirectory sourceDirectory, int numImages) {
		super(source, sessionSource);
		this.sourceDirectory = sourceDirectory;
		this.numImages = numImages;
	}
	
	public SourceDirectory getSourceDirectory() {
		return this.sourceDirectory;
	}

	public int getNumImages() {
		return this.numImages;
	}
}
