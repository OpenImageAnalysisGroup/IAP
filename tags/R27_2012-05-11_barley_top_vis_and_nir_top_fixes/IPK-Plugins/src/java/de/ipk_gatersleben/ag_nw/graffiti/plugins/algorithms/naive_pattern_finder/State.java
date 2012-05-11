/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

/**
 * Interface for states, which may be used by the <code>Matcher</code> object
 * for finding graph and subgraph isomorphisms.
 * 
 * @author Dirk Koschützki
 * @see Matcher
 */
interface State
					extends Cloneable {
	
	/**
	 * The integer value for "not a node at all".
	 */
	public static int NULL_NODE = -1;
	
	/**
	 * Computes the next pair of nodes to be checked. prevPatternNodeID and
	 * prevTargetNodeID must be the last nodes tried, or NULL_NODE to start
	 * from the first pair.
	 * 
	 * @param prevPatternNodeID
	 *           node id of the pattern graph
	 * @param prevTargetNodeID
	 *           node id of the target graph
	 * @return false, if no more pairs are available.
	 */
	public boolean computeNextPair(int prev_n1, int prev_n2);
	
	/**
	 * Checks if the given nodes are feasible, e.g. if they are compatible in
	 * the current state.
	 * 
	 * @param nodeOfPatternGraph
	 *           a node from the pattern graph
	 * @param nodeOfTargetGraph
	 *           a node from the target graph
	 * @return true, if (nodeOfPatternGraph, nodeOfTargetGraph) can be added
	 *         to the state
	 */
	public boolean isFeasiblePair(int node1, int node2);
	
	/**
	 * Adds the pair to the Core set of the state.
	 * 
	 * @param nodeOfPatternGraph
	 *           a node from the pattern graph
	 * @param nodeOfTargetGraph
	 *           a node from the target graph
	 */
	public void addPair(int node1, int node2);
	
	/**
	 * Returns the next node of the pattern graph.
	 * 
	 * @return the next node the try
	 */
	public int getNextNodeOfPattern();
	
	/**
	 * Returns the next node of the target graph.
	 * 
	 * @return the next node the try
	 */
	public int getNextNodeOfTarget();
	
	/**
	 * Checks if we arrived at the goal.
	 * 
	 * @return true, if we found a complete match.
	 */
	public boolean isGoal();
	
	/**
	 * Checks if there is another interesting state.
	 * 
	 * @return True, if no more feasible states exists.
	 */
	public boolean isDead();
	
	/**
	 * Perform a back tracking step to the previous state.
	 */
	public void backtrack();
	
	/**
	 * Returns the pattern graph.
	 * 
	 * @return the pattern graph
	 */
	public Graph getPatternGraph();
	
	/**
	 * Returns the target graph.
	 * 
	 * @return the target graph
	 */
	public Graph getTargetGraph();
	
	/**
	 * Returns the length of the core.
	 * 
	 * @return the current length of the core.
	 */
	public int getCoreLength();
	
	/**
	 * Returns an array of matching nodes from the pattern graph.
	 * 
	 * @return the matching nodes from the pattern graph.
	 */
	public Node[] getMatchingNodesOfPattern();
	
	/**
	 * Returns an array of matching nodes from the target graph.
	 * 
	 * @return the matching nodes from the target graph.
	 */
	public Node[] getMatchingNodesOfTarget();
	
	/**
	 * Creates and returns a deep copy of this object.
	 * 
	 * @return a deep copy of this object.
	 */
	public Object clone();
}
