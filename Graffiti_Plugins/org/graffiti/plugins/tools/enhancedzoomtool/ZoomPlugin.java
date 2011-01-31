// ==============================================================================
//
// ZoomPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ZoomPlugin.java,v 1.1 2011-01-31 09:03:39 klukas Exp $

package org.graffiti.plugins.tools.enhancedzoomtool;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.gui.GraffitiComponent;

/**
 * This plugin contains the standard editing tools.
 * 
 * @version $Revision: 1.1 $
 */
public class ZoomPlugin
					extends EditorPluginAdapter {
	// ~ Instance fields ========================================================
	
	/** The button for the zoom tool */
	private GraffitiComponent zoomButton;
	
	/** The <code>ImageBundle</code> of the main frame. */
	// private ImageBundle iBundle = ImageBundle.getInstance();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new StandardTools object.
	 */
	public ZoomPlugin() {
		zoomButton = new ZoomChangeComponent("defaultToolbar");
		guiComponents = new GraffitiComponent[1];
		guiComponents[0] = zoomButton;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
