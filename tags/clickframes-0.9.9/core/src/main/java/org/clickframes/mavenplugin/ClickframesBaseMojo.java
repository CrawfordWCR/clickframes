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

import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.clickframes.AppspecReader;
import org.clickframes.InternalLinkResolutionException;
import org.clickframes.VelocityHelper;
import org.clickframes.model.Appspec;
import org.clickframes.model.AppspecConstraintViolationException;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.techspec.Techspec;
import org.clickframes.techspec.TechspecReader;

/**
 * @author Vineet Manohar
 */
public abstract class ClickframesBaseMojo extends AbstractMojo {
    public void execute() throws MojoExecutionException {
        File techspecXml = getTechspecXml();
        File appspecXml = getAppspecXml();
        File outputDirectory = getOutputDirectory();

        if (appspecXml == null) {
            throw new MojoExecutionException("Unable to open appspec xml file: " + appspecXml
                    + ". The expected formats is foo/bar/appspec.xml. The file should be available in classpath.");
        }

        if (!appspecXml.exists()) {
            throw new MojoExecutionException("appspec xml file does not exist: [" + appspecXml.getAbsolutePath() + "]");
        }

        if (techspecXml == null) {
            throw new MojoExecutionException("Unable to open techspec xml file: " + techspecXml
                    + ". The expected formats is foo/bar/techspec.xml. The file should be available in classpath.");
        }

        if (!techspecXml.exists()) {
            throw new MojoExecutionException("techspec xml file does not exist: [" + techspecXml.getAbsolutePath()
                    + "]");
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

    protected abstract File getClickframesDirectory();

    protected File getOutputDirectory() {
        return new File(".");
    }

    protected final File getTechspecXml() {
        return new File(getClickframesDirectory(), "techspec.xml");
    }

    protected final File getAppspecXml() {
        return new File(getClickframesDirectory(), "appspec.xml");
    }

    protected abstract void processProject(Techspec techspec, Appspec appspec) throws Exception;

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
}