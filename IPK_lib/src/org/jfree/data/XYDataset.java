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
 * --------------
 * XYDataset.java
 * --------------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XYDataset.java,v 1.1 2011-01-31 09:02:18 klukas Exp $
 * Changes (from 18-Sep-2001)
 * --------------------------
 * 18-Sep-2001 : Added standard header and fixed DOS encoding problem (DG);
 * 15-Oct-2001 : Moved to a new package (com.jrefinery.data.*) (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 17-Nov-2001 : Now extends SeriesDataset (DG);
 */

package org.jfree.data;

/**
 * An interface through which data in the form of (x, y) items can be accessed.
 */
public interface XYDataset extends SeriesDataset {

	/**
	 * Returns the number of items in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The item count.
	 */
	public int getItemCount(int series);

	/**
	 * Returns the x-value for an item within a series. The x-values may or may not be returned
	 * in ascending order, that is up to the class implementing the interface.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The x-value (possibly <code>null</code>).
	 */
	public Number getXValue(int series, int item);

	/**
	 * Returns the x-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The x-value.
	 */
	public double getX(int series, int item);

	/**
	 * Returns the y-value for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the y-value (possibly <code>null</code>).
	 */
	public Number getYValue(int series, int item);

	/**
	 * Returns the y-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The y-value.
	 */
	public double getY(int series, int item);

}
