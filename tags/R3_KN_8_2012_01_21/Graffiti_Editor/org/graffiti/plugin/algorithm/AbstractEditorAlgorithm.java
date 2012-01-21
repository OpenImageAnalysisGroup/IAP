// ==============================================================================
//
// AbstractEditorAlgorithm.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractEditorAlgorithm.java,v 1.1 2011-01-31 09:04:32 klukas Exp $

package org.graffiti.plugin.algorithm;

import javax.swing.ImageIcon;

import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterDialog;
import org.graffiti.selection.Selection;

public abstract class AbstractEditorAlgorithm
					extends AbstractAlgorithm
					implements EditorAlgorithm {
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.plugin.algorithm.EditorAlgorithm#getParameterDialog(org.graffiti.selection.Selection)
	 */
	public ParameterDialog getParameterDialog(Selection s) {
		return null;
	}
	
	protected MainFrame getMainFrame() {
		return MainFrame.getInstance();
	}
	
	public String getShortName() {
		return getName();
	}
	
	/**
	 * Returns the icon of the algorithm (used, e.g. in the menu bar).
	 * Icon is only shown if showMenuIcon returns true.
	 * 
	 * @return The icon of the algorithm or null if the plugin icon should be used.
	 */
	public ImageIcon getIcon() {
		return null;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
