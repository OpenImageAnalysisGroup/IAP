/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

/**
 * Finds pattern within a given target graph. This algorithm provides the <code>Matcher</code> functionalities for Graffiti.
 * 
 * @author Dirk Kosch�tzki, Christian Klukas: extended to support undirected graphs and to optionally ignore the edge direction
 */
public class NaivePatternFinderAlgorithm
					extends AbstractAlgorithm {
	/*************************************************************/
	/* Member variables */
	/*************************************************************/
	
	/**
	 * All patterns defined within Graffiti.
	 */
	private ArrayList<Graph> listOfPatterns;
	
	private boolean ignoreEdgeDirection = false;
	
	/**
	 * Creates a new NaivePatternFinderAlgorithm object.
	 */
	public NaivePatternFinderAlgorithm() {
		super();
	}
	
	/*************************************************************/
	/* Declarations of methods */
	/*************************************************************/
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name
	 */
	public String getName() {
		return "Perform Subgraph-Search";
	}
	
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	/**
	 * Checks the preconditions of the algorithm. These are: non empty target
	 * graph, directed target graph, no multiple edges in the target graph,
	 * none empty list of pattern, all pattern must be directed and the
	 * "left" internal frame must be active.
	 * 
	 * @throws PreconditionException
	 *            if a precondition is violated.
	 */
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("The graph instance may not be null.");
		}
		
		if (!graph.isDirected() && !ignoreEdgeDirection) {
			errors.add("The graph must be directed.");
		}
		
		if (hasMultiEdgeDirectedGraph(graph)) {
			errors.add("The graph shall not have multiple edges.");
		}
		
		listOfPatterns = GravistoService.getInstance().getPatternGraphs();
		
		if (listOfPatterns == null || listOfPatterns.size() == 0) {
			errors.add("The list of patterns is empty.");
		} else {
			/* Just to make sure we use the correct graph... */
			for (Graph gtest : listOfPatterns) {
				if (graph == gtest) {
					errors.add("Please select a graph editor frame at the left.");
				}
			}
			Iterator<Graph> allPatternsIterator = listOfPatterns.iterator();
			if (!ignoreEdgeDirection)
				while (allPatternsIterator.hasNext()) {
					Graph aPattern = (Graph) allPatternsIterator.next();
					
					if (!aPattern.isDirected()) {
						errors.add("All patterns have to be directed graphs.");
					}
				}
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	/**
	 * Performs the matching of the target graph with all pattern graphs.
	 */
	public void execute() {
		System.err.println("There are " + listOfPatterns.size()
							+ " patterns in the list.");
		
		Iterator<Graph> i = listOfPatterns.iterator();
		int j = 1;
		
		while (i.hasNext()) {
			Graph currentPattern = (Graph) i.next();
			
			System.err.println("This pattern has "
								+ currentPattern.getNumberOfNodes()
								+ " nodes and "
								+ currentPattern.getNumberOfEdges() + " edges.");
			
			if (currentPattern.getNumberOfNodes() == 0) {
				System.err.println("Pattern has no nodes, skipping...");
				continue;
			}
			
			State state = new UllmannSubgraphIsomState(currentPattern, graph, ignoreEdgeDirection, null);
			Matcher matcher = new Matcher();
			PatternVisitor pv = new MarkingPatternVisitor(ignoreEdgeDirection);
			
			matcher.match(state, pv, null, null, "Pattern_" + j);
			j++;
		}
		MainFrame.showMessage("Pattern finder finished", MessageType.INFO);
	}
	
	/**
	 * Search a number of pattern graphs in the target graph, beginning with the largest pattern
	 * (determined by number of nodes)
	 * 
	 * @param graph
	 * @param patterns
	 * @param ignoreEdgeDirection
	 */
	public static void searchPatterns(
						Graph graph,
						List<Graph> patterns,
						Algorithm optLayoutAlgorithm,
						boolean ignoreEdgeDirection,
						final boolean startWithLargestCircle,
						BackgroundTaskStatusProviderSupportingExternalCall status) {
		System.err.println("There are " + patterns.size() + " patterns in the list.");
		
		int j = 1;
		Collections.sort(patterns, new Comparator<Graph>() {
			public int compare(Graph g0, Graph g1) {
				if (startWithLargestCircle) {
					if (g0.getNumberOfNodes() < g1.getNumberOfNodes())
						return 1;
					if (g0.getNumberOfNodes() > g1.getNumberOfNodes())
						return -1;
				} else {
					if (g0.getNumberOfNodes() < g1.getNumberOfNodes())
						return -1;
					if (g0.getNumberOfNodes() > g1.getNumberOfNodes())
						return 1;
					
				}
				return 0;
			}
		});
		int i = 0;
		int maxI = patterns.size();
		HashSet<Node> resultNodes = new HashSet<Node>();
		for (Graph currentPattern : patterns) {
			if (status.wantsToStop())
				break;
			status.setCurrentStatusValueFine(i / (double) maxI * 100d);
			status.setCurrentStatusText1("Process pattern " + processName(currentPattern.getName()));
			status.setCurrentStatusText2("Size: " + currentPattern.getNumberOfNodes()
								+ " nodes and "
								+ currentPattern.getNumberOfEdges() + " edges");
			i++;
			System.err.println("This pattern has "
								+ currentPattern.getNumberOfNodes()
								+ " nodes and "
								+ currentPattern.getNumberOfEdges() + " edges.");
			
			if (currentPattern.getNumberOfNodes() == 0) {
				System.err.println("Pattern has no nodes, skipping...");
				continue;
			}
			
			State state = new UllmannSubgraphIsomState(currentPattern, graph, ignoreEdgeDirection, resultNodes);
			Matcher matcher = new Matcher();
			PatternVisitor pv = new MarkingPatternVisitor(ignoreEdgeDirection);
			
			PatternVisitor layoutVisitor = null;
			
			if (optLayoutAlgorithm != null) {
				layoutVisitor = new PatternVistorLayouter(optLayoutAlgorithm);
			} else
				layoutVisitor = new SelectResultsVisitor();
			
			matcher.match(state, pv, layoutVisitor, resultNodes, "Pattern_" + processName(currentPattern.getName() + "_" + j));
			j++;
		}
		status.setCurrentStatusValueFine(100d);
		MainFrame.showMessage("Pattern finder finished", MessageType.INFO);
	}
	
	private static String processName(String name) {
		if (name == null)
			return null;
		else
			return StringManipulationTools.stringReplace(name, "*", "");
	}
	
	/**
	 * Checks if the given graph has multiple edges.
	 * 
	 * @param graph
	 *           the graph to check
	 * @return true, if the graph has multiple edges between any two nodes
	 */
	private boolean hasMultiEdgeDirectedGraph(Graph graph) {
		Iterator<?> allNodesIt = graph.getNodesIterator();
		
		while (allNodesIt.hasNext()) {
			Node aSourceNode = (Node) allNodesIt.next();
			
			Iterator<?> descendantNodes = aSourceNode.getOutNeighborsIterator();
			
			while (descendantNodes.hasNext()) {
				Node aTargetNode = (Node) descendantNodes.next();
				
				/*
				 * There is no method to check only for directed edges between a and b.
				 * therefore we have to do it manually.
				 */
				Collection<?> relevantEdges =
									graph.getEdges(aSourceNode, aTargetNode);
				Iterator<?> relevantEdgesIterator = relevantEdges.iterator();
				boolean edgeSeenBefore = false;
				
				while (relevantEdgesIterator.hasNext()) {
					Edge anEdge = (Edge) (relevantEdgesIterator.next());
					
					if ((anEdge.getSource() == aSourceNode)
										&& (anEdge.getTarget() == aTargetNode)) {
						if (edgeSeenBefore) {
							return true;
						} else {
							edgeSeenBefore = true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public void setIgnoreEdgeDirection(boolean ignoreEdgeDirection) {
		this.ignoreEdgeDirection = ignoreEdgeDirection;
	}
}
