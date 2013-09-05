/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 15.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.apply_from_graph;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.swing.KeyStroke;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.actions.FileOpenAction;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.hamming_distance.HammingCalculator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.hamming_distance.WorkSettings;

public class ApplyGraphLayout implements Algorithm {
	
	Graph g;
	Selection s;
	
	public String getName() {
		return "Apply from Layouted File...";
	}
	
	public void setParameters(Parameter[] params) {
	}
	
	public Parameter[] getParameters() {
		return null;
	}
	
	public void attach(Graph g, Selection selection) {
		this.g = g;
		this.s = selection;
	}
	
	public void check() throws PreconditionException {
		if (g == null)
			throw new PreconditionException("No active graph");
		if (g.getNumberOfNodes() <= 0)
			throw new PreconditionException("Graph contains no nodes!");
	}
	
	public void execute() {
		
		MainFrame.showMessageDialog(
							"<html>" +
												"With this command you may apply the layout of another graph (file)<br>" +
												"to the active graph.<br>" +
												"A node matching is done on on the basis of node-labels. If a label in<br>" +
												"graph A and B is equal, the node A of the source graph is repositioned<br>" +
												"to the position of B.<br>" +
												"If there are several possible positions for a node A, because the selected<br>" +
												"graph file contains several nodes with the same label as A, a similarity<br>" +
												"measure is calculated. For that the node degree difference and missing<br>" +
												"neighbour nodes labels are considered.<br><br>" +
												"Additionally this command makes it possible, to not only to supply a<br>" +
												"specific graph (file) as the reference, but to supply a number of graphs.<br>" +
												"For that, select several files in the appearing File-Open dialog.<br>" +
												"The graph edit distances between the active graph and the selected graphs is<br>" +
												"calculated. The most similar graph is solely considered.<br><br>" +
												"Example Use-Case: Save a directed graph as a DOT file, and use the external<br>" +
												"dot layouter to layout the graph. Apply that layout to the source graph.<br>" +
												"(closing the source file and loading the DOT file would result in loss of<br>" +
												"certain graphical aspects, supported by this application, but which are<br>" +
												"not supported by the DOT format or by the DOT export/import - so this<br>" +
												"is not a good choice for many cases)",
							"Apply Layout");
		
		WorkSettings hammingCalculationSettings = new WorkSettings(true, 1, true, false, 1, 0);
		
		Collection<File> files = FileOpenAction.getGraphFilesFromUser();
		if (files != null) {
			Graph withLowestDistance = null;
			int lowestDistance = Integer.MAX_VALUE;
			HashSet<String> nodesIn1 = new HashSet<String>();
			for (Node n : g.getNodes()) {
				String lbl = AttributeHelper.getLabel(n, null);
				if (lbl != null && lbl.length() > 0)
					nodesIn1.add(lbl);
			}
			for (File file : files) {
				Graph g2;
				try {
					g2 = MainFrame.getInstance().getGraph(file);
					if (g2 == null || g2.getNumberOfNodes() <= 0)
						continue;
					HashSet<String> nodesIn2 = new HashSet<String>();
					for (Node n : g2.getNodes()) {
						String lbl = AttributeHelper.getLabel(n, null);
						if (lbl != null && lbl.length() > 0)
							nodesIn1.add(lbl);
					}
					int dist = HammingCalculator.compareTwoGraphs(g, g2, nodesIn1, nodesIn2, hammingCalculationSettings);
					if (dist < lowestDistance) {
						withLowestDistance = g2;
						lowestDistance = dist;
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			if (lowestDistance < Integer.MAX_VALUE && withLowestDistance != null) {
				// alles IO
			} else {
				ErrorMsg.addErrorMessage("No valid graph found");
				return;
			}
			Graph layoutedGraph = withLowestDistance;
			ArrayList<Node> nodesWithNoMatch = applyLayoutFromGraphToGraph(s, g, layoutedGraph, getName(), true);
			if (nodesWithNoMatch.size() > 0) {
				GraphHelper.clearSelection();
				GraphHelper.selectNodes(nodesWithNoMatch);
				MainFrame.showMessage("Best match found with selected graph " + withLowestDistance.getName() + " (Distance: " + lowestDistance + "), "
									+ nodesWithNoMatch.size() +
									" nodes could not be matched and are selected", MessageType.INFO);
			} else {
				MainFrame.showMessage("Best match found with selected graph " + withLowestDistance.getName() + " (Distance: " + lowestDistance + ")",
									MessageType.INFO);
			}
		}
	}
	
	/**
	 * @param layoutedGraph
	 * @return Nodes which are not relayouted, because they could not be found in the layouted graph
	 */
	public static ArrayList<Node> applyLayoutFromGraphToGraph(Selection s, Graph g, Graph layoutedGraph, String commandName, boolean considerEdgeLayout) {
		HashMap<String, ArrayList<Vector2d>> label2position = new HashMap<String, ArrayList<Vector2d>>();
		HashMap<String, ArrayList<Node>> label2otherNode = new HashMap<String, ArrayList<Node>>();
		for (org.graffiti.graph.Node n : layoutedGraph.getNodes()) {
			Vector2d pos = AttributeHelper.getPositionVec2d(n);
			String lbl = AttributeHelper.getLabel(n, null);
			if (lbl != null) {
				lbl = lbl.trim();
				if (!label2position.containsKey(lbl)) {
					label2position.put(lbl, new ArrayList<Vector2d>());
					label2otherNode.put(lbl, new ArrayList<Node>());
				}
				label2position.get(lbl).add(pos);
				label2otherNode.get(lbl).add(n);
			}
		}
		Collection<Node> workNodes = new ArrayList<Node>();
		if (s != null && s.getNodes().size() > 0)
			workNodes.addAll(s.getNodes());
		else
			workNodes.addAll(g.getNodes());
		ArrayList<Node> nodesWithNoMatch = new ArrayList<Node>();
		
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		HashMap<Node, Node> nodes2refNode = new HashMap<Node, Node>();
		
		for (Node n : workNodes) {
			String lbl = AttributeHelper.getLabel(n, null);
			if (lbl != null) {
				ArrayList<Vector2d> possiblePositions = label2position.get(lbl.trim());
				ArrayList<Node> possibleNodes = label2otherNode.get(lbl.trim());
				if (possiblePositions != null && possiblePositions.size() == 1) {
					nodes2newPositions.put(n, possiblePositions.get(0));
					nodes2refNode.put(n, possibleNodes.get(0));
				} else
					if (possiblePositions != null && possiblePositions.size() > 1) {
						int idxWithLowestDistance = -1;
						int lowestNodeDistance = Integer.MAX_VALUE;
						
						for (int i = 0; i < possiblePositions.size(); i++) {
							Node layoutedNode = label2otherNode.get(lbl).get(i);
							int dist = calcNodeDifferenceDistance(n, layoutedNode);
							if (dist < lowestNodeDistance) {
								lowestNodeDistance = dist;
								idxWithLowestDistance = i;
							}
						}
						
						if (idxWithLowestDistance >= 0) {
							nodes2newPositions.put(n, possiblePositions.get(idxWithLowestDistance));
							nodes2refNode.put(n, possibleNodes.get(idxWithLowestDistance));
							possiblePositions.remove(idxWithLowestDistance);
						}
					} else {
						nodesWithNoMatch.add(n);
					}
			}
		}
		if (considerEdgeLayout) {
			HashSet<Edge> processedEdges = new HashSet<Edge>();
			HashMap<Node, Node> refNode2node = new HashMap<Node, Node>();
			for (Entry<Node, Node> nn : nodes2refNode.entrySet()) {
				refNode2node.put(nn.getValue(), nn.getKey());
			}
			for (Entry<Node, Node> nn : refNode2node.entrySet()) {
				Node ln1 = nn.getKey();
				Node tn1 = nn.getValue();
				for (Edge e : ln1.getAllOutEdges()) {
					Node tn2 = refNode2node.get(e.getTarget());
					if (tn2 != null) {
						for (Edge te : tn1.getAllOutEdges()) {
							if (te.getTarget() != tn2)
								continue;
							if (processedEdges.contains(te))
								continue;
							copyBends(e, te);
							processedEdges.add(te);
						}
					}
				}
			}
		}
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, commandName);
		return nodesWithNoMatch;
	}
	
	private static void copyBends(Edge e, Edge e2) {
		ArrayList<Vector2d> eb = AttributeHelper.getEdgeBends(e);
		AttributeHelper.removeEdgeBends(e2);
		if (eb != null && eb.size() > 0) {
			AttributeHelper.addEdgeBends(e2, eb);
		}
		AttributeHelper.setEdgeBendStyle(e2, AttributeHelper.getEdgeBendStyle(e));
	}
	
	public static int calcNodeDifferenceDistance(Node n, Node layoutedNode) {
		HashSet<String> surroundingLabelsA = new HashSet<String>();
		for (Node nei : n.getNeighbors()) {
			String lbl = AttributeHelper.getLabel(nei, null);
			if (lbl != null && lbl.length() > 0)
				surroundingLabelsA.add(lbl);
		}
		HashSet<String> surroundingLabelsB = new HashSet<String>();
		for (Node nei : layoutedNode.getNeighbors()) {
			String lbl = AttributeHelper.getLabel(nei, null);
			if (lbl != null && lbl.length() > 0)
				surroundingLabelsB.add(lbl);
		}
		int differences = 0;
		for (Node nei : n.getNeighbors()) {
			String lbl = AttributeHelper.getLabel(nei, null);
			if (lbl != null && lbl.length() > 0) {
				if (!surroundingLabelsB.contains(lbl))
					differences++;
			}
		}
		for (Node nei : layoutedNode.getNeighbors()) {
			String lbl = AttributeHelper.getLabel(nei, null);
			if (lbl != null && lbl.length() > 0) {
				if (!surroundingLabelsA.contains(lbl))
					differences++;
			}
		}
		differences += Math.abs(n.getDegree() - layoutedNode.getDegree());
		return differences;
	}
	
	public void reset() {
	}
	
	public String getCategory() {
		return "Layout";
	}
	
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	public boolean showMenuIcon() {
		return false;
	}
	
	public KeyStroke getAcceleratorKeyStroke() {
		return null;
	}
	
	public String getDescription() {
		return null;
	}
	
	public ActionEvent getActionEvent() {
		return null;
	}
	
	public void setActionEvent(ActionEvent a) {
		// empty
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}
}
