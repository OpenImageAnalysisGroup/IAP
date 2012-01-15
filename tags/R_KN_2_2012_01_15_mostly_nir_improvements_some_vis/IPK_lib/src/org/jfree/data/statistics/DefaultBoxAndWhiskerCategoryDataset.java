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
 * ----------------------------------------
 * DefaultBoxAndWhiskerCategoryDataset.java
 * ----------------------------------------
 * (C) Copyright 2003, by David Browning and Contributors.
 * Original Author: David Browning (for Australian Institute of Marine Science);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultBoxAndWhiskerCategoryDataset.java,v 1.1 2011-01-31 09:02:05 klukas Exp $
 * Changes
 * -------
 * 05-Aug-2003 : Version 1, contributed by David Browning (DG);
 * 27-Aug-2003 : Moved from org.jfree.data --> org.jfree.data.statistics (DG);
 * 12-Nov-2003 : Changed 'data' from private to protected and added a new 'add' method as proposed
 * by Tim Bardzil. Also removed old code (DG);
 * 01-Mar-2004 : Added equals() method (DG);
 */

package org.jfree.data.statistics;

import java.util.List;

import org.jfree.data.AbstractDataset;
import org.jfree.data.KeyedObjects2D;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.util.ObjectUtils;

/**
 * A convenience class that provides a default implementation of the {@link BoxAndWhiskerCategoryDataset} interface.
 * 
 * @author David Browning (for Australian Institute of Marine Science)
 */
