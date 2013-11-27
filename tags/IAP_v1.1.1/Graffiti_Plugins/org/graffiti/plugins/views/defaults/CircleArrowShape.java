/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org.graffiti.plugins.views.defaults;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class CircleArrowShape
					extends AbstractArrowShape
					implements SupportsHollowDrawing {
	private boolean hollow = false;
	
	public CircleArrowShape(float size) {
		super();
		updateSize(size);
	}
	
	@Override
	public void updateSize(double size) {
		super.updateSize(size);
		double ns = size;
		if (isHollow())
			ns -= lineWidth;
		GeneralPath arrow = new GeneralPath(new Ellipse2D.Double(ns * 1f / 6f, ns * 1f / 6f, ns * 4f / 6f, ns * 4f / 6f));
		
		this.head = new Point2D.Double(ns, ns / 2d);
		this.anchor = new Point2D.Double(ns / 5, ns / 2d);
		this.arrowShape = arrow;
		this.arrowWidth = ns; // this.arrowShape.getBounds2D().getHeight();
	}
	
	public CircleArrowShape() {
		// this(10f);
	}
	
	public boolean isHollow() {
		return hollow;
	}
	
	public void setHollow(boolean h) {
		this.hollow = h;
	}
}
