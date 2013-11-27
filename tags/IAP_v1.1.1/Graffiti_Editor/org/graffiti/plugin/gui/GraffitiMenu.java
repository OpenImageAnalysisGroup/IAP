// ==============================================================================
//
// GraffitiMenu.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiMenu.java,v 1.1 2011-01-31 09:04:32 klukas Exp $

package org.graffiti.plugin.gui;

import javax.swing.JMenu;

import org.graffiti.editor.MainFrame;

/**
 * DOCUMENT ME!
 */
public class GraffitiMenu
					extends JMenu
					implements GraffitiComponent {
	// ~ Methods ================================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#setMainFrame(org.graffiti.editor.MainFrame)
	 */
	public void setMainFrame(MainFrame mf) {
	}
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return "menu";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
