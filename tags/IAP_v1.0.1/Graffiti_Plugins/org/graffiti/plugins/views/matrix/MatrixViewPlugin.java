// ==============================================================================
//
// MatrixViewPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MatrixViewPlugin.java,v 1.1 2011-01-31 09:03:37 klukas Exp $

package org.graffiti.plugins.views.matrix;

import org.graffiti.plugin.EditorPluginAdapter;

/**
 * Provides a matrix view implementation.
 * 
 * @version $Revision: 1.1 $
 */
public class MatrixViewPlugin
					extends EditorPluginAdapter {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new matrix view plugin.
	 */
	public MatrixViewPlugin() {
		this.views = new String[1];
		this.views[0] = "org.graffiti.plugins.views.matrix.MatrixView";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
