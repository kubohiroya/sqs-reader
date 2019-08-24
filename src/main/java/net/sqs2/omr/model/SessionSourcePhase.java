package net.sqs2.omr.model;

public class SessionSourcePhase {
	
	public static final SessionSourcePhase NOT_INITIALIZED = new SessionSourcePhase();
	
	public static enum Phase{
		notYet, doing, done, fail, stop
	}
	public static Phase NOT_YET = Phase.notYet;
	public static Phase DOING = Phase.doing;
	public static Phase DONE = Phase.done;
	public static Phase FAIL = Phase.fail;
	public static Phase STOP = Phase.stop;
	
	public Phase sessionRunningPhase = Phase.notYet;
	public Phase initializingPhase = Phase.notYet;
	public Phase scanningPhase = Phase.notYet;
	public Phase exportingPhase = Phase.notYet;

	public SessionSourcePhase(){}
	
	public Phase getSessionRunningPhase() {
		return sessionRunningPhase;
	}
	
	public void reset(){
		this.sessionRunningPhase = Phase.notYet;
		this.initializingPhase = Phase.notYet;
		this.scanningPhase = Phase.notYet;
		this.exportingPhase = Phase.notYet;
	}

	public void setSessionRunningPhase(Phase sessionRunningPhase) {
		this.sessionRunningPhase = sessionRunningPhase;
	}
	
	public boolean hasStopped(){
		return getSessionRunningPhase() == SessionSourcePhase.Phase.stop;
	}

	public boolean hasInitialized(){
		return this.initializingPhase != DONE;
	}

	public Phase getInitializingPhase() {
		return initializingPhase;
	}

	public void setInitializingPhase(Phase initializingPhase) {
		this.initializingPhase = initializingPhase;
	}

	public Phase getScanningPhase() {
		return scanningPhase;
	}

	public void setScanningPhase(Phase scanningPhase) {
		this.scanningPhase = scanningPhase;
	}

	public Phase getExportingPhase() {
		return exportingPhase;
	}

	public void setExportingPhase(Phase exportingPhase) {
		this.exportingPhase = exportingPhase;
	}
	
	public static final int SESSION_RUNNING_CATEGORY_INDEX = 0;
	public static final int INITIALIZING_CATEGORY_INDEX = 1;
	public static final int SCANNING_CATEGORY_INDEX = 2;
	public static final int EXPORTING_CATEGORY_INDEX = 3;
	

	public void setPhase(int categoryIndex, SessionSourcePhase.Phase phase) {
		switch(categoryIndex){
		case SESSION_RUNNING_CATEGORY_INDEX:
			this.sessionRunningPhase = phase;
			break;
		case INITIALIZING_CATEGORY_INDEX:
			this.initializingPhase = phase;
			break;
		case SCANNING_CATEGORY_INDEX:
			this.scanningPhase = phase;
			break;
		case EXPORTING_CATEGORY_INDEX:
			this.exportingPhase = phase;
			break;
		}
	}
	
	public String toString(){
		switch(this.sessionRunningPhase){
			case notYet:
				return "NOT_RUNNING";
			case doing:
				switch(this.initializingPhase){
					case notYet:
						return "NOT_INITIALIZING";
					case doing:
						return "INITIALIZING";
					case done:
						switch(this.scanningPhase){
							case notYet:
								return "INITIALIZED";
							case doing:
								return "SCANNING";
							case done:
								switch(this.exportingPhase){
									case notYet:
										return "SCANNED";
									case doing:
										return "EXPORTING";
									case done:
										return "EXPORTED";
								}
						}
				}
				break;
			case done:
				return "FINISHED";
			case fail:
				return "FAILED";
			case stop:
				return "STOPPED";
			default:
				return "(UNKNOWN PHASE)";
		}
		return "(UNKNOWN PHASE)";
	}
	
	public boolean isRunning(){
		return (this.sessionRunningPhase == Phase.doing);
	}

}
