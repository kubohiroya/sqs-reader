package net.sqs2.omr.session.init;

import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SourceDirectoryConfiguration;

public class ConfigFileFoundEvent extends SessionSourceInitEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SourceDirectoryConfiguration sourceDirectoryConfiguration;
	public ConfigFileFoundEvent(Object source, SessionSource sessionSource, SourceDirectoryConfiguration sourceDirectoryConfiguration) {
		super(source, sessionSource);
		this.sourceDirectoryConfiguration = sourceDirectoryConfiguration;
	}
	public SourceDirectoryConfiguration getSourceDirectoryConfiguration() {
		return sourceDirectoryConfiguration;
	}

}
