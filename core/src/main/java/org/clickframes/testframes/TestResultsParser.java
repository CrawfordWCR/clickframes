/*
 * Clickframes: Full lifecycle software development automation.
 * Copyright (C)  2009 Children's Hospital Boston
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.clickframes.testframes;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.clickframes.model.test.ProjectTestResults;

/**
 * parses a zip file and generates structured test results for a project
 *
 * @author Vineet Manohar
 */
public class TestResultsParser {
	public static ProjectTestResults parseTestResults(File zip) throws IOException {
		// tmp directory
		File tmpDirectory = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
		tmpDirectory.mkdirs();

		ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
		ZipEntry entry;
		int BUFFER = 4096;
		while ((entry = zis.getNextEntry()) != null) {
			int count;
			byte data[] = new byte[BUFFER];
			// write the files to the disk
			File destinationFile = new File(tmpDirectory, entry.getName());
			destinationFile.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(destinationFile);
			BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
			while ((count = zis.read(data, 0, BUFFER)) != -1) {
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
		}

		File firefoxDir = new File(tmpDirectory, "firefox");

		if (!firefoxDir.exists()) {
			throw new RuntimeException("Invalid test results zip file uploaded. firefox directory does not exist.");
		}

		File testResultsXmlFile = new File(tmpDirectory, "testResults.xml");

		XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(testResultsXmlFile)));
		ProjectTestResults projectTestResults = (ProjectTestResults) d.readObject();
		d.close();

		return projectTestResults;
	}

	private ProjectTestResults parseResults(File firefoxDir) throws IOException {
		ProjectTestResults projectTestResults = new ProjectTestResults();

		int totalPassed = 0;
		int totalFailed = 0;
		for (File suiteResultsFile : firefoxDir.listFiles()) {
			if (!suiteResultsFile.getName().endsWith("_result.html")) {
				continue;
			}

			String suiteResultsFileContents = FileUtils.readFileToString(suiteResultsFile);
			/**
			 * <table>
			 * <tr>
			 * <td>result:</td>
			 * <td>failed</td>
			 */
			Matcher m1 = Pattern.compile("^.*<table>\\s*<tr>\\s*<td>\\s*result:</td>\\s*<td>(failed|passed)</td>.*$",
					Pattern.DOTALL).matcher(suiteResultsFileContents);
			if (!m1.matches()) {
				throw new RuntimeException("Invalid file found in the test results directory: "
						+ suiteResultsFile.getAbsolutePath());
			}

			if (m1.group(1).equals("passed")) {
				totalPassed++;
			} else {
				totalFailed++;
			}
		}
		projectTestResults.setFailedCount(totalFailed);
		projectTestResults.setPassedCount(totalPassed);
		return projectTestResults;
	}
}