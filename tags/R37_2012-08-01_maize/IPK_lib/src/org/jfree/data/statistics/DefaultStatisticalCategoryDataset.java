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
 * --------------------------------------
 * DefaultStatisticalCategoryDataset.java
 * --------------------------------------
 * (C) Copyright 2002, 2003, by Pascal Collet.
 * Original Author: Pascal Collet;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultStatisticalCategoryDataset.java,v 1.1 2011-01-31 09:02:06 klukas Exp $
 * Changes
 * -------
 * 21-Aug-2002 : Version 1, contributed by Pascal Collet (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 05-Feb-2003 : Revised implementation to use KeyedObjects2D (DG);
 * 28-Aug-2003 : Moved from org.jfree.data --> org.jfree.data.statistics (DG);
 * 06-Oct-2003 : Removed incorrect Javadoc text (DG);
 */

package org.jfree.data.statistics;

import java.util.List;

import org.jfree.data.AbstractDataset;
import org.jfree.data.KeyedObjects2D;
import org.jfree.data.MeanAndStandardDeviation;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;

/**
 * A convenience class that provides a default implementation of the {@link StatisticalCategoryDataset} interface.
 * 
 * @author Pascal Collet
 */
public class DefaultStatisticalCategoryDataset extends AbstractDataset
																implements StatisticalCategoryDataset,
																				RangeInfo {

	/** Storage for the data. */
	protected KeyedObjects2D data;

	/** The minimum range value. */
	private Number minimumRangeValue;

	/** The maximum range value. */
	private Number maximumRangeValue;

	/** The range of values. */
	private Range valueRange;

	private boolean drawOnlyTopOfErrorBar;
	private double errorBarLen;

	/**
	 * Creates a new dataset.
	 */
	public DefaultStatisticalCategoryDataset() {

		this.data = new KeyedObjects2D();
		this.minimumRangeValue = new Double(0.0);
		this.maximumRangeValue = new Double(0.0);
		this.valueRange = new Range(0.0, 0.0);

	}

	/**
	 * Returns the mean value for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return the mean value.
	 */
	public Number getMeanValue(final int row, final int column) {

		Number result = null;
		final MeanAndStandardDeviation masd = (MeanAndStandardDeviation) this.data.getObject(row, column);
		if (masd != null && (!Double.isNaN(masd.getMean().doubleValue()))) {
			result = masd.getMean();
		}
		return result;

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
		return getMeanValue(row, column);
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
		return getMeanValue(rowKey, columnKey);
	}

	/**
	 * Returns the mean value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the columnKey.
	 * @return the mean value.
	 */
	public Number getMeanValue(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final MeanAndStandardDeviation masd = (MeanAndStandardDeviation) this.data.getObject(rowKey, columnKey);
		if (masd != null) {
			result = masd.getMean();
		}
		return result;

	}

	/**
	 * Returns the standard deviation value for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return the standard deviation.
	 */
	public Number getStdDevValue(final int row, final int column) {

		Number result = null;
		final MeanAndStandardDeviation masd = (MeanAndStandardDeviation) this.data.getObject(row, column);
		if (masd != null) {
			result = masd.getStandardDeviation();
		}
		return result;

	}

	/**
	 * Returns the standard deviation value for an item.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the columnKey.
	 * @return the standard deviation.
	 */
	public Number getStdDevValue(final Comparable rowKey, final Comparable columnKey) {

		Number result = null;
		final MeanAndStandardDeviation masd = (MeanAndStandardDeviation) this.data.getObject(rowKey, columnKey);
		if (masd != null) {
			result = masd.getStandardDeviation();
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
	 * Adds a mean and standard deviation to the table.
	 * 
	 * @param mean
	 *           the mean.
	 * @param standardDeviation
	 *           the standard deviation.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void add(final double mean, final double standardDeviation,
							final Comparable rowKey, final Comparable columnKey, boolean showOnlyHalfErrorBar) {

		final MeanAndStandardDeviation item = new MeanAndStandardDeviation(new Double(mean),
																							new Double(standardDeviation));
		addDataObject(item, rowKey, columnKey);
		addStep2(mean, standardDeviation, rowKey, columnKey, showOnlyHalfErrorBar);
	}

	public void add(final double mean, final double standardDeviation,
						final Comparable rowKey, final Comparable columnKey) {
		final MeanAndStandardDeviation item = new MeanAndStandardDeviation(new Double(mean),
																							new Double(standardDeviation));
		addDataObject(item, rowKey, columnKey);
		addStep2(mean, standardDeviation, rowKey, columnKey, false);
	}

	protected void addDataObject(MeanAndStandardDeviation item,
						final Comparable rowKey, final Comparable columnKey) {
		this.data.addObject(item, rowKey, columnKey);
	}

	protected void addStep2(double mean, double standardDeviation,
							final Comparable rowKey, final Comparable columnKey,
							boolean showOnlyHalfErrorBar) {
		if (Double.isInfinite(standardDeviation) || Double.isNaN(standardDeviation))
			standardDeviation = 0;
		if (!Double.isInfinite(mean) && !Double.isInfinite(standardDeviation)
							&& !Double.isNaN(mean) && !Double.isNaN(standardDeviation)) {
			if ((mean + standardDeviation) > this.maximumRangeValue.doubleValue()) {
				this.maximumRangeValue = new Double(mean + standardDeviation);
				this.valueRange = new Range(this.minimumRangeValue.doubleValue(),
															this.maximumRangeValue.doubleValue());
			}
			if ((mean - standardDeviation) < this.minimumRangeValue.doubleValue()) {
				this.minimumRangeValue = new Double(mean - (showOnlyHalfErrorBar ? 0 : standardDeviation));
				this.valueRange = new Range(this.minimumRangeValue.doubleValue(),
															this.maximumRangeValue.doubleValue());
			}
			fireDatasetChanged();
		}
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

	public boolean drawOnlyTopOfErrorBar() {
		return drawOnlyTopOfErrorBar;
	}

	public void setDrawOnlyTopOfErrorBar(boolean value) {
		this.drawOnlyTopOfErrorBar = value;
	}

	public double getErrorBarLen() {
		return errorBarLen;
	}

	public void setErrorBarLen(double value) {
		this.errorBarLen = value;
	}

}
