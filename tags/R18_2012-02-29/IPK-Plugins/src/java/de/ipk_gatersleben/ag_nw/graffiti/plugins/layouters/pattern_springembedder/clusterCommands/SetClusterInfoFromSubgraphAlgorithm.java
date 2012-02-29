/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author Christian Klukas
 */
public class SetClusterInfoFromSubgraphAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "<html>" +
								"Detect connected subgraphs and set<br>" +
								"cluster ID to differentiate subgraphs";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"About to use the information about connected subgraphs for the assignment of a cluster ID.<br>";
	}
	
	@Override
	public String getCategory() {
		return "Elements"; // "menu.edit";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getNumberOfNodes() <= 0)
			throw new PreconditionException("Graph contains no graph elements!");
	}
	
	public void execute() {
		HashMap<GraphElement, String> ge2newClusterID = new HashMap<GraphElement, String>();
		int subgraphIndex = 1;
		graph.numberGraphElements();
		HashSet<GraphElement> processedSubgraphElements = new HashSet<GraphElement>();
		HashSet<GraphElement> validElements = new HashSet<GraphElement>(getSelectedOrAllGraphElements());
		for (GraphElement ge : validElements) {
			if (!processedSubgraphElements.contains(ge)) {
				if (ge instanceof Node) {
					Set<Node> connectedNodes = GraphHelper.getConnectedNodes((Node) ge);
					for (Node n : connectedNodes) {
						processedSubgraphElements.add(n);
						if (validElements.contains(n))
							ge2newClusterID.put(n, "" + subgraphIndex);
						for (Edge e : n.getEdges()) {
							if (!processedSubgraphElements.contains(e) && validElements.contains(e)) {
								ge2newClusterID.put(e, "" + subgraphIndex);
								processedSubgraphElements.add(e);
							}
						}
					}
					subgraphIndex++;
					System.out.println("Subgraph index: " + subgraphIndex + ", number of nodes: " + connectedNodes.size());
				}
			}
		}
		GraphHelper.applyUndoableClusterIdAssignment(graph, ge2newClusterID, getName(), true);
		MainFrame.showMessage(subgraphIndex + " subgraphs found, " + ge2newClusterID.size() + " nodes and edges elements processed", MessageType.INFO);
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}