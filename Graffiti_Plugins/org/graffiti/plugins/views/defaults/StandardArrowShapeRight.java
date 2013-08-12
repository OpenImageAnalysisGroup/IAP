// ==============================================================================
//
// StandardArrowShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StandardArrowShapeRight.java,v 1.1 2012-11-07 14:42:20 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 */
public class StandardArrowShapeRight
					extends AbstractArrowShape
					implements SupportsHollowDrawing {
	// ~ Constructors
	// ===========================================================
	
	boolean hollow = false;
	
	public StandardArrowShapeRight(float size) {
		super();
		updateSize(size);
	}
	
	@Override
	public void updateSize(double size) {
		super.updateSize(size);
		// arrow points horizontally to the right
		float ns = (float) size;
		if (hollow && lineWidth > 1)
			ns -= lineWidth;
		GeneralPath arrow = new GeneralPath();
		
		// if thickness == 1, fixed arrow size of 3 is used in VANTED
		float off = size != 1 ? lineWidth / 2f : (float) size / 3f / 2f;
		
		// correction factor needed because of partially discrete positioning
		off -= ((ns / 2f) - Math.floor(ns / 2f)) / lineWidth / 5f;
		
		arrow.moveTo(0f, ns / 2 - off);
		arrow.lineTo(0f, ns - off);
		arrow.lineTo(ns, ns / 2f - off);
		
		// arrow.lineTo(0f, 0f);
		arrow.closePath();
		
		this.head = new Point2D.Double(ns, ns / 2d);
		this.anchor = new Point2D.Double(0, ns / 2d);
		this.arrowShape = arrow;
		this.arrowWidth = ns;
	}
	
	/**
	 * Constructs a new arrow. Creates the <code>GeneralPath</code> representing the arrow and sets head and anchor.
	 */
	public StandardArrowShapeRight() {
		// this(10f);
	}
	
	public boolean isHollow() {
		return hollow;
	}
	
	public void setHollow(boolean h) {
		this.hollow = h;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
