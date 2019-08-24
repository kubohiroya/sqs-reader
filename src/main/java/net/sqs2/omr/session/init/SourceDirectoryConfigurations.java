package net.sqs2.omr.session.init;

import java.io.IOException;

import net.sqs2.omr.model.Config;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.util.FileResourceID;

public class SourceDirectoryConfigurations {

	private static boolean userConfigurationEnabled = false;


	public SourceDirectoryConfiguration create(FileResourceID configFileResourceID, Config config) throws IOException,ConfigSchemeException {
		SourceDirectoryConfiguration configuration = new SourceDirectoryConfiguration(configFileResourceID, config);
		return configuration;
	}
	
	public static void setUserDefaultConfigurationEnabled(boolean userConfigurationEnabled) {
		SourceDirectoryConfigurations.userConfigurationEnabled = userConfigurationEnabled;
	}

	public static boolean isUserConfigurationEnabled() {
		return SourceDirectoryConfigurations.userConfigurationEnabled;
	}

}
