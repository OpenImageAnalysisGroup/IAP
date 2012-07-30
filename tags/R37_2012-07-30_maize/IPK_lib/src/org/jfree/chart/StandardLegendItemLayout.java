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
 * -----------------------------
 * StandardLegendItemLayout.java
 * -----------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: StandardLegendItemLayout.java,v 1.1 2011-01-31 09:03:11 klukas Exp $
 * Changes
 * -------
 * 07-Feb-2002 : Version 1 (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart;

import java.util.Iterator;

/**
 * A class for arranging legend items.
 */
public class StandardLegendItemLayout implements LegendItemLayout {

	/** Useful constant for vertical orientation. */
	public static final int VERTICAL = 0;

	/** Useful constant for horizontal orientation. */
	public static final int HORIZONTAL = 1;

	/** The orientation of the layout (HORIZONTAL or VERTICAL). */
	private int orientation;

	/** A constraint on one of the dimesions in the layout. */
	private double dimension;

	/**
	 * Constructs a new layout class for legend items.
	 * 
	 * @param orientation
	 *           the orientation of the layout (HORIZONTAL or VERTICAL).
	 * @param dimension
	 *           the constrained dimension.
	 */
	public StandardLegendItemLayout(int orientation, double dimension) {

		this.orientation = orientation;
		this.dimension = dimension;

	}

	/**
	 * Performs a layout on the items in the collection.
	 * 
	 * @param collection
	 *           the collection to be laid out.
	 */
	public void layoutLegendItems(LegendItemCollection collection) {

		if (this.orientation == HORIZONTAL) {
			doHorizontalLayout(collection);
		} else
			if (this.orientation == VERTICAL) {
				doVerticalLayout(collection);
			}

	}

	/**
	 * Lays out the items horizontally, with a constraint on the width.
	 * 
	 * @param collection
	 *           The collection to be laid out.
	 */
	private void doHorizontalLayout(LegendItemCollection collection) {

		// run through the items in the collection and set their coordinates
		// relative to (0, 0)
		Iterator iterator = collection.iterator();

		// int rowCount = 0;
		// double totalHeight = 0.0;
		boolean first = true;
		double currentRowX = 0.0;
		double currentRowY = 0.0;
		double currentRowHeight = 0.0;

		while (iterator.hasNext()) {
			DrawableLegendItem item = (DrawableLegendItem) iterator.next();
			if ((first) || (item.getWidth() < (this.dimension - currentRowX))) {
				item.setX(currentRowX);
				item.setY(currentRowY);
				currentRowX = currentRowX + item.getWidth();
				currentRowHeight = Math.max(currentRowHeight, item.getHeight());
				first = false;
			} else { // start new row
				currentRowY = currentRowY + currentRowHeight;
				currentRowHeight = item.getHeight();
				item.setX(0.0);
				currentRowX = item.getWidth();
			}

		}

	}

	/**
	 * Lays out the items vertically, with a constraint on the height.
	 * 
	 * @param collection
	 *           The collection to be laid out.
	 */
	private void doVerticalLayout(LegendItemCollection collection) {

		// run through the items in the collection and set their coordinates
		// relative to (0, 0)
		Iterator iterator = collection.iterator();

		// int columnCount = 0;
		// double totalWidth = 0.0;
		boolean first = true;
		double currentColumnX = 0.0;
		double currentColumnY = 0.0;
		double currentColumnWidth = 0.0;

		while (iterator.hasNext()) {
			DrawableLegendItem item = (DrawableLegendItem) iterator.next();
			if ((first) || (item.getHeight() < (this.dimension - currentColumnY))) {
				item.setX(currentColumnX);
				item.setY(currentColumnY);
				currentColumnY = currentColumnY + item.getHeight();
				currentColumnWidth = Math.max(currentColumnWidth, item.getWidth());
				first = false;
			} else { // start new column
				currentColumnX = currentColumnX + currentColumnWidth;
				currentColumnWidth = item.getWidth();
				item.setY(0.0);
				currentColumnY = item.getHeight();
			}

		}
	}

}
