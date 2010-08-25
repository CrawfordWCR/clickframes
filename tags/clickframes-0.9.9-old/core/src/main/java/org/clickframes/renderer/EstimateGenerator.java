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

package org.clickframes.renderer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.graph.GraphGenerator;
import org.clickframes.graph.GraphUtils;
import org.clickframes.graph.GraphingEngine;
import org.clickframes.model.Appspec;
import org.clickframes.model.Page;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.plugins.requirements.Requirement;
import org.clickframes.techspec.Techspec;

import com.oy.shared.lm.graph.Graph;

public class EstimateGenerator {
	private static final int BASE_EU_PER_PAGE = 0;
	private static final int SINGLE_USER_INPUT_EU = 1;
	private static final int PAGE_REQUIREMENT_EU = 4;
	private static final Log log = LogFactory.getLog(EstimateGenerator.class);

	public static void generateEstimatesGraph(Appspec appspec, Techspec techspec) throws IOException {
		if (!GraphUtils.isGraphingAvailable()) {
			log.warn("Graphing is not installed");
			return;
		}

		ProjectEstimate projectEstimate = generateEstimates(appspec);

		Graph projectGraph = GraphGenerator.buildProjectEstimateGraph(appspec, projectEstimate);

		File estimatesDirectory = new File(techspec.getOutputDirectory(), "estimates");

		GraphUtils.toHtmlAndPng(GraphingEngine.DOT, projectGraph, new File(estimatesDirectory, "project.html"),
				new File(estimatesDirectory, "project.png"));
	}

	public static ProjectEstimate generateEstimates(Appspec appspec) {
		List<Requirement> requirements = Requirement.generateRequirementsForProject(appspec);

		ProjectEstimate projectEstimate = new ProjectEstimate();

		Map<String, Integer> pageIdNumRequirementsMap = new HashMap<String, Integer>();

		for (Requirement requirement : requirements) {
			Page page = requirement.getPage();
			if (page == null) {
				// collect global estimates
				// TODO
				continue;
			}
			if (!pageIdNumRequirementsMap.containsKey(page.getId())) {
				pageIdNumRequirementsMap.put(page.getId(), BASE_EU_PER_PAGE);
			}
			int oldEstimate = pageIdNumRequirementsMap.get(page.getId());
			int estimateForRequirement = getEstimateForRequirement(requirement);
			pageIdNumRequirementsMap.put(page.getId(), oldEstimate + estimateForRequirement);
		}

		for (String pageId : pageIdNumRequirementsMap.keySet()) {
			Page page;
			try {
				page = appspec.getPage(pageId);
			} catch (PageNotFoundException e) {
				throw new RuntimeException("Developer error", e);
			}

			// add implementation factor to points
			if (page.getProperties().containsKey("implementationFactor")) {
				String implementationFactor = page.getProperties().get("implementationFactor");
				int implFactor = Integer.parseInt(implementationFactor);

				int oldEstimate = pageIdNumRequirementsMap.get(page.getId());
				pageIdNumRequirementsMap.put(page.getId(), oldEstimate + implFactor);
			}

			PageEstimate pageEstimate = new PageEstimate(page, pageIdNumRequirementsMap.get(page.getId()));

			projectEstimate.addPageEstimate(pageEstimate);
		}

		return projectEstimate;
	}

	private static int getEstimateForRequirement(Requirement requirement) {
		if (requirement.getSingleUserInput() != null) {
			return SINGLE_USER_INPUT_EU;
		}

		return PAGE_REQUIREMENT_EU;
	}

	public static void generateEstimatesHtml(Appspec appspec, Techspec techspec) {
		ProjectEstimate projectEstimate = generateEstimates(appspec);

		File outputDir = new File(techspec.getOutputDirectory(), "estimates");
		// HtmlGenerator.createOutputDirectoryIfNeeded(outputDir);

		//String filename = HtmlGenerator.generateFilename(appspec);
		//File outputFile = new File(outputDir, filename);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appspec", appspec);
		params.put("projectEstimate", projectEstimate);
		//VelocityHelper.runMacro(params, "estimates.vm", outputFile);
	}
}