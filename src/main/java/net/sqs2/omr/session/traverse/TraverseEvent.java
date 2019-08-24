package net.sqs2.omr.session.traverse;

public class TraverseEvent {

	boolean isStart;

	int index = 0;
	int numEvents;

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}

	public void setNumEvents(int numEvents) {
		this.numEvents = numEvents;
	}

	public int getNumEvents() {
		return this.numEvents;
	}

	public TraverseEvent() {
		this.isStart = true;
	}

	public TraverseEvent(boolean isStart) {
		this.isStart = isStart;
	}

	public void setStart() {
		this.isStart = true;
	}

	public void setEnd() {
		this.isStart = false;
	}

	public boolean isStart() {
		return this.isStart;
	}

	public boolean isEnd() {
		return !this.isStart;
	}

}
