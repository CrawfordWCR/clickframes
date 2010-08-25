package org.clickframes.jsf;

import org.clickframes.model.Appspec;
import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.Assert;
import org.testng.annotations.Test;
@Test
public class TestDynamicInputFactory extends AbstractPluginTest{
	public void simpleRuntimeExceptionTest(){
		Appspec appspec = techspecContext.getAppspec();
		Assert.assertNotNull(appspec);
		logger.debug("hello world" );
		logger.debug(DynamicInputFactory.createFormService(appspec));
	}
}
