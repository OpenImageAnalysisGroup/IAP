// ==============================================================================
//
// Zoomable.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Zoomable.java,v 1.1 2011-01-31 09:04:24 klukas Exp $

package org.graffiti.plugin.view;

import java.awt.geom.AffineTransform;

/**
 * DOCUMENT ME!
 * 
 * @author Paul
 */
public interface Zoomable {
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public AffineTransform getZoom();
	
	/**
	 * @return
	 */
	public boolean redrawActive();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
