/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 20.12.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;

public class WeightedDistanceInfo {
	private HashSet<GraphElement> sourceGraphElementsWithShortestDistance = new HashSet<GraphElement>();
	private HashSet<GraphElement> rejectedGraphElementsBecauseOfNonOptimalDistance = new HashSet<GraphElement>();
	private double currentlyKnownMinimumDistance;
	private double myDistancePenality;
	private GraphElement thisGraphElement;
	private static double epsilon = 0.000000000001;
	
	public WeightedDistanceInfo(double initDistance, GraphElement sourceNode, GraphElement thisNode,
						boolean considerNodeWeight, boolean considerEdgeWeight,
						AttributePathNameSearchType weightattribute, boolean putWeightOnEdges,
						boolean addAttribute) {
		sourceGraphElementsWithShortestDistance.add(sourceNode);
		this.thisGraphElement = thisNode;
		if (thisGraphElement instanceof Node) {
			if (!considerNodeWeight || weightattribute == null)
				myDistancePenality = 0;
			else
				myDistancePenality = weightattribute.getAttributeValue(thisGraphElement, Double.MAX_VALUE);
			if (!considerEdgeWeight && !considerNodeWeight && !putWeightOnEdges)
				myDistancePenality = 1;
		}
		if (thisGraphElement instanceof Edge) {
			if (!considerEdgeWeight) {
				if (considerNodeWeight)
					myDistancePenality = 0;
			} else
				myDistancePenality = weightattribute.getAttributeValue(thisGraphElement, Double.MAX_VALUE);
			if (!considerEdgeWeight && !considerNodeWeight && putWeightOnEdges)
				myDistancePenality = 1;
		}
		currentlyKnownMinimumDistance = initDistance + myDistancePenality;
		if (addAttribute)
			AttributeHelper.setAttribute(thisNode, "properties", "weight", myDistancePenality);
	}
	
	public GraphElement getGraphElement() {
		return thisGraphElement;
	}
	
	public double getMinDistance() {
		return currentlyKnownMinimumDistance;
	}
	
	public void checkDistanceAndMemorizePossibleSourceElement(GraphElement workGraphElement, double distanceUntilNeighbourElement) {
		if (Math.abs(distanceUntilNeighbourElement + myDistancePenality - currentlyKnownMinimumDistance) < epsilon) {
			sourceGraphElementsWithShortestDistance.add(workGraphElement);
		} else
			if (distanceUntilNeighbourElement + myDistancePenality < currentlyKnownMinimumDistance) {
				sourceGraphElementsWithShortestDistance.clear();
				sourceGraphElementsWithShortestDistance.add(workGraphElement);
				currentlyKnownMinimumDistance = distanceUntilNeighbourElement + myDistancePenality;
			} else {
				rejectedGraphElementsBecauseOfNonOptimalDistance.add(workGraphElement);
			}
	}
	
	public Collection<GraphElement> getConnectedGraphElements(boolean directed) {
		Collection<GraphElement> neighbours = new ArrayList<GraphElement>();
		if (directed) {
			if (thisGraphElement instanceof Node) {
				for (Edge e : ((Node) thisGraphElement).getDirectedOutEdges())
					neighbours.add(e);
			} else {
				neighbours.add(((Edge) thisGraphElement).getTarget());
			}
		} else {
			if (thisGraphElement instanceof Node) {
				for (Edge e : ((Node) thisGraphElement).getEdges())
					neighbours.add(e);
			} else {
				neighbours.add(((Edge) thisGraphElement).getSource());
				neighbours.add(((Edge) thisGraphElement).getTarget());
			}
		}
		return neighbours;
	}
	
	public boolean allPossibleSourcePathsTraversed(boolean directed) {
		boolean foundNotProcessed = false;
		for (GraphElement ge : getConnectedGraphElements(directed)) {
			if (!sourceGraphElementsWithShortestDistance.contains(ge) &&
								!rejectedGraphElementsBecauseOfNonOptimalDistance.contains(ge)) {
				foundNotProcessed = true;
				break;
			}
		}
		return !foundNotProcessed;
	}
	
	public Collection<GraphElement> getSourceGraphElementsWithMinimalDistance() {
		return sourceGraphElementsWithShortestDistance;
	}
}
