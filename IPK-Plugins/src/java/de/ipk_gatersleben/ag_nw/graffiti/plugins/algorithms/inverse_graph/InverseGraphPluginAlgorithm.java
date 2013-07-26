/*******************************************************************************
 * Copyright (c) 2003-2007 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.inverse_graph;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author Christian Klukas
 */
public class InverseGraphPluginAlgorithm extends AbstractAlgorithm {
	
	public void execute() {
		Collection<GraphElement> ws = getSelectedOrAllGraphElements();
		Collection<Edge> edges = new ArrayList<Edge>();
		for (GraphElement ge : ws)
			if (ge instanceof Edge)
				edges.add((Edge) ge);
		GraphHelper.applyUndoableEdgeReversal(graph, edges, getName());
	}
	
	public String getName() {
		return "Reverse Edge Direction";
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
	@Override
	public String getCategory() {
		return "Edges";
	}
}
