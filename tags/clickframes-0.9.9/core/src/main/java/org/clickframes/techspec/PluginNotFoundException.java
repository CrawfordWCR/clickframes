package org.clickframes.techspec;

/**
 * An included plugin cannot be found in classpath
 *
 * @author Vineet Manohar
 */
public class PluginNotFoundException extends PluginException {
    private static final long serialVersionUID = 1L;

    /**
     * The plugin definition which caused this problem
     *
     * @param plugin
     * @param e
     */
    public PluginNotFoundException(Plugin plugin, ClassNotFoundException e) {
        super(plugin, "Plugin specified by class: " + plugin.getClazz() + " "
                + "cannot be found. Please make sure that the class name is spelled correctly "
                + "and is present in your classpath.", e);
        setPlugin(plugin);
    }

}
