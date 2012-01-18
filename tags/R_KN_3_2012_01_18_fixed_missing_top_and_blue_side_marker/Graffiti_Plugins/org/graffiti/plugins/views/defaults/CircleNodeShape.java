// ==============================================================================
//
// CircleNodeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: CircleNodeShape.java,v 1.1 2011-01-31 09:03:29 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;

/**
 * Class representing a circle.
 */
public class CircleNodeShape
					extends CircularNodeShape {
	// ~ Constructors ===========================================================
	
	/**
	 * The constructor creates a circle using default values.
	 */
	public CircleNodeShape() {
		super();
		this.ell2D = new Ellipse2D.Double(0, 0, DEFAULT_WIDTH, DEFAULT_WIDTH);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Calculates the intersection between this shape and a line.
	 * 
	 * @param line
	 * @return the intersection point or null if shape and line do not
	 *         intersect.
	 */
	@Override
	public Point2D getIntersection(Line2D line) {
		Rectangle2D rect = getRealBounds2D();
		Ellipse2D realEll2D = new Ellipse2D.Double(rect.getX(), rect.getY(),
							rect.getWidth(), rect.getHeight());
		
		return getIntersectionWithCircle(realEll2D, line);
	}
	
	/**
	 * This method sets all necessary properties using the values contained
	 * within the <code>CollectionAttribute</code> (like size etc.).
	 * 
	 * @param nodeAttr
	 *           The attribute that contains all necessary information to
	 *           construct an circle.
	 */
	@Override
	public void buildShape(NodeGraphicAttribute nodeAttr) {
		this.nodeAttr = nodeAttr;
		
		DimensionAttribute dim = nodeAttr.getDimension();
		double r = dim.getWidth();
		
		if (r != dim.getHeight()) {
			// TODO set it to equal / inform / ...
		}
		
		double ft = Math.floor(nodeAttr.getFrameThickness());
		double offset = ft / 2d;
		this.ell2D.setFrame(offset, offset, r, r);
		
		double corr = r + ft;
		
		if (Math.floor(offset) == offset) {
			corr = r + ft + 1;
		}
		
		((RectangularShape) this.thickShape).setFrame(0, 0, corr, corr);
	}
	
	public int shapeHeightCorrection() {
		return 0;
	}
	
	public int shapeWidthCorrection() {
		return 0;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
