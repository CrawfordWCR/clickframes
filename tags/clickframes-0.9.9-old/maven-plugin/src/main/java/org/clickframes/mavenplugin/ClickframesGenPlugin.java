/*
 * Clickframes: Full lifecycle software development automation.
 * Copyright (C)  2009 Children's Hospital Boston
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.clickframes.mavenplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.clickframes.AppspecReader;
import org.clickframes.InternalLinkResolutionException;
import org.clickframes.VelocityHelper;
import org.clickframes.engine.manifest.RuntimeManifestGenerator;
import org.clickframes.model.Appspec;
import org.clickframes.model.AppspecConstraintViolationException;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.techspec.Techspec;
import org.clickframes.techspec.TechspecReader;
import org.clickframes.techspec.TechspecRunner;
import org.clickframes.techspec.manifest.TechspecManifest;
import org.clickframes.util.ClickframeUtils;
import org.clickframes.util.VelocityCodeGenerator;

/**
 * Generate source code and other resources from the appspec, by running the
 * plugins listed in the techspec
 * 
 * 1) This plugin reads src/main/clickframes/appspec.xml and creates an
 * application model
 * 
 * 2) It reads the plugins from the file src/main/clickframes/techspec.xml
 * 
 * It runs the plugins listed in the order in which they are specified in the
 * techspec.xml against the application specification specified in the
 * appspec.xml file and generates source code and other resources.
 * 
 * 
 * Bootstraping:
 * 
 * 1) If an appspec is not present, an empty appspec is created
 * 
 * 2) If a techspec is not present, a techspec is created with the list of
 * plugins specified by the command line parameter "plugins". If no parameter is
 * specified, the default value "clips" is used.
 * 
 * @goal gen
 * @requiresProject false
 * @author Vineet Manohar
 */
public class ClickframesGenPlugin extends AbstractMojo {
    public void execute() throws MojoExecutionException {
        // if called without the pom.xml
        if (basedir == null) {
            try {
                basedir = new File(".").getCanonicalFile();
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to get current directory name", e);
            }
        }

        bootstrap();

        File techspecXml = getTechspecXml();
        File appspecXml = getAppspecXml();
        File outputDirectory = getOutputDirectory();

        if (appspecXml == null) {
            throw new MojoExecutionException("Unable to open appspec xml file: " + appspecXml
                    + ". The expected formats is foo/bar/appspec.xml. The file should be available in classpath.");
        }

        if (!appspecXml.exists()) {

            if (!appspecXml.exists()) {
                throw new MojoExecutionException("appspec xml file does not exist: [" + appspecXml.getAbsolutePath()
                        + "]");
            }
        }

        if (techspecXml == null) {
            throw new MojoExecutionException("Unable to open techspec xml file: " + techspecXml
                    + ". The expected formats is foo/bar/techspec.xml. The file should be available in classpath.");
        }

        if (!techspecXml.exists()) {
            if (!techspecXml.exists()) {
                throw new MojoExecutionException("techspec xml file does not exist: [" + techspecXml.getAbsolutePath()
                        + "]");
            }
        }

        if (!outputDirectory.exists()) {
            getLog().info("Creating output directory " + outputDirectory.getAbsolutePath());
            outputDirectory.mkdirs();
        }

        Appspec appspec = readProject();
        Techspec techspec = readTechspec();

        try {
            VelocityHelper.initVelocity(getClickframesDirectory());
        } catch (Exception e) {
            throw new MojoExecutionException("IOException while initialing velocity", e);
        }

        try {
            processProject(techspec, appspec);
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Throwable t) {
            throw new MojoExecutionException(t.getMessage(), t);
        }
    }

    private void bootstrap() throws MojoExecutionException {
        bootstrapAppspec();
        bootstrapTechspec();
    }

    private void bootstrapAppspec() throws MojoExecutionException {
        // create appspec and copy it to the give location
        String id = getId();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("id", id);
        // TODO: create a new api call which does not required creating a fake
        // techspec
        Techspec techspec = new Techspec(null);
        techspec.setOutputDirectory(getBasedir());
        context.put("techspec", techspec);
        try {
            VelocityCodeGenerator.generateFromTemplate(context, "appspec.xml",
                    "/clickframes/bootstrap/autoscan/src/main/clickframes/appspec.xml",
                    "src/main/clickframes/appspec.xml");
        } catch (IOException e) {
            throw new MojoExecutionException("Could not generate appspec.xml", e);
        }
    }

    private String getId() {
        if (id == null) {
            return ClickframeUtils.cleanId(getBasedir().getName());
        }

        return id;
    }

