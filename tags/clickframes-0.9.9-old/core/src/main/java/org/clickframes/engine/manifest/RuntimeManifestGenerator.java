package org.clickframes.engine.manifest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility to find clickframes templates from a compiled Jar in the classpath.
 *
 * @author Steven Boscarine
 *
 */
public class RuntimeManifestGenerator {
    /**
     * Our convention for the root folder in which you'll put your autoscan
     * plugins template sets.
     */
    static final String ROOT_FOLDER = "clickframes";
    /**
     * Our convention for the first directory in the plugin. For example:
     * /clickframes/jsf/autoscan/src/main/webapp/WEB-INF/web.xml
     */
    static final String AUTOSCAN_FOLDER_NAME = "/autoscan/";
    private static final Log logger = LogFactory.getLog(RuntimeManifestGenerator.class);

    /**
     * @param jarIdentifiers
     *            use this parameter to narrow your serach. For example, if you
     *            entered "clickframes", "clickframes-core", it would select
     *            ~/.m2
     *            /repository/org/clickframes/clickframes-core/0.9.2-SNAPSHOT
     *            /clickframes-core-0.9.2-SNAPSHOT.jar, but reject
     *            ~/.m2/repository
     *            /org/clickframes/clickframes-mvc-archetype/0.9.2
     *            -SNAPSHOT/clickframes-mvc-archetype -0.9.2-SNAPSHOT.jar
     * @return list of classpath-relative paths from Jars to Clickframes
     *         templates
     */
    public static List<String> getAutoscanTemplatesFromClasspath(String... jarIdentifiers) throws IOException {
        final String currentClasspathAsString = System.getProperty("java.class.path");
        return getAutoscanTemplatesFromProvidedClasspath(currentClasspathAsString, jarIdentifiers);
    }

    public static List<String> getAutoscanTemplatesFromProvidedClasspath(final String currentClasspathAsString,
            String... jarIdentifiers) throws IOException {
        final String[] classpathEntries = currentClasspathAsString.split(System.getProperty("path.separator"));
        return getAutoscanTemplatesFromClasspath(Arrays.asList(classpathEntries), jarIdentifiers);
    }

    public static List<String> getAutoscanTemplatesFromClasspath(List<String> classpathEntries,
            String... jarIdentifiers) throws IOException {
        final List<String> entries = new ArrayList<String>();
        for (String jarName : classpathEntries) {
            if (amIanAutoscanJar(jarName, jarIdentifiers)) {
                // logger.debug("Scanning " + jarName + " for templates");
                JarFile jar = new JarFile(jarName);
                entries.addAll(getAutoscanTemplatesFromJar(jar));
            }
        }
        // logger.info("Found " + entries.size() + " templates ");
        return entries;
    }

    public static List<String> getAutoscanTemplatesFromJar(final JarFile jar) {
        final List<String> entries = new ArrayList<String>();
        JarEntry entry = null;
        for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); entry = e.nextElement()) {
            if (entry != null && amIaClickframesTemplate(entry)) {
                entries.add(entry.getName());
            }
        }
        return entries;
    }

    /**
     * @return true if entry is a jar and contains all tokens passed.
     */
    private static boolean amIanAutoscanJar(String entry, String... tokens) {
        // we're only interested in jars.
        if (!entry.endsWith(".jar")) {
            return false;
        }
        // reject if any token is missing.
        for (String token : tokens) {
            if (!entry.contains(token)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if template is not a directory or compiled class and was
     *         placed in the correct directory.
     */
    private static boolean amIaClickframesTemplate(JarEntry entry) {
        String name = entry.getName();
        // Compiled artifacts cannot be passed through Velocity.
        if (name.endsWith(".class")) {
            return false;
        }
        // this means it is a directory.
        if (name.endsWith("/")) {
            return false;
        }
        // our enforced naming convention.
        if (name.startsWith(ROOT_FOLDER) && name.contains(AUTOSCAN_FOLDER_NAME)) {
            return true;
        }
        // logger.debug("rejecting " + name +
        // " please confirm this is not a mistake");
        return false;
    }
}