package net.sqs2.omr.session.traverse;

public class TraverseStopException extends Exception {
	private static final long serialVersionUID = 1L;

	public TraverseStopException(){}
	public TraverseStopException(String message){
		super(message);
	}
	public TraverseStopException(Exception ex){
		super(ex);
	}
}
