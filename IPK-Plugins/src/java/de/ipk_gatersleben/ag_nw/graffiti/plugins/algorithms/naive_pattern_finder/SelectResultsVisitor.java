/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class SelectResultsVisitor implements PatternVisitor {
	
	public boolean visitPattern(int numberOfNodesInMatch,
						Node[] matchInPattern, Node[] matchInTarget, String patternName) {
		for (Node n : matchInTarget)
			GraphHelper.selectGraphElement(n);
		return false;
	}
	
}
