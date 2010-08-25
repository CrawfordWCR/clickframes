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

package org.clickframes.testframes;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.clickframes.mavenplugin.ClickframesBaseMojo;
import org.clickframes.model.Appspec;
import org.clickframes.techspec.Techspec;

/**
 * Goal which generates a seam project controller file per page
 */
public abstract class AbstractSeleniumTestPlugin extends ClickframesBaseMojo {
    /**
     * @parameter expression="${project.build.directory}/clickframes"
     * @required
     */
    private File outputDirectory;
    /**
     * @parameter expression="${project.basedir}/src/main/clickframes"
     * @required
     */
    private File clickframesDirectory;

    /**
     * @parameter expression="${clickframes.page}"
     */
    protected String page;

    /**
     * @parameter expression="${clickframes.regex}"
     */
    protected String regex;

    /**
     * @parameter expression="${clickframes.baseUrl}"
     */
    protected String baseUrl;

    /**
     * @parameter expression="${clickframes.browser}"
     */
    protected String browser;

    /**
     * @parameter expression="${clickframes.profile}"
     */
    protected String profile = "default";

    @Override
    public final void processProject(Techspec techspec, Appspec appspec) throws MojoExecutionException {
        try {
            TestProfile testProfile = createProfile(techspec, appspec);

            processProjectTests(testProfile, techspec, appspec);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract void processProjectTests(TestProfile testProfile, Techspec techspec, Appspec appspec)
            throws Exception;

    private TestProfile createProfile(Techspec techspec, Appspec appspec) {
        TestProfile testProfile = TestProfile.create(appspec.getId(), profile, baseUrl, browser);

        return testProfile;
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public File getClickframesDirectory() {
        return clickframesDirectory;
    }

    public String getPage() {
        return page;
    }

    public String getRegex() {
        return regex;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}