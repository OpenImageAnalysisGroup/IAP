/*
 * ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jfreechart/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ---------------
 * PlumNeedle.java
 * ---------------
 * (C) Copyright 2002, 2003, by the Australian Antarctic Division and Contributors.
 * Original Author: Bryan Scott (for the Australian Antarctic Division);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: PlumNeedle.java,v 1.1 2011-01-31 09:02:42 klukas Exp $
 * Changes:
 * --------
 * 25-Sep-2002 : Version 1, contributed by Bryan Scott (DG);
 * 27-Mar-2003 : Implemented Serializable (DG);
 * 09-Sep-2003 : Added equals(...) method (DG);
 */

package org.jfree.chart.needle;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * A needle for use with the {@link org.jfree.chart.plot.CompassPlot} class.
 * 
 * @author Bryan Scott
 */
public class PlumNeedle extends MeterNeedle implements Serializable {

	/**
	 * Draws the needle.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the plot area.
	 * @param rotate
	 *           the rotation point.
	 * @param angle
	 *           the angle.
	 */
	protected void drawNeedle(Graphics2D g2, Rectangle2D plotArea, Point2D rotate, double angle) {

		Arc2D shape = new Arc2D.Double(Arc2D.PIE);
		double radius = plotArea.getHeight();
		double halfX = plotArea.getWidth() / 2;
		double diameter = 2 * radius;

		shape.setFrame(plotArea.getMinX() + halfX - radius,
								plotArea.getMinY() - radius,
								diameter, diameter);
		radius = Math.toDegrees(Math.asin(halfX / radius));
		shape.setAngleStart(270 - radius);
		shape.setAngleExtent(2 * radius);

		Area s = new Area(shape);

		if ((rotate != null) && (angle != 0)) {
			// / we have rotation houston, please spin me
			getTransform().setToRotation(angle, rotate.getX(), rotate.getY());
			s.transform(getTransform());
		}

		defaultDisplay(g2, s);
	}

	/**
	 * Tests another object for equality with this object.
	 * 
	 * @param object
	 *           the object to test.
	 * @return A boolean.
	 */
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		if (super.equals(object) && object instanceof PlumNeedle) {
			return true;
		}
		return false;
	}

}
