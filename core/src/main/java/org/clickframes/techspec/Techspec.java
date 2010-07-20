package org.clickframes.techspec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Represents list of plugins and other technical choices made to implement the
 * application
 *
 * @author Vineet Manohar
 */
public class Techspec {
    /**
     * The list of plugin directives to process
     */
    private List<Plugin> plugins = new ArrayList<Plugin>();

    /**
     * The base output directory, by default it is "." (the current directory)
     */
    private File outputDirectory = new File(".");

    private String packageName = "demo";

    private List<Property> properties = new ArrayList<Property>();

    public Techspec(Techspec parent) {
        if (parent != null) {
            setOutputDirectory(parent.getOutputDirectory());
            setPackageName(parent.getPackageName());
            getProperties().addAll(parent.getProperties());
        }
    }

    /**
     * if javaPackage is org.clickframes, then domain is clickframes.org
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public String getDomain() {
        if (packageName == null) {
            return null;
        }

        String[] segments = this.packageName.split("\\.");
        ArrayUtils.reverse(segments);
        return StringUtils.join(segments, ".");
    }

    /**
     * if javaPackage is org.clickframes, then path is org/clickframes
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public String getPackagePath() {
        if (packageName == null) {
            return "";
        }
        return this.packageName.replaceAll("\\.", "/");
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public String toString() {
        return "Techspec(" + getPlugins() + ", " + getOutputDirectory() + ")";
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public static Techspec create(org.clickframes.xmlbindings.techspec.Techspec techspecType, Techspec parent) {
        Techspec techspec = new Techspec(parent);

        if (techspecType.getPackage() != null) {
            techspec.setPackageName(techspecType.getPackage());
        }

        if (techspecType.getPlugins() != null) {
            techspec.getPlugins().addAll(Plugin.createList(techspecType.getPlugins().getPlugin()));
        }
        techspec.getProperties().addAll(Property.createList(techspecType.getProperties()));

        return techspec;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}