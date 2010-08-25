package org.clickframes.objectfactory;

import org.clickframes.objectfactory.MVCControllerFactory;
import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.annotations.Test;

@Test
public class TestMVCControllerFactory extends AbstractPluginTest{
	public void test(){
		MVCControllerFactory.createMVCController(techspecContext.getAppspec().getDefaultPage());
	}
}
