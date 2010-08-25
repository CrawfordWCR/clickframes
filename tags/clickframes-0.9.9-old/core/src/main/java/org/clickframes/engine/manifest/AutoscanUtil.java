package org.clickframes.engine.manifest;

import static org.clickframes.engine.manifest.RuntimeManifestGenerator.AUTOSCAN_FOLDER_NAME;
import static org.clickframes.engine.manifest.RuntimeManifestGenerator.ROOT_FOLDER;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.mortbay.log.LogFactory;

/**
 *
 * @author sboscarine
 *
 */
public class AutoscanUtil {
    @SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(AutoscanUtil.class);

    /**
     * Narrows large group of templates down to the ones used in your plugin.
     *
     * @param allTemplates
     *            All available templates
     * @param pluginName
     *            the plugin you're interested in using.
     * @return a list of templates needed for this plugin.
     */
    public static List<String> refineAutoscanTemplatesByPlugin(List<String> allTemplates, String pluginName) {
        List<String> templatesInMyPlugin = new ArrayList<String>();
        String strInPathToSearchFor = ROOT_FOLDER + "/" + pluginName + AUTOSCAN_FOLDER_NAME;
        for (String template : allTemplates) {
            if (template.matches("^/?" + strInPathToSearchFor + ".*$")) {
                templatesInMyPlugin.add(template);
            }
        }

        if (templatesInMyPlugin.size() == 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n\nLooking for templates in " + pluginName
                    + " plugin, but didn't find any check below for errors:\n");
            for (String template : allTemplates) {
                sb.append(template + "\n");
            }
            sb.append("total templates passed: " + allTemplates.size());
            // logger.warn(sb.toString());
        }

        return templatesInMyPlugin;
    }

    // private static List<String> refineAutoscanTemplatesByPlugin(String
    // pluginName) {
    // try {
    // List<String> allTemplatesInClasspath =
    // RuntimeManifestGenerator.getAutoscanTemplatesFromClasspath();
    // logger.info("found total of " + allTemplatesInClasspath.size() +
    // " templates.");
    // return refineAutoscanTemplatesByPlugin(allTemplatesInClasspath,
    // pluginName);
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }
}