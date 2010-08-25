package org.clickframes.plugins.graph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.clickframes.graph.GraphUtils;
import org.clickframes.graph.GraphingEngine;
import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.ExternalLink;
import org.clickframes.model.Form;
import org.clickframes.model.InternalLink;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Page;
import org.clickframes.model.PageNotFoundException;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.techspec.Context;
import org.clickframes.techspec.Techspec;
import org.clickframes.techspec.TechspecContext;
import org.clickframes.util.ImageMapArea;
import com.oy.shared.lm.graph.Graph;
import com.oy.shared.lm.graph.GraphEdge;
import com.oy.shared.lm.graph.GraphFactory;
import com.oy.shared.lm.graph.GraphGroup;
import com.oy.shared.lm.graph.GraphNode;
import com.oy.shared.lm.out.DOTtoGIF;
import com.oy.shared.lm.out.GRAPHtoDOT;

public class Plugin extends ClickframesPlugin {
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

	private Map<String, GraphNode> pageIdNodeMap;
	private Map<String, GraphGroup> pageIdGroupMap;
	private Map<String, GraphNode> externalLinkNodeMap;

	@Override
	public void globalContext(Context appspecContext,
			TechspecContext techspecContext) {
		Appspec appspec = techspecContext.getAppspec();
		Techspec techspec = techspecContext.getTechspec();

		Graph projectGraph = buildProjectGraph(appspec);

		File graphOutputDirectory = new File(techspec.getOutputDirectory(),
				(String) appspecContext.get("outputDir"));

		try {
			Map<String, Object> params = toHtmlAndPng(GraphingEngine.DOT,
					projectGraph, new File(graphOutputDirectory, "project.png"));
			appspecContext.putAll(params);
		} catch (IOException e) {
			throw new RuntimeException("Error while creating graph", e);
		}
	}

	private Map<String, Object> toHtmlAndPng(GraphingEngine graphingEngine,
			Graph graph, File pngFile) throws IOException {
		File dotFile = File.createTempFile("clickframes", ".dot");
		dotFile.deleteOnExit();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GRAPHtoDOT.transform(graph, baos);
		String dot = baos.toString();
		dot = dot
				.replaceAll(
						"label=.http.:././www..softwaresecretweapons..com.l.; URL=.http.:././www..softwaresecretweapons..com.l.;",
						"");
		dot = dot.replaceAll("softwaresecretweapons..com", "clickframes\\.org");
		FileUtils.writeStringToFile(dotFile, dot);

		// make sure that pngFile's parent exists
		if (!pngFile.getParentFile().exists()
				&& !pngFile.getParentFile().mkdirs()) {
			throw new RuntimeException("Could not create directory "
					+ pngFile.getAbsolutePath());
		}

		if (GraphUtils.isGraphingAvailable(graphingEngine)) {
			DOTtoGIF.transform(graphingEngine.getCommand(), dotFile
					.getAbsolutePath(), pngFile.getAbsolutePath());
			return createHtmlFile(graphingEngine, dotFile, pngFile.getName());
		}

		return new HashMap<String, Object>();
	}

