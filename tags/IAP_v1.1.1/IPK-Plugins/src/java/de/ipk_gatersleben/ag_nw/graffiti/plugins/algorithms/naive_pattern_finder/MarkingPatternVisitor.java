/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.graphics.NodeLabelAttribute;

/**
 * This pattern visitor marks all occurances of the matched pattern in the
 * target graph. As a special extension a match is skipped, if at least one
 * node is a member of a previous match. Therefore no node will be a member
 * of more than one match.
 * 
 * @author Dirk Koschützki
 */
class MarkingPatternVisitor
					implements PatternVisitor {
	
	/**
	 * Mapping from pattern names to a counter of occurances of the pattern in
	 * the target graph.
	 */
	private Map<String, Integer> maxOccurancePatternName;
	private boolean ignoreEdgeDirection;
	
	/**
	 * Creates a new MarkingPatternVisitor object.
	 */
	public MarkingPatternVisitor(boolean ignoreEdgeDirection) {
		super();
		maxOccurancePatternName = new HashMap<String, Integer>();
		this.ignoreEdgeDirection = ignoreEdgeDirection;
	}
	
	/**
	 * This method is invoked for every match that has been found by the
	 * matcher. The matched nodes are marked with an pattern attribute.
	 * 
	 * @param numberOfNodesInMatch
	 *           the number of nodes in the match
	 * @param matchInPattern
	 *           the nodes from the pattern graph
	 * @param matchInTarget
	 *           the nodes from the target graph
	 * @param patternName
	 *           name of the pattern which was found
	 * @return always false as we are interested in all matches
	 */
	public boolean visitPattern(int numberOfNodesInMatch,
						Node[] matchInPattern, Node[] matchInTarget,
						String patternName) {
		/* if we have marked one of the nodes, then we ignore this match completely. */
		if (checkForDuplicateMatch(matchInTarget)) {
			return false;
		}
		
		printoutLabels(matchInPattern, "Pattern");
		printoutLabels(matchInTarget, "Target");
		
		markNodes(matchInPattern, matchInTarget, patternName);
		markEdges(matchInPattern, matchInTarget, patternName);
		incrMaxOccurance(patternName);
		
		// returning false means: i'm not happy. give me the next one.
		return false;
	}
	
	/**
	 * Checks if one of the nodes is already marked with a pattern.
	 * 
	 * @param matchInTarget
	 *           the nodes of interest
	 * @return true, if at least one node is already part of a match
	 */
	public static boolean checkForDuplicateMatch(Node[] matchInTarget) {
		for (int i = 0; i < matchInTarget.length; i++) {
			Node nodeInTarget = matchInTarget[i];
			
			int patternPosition =
								PatternAttributeUtils.getMaximumPatternPosition(nodeInTarget);
			
			if (patternPosition >= 1) {
				// System.err.println("Skipping a match!");
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Prints the labels of the nodes to stderr.
	 * 
	 * @param matchingNodes
	 *           the nodes
	 * @param prefix
	 *           a prefix used in the line
	 */
	private void printoutLabels(Node[] matchingNodes, String prefix) {
		/* print out the labels from the graph! */
		for (int i = 0; i < matchingNodes.length; i++) {
			Node n1 = matchingNodes[i];
			
			try {
				NodeLabelAttribute nl1 =
									(NodeLabelAttribute) n1.getAttribute("label");
				
				System.err.println(prefix + " : " + nl1.getLabel());
			} catch (AttributeNotFoundException e) {
				System.err.println(prefix + " : NO-LABEL!");
			}
		}
	}
	
	/**
	 * Markes the given nodes in the target graph with the given pattern name
	 * and the necessary additional occurance information.
	 * 
	 * @param matchInPattern
	 *           Nodes from the pattern graph
	 * @param matchInTarget
	 *           Nodes from the target graph
	 * @param patternName
	 *           Name of the pattern
	 */
	private void markNodes(Node[] matchInPattern, Node[] matchInTarget,
						String patternName) {
		Integer patternOccurance = getMaxOccurance(patternName);
		
		for (int i = 0; i < matchInTarget.length; i++) {
			Node nodeInTarget = matchInTarget[i];
			
			int patternPosition =
								PatternAttributeUtils.getMaximumPatternPosition(nodeInTarget);
			
			patternPosition = patternPosition + 1;
			
			assert (patternPosition == 1);
			
			PatternAttributeUtils.addPatternInformation(nodeInTarget,
								patternPosition,
								patternName,
								patternOccurance,
								new Integer(i + 1),
								matchInPattern[i]);
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param matchInPattern
	 *           DOCUMENT ME!
	 * @param matchInTarget
	 *           DOCUMENT ME!
	 * @param patternName
	 *           DOCUMENT ME!
	 */
	private void markEdges(Node[] matchInPattern, Node[] matchInTarget,
						String patternName) {
		Integer patternOccurance = getMaxOccurance(patternName);
		
		for (int i = 0; i < matchInPattern.length; i++) {
			Node sourceInPatternGraph = matchInPattern[i];
			
			for (int j = 0; j < matchInPattern.length; j++) {
				Node targetInPatternGraph = matchInPattern[j];
				
				if (NaivePatternFinderUtils.checkIfEdgeExists(sourceInPatternGraph,
									targetInPatternGraph,
									ignoreEdgeDirection)) {
					Edge edgeInTargetGraph =
										NaivePatternFinderUtils.getUniqueDirectedEdge(matchInTarget[i],
															matchInTarget[j],
															ignoreEdgeDirection);
					
					int patternPosition =
										PatternAttributeUtils.getMaximumPatternPosition(edgeInTargetGraph);
					
					patternPosition = patternPosition + 1;
					
					assert (patternPosition == 1);
					
					PatternAttributeUtils.addPatternInformation(edgeInTargetGraph,
										patternPosition,
										patternName,
										patternOccurance,
										new Integer(i
															+ 1));
					
				}
			}
		}
	}
	
	/**
	 * Returns the max occurance value for the given pattern name.
	 * 
	 * @param patternName
	 *           the pattern name of interest
	 * @return the max occurance value
	 */
	private Integer getMaxOccurance(String patternName) {
		Object currentValue = maxOccurancePatternName.get(patternName);
		
		if (currentValue == null) {
			Integer eins = new Integer(1);
			
			maxOccurancePatternName.put(patternName, eins);
			return eins;
		} else {
			return ((Integer) currentValue);
		}
	}
	
	/**
	 * Increments the max occurance value of the given pattern name.
	 * 
	 * @param patternName
	 *           the pattern name of interest
	 */
	private void incrMaxOccurance(String patternName) {
		Integer currentValue =
							(Integer) maxOccurancePatternName.get(patternName);
		Integer newValue = new Integer(currentValue.intValue() + 1);
		
		maxOccurancePatternName.put(patternName, newValue);
	}
}
