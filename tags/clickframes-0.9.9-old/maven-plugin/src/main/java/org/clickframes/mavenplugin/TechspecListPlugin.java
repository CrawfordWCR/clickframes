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
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.clickframes.engine.manifest.RuntimeManifestGenerator;
import org.clickframes.model.Appspec;
import org.clickframes.techspec.Techspec;
import org.clickframes.techspec.TechspecContext;
import org.clickframes.techspec.manifest.TechspecManifest;
import org.clickframes.techspec.manifest.TechspecManifestEntry;

/**
 * @goal list
 * 
 * @author Vineet Manohar
 */
public class TechspecListPlugin extends ClickframesBaseMojo {
    /**
     * @parameter expression="${project.basedir}/src/main/clickframes"
     * @required
     */
    private File clickframesDirectory;

    /**
     * @parameter expression="${type}"
     */
    private String type;

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

    @Override
    public void processProject(Techspec techspec, Appspec appspec) throws IOException, MojoExecutionException {
        List<String> classpathEntries = getMavenRuntimeClasspathEntries();
        List<String> autoscanTemplatesFromClasspath = RuntimeManifestGenerator
                .getAutoscanTemplatesFromClasspath(classpathEntries);

        TechspecContext techspecContext = new TechspecContext(techspec, appspec);

        File techspecManifestFile = new File(clickframesDirectory, "techspec-manifest.xml");

        if (!techspecManifestFile.exists()) {
            getLog().warn("No entries found, make sure to generate first");
            return;
        }

        try {
            TechspecManifest techspecManifest = TechspecManifest.readTechspecManifest(new FileInputStream(
                    techspecManifestFile));

            List<TechspecManifestEntry> entries;

            if (type == null) {
                type = "all";
            }

            if (type.equals("orphan")) {
                entries = techspecManifest.getOrphans(techspecContext, autoscanTemplatesFromClasspath);
            } else if (type.equals("modified")) {
                entries = techspecManifest.getModified();
            } else if (type.equals("notModified")) {
                entries = techspecManifest.getNotModified();
            } else {
                entries = techspecManifest.getAll();
            }

            getLog().info(
                    "Listing " + type + " entries: " + " (showing " + entries.size() + "/"
                            + techspecManifest.getEntries().size() + " total)");
            TechspecManifest.print(entries);
        } catch (JAXBException e) {
            throw new MojoExecutionException("Invalid manifest file: " + techspecManifestFile.getAbsolutePath(), e);
        }

        // Runner.run(techspec, appspec, autoscanTemplatesFromClasspath);
    }

    public File getClickframesDirectory() {
        return clickframesDirectory;
    }
}