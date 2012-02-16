/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: AllPathsSelectionAlgorithm.java,v 1.1 2011-01-31 09:00:41 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.KeyStroke;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

public class AllPathsSelectionAlgorithm
					extends AbstractAlgorithm {
	
	Selection selection;
	
	private boolean settingIncludeInnerEdges = false;
	private boolean settingDirected = true;
	private boolean settingIncludeEdges = true;
	
	/**
	 * Constructs a new instance.
	 */
	public AllPathsSelectionAlgorithm() {
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new BooleanParameter(settingDirected, "Consider Edge Direction", ""),
							new BooleanParameter(settingIncludeEdges, "Select Edges", "If enabled, edges along the shortest path(s) are selected"),
							new BooleanParameter(settingIncludeInnerEdges, "Select Inner-Edges",
												"If selected, all edges connecting nodes of the shortest path(s) are selected") };
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		settingDirected = ((BooleanParameter) params[i++]).getBoolean();
		settingIncludeEdges = ((BooleanParameter) params[i++]).getBoolean();
		settingIncludeInnerEdges = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		Selection sel = new Selection("id");
		ArrayList<GraphElement> currentSelElements = new ArrayList<GraphElement>();
		selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
		graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		graph.numberGraphElements();
		if (selection != null)
			currentSelElements.addAll(selection.getElements());
		Collection<Node> graphNodes = graph.getNodes();
		HashSet<Node> targetNodesToBeProcessed = new HashSet<Node>();
		for (GraphElement ge : currentSelElements) {
			if (ge instanceof Node) {
				Node n = (Node) ge;
				targetNodesToBeProcessed.add(n);
			}
		}
		for (GraphElement ge : currentSelElements) {
			if (ge instanceof Node) {
				Node n = (Node) ge;
				Collection<GraphElement> shortestPathNodesAndEdges = getPathElements(
									graphNodes,
									n,
									targetNodesToBeProcessed,
									settingIncludeInnerEdges, settingDirected, settingIncludeEdges, Integer.MAX_VALUE);
				sel.addAll(shortestPathNodesAndEdges);
				if (!settingDirected)
					targetNodesToBeProcessed.remove(n);
				// System.out.println("=============");
			}
		}
		sel.addAll(currentSelElements);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
	}
	
	public static Collection<GraphElement> getPathElements(
						Collection<Node> validNodes,
						Node sourceNode, Collection<Node> targetNodes,
						boolean includeInnerEdges,
						boolean directed,
						boolean includeEdges, int maxDistance) {
		
		Queue<Node> findTheseNodes = new LinkedList<Node>();
		findTheseNodes.addAll(targetNodes);
		
		HashSet<GraphElement> shortestPath = new HashSet<GraphElement>();
		Queue<DistanceInfo> toDo = new LinkedList<DistanceInfo>();
		HashMap<Node, DistanceInfo> node2distanceinfo = new HashMap<Node, DistanceInfo>();
		DistanceInfo di = new DistanceInfo(0, sourceNode, sourceNode);
		toDo.add(di);
		node2distanceinfo.put(sourceNode, di);
		
		do {
			DistanceInfo checkNeighbours = toDo.remove();
			Node workNode = checkNeighbours.getNode();
			Collection<Node> neighbours;
			if (directed)
				neighbours = workNode.getOutNeighbors();
			else
				neighbours = workNode.getNeighbors();
			for (Node neighbour : neighbours) {
				if (!validNodes.contains(neighbour))
					continue;
				if (node2distanceinfo.containsKey(neighbour)) {
					DistanceInfo processedEntity = node2distanceinfo.get(neighbour);
					processedEntity.updateSourceIfValid(workNode, Double.NaN);
				} else {
					DistanceInfo newInfo = new DistanceInfo(checkNeighbours.getMinDistance() + 1, workNode, neighbour);
					if (newInfo.getMinDistance() <= maxDistance)
						toDo.add(newInfo);
					node2distanceinfo.put(neighbour, newInfo);
				}
				if (targetNodes.contains(neighbour)) {
					DistanceInfo thisEntity = node2distanceinfo.get(neighbour);
					if (thisEntity.allRelevantEdgesProcessed(directed))
						findTheseNodes.remove(neighbour);
				}
			}
		} while ((!toDo.isEmpty() && !findTheseNodes.isEmpty()));
		for (Node targetNode : targetNodes) {
			shortestPath.add(targetNode);
			if (node2distanceinfo.containsKey(targetNode)) {
				DistanceInfo distInfo = node2distanceinfo.get(targetNode);
				processReverseDistanceInfo(
									node2distanceinfo,
									shortestPath, distInfo,
									includeInnerEdges,
									directed,
									includeEdges);
			}
		}
		return shortestPath;
	}
	
	private static void processReverseDistanceInfo(
						HashMap<Node, DistanceInfo> node2distanceinfo,
						HashSet<GraphElement> path,
						DistanceInfo distInfo,
						boolean includeInnerEdges,
						boolean directed,
						boolean includeEdges) {
		path.add(distInfo.getNode());
		Collection<Node> sn = distInfo.getSourceNodes();
		for (Node sourceNode : sn) {
			if (path.contains(sourceNode))
				continue;
			path.add(sourceNode);
			boolean debug = false;
			if (debug) {
				NodeHelper nhN = new NodeHelper(distInfo.getNode());
				NodeHelper nhS = new NodeHelper(sourceNode);
				System.out.println("Process: " + nhN.getLabel() + " ==> " + nhS.getLabel());
			}
			if (sourceNode != distInfo.getNode())
				if (node2distanceinfo.containsKey(sourceNode)) {
					DistanceInfo pdi = node2distanceinfo.get(sourceNode); // process distance
					processReverseDistanceInfo(node2distanceinfo, path, pdi,
										includeInnerEdges,
										directed,
										includeEdges);
				}
		}
		if (includeEdges) {
			Collection<Edge> edges = distInfo.getNode().getEdges();
			for (Edge e : edges) {
				if (directed) {
					if (sn.contains(e.getSource()) && path.contains(e.getTarget()))
						path.add(e);
				} else {
					Node edgeSource = e.getSource();
					if (edgeSource == distInfo.getNode())
						edgeSource = e.getTarget();
					if (sn.contains(edgeSource))
						path.add(e);
				}
			}
		}
		if (includeInnerEdges) {
			for (Node sourceNode : sn) {
				Collection<Edge> edges = sourceNode.getEdges();
				for (Edge e : edges) {
					if (sn.contains(e.getSource()) && sn.contains(e.getTarget())) {
						path.add(e);
					}
				}
			}
		}
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		graph = null;
		selection = null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return null;
		// return "Find Paths";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
	}
	
	@Override
	public String getCategory() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "menu.edit";
		else
			return "Analysis";
	}
	
	/**
	 * Sets the selection on which the algorithm works.
	 * 
	 * @param selection
	 *           the selection
	 */
	public void setSelection(Selection selection) {
		this.selection = selection;
	}
}
