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
 * -----------------
 * ShapeFactory.java
 * -----------------
 * (C) Copyright 2002-2004, by Jeremy Bowman and Contributors.
 * Original Author: Jeremy Bowman;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: ShapeFactory.java,v 1.1 2011-01-31 09:03:12 klukas Exp $
 * Changes
 * -------
 * 13-May-2002 : Version 1 (JB);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-Aug-2003 : No longer required, so deprecated (DG);
 */

package org.jfree.chart;

import java.awt.Shape;

/**
 * Interface to be implemented by classes which provide shapes for indicating
 * data points on a Plot.
 * 
 * @deprecated This interface is no longer used, shapes are obtained from the DrawingSupplier.
 * @author Jeremy Bowman
 */
public interface ShapeFactory {

	/**
	 * Returns a Shape that can be used in plotting data. Used in XYPlots.
	 * 
	 * @param series
	 *           the index of the series.
	 * @param item
	 *           the index of the item.
	 * @param x
	 *           x-coordinate of the item.
	 * @param y
	 *           y-coordinate of the item.
	 * @param scale
	 *           the size.
	 * @return a Shape that can be used in plotting data.
	 */
	public Shape getShape(int series, int item, double x, double y,
									double scale);

	/**
	 * Returns a Shape that can be used in plotting data. Used in
	 * CategoryPlots.
	 * 
	 * @param series
	 *           the index of the series.
	 * @param category
	 *           the category.
	 * @param x
	 *           x-coordinate of the category.
	 * @param y
	 *           y-coordinate of the category.
	 * @param scale
	 *           the size.
	 * @return a Shape that can be used in plotting data.
	 */
	public Shape getShape(int series, Object category, double x, double y,
									double scale);

}
