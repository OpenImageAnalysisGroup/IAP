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
 * -------------------------
 * XYDrawableAnnotation.java
 * -------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XYDrawableAnnotation.java,v 1.1 2011-01-31 09:02:51 klukas Exp $
 * Changes:
 * --------
 * 21-May-2003 : Version 1 (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 */

package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Drawable;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtils;

/**
 * A general annotation that can be placed on an {@link org.jfree.chart.plot.XYPlot}.
 */
public class XYDrawableAnnotation implements XYAnnotation, Cloneable, Serializable {

	/** The x-coordinate. */
	private double x;

	/** The y-coordinate. */
	private double y;

	/** The width. */
	private double width;

	/** The height. */
	private double height;

	/** The drawable object. */
	private Drawable drawable;

	/**
	 * Creates a new annotation to be displayed within the given area.
	 * 
	 * @param x
	 *           the x-coordinate for the area.
	 * @param y
	 *           the y-coordinate for the area.
	 * @param width
	 *           the width of the area.
	 * @param height
	 *           the height of the area.
	 * @param drawable
	 *           the drawable object.
	 */
	public XYDrawableAnnotation(double x, double y, double width, double height,
											Drawable drawable) {

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.drawable = drawable;

	}

	/**
	 * Draws the annotation.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param dataArea
	 *           the data area.
	 * @param domainAxis
	 *           the domain axis.
	 * @param rangeAxis
	 *           the range axis.
	 */
	public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
							ValueAxis domainAxis, ValueAxis rangeAxis) {

		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(),
																						orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(),
																						orientation);
		float j2DX = (float) domainAxis.valueToJava2D(this.x, dataArea, domainEdge);
		float j2DY = (float) rangeAxis.valueToJava2D(this.y, dataArea, rangeEdge);
		Rectangle2D area = new Rectangle2D.Double(j2DX - this.width / 2.0,
																	j2DY - this.height / 2.0,
																	this.width, this.height);
		this.drawable.draw(g2, area);

	}

	/**
	 * Tests this annotation for equality with an object.
	 * 
	 * @param object
	 *           the object to test against.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (object instanceof XYDrawableAnnotation) {

			XYDrawableAnnotation a = (XYDrawableAnnotation) object;
			boolean b0 = (this.x == a.x);
			boolean b1 = (this.y == a.y);
			boolean b2 = (this.width == a.width);
			boolean b3 = (this.height == a.height);
			boolean b4 = ObjectUtils.equal(this.drawable, a.drawable);
			return b0 && b1 && b2 && b3 && b4;
		}

		return false;

	}

	/**
	 * Returns a clone of the annotation.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if the annotation can't be cloned.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
