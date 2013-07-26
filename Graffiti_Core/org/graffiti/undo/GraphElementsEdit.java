// ==============================================================================
//
// GraphElementsEdit.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphElementsEdit.java,v 1.1 2011-01-31 09:05:04 klukas Exp $

package org.graffiti.undo;

import java.util.Map;

import org.graffiti.graph.Graph;

/**
 * <code>GraphElementsEdit</code> is abstract class for building edits belong
 * to the operations on graph elements like adding or removing.
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:05:04 $
 */
public abstract class GraphElementsEdit extends GraffitiAbstractUndoableEdit {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Necessary graph reference */
	protected Graph graph;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Create a nes <code>GraphElementsEdit</code>.
	 * 
	 * @param graph
	 *           a graph reference
	 * @param geMap
	 *           reference to the map supports the undo operations.
	 */
	@SuppressWarnings("unchecked")
	public GraphElementsEdit(Graph graph, Map geMap) {
		super(geMap);
		assert graph != null;
		this.graph = graph;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
