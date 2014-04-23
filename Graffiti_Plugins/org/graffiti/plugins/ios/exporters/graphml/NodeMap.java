// ==============================================================================
//
// NodeMap.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeMap.java,v 1.1 2011-01-31 09:03:25 klukas Exp $

package org.graffiti.plugins.ios.exporters.graphml;

import java.util.HashMap;

import org.graffiti.graph.Node;

/**
 * This class provides a mapping from nodes to ids.
 * 
 * @author ruediger
 */
class NodeMap {
	// ~ Instance fields ========================================================
	
	/** Maps nodes to ids. */
	private HashMap<Node, Integer> map;
	
	/** Counter for the ids. */
	private int count;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>NodeMap</code>.
	 */
	NodeMap() {
		this.map = new HashMap<Node, Integer>();
		this.count = -1;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the id corresponding to the specified node.
	 * 
	 * @param n
	 *           the node of which to return the id.
	 * @return the id corresponding to the specified node.
	 */
	int getId(Node n) {
		Integer id = (Integer) this.map.get(n);
		
		return id.intValue();
	}
	
	/**
	 * Adds a new node to the mapping and assigns it a new id.
	 * 
	 * @param n
	 *           the node to be added.
	 * @return the id of the node which has been added to the mapping.
	 */
	int add(Node n) {
		this.map.put(n, new Integer(++count));
		
		return count;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
