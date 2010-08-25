package org.clickframes.testframes.selenium;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SeleniumUtilsTest {
    private static File testSuiteFile = new File("src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "selenium-samples" + File.separator + "/test-suite1.html");
    private static File testCaseFile = new File("src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "selenium-samples" + File.separator + "/test-case1.html");
    private static Log log = LogFactory.getLog(SeleniumUtilsTest.class);

    @Test
    public void testTestSuiteToTestCase() throws IOException {
        File outputFile = File.createTempFile("test-case", ".html");

        File testCase = SeleniumUtils.testSuiteToTestCase(testSuiteFile, outputFile);
        Assert.assertNotNull(testCase);
        log.info(testCase.getAbsolutePath());
    }

    @Test
    public void testReadTestSuite() throws IOException {
        SeleniumTestSuite seleniumTestSuite = SeleniumUtils.readTestSuite(testSuiteFile);
        Assert.assertNotNull(seleniumTestSuite);
        Assert.assertEquals(seleniumTestSuite.getName(), "Test Suite");
        Assert.assertEquals(seleniumTestSuite.getTestCases().size(), 2);

        SeleniumTestCase testCase = seleniumTestSuite.getTestCases().get(0);
        Assert.assertEquals(testCase.getName(), "test-case1");
        Assert.assertEquals(testCase.getFile().getName(), "test-case1.html");
    }

    @Test
    public void testReadTestCase() {
        SeleniumTestCase seleniumTestCase = SeleniumTestCase.readTestCase(testCaseFile, null);
        Assert.assertNotNull(seleniumTestCase);
        Assert.assertEquals(seleniumTestCase.getName(), "test-case1");
        Assert.assertEquals(seleniumTestCase.getFile().getName(), "test-case1.html");
        Assert.assertEquals(seleniumTestCase.getSteps().size(), 2);
        SeleniumTestStep step = seleniumTestCase.getSteps().get(0);
        Assert.assertEquals(step.getCommand(), "open");
        Assert.assertEquals(step.getTarget(),
                "http://www.google.com/search?hl=en&q=java+hello+world&aq=f&oq=&aqi=g-e1g8g-s1");
        Assert.assertEquals(step.getValue(), "");
    }
}