package org.clickframes.testframes.selenium;

import java.util.ArrayList;
import java.util.List;

public class SeleniumTestSuite {
    private String name;
    private List<SeleniumTestCase> testCases = new ArrayList<SeleniumTestCase>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SeleniumTestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<SeleniumTestCase> testCases) {
        this.testCases = testCases;
    }
}