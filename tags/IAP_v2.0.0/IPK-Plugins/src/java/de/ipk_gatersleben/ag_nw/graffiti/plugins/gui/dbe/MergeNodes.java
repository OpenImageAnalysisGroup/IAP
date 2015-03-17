/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.RemoveMappingDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class MergeNodes extends AbstractAlgorithm {
	
	public String getName() {
		return "Merge Nodes...";
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public String getDescription() {
		return "<html>Hint: If you would like to merge frequently occuring nodes, which are recognized by<br>"
							+ "their identical labels, then this command is not suitable for this task.<br>"
							+ "Please use the node-processing commands from the 'Tools' tab, instead.<br>"
							+ "There you will find a node merge-command which processes the edge-connections<br>"
							+ "and occurrence of nodes with the same label.<br><br>"
							+ "With this command you may merge nodes into a single node (processing<br>"
							+ "connections to other nodes and node data-mappings)";
	}
	
	@Override
	public Parameter[] getParameters() {
		return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		
	}
	
	public void execute() {
		Collection<Node> workNodes = new ArrayList<Node>(getSelectedOrAllNodes());
		graph.getListenerManager().transactionStarted(this);
		mergeNodesIntoSingleNode(graph, workNodes);
		graph.getListenerManager().transactionFinished(this, true);
		GraphHelper.issueCompleteRedrawForGraph(graph);
		MainFrame.showMessage("Merged " + workNodes.size() + " nodes", MessageType.INFO);
	}
	
	public static void mergeNodesIntoSingleNode(Graph graph, Collection<Node> workNodes) {
		Vector2d center = NodeTools.getCenter(workNodes);
		List<SubstanceInterface> targetMappingList = new ArrayList<SubstanceInterface>();
		boolean firstNode = true;
		Node mergedNode = null;
		double targetArea = 0;
		double sumWidth = 0;
		double sumHeight = 0;
		String targetNodeName = "";
		HashMap<SubstanceInterface, String> mapping2chartTitle = new HashMap<SubstanceInterface, String>();
		for (Node workNode : workNodes) {
			if (targetNodeName.length() > 0) {
				targetNodeName += ", " + AttributeHelper.getLabel(workNode, "[unnamed]");
			} else {
				targetNodeName = AttributeHelper.getLabel(workNode, "[unnamed]");
			}
			
			if (firstNode) {
				firstNode = false;
				mergedNode = mergeNode(graph, workNodes, center, true);
			}
			
			Vector2d size = AttributeHelper.getSize(workNode);
			targetArea += size.x * size.x + size.y * size.y;
			sumWidth += size.x;
			sumHeight += size.y;
			
			extractDataMappingInformation(targetMappingList, mapping2chartTitle, workNode);
			
			if (mergedNode != workNode)
				graph.deleteNode(workNode);
		}
		Vector2d tsize = new Vector2d(0d, 0d);
		tsize.x = Math.sqrt(targetArea) * sumWidth / sumHeight;
		tsize.y = Math.sqrt(targetArea) * sumHeight / sumWidth;
		// AttributeHelper.setSize(mergedNode, tsize.x, tsize.y);
		AttributeHelper.setLabel(mergedNode, targetNodeName);
		
		applyDataMappingInformation(targetMappingList, mergedNode, mapping2chartTitle);
	}
	
	private static void applyDataMappingInformation(List<SubstanceInterface> targetMappingList, Node node,
						HashMap<SubstanceInterface, String> mapping2chartTitle) {
		RemoveMappingDataAlgorithm.removeMappingDataFrom(node);
		int idx = 0;
		HashSet<String> titles = new HashSet<String>();
		for (SubstanceInterface mappData : targetMappingList) {
			String s = mapping2chartTitle.get(mappData);
			if (s != null && s.length() > 0)
				titles.add(s);
		}
		for (SubstanceInterface mappData : targetMappingList) {
			Experiment2GraphHelper.addMappingData2Node(mappData, node);
			if (titles.size() > 1)
				AttributeHelper.setAttribute(node, "charting", "chartTitle" + (++idx), mapping2chartTitle.get(mappData));
		}
	}
	
	private static void extractDataMappingInformation(List<SubstanceInterface> targetMappingList,
						HashMap<SubstanceInterface, String> mapping2chartTitle, Node node) {
		Iterable<SubstanceInterface> mappingList = Experiment2GraphHelper.getMappedDataListFromGraphElement(node);
		if (mappingList != null) {
			int idx = 0;
			for (SubstanceInterface mapping : mappingList) {
				String chartTitle = (String) AttributeHelper.getAttributeValue(node, "charting", "chartTitle" + (++idx),
									"", "");
				if (chartTitle == null || chartTitle.length() <= 0) {
					chartTitle = AttributeHelper.getLabel(node, null);
				} else {
					chartTitle = AttributeHelper.getLabel(node, null) + ": " + chartTitle;
				}
				mapping2chartTitle.put(mapping, chartTitle);
			}
			for (SubstanceInterface m : mappingList)
				targetMappingList.add(m);
		}
	}
	
	public static void convertIDs(Graph graph) {
		for (Node checkNode : graph.getNodes()) {
			String clusterID = NodeTools.getClusterID(checkNode, "");
			String mapNumber = clusterID;
			if (mapNumber != null && mapNumber.startsWith("path:map") && mapNumber.indexOf(";") < 0) {
				mapNumber = mapNumber.substring("path:map".length());
				String[] cat = KoService.getPathwayGroupFromMapNumber(mapNumber);
				if (cat != null && cat.length > 0) {
					mapNumber = AttributeHelper.getStringList(cat, ".");
					NodeTools.setClusterID(checkNode, mapNumber);
				}
			}
		}
	}
	
	public static Node mergeNode(Graph graph, Collection<Node> toBeMerged, Vector2d center, boolean retainClusterIDs) {
		Node mergedNode;
		mergedNode = graph.addNodeCopy(toBeMerged.iterator().next());
		if (mergedNode == null || mergedNode.getGraph() == null) {
			ErrorMsg.addErrorMessage("Merge Operation Error: Could not add node-copy.");
			return null;
		}
		List<SubstanceInterface> targetMappingList = new ArrayList<SubstanceInterface>();
		HashMap<SubstanceInterface, String> mapping2chartTitle = new HashMap<SubstanceInterface, String>();
		HashSet<String> clusterIDs = new HashSet<String>();
		AttributeHelper.setPosition(mergedNode, center);
		for (Node checkNode : toBeMerged) {
			if (retainClusterIDs) {
				String clusterID = NodeTools.getClusterID(checkNode, "");
				String mapNumber = clusterID;
				if (mapNumber != null && mapNumber.startsWith("path:map")) {
					mapNumber = mapNumber.substring("path:map".length());
					String[] cat = KoService.getPathwayGroupFromMapNumber(mapNumber);
					if (cat != null && cat.length > 0) {
						mapNumber = AttributeHelper.getStringList(cat, ".");
					} else
						clusterIDs.add(clusterID);
				} else
					clusterIDs.add(clusterID);
			}
			extractDataMappingInformation(targetMappingList, mapping2chartTitle, checkNode);
			for (Edge undirEdge : checkNode.getUndirectedEdges()) {
				if (undirEdge.getSource() == checkNode
									&& !mergedNode.getUndirectedNeighbors().contains(undirEdge.getTarget())) {
					if (undirEdge.getTarget() != checkNode)
						graph.addEdgeCopy(undirEdge, mergedNode, undirEdge.getTarget());
					else
						graph.addEdgeCopy(undirEdge, mergedNode, mergedNode);
				}
				if (undirEdge.getTarget() == checkNode
									&& !mergedNode.getUndirectedNeighbors().contains(undirEdge.getSource())) {
					if (undirEdge.getSource() != checkNode)
						graph.addEdgeCopy(undirEdge, undirEdge.getSource(), mergedNode);
					else
						graph.addEdgeCopy(undirEdge, mergedNode, mergedNode);
				}
			}
			for (Edge inEdge : checkNode.getAllInEdges()) {
				if (!mergedNode.getInNeighbors().contains(inEdge.getSource())) {
					graph.addEdgeCopy(inEdge, inEdge.getSource(), mergedNode);
				}
			}
			for (Edge outEdge : checkNode.getAllOutEdges()) {
				if (!mergedNode.getOutNeighbors().contains(outEdge.getTarget())) {
					graph.addEdgeCopy(outEdge, mergedNode, outEdge.getTarget());
				}
			}
			
			for (Node undirNode : checkNode.getUndirectedNeighbors())
				if (!mergedNode.getUndirectedNeighbors().contains(undirNode)) {
					graph.addEdge(mergedNode, undirNode, false);
				}
		}
		if (retainClusterIDs && clusterIDs.size() > 1)
			NodeTools.setClusterID(mergedNode, AttributeHelper.getStringList(clusterIDs, ";"));
		if (targetMappingList.size() > 0)
			applyDataMappingInformation(targetMappingList, mergedNode, mapping2chartTitle);
		return mergedNode;
	}
}
