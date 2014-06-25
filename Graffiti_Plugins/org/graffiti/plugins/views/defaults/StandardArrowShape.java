// ==============================================================================
//
// StandardArrowShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StandardArrowShape.java,v 1.1 2011-01-31 09:03:29 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 */
public class StandardArrowShape
					extends AbstractArrowShape
					implements SupportsHollowDrawing {
	// ~ Constructors
	// ===========================================================
	
	boolean hollow = false;
	
	public StandardArrowShape(float size) {
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
		arrow.moveTo(0f, 0f);
		arrow.lineTo(0f, ns);
		arrow.lineTo(ns, ns / 2f);
		
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
	public StandardArrowShape() {
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
