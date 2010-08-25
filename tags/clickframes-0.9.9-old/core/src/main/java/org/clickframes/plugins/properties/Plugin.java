package org.clickframes.plugins.properties;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Appspec;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.plugins.PluginContext;
import org.clickframes.swf.MonolithicPropertiesFileGenerator;

/**
 * Generate properties file from appspec
 *
 * @author Steven Boscarine
 *
 */
public class Plugin extends ClickframesPlugin {

    @SuppressWarnings("unused")
	private final Log logger = LogFactory.getLog(getClass());
    /**
     * Where messages are written, such as form captions and validation
     * messages.
     */
    private static final String PROPERTIES_FILE = "src" + File.separator + "main" + File.separator + "resources"
            + File.separator + "messages.properties";


//    private static final String PROPERTIES_FOLDER=  "src" + File.separator + "main" + File.separator + "resources"
//    + File.separator + "properties";
    @Override
    public void execute(PluginContext pluginContext) {
        Appspec appspec = pluginContext.getAppspec();
        final File propertiesFileInTarget = new File(pluginContext.getTechspec().getOutputDirectory(), PROPERTIES_FILE);
        //the actual source file, not the one in target/clickframes-incoming
        final File propertiesFileInSrc = new File(pluginContext.getTechspec().getOutputDirectory().getParentFile().getParentFile(), PROPERTIES_FILE);
        // prevents exceptions when user didn't use archetype
        propertiesFileInTarget.getParentFile().mkdirs();
        //Merge existing file if available.
        //TODO:  Get a better variable name + explanation in here
        final File finalDestination = propertiesFileInSrc.exists()?propertiesFileInSrc:propertiesFileInTarget;
		MonolithicPropertiesFileGenerator.writeToPropertiesFile(finalDestination, appspec);

        //new way
//        final File propertiesFolder = new File(pluginContext.getTechspec().getOutputDirectory(), PROPERTIES_FOLDER);
//        MultiPagePropertiesFileGenerator generator = new MultiPagePropertiesFileGenerator(false, appspec, propertiesFolder);
//        generator.writeToPropertiesFile();
    }

}
