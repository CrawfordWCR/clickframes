package org.clickframes.techspec;

import java.io.File;
import java.util.List;

import org.clickframes.model.Appspec;
import org.clickframes.plugins.autoscan.AbstractPluginTest;
import org.testng.annotations.Test;

@Test(enabled = true)
public class TechspecRunnerTest extends AbstractPluginTest {
    public void testRun() {
        Techspec techspec = techspecContext.getTechspec();
        Appspec appspec = techspecContext.getAppspec();

        // placeholder. TODO: Replace me with correct values.
        // List<String> autoscanTemplates =
        // RuntimeManifestGenerator.getAutoscanTemplatesFromClasspath();

        List<String> autoscanTemplates = TechspecRunner.getAutoscanTemplatesFromDirectory(new File("src"
                + File.separator + "main" + File.separator + "resources" + File.separator + "clickframes"), "debug");

        autoscanTemplates.addAll(TechspecRunner.getAutoscanTemplatesFromDirectory(new File("src" + File.separator
                + "main" + File.separator + "resources" + File.separator + "clickframes"), "jsf"));

        autoscanTemplates.addAll(TechspecRunner.getAutoscanTemplatesFromDirectory(new File("src" + File.separator
                + "test" + File.separator + "resources" + File.separator + "clickframes"), "unittest"));

        TechspecRunner.run(techspec, appspec, autoscanTemplates);
    }
}