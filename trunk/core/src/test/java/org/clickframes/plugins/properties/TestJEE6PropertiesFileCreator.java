package org.clickframes.plugins.properties;

import java.io.File;
import java.io.IOException;

import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.annotations.Test;
@Test
public class TestJEE6PropertiesFileCreator extends AbstractPluginTest {
	public void test() throws IOException {
		File path = File.createTempFile("testNGTest", "properties");
		JEE6PropertiesFileCreator.writeToPropertiesFile(path, techspecContext.getAppspec());
	}
}