	private Map<String, Object> createHtmlFile(GraphingEngine graphingEngine,
			File dotFile, String imageFilename) throws IOException {
		// run this command 'dot test.dot -Timap'
		// in the output look for lines like: shape url coords
		Process p;
		String error = null;
		try {
			String[] cmd = new String[] { graphingEngine.getCommand(),
					dotFile.getAbsolutePath(), "-Timap" };

			boolean reportErrors = false;
			p = Runtime.getRuntime().exec(cmd);

			List<String> lines = null;
			InputStream in = p.getInputStream();

			boolean finished = false; // Set to true when p is finished
			while (!finished) {
				try {
					while (in.available() > 0) {
						// Print the output of our system call.
						// Character c = new Character((char) in.read());
						// System.out.print(c);
						lines = IOUtils.readLines(in);
					}
					// Ask the process for its exitValue. If the process
					// is not finished, an IllegalThreadStateException
					// is thrown. If it is finished, we fall through and
					// the variable finished is set to true.
					p.exitValue();
					finished = true;
				} catch (IllegalThreadStateException e) {
					Thread.currentThread();
					// Sleep a little to save on CPU cycles
					Thread.sleep(500);
				}
			}

			Pattern pattern = Pattern.compile("^(\\S+) (\\S+) (.+)$");
			List<ImageMapArea> areas = new ArrayList<ImageMapArea>();
			for (String line : lines) {
				Matcher m = pattern.matcher(line);
				if (m.matches()) {
					String shape = m.group(1);
					String href = m.group(2);
					String coords = m.group(3).trim();
					ImageMapArea area = new ImageMapArea();
					area.setShape(shape);
					area.setHref(href.replaceAll("\\\\l", "").replaceAll(
							"\\\\", ""));
					area.setCoords(coords);
					areas.add(area);
				}
			}

			Map<String, Object> params = new HashMap<String, Object>();

			params.put("imageName", imageFilename);
			params.put("areaList", areas);

			return params;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void init() {
		pageIdNodeMap = new HashMap<String, GraphNode>();
		pageIdGroupMap = new HashMap<String, GraphGroup>();
		externalLinkNodeMap = new HashMap<String, GraphNode>();
	}

	private Graph buildProjectGraph(Appspec appspec)
			throws PageNotFoundException {
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
		// buildProjectLinkSets(appspec, graph);

		// edges
		for (Page page : appspec.getPages()) {
			for (Form form : page.getForms()) {
				// actions/outcomes
				for (Action action : form.getActions()) {
					GraphNode actionNode = createNode(graph, page, action);
					GraphEdge actionEdge = graph
							.addEdge(node(page), actionNode);
					// actionEdge.getInfo().setCaption(action.getTitle());

					// links
					for (Link link : action.getOutcomes()) {
						GraphNode dest = findOrCreateDestinationNode(graph,
								link);

						GraphEdge outcomeEdge = graph.addEdge(actionNode, dest);
						outcomeEdge.getInfo().setCaption(
								wrapped(link.getTitle()));
						outcomeEdge.getInfo().setFontSize(9.0);
						if (link.isNegative()) {
							outcomeEdge.getInfo().setLineColor(
									NEGATIVE_OUTCOME_LINE_COLOR);
							outcomeEdge.getInfo().setFontColor(
									NEGATIVE_OUTCOME_FONT_COLOR);
						} else {
							outcomeEdge.getInfo().setLineColor(
									POSITIVE_OUTCOME_LINE_COLOR);
							outcomeEdge.getInfo().setFontColor(
									POSITIVE_OUTCOME_FONT_COLOR);
							// outcomeEdge.getInfo()
							// .setAttributes("decorate=\"true\"");
						}
					}
				}
			}

			// links
			for (Link link : page.getLinks()) {
				if (!link.isInternal()) {
					// ignore external links
					continue;
				}
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

	public static String wrapped(String title) {
		return title.replaceAll("(\\w+\\s+\\w+\\s+\\w+\\s+)", "$1\\\\n");
	}

	private void buildProjectLinkSets(Appspec appspec, Graph graph)
			throws PageNotFoundException {
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

	private GraphNode buildPageNode(Graph graph, Page page) {
		GraphNode n = graph.addNode();
		// n.getInfo().setCaption(page.getId());
		// n.getInfo().setAttributes("shape=point");
		// n.getInfo().setShapeHexagon();
		// n.getInfo().setShapeOctagon();
		// n.getInfo().setShapeBox();
		// n.getInfo().setAttributes("shape=hexagon");
		n.getInfo().setShapeEllipse();
		n.getInfo().setAttributes(
				"URL=\"" + page.getId() + "\\.html\", label=\"HTML\"");
		// n.getInfo().setCaption("\"HTML\"");
		// n.getInfo().setAttributes("label=\"HTML\"");
		pageIdNodeMap.put(page.getId(), n);

		return n;
	}

	private GraphNode findOrCreateDestinationNode(Graph graph, Link link)
			throws PageNotFoundException {
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

	private GraphGroup findOrCreateGroupForPage(Graph graph, Page page) {
		if (!pageIdGroupMap.containsKey(page.getId())) {
			GraphGroup pageGroup = graph.addGroup();
			pageGroup.getInfo().setCaption(page.getTitle());
			pageGroup.getInfo().setFillColor(GRAY);
			pageGroup.getInfo().setModeFilled();

			pageIdGroupMap.put(page.getId(), pageGroup);
		}
		return pageIdGroupMap.get(page.getId());
	}

	private GraphNode findOrCreateNode(Graph graph, ExternalLink externalLink) {
		if (!externalLinkNodeMap.containsKey(externalLink.getHref())) {
			GraphNode n = graph.addNode();
			n.getInfo().setCaption(externalLink.getHref());
			externalLinkNodeMap.put(externalLink.getHref(), n);
		}
		return externalLinkNodeMap.get(externalLink.getHref());
	}

	private GraphNode node(Page page) {
		return pageIdNodeMap.get(page.getId());
	}

	private GraphNode createNode(Graph graph, Page page, Action action) {
		GraphNode n = graph.addNode();
		n.getInfo().setCaption(action.getTitle());
		n.setMemberOf(findOrCreateGroupForPage(graph, page));
		n.getInfo().setRounded(true);
		// n.getInfo().setShapeDiamond();

		return n;
	}
}