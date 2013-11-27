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
 * XYShapeAnnotation.java
 * ---------------------
 * (C) Copyright 2003, 2004, by Ondax, Inc. and Contributors.
 * Original Author: Greg Steckman (for Ondax, Inc.);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: XYShapeAnnotation.java,v 1.1 2011-01-31 09:02:52 klukas Exp $
 * Changes:
 * --------
 * 15-Aug-2003 : Version 1, adapted from org.jfree.chart.annotations.XYLineAnnotation (GS);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 20-Apr-2004 : Added new constructor and fixed bug 934258 (DG);
 */

package org.jfree.chart.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

/**
 * A simple <code>Shape</code> annotation that can be placed on an {@link XYPlot}. The
 * shape coordinates are specified in data space.
 * 
 * @author Greg Steckman
 */
public class XYShapeAnnotation implements XYAnnotation, Serializable {

	/** The Shape to draw. */
	private Shape shape;

	/** The shape's stroke. */
	private Stroke stroke;

	/** The shape's color. */
	private Paint paint;

	/**
	 * Creates a new annotation.
	 * 
	 * @param shape
	 *           the shape (coordinates in data space).
	 */
	public XYShapeAnnotation(Shape shape) {
		this(shape, new BasicStroke(1.0f), Color.black);
	}

	/**
	 * Creates a new annotation.
	 * 
	 * @param shape
	 *           the shape (<code>null</code> not permitted).
	 * @param stroke
	 *           the shape stroke (<code>null</code> not permitted).
	 * @param paint
	 *           the shape color (<code>null</code> not permitted).
	 */
	public XYShapeAnnotation(Shape shape, Stroke stroke, Paint paint) {
		if (shape == null) {
			throw new IllegalArgumentException("Null 'shape' argument.");
		}
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		}
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		this.shape = shape;
		this.stroke = stroke;
		this.paint = paint;
	}

	/**
	 * Draws the annotation. This method is usually called by the {@link XYPlot} class, you
	 * shouldn't need to call it directly.
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
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
							plot.getDomainAxisLocation(), orientation
							);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
							plot.getRangeAxisLocation(), orientation
							);

		// compute transform matrix elements via sample points. Assume no rotation or shear.
		// x-axis translation
		double m02 = domainAxis.valueToJava2D(0, dataArea, domainEdge);
		// y-axis translation
		double m12 = rangeAxis.valueToJava2D(0, dataArea, rangeEdge);
		// x-axis scale
		double m00 = domainAxis.valueToJava2D(1, dataArea, domainEdge) - m02;
		// y-axis scale
		double m11 = rangeAxis.valueToJava2D(1, dataArea, rangeEdge) - m12;

		// create transform & transform shape
		Shape s = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			AffineTransform t1 = new AffineTransform(0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
			AffineTransform t2 = new AffineTransform(m11, 0.0f, 0.0f, m00, m12, m02);
			s = t1.createTransformedShape(this.shape);
			s = t2.createTransformedShape(s);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				AffineTransform t = new AffineTransform(m00, 0, 0, m11, m02, m12);
				s = t.createTransformedShape(this.shape);
			}

		g2.setPaint(this.paint);
		g2.setStroke(this.stroke);
		g2.draw(s);

	}

}
