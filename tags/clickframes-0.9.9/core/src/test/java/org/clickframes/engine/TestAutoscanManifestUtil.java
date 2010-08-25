package org.clickframes.engine;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

@Test(enabled=false)
@Deprecated
public class TestAutoscanManifestUtil {
	private final Log logger = LogFactory.getLog(getClass());
	public void basicTest(){

		final File manifestPath = new File("src/test/resources/AutoscanManifest.txt");
//		logger.debug(manifestPath.getAbsolutePath());
////		logger.debug(AutoscanManifestUtil.getFilenamesFromManifest(manifestPath));
//		logger.debug(AutoscanManifestUtil.getTempFilesFromManifest(manifestPath));
	}
}
