package org.clickframes.testframes;

import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.annotations.Test;

public class TestPreparationUtilTest extends AbstractPluginTest {
    @Test
    public void testPrepareRegexTests() throws Exception {
        TestPreparationUtil.prepareTestSuitsWithRegex(null, techspecContext.getTechspec(), techspecContext.getAppspec(),
                "suite.*html");
    }
}