    private String getJavaPackage() {
        if (javaPackage == null) {
            return getId();
        }

        return javaPackage;
    }

    private void bootstrapTechspec() throws MojoExecutionException {
        String id = getId();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("id", id);
        context.put("javaPackage", getJavaPackage());
        // TODO: create a new api call which does not required creating a fake
        // techspec
        Techspec techspec = new Techspec(null);
        techspec.setOutputDirectory(getBasedir());

        List<String> pluginList = new ArrayList<String>();
        if (plugins == null) {
            pluginList.add("clips");
        } else {
            for (String plugin : plugins.split(",")) {
                pluginList.add(plugin.trim());
            }
        }
        context.put("plugins", pluginList);
        context.put("techspec", techspec);

        try {
            VelocityCodeGenerator.generateFromTemplate(context, "techspec.xml",
                    "/clickframes/bootstrap/autoscan/src/main/clickframes/techspec.xml",
                    "src/main/clickframes/techspec.xml");
        } catch (IOException e) {
            throw new MojoExecutionException("Could not generate appspec.xml", e);
        }
    }

    protected File getOutputDirectory() throws MojoExecutionException {
        try {
            return new File(".").getCanonicalFile();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to get current directory name", e);
        }
    }

    protected final File getTechspecXml() {
        return new File(getClickframesDirectory(), "techspec.xml");
    }

    protected final File getAppspecXml() {
        return new File(getClickframesDirectory(), "appspec.xml");
    }

    protected Appspec readProject() throws MojoExecutionException {
        File appspecXml = getAppspecXml();

        InputStream is;
        try {
            is = new FileInputStream(appspecXml);
        } catch (IOException e1) {
            throw new MojoExecutionException("Unable to open appspec xml file: [" + appspecXml.getAbsolutePath()
                    + "]. The expected formats is /foo/appspec.xml. The file should be available in classpath.", e1);
        }

        Appspec appspec;

        try {
            appspec = AppspecReader.readProject(is);
        } catch (JAXBException e) {
            throw new MojoExecutionException(
                    "Could not read the xml, make sure that it is a valid xml and it validates against the schema", e);
        } catch (InternalLinkResolutionException e) {
            throw new MojoExecutionException("One of the pageRefs does could not be resolved: "
                    + e.getLink().getTitle() + ": " + e.getLink().getDescription() + ", pageRef="
                    + e.getLink().getPageRef(), e);
        } catch (PageNotFoundException e) {
            throw new MojoExecutionException(
                    "Either the default page or one of the pageRefs does could not be resolved: " + e.getPageId(), e);
        } catch (AppspecConstraintViolationException e) {
            throw new MojoExecutionException("Invalid appspec, constraint violation", e);
        }

        return appspec;
    }

    protected Techspec readTechspec() throws MojoExecutionException {
        File techspecXml = getTechspecXml();

        InputStream is;
        try {
            is = new FileInputStream(techspecXml);
        } catch (IOException e1) {
            throw new MojoExecutionException("Unable to open techspec xml file: [" + techspecXml.getAbsolutePath()
                    + "]. The expected formats is foo/techspec.xml. The file should be available in classpath.", e1);
        }

        Techspec techspec;

        try {
            techspec = TechspecReader.readTechspec(is, null);
        } catch (JAXBException e) {
            throw new MojoExecutionException(
                    "Could not read the xml, make sure that it is a valid xml and it validates against the techspec schema",
                    e);
        }

        return techspec;
    }

    /**
     * This parameter is used when starting a new project. Use this parameter to
     * set the application identifier of the project that you want to create.
     * The value is stored as <appspec id=""> appspec element's "id" attribute.
     * 
     * @parameter expression="${id}"
     */
    private String id;

    /**
     * This parameter is used when starting a new project. Use this parameter to
     * set the application's java package of the project that you want to
     * create. The value is stored as <techspec> <package>....</package>. If not
     * specified, the value defaults to the value of application identifier.
     * 
     * @parameter expression="${javaPackge}"
     */
    private String javaPackage;

    /**
     * This option should be used when starting a new project. A comma separated
     * list of plugins that should be added to the new project. For example:
     * "jboss5seam,clips".
     * 
     * If not specified, the default value is "clips"
     * 
     * @parameter expression="${plugins}"
     */
    private String plugins;

    /**
     * The base directory of the project.
     * 
     * 1) If not specified, the ${project.basedir} value is used.
     * 
     * 2) If not maven project pom.xml is present, the current directory is used
     * 
     * @readonly
     * @parameter expression="${project.basedir}"
     */
    private File basedir;

    public File getBasedir() {
        return basedir;
    }

