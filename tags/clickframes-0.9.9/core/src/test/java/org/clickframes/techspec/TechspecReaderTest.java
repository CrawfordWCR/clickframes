package org.clickframes.techspec;

import static org.testng.Assert.assertNotNull;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Vineet Manohar
 */
public class TechspecReaderTest {
    private static Techspec techspec;

    @BeforeClass
    public static void beforeClass() throws JAXBException {
        techspec = createTechspec();
    }

    public static Techspec createTechspec() throws JAXBException {
        InputStream is = TechspecReader.class.getResourceAsStream("/techspec_junit.xml");
        assertNotNull(is, "Sample techspec file not found");
        Techspec techspec = TechspecReader.readTechspec(is, null);
        return techspec;
    }

    @Test
    public void testTechspec() {
        assertNotNull(techspec, "Unable to read sample techspec");
        Assert.assertEquals(techspec.getPackageName(), "net.clickframes.unittest.sampleapp");
    }

    @Test
    public void testPlugins() {
        List<Plugin> plugins = techspec.getPlugins();
        Assert.assertEquals(plugins.size(), 3);
    }

    @Test
    public void testPlugin() {
        Plugin plugin = techspec.getPlugins().get(2);
        Assert.assertEquals(plugin.getClazz(), "unittest");
    }

    @Test
    public void testPluginFilters() {
        Plugin plugin = techspec.getPlugins().get(2);
        List<Filter> filters = plugin.getFilters();

        Assert.assertEquals(filters.size(), 1);

        Filter filter = filters.get(0);
        Assert.assertEquals(filter.getType(), FilterType.INCLUDE);
        Assert.assertEquals(filter.getCondition(), "$page.id == 'home'");
    }
}