/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 27.2.2006
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.ratioview;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeSortCommand;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.PajekClusterColor;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 */
public class RatioViewAlgorithm extends AbstractAlgorithm {
	private String selSeriesName1;
	
	private boolean sortConsiderClusters = true;
	private boolean showRangeAxisForNode1 = false;
	private boolean calcLog = true;
	
	private static int createdRatioGraph = 0;
	
	private NodeSortCommand sortCommand = NodeSortCommand.dontSort;
	
	@Override
	public String getName() {
		return "Create Condition Ratio View...";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public String getDescription() {
		return "<html>Creates a ratio view for a specified time point of the mapped data.<br>"
				+ "This algorithm requires mapped data from at least two different lines.<br><br>";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null)
			throw new PreconditionException("No graph available");
		if (graph.getNodes().size() <= 0)
			throw new PreconditionException("Graph contains no nodes");
		boolean foundData = false;
		for (Node n : graph.getNodes()) {
			if (Experiment2GraphHelper.getMappedDataListFromGraphElement(n) != null) {
				foundData = true;
				break;
			}
		}
		if (!foundData)
			throw new PreconditionException("Graph nodes have no data assigned");
	}
	
	@Override
	public Parameter[] getParameters() {
		List<NodeHelper> helperNodes = GraphHelper.getHelperNodes(graph);
		SortedSet<Integer> timePoints = new TreeSet<Integer>();
		SortedSet<String> seriesNames = new TreeSet<String>();
		
		for (NodeHelper nh : helperNodes) {
			timePoints.addAll(nh.getMappedUniqueTimePoints());
			seriesNames.addAll(nh.getMappedSeriesNames());
		}
		// List<String> sortCriteria = new ArrayList<String>();
		// sortCriteria.add(sortDont);
		// sortCriteria.add(sortLabel);
		// sortCriteria.add(sortLabelInverse);
		// sortCriteria.add(sortRatioMin);
		// sortCriteria.add(sortRatioMax);
		Iterator<String> itSer = seriesNames.iterator();
		String initSelSeries1 = selSeriesName1 != null || seriesNames.size() == 0 ? selSeriesName1 : itSer.next();
		if (!seriesNames.contains(initSelSeries1) && seriesNames.size() > 0)
			initSelSeries1 = seriesNames.first();
		ObjectListParameter olpSN1 = new ObjectListParameter(initSelSeries1, "Reference Series",
				"Select the desired reference dataset.", seriesNames);
		ObjectListParameter olpSort = new ObjectListParameter(sortCommand, "Sort Data",
				"Select the desired sort criteria", NodeSortCommand.values());
		BooleanParameter bpConsiderCluster = new BooleanParameter(sortConsiderClusters, "Consider Clusters",
				"If selected, each cluster will be individually sorted");
		BooleanParameter bpRangeAxisForNode1 = new BooleanParameter(showRangeAxisForNode1, "Show Range Axis ",
				"If selected, the first node will have a visible range axis");
		BooleanParameter bpCalcLog = new BooleanParameter(calcLog, "Log-Scale",
				"If selected, the logarithm of the ratio will be calculated and shown");
		
		return new Parameter[] { olpSN1, olpSort, bpConsiderCluster, bpRangeAxisForNode1, bpCalcLog };
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		ObjectListParameter olp2 = (ObjectListParameter) params[i++];
		selSeriesName1 = (String) olp2.getValue();
		ObjectListParameter olp4 = (ObjectListParameter) params[i++];
		sortCommand = (NodeSortCommand) olp4.getValue();
		BooleanParameter bpSortConsiderClusters = (BooleanParameter) params[i++];
		sortConsiderClusters = bpSortConsiderClusters.getBoolean().booleanValue();
		BooleanParameter bpShowRangeForNode1 = (BooleanParameter) params[i++];
		showRangeAxisForNode1 = bpShowRangeForNode1.getBoolean().booleanValue();
		BooleanParameter bpCalcLog = (BooleanParameter) params[i++];
		calcLog = bpCalcLog.getBoolean().booleanValue();
	}
	
	@Override
	public void execute() {
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Create Ratio View", "Please wait...");
		
		MainFrame.showMessage("Create ratio view for reference series " + selSeriesName1 + "...", MessageType.INFO);
		final List<NodeHelper> workNodes;
		if (MainFrame.getInstance().getActiveEditorSession().getGraph() == graph)
			workNodes = GraphHelper.getSelectedOrAllHelperNodes(MainFrame.getInstance().getActiveEditorSession());
		else
			workNodes = GraphHelper.getHelperNodes(graph);
		BackgroundTaskHelper.issueSimpleTask("Ratio View", "Create Ratio View", new Runnable() {
			@Override
			public void run() {
				createRatioView(status, workNodes, graph, sortConsiderClusters, showRangeAxisForNode1, calcLog,
						sortCommand, selSeriesName1,
						// selSeriesName2, selTimePoint,
						false);
			}
		}, null, status);
	}
	
