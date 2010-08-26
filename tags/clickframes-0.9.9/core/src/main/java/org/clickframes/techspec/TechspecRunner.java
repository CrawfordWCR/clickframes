package org.clickframes.techspec;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.engine.AutoscanEngine3;
import org.clickframes.engine.InternalVariableResolver;
import org.clickframes.model.Appspec;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.plugins.PluginContext;
import org.clickframes.util.ClickframeUtils;

/**
 * This class takes a techspec and runs it.
 *
 * @author Vineet Manohar
 */
public class TechspecRunner {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(TechspecRunner.class);

    /**
     * Order of processing:
     *
     * 1) Any dependencies will be resolved first.
     * clickframes/com.mycompany.myplugin/techspec.xml
     *
     * 2) Autoscan will be run on the following folder
     * clickframes/com.mycompany.myplugin/autoscan/
     *
     * You can put custom autoscan handlers here:
     * com/mycompany/myplugin/AutoscanHandler.class
     *
     * 3) Custom class will be run, if present
     * com/mycompany/myplugin/ClickframesPlugin.class
     *
     * 4) Run local templates, if available src/main/clickframes/autoscan
     *
     * @param techspec
     * @param appspec
     *
     * @author Vineet Manohar
     */
    public static void run(Techspec techspec, Appspec appspec, List<String> allAutoscanTemplates) {
        runInternal(techspec, appspec, allAutoscanTemplates, true);
    }

    static void runInternal(Techspec techspec, Appspec appspec, List<String> allAutoscanTemplates,
            boolean runLocalTemplates) {
        // log.debug("Total " + allAutoscanTemplates.size() +
        // " templates for all plugins");

        for (Plugin plugin : techspec.getPlugins()) {
            String pluginPackageName = plugin.getClazz();

            // step 1: run dependencies
            // create plugin context
            PluginContext pluginContext = new PluginContext(plugin, techspec, appspec);

            Techspec dependentTechspec = runDependenciesIfTheyExist(pluginContext, pluginPackageName,
                    allAutoscanTemplates);

            // if dependency found, create a new plugin context - which is
            // customized by plugin's dependent techspec.xml
            if (dependentTechspec != null) {
                pluginContext = new PluginContext(plugin, dependentTechspec, appspec);
            }

            // step 2: autoscan
            // log.info("Found plugin: " + plugin);

            ClickframesPlugin clickframesPlugin = getCustomJavaPluginOrDefaultPlugin(plugin, pluginPackageName);

            autoscanIfPresent(pluginContext, pluginPackageName, allAutoscanTemplates, clickframesPlugin);

            // step 3: run custom java plugins, if present
            // log.info("Executing custom Java code: " +
            // clickframesPlugin.getClass().getName() + "...");
            clickframesPlugin.execute(pluginContext);
            // log.info("Executing custom Java code: " +
            // clickframesPlugin.getClass().getName() + ".");

            // log.info("Finished plugin: " + plugin + ", dependency = " +
            // dependentTechspec != null + ", autoscan = "
            // + autoscanFound + ", java plugin = " +
            // clickframesPlugin.getClass().getName());
        }

        TechspecContext localTechspecContext = new TechspecContext(techspec, appspec);

        if (runLocalTemplates) {
            runLocalTemplates(localTechspecContext);
        }
    }

    private static void runLocalTemplates(TechspecContext techspecContext) {
        File clickframesDir = new File("src" + File.separator + "main" + File.separator + "clickframes");
        List<String> scannedTemplates = getAutoscanTemplatesFromDirectory(clickframesDir, new File(clickframesDir,
                "autoscan"));

        List<String> templates = new ArrayList<String>();

        // remove first char as slash '/' if exists
        for (String template : scannedTemplates) {
            templates.add(template.replaceAll("^/clickframes", ""));
        }

        autoscanLocalFolder(techspecContext, "autoscan", templates, new ClickframesPlugin());
    }

    private static ClickframesPlugin getCustomJavaPluginOrDefaultPlugin(Plugin plugin, String pluginPackageName) {
        try {
            Class<?> clazz = null;

            for (String expandedPackageName : convertToExpandedPluginPackageNames(pluginPackageName)) {
                String classSuffix = ".Plugin";
                String javaClassName = expandedPackageName + classSuffix;
                try {
                    // log.info("Looking for class: " + javaClassName + "...");
                    clazz = Class.forName(javaClassName);
                } catch (ClassNotFoundException e) {
                    // throw new PluginNotFoundException(plugin, e);
                }

                if (clazz != null) {
                    break;
                }
            }

            if (clazz == null) {
                // log.info("No plugin found for '" + pluginPackageName + "'");
                return new ClickframesPlugin();
            }

            ClickframesPlugin clickframesPlugin = (ClickframesPlugin) clazz.newInstance();

            // log.info("Found plugin class: " + clazz.getName() +
            // ", executing it now...");

            return clickframesPlugin;
        } catch (InstantiationException e) {
            throw new PluginException(plugin, "Plugin class not be created", e);
        } catch (IllegalAccessException e) {
            throw new PluginException(plugin, "Plugin class not be created", e);
        }
    }

