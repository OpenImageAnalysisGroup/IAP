// ==============================================================================
//
// EdgeTab.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeTab.java,v 1.1 2011-01-31 09:03:30 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import java.util.Collection;

import org.graffiti.selection.SelectionEvent;

/**
 * Represents a tabulator in the inspector, which handles the properties of
 * edges.
 * 
 * @version $Revision: 1.1 $
 */
public class EdgeTab
					extends AbstractTab {
	// ~ Constructors ===========================================================
	
	private static final long serialVersionUID = 1L;
	private static EdgeTab instance = null;
	
	/**
	 * Constructs a <code>EdgeTab</code> and sets the title.
	 */
	public EdgeTab() {
		this.title = "Edge";
		EdgeTab.instance = this;
	}
	
	@Override
	public String getEmptyDescription() {
		return "Properties of active edge selection are editable at this place.";
	}
	
	public static EdgeTab getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void selectionChanged(SelectionEvent e) {
		rebuildTree((Collection) e.getSelection().getEdges());
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
