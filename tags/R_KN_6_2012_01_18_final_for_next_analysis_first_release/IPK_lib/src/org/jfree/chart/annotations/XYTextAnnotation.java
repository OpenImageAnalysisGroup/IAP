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
 * ---------------------
 * XYTextAnnotation.java
 * ---------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XYTextAnnotation.java,v 1.1 2011-01-31 09:02:52 klukas Exp $
 * Changes:
 * --------
 * 28-Aug-2002 : Version 1 (DG);
 * 07-Nov-2002 : Fixed errors reported by Checkstyle (DG);
 * 13-Jan-2003 : Reviewed Javadocs (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 02-Jul-2003 : Added new text alignment and rotation options (DG);
 * 19-Aug-2003 : Implemented Cloneable (DG);
 * 17-Jan-2003 : Added fix for bug 878706, where the annotation is placed incorrectly
 * for a plot with horizontal orientation (thanks to Ed Yu for the
 * fix) (DG);
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
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;

/**
 * A text annotation that can be placed at a particular (x, y) location on an {@link XYPlot}.
 */
public class XYTextAnnotation extends TextAnnotation
										implements XYAnnotation, Cloneable, Serializable {

	/** The x-coordinate. */
	private double x;

	/** The y-coordinate. */
	private double y;

	/**
	 * Creates a new annotation to be displayed at the given coordinates. The coordinates
	 * are specified in data space (they will be converted to Java2D space for display).
	 * 
	 * @param text
	 *           the text.
	 * @param x
	 *           the x-coordinate.
	 * @param y
	 *           the y-coordinate.
	 */
	public XYTextAnnotation(String text, double x, double y) {
		super(text);
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the x coordinate for the text anchor point (measured against the domain axis).
	 * 
	 * @return The x coordinate.
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Sets the x coordinate for the text anchor point (measured against the domain axis).
	 * 
	 * @param x
	 *           the x coordinate.
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Returns the y coordinate for the text anchor point (measured against the range axis).
	 * 
	 * @return The y coordinate.
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * Sets the y coordinate for the text anchor point (measured against the range axis).
	 * 
	 * @param y
	 *           the y coordinate.
	 */
	public void setY(double y) {
		this.y = y;
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

		float anchorX = (float) domainAxis.valueToJava2D(this.x, dataArea, domainEdge);
		float anchorY = (float) rangeAxis.valueToJava2D(this.y, dataArea, rangeEdge);

		if (orientation == PlotOrientation.HORIZONTAL) {
			float tempAnchor = anchorX;
			anchorX = anchorY;
			anchorY = tempAnchor;
		}

		g2.setFont(getFont());
		g2.setPaint(getPaint());
		RefineryUtilities.drawRotatedString(
							getText(),
							g2,
							anchorX,
							anchorY,
							getTextAnchor(),
							getRotationAnchor(),
							getRotationAngle()
							);

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
