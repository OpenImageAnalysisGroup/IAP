// ==============================================================================
//
// ZoomListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ZoomListener.java,v 1.1 2011-01-31 09:04:24 klukas Exp $

package org.graffiti.plugin.view;

import java.awt.geom.AffineTransform;

/**
 * DOCUMENT ME!
 * 
 * @author Paul
 */
public interface ZoomListener {
	// ~ Methods ================================================================
	
	/**
	 * Indicates that a zoom value has changed.
	 */
	public void zoomChanged(AffineTransform newZoom);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