    /**
     * Route to classpath
     * 
     * @parameter expression="${plugin.artifacts}"
     * @readonly
     * @required
     */
    protected List<DefaultArtifact> artifacts;

    /**
     * Maven's answer to
     * System.getProperty("java.class.path").split(System.getProperty
     * ("path.separator"));
     * 
     * @return list of each jar included in maven classpath.
     */
    private List<String> getMavenRuntimeClasspathEntries() {
        List<String> entries = new ArrayList<String>();
        for (DefaultArtifact o : artifacts) {
            entries.add(o.getFile().getAbsolutePath());
        }
        return entries;
    }

    @SuppressWarnings("unchecked")
    // @Override
    public void processProject(Techspec techspec, Appspec appspec) throws IOException, MojoExecutionException {
        List<String> classpathEntries = getMavenRuntimeClasspathEntries();
        List<String> autoscanTemplatesFromClasspath = RuntimeManifestGenerator
                .getAutoscanTemplatesFromClasspath(classpathEntries);

        File originalOutputDirectory = techspec.getOutputDirectory();
        File incomingDirectory = new File(originalOutputDirectory, "target" + File.separator + "clickframes-incoming");
        File modifiedDirectory = new File(originalOutputDirectory, "target" + File.separator + "clickframes-modified");

        try {
            if (modifiedDirectory.exists()) {
                FileUtils.deleteDirectory(modifiedDirectory);
            }
            // run the techspec in a temp directory
            if (incomingDirectory.exists()) {
                FileUtils.cleanDirectory(incomingDirectory);
            }
            techspec.setOutputDirectory(incomingDirectory);

            TechspecRunner.run(techspec, appspec, autoscanTemplatesFromClasspath);

            // update manifest from directory, using basepath
            TechspecManifest techspecManifest;

            File techspecManifestFile = new File(getClickframesDirectory(), "techspec-manifest.xml");

            if (!techspecManifestFile.exists()) {
                techspecManifest = TechspecManifest.create();
            } else {
                try {
                    techspecManifest = TechspecManifest.readTechspecManifest(new FileInputStream(techspecManifestFile));
                } catch (JAXBException e) {
                    throw new MojoExecutionException("Invalid manifest file", e);
                }
            }

            int addedCount = 0;
            int modifications = 0;
            for (File newFile : (Collection<File>) FileUtils.listFiles(techspec.getOutputDirectory(), FileFilterUtils
                    .trueFileFilter(), FileFilterUtils.trueFileFilter())) {
                addedCount += techspecManifest.addOrUpdateEntry(techspec.getOutputDirectory(), newFile) ? 1 : 0;

                // copy newFile from tmpdir to originalOutputDir, preserving the
                // relative path - if original source is not modified

                File dest = ClickframeUtils.getDestinationFile(techspec.getOutputDirectory(), newFile,
                        originalOutputDirectory);

                // if dest is modified, we want to keep a list of original
                // version of modified files handy
                if (dest.exists() && VelocityCodeGenerator.isModified(dest)) {
                    File modifiedDest = ClickframeUtils.getDestinationFile(techspec.getOutputDirectory(), newFile,
                            modifiedDirectory);
                    FileUtils.copyFile(newFile, modifiedDest);
                }

                boolean moved = VelocityCodeGenerator.moveIfContentNotModified(newFile, dest);

                if (!moved && VelocityCodeGenerator.isSameEmbeddedChecksum(newFile, dest)) {
                    // source template is not modified, but destination may have
                    // been
                    // customized
                    newFile.delete();
                    modifications += 1;
                }

                // remove empty directories - as we go along
                File parent = newFile.getParentFile();

                while (parent.list().length == 0
                        && parent.getCanonicalPath() != techspec.getOutputDirectory().getCanonicalPath()) {
                    parent.delete();
                    parent = parent.getParentFile();
                }
            }

            if (modifications > 0) {
                getLog().info(
                        modifications + " incoming changes blocked by local modifications, see "
                                + incomingDirectory.getCanonicalPath());
            }

            // save techspec manifest
            try {
                techspecManifestFile.getParentFile().mkdirs();
                FileUtils.writeStringToFile(techspecManifestFile, techspecManifest.toXml());
                // getLog().info(addedCount +
                // " entries added to manifest, wrote " +
                // techspecManifestFile.getCanonicalPath());
            } catch (JAXBException e) {
                throw new MojoExecutionException("Error saving manifest", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not clean the tmp directory:" + incomingDirectory.getAbsolutePath());
        } finally {
            techspec.setOutputDirectory(originalOutputDirectory);
        }
    }

    private File getClickframesDirectory() {
        return new File(getBasedir(), "src" + File.separator + "main" + File.separator + "clickframes");
    }
}