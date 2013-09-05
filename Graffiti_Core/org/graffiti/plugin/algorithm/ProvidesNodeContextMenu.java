// ==============================================================================
//
// AlgorithmWithContextMenu.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ProvidesNodeContextMenu.java,v 1.1 2011-01-31 09:04:44 klukas Exp $

package org.graffiti.plugin.algorithm;

import java.util.Collection;

import javax.swing.JMenuItem;

import org.graffiti.graph.Node;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.1 $
 */
public interface ProvidesNodeContextMenu {
	// ~ Methods ================================================================
	
	/**
	 * This method should be implemented, as that it returns the desired
	 * Context-MenuItem for the Plugin. It will be added on the fly to a newly
	 * created context menu, when the user right-clicks an EditorFrame. The
	 * plugin should implement the Interface <code>SelectionListener</code> if
	 * the menu item should be variable to the current selection. You could
	 * also return a MenuItem that contains a subMenu.
	 * 
	 * @return <code>MenuItem</code> the menu item for the context menu
	 */
	public JMenuItem[] getCurrentNodeContextMenuItem(Collection<Node> selectedNodes);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
