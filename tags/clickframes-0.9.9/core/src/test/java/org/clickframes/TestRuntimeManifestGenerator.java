package org.clickframes;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.engine.manifest.RuntimeManifestGenerator;
import org.testng.annotations.Test;

/**
 * Trivial unit test.  Not too effective from it's own project.
 */
@Test(enabled = true)
public class TestRuntimeManifestGenerator {
	private Log logger = LogFactory.getLog(getClass());

	/**
	 * Presently doesn't actually find any useful jar. To accurately test, you'll need to test this from maven command
	 * line in a different project. Eclipse will include dependencies in your workspace as the actual eclipse project,
	 * not the imported jar.
	 */
	public void coverageTestWithNoArgs() throws IOException {
		List<String> templates = RuntimeManifestGenerator.getAutoscanTemplatesFromClasspath();
		logger.info(templates.size() + " entries found");
	}

	public void coverageTestWithClickframesCore() throws IOException {
		List<String> templates = RuntimeManifestGenerator.getAutoscanTemplatesFromClasspath();
		logger.info(templates.size() + " entries found");
	}
}
