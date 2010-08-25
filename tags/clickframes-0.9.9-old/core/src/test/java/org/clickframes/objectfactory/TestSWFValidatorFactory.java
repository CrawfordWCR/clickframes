package org.clickframes.objectfactory;

import org.clickframes.model.Form;
import org.clickframes.model.Page;
import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(enabled=false)
public class TestSWFValidatorFactory extends AbstractPluginTest{
	public void test(){
		Page page = techspecContext.getAppspec().getDefaultPage();
		Form form = page.getForms().get(0);
		Assert.assertNotNull(page);
		Assert.assertNotNull(form);
		logger.debug(SWFValidatorFactory.createValidator(page, form));
	}

}
