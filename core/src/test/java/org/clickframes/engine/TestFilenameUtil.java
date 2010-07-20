package org.clickframes.engine;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.mortbay.log.LogFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestFilenameUtil {
	private final Log logger = LogFactory.getLog(getClass());
	@Test(dataProvider="FIT", enabled=false)
	public void test(String input, String expectedFolder, String expectedFileName){
		String[] result = FilenameUtil.splitFilenameAndFolder(input);
		logger.debug(result[0] + "\t"  +result[1]);
		Assert.assertEquals(expectedFileName, result[1]);
		Assert.assertEquals(expectedFolder, result[0]);
	}
	@Test
	public void tmp(){
		String separator = File.separator;
		String javaFolders = "foo.bar.com".replace(".", separator) + separator;
		logger.debug(javaFolders);
		separator = "\\";
		javaFolders = "foo.bar.com".replace(".", separator) + separator;
		logger.debug(javaFolders);
	}


	@DataProvider(name = "FIT")
	public Object[][] dataTable() {
		return new String[][] {

				{ File.separator + "folder0" + File.separator + "folder1" + File.separator + "foo.txt",
						File.separator + "folder0" + File.separator + "folder1" + File.separator, "foo.txt" },
				{ "folder1" + File.separator + "foo.txt", "folder1" + File.separator, "foo.txt" },
				{ "foo.txt", "", "foo.txt" },
				{ File.separator + "folder0" + File.separator, File.separator + "folder0" + File.separator, "" },
		};
	}
}
