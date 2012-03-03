/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org.graffiti.plugins.views.defaults;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class AbsoluteInhibitorArrowShape extends AbstractArrowShape {
	public AbsoluteInhibitorArrowShape(float size) {
		super();
		updateSize(size);
	}
	
	@Override
	public void updateSize(double size) {
		super.updateSize(size);
		
		float inhibitorLineWidth;
		
		GeneralPath arrow = new GeneralPath();
		
		if (SIZE >= 0.99999 && SIZE <= 1.00001) {
			SCALE_FACTOR = 6;
			inhibitorLineWidth = SIZE / SCALE_FACTOR * 1;
		} else {
			inhibitorLineWidth = lineWidth;
		}
		
		float inhibitorLineLength = SIZE;
		
		arrow.moveTo(0f, 0f);
		arrow.lineTo(0f, inhibitorLineLength);
		arrow.lineTo(inhibitorLineWidth, inhibitorLineLength);
		arrow.lineTo(inhibitorLineWidth, 0f);
		arrow.lineTo(0f, 0f);
		
		arrow.moveTo(-inhibitorLineWidth * 2f, -inhibitorLineLength * 0.1f);
		arrow.lineTo(-inhibitorLineWidth * 2f, inhibitorLineLength * 1.1f);
		arrow.lineTo(-inhibitorLineWidth * 2f + inhibitorLineWidth, inhibitorLineLength * 1.1f);
		arrow.lineTo(-inhibitorLineWidth * 2f + inhibitorLineWidth, -inhibitorLineLength * 0.1f);
		
		arrow.closePath();
		
		this.head = new Point2D.Double(SIZE / 2, SIZE / 2d);
		this.anchor = new Point2D.Double(0, SIZE / 2d);
		this.arrowShape = arrow;
		this.arrowWidth = this.arrowShape.getBounds2D().getHeight();
	}
	
	public AbsoluteInhibitorArrowShape() {
		// this(10f);
	}
}