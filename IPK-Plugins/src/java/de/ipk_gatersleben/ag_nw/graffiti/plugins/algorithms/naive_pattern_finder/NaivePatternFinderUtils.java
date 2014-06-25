/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;

/**
 * Utilities for the naive pattern finder package.
 * 
 * @author Dirk Kosch�tzki, Christian Klukas: extended to support edge direction ignoration
 */
class NaivePatternFinderUtils {
	
	/**
	 * Checks if in the given graph an edge between s and t exists.
	 * 
	 * @param g
	 *           the graph
	 * @param s
	 *           the source node
	 * @param t
	 *           the target node
	 * @param check
	 * @return true if an edge exists.
	 */
	static boolean checkIfEdgeExists(Node s, Node t, boolean ignoreEdgeDirection) {
		if (!ignoreEdgeDirection) {
			// Collection allEdges = g.getEdges(s, t);
			//
			// Iterator i = allEdges.iterator();
			//
			// while (i.hasNext()) {
			// Edge e = (Edge) i.next();
			//
			// if (e.getSource() == s && e.getTarget() == t) {
			// return true;
			// }
			// }
			//
			// return false;
			//
			
			return s.getOutNeighbors().contains(t);
		} else {
			return s.getNeighbors().contains(t);
		}
	}
	
	static Edge getUniqueDirectedEdge(Node s, Node t, boolean ignoreEdgeDirection) {
		if (!ignoreEdgeDirection) {
			// Edge resultEdge = null;
			// boolean edgeFound = false;
			// Collection allEdges = g.getEdges(s, t);
			//
			// Iterator i = allEdges.iterator();
			//
			// while (i.hasNext()) {
			// Edge e = (Edge) i.next();
			//
			// if (e.getSource() == s && e.getTarget() == t) {
			// assert (!edgeFound);
			// resultEdge = e;
			// edgeFound = true;
			// }
			// }
			//
			// return resultEdge;
			
			for (Edge e : s.getAllOutEdges()) {
				if (e.getTarget() == t)
					return e;
			}
		} else {
			for (Edge e : s.getEdges()) {
				if ((e.getSource() == t && e.getTarget() == s) || (e.getSource() == s && e.getTarget() == t))
					return e;
			}
		}
		return null;
	}
	
}
