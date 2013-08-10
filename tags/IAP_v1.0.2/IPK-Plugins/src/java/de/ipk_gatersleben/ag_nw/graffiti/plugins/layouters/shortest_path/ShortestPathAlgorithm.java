/*******************************************************************************
 * Copyright (c) 2008 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.06.2003
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.shortest_path;

import java.util.Collection;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

/**
 * @author Christian Klukas
 */
public class ShortestPathAlgorithm extends AbstractAlgorithm {
	
	public void execute() {
		AreaGrid ag = new AreaGrid(graph);
		ag.claimNodeSpace();
		ag.setEdgeCrossingTravelCost(ag.getGridDiameter() / 5);
		ag.setEdgeCrossingTravelCost(ag.getGridDiameter() / 2);
		try {
			graph.getListenerManager().transactionStarted(this);
			for (Edge edge : graph.getEdges()) {
				AttributeHelper.removeEdgeBends(edge);
				Collection<Vector2d> points = ag.routeEdge(edge.getSource(), edge.getTarget());
				if (points != null && points.size() > 0)
					AttributeHelper.addEdgeBends(edge, points);
			}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	public String getName() {
		return null; // "SP Edge Routing";
	}
}
