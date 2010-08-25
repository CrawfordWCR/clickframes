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

package org.clickframes;

import java.io.IOException;
import java.util.Map;

import org.clickframes.graph.GraphGenerator;
import org.clickframes.model.Appspec;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.renderer.EstimateGenerator;
import org.clickframes.techspec.Techspec;
import org.clickframes.testframes.TestGenerator;

public class AppspecTools {
    public static void generateAllArtifacts(Appspec appspec, Techspec techspec) throws IOException,
            PageNotFoundException {
        generateAllArtifacts(appspec, null, techspec);
    }

    // public static void generateClickframes(Appspec appspec, Map<String,
    // Object> params) throws IOException {
    // HtmlGenerator.generate(appspec, params);
    // }

    public static void generateClickframesAndTestArtifacts(Appspec appspec, Map<String, Object> params)
            throws IOException {
        TestGenerator.generateTestStrategy(appspec, params);
    }

    public static void generateAllArtifactsNoGraphs(Appspec appspec, Map<String, Object> params, Techspec techspec)
            throws IOException {
        // HtmlGenerator.generate(appspec, params);
        EstimateGenerator.generateEstimatesHtml(appspec, techspec);
        // SeamGenerator.generateAll(appspec);
        TestGenerator.generateAllTests(appspec, new DefaultAppspecCustomizer());
    }

    public static void generateAllArtifacts(Appspec appspec, Map<String, Object> params, Techspec techspec)
            throws IOException, PageNotFoundException {
        // HtmlGenerator.generate(appspec, params);
        EstimateGenerator.generateEstimatesGraph(appspec, techspec);
        EstimateGenerator.generateEstimatesHtml(appspec, techspec);
        GraphGenerator.generate(techspec, appspec);
    }
}