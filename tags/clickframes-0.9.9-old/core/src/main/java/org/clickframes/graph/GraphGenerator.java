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

package org.clickframes.graph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.ExternalLink;
import org.clickframes.model.Form;
import org.clickframes.model.InternalLink;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Page;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.renderer.PageEstimate;
import org.clickframes.renderer.ProjectEstimate;
import org.clickframes.techspec.Techspec;

import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.graph.GraphEdge;
import com.oy.shared.lm.graph.GraphFactory;
import com.oy.shared.lm.graph.GraphGroup;
import com.oy.shared.lm.graph.GraphNode;
import com.oy.shared.lm.in.StringUtil;

public class GraphGenerator {
    private static final Log log = LogFactory.getLog(GraphGenerator.class);
    private static final String RED = "#ff0000";

    private static final String GREEN = "#009900";

    private static final String BLUE = "blue";
    // private static final String RAINBOW =
    // "##ff00ff:#ffffff:#00ffff:#000000:#00ff00:#ffff00:#ff0000:#0000ff";

    private static final String GRAY = "gray93";

    private static final String POSITIVE_OUTCOME_FONT_COLOR = GREEN;

    private static final String NEGATIVE_OUTCOME_FONT_COLOR = RED;

    private static final String PAGE_LINK_COLOR = BLUE;

    private static final String NEGATIVE_OUTCOME_LINE_COLOR = RED;

    private static final String POSITIVE_OUTCOME_LINE_COLOR = GREEN;

    private static final String GLOBAL_LINK_COLOR = "grey";

    private static final double HEIGHT_UNIT = 0.50;

    private static final double WIDTH_UNIT = 1.33 * HEIGHT_UNIT;

    private static final double FONT_SIZE_FOR_ESTIMATE = 6.0;

    private static Map<String, GraphNode> pageIdNodeMap;
    private static Map<String, GraphGroup> pageIdGroupMap;
    private static Map<String, GraphNode> externalLinkNodeMap;

    public static void init() {
        pageIdNodeMap = new HashMap<String, GraphNode>();
        pageIdGroupMap = new HashMap<String, GraphGroup>();
        externalLinkNodeMap = new HashMap<String, GraphNode>();
    }

    public static void generate(Techspec techspec, Appspec appspec) throws IOException {
        if (!GraphUtils.isGraphingAvailable()) {
            log.warn("Graphing is not installed");
            return;
        }

        Graph projectGraph = buildProjectGraph(appspec);

        File graphOutputDirectory = techspec.getOutputDirectory();

        GraphUtils.toHtmlAndPng(GraphingEngine.DOT, projectGraph, new File(graphOutputDirectory, "project.html"),
                new File(graphOutputDirectory, "project.png"));

        boolean ignore = true;
        if (ignore) {
            return;
        }

        for (Page page : appspec.getPages()) {
            Graph pageGraph = buildProjectGraph(appspec);

            StringUtil.filterNodesByName(pageIdNodeMap, page.getId(), "", pageGraph);

            GraphUtils.toHtmlAndPng(GraphingEngine.DOT, pageGraph, new File(graphOutputDirectory, page.getId()
                    + "_graph.html"), new File(graphOutputDirectory, page.getId() + ".png"));
        }
    }

    private static GraphNode buildPageNode(Graph graph, Page page) {
        GraphNode n = graph.addNode();
        // n.getInfo().setCaption(page.getId());
        // n.getInfo().setAttributes("shape=point");
        // n.getInfo().setShapeHexagon();
        // n.getInfo().setShapeOctagon();
        // n.getInfo().setShapeBox();
        // n.getInfo().setAttributes("shape=hexagon");
        n.getInfo().setShapeEllipse();
        n.getInfo().setAttributes("URL=\"" + page.getId() + "\\.html\", label=\"HTML\"");
        // n.getInfo().setCaption("\"HTML\"");
        // n.getInfo().setAttributes("label=\"HTML\"");
        pageIdNodeMap.put(page.getId(), n);

        return n;
    }