	public Graph executeAndReturnGraph(BackgroundTaskStatusProviderSupportingExternalCall status,
			boolean sortConsiderClusters, boolean showRangeAxisForNode1, boolean calcLog, NodeSortCommand sortCommand,
			String selSeriesName1, String selSeriesName2, int selTimePoint) {
		List<NodeHelper> workNodes = GraphHelper.getHelperNodes(graph);
		return createRatioView(status, workNodes, graph, sortConsiderClusters, showRangeAxisForNode1, calcLog,
				/* getSortCommand(sortCommand) */sortCommand, selSeriesName1,
				// selSeriesName2, selTimePoint,
				true);
	}
	
	private static Graph createRatioView(BackgroundTaskStatusProviderSupportingExternalCall status,
			List<NodeHelper> workNodes, Graph graph, boolean sortConsiderClusters, boolean showRangeAxisForNode1,
			boolean calcLog, NodeSortCommand sortCommand, String selSeriesName1,
			// String selSeriesName2,
			// int selTimePoint,
			boolean returnGraphDontShowResult) {
		if (status != null) {
			status.setCurrentStatusValueFine(0);
			status.setCurrentStatusText2("Analyze Cluster Information...");
		}
		final AdjListGraph ratioGraph = new AdjListGraph(new ListenerManager());
		Double refDataValue = null;
		double maxValue = Double.NEGATIVE_INFINITY;
		double minValue = Double.MAX_VALUE;
		Collection<String> clusters = GraphHelper.getClusters(workNodes);
		if (!clusters.contains(""))
			clusters.add("");
		SortedSet<String> sortedClusters = new TreeSet<String>();
		for (String validCluster : clusters)
			sortedClusters.add(validCluster);
		
		if (status != null) {
			status.setCurrentStatusValueFine(0);
			status.setCurrentStatusText2("Create Ratio Dataset...");
		}
		
		Graph workGraph = graph;
		
		SortedSet<Integer> timePoints = new TreeSet<Integer>();
		SortedSet<String> seriesNames = new TreeSet<String>();
		for (NodeHelper nh : workNodes) {
			timePoints.addAll(nh.getMappedUniqueTimePoints());
			seriesNames.addAll(nh.getMappedSeriesNames());
		}
		
		int row = 0;
		int maxNodesPerRow = 0;
		double workParts = (seriesNames.size() - 1) * timePoints.size();
		int workProgress = 0;
		HashSet<Integer> headerSet = new HashSet<Integer>();
		mainloop: for (String selSeriesName2 : seriesNames) {
			if (selSeriesName2.equals(selSeriesName1))
				continue;
			
			String seriesInfo = selSeriesName1 + "<br>vs. " + selSeriesName2 + "<br>at ";
			
			row++;
			int column = 0;
			for (int selTimePoint : timePoints) {
				int nidx = 0;
				column++;
				int nodesInCurrentSet = 0;
				
				String timeInfo = "";
				
				ArrayList<Node> createdNodesForThisBlock = new ArrayList<Node>();
				
				double workLoad = workNodes.size();
				
				workProgress++;
				
				for (String validCluster : sortedClusters) {
					for (NodeHelper nh : workNodes) {
						if (status != null && status.wantsToStop()) {
							break mainloop;
						}
						if (!nh.getClusterID("").equals(validCluster) || !nh.hasDataMapping())
							continue;
						if (status != null) {
							status.setCurrentStatusValueFine(100d * (workProgress - 1d) / workParts + 1d / workParts
									* (nidx / workLoad));
							status.setCurrentStatusText2("Process Node " + (nidx) + "/" + (int) workLoad + "...");
						}
						workGraph = nh.getGraph();
						Node ratioNode = ratioGraph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(
								Math.random() * 100d, Math.random() * 100d));
						NodeHelper ratioNodeHelper = new NodeHelper(ratioNode, false);
						
						createdNodesForThisBlock.add(ratioNodeHelper);
						
						ConditionInterface refDataSet = null;
						for (ConditionInterface sd : nh.getMappedSeriesData()) {
							if (sd.getConditionName().equals(selSeriesName1)) {
								refDataSet = sd;
								ArrayList<Integer> meanTimePoints = new ArrayList<Integer>(sd.getMeanTimePoints());
								if (meanTimePoints.contains(selTimePoint)) {
									int idx = meanTimePoints.indexOf(selTimePoint);
									ArrayList<Double> meanValues = new ArrayList<Double>(sd.getMeanValues());
									refDataValue = meanValues.get(idx);
									Collection<String> timeUnits = sd.getMeanTimeUnits();
									if (timeUnits != null && timeUnits.size() >= 1)
										timeInfo = timeUnits.iterator().next() + " " + selTimePoint;
								}
								break;
							}
						}
						int added = 0;
						if (refDataSet != null && refDataValue != null) {
							for (ConditionInterface sd : nh.getMappedSeriesData()) {
								if (!sd.getConditionName().equals(selSeriesName1)
										&& sd.getConditionName().equals(selSeriesName2)) {
									ArrayList<Integer> meanTimePoints = new ArrayList<Integer>(sd.getMeanTimePoints());
									if (meanTimePoints.contains(selTimePoint)) {
										ArrayList<String> meanTimeUnits = new ArrayList<String>(sd.getMeanTimeUnits());
										ArrayList<Double> meanValues = new ArrayList<Double>(sd.getMeanValues());
										int idx = meanTimePoints.indexOf(selTimePoint);
										int plantID = ratioNodeHelper.memGetPlantID(sd.getSpecies(), sd.getGenotype(), null,
												null, sd.getTreatment());
										double value;
										double thisVal = meanValues.get(idx);
										if (thisVal >= refDataValue)
											value = thisVal / refDataValue;
										else
											value = -refDataValue / thisVal;
										if (calcLog) {
											boolean neg = value < 0;
											value = Math.log(Math.abs(value));
											if (neg)
												value = -value;
										}
										if (!Double.isNaN(value) && !Double.isInfinite(value)) {
											if (value < minValue)
												minValue = value;
											if (value > maxValue)
												maxValue = value;
											ratioNodeHelper.memSample(value, 1, plantID, "ratio", meanTimeUnits.get(idx),
													selTimePoint);
											added++;
										}
									}
								}
							}
							if (added > 0)
								ratioNodeHelper.memAddDataMapping(nh.getLabel(), "ratio", AttributeHelper.getDateString(new Date()), "Ratio View",
										"auto generated", "", "");
						}
						// if (added<=0)
						// ratioGraph.deleteNode(ratioNode);
						// else {
						nodesInCurrentSet++;
						if (nodesInCurrentSet > maxNodesPerRow)
							maxNodesPerRow = nodesInCurrentSet;
						boolean setHeader = timeInfo.length() > 0 && !headerSet.contains(workProgress);
						processNodeDesign(ratioGraph, nh, ratioNodeHelper, nodesInCurrentSet, column, row, maxNodesPerRow,
								sortCommand, seriesInfo, timeInfo, setHeader);
						if (setHeader)
							headerSet.add(workProgress);
						// }
						nidx++;
					}
				}
				if (!sortCommand.equals(NodeSortCommand.dontSort)) {
					if (status != null)
						status.setCurrentStatusText2("Sort Nodes...");
					GraphHelper.exchangePositions(createdNodesForThisBlock, sortCommand, sortConsiderClusters);
					Collection<NodeHelper> sortedNodes = GraphHelper.getSortedNodeHelpers(createdNodesForThisBlock,
							sortCommand, sortConsiderClusters);
					createdNodesForThisBlock.clear();
					for (NodeHelper nh : sortedNodes) {
						ratioGraph.deleteNode(nh.getGraphNode());
						createdNodesForThisBlock.add(ratioGraph.addNodeCopy(nh.getGraphNode()));
					}
				}
				if (createdNodesForThisBlock.size() > 0 && showRangeAxisForNode1) {
					Node n1 = createdNodesForThisBlock.iterator().next();
					NodeHelper nh = new NodeHelper(n1);
					nh.setAttributeValue("charting", "showRangeAxis", new Boolean(true));
					double spY = 16d;
					nh.setPosition(nh.getPosition().getX(), nh.getPosition().getY() - spY / 2);
					nh.setSize(nh.getWidth(), nh.getHeight() + spY);
				}
			}
		}
		
