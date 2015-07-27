/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org.graffiti.plugins.views.defaults;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class DiamondArrowShape extends AbstractArrowShape
					implements SupportsHollowDrawing {
	private boolean hollow;
	
	public DiamondArrowShape(float size) {
		super();
		updateSize(size);
	}
	
	@Override
	public void updateSize(double size) {
		float ns = (float) size;
		super.updateSize(ns);
		if (isHollow() && lineWidth > 1)
			ns -= lineWidth;
		
		GeneralPath arrow = new GeneralPath();
		/*
		 * if (isHollow()) {
		 * arrow.moveTo(ns / 2f, ns * 5f / 6f+lineWidth/2f);
		 * arrow.lineTo(ns*1f/6f, ns / 2f+lineWidth/2f);
		 * arrow.lineTo(ns / 2f, ns / 6f+lineWidth/2f);
		 * arrow.lineTo(ns*5f/6f, ns / 2f+lineWidth/2f);
		 * } else {
		 * arrow.moveTo(ns / 2f, ns * 5f / 6f);
		 * arrow.lineTo(ns*1f/6f, ns / 2f);
		 * arrow.lineTo(ns / 2f, ns / 6f);
		 * arrow.lineTo(ns*5f/6f, ns / 2f);
		 * }
		 */
		float offX = -ns;
		float offY = -ns / 2f;
		arrow.moveTo(offX + ns / 2f, offY + ns);
		arrow.lineTo(offX + 0f, offY + ns / 2f);
		arrow.lineTo(offX + ns / 2f, offY);
		arrow.lineTo(offX + ns, offY + ns / 2f);
		
		arrow.closePath();
		
		this.head = new Point2D.Double(ns / 4, 0); // ns / 2d);
		if (isHollow())
			this.anchor = new Point2D.Double(-ns, 0); // ns*1f/6f, ns / 2d);
		else
			this.anchor = new Point2D.Double(-ns / 2, 0); // ns / 2, ns / 2d);
		this.arrowShape = arrow;
		this.arrowWidth = ns / 1.1; // this.arrowShape.getBounds2D().getHeight();
	}
	
	public DiamondArrowShape() {
		// this(10f);
	}
	
	// @Override
	// public Rectangle2D getBounds2D() {
	// Rectangle2D res = super.getBounds2D();
	// res.add(res.getMinX()-lineWidth, res.getMinY()-lineWidth);
	// res.add(res.getMaxX()+lineWidth, res.getMaxY()+lineWidth);
	// return res;
	// }
	//
	// @Override
	// public Rectangle getBounds() {
	// return getBounds2D().getBounds();
	// }
	
	public void setHollow(boolean hollow) {
		this.hollow = hollow;
	}
	
	public boolean isHollow() {
		return hollow;
	}
}
