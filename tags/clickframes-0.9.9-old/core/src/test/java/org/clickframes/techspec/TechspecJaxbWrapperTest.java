package org.clickframes.techspec;

import static org.testng.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.clickframes.xmlbindings.techspec.ObjectFactory;
import org.clickframes.xmlbindings.techspec.PluginType;
import org.clickframes.xmlbindings.techspec.PluginsType;
import org.testng.annotations.Test;

public class TechspecJaxbWrapperTest {
    @Test
    public void testToXml() throws JAXBException {
        InputStream is = TechspecJaxbWrapperTest.class.getResourceAsStream("/techspec_junit.xml");
        assertNotNull(is, "Sample techspec file not found");
        org.clickframes.xmlbindings.techspec.Techspec techspecType = TechspecJaxbWrapper.inputStreamToJava(is);
        assertNotNull(techspecType, "Unable to read sample techspec");
    }

    @Test
    public void testInputStreamToJava() throws JAXBException {
        ObjectFactory objectFactory = new ObjectFactory();
        org.clickframes.xmlbindings.techspec.Techspec techspecType = objectFactory.createTechspec();
        PluginsType pluginsType = objectFactory.createPluginsType();
        PluginType autoscanType = objectFactory.createPluginType();
        autoscanType.setClazz("bdb");
        pluginsType.getPlugin().add(autoscanType);
        techspecType.setPlugins(pluginsType);
        String xml = TechspecJaxbWrapper.toXml(techspecType);
        assertNotNull(xml);
    }
}