		if (status != null && status.wantsToStop()) {
			status.setCurrentStatusText1("Processing incomplete");
			status.setCurrentStatusText2("Aborting further processing");
			return null;
		}
		
		AttributeHelper.setAttribute(ratioGraph, "", "node_showRangeAxis", new Boolean(false));
		AttributeHelper.setAttribute(ratioGraph, "", "node_outlineBorderWidth", new Double(1d));
		AttributeHelper.setAttribute(ratioGraph, "", "node_halfErrorBar", new Boolean(false));
		AttributeHelper.setAttribute(ratioGraph, "", "node_chartStdDevLineWidth", new Double(0d));
		AttributeHelper.setAttribute(ratioGraph, "", "node_plotOrientationHor", new Boolean(false));
		AttributeHelper.setAttribute(ratioGraph, "", "node_gridWidth", new Double(1d));
		AttributeHelper.setAttribute(ratioGraph, "", "node_axisWidth", new Double(1d));
		AttributeHelper.setAttribute(ratioGraph, "", "node_plotAxisFontSize", new Integer(30));
		AttributeHelper.setAttribute(ratioGraph, "", "node_showGridRange", new Boolean(false));
		AttributeHelper.setAttribute(ratioGraph, "", "node_showGridCategory", new Boolean(false)); // showRangeAxisForNode1));
		if (minValue < 0 && maxValue > 0 && -minValue < maxValue)
			minValue = -maxValue;
		if (minValue < 0 && maxValue > 0 && -minValue > maxValue)
			maxValue = -minValue;
		for (Node n : ratioGraph.getNodes()) {
			NodeHelper nh = new NodeHelper(n);
			nh.setChartRange(minValue, maxValue);
		}
		ratioGraph.setName("Ratio View " + (++createdRatioGraph));
		ClusterColorAttribute cca = (ClusterColorAttribute) AttributeHelper.getAttributeValue(workGraph,
				ClusterColorAttribute.attributeFolder, ClusterColorAttribute.attributeName, ClusterColorAttribute
						.getDefaultValue(clusters.size()), new ClusterColorAttribute("resulttype"), false);
		ClusterColorAttribute cca2 = (ClusterColorAttribute) cca.copy();
		ratioGraph.addAttribute(cca2, ClusterColorAttribute.attributeFolder);
		if (status != null)
			status.setCurrentStatusText2("Colorize Nodes...");
		PajekClusterColor.executeClusterColoringOnGraph(ratioGraph, cca2);
		
