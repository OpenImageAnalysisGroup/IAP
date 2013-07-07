/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import java.util.HashSet;

import org.graffiti.graph.Node;

/**
 * Graph matcher for graph and subgraph isomorphism.
 * 
 * @author Dirk Koschützki
 */
class Matcher {
	
	/**
	 * The matching nodes from the pattern graph.
	 */
	private Node[] matchingNodesOfPattern;
	
	/**
	 * The matching nodes from the target graph.
	 */
	private Node[] matchingNodesOfTarget;
	
	/**
	 * Number of nodes in this special match.
	 */
	private int numberOfMatchingNodes;
	
	/**
	 * Finds a matching between two graph, if it exists, given the initial
	 * state of the matching process. Returns true if a match has been found.
	 * numberOfMatchingNodes is assigned the number of matched nodes, and
	 * matchingNodesOfPattern and matchingNodesOfTarget will contain the
	 * corresponding nodes in the two graphs.
	 * 
	 * @param state
	 *           the state for the matching process
	 * @return true, if a match was found
	 */
	public boolean match(State state) {
		return match2(state);
	}
	
	/**
	 * Visits all the matches between two graphs, given the initial state of
	 * the match. Stops when there are no more matches, or the visitor
	 * returns true.
	 * 
	 * @param state
	 *           the state for the matching process
	 * @param visitor
	 *           the visitor to call after every match
	 * @param patternName
	 *           the name of the pattern
	 */
	public void match(State state, PatternVisitor visitor,
						PatternVisitor optVisitor2, HashSet<Node> resultNodes, String patternName) {
		match2(state, visitor, optVisitor2, resultNodes, patternName);
	}
	
	/**
	 * Returns the matching nodes from the pattern graph.
	 * 
	 * @return the matching nodes
	 */
	public Node[] getMatchingNodesOfPattern() {
		return matchingNodesOfPattern;
	}
	
	/**
	 * Returns the matching nodes from the target graph.
	 * 
	 * @return the matching nodes
	 */
	public Node[] getMatchingNodesOfTarget() {
		return matchingNodesOfTarget;
	}
	
	/**
	 * Returns the number of matching nodes.
	 * 
	 * @return the number of matching nodes
	 */
	public int getNumberOfMatchingNodes() {
		return numberOfMatchingNodes;
	}
	
	/**
	 * Recursive implementation of the matching function.
	 * 
	 * @param state
	 *           the state for the matching process
	 * @return true, if a match was found
	 */
	private boolean match2(State state) {
		if (state.isGoal()) {
			numberOfMatchingNodes = state.getCoreLength();
			matchingNodesOfPattern = state.getMatchingNodesOfPattern();
			matchingNodesOfTarget = state.getMatchingNodesOfTarget();
			return true;
		}
		
		if (state.isDead()) {
			return false;
		}
		
		int n1 = State.NULL_NODE;
		int n2 = State.NULL_NODE;
		boolean found = false;
		
		while (!found && state.computeNextPair(n1, n2)) {
			n1 = state.getNextNodeOfPattern();
			n2 = state.getNextNodeOfTarget();
			if (state.isFeasiblePair(n1, n2)) {
				State nextState = (UllmannSubgraphIsomState) state.clone();
				
				nextState.addPair(n1, n2);
				found = match2(nextState);
				nextState.backtrack();
			}
		}
		
		return found;
	}
	
	/**
	 * Recursive implementation of the matching function.
	 * 
	 * @param state
	 *           the state for the matching process
	 * @param visitor
	 *           the visitor to call after every match
	 * @param additionalInformation
	 *           the name of the pattern
	 * @return true, if a match was found
	 */
	private boolean match2(State state, PatternVisitor visitor, PatternVisitor optVisitor2,
						HashSet<Node> resultNodes,
						String additionalInformation) {
		if (state.isGoal()) {
			numberOfMatchingNodes = state.getCoreLength();
			matchingNodesOfPattern = state.getMatchingNodesOfPattern();
			matchingNodesOfTarget = state.getMatchingNodesOfTarget();
			if (resultNodes != null)
				for (Node n : matchingNodesOfTarget)
					resultNodes.add(n);
			if (optVisitor2 != null)
				optVisitor2.visitPattern(numberOfMatchingNodes,
									matchingNodesOfPattern,
									matchingNodesOfTarget,
									additionalInformation);
			return visitor.visitPattern(numberOfMatchingNodes,
								matchingNodesOfPattern,
								matchingNodesOfTarget,
								additionalInformation);
		}
		
		if (state.isDead()) {
			return false;
		}
		
		int nodeOfPattern = State.NULL_NODE;
		int nodeOfTarget = State.NULL_NODE;
		
		while (state.computeNextPair(nodeOfPattern, nodeOfTarget)) {
			nodeOfPattern = state.getNextNodeOfPattern();
			nodeOfTarget = state.getNextNodeOfTarget();
			if (state.isFeasiblePair(nodeOfPattern, nodeOfTarget)) {
				State nextState = (UllmannSubgraphIsomState) state.clone();
				
				nextState.addPair(nodeOfPattern, nodeOfTarget);
				
				if (match2(nextState, visitor, optVisitor2, resultNodes, additionalInformation)) {
					nextState.backtrack();
					return true;
				} else {
					nextState.backtrack();
				}
			}
		}
		
		return false;
	}
}
