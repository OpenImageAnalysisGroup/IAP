package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths;

import org.graffiti.graph.Edge;

public interface EdgeFollowingVetoEvaluation {
	public boolean followEdge(Edge e);
}