    /**
     * auto
     *
     * @param techspecContext
     * @param pluginPackageName
     * @return if we are reading from autoscan folder or
     *
     * @author Vineet Manohar
     * @param clickframesPlugin
     */
    private static boolean autoscanIfPresent(PluginContext pluginContext, String pluginPackageName,
            List<String> allAutoscanTemplates, ClickframesPlugin clickframesPlugin) {
        String autoscanDirectoryName = "clickframes/" + pluginPackageName + "/autoscan";

        InternalVariableResolver resolver = getVariableResolverForPlugin(pluginContext);

        AutoscanEngine3 engine = new AutoscanEngine3(pluginContext, resolver, autoscanDirectoryName,
                allAutoscanTemplates, clickframesPlugin);

        engine.process();

        // log.info("Finished autoscan on " + autoscanDirectoryName);
        return true;
    }

    /**
     * @author Vineet Manohar
     */
    private static boolean autoscanLocalFolder(TechspecContext techspecContext, String autoscanDirectoryName,
            List<String> allAutoscanTemplates, ClickframesPlugin clickframesPlugin) {
        // step 4: run local templates, if available
        // log.info("Looking for local templates in: " + autoscanDirectoryName);

        InternalVariableResolver resolver = new InternalVariableResolver(techspecContext);

        AutoscanEngine3 engine = new AutoscanEngine3(techspecContext, resolver, autoscanDirectoryName,
                allAutoscanTemplates, clickframesPlugin);

        engine.process();

        // log.info("Finished scanning local templates in: " +
        // autoscanDirectoryName);
        return true;
    }

    private static InternalVariableResolver getVariableResolverForPlugin(PluginContext pluginContext) {
        return new InternalVariableResolver(pluginContext);
    }

    /**
     * Runs a dependent techspec found in the plugin resource directory:
     * /clickframes/<plugin url>/techspec.xml
     *
     * @param techspecContext
     *            TODO
     * @param pluginUrl
     *
     * @return whether this was an internal dependency or not, or null if no
     *         dependency was found
     *
     * @author Vineet Manohar
     */
    private static Techspec runDependenciesIfTheyExist(PluginContext pluginContext, String pluginPackageName,
            List<String> autoscanTemplates) {
        URL dependencyTechspecUrl = getDependentTechspecUrl(pluginPackageName);
        // run the dependency, if it exists
        if (dependencyTechspecUrl != null) {
            // log.info("Found dependency: " + dependencyTechspecUrl);
            try {
                Techspec dependency = TechspecReader.readTechspec(dependencyTechspecUrl.openStream(), pluginContext
                        .getTechspec());

                runInternal(dependency, pluginContext.getAppspec(), autoscanTemplates, false);

                return dependency;
            } catch (JAXBException e) {
                throw new PluginException(pluginContext.getPlugin(), "could not parse techspec dependency", e);
            } catch (IOException e) {
                throw new PluginException(pluginContext.getPlugin(), "error reading techspec dependency", e);
            }
        }

        // no dependency was found
        // log.debug("No dependencies were found for plugin: " +
        // pluginPackageName);
        return null;
    }

    private static URL getDependentTechspecUrl(String pluginPackage) {
        return TechspecRunner.class.getResource(getPluginResourceBase(pluginPackage) + "/techspec.xml");
    }

    /**
     * here's the techspec.xml URL logic
     *
     * org.clickframes.plugins.graph => /clickframes/clips/
     *
     * com.mycompany.foo.myplugin => /clickframes/com.mycompany.foo.myplugin/
     *
     *
     * @param pluginPackage
     * @return
     *
     * @author Vineet Manohar
     */
    private static String getPluginResourceBase(String pluginPackage) {
        String pluginResourceBase = "/clickframes/" + pluginPackage.replaceAll("^org.clickframes.plugins.", "");
        return pluginResourceBase;
    }

    private static List<String> convertToExpandedPluginPackageNames(String className) {
        List<String> list = new ArrayList<String>();
        if (!isBuiltIn(className)) {
            list.add("org.clickframes.plugins." + className);
        }
        list.add(className);
        return list;
    }

    private static boolean isBuiltIn(String packageName) {
        return packageName.startsWith("org.clickframes.plugins.");
    }

    public static List<String> getAutoscanTemplatesFromDirectory(File clickframesDir, String plugin) {
        return getAutoscanTemplatesFromDirectory(clickframesDir, new File(clickframesDir, plugin + File.separator
                + "autoscan"));
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAutoscanTemplatesFromDirectory(File clickframesDir, File autoscanDir) {
        List<String> retVal = new ArrayList<String>();

        if (!clickframesDir.exists() || !clickframesDir.isDirectory() || !autoscanDir.exists()
                || !autoscanDir.isDirectory()) {
            return retVal;
        }

        for (File file : (Collection<File>) FileUtils.listFiles(autoscanDir, FileFilterUtils.trueFileFilter(),
                FileFilterUtils.makeSVNAware(FileFilterUtils.trueFileFilter()))) {
            String absolutePath = file.getAbsolutePath();

            String base = clickframesDir.getParentFile().getAbsolutePath();
            String relativePath = absolutePath.substring(base.length(), absolutePath.length());

            relativePath = relativePath.replaceAll(ClickframeUtils.getFileSeparatorLiteral(), "/");

            retVal.add(relativePath);
        }

        return retVal;
    }
}