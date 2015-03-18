/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.circle_search;

import java.util.LinkedList;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder.NaivePatternFinderAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder.PatternAttributeUtils;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 */
public class CircleSearchAndLayoutAlgorithm extends AbstractAlgorithm {
	
	private double patternNodeDistance = 50;
	private int startNodeCount = 10;
	private int endNodeCount = 3;
	private boolean doCircleLayout = true;
	private boolean startWithLargestCircle = true;
	
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"This algorithm searches for circular subgraphs<br>" +
							"and applies a circle layout to these subgraphs.<br>" +
							"Plase specify the aproximate node distance and<br>" +
							"the minimum and maximum size of circles (node count).<br>" +
							"<small>Remark: There may be additional, overlapping circles -<br>" +
							"only one of the possibilities is processed.</small>";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new IntegerParameter(10, 3, graph.getNumberOfNodes(), "Maximum Node Count", ""),
							new IntegerParameter(3, 3, graph.getNumberOfNodes(), "Minimum Node Count", ""),
							new BooleanParameter(startWithLargestCircle, "Start search with large circles",
												"If enabled, the circle search starts with the maximum node count."),
							new BooleanParameter(doCircleLayout, "Apply Circle Layout/Select Nodes",
												"<html>If selected, the a circle layout is applied,<br>" +
																	"if not selected, the circles are selected."),
							new DoubleParameter(patternNodeDistance, "Approximate Node Distance", "") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		this.startNodeCount = ((IntegerParameter) params[i++]).getInteger();
		this.endNodeCount = ((IntegerParameter) params[i++]).getInteger();
		this.startWithLargestCircle = ((BooleanParameter) params[i++]).getBoolean();
		this.doCircleLayout = ((BooleanParameter) params[i++]).getBoolean();
		this.patternNodeDistance = ((DoubleParameter) params[i++]).getDouble();
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null || graph.getNumberOfNodes() < 3)
			throw new PreconditionException("No active graph or graph too small (less than 3 nodes)!");
		if (startNodeCount < endNodeCount)
			throw new PreconditionException("Start node count can not be smaller than end node count!");
	}
	
	public void execute() {
		for (GraphElement ge : graph.getGraphElements()) {
			AttributeHelper.deleteAttribute(ge, PatternAttributeUtils.PATTERN_PATH, PatternAttributeUtils.PATTERN_RECORD_PREFIX + "*");
		}
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
		final Graph graph = this.graph;
		final boolean startLarge = startWithLargestCircle;
		BackgroundTaskHelper.issueSimpleTask("Find and layout circles", "Please wait...",
							new Runnable() {
								public void run() {
									List<Graph> circleGraphs = new LinkedList<Graph>();
									for (int n = startNodeCount; n >= endNodeCount; n--) {
										if (status.wantsToStop())
											break;
										status.setCurrentStatusText2("Create circle of size " + n);
										Graph circleGraph = new AdjListGraph();
										Node previousNode = null;
										Node firstNode = null;
										for (int i = 0; i < n; i++) {
											Node node = circleGraph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(20, 20));
											if (firstNode == null)
												firstNode = node;
											if (previousNode != null) {
												circleGraph.addEdge(previousNode, node, true);
											}
											previousNode = node;
										}
										if (firstNode != null && previousNode != null)
											circleGraph.addEdge(firstNode, previousNode, true);
										circleGraph.setName("circle_" + circleGraph.getNumberOfNodes());
										circleGraphs.add(circleGraph);
									}
									if (!status.wantsToStop()) {
										status.setCurrentStatusText2("Search circles in graph...");
										if (circleGraphs.size() > 0) {
											CircleLayouterAlgorithm layout = null;
											if (doCircleLayout) {
												layout = new CircleLayouterAlgorithm();
												layout.setPatternNodeDistance(patternNodeDistance);
											}
											try {
												graph.getListenerManager().transactionStarted(this);
												NaivePatternFinderAlgorithm.searchPatterns(
																	graph,
																	circleGraphs,
																	layout,
																	true,
																	startLarge,
																	status);
											} finally {
												graph.getListenerManager().transactionFinished(this, true);
												GraphHelper.issueCompleteRedrawForGraph(graph);
											}
										}
										status.setCurrentStatusText2("Processing finished");
									}
								}
							},
							null, status);
	}
	
	public String getName() {
		return "Find and Layout Circles...";
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}
