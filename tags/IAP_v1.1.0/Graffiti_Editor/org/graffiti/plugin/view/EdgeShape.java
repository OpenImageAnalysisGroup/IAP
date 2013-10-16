// ==============================================================================
//
// EdgeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeShape.java,v 1.1 2011-01-31 09:04:24 klukas Exp $

package org.graffiti.plugin.view;

import java.awt.Shape;

import org.graffiti.graphics.EdgeGraphicAttribute;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.1 $
 */
public interface EdgeShape
					extends GraphElementShape {
	// ~ Static fields/initializers =============================================
	
	/**
	 * The allowed distance of a mouse click from an edge where the edge is
	 * still selected.
	 */
	public static final double CLICK_TOLERANCE = 3.0d;
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the shape representing the arrow at the target node.
	 * 
	 * @return the shape representing the arrow at the target node.
	 */
	public Shape getHeadArrow();
	
	// /**
	// * Returns the <code>CoordinateAttribute</code> that represents the bend
	// * that is near the coordinates <code>x</code>, <code>y</code>.
	// * It returns null if no bend is near.
	// *
	// * @param x x coordinate relative to the coordinates of this shape.
	// * @param y y coordinate relative to the coordinates of this shape.
	// *
	// * @return the <code>CoordinateAttribute</code> of the bend hit or null if
	// * no bend was hit.
	// */
	// public CoordinateAttribute bendHit(double x, double y);
	
	/**
	 * Returns the shape representing the arrow at the source node.
	 * 
	 * @return the shape representing the arrow at the source node.
	 */
	public Shape getTailArrow();
	
	/**
	 * Called when one of the nodes belonging to this edge has changed.
	 * 
	 * @param graphics
	 * @param source
	 * @param target
	 */
	public void buildShape(EdgeGraphicAttribute graphics, NodeShape source,
						NodeShape target)
						throws ShapeNotFoundException;
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
