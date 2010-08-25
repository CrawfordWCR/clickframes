package org.clickframes.plugins;

import org.clickframes.model.Appspec;
import org.clickframes.techspec.Plugin;
import org.clickframes.techspec.Techspec;
import org.clickframes.techspec.TechspecContext;

/**
 * Captures the context in which this plugin was called
 *
 * @author Vineet Manohar
 */
public class PluginContext extends TechspecContext {
    private Plugin plugin;

    public PluginContext(Plugin plugin, Techspec techspec, Appspec appspec) {
        super(techspec, appspec);
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
