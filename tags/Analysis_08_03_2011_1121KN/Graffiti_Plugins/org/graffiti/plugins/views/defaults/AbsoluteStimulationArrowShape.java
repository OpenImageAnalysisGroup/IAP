/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package org.graffiti.plugins.views.defaults;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 */
public class AbsoluteStimulationArrowShape
					extends AbstractArrowShape
					implements SupportsHollowDrawing {
	// ~ Constructors
	// ===========================================================
	
	boolean hollow = false;
	
	public AbsoluteStimulationArrowShape(float size) {
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
		arrow.moveTo(0f, ns / 2f);
		arrow.lineTo(0f, ns);
		arrow.lineTo(ns, ns / 2f);
		arrow.lineTo(0f, 0f);
		arrow.lineTo(0f, ns / 2f);
		
		float off = -ns;
		arrow.moveTo(off + 0f, 0f);
		arrow.lineTo(off + 0f, ns);
		arrow.lineTo(off + ns, ns / 2f);
		arrow.lineTo(off + 0f, 0f);
		
		// arrow.lineTo(0f, 0f);
		arrow.closePath();
		
		this.head = new Point2D.Double(ns, ns / 2d);
		this.anchor = new Point2D.Double(-ns, ns / 2d);
		this.arrowShape = arrow;
		this.arrowWidth = ns; // this.arrowShape.getBounds2D().getHeight();
	}
	
	/**
	 * Constructs a new arrow. Creates the <code>GeneralPath</code> representing the arrow and sets head and anchor.
	 */
	public AbsoluteStimulationArrowShape() {
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
