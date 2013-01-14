/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 29.03.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths;

import java.util.Collection;
import java.util.HashSet;

import org.graffiti.graph.Node;

public class DistanceInfo {
	
	private HashSet<Node> sourceNodes = new HashSet<Node>();
	private HashSet<Node> rejectedNodes = new HashSet<Node>();
	private double minDistance = Double.MIN_VALUE;
	private Node thisNode;
	
	public DistanceInfo(double distance, Node sourceNode, Node thisNode) {
		minDistance = distance;
		sourceNodes.add(sourceNode);
		this.thisNode = thisNode;
	}
	
	public Node getNode() {
		return thisNode;
	}
	
	public double getMinDistance() {
		return minDistance;
	}
	
	public void updateSourceIfValid(Node workNode, double minDistanceCheck) {
		if (minDistanceCheck <= minDistance) {
			sourceNodes.add(workNode);
		} else {
			rejectedNodes.add(workNode);
		}
	}
	
	public boolean allRelevantEdgesProcessed(boolean directed) {
		Collection<Node> checkTheseNodes;
		if (directed)
			checkTheseNodes = thisNode.getAllInNeighbors();
		else
			checkTheseNodes = thisNode.getNeighbors();
		boolean foundNotProcessed = false;
		for (Node n : checkTheseNodes) {
			if (!sourceNodes.contains(n) && !rejectedNodes.contains(n)) {
				foundNotProcessed = true;
				break;
			}
		}
		return !foundNotProcessed;
	}
	
	public Collection<Node> getSourceNodes() {
		return sourceNodes;
	}
}
