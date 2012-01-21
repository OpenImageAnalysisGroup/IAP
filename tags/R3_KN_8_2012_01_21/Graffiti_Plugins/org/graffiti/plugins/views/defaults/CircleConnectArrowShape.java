/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org.graffiti.plugins.views.defaults;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class CircleConnectArrowShape
					extends AbstractArrowShape {
	public CircleConnectArrowShape(float size) {
		super();
		updateSize(size);
	}
	
	@Override
	public void updateSize(double size) {
		super.updateSize(size);
		GeneralPath arrow = new GeneralPath(new Ellipse2D.Double(
							SIZE * 1f / 6f, SIZE * 1f / 6f, SIZE * 4f / 6f, SIZE * 4f / 6f));
		// arrow.moveTo(SIZE/2f, SIZE*5f/6f);
		// arrow.lineTo(0f, SIZE/2f);
		// arrow.lineTo(SIZE/2f, SIZE/6f);
		// arrow.lineTo(SIZE, SIZE/2f);
		// arrow.closePath();
		
		this.head = new Point2D.Double(SIZE / 2.5, SIZE / 2d);
		this.anchor = new Point2D.Double(SIZE / 2.5, SIZE / 2d);
		this.arrowShape = arrow;
		this.arrowWidth = SIZE; // this.arrowShape.getBounds2D().getHeight();
	}
	
	public CircleConnectArrowShape() {
		// this(10f);
	}
}
