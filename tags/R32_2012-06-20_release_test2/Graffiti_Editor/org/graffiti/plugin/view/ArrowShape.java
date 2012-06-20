// ==============================================================================
//
// ArrowShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ArrowShape.java,v 1.1 2011-01-31 09:04:25 klukas Exp $

package org.graffiti.plugin.view;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.1 $
 */
public interface ArrowShape extends Shape {
	// ~ Methods
	// ================================================================
	
	/**
	 * Returns the anchor of the arrow, i.e. the point where the line should be
	 * attached to the arrow. This is only valid after a call to <code>affix</code>.
	 * 
	 * @return the anchor of the arrow.
	 */
	public Point2D getAnchor();
	
	/**
	 * Sets this arrow to the target point and rotates it according to the line
	 * given by the connection between points target and other.
	 * 
	 * @param target
	 *           the point where the arrow should be put
	 * @param other
	 *           needed to calculate the direction in which the arrow should
	 *           point to. The line is given by the two points target and
	 *           other.
	 * @param lineWidth
	 *           The total width of the line. May be used to scale the arrow so
	 *           as to be larger than the line.
	 * @return DOCUMENT ME!
	 */
	public Shape affix(Point2D target, Point2D other, double lineWidth);
	
	public void updateSize(double sz);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
