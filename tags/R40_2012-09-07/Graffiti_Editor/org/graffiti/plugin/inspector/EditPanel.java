// ==============================================================================
//
// EditPanel.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EditPanel.java,v 1.1 2011-01-31 09:04:35 klukas Exp $

package org.graffiti.plugin.inspector;

import java.util.Collection;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.event.ListenerManager;

/**
 * Represents the edit panel in the inspector.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class EditPanel
					extends JPanel {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** a reference to the map between old and new graph elements */
	protected Map<?, ?> geMap;
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the map of editcomponents to the given map.
	 * 
	 * @param map
	 *           DOCUMENT ME!
	 */
	public abstract void setEditComponentMap(Map<?, ?> map);
	
	/**
	 * Sets the ListenerManager.
	 * 
	 * @param lm
	 *           DOCUMENT ME!
	 */
	public abstract void setListenerManager(ListenerManager lm);
	
	/**
	 * Builds the table that is used for editing attributes from scratch.
	 * 
	 * @param treeNode
	 *           root attribute.
	 * @param graphElements
	 *           DOCUMENT ME!
	 */
	public abstract void buildTable(DefaultMutableTreeNode treeNode,
						Collection<Attributable> graphElements, String tabName);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param attr
	 *           DOCUMENT ME!
	 */
	public abstract void updateTable(Attribute attr);
	
	/**
	 * Sets a reference to the map between old and new graph elements needed by
	 * edit panel for creation of undo edits.
	 * 
	 * @param geMap
	 *           a reference to the map between old and new graph elements
	 */
	public void setGraphElementMap(Map<?, ?> geMap) {
		this.geMap = geMap;
	}
	
	public abstract void showEmpty();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
