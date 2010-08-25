package org.clickframes.testframes;

import org.clickframes.testframes.selenium.SeleniumTestCase;
import org.clickframes.testframes.selenium.SeleniumTestStep;

/**
 * Transform steps when copying them
 *
 * @author Vineet Manohar
 */
public interface StepFilter {
    public SeleniumTestStep filter(SeleniumTestCase testCase, SeleniumTestStep testStep);
}