    private static Graph buildProjectGraph(Appspec appspec) throws PageNotFoundException {
        init();
        Graph graph = GraphFactory.newGraph();
        graph.getInfo().setCaption(appspec.getTitle());

        // pages
        for (Page page : appspec.getPages()) {
            GraphNode n = buildPageNode(graph, page);
            GraphGroup gg = findOrCreateGroupForPage(graph, page);
            // gg.getInfo().setAttributes("URL=" + page.getId() + "\\.html");
            n.setMemberOf(gg);
        }

        // linksets
        buildProjectLinkSets(appspec, graph);

        // edges
        for (Page page : appspec.getPages()) {
            for (Form form : page.getForms()) {
                // actions/outcomes
                for (Action action : form.getActions()) {
                    GraphNode actionNode = createNode(graph, page, action);
                    GraphEdge actionEdge = graph.addEdge(node(page), actionNode);
                    // actionEdge.getInfo().setCaption(action.getTitle());

                    // links
                    for (Link link : action.getOutcomes()) {
                        GraphNode dest = findOrCreateDestinationNode(graph, link);

                        GraphEdge outcomeEdge = graph.addEdge(actionNode, dest);
                        outcomeEdge.getInfo().setCaption(wrapped(link.getTitle()));
                        outcomeEdge.getInfo().setFontSize(9.0);
                        if (link.isNegative()) {
                            outcomeEdge.getInfo().setLineColor(NEGATIVE_OUTCOME_LINE_COLOR);
                            outcomeEdge.getInfo().setFontColor(NEGATIVE_OUTCOME_FONT_COLOR);
                        } else {
                            outcomeEdge.getInfo().setLineColor(POSITIVE_OUTCOME_LINE_COLOR);
                            outcomeEdge.getInfo().setFontColor(POSITIVE_OUTCOME_FONT_COLOR);
                            // outcomeEdge.getInfo()
                            // .setAttributes("decorate=\"true\"");
                        }
                    }
                }
            }

            // links
            for (Link link : page.getLinks()) {
                GraphNode dest;
                dest = findOrCreateDestinationNode(graph, link);
                GraphEdge edge = graph.addEdge(node(page), dest);
                edge.getInfo().setCaption(wrapped(link.getTitle()));
                edge.getInfo().setLineColor(PAGE_LINK_COLOR);
                edge.getInfo().setFontColor(PAGE_LINK_COLOR);
                edge.getInfo().setFontSize(9.0);
            }
        }

        return graph;
    }

    private static void buildProjectLinkSets2(Appspec appspec, Graph graph) throws PageNotFoundException {
        for (LinkSet linkSet : appspec.getLinkSets()) {
            GraphGroup linkSetGroup = graph.addGroup();
            GraphNode linkSetNode = graph.addNode();
            linkSetNode.getInfo().setCaption(linkSet.getTitle());
            linkSetNode.getInfo().setShapeRecord();
            linkSetNode.setMemberOf(linkSetGroup);

            for (Link link : linkSet.getLinks()) {
                GraphNode dest = findOrCreateDestinationNode(graph, link);
                GraphNode linkReference = graph.addNode();
                linkReference.getInfo().setCaption(link.getTargetTitle());
                linkReference.setMemberOf(linkSetGroup);
                GraphEdge globalLink = graph.addEdge(linkReference, dest);
                globalLink.getInfo().setLineColor(GLOBAL_LINK_COLOR);
            }
        }
    }

    private static void buildProjectLinkSets(Appspec appspec, Graph graph) throws PageNotFoundException {
        buildProjectLinkSets1(appspec, graph);
    }

    private static void buildProjectLinkSets1(Appspec appspec, Graph graph) throws PageNotFoundException {
        for (LinkSet linkSet : appspec.getLinkSets()) {
            GraphGroup linkSetGroup = graph.addGroup();
            linkSetGroup.getInfo().setCaption(linkSet.getTitle());

            for (Link link : linkSet.getLinks()) {
                GraphNode dest = findOrCreateDestinationNode(graph, link);
                GraphNode linkReference = graph.addNode();
                linkReference.getInfo().setCaption(link.getTargetTitle());
                linkReference.setMemberOf(linkSetGroup);
                GraphEdge globalLink = graph.addEdge(linkReference, dest);
                globalLink.getInfo().setLineColor(GLOBAL_LINK_COLOR);
            }
        }
    }

