package org.clickframes.plugins.autoscan;

import org.clickframes.plugins.PluginContext;
import org.clickframes.plugins.properties.Plugin;
import org.testng.annotations.Test;

@Test
public class TestPropertiesFile extends AbstractPluginTest {
    public void test() {
        Plugin propertiesPlugin = new Plugin();
        PluginContext pluginContext = new PluginContext(techspecContext.getTechspec().getPlugins().get(0),
                techspecContext.getTechspec(), techspecContext.getAppspec());
        propertiesPlugin.execute(pluginContext);
    }
}
