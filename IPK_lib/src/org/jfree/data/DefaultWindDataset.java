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
 * -----------------------
 * DefaultWindDataset.java
 * -----------------------
 * (C) Copyright 2001-2004, by Achilleus Mantzios and Contributors.
 * Original Author: Achilleus Mantzios;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultWindDataset.java,v 1.1 2011-01-31 09:02:12 klukas Exp $
 * Changes
 * -------
 * 06-Feb-2002 : Version 1, based on code contributed by Achilleus Mantzios (DG);
 * 05-May-2004 : Now extends AbstractXYDataset (DG);
 */

package org.jfree.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A default implementation of the {@link WindDataset} interface.
 * 
 * @author Achilleus Mantzios
 */
public class DefaultWindDataset extends AbstractXYDataset implements WindDataset {

	/** The names of the series. */
	private List seriesNames;

	/** Storage for the series data. */
	private List allSeriesData;

	/**
	 * Constructs a new, empty, dataset.
	 */
	public DefaultWindDataset() {
		this.seriesNames = new java.util.ArrayList();
		this.allSeriesData = new java.util.ArrayList();
	}

	/**
	 * Constructs a dataset based on the specified data.
	 * 
	 * @param data
	 *           the data.
	 */
	public DefaultWindDataset(final Object[][][] data) {
		this(seriesNameListFromDataArray(data), data);
	}

	/**
	 * Constructs a dataset based on the specified data.
	 * 
	 * @param seriesNames
	 *           the names of the series.
	 * @param data
	 *           the wind data.
	 */
	public DefaultWindDataset(final String[] seriesNames, final Object[][][] data) {
		this(Arrays.asList(seriesNames), data);
	}

	/**
	 * Constructs a dataset based on the specified data.
	 * 
	 * @param seriesNames
	 *           the names of the series.
	 * @param data
	 *           the wind dataset.
	 */
	public DefaultWindDataset(final List seriesNames, final Object[][][] data) {

		this.seriesNames = seriesNames;

		final int seriesCount = data.length;

		this.allSeriesData = new java.util.ArrayList(seriesCount);

		for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
			final List oneSeriesData = new java.util.ArrayList();
			final int maxItemCount = data[seriesIndex].length;
			for (int itemIndex = 0; itemIndex < maxItemCount; itemIndex++) {
				final Object xObject = data[seriesIndex][itemIndex][0];
				if (xObject != null) {
					final Number xNumber;
					if (xObject instanceof Number) {
						xNumber = (Number) xObject;
					} else {
						if (xObject instanceof Date) {
							final Date xDate = (Date) xObject;
							xNumber = new Long(xDate.getTime());
						} else {
							xNumber = new Integer(0);
						}
					}
					final Number windDir = (Number) data[seriesIndex][itemIndex][1];
					final Number windForce = (Number) data[seriesIndex][itemIndex][2];
					oneSeriesData.add(new WindDataItem(xNumber, windDir, windForce));
				}
			}
			Collections.sort(oneSeriesData);
			this.allSeriesData.add(seriesIndex, oneSeriesData);
		}

	}

	/**
	 * Returns the number of series in the dataset.
	 * 
	 * @return The series count.
	 */
	public int getSeriesCount() {
		return this.allSeriesData.size();
	}

	/**
	 * Returns the number of items in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The item count.
	 */
	public int getItemCount(final int series) {
		final List oneSeriesData = (List) this.allSeriesData.get(series);
		return oneSeriesData.size();
	}

	/**
	 * Returns the name of a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The series name.
	 */
	public String getSeriesName(final int series) {
		return this.seriesNames.get(series).toString();
	}

	/**
	 * Returns the x-value for one item within a series. This should represent
	 * a point in time, encoded as milliseconds in the same way as
	 * java.util.Date.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The x-value for the item within the series.
	 */
	public Number getXValue(final int series, final int item) {
		final List oneSeriesData = (List) this.allSeriesData.get(series);
		final WindDataItem windItem = (WindDataItem) oneSeriesData.get(item);
		return windItem.getX();
	}

	/**
	 * Returns the y-value for one item within a series. This maps to the
	 * getWindForce(...) method and is implemented because WindDataset is an
	 * extension of XYDataset.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The y-value for the item within the series.
	 */
	public Number getYValue(final int series, final int item) {
		return getWindForce(series, item);
	}

	/**
	 * Returns the wind direction for one item within a series. This is a
	 * number between 0 and 12, like the numbers on a clock face.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The wind direction for the item within the series.
	 */
	public Number getWindDirection(final int series, final int item) {
		final List oneSeriesData = (List) this.allSeriesData.get(series);
		final WindDataItem windItem = (WindDataItem) oneSeriesData.get(item);
		return windItem.getWindDirection();
	}

	/**
	 * Returns the wind force for one item within a series. This is a number
	 * between 0 and 12, as defined by the Beaufort scale.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The wind force for the item within the series.
	 */
	public Number getWindForce(final int series, final int item) {
		final List oneSeriesData = (List) this.allSeriesData.get(series);
		final WindDataItem windItem = (WindDataItem) oneSeriesData.get(item);
		return windItem.getWindForce();
	}

	/**
	 * Utility method for automatically generating series names.
	 * 
	 * @param data
	 *           the wind dataset.
	 * @return An array of <i>Series N</i> with N = { 1 .. data.length }.
	 */
	public static List seriesNameListFromDataArray(final Object[][] data) {

		final int seriesCount = data.length;
		final List seriesNameList = new java.util.ArrayList(seriesCount);
		for (int i = 0; i < seriesCount; i++) {
			seriesNameList.add("Series " + (i + 1));
		}
		return seriesNameList;

	}

}

/**
 * A wind data item.
 * 
 * @author Achilleus Mantzios
 */
class WindDataItem implements Comparable {

	/** The x-value. */
	private Number x;

	/** The wind direction. */
	private Number windDir;

	/** The wind force. */
	private Number windForce;

	/**
	 * Creates a new wind data item.
	 * 
	 * @param x
	 *           the x-value.
	 * @param windDir
	 *           the direction.
	 * @param windForce
	 *           the force.
	 */
	public WindDataItem(final Number x, final Number windDir, final Number windForce) {
		this.x = x;
		this.windDir = windDir;
		this.windForce = windForce;
	}

	/**
	 * Returns the x-value.
	 * 
	 * @return The x-value.
	 */
	public Number getX() {
		return this.x;
	}

	/**
	 * Returns the wind direction.
	 * 
	 * @return The wind direction.
	 */
	public Number getWindDirection() {
		return this.windDir;
	}

	/**
	 * Returns the wind force.
	 * 
	 * @return The wind force.
	 */
	public Number getWindForce() {
		return this.windForce;
	}

	/**
	 * Compares this item to another object.
	 * 
	 * @param object
	 *           the other object.
	 * @return An int that indicates the relative comparison.
	 */
	public int compareTo(final Object object) {
		if (object instanceof WindDataItem) {
			final WindDataItem item = (WindDataItem) object;
			if (this.x.doubleValue() > item.x.doubleValue()) {
				return 1;
			} else
				if (this.x.equals(item.x)) {
					return 0;
				} else {
					return -1;
				}
		} else {
			throw new ClassCastException("WindDataItem.compareTo(error)");
		}
	}

}
