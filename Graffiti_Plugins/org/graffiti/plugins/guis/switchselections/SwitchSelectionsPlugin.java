// ==============================================================================
//
// SwitchSelectionsPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SwitchSelectionsPlugin.java,v 1.1 2011-01-31 09:03:38 klukas Exp $

package org.graffiti.plugins.guis.switchselections;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;

/**
 * Provides a spring embedder algorithm a la KK.
 * 
 * @version $Revision: 1.1 $
 */
public class SwitchSelectionsPlugin
					extends EditorPluginAdapter
					implements SelectionListener {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	SelectionMenu selMenu = new SelectionMenu();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new TrivialGridRestrictedPlugin object.
	 */
	public SwitchSelectionsPlugin() {
		this.guiComponents = new GraffitiComponent[1];
		this.guiComponents[0] = selMenu;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.GenericPluginAdapter#isSelectionListener()
	 */
	@Override
	public boolean isSelectionListener() {
		return true;
	}
	
	/**
	 * @see org.graffiti.selection.SelectionListener#selectionChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionChanged(SelectionEvent e) {
		((SelectionListener) selMenu).selectionChanged(e);
	}
	
	/**
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionListChanged(SelectionEvent e) {
		((SelectionListener) selMenu).selectionListChanged(e);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
