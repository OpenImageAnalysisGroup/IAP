// ==============================================================================
//
// GraffitiAbstractUndoableEdit.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiAbstractUndoableEdit.java,v 1.1 2011-01-31 09:05:05 klukas Exp $

package org.graffiti.undo;

import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;

import org.graffiti.core.StringBundle;
import org.graffiti.graph.GraphElement;

/**
 * GraffitiAbstractUndoableEdit
 * 
 * @version $Revision: 1.1 $
 */
public abstract class GraffitiAbstractUndoableEdit
					extends AbstractUndoableEdit {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The reference for the map between graph elements recreated after undo
	 * processing and original graph elements.
	 */
	protected Map<GraphElement, GraphElement> geMap;
	
	/** The <code>StringBundle</code> of the main frame. */
	protected StringBundle sBundle = StringBundle.getInstance();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new <code>GraffitiAbstractUndoableEdit</code> object.
	 * 
	 * @param geMap
	 *           reference to the map supports the undo operations.
	 */
	public GraffitiAbstractUndoableEdit(Map<GraphElement, GraphElement> geMap) {
		super();
		this.geMap = geMap;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Executes action for corresponding tools.
	 */
	public abstract void execute();
	
	/**
	 * Return a new graph element reference through the mapping from old ones.
	 * 
	 * @param oldGraphElement
	 *           a graph element has to be updated.
	 * @return a new existing graph element mapped from the given ones.
	 */
	protected GraphElement getNewGraphElement(GraphElement oldGraphElement) {
		if (geMap.get(oldGraphElement) != null)
			return geMap.get(oldGraphElement);
		else
			return oldGraphElement;
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
