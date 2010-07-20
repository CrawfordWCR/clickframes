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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.clickframes.model.test.ProjectTestResults;
import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestRunnerTest extends AbstractPluginTest {
    private boolean disabled = true;
    private String baseUrl = "http://localhost:8080/";
    private static TestProfile testProfile;

    @BeforeClass
    public void beforeClass() {
        testProfile = TestProfile.create("unittest", "unittest", baseUrl, null);
    }

    @Test
    public void testAggregrateTestResultsFile() throws IOException {
        File zipFile = TestRunner.aggregrateTestResults(new File(".", "src/test/resources/sample"), "all");
        // ProjectTestResults projectTestResults =
        // TestResultsParser.parseTestResults(zipFile);
        TestResultsParser.parseTestResults(zipFile);
    }

    @Test
    public void testParseTestResults() throws IOException {
        // generate some data
        File resultsFile = TestRunner.aggregrateTestResults(new File(".", "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + "sample"), "all");

        // make sure results file looks rights
        assertNotNull(resultsFile, "Results file should not be null");
        assertTrue(resultsFile.exists() && resultsFile.length() > 0 && resultsFile.getName().endsWith(".zip"),
                "Results file should be ");

        // parse it
        ProjectTestResults results = TestResultsParser.parseTestResults(resultsFile);
        assertNotNull(results, "Oops! Test results are null");

        assertEquals(2, results.getTotalCount(), "Wrong number of total tests returned");
        assertEquals(0, results.getFailedCount(), "Wrong number of failed tests returned");
        assertEquals(2, results.getPassedCount(), "Wrong number of passed tests returned");
    }

    @Test
    public void testRunAllTestSuits() throws Exception {
        if (disabled) {
            return;
        }

        boolean disabled = true;
        if (disabled) {
            return;
        }
        ProjectTestResults projectTestResults = TestRunner.runAllTestSuits(testProfile, techspecContext.getTechspec(),
                techspecContext.getAppspec());
        assertNotNull(projectTestResults, "Appspec test results is null");
        assertEquals(3, projectTestResults.getTotalCount(), "Wrong number of total tests returned");
        assertEquals(0, projectTestResults.getFailedCount(), "Wrong number of failed tests returned");
        assertEquals(3, projectTestResults.getPassedCount(), "Wrong number of passed tests returned");
        assertTrue(projectTestResults.getExecutionTimeInMillis() < 60 * 1000, "Test took too much time");
    }

    @Test
    public void testPageTests() throws Exception {
        if (disabled) {
            return;
        }

        boolean disabled = false;
        if (disabled) {
            return;
        }
        ProjectTestResults projectTestResults = TestRunner.runPageTests(testProfile, techspecContext.getTechspec(),
                techspecContext.getAppspec(), "page1");
        assertNotNull(projectTestResults, "Appspec test results is null");
        assertEquals(1, projectTestResults.getTotalCount(), "Wrong number of total tests returned");
        assertEquals(0, projectTestResults.getFailedCount(), "Wrong number of failed tests returned");
        assertEquals(1, projectTestResults.getPassedCount(), "Wrong number of passed tests returned");
        assertTrue(projectTestResults.getExecutionTimeInMillis() < 60 * 1000, "Test took too much time");
    }

    @Test
    public void testRegexTests() throws Exception {
        if (disabled) {
            return;
        }

        ProjectTestResults projectTestResults = TestRunner.runTestSuitsWithRegex(testProfile, techspecContext
                .getTechspec(), techspecContext.getAppspec(), "TS_pgpage1_suite.html");
        assertNotNull(projectTestResults, "Appspec test results is null");
        assertEquals(1, projectTestResults.getTotalCount(), "Wrong number of total tests returned");
        assertEquals(0, projectTestResults.getFailedCount(), "Wrong number of total tests returned");
        assertEquals(1, projectTestResults.getPassedCount(), "Wrong number of passed tests returned");
        assertTrue(projectTestResults.getExecutionTimeInMillis() < 60 * 1000, "Test took too much time");
    }
}
