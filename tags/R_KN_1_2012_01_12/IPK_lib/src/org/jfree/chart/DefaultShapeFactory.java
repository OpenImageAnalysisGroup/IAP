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
 * ------------------------
 * DefaultShapeFactory.java
 * ------------------------
 * (C) Copyright 2002-2004, by Jeremy Bowman and Contributors.
 * Original Author: Jeremy Bowman;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultShapeFactory.java,v 1.1 2011-01-31 09:03:12 klukas Exp $
 * Changes
 * -------
 * 13-May-2002 : Version 1 (JB);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-Aug-2003 : No longer required, so deprecated (DG);
 */

package org.jfree.chart;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Default provider of shapes for indicating data points on a Plot.
 * 
 * @deprecated No longer used. Shapes are supplied by the DrawingSupplier if necessary.
 * @author Jeremy Bowman
 */
public class DefaultShapeFactory implements ShapeFactory {

	/**
	 * Returns a Shape that can be used in plotting data. Used in XYPlots.
	 * 
	 * @param series
	 *           number of series.
	 * @param item
	 *           index of the item. <i>Not used, i.e. redundant.</i>.
	 * @param x
	 *           the centered x position of the shape.
	 * @param y
	 *           the centered y position of the shape.
	 * @param scale
	 *           the size of the shape (width and height, radius).
	 * @return a square for series == 0, a circle otherwise.
	 */
	public Shape getShape(int series, int item, double x, double y, double scale) {

		if (series == 0) {
			return new Rectangle2D.Double(x - 0.5 * scale, y - 0.5 * scale, scale, scale);
		} else {
			return new Ellipse2D.Double(x - 0.5 * scale, y - 0.5 * scale, scale, scale);
		}

	}

	/**
	 * Returns a Shape that can be used in plotting data. Used in CategoryPlots.
	 * 
	 * @param series
	 *           number of series. <i>Not used, i.e. redundant</i>.
	 * @param category
	 *           the category. <i>Not used, i.e. redundant</i>.
	 * @param x
	 *           the centered x position of the shape.
	 * @param y
	 *           the centered y position of the shape.
	 * @param scale
	 *           the size of the shape (width and height, radius).
	 * @return a circle with the radius <code>scale</code> centered at (x,y).
	 */
	public Shape getShape(int series, Object category, double x, double y, double scale) {

		return new Ellipse2D.Double(x - 0.5 * scale, y - 0.5 * scale, scale, scale);

	}
}
