package org.clickframes.util;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

public class JavaBeautifierTest {
	private static Log log = LogFactory.getLog(JavaBeautifierTest.class);

	@Test
	public void testBeautify() throws IOException, ParseException {
		File input = new File("src" + File.separator + "test" + File.separator
				+ "data" + File.separator + "GeneratedNewIssueController.java");
		File output = File
				.createTempFile("GeneratedNewIssueController", "java");
		JavaBeautifier.beautify(input, output);
	}
}