// ==============================================================================
//
// EditorAlgorithm.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EditorAlgorithm.java,v 1.1 2011-01-31 09:04:32 klukas Exp $

package org.graffiti.plugin.algorithm;

import javax.swing.ImageIcon;

import org.graffiti.editor.dialog.ParameterDialog;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;

/**
 *
 */
public interface EditorAlgorithm
					extends Algorithm {
	// ~ Methods ================================================================
	
	/**
	 * Returns a custom <code>ParameterDialog</code> if the algorithm wants to
	 * provide one. If this method returns null, a generic dialog will be
	 * generated using standard <code>EditComponent</code>s.
	 * 
	 * @return DOCUMENT ME!
	 */
	public ParameterDialog getParameterDialog(Selection sel);
	
	public boolean activeForView(View v);
	
	/**
	 * @return A short name, used for the parameter dialog window.
	 */
	public String getShortName();
	
	/**
	 * Returns the icon of the algorithm (used, e.g. in the menu bar).
	 * Icon is only shown if showMenuIcon returns true.
	 * 
	 * @return The icon of the algorithm or null if the plugin icon should be used.
	 */
	public ImageIcon getIcon();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
