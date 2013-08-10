// ==============================================================================
//
// StandardArrowShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: TriggerArrowShape.java,v 1.2 2012-11-07 14:42:20 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.2 $
 */
public class TriggerArrowShape
					extends AbstractArrowShape
					implements SupportsHollowDrawing {
	// ~ Constructors
	// ===========================================================
	
	boolean hollow = true;
	
	public TriggerArrowShape(float size) {
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
		// original version
		// arrow.moveTo(-ns / 2, ns * 1.3f);
		// arrow.lineTo(-ns / 2, -ns * 0.3f);
		//
		// arrow.moveTo(0f, 0f);
		// arrow.lineTo(0f, ns);
		// arrow.lineTo(ns, ns / 2f);
		
		// new version to draw PowerPoint slides correctly
		arrow.moveTo(-ns / 2f, ns / 2f);
		arrow.lineTo(-ns / 2f, ns * 1.3f);
		arrow.moveTo(-ns / 2f, ns / 2f);
		arrow.lineTo(-ns / 2f, -ns * 0.3f);
		
		arrow.moveTo(0f, ns / 2f);
		arrow.lineTo(0f, 0f);
		arrow.lineTo(ns, ns / 2f);
		arrow.lineTo(0f, ns);
		arrow.lineTo(0f, ns / 2f);
		
		arrow.closePath();
		
		this.head = new Point2D.Double(ns * 1.1, ns / 2d);
		this.anchor = new Point2D.Double(0, ns / 2d);
		this.arrowShape = arrow;
		this.arrowWidth = ns; // this.arrowShape.getBounds2D().getHeight();
	}
	
	/**
	 * Constructs a new arrow. Creates the <code>GeneralPath</code> representing the arrow and sets head and anchor.
	 */
	public TriggerArrowShape() {
		// this(10f);
	}
	
	public boolean isHollow() {
		return hollow;
	}
	
	public void setHollow(boolean h) {
		// always "hollow"
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