    private static GraphNode findOrCreateDestinationNode(Graph graph, Link link) throws PageNotFoundException {
        GraphNode dest;
        if (link instanceof InternalLink) {
            InternalLink internalLink = (InternalLink) link;
            dest = node(internalLink.getPage());
        } else {
            ExternalLink externalLink = (ExternalLink) link;
            dest = findOrCreateNode(graph, externalLink);
        }
        return dest;
    }

    public static String wrapped(String title) {
        return title.replaceAll("(\\w+\\s+\\w+\\s+\\w+\\s+)", "$1\\\\n");
    }

    private static GraphNode createNode(Graph graph, Page page, Action action) {
        GraphNode n = graph.addNode();
        n.getInfo().setCaption(action.getTitle());
        n.setMemberOf(findOrCreateGroupForPage(graph, page));
        n.getInfo().setRounded(true);
        // n.getInfo().setShapeDiamond();

        return n;
    }

    private static GraphGroup findOrCreateGroupForPage(Graph graph, Page page) {
        if (!pageIdGroupMap.containsKey(page.getId())) {
            GraphGroup pageGroup = graph.addGroup();
            pageGroup.getInfo().setCaption(page.getTitle());
            pageGroup.getInfo().setFillColor(GRAY);
            pageGroup.getInfo().setModeFilled();

            pageIdGroupMap.put(page.getId(), pageGroup);
        }
        return pageIdGroupMap.get(page.getId());
    }

    private static GraphNode findOrCreateNode(Graph graph, ExternalLink externalLink) {
        if (!externalLinkNodeMap.containsKey(externalLink.getHref())) {
            GraphNode n = graph.addNode();
            n.getInfo().setCaption(externalLink.getHref());
            externalLinkNodeMap.put(externalLink.getHref(), n);
        }
        return externalLinkNodeMap.get(externalLink.getHref());
    }

    private static GraphNode node(Page page) {
        return pageIdNodeMap.get(page.getId());
    }

    public static Graph buildProjectEstimateGraph(Appspec appspec, ProjectEstimate projectEstimate) {
        GraphGenerator.init();
        Graph graph = GraphFactory.newGraph();
        graph.getInfo().setCaption(appspec.getTitle() + "(" + projectEstimate.getPoints() + " EUs)");
        // graph.getInfo().setAttributes("rankdir=LR");
        // GraphNode total = graph.addNode();
        // total.getInfo().setCaption(projectEstimate.getPoints() + "");

        // pages
        for (String pageId : projectEstimate.getPageEstimates().keySet()) {
            Page page;
            try {
                page = appspec.getPage(pageId);
            } catch (PageNotFoundException e) {
                throw new RuntimeException("Developer error", e);
            }
            PageEstimate pageEstimate = projectEstimate.getPageEstimates().get(pageId);

            GraphNode n = buildPageEstimateNode(graph, pageEstimate);
            boolean drawEdges = false;
            if (drawEdges) {
                // GraphEdge edge = graph.addEdge(total, n);
                // edge.getInfo().setLineColor(GRAY);
                // edge.getInfo().setArrowHeadNormal();
                // edge.getInfo().setAttributes("");
            }
        }
        return graph;
    }

    private static GraphNode buildPageEstimateNode(Graph graph, PageEstimate pageEstimate) {
        GraphNode n = graph.addNode();
        Page page = pageEstimate.getPage();
        findOrCreateGroupForPage(graph, page);
        n.getInfo().setCaption(pageEstimate.getPoints() + "");
        double fontSize = FONT_SIZE_FOR_ESTIMATE * Math.log(pageEstimate.getPoints());
        n.getInfo().setFontSize(fontSize);
        // n.getInfo().setHeader();
        n.getInfo().setShapeRecord();
        double height = Math.log(pageEstimate.getPoints()) * HEIGHT_UNIT;
        double width = Math.log(pageEstimate.getPoints()) * WIDTH_UNIT;
        n.getInfo().setAttributes("height=\"" + height + "\", width=\"" + width + "\"");
        // n.getInfo().setHeader("header");
        // n.getInfo().setFooter("footer");

        pageIdNodeMap.put(page.getId(), n);

        GraphGroup gg = findOrCreateGroupForPage(graph, page);
        n.setMemberOf(gg);

        return n;
    }
}