package net.sqs2.omr.result.path;

import java.util.ArrayList;
import java.util.List;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.result.model.FormMasterItem;

public class SessionSelection {
	protected SessionSource sessionSource;
	public SessionSelection(SessionPath sessionPath){
		this.sessionSource = SessionSourceManager.getInstance(sessionPath.getSessionID());
	}

	public SessionSource getSessionSource() {
		return sessionSource;
	}

	public List<FormMasterItem> createFormMasterItemList(){
		List<FormMasterItem> formMasterItemList = new ArrayList<FormMasterItem>(); 
		for(int masterIndex = 0; masterIndex < sessionSource.getNumFormMasters(); masterIndex++){
			FormMaster formMaster = sessionSource.getFormMaster(masterIndex); 
			formMasterItemList.add(new FormMasterItem(formMaster));	
		}
		return formMasterItemList;
	}

}
