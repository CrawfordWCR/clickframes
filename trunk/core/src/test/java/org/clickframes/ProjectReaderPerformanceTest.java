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

package org.clickframes;

import java.io.InputStream;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.AppspecReader;
import org.clickframes.model.Appspec;
import org.clickframes.model.AppspecConstraintViolationException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Vineet Manohar
 */
public class ProjectReaderPerformanceTest {
	private static Log log = LogFactory.getLog(ProjectReaderPerformanceTest.class);

	@Test(groups = "ignore")
	public void testProjectReaderPerformance() throws JAXBException, AppspecConstraintViolationException {
		int numIterations = 100;
		long total = 0;
		for (int i = 0; i < numIterations; i++) {
			long duration = readProject();
			// SB: Do we need this much output?
			// log.info("Iteration: " + (i + 1) + ": " + duration + " ms");
			total += duration;
		}

		log.info("Avg: " + total / numIterations + " ms");
	}

	private long readProject() throws JAXBException, AppspecConstraintViolationException {
		Appspec appspec;
		InputStream is = AppspecReader.class.getResourceAsStream("/appspec_junit.xml");
		Assert.assertNotNull(is, "Sample file not found");
		long startTime = new Date().getTime();
		appspec = AppspecReader.readProject(is);
		long endTime = new Date().getTime();

		Assert.assertNotNull(appspec, "Unable to read sample project");
		return endTime - startTime;
	}
}