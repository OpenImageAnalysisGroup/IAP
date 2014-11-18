/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import org.graffiti.graph.Node;

/**
 * Provides the entry point for the <code>Matcher</code>. The method
 * visitPattern is called from the matcher everytime a match was found.
 * 
 * @author Dirk Koschützki
 */
interface PatternVisitor {
	
	/**
	 * This method is invoked for every match that has been found by the
	 * matcher. If the function returns false, then the next match is
	 * searched; else the search process terminates.
	 * 
	 * @param numberOfNodesInMatch
	 *           the number of nodes in the match
	 * @param matchInPattern
	 *           the nodes from the pattern graph
	 * @param matchInTarget
	 *           the nodes from the target graph
	 * @param patternName
	 *           name of the pattern which was found
	 * @return true, if the search was "successful" and no more matches are
	 *         required
	 */
	boolean visitPattern(int numberOfNodesInMatch, Node[] matchInPattern,
						Node[] matchInTarget, String patternName);
}
