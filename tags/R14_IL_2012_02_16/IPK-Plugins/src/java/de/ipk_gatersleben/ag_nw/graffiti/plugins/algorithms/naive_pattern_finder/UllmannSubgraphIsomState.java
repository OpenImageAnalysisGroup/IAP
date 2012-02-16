/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.EdgeLabelAttribute;
import org.graffiti.graphics.NodeLabelAttribute;

/**
 * A representation of the current search state of the Ullmann's algorithm for
 * graph-subgraph isomorphism.
 * 
 * @author Dirk Koschützki, some performance optimization by Christian Klukas
 */
class UllmannSubgraphIsomState
					implements State {
	
	/**
	 * The graph we are searching within.
	 */
	private Graph targetGraph;
	
	/**
	 * The pattern we are interested in.
	 */
	private Graph patternGraph;
	
	/**
	 * The number of nodes in the target graph.
	 */
	private int numberOfNodesInTargetGraph;
	
	/**
	 * The number of nodes in the pattern graph.
	 */
	private int numberOfNodesInPatternGraph;
	
	/**
	 * A mapping from integer values to nodes for the target graph.
	 */
	private Node[] numberedNodesInTargetGraph;
	
	/**
	 * A mapping from integer values to nodes for the pattern graph.
	 */
	private Node[] numberedNodesInPatternGraph;
	
	/**
	 * The length of the current "core set" (the result array).
	 */
	private int currentLengthOfCore;
	
	/**
	 * Ids of the nodes in the target graph, for this special match.
	 */
	private int[] coreSetOfTargetGraph;
	
	/**
	 * Ids of the nodes in the pattern graph, for this special match.
	 */
	private int[] coreSetOfPatternGraph;
	
	/**
	 * The Id of the node in the target graph we have to check next.
	 */
	private int nextNodeOfTarget;
	
	/**
	 * The Id of the node in the pattern graph we have to check next.
	 */
	private int nextNodeOfPattern;
	
	/**
	 * Boolean matrix showing the compatibility of nodes in the two graphs.
	 */
	private boolean[][] compatibilityMatrix;
	
	private boolean ignoreEdgeDirection;
	
	HashMap<Node, HashSet<Node>> node2neighbour = new HashMap<Node, HashSet<Node>>();
	HashMap<Node, HashSet<Node>> patNode2neighbour = new HashMap<Node, HashSet<Node>>();
	
	/**
	 * Creates an empty UllmannSubgraphIsomState object. This method is used
	 * by the clone method to create a new (empty) object instance.
	 */
	private UllmannSubgraphIsomState(boolean ignoreEdgeDirection) {
		this.ignoreEdgeDirection = ignoreEdgeDirection;
		
		node2neighbour.clear();
		patNode2neighbour.clear();
	}
	
	/**
	 * Creates a new UllmannSubgraphIsomState object.
	 * 
	 * @param patternGraph
	 *           the pattern graph
	 * @param targetGraph
	 *           the target graph
	 */
	public UllmannSubgraphIsomState(Graph patternGraph, Graph targetGraph, boolean ignoreEdgeDirection, HashSet<Node> ignoreTheseNodes) {
		
		this.ignoreEdgeDirection = ignoreEdgeDirection;
		
		this.targetGraph = targetGraph;
		this.patternGraph = patternGraph;
		
		this.numberOfNodesInTargetGraph = targetGraph.getNumberOfNodes();
		this.numberOfNodesInPatternGraph = patternGraph.getNumberOfNodes();
		
		this.numberedNodesInTargetGraph = encodeNodes(targetGraph);
		this.numberedNodesInPatternGraph = encodeNodes(patternGraph);
		
		node2neighbour.clear();
		patNode2neighbour.clear();
		
		this.currentLengthOfCore = 0;
		
		this.coreSetOfTargetGraph = new int[numberOfNodesInTargetGraph];
		this.coreSetOfPatternGraph = new int[numberOfNodesInPatternGraph];
		
		for (int i = 0; i < numberOfNodesInPatternGraph; i++) {
			coreSetOfPatternGraph[i] = State.NULL_NODE;
		}
		
		for (int i = 0; i < numberOfNodesInTargetGraph; i++) {
			coreSetOfTargetGraph[i] = State.NULL_NODE;
		}
		
		this.nextNodeOfTarget = State.NULL_NODE;
		this.nextNodeOfPattern = State.NULL_NODE;
		
		this.compatibilityMatrix = new boolean[numberOfNodesInPatternGraph][];
		
		for (int i = 0; i < numberOfNodesInPatternGraph; i++) {
			compatibilityMatrix[i] = new boolean[numberOfNodesInTargetGraph];
		}
		
		Node nodeOfPatternGraph;
		Node nodeOfTargetGraph;
		
		for (int i = 0; i < numberOfNodesInPatternGraph; i++) {
			nodeOfPatternGraph = numberedNodesInPatternGraph[i];
			for (int j = 0; j < numberOfNodesInTargetGraph; j++) {
				nodeOfTargetGraph = numberedNodesInTargetGraph[j];
				if (ignoreTheseNodes != null && ignoreTheseNodes.contains(nodeOfTargetGraph)) {
					compatibilityMatrix[i][j] = false;
					continue;
				}
				if (!ignoreEdgeDirection) {
					/*
					 * Two nodes are compatible, if
					 * - the indegree of the target graph is equal to the indegree of the node of the
					 * pattern graph plus the given range (min/max value)
					 * - the outdegree of the target graph is equal to the outdegree of the node of the
					 * pattern graph plus the given range (min/max value)
					 * - and if the labels are compatible to each other.
					 */
					int nodeOfTargetGraphInDegree =
										nodeOfTargetGraph.getInDegree();
					int nodeOfTargetGraphOutDegree =
										nodeOfTargetGraph.getOutDegree();
					
					int nodeOfPatternGraphInDegree =
										nodeOfPatternGraph.getInDegree();
					int nodeOfPatternGraphOutDegree =
										nodeOfPatternGraph.getOutDegree();
					
					int patternGraphMinAddIn =
										PatternAttributeUtils.getMinAddIncEdges(nodeOfPatternGraph);
					int patternGraphMaxAddIn =
										PatternAttributeUtils.getMaxAddIncEdges(nodeOfPatternGraph);
					
					int patternGraphMinAddOut =
										PatternAttributeUtils.getMinAddOutEdges(nodeOfPatternGraph);
					int patternGraphMaxAddOut =
										PatternAttributeUtils.getMaxAddOutEdges(nodeOfPatternGraph);
					
					boolean compatibleInDegree =
										((nodeOfPatternGraphInDegree + patternGraphMinAddIn) <= nodeOfTargetGraphInDegree)
															&& (nodeOfTargetGraphInDegree <= (nodeOfPatternGraphInDegree
															+ patternGraphMaxAddIn));
					
					boolean compatibleOutDegree =
										((nodeOfPatternGraphOutDegree + patternGraphMinAddOut) <= nodeOfTargetGraphOutDegree)
															&& (nodeOfTargetGraphOutDegree <= (nodeOfPatternGraphOutDegree
															+ patternGraphMaxAddOut));
					
					boolean compatibleLabels =
										isCompatibleNode(nodeOfPatternGraph, nodeOfTargetGraph);
					
					compatibilityMatrix[i][j] = compatibleInDegree && compatibleOutDegree
										&& compatibleLabels;
				} else {
					/*
					 * Two nodes are compatible, if
					 * - the degree of the target graph is equal to the degree of the node of the
					 * pattern graph plus the given range (min/max value)
					 * - and if the labels are compatible to each other.
					 */
					int nodeOfTargetGraphDegree =
										nodeOfTargetGraph.getDegree();
					
					int nodeOfPatternGraphDegree =
										nodeOfPatternGraph.getDegree();
					
					int patternGraphMinAdd =
										PatternAttributeUtils.getMinAddIncEdges(nodeOfPatternGraph);
					int patternGraphMaxAdd =
										PatternAttributeUtils.getMaxAddIncEdges(nodeOfPatternGraph);
					
					boolean compatibleDegree =
										((nodeOfPatternGraphDegree + patternGraphMinAdd) <= nodeOfTargetGraphDegree)
															&& (nodeOfTargetGraphDegree <= (nodeOfPatternGraphDegree + patternGraphMaxAdd));
					
					boolean compatibleLabels =
										isCompatibleNode(nodeOfPatternGraph, nodeOfTargetGraph);
					
					compatibilityMatrix[i][j] = compatibleDegree && compatibleLabels;
					
				}
			}
		}
	}
	
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
	public boolean computeNextPair(int prevPatternNodeID, int prevTargetNodeID) {
		if (prevPatternNodeID == State.NULL_NODE) {
			prevPatternNodeID = currentLengthOfCore;
			prevTargetNodeID = 0;
		} else {
			if (prevTargetNodeID == State.NULL_NODE) {
				prevTargetNodeID = 0;
			} else {
				prevTargetNodeID++;
			}
		}
		
		if (prevTargetNodeID >= numberOfNodesInTargetGraph) {
			prevPatternNodeID++;
			prevTargetNodeID = 0;
		}
		
		if (prevPatternNodeID != currentLengthOfCore) {
			return false;
		}
		while (prevTargetNodeID < numberOfNodesInTargetGraph
							&& !compatibilityMatrix[prevPatternNodeID][prevTargetNodeID]) {
			prevTargetNodeID++;
		}
		
		if (prevTargetNodeID < numberOfNodesInTargetGraph) {
			nextNodeOfPattern = prevPatternNodeID;
			nextNodeOfTarget = prevTargetNodeID;
			return true;
		} else {
			return false;
			
		}
	}
	
	/**
	 * Returns the next node of the pattern graph.
	 * 
	 * @return the next node the try
	 */
	public int getNextNodeOfPattern() {
		return nextNodeOfPattern;
	}
	
	/**
	 * Returns the next node of the target graph.
	 * 
	 * @return the next node the try
	 */
	public int getNextNodeOfTarget() {
		return nextNodeOfTarget;
	}
	
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
	public boolean isFeasiblePair(int nodeOfPatternGraph, int nodeOfTargetGraph) {
		return compatibilityMatrix[nodeOfPatternGraph][nodeOfTargetGraph];
	}
	
	/**
	 * Adds the pair to the Core set of the state.
	 * 
	 * @param nodeOfPatternGraph
	 *           a node from the pattern graph
	 * @param nodeOfTargetGraph
	 *           a node from the target graph
	 */
	public void addPair(int nodeOfPatternGraph, int nodeOfTargetGraph) {
		coreSetOfPatternGraph[nodeOfPatternGraph] = nodeOfTargetGraph;
		coreSetOfTargetGraph[nodeOfTargetGraph] = nodeOfPatternGraph;
		
		currentLengthOfCore++;
		
		int k;
		
		for (k = currentLengthOfCore; k < numberOfNodesInPatternGraph; k++) {
			compatibilityMatrix[k][nodeOfTargetGraph] = false;
		}
		
		refine();
	}
	
	/**
	 * Checks if we arrived at the goal.
	 * 
	 * @return true, if we found a complete match.
	 */
	public boolean isGoal() {
		return currentLengthOfCore == numberOfNodesInPatternGraph;
	}
	
	/**
	 * Checks if there is another interesting state.
	 * 
	 * @return True, if no more feasible states exists.
	 */
	public boolean isDead() {
		if (numberOfNodesInPatternGraph > numberOfNodesInTargetGraph) {
			return true;
		}
		
		boolean finishRow = false;
		int i = currentLengthOfCore;
		
		while (i < numberOfNodesInPatternGraph) {
			int j = 0;
			
			finishRow = false;
			while (!finishRow && j < numberOfNodesInTargetGraph) {
				if (compatibilityMatrix[i][j]) {
					finishRow = true;
				}
				
				j++;
			}
			
			if (!finishRow) {
				return true;
			} else {
				i++;
			}
		}
		
		return false;
	}
	
	/**
	 * Dummy method to fit into the general matcher. This algorithm does not
	 * perform a backtrack at all.
	 */
	public void backtrack() {
		/* empty method! */
	}
	
	/**
	 * Returns the target graph.
	 * 
	 * @return the target graph.
	 */
	public Graph getTargetGraph() {
		return targetGraph;
	}
	
	/**
	 * Returns the pattern graph.
	 * 
	 * @return the pattern graph.
	 */
	public Graph getPatternGraph() {
		return patternGraph;
	}
	
	/**
	 * Returns the length of the core.
	 * 
	 * @return the current length of the core.
	 */
	public int getCoreLength() {
		return currentLengthOfCore;
	}
	
	/**
	 * Returns an array of matching nodes from the target graph.
	 * 
	 * @return the matching nodes from the target graph.
	 */
	public Node[] getMatchingNodesOfTarget() {
		int sizeOfArray =
							numberOfNodesInPatternGraph <= numberOfNodesInTargetGraph
												? numberOfNodesInPatternGraph
												: numberOfNodesInTargetGraph;
		
		int[] resultNodeIds = new int[sizeOfArray];
		int i;
		int j;
		
		for (i = 0, j = 0; i < numberOfNodesInPatternGraph; i++) {
			if (coreSetOfPatternGraph[i] != State.NULL_NODE) {
				resultNodeIds[j] = coreSetOfPatternGraph[i];
				j++;
			}
		}
		
		return extractNodes(numberedNodesInTargetGraph, resultNodeIds);
	}
	
	/**
	 * Returns an array of matching nodes from the pattern graph.
	 * 
	 * @return the matching nodes from the pattern graph.
	 */
	public Node[] getMatchingNodesOfPattern() {
		int sizeOfArray =
							numberOfNodesInPatternGraph <= numberOfNodesInTargetGraph
												? numberOfNodesInPatternGraph
												: numberOfNodesInTargetGraph;
		
		int[] resultNodeIds = new int[sizeOfArray];
		int i;
		int j;
		
		for (i = 0, j = 0; i < numberOfNodesInPatternGraph; i++) {
			if (coreSetOfPatternGraph[i] != State.NULL_NODE) {
				resultNodeIds[j] = i;
				j++;
			}
		}
		
		return extractNodes(numberedNodesInPatternGraph, resultNodeIds);
	}
	
	/**
	 * Creates a copy of this search state.
	 * 
	 * @return a clone.
	 */
	@Override
	public Object clone() {
		UllmannSubgraphIsomState newState = new UllmannSubgraphIsomState(ignoreEdgeDirection);
		
		newState.patternGraph = this.patternGraph;
		newState.targetGraph = this.targetGraph;
		
		newState.numberOfNodesInTargetGraph = this.numberOfNodesInTargetGraph;
		newState.numberOfNodesInPatternGraph =
							this.numberOfNodesInPatternGraph;
		
		newState.numberedNodesInTargetGraph = this.numberedNodesInTargetGraph;
		newState.numberedNodesInPatternGraph =
							this.numberedNodesInPatternGraph;
		
		newState.node2neighbour = this.node2neighbour;
		newState.patNode2neighbour = this.patNode2neighbour;
		
		newState.nextNodeOfTarget = this.nextNodeOfTarget;
		newState.nextNodeOfPattern = this.nextNodeOfPattern;
		
		newState.currentLengthOfCore = this.currentLengthOfCore;
		
		newState.coreSetOfTargetGraph =
							new int[this.numberOfNodesInTargetGraph];
		newState.coreSetOfPatternGraph =
							new int[this.numberOfNodesInPatternGraph];
		
		for (int i = 0; i < this.numberOfNodesInTargetGraph; i++) {
			newState.coreSetOfTargetGraph[i] = this.coreSetOfTargetGraph[i];
		}
		
		for (int i = 0; i < this.numberOfNodesInPatternGraph; i++) {
			newState.coreSetOfPatternGraph[i] = this.coreSetOfPatternGraph[i];
		}
		
		newState.compatibilityMatrix =
							new boolean[this.numberOfNodesInPatternGraph][];
		
		for (int i = 0; i < this.numberOfNodesInPatternGraph; i++) {
			newState.compatibilityMatrix[i] =
								new boolean[this.numberOfNodesInTargetGraph];
		}
		
		for (int i = 0; i < this.numberOfNodesInPatternGraph; i++) {
			for (int j = 0; j < this.numberOfNodesInTargetGraph; j++) {
				newState.compatibilityMatrix[i][j] =
									this.compatibilityMatrix[i][j];
			}
		}
		
		return newState;
	}
	
	/**
	 * The finalizer for the state. We implemented a finalizer, just to be
	 * sure, that every reference in a matrix is cleaned up as early as
	 * possible.
	 * 
	 * @throws Throwable
	 *            required by the contract with object.
	 */
	@Override
	protected void finalize()
						throws Throwable {
		coreSetOfPatternGraph = null;
		coreSetOfTargetGraph = null;
		
		int i;
		
		for (i = 0; i < numberOfNodesInPatternGraph; i++) {
			compatibilityMatrix[i] = null;
		}
		
		compatibilityMatrix = null;
		
		patternGraph = null;
		targetGraph = null;
		numberedNodesInPatternGraph = null;
		numberedNodesInTargetGraph = null;
		
		node2neighbour.clear();
		patNode2neighbour.clear();
	}
	
	/**
	 * Remove from the compatiblityMatrix all pairs which are not compatible
	 * with the isomorphism condition.
	 */
	private void refine() {
		/*
		 * This code is more or less a copy of the original one.
		 * I have no time to analyse it in detail and refactor it.
		 * The idea is: change as much as possible entries of the compatibility matrix
		 * to false. This reduces computation time dramatically.
		 */
		int patN1;
		int targetN1;
		int patN2;
		int targetN2;
		Node patternNode1;
		Node patternNode2;
		Node targetNode1;
		Node targetNode2;
		boolean edge_exists_pn12;
		boolean edge_exists_pn21;
		boolean edge_exists_tn12;
		boolean edge_exists_tn21;
		
		for (patN1 = currentLengthOfCore; patN1 < numberOfNodesInPatternGraph; patN1++) {
			patternNode1 = numberedNodesInPatternGraph[patN1];
			HashSet<Node> neighboursOfPatN1 = patNode2neighbour.get(patternNode1);
			
			if (neighboursOfPatN1 == null) {
				for (Node n : numberedNodesInPatternGraph) {
					if (ignoreEdgeDirection)
						patNode2neighbour.put(n, new HashSet<Node>(n.getNeighbors()));
					else
						patNode2neighbour.put(n, new HashSet<Node>(n.getOutNeighbors()));
				}
				neighboursOfPatN1 = patNode2neighbour.get(patternNode1);
			}
			
			for (targetN1 = 0; targetN1 < numberOfNodesInTargetGraph; targetN1++) {
				targetNode1 = numberedNodesInTargetGraph[targetN1];
				
				HashSet<Node> neighboursOfTargetN1 = node2neighbour.get(targetNode1);
				if (neighboursOfTargetN1 == null) {
					for (Node n : numberedNodesInTargetGraph) {
						if (ignoreEdgeDirection)
							node2neighbour.put(n, new HashSet<Node>(n.getNeighbors()));
						else
							node2neighbour.put(n, new HashSet<Node>(n.getOutNeighbors()));
					}
					neighboursOfTargetN1 = node2neighbour.get(targetNode1);
				}
				
				if (compatibilityMatrix[patN1][targetN1]) {
					for (patN2 = currentLengthOfCore - 1; patN2 < currentLengthOfCore; patN2++) {
						targetN2 = coreSetOfPatternGraph[patN2];
						patternNode2 = numberedNodesInPatternGraph[patN2];
						targetNode2 = numberedNodesInTargetGraph[targetN2];
						
						if (!ignoreEdgeDirection) {
							edge_exists_pn12 = neighboursOfPatN1.contains(patternNode2);
							edge_exists_pn21 = patNode2neighbour.get(patternNode2).contains(patternNode1);
							edge_exists_tn12 = neighboursOfTargetN1.contains(targetNode2);
							edge_exists_tn21 = node2neighbour.get(targetNode2).contains(targetNode1);
							
							if (edge_exists_pn12 != edge_exists_tn12 || edge_exists_pn21 != edge_exists_tn21) {
								compatibilityMatrix[patN1][targetN1] = false;
								break;
							} else
								if (edge_exists_pn12
													&& !compatibleEdgeExists(patternNode1, patternNode2,
																		targetNode1, targetNode2)) {
									compatibilityMatrix[patN1][targetN1] = false;
									break;
								} else
									if (edge_exists_pn21
														&& !compatibleEdgeExists(patternNode2, patternNode1,
																			targetNode2, targetNode1)) {
										compatibilityMatrix[patN1][targetN1] = false;
										break;
									}
						} else {
							edge_exists_pn12 = neighboursOfPatN1.contains(patternNode2);
							edge_exists_tn12 = neighboursOfTargetN1.contains(targetNode2);
							
							if (edge_exists_pn12 != edge_exists_tn12) {
								compatibilityMatrix[patN1][targetN1] = false;
								break;
							} else
								if (edge_exists_pn12
													&& !compatibleEdgeExists(patternNode1, patternNode2,
																		targetNode1, targetNode2)
													&& !compatibleEdgeExists(patternNode2, patternNode1,
																		targetNode1, targetNode2)
													&& !compatibleEdgeExists(patternNode1, patternNode2,
																		targetNode2, targetNode1)
															&& !compatibleEdgeExists(patternNode2, patternNode1,
																				targetNode2, targetNode1)) {
									compatibilityMatrix[patN1][targetN1] = false;
									break;
								}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Checks if the label of both nodes are compatible. We use a reg exp
	 * based comparison, see paper!
	 * 
	 * @param nodeOfPatternGraph
	 *           a node from the pattern graph
	 * @param nodeOfTargetGraph
	 *           a node from the target graph
	 * @return true, if the labels are compatible
	 */
	private boolean isCompatibleNode(Node nodeOfPatternGraph,
						Node nodeOfTargetGraph) {
		NodeLabelAttribute patternNodeLabel;
		NodeLabelAttribute targetNodeLabel;
		
		try {
			patternNodeLabel =
								(NodeLabelAttribute) nodeOfPatternGraph.getAttribute("label");
		} catch (AttributeNotFoundException e) {
			/* The pattern graph has no label, therefore everything is a match */
			return true;
		}
		
		try {
			targetNodeLabel =
								(NodeLabelAttribute) nodeOfTargetGraph.getAttribute("label");
		} catch (AttributeNotFoundException e) {
			targetNodeLabel = null;
		}
		
		String patternLabel = patternNodeLabel.getLabel();
		String targetLabel =
							targetNodeLabel != null
												? targetNodeLabel.getLabel()
												: "";
		
		Pattern patternRegExp = Pattern.compile(patternLabel);
		
		Matcher patternMatcher = patternRegExp.matcher(targetLabel);
		
		return patternMatcher.matches();
	}
	
	/**
	 * Checks the label compatibility of the edges between the given nodes. We
	 * use a reg exp based matching, see paper.
	 * 
	 * @param sourceNodeOfPatternGraph
	 *           DOCUMENT ME!
	 * @param targetNodeOfPatternGraph
	 *           DOCUMENT ME!
	 * @param sourceNodeOfTargetGraph
	 *           DOCUMENT ME!
	 * @param targetNodeOfTargetGraph
	 *           DOCUMENT ME!
	 * @return true, if the edges are compatible
	 */
	private boolean compatibleEdgeExists(Node sourceNodeOfPatternGraph,
						Node targetNodeOfPatternGraph,
						Node sourceNodeOfTargetGraph,
						Node targetNodeOfTargetGraph) {
		Collection<Edge> allEdgesFromPatternGraph = new HashSet<Edge>();
		allEdgesFromPatternGraph.addAll(sourceNodeOfPatternGraph.getEdges());
		allEdgesFromPatternGraph.addAll(targetNodeOfPatternGraph.getEdges());
		
		Collection<Edge> allEdgesFromTargetGraph = new HashSet<Edge>();
		
		allEdgesFromTargetGraph.addAll(sourceNodeOfTargetGraph.getEdges());
		allEdgesFromTargetGraph.addAll(targetNodeOfTargetGraph.getEdges());
		
		boolean compatible = true;
		boolean edgeFound = false;
		
		/* iterator over all PatternGraph X TargetGraph edges */
		for (Edge edgeFromPatternGraph : allEdgesFromPatternGraph) {
			
			if (edgeFromPatternGraph.getSource() == sourceNodeOfPatternGraph
								&& edgeFromPatternGraph.getTarget() == targetNodeOfPatternGraph) {
				/* This edge of the pattern graph has to be checked. */
				for (Edge edgeFromTargetGraph : allEdgesFromTargetGraph) {
					if (edgeFromTargetGraph.getSource() == sourceNodeOfTargetGraph
										&& edgeFromTargetGraph.getTarget() == targetNodeOfTargetGraph) {
						edgeFound = true;
						
						/* This edge of the target graph has to be checked, too */
						EdgeLabelAttribute labelFromPatternGraph;
						EdgeLabelAttribute labelFromTargetGraph;
						
						try {
							labelFromPatternGraph =
												(EdgeLabelAttribute) edgeFromPatternGraph
																	.getAttribute("label");
						} catch (AttributeNotFoundException e) {
							labelFromPatternGraph = null;
						}
						
						try {
							labelFromTargetGraph =
												(EdgeLabelAttribute) edgeFromTargetGraph
																	.getAttribute("label");
						} catch (AttributeNotFoundException e) {
							labelFromTargetGraph = null;
						}
						
						String patternLabel =
											labelFromPatternGraph != null
																? labelFromPatternGraph.getLabel()
																: ".*";
						String targetLabel =
											labelFromTargetGraph != null
																? labelFromTargetGraph.getLabel()
																: "";
						
						Pattern patternRegExp = Pattern.compile(patternLabel);
						
						Matcher patternMatcher =
											patternRegExp.matcher(targetLabel);
						
						compatible = compatible && patternMatcher.matches();
					}
				}
			}
		}
		
		return edgeFound && compatible;
	}
	
	/**
	 * Creates an array of the nodes of the given graph
	 * 
	 * @param g
	 *           a graph
	 * @return an array of nodes
	 */
	private Node[] encodeNodes(Graph g) {
		Node[] nodeArray = new Node[g.getNumberOfNodes()];
		
		int i = 0;
		Iterator<?> it1 = g.getNodesIterator();
		
		while (it1.hasNext()) {
			Node n = (Node) it1.next();
			
			nodeArray[i] = n;
			i++;
		}
		
		return nodeArray;
	}
	
	/**
	 * Extracts the required nodes from the given node array.
	 * 
	 * @param allNodesOfGraph
	 *           the array of all nodes of the graph
	 * @param resultNodeIds
	 *           the requested node ids
	 * @return an array of nodes
	 */
	private Node[] extractNodes(Node[] allNodesOfGraph, int[] resultNodeIds) {
		Node[] resultNodes = new Node[resultNodeIds.length];
		
		for (int i = 0; i < resultNodeIds.length; i++) {
			resultNodes[i] = allNodesOfGraph[resultNodeIds[i]];
		}
		
		return resultNodes;
	}
}
