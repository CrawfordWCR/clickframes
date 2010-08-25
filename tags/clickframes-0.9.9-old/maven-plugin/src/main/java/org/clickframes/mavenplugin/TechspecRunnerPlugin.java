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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.clickframes.engine.manifest.RuntimeManifestGenerator;
import org.clickframes.model.Appspec;
import org.clickframes.techspec.Techspec;
import org.clickframes.techspec.TechspecRunner;
import org.clickframes.techspec.manifest.TechspecManifest;
import org.clickframes.util.ClickframeUtils;
import org.clickframes.util.VelocityCodeGenerator;

/**
 * @goal generate
 * 
 * @author Vineet Manohar
 */
public class TechspecRunnerPlugin extends ClickframesBaseMojo {
    /**
     * @parameter expression="${project.basedir}/src/main/clickframes"
     * @required
     */
    private File clickframesDirectory;

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
    public void processProject2(Techspec techspec, Appspec appspec) throws IOException, MojoExecutionException {
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

            File techspecManifestFile = new File(clickframesDirectory, "techspec-manifest.xml");

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

    @Override
    public void processProject(Techspec techspec, Appspec appspec) throws IOException, MojoExecutionException {
        processProject2(techspec, appspec);
    }

    public void processProject1(Techspec techspec, Appspec appspec) throws IOException, MojoExecutionException {
        List<String> classpathEntries = getMavenRuntimeClasspathEntries();
        List<String> autoscanTemplatesFromClasspath = RuntimeManifestGenerator
                .getAutoscanTemplatesFromClasspath(classpathEntries);

        // snapshot 1
        Collection<File> snapshot1 = FileUtils.listFiles(new File("."), FileFilterUtils.trueFileFilter(),
                FileFilterUtils.trueFileFilter());

        TechspecRunner.run(techspec, appspec, autoscanTemplatesFromClasspath);

        // snapshot 2
        Collection<File> snapshot2 = FileUtils.listFiles(new File("."), FileFilterUtils.trueFileFilter(),
                FileFilterUtils.trueFileFilter());

        TechspecManifest techspecManifest;

        File techspecManifestFile = new File(clickframesDirectory, "techspec-manifest.xml");

        if (!techspecManifestFile.exists()) {
            techspecManifest = TechspecManifest.create();
        } else {
            try {
                techspecManifest = TechspecManifest.readTechspecManifest(new FileInputStream(techspecManifestFile));
            } catch (JAXBException e) {
                throw new MojoExecutionException("Invalid manifest file", e);
            }
        }

        // add to manifest if present in snapshot2, but not in 1
        snapshot2.removeAll(snapshot1);

        for (File newFile : snapshot2) {
            techspecManifest.addOrUpdateEntry(techspec.getOutputDirectory(), newFile);
        }

        // save techspec manifest
        try {
            techspecManifestFile.getParentFile().mkdirs();
            FileUtils.writeStringToFile(techspecManifestFile, techspecManifest.toXml());
            getLog().info(snapshot2.size() + " changes detected, wrote " + techspecManifestFile.getCanonicalPath());
        } catch (JAXBException e) {
            throw new MojoExecutionException("Error saving manifest", e);
        }
    }

    public File getClickframesDirectory() {
        return clickframesDirectory;
    }
}