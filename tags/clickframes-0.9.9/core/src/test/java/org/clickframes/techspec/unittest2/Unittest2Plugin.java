package org.clickframes.techspec.unittest2;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Appspec;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.plugins.PluginContext;
import org.clickframes.techspec.Plugin;
import org.clickframes.techspec.Techspec;

/**
 *
 * @author Vineet Manohar
 *
 */
public class Unittest2Plugin extends ClickframesPlugin {
    private static final Log log = LogFactory.getLog(Unittest2Plugin.class);

    @Override
    public void execute(PluginContext pluginContext) {
        log.info("Executing plugin: Unittest");
        Plugin plugin = pluginContext.getPlugin();
        log.info("Class: " + plugin.getClazz());
        Techspec techspec = pluginContext.getTechspec();
        File outputDirectory = techspec.getOutputDirectory();
        log.info("Output directory: " + outputDirectory.getAbsolutePath());
        Appspec appspec = pluginContext.getAppspec();
        log.info(appspec.getDescription());
        log.info("Appspec has " + appspec.getPages().size() + " pages");
    }
}