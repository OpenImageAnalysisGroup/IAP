/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.copy_pattern_layout;

import java.util.ArrayList;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.NodeCacheEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;

public class CopyPatternLayoutAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.ThreadSafeAlgorithm#getName()
	 */
	public String getName() {
		return "Apply Search-Subgraph Layout";
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	/**
	 * Vecor node array, contains <code>patternNodeStruct</code> objects,
	 * which saves/caches the information about pattern type and number for
	 * all nodes of a graph
	 */
	private ArrayList<NodeCacheEntry> nodeArray;
	
	private HashMap<Node, NodeCacheEntry> nodeSearch;
	
	/**
	 * The pattern algorithm asumes that the nodes of a pattern are layouted
	 * acording to the layout of the pattern. The patterns are not layouted
	 * again, only the position of the whole pattern is modified.
	 * 
	 * @param nodeStructArray
	 *           List contains entries of type <code>NodeCacheEntry</code>
	 */
	private void doInitalLayoutOfPattern(ArrayList<NodeCacheEntry> nodeStructArray) {
		HashMap<NodeCacheEntry, Vector2d> nce2pos = new HashMap<NodeCacheEntry, Vector2d>();
		for (int i = 0; i < nodeStructArray.size(); i++) {
			NodeCacheEntry currentNodeInfo =
								nodeStructArray.get(i);
			if (!currentNodeInfo.patternTypeEmpty) {
				ArrayList<NodeCacheEntry> connectedNodes = PatternSpringembedder.getPatternNodesPublic(nodeStructArray, currentNodeInfo);
				if (connectedNodes.size() > 0) {
					double xsum = 0;
					double ysum = 0;
					for (NodeCacheEntry nce : connectedNodes) {
						xsum += nce.position.x;
						ysum += nce.position.y;
					}
					Vector2d midPoint = new Vector2d(xsum / connectedNodes.size(), ysum / connectedNodes.size());
					nce2pos.put(currentNodeInfo, midPoint);
				}
			}
		}
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		for (int i = 0; i < nodeStructArray.size(); i++) {
			NodeCacheEntry currentNodeInfo =
								nodeStructArray.get(i);
			if (!currentNodeInfo.patternTypeEmpty) {
				double patternPosX;
				double patternPosY;
				
				patternPosX = AttributeHelper.getPositionX(currentNodeInfo.patternNode);
				patternPosY = AttributeHelper.getPositionY(currentNodeInfo.patternNode);
				
				Vector2d patternAvg = NodeTools.getCenter(currentNodeInfo.patternNode.getGraph().getNodes());
				Vector2d midPatternStruct = nce2pos.get(currentNodeInfo);
				if (patternAvg != null && midPatternStruct != null) {
					patternPosX = patternPosX + midPatternStruct.x - patternAvg.x;
					patternPosY = patternPosY + midPatternStruct.y - patternAvg.y;
				}
				nodes2newPositions.put(currentNodeInfo.node, new Vector2d(patternPosX, patternPosY));
			} // if patternType<>null
		} // for all nodes
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());
	} // doInitialLayout
	
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("The graph instance may not be null.");
		}
		
		ArrayList<Graph> listOfPatterns = GravistoService.getInstance()
							.getPatternGraphs();
		
		if (listOfPatterns == null || listOfPatterns.size() == 0) {
			errors.add("The list of patterns is empty. Use tab \"Tools\"&rarr;\"Search Subgraph\" to create some.");
		} else {
			/* Just to make sure we use the correct graph... */
			for (Graph gtest : listOfPatterns) {
				if (graph == gtest) {
					errors.add("Please select the left frame.");
				}
			}
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	public void execute() {
		nodeArray = new ArrayList<NodeCacheEntry>();
		nodeSearch = new HashMap<Node, NodeCacheEntry>();
		MyTools.initNodeCache(nodeArray, nodeSearch, graph, null,
							GravistoService.getInstance().getPatternGraphs());
		doInitalLayoutOfPattern(nodeArray);
	}
	
	@Override
	public String toString() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#getGraph()
	 */
	public Graph getGraph() {
		return graph;
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
}