public class DefaultBoxAndWhiskerCategoryDataset extends AbstractDataset
																	implements BoxAndWhiskerCategoryDataset,
																				RangeInfo {

	/** Storage for the data. */
	protected KeyedObjects2D data;

	/** The minimum range value. */
	private Number minimumRangeValue;

	/** The maximum range value. */
	private Number maximumRangeValue;

	/** The range of values. */
	private Range valueRange;

	/**
	 * Creates a new dataset.
	 */
	public DefaultBoxAndWhiskerCategoryDataset() {

		this.data = new KeyedObjects2D();
		this.minimumRangeValue = null;
		this.maximumRangeValue = null;
		this.valueRange = new Range(0.0, 0.0);

	}

	/**
	 * Returns the value for an item.
	 * 
	 * @param row
	 *           the row index.
	 * @param column
	 *           the column index.
	 * @return the value.
	 */
	public Number getValue(final int row, final int column) {
		return getMedianValue(row, column);
	}

	/**
	 * Returns the value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the columnKey.
	 * @return the value.
	 */
	public Number getValue(final Comparable rowKey, final Comparable columnKey) {
		return getMedianValue(rowKey, columnKey);
	}

	/**
	 * Returns the mean value for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The mean value.
	 */
	public Number getMeanValue(final int row, final int column) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getMean();
		}
		return result;

	}

	/**
	 * Returns the mean value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return The mean value.
	 */
	public Number getMeanValue(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getMean();
		}
		return result;

	}

	/**
	 * Returns the median value for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return the median value.
	 */
	public Number getMedianValue(final int row, final int column) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getMedian();
		}
		return result;

	}

	/**
	 * Returns the median value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the columnKey.
	 * @return the median value.
	 */
	public Number getMedianValue(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getMedian();
		}
		return result;

	}

	/**
	 * Returns the first quartile value.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The first quartile value.
	 */
	public Number getQ1Value(final int row, final int column) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getQ1();
		}
		return result;

	}

	/**
	 * Returns the first quartile value.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return The first quartile value.
	 */
	public Number getQ1Value(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getQ1();
		}
		return result;

	}

	/**
	 * Returns the third quartile value.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The third quartile value.
	 */
	public Number getQ3Value(final int row, final int column) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getQ3();
		}
		return result;

	}

	/**
	 * Returns the third quartile value.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return The third quartile value.
	 */
	public Number getQ3Value(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getQ3();
		}
		return result;

	}

	/**
	 * Returns the column index for a given key.
	 * 
	 * @param key
	 *           the column key.
	 * @return the column index.
	 */
	public int getColumnIndex(final Comparable key) {
		return this.data.getColumnIndex(key);
	}

	/**
	 * Returns a column key.
	 * 
	 * @param column
	 *           the column index (zero-based).
	 * @return the column key.
	 */
	public Comparable getColumnKey(final int column) {
		return this.data.getColumnKey(column);
	}

	/**
	 * Returns the column keys.
	 * 
	 * @return the keys.
	 */
	public List getColumnKeys() {
		return this.data.getColumnKeys();
	}

	/**
	 * Returns the row index for a given key.
	 * 
	 * @param key
	 *           the row key.
	 * @return the row index.
	 */
	public int getRowIndex(final Comparable key) {
		return this.data.getRowIndex(key);
	}

	/**
	 * Returns a row key.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @return the row key.
	 */
	public Comparable getRowKey(final int row) {
		return this.data.getRowKey(row);
	}

	/**
	 * Returns the row keys.
	 * 
	 * @return the keys.
	 */
	public List getRowKeys() {
		return this.data.getRowKeys();
	}

	/**
	 * Returns the number of rows in the table.
	 * 
	 * @return the row count.
	 */
	public int getRowCount() {
		return this.data.getRowCount();
	}

	/**
	 * Returns the number of columns in the table.
	 * 
	 * @return the column count.
	 */
	public int getColumnCount() {
		return this.data.getColumnCount();
	}

	/**
	 * Adds a list of values relating to one Box and Whisker entity to the table.
	 * The various median values are calculated.
	 * 
	 * @param list
	 *           a collection of values from which the various medians will be calculated.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void add(final List list, final Comparable rowKey, final Comparable columnKey) {

		final BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(list);
		add(item, rowKey, columnKey);

	}

	/**
	 * Adds a list of values relating to one Box and Whisker entity to the table.
	 * The various median values are calculated.
	 * 
	 * @param item
	 *           a box and whisker item.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void add(final BoxAndWhiskerItem item,
							final Comparable rowKey,
							final Comparable columnKey) {

		this.data.addObject(item, rowKey, columnKey);
		final double minval = item.getMinOutlier().doubleValue();
		final double maxval = item.getMaxOutlier().doubleValue();

		if (this.maximumRangeValue == null) {
			this.maximumRangeValue = new Double(maxval);
		} else
			if (maxval > this.maximumRangeValue.doubleValue()) {
				this.maximumRangeValue = new Double(maxval);
			}

		if (this.minimumRangeValue == null) {
			this.minimumRangeValue = new Double(minval);
		} else
			if (minval < this.minimumRangeValue.doubleValue()) {
				this.minimumRangeValue = new Double(minval);
			}

		this.valueRange = new Range(this.minimumRangeValue.doubleValue(),
												this.maximumRangeValue.doubleValue());

		fireDatasetChanged();

	}

	/**
	 * Returns the minimum value in the dataset's range (or null if all the
	 * values in the range are null).
	 * 
	 * @return the minimum value.
	 */
	public Number getMinimumRangeValue() {
		return this.minimumRangeValue;
	}

	/**
	 * Returns the maximum value in the dataset's range (or null if all the
	 * values in the range are null).
	 * 
	 * @return the maximum value.
	 */
	public Number getMaximumRangeValue() {
		return this.maximumRangeValue;
	}

	/**
	 * Returns the range of the values in this dataset's range.
	 * 
	 * @return the range.
	 */
	public Range getValueRange() {
		return this.valueRange;
	}

	// public Number getInterquartileRangeValue(int row, int column) {
	//
	// Number result = null;
	// QuartileCalculator qCalc = (QuartileCalculator) this.data.getObject(row, column);
	// if (qCalc != null) {
	// result = new Double(qCalc.getInterquartileRange());
	// }
	// return result;
	//
	// }
	//
	// public Number getInterquartileRangeValue(Comparable rowKey, Comparable columnKey) {
	//
	// Number result = null;
	// QuartileCalculator qCalc = (QuartileCalculator) this.data.getObject(rowKey, columnKey);
	// if (qCalc != null) {
	// result = new Double(qCalc.getInterquartileRange());
	// }
	// return result;
	//
	// }

	/**
	 * Returns the minimum regular (non outlier) value for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The minimum regular value.
	 */
	public Number getMinRegularValue(final int row, final int column) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getMinRegularValue();
		}
		return result;

	}

	/**
	 * Returns the minimum regular (non outlier) value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return The minimum regular value.
	 */
	public Number getMinRegularValue(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getMinRegularValue();
		}
		return result;

	}

	/**
	 * Returns the maximum regular (non outlier) value for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The maximum regular value.
	 */
	public Number getMaxRegularValue(final int row, final int column) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getMaxRegularValue();
		}
		return result;

	}

	/**
	 * Returns the maximum regular (non outlier) value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return The maximum regular value.
	 */
	public Number getMaxRegularValue(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getMaxRegularValue();
		}
		return result;

	}

	/**
	 * Returns the minimum outlier (non farout) value for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The minimum outlier.
	 */
	public Number getMinOutlier(final int row, final int column) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getMinOutlier();
		}
		return result;

	}

	/**
	 * Returns the minimum outlier (non farout) value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return The minimum outlier.
	 */
	public Number getMinOutlier(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getMinOutlier();
		}
		return result;

	}

	/**
	 * Returns the maximum outlier (non farout) value for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The maximum outlier.
	 */
	public Number getMaxOutlier(final int row, final int column) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getMaxOutlier();
		}
		return result;

	}

	/**
	 * Returns the maximum outlier (non farout) value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return The maximum outlier.
	 */
	public Number getMaxOutlier(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getMaxOutlier();
		}
		return result;

	}

	/**
	 * Returns a list of outlier values for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return A list of outlier values.
	 */
	public List getOutliers(final int row, final int column) {

		List result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			result = item.getOutliers();
		}
		return result;

	}

	/**
	 * Returns a list of outlier values for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return A list of outlier values.
	 */
	public List getOutliers(final Comparable rowKey, final Comparable columnKey) {

		List result = null;
		final BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(rowKey, columnKey);
		if (item != null) {
			result = item.getOutliers();
		}
		return result;

	}

	/**
	 * Tests this dataset for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object to test against (<code>null</code> permitted).
	 * @return a boolean.
	 */
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof DefaultBoxAndWhiskerCategoryDataset) {
			final DefaultBoxAndWhiskerCategoryDataset dataset = (DefaultBoxAndWhiskerCategoryDataset) obj;
			return ObjectUtils.equal(this.data, dataset.data);
		}

		return false;
	}

}
