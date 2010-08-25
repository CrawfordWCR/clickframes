package org.clickframes.techspec;

import java.util.ArrayList;
import java.util.List;

import org.clickframes.xmlbindings.techspec.PluginType;

/**
 * Represents a plugin declaration in techspec
 *
 * @author Vineet Manohar
 *
 */
public class Plugin {
    private String clazz;
    private List<Property> properties = new ArrayList<Property>();

    private List<Filter> filters = new ArrayList<Filter>();

    public static List<Plugin> createList(List<PluginType> pluginTypeList) {
        List<Plugin> list = new ArrayList<Plugin>();
        for (PluginType pluginType : pluginTypeList) {
            list.add(create(pluginType));
        }
        return list;
    }

    public static Plugin create(PluginType pluginType) {
        Plugin plugin = new Plugin();
        plugin.setClazz(pluginType.getClazz());
        plugin.setFilters(Filter.createList(pluginType.getInclude()));
        plugin.getProperties().addAll(Property.createList(pluginType.getProperties()));
        return plugin;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return getClazz();
    }

    public List<Filter> getFilters() {
        return this.filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}