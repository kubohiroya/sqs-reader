
package net.sqs2.omr.result.tree;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

public class QuestionTreeConnectorServlet extends ConsoleConnectorServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void printPathTreeItems(PrintWriter w, long sessionID, String dir) {
		Logger.getLogger(this.getClass()).info("QuestionTreeConnectorServlet");
	}
}
