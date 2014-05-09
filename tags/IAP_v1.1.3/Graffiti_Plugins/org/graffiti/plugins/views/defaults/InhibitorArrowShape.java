/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org.graffiti.plugins.views.defaults;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class InhibitorArrowShape extends AbstractArrowShape {
	public InhibitorArrowShape(float size) {
		super();
		updateSize(size);
	}
	
	@Override
	public void updateSize(double size) {
		super.updateSize(size);
		
		GeneralPath arrow = new GeneralPath();
		if (SIZE >= 0.99999 && SIZE <= 1.00001) {
			SCALE_FACTOR = 6;
			arrow.moveTo(0f, 0);
			arrow.lineTo(0f, SIZE);
			arrow.lineTo(SIZE / SCALE_FACTOR * 0.5f, SIZE);
			arrow.lineTo(SIZE / SCALE_FACTOR * 0.5f, 0);
		} else {
			arrow.moveTo(0f, 0f);
			arrow.lineTo(0f, SIZE);
			arrow.lineTo(lineWidth, SIZE);
			arrow.lineTo(lineWidth, 0);
		}
		// arrow.lineTo(width, 0);
		// arrow.moveTo(0f, 0f);
		// arrow.lineTo(0f, SIZE);
		arrow.closePath();
		
		this.head = new Point2D.Double(SIZE / 2, SIZE / 2d);
		this.anchor = new Point2D.Double(0, SIZE / 2d);
		this.arrowShape = arrow;
		this.arrowWidth = this.arrowShape.getBounds2D().getHeight();
	}
	
	public InhibitorArrowShape() {
		// this(10f);
	}
}