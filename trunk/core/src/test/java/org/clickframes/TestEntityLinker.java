package org.clickframes;

import javax.xml.bind.JAXBException;

import org.clickframes.model.Appspec;
import org.clickframes.model.AppspecConstraintViolationException;
import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class TestEntityLinker extends AbstractPluginTest{

	public void test() throws JAXBException, AppspecConstraintViolationException{
		Appspec appspec = createTechspecContext().getAppspec();
		Assert.assertNotNull(appspec);

		Appspec.resolveEntities(appspec);
	}
}
