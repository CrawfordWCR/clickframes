package org.clickframes.techspec;

/**
 * An excpetion throws in context of a plugin
 *
 * @author Vineet Manohar
 */
public class PluginException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Plugin plugin;

    public PluginException(Plugin plugin, String message, Throwable cause) {
        super(message + ", plugin: " + plugin.getClazz(), cause);
        setPlugin(plugin);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
}