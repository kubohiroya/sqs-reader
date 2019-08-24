package net.sqs2.omr.session.init;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import net.sqs2.omr.model.Config;
import net.sqs2.omr.model.ConfigImpl;
import net.sqs2.omr.model.ConfigSchemeException;
import net.sqs2.omr.model.SourceDirectory;
import net.sqs2.omr.model.SourceDirectoryConfiguration;
import net.sqs2.util.FileResourceID;
import net.sqs2.util.FileUtil;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.SAXException;

public class ConfigManager {
	
	private static String CONFIG_FILENAME = "config.xml";
	private static String CONFIG_RULE_FILENAME = "configRule.xml";
	private static Digester digester;
	private static Config defaultConfigInstance;

	public static SourceDirectoryConfiguration createSourceDirectoryConfiguration(SourceDirectory sourceDirectory,  FileResourceID configFileResourceID) throws IOException,ConfigSchemeException {
		return createSourceDirectoryConfiguration(sourceDirectory.getSourceDirectoryRootFile(), configFileResourceID);
	}

	static SourceDirectoryConfiguration createSourceDirectoryConfiguration(File sourceDirectoryRoot, FileResourceID configFileResourceID) throws IOException,ConfigSchemeException {
		File configFile = new File(sourceDirectoryRoot, configFileResourceID.getRelativePath());
		Config config = ConfigManager.createConfigInstance(configFile);
		return new SourceDirectoryConfiguration(configFileResourceID, config);
	}

	public static synchronized Config createConfigInstance(File configFile) throws ConfigSchemeException{
		try {
			initDefaultConfigInstance();
			URL url = configFile.toURI().toURL();
			Config config = (ConfigImpl) digester.parse(url);
			if (! defaultConfigInstance.getVersion().equals(config.getVersion())) {
				Logger.getLogger("ConfigManager").warning("Config file version in Source Directory contains version mismatch. Override it.");
				File backupConfigFile = new File(FileUtil.getBasepath(configFile) + "-"+ config.getVersion() + ".old.xml");
				if (!backupConfigFile.exists()) {
					configFile.renameTo(backupConfigFile);
				}
				ConfigFileUtil.createConfigFile(configFile);
				config = (ConfigImpl) digester.parse(url);
			}
			return config;

		} catch (IOException ex) {
			throw new ConfigSchemeException(ex);
		} catch (SAXException ex) {
			throw new ConfigSchemeException(ex);
		}
	}

	static void initDefaultConfigInstance() throws MalformedURLException, IOException, SAXException {
		if(defaultConfigInstance == null){
			defaultConfigInstance = createDefaultConfigInstance();
		}
	}
	
	static Config createDefaultConfigInstance() throws MalformedURLException, IOException, SAXException {
		String baseURI = "class://"+ConfigManager.class.getCanonicalName()+"/";
		URL defaultConfigRuleFileURL = new URL(baseURI + CONFIG_RULE_FILENAME);
		URL defaultConfigFileURL = new URL(baseURI + CONFIG_FILENAME);
		digester = DigesterLoader.createDigester(defaultConfigRuleFileURL);
		return (Config) digester.parse(defaultConfigFileURL);
	}
}