		if (status != null) {
			status.setCurrentStatusValue(100);
			status.setCurrentStatusText2("Show Graph (please wait)...");
		}
		if (!returnGraphDontShowResult) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (ratioGraph != null && ratioGraph.getNumberOfNodes() > 0)
						MainFrame.getInstance().showGraph(ratioGraph, null);
					else
						MainFrame.getInstance().showMessageDialog(
								"<html>" + "Ratio calculation found no valid data points.<br>"
										+ "Please select a time point for which at least<br>"
										+ "one substance has data points for both<br>" + "selected lines/conditions.");
				}
			});
			return null;
		} else {
			return ratioGraph;
		}
	}
	
	// processNodeDesign(ratioGraph, nh, ratioNodeHelper, nodesInCurrentSet,
	// column, row, maxNodesPerRow);
	
	private static void processNodeDesign(Graph ratioGraph, NodeHelper nh, NodeHelper ratioNodeHelper, int idx,
			int column, int row, int maxNodesPerSet, NodeSortCommand sort, String seriesInfo, String timeInfo,
			boolean setHeader) {
		double nodeHeight = 40d;
		double colwidth;
		int fontSize = 0;
		if (sort == NodeSortCommand.dontSort || sort == NodeSortCommand.sortLabel
				|| sort == NodeSortCommand.sortLabelInverse) {
			colwidth = 200d;
		} else {
			colwidth = 400d;
			fontSize = 20;
		}
		if (column == 1)
			fontSize = 20;
		
		if (setHeader) {
			Node header = ratioGraph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(350 + (column - 1)
					* colwidth, -100 + 150 + (nodeHeight - 2d) + maxNodesPerSet * (row - 1) * nodeHeight + (row - 1) * 50d));
			String lbl = "<html><center>" + seriesInfo + (timeInfo.length() > 0 ? "" + timeInfo : "");
			lbl = StringManipulationTools.stringReplace(lbl, "(", "<br>(");
			AttributeHelper.setLabel(header, lbl);
			AttributeHelper.getLabel(-1, header).setFontSize(12);
			AttributeHelper.setSize(header, colwidth, 100);
			AttributeHelper.setBorderWidth(header, 0);
		}
		ratioNodeHelper.setLabel(nh.getLabel());
		ratioNodeHelper.setClusterID(nh.getClusterID(null));
		ratioNodeHelper.setFillColor(new Color(240, 240, 240));
		ratioNodeHelper.setBorderWidth(0);
		ratioNodeHelper.setSize(200d, nodeHeight);
		ratioNodeHelper.setPosition(350 + (column - 1) * colwidth, 150 + (nodeHeight - 2d) * idx + maxNodesPerSet
				* (row - 1) * nodeHeight + (row - 1) * 120d);
		ratioNodeHelper.setAttributeValue("charting", "rangeAxis", "");
		ratioNodeHelper.setAttributeValue("labelgraphics", "anchor", "w");
		ratioNodeHelper.setAttributeValue("labelgraphics", "fontSize", new Integer(fontSize));
		ratioNodeHelper.setAttributeValue("graphics", "component", "chart2d_type3");
		ratioNodeHelper.setAttributeValue("charting", "empty_border_width", new Double(0d));
	}
}
