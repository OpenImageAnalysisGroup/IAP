/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder;

import java.util.ArrayList;

import org.graffiti.graph.Node;

/**
 * Saves the information about pattern type and number for all nodes of a
 * graph
 * 
 * @author klukas
 */
public class NodeCacheEntry3d {
	
	/**
	 * Cache for Node Positions
	 */
	public org.Vector3d position;
	
	/**
	 * Used for storing the information about the last run-number of the last
	 * movement. The springembedder algorithm runs a number of times, each
	 * time a counter is increased by 1. As soon as a node has been updated,
	 * it lastTouch value is set to the current counter number. Patterns are
	 * calculated as one group. Because of the lastTouch value it can be
	 * checked, if a node has been already moved in the current
	 * springembedder run. This way the patterns are threated more equalily
	 * to the normal nodes, as they are not updated more frequently. This
	 * number has to be initialized to -1.
	 */
	public int lastTouch;
	
	/**
	 * Vector contains the size of the node (width, height)
	 */
	public org.Vector2d size;
	
	/**
	 * Name of pattern, "" if no pattern assigned
	 */
	public String patternType;
	
	/**
	 * Vector with <code>patterNodes</code> Objects, which store the
	 * information about all connected patterns.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList patternNodes;
	
	/**
	 * Contains the connected nodes (NodeCacheEntrys)
	 */
	@SuppressWarnings("unchecked")
	public ArrayList connectedNodes;
	
	/**
	 * 1..x Number of pattern. Similar patterns in the graph are numbered. One
	 * node might have the patternType 1 and patternIndex 1, the next node,
	 * that is not connected to this pattern might have the number
	 * patternType 1 and patternIndex 2. patternIndex == -1 if no pattern is
	 * assigned
	 */
	public int patternIndex;
	
	/**
	 * Saves the index of the node in the graph-array.
	 */
	public int nodeIndex;
	
	/**
	 * If cluster information for a node is available, then this number is set
	 * to the specific cluster nubmer. All nodes within a cluster get the same
	 * cluster index number (id). The node attribute is called cluster/cluster:xyz.
	 * This value will be set to "", if no cluster information is set
	 * for this node.
	 */
	String clusterIndexNumber;
	
	/**
	 * Reference to a node in the pattern graph.
	 */
	public Node patternNode;
	
	/**
	 * Reference to the node in the graph.
	 */
	public Node node;
	
	/**
	 * This field should be set to true, if the user has selected this node in the GUI.
	 */
	public boolean selected;
}
