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
 * -------------------
 * EmptyXYDataset.java
 * -------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: EmptyXYDataset.java,v 1.1 2011-01-31 09:01:51 klukas Exp $
 * Changes
 * -------
 * 22-Nov-2001 : Version 1 (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 05-May-2004 : Now extends AbstractXYDataset (DG);
 */

package org.jfree.chart.demo;

import org.jfree.data.AbstractXYDataset;
import org.jfree.data.XYDataset;

/**
 * An empty dataset for testing purposes.
 */
public class EmptyXYDataset extends AbstractXYDataset implements XYDataset {

	/**
	 * Default constructor.
	 */
	public EmptyXYDataset() {
		super();
	}

	/**
	 * Returns the x-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the x-value (always null for this class).
	 */
	public Number getXValue(final int series, final int item) {
		return null;
	}

	/**
	 * Returns the y-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the y-value (always null for this class).
	 */
	public Number getYValue(final int series, final int item) {
		return null;
	}

	/**
	 * Returns the number of series in the dataset.
	 * 
	 * @return the series count (always zero for this class).
	 */
	public int getSeriesCount() {
		return 0;
	}

	/**
	 * Returns the name of the series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the name of the series (always null in this class).
	 */
	public String getSeriesName(final int series) {
		return null;
	}

	/**
	 * Returns the number of items in the specified series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the item count (always zero in this class).
	 */
	public int getItemCount(final int series) {
		return 0;
	}

}
