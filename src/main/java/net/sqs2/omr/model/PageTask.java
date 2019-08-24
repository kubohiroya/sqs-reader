package net.sqs2.omr.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.Delayed;

import net.sqs2.util.FileResourceID;

public class PageTask extends AbstractPageTask implements Serializable, Delayed{

	private static final long serialVersionUID = 1L;
	protected PageID pageID;
	protected String id;

	public PageTask() {
		super();
	}

	public PageTask(long sessionID,
			PageID pageID, FileResourceID configFileResourceID) {
		super(configFileResourceID, sessionID);
		this.pageID = pageID;

	}

	public PageID getPageID() {
		return this.pageID;
	}

	public String getID() {
		return this.id;
	}

	@Override
	public String toString() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	public String createID(){
		return createID(this.pageID);
	}

	public static String createID(PageID pageID) {
		return pageID.createID();
	}

	public static class TaskComparator implements Comparator<OMRPageTask> {
		public int compare(OMRPageTask a, OMRPageTask b) {
			int diff = a.getPageID().getFileResourceID().compareTo(b.getPageID().getFileResourceID());
			if (diff != 0) {
				return diff;
			}
			diff = a.getPageID().getIndexInFile() - b.getPageID().getIndexInFile();
			if (diff != 0) {
				return diff;
			}
			diff = a.getConfigFileResourceID().getRelativePath().compareTo(
					b.getConfigFileResourceID().getRelativePath());
			if (diff != 0) {
				return diff;
			} else {
				return (int) (a.configFileResourceID.getLastModified() - b.configFileResourceID.getLastModified());
			}
		}
	}


	@Override
	public boolean equals(Object o) {
		try {
			OMRPageTask task = (OMRPageTask) o;
			return this.id.equals(task.id) 
					&& this.configFileResourceID.equals(task.configFileResourceID)
					&& this.pageID.equals(task.pageID);
		} catch (ClassCastException ignore) {
		}
		return false;
	}
	
	public int compareTo(Delayed o) {
		try {
			PageTask task = (PageTask) o;
			int diff = 0;
			if (this.id.equals(task.id)) {
				return 0;
			}
			if ((diff = this.configFileResourceID.compareTo(task.configFileResourceID)) != 0) {
				return diff;
			}
			if ((diff = this.pageID.compareTo(task.pageID)) != 0) {
				return diff;
			}
		} catch (ClassCastException ignore) {
		}
		return 1;
	}
}