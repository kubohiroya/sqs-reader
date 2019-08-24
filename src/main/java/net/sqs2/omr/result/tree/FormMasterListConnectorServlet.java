package net.sqs2.omr.result.tree;

import java.io.PrintWriter;

import net.sqs2.omr.master.FormMaster;
import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.result.model.FormMasterItem;
import net.sqs2.omr.result.tree.PathInfoParser.FormMasterListPathItem;
import net.sqs2.omr.util.URLSafeRLEBase64;

public class FormMasterListConnectorServlet extends ConsoleConnectorServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	protected void printPathTreeItems(final PrintWriter w, long sessionID, final String rootPath){
		FormMasterListPathItem pathItem = new FormMasterListPathInfoParser(rootPath).parse();
		printMasterTreeItems(w, sessionID, rootPath, pathItem);
	}

	
	private void printMasterTreeItems(final PrintWriter w, long sessionID, final String rootPath, FormMasterListPathItem pathItem) {
		SessionSource source = SessionSourceManager.getInstance(pathItem.getSessionID());
		
		for(int masterIndex = 0; masterIndex < source.getNumFormMasters(); masterIndex++){
			FormMaster formMaster = source.getFormMaster(masterIndex);
			printListItems(w, rootPath, new ModelToPathItemFactory<FormMaster>(){
				public ListItem create(int index, FormMaster formMaster){
					return new ListItem(formMaster.getRelativePath(), 
							URLSafeRLEBase64.encode(index),
							formMaster.getPageMasterMetadata().getType(),
							ListItem.State.LEAF,
							new FormMasterItem(formMaster));
				}
			}.create(masterIndex++, formMaster));
		}
	}
}
