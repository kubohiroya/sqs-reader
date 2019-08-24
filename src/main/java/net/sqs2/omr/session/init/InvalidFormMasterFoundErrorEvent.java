package net.sqs2.omr.session.init;

import java.io.File;

import net.sqs2.omr.model.SessionSource;

public class InvalidFormMasterFoundErrorEvent extends SessionSourceInitErrorEvent {

	File file;
	String message;
	
	public InvalidFormMasterFoundErrorEvent(Object source, SessionSource sessionSource, File file, String message) {
		super(source, sessionSource);
		this.file = file;
		this.message = message;
	}

	public File getFile() {
		return file;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString(){
		return this.getClass().getName()+"{file="+this.getFile().getAbsolutePath()+"}";
	}

}
