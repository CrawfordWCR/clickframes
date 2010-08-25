package org.clickframes.engine;

import java.util.List;

import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.plugins.PluginContext;
import org.clickframes.techspec.TechspecContext;

public class AutoscanEngine3 extends AutoscanEngineAPI3 {
    /**
     * Use this constructor if you want to use your own template call factory
     *
     * @param appspec
     * @param callFactory
     * @param templates
     */
    public AutoscanEngine3(TechspecContext techspecContext, InternalVariableResolver internalVariableResolver,
            String autoscanDirectoryName, List<String> templates, ClickframesPlugin clickframesPlugin) {
        super(techspecContext, internalVariableResolver, autoscanDirectoryName, templates, clickframesPlugin);
    }

    public AutoscanEngine3(PluginContext pluginContext, InternalVariableResolver internalVariableResolver,
            String autoscanDirectoryName, List<String> templates, ClickframesPlugin clickframesPlugin) {
        super(pluginContext, internalVariableResolver, autoscanDirectoryName, templates, clickframesPlugin);
    }
}