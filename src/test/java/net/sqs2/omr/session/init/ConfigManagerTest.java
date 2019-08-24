package net.sqs2.omr.session.init;


import static org.testng.Assert.assertEquals;

import java.net.URL;

import net.sqs2.net.ClassURLStreamHandlerFactory;
import net.sqs2.omr.model.Config;

import org.testng.annotations.Test;

public class ConfigManagerTest {
	
	static{
		try{
			URL.setURLStreamHandlerFactory(ClassURLStreamHandlerFactory.getSingleton());
		}catch(Error ignore){}
	}
	
	@Test
	public void testCreateConfigInstance()throws Exception{
		Config config = ConfigManager.createDefaultConfigInstance();
		assertEquals("2.1.2", config.getVersion());
	}
}
