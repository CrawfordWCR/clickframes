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
import java.io.IOException;

import org.clickframes.DefaultAppspecCustomizer;
import org.clickframes.mavenplugin.ClickframesBaseMojo;
import org.clickframes.model.Appspec;
import org.clickframes.techspec.Techspec;

/**
 * Goal which generates a seam project controller file per page
 *
 * @goal generate-tests
 *
 * @phase generate-resources
 */
public class SeleniumTestGeneratorMojo extends ClickframesBaseMojo {
    /**
     * @parameter expression="${project.basedir}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter
     *            expression="${project.basedir}/src/main/clickframes"
     * @required
     */
    private File clickframesDirectory;

    @Override
    protected void processProject(Techspec techspec, Appspec appspec) throws IOException {
        TestGenerator.generateAllTests(appspec, new DefaultAppspecCustomizer());
    }

    @Override
	public File getOutputDirectory() {
        return outputDirectory;
    }

    public File getClickframesDirectory() {
        return clickframesDirectory;
    }
}