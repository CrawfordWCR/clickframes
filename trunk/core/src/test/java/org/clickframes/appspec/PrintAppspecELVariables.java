package org.clickframes.appspec;

import java.util.Map;

import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.annotations.Test;

/**
 * Proof of concept
 * 
 * @author Steven Boscarine
 * 
 */
@Test
public class PrintAppspecELVariables extends AbstractPluginTest {
	public void test() {
		final Map<String, Object> vars = AppspecPrinter.printAppspecAsELVariables(techspecContext.getAppspec(), true);
		for (String in : vars.keySet()) {
			logger.debug(in + "\t\t" + vars.get(in));
//			System.out.println(in + "\t\t" + vars.get(in));
		}
	}
}