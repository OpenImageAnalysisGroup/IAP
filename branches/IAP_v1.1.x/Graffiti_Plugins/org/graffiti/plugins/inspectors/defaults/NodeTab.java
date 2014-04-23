// ==============================================================================
//
// NodeTab.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeTab.java,v 1.1 2011-01-31 09:03:30 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import java.util.Collection;

import org.graffiti.selection.SelectionEvent;

/**
 * Represents the tab of the inspector, which edits the properties of a node.
 * 
 * @version $Revision: 1.1 $
 */
public class NodeTab
					extends AbstractTab {
	// ~ Constructors ===========================================================
	
	private static final long serialVersionUID = 1L;
	
	private static NodeTab instance = null;
	
	/**
	 * Constructs a <code>NodeTab</code> and sets the title.
	 */
	public NodeTab() {
		super();
		this.title = "Node";
		NodeTab.instance = this;
	}
	
	@Override
	public String getEmptyDescription() {
		return "Properties of node selection are editable at this place.";
	}
	
	public static NodeTab getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void selectionChanged(SelectionEvent e) {
		rebuildTree((Collection) e.getSelection().getNodes());
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
