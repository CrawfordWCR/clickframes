package org.clickframes.plugins.autoscan;

import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.AppspecReader;
import org.clickframes.model.Appspec;
import org.clickframes.model.AppspecConstraintViolationException;
import org.clickframes.techspec.Techspec;
import org.clickframes.techspec.TechspecContext;
import org.clickframes.techspec.TechspecReader;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test
public class AbstractPluginTest {
    protected final Log logger = LogFactory.getLog(getClass());
    protected static TechspecContext techspecContext;

    @BeforeTest
    public void generateManifests() throws JAXBException, AppspecConstraintViolationException {
        techspecContext = createTechspecContext();
        logger.info("initialized");
    }

    public static TechspecContext createTechspecContext() throws JAXBException, AppspecConstraintViolationException {
        Techspec techspec = createTechspec("/techspec_autoscan_test.xml");
        assertNotNull(techspec);

        File baseDir = new File(new File(System.getProperty("java.io.tmpdir")), "appspec_junit_test");
        baseDir.mkdirs();
        techspec.setOutputDirectory(baseDir);

        Appspec appspec = createAppspec();

        techspecContext = new TechspecContext(techspec, appspec);
        return techspecContext;
    }

    private static Appspec createAppspec() throws JAXBException, AppspecConstraintViolationException {
        InputStream is = AbstractPluginTest.class.getResourceAsStream("/appspec_junit.xml");
        assertNotNull(is, "Sample file not found");
        Appspec appspec = AppspecReader.readProject(is);
        assertNotNull(appspec, "Unable to read sample appspec");

        // enable autoscan to run
        File baseDir = new File(new File(System.getProperty("java.io.tmpdir")), "appspec_junit_test");
        baseDir.mkdirs();

        return appspec;
    }

    protected static Techspec createTechspec(String filename) throws JAXBException {
        InputStream is = TechspecReader.class.getResourceAsStream(filename);
        assertNotNull(is, "Sample techspec file not found");
        Techspec techspec = TechspecReader.readTechspec(is, null);
        assertNotNull(techspec, "Unable to read sample techspec");
        assertNotNull(techspec.getPlugins());
        return techspec;
    }
}
