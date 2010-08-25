package org.clickframes.engine;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class TestAutoscanDirectoryUtil {
	@SuppressWarnings("unused")
	private Log logger = LogFactory.getLog(getClass());
	private static final String SAMPLE_AUTOSCAN_DIR = "autoscan_samples";

	public void confirmFolderListingFunctionIsReturningCorrectNumResults() throws IOException {
		File sampleAutoscanDirectory = (new ClassPathResource(SAMPLE_AUTOSCAN_DIR)).getFile();
		Collection<File> dirs = AutoscanDirectoryUtil.recursivelyScanDirectory(sampleAutoscanDirectory);
		Assert.assertEquals(dirs.size(), 7);
	}
}