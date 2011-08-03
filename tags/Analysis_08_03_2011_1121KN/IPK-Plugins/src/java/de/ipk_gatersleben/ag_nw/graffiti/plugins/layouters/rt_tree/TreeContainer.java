/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;

/**
 * Contains additional informations about each tree in the forest.
 * 
 * @author Joerg Bartelheimer
 */
public class TreeContainer {
	
	/**
	 * Height offset of nodes in each level of the tree.
	 * 
	 * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.RTTreeLayout#depthOffsets
	 */
	private HashMap<Integer, Double> depthOffset = new LinkedHashMap<Integer, Double>();
	
	/**
	 * Max y dimension of nodes in each level of the tree.
	 * 
	 * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.RTTreeLayout#maxNodeHeight
	 */
	private HashMap<Integer, Double> maxNodeHeight = new LinkedHashMap<Integer, Double>();
	
	/**
	 * The depth for each node .
	 * 
	 * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.RTTreeLayout#bfsNum
	 */
	private HashMap<Node, Integer> bfsNum = new LinkedHashMap<Node, Integer>();
	
	private HashMap<Node, Integer> bfsNumUpsideDown = new LinkedHashMap<Node, Integer>();
	
	/**
	 * Holds the tree in a linkedlist which is used by getLeftBrother
	 * 
	 * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.RTTreeLayout#treeMap
	 */
	private LinkedList<?> treeMap = new LinkedList<Object>();
	
	private HashSet<Edge> edges;
	
	public TreeContainer(
						LinkedList<?> treeMap,
						HashMap<Integer, Double> depthOffset,
						HashMap<Node, Integer> bfsNum,
						HashMap<Integer, Double> maxNodeHeight) {
		this.treeMap = treeMap;
		this.depthOffset = depthOffset;
		this.bfsNum = bfsNum;
		this.maxNodeHeight = maxNodeHeight;
	}
	
	public TreeContainer(HashMap<Node, Integer> bfsNum, HashMap<Integer, Double> maxNodeHeight) {
		this.bfsNum = bfsNum;
		this.maxNodeHeight = maxNodeHeight;
	}
	
	public TreeContainer(HashMap<Node, Integer> bfsNum,
						HashMap<Integer, Double> maxNodeHeight, HashSet<Edge> edges) {
		this.bfsNum = bfsNum;
		this.maxNodeHeight = maxNodeHeight;
		this.edges = edges;
	}
	
	public TreeContainer(
						HashMap<Integer, Double> depthOffset,
						HashMap<Node, Integer> bfsNum,
						HashMap<Integer, Double> maxNodeHeight,
						HashMap<Node, Integer> bfsNumUpsideDown) {
		this.depthOffset = depthOffset;
		this.bfsNum = bfsNum;
		this.maxNodeHeight = maxNodeHeight;
		this.bfsNumUpsideDown = bfsNumUpsideDown;
	}
	
	/**
	 * @return depthOffset stored for each node in the forest
	 */
	public HashMap<Integer, Double> getDepthOffset() {
		return depthOffset;
	}
	
	/**
	 * @return treeMap stored for each node in the forest
	 */
	public LinkedList<?> getTreeMap() {
		return treeMap;
	}
	
	/**
	 * @param depthOffset
	 */
	public void setDepthOffset(HashMap<Integer, Double> map) {
		depthOffset = map;
	}
	
	/**
	 * @param treeMap
	 */
	public void setTreeMap(LinkedList<?> list) {
		treeMap = list;
	}
	
	/**
	 * @return bfsNum stored for each node in the forest
	 */
	public HashMap<Node, Integer> getBfsNum() {
		return bfsNum;
	}
	
	/**
	 * @param bfsNum
	 */
	public void setBfsNum(HashMap<Node, Integer> map) {
		bfsNum = map;
	}
	
	/**
	 * @return the y dimension for each node
	 */
	public HashMap<Integer, Double> getMaxNodeHeight() {
		return maxNodeHeight;
	}
	
	/**
	 * @param maxNodeHeight
	 */
	public void setMaxNodeHeight(HashMap<Integer, Double> map) {
		maxNodeHeight = map;
	}
	
	public HashSet<Edge> getEdges() {
		return edges;
	}
	
	public HashMap<Node, Integer> getBfsNumUpsideDown() {
		return bfsNumUpsideDown;
	}
}
