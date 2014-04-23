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
 * XYSeriesCollection.java
 * -----------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited and Contributors.
 * Original Author: Andreas Schroeder;
 * Contributor(s): ;
 * $Id: CategoryTableXYDataset.java,v 1.1 2011-01-31 09:02:13 klukas Exp $
 * Changes
 * -------
 * 31-Mar-2004 : Version 1 (AS);
 * 05-May-2004 : Now extends AbstractIntervalXYDataset (DG);
 */

package org.jfree.data;

/**
 * An implementation variant of the {@link TableXYDataset} where every series
 * shares the same x-values (required for generating stacked area charts).
 * This implementation uses a {@link DefaultKeyedValues2D} Object as backend
 * implementation and is hence more "category oriented" than the {@link DefaultTableXYDataset} implementation.
 * <p>
 * This implementation provides no means to remove data items yet. This is due to the lack of such facility in the DefaultKeyedValues2D class.
 * <p>
 * This class also implements the {@link IntervalXYDataset} interface, but this implementation is provisional.
 * 
 * @author Andreas Schroeder
 */
public class CategoryTableXYDataset extends AbstractIntervalXYDataset
												implements TableXYDataset,
																IntervalXYDataset,
																DomainInfo {

	/**
	 * The backing data structure.
	 */
	private DefaultKeyedValues2D values;

	/** A delegate for controlling the interval width. */
	private IntervalXYDelegate intervalDelegate;

	/**
	 * Creates a new empty CategoryTableXYDataset.
	 */
	public CategoryTableXYDataset() {
		this.values = new DefaultKeyedValues2D(true);
		this.intervalDelegate = new IntervalXYDelegate(this);
	}

	/**
	 * Adds a data item to this data set.
	 * 
	 * @param x
	 *           the x value of the item to add.
	 * @param y
	 *           the y value of the item to add.
	 * @param seriesName
	 *           the name of the series to add the data item.
	 */
	public void add(final double x, final double y, final String seriesName) {
		add(new Double(x), new Double(y), seriesName, true);
	}

	/**
	 * Adds a data item to this data set.
	 * 
	 * @param x
	 *           the x value of the item to add.
	 * @param y
	 *           the y value of the item to add.
	 * @param seriesName
	 *           the name of the series to add the data item.
	 * @param notify
	 *           a flag that controls whether or not a {@link SeriesChangeEvent} is sent to all registered listeners.
	 */
	public void add(final Number x, final Number y, final String seriesName, final boolean notify) {
		this.values.addValue(y, (Comparable) x, seriesName);

		final int series = this.values.getColumnIndex(seriesName);
		final int item = this.values.getRowIndex((Comparable) x);
		this.intervalDelegate.itemAdded(series, item);

		if (notify) {
			fireDatasetChanged();
		}
	}

	/**
	 * Removes a value from the dataset.
	 * 
	 * @param x
	 *           the x-value.
	 * @param seriesName
	 *           the series name.
	 */
	public void remove(final double x, final String seriesName) {
		remove(new Double(x), seriesName, true);
	}

	/**
	 * Removes an item from the dataset.
	 * 
	 * @param x
	 *           the x-value.
	 * @param seriesName
	 *           the series name.
	 * @param notify
	 *           notify listeners?
	 */
	public void remove(final Number x, final String seriesName, final boolean notify) {
		this.values.removeValue((Comparable) x, seriesName);

		this.intervalDelegate.itemRemoved(x.doubleValue());

		if (notify) {
			fireDatasetChanged();
		}
	}

	/**
	 * Returns the number of series in the collection.
	 * 
	 * @return the number of series in the collection.
	 */
	public int getSeriesCount() {
		return this.values.getColumnCount();
	}

	/**
	 * Returns the name of a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the name of a series.
	 */
	public String getSeriesName(final int series) {
		return this.values.getColumnKey(series).toString();
	}

	/**
	 * Returns the number of x values in the dataset.
	 * 
	 * @return the number of x values in the dataset.
	 */
	public int getItemCount() {
		return this.values.getRowCount();
	}

	/**
	 * Returns the number of items in the specified series.
	 * Returns the same as {@link CategoryTableXYDataset#getItemCount()}.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the number of items in the specified series.
	 */
	public int getItemCount(final int series) {
		return getItemCount();
	}

	/**
	 * Returns the x-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the x-value for the specified series and item.
	 */
	public Number getXValue(final int series, final int item) {
		return (Number) this.values.getRowKey(item);
	}

	/**
	 * Returns the starting X value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The starting X value.
	 */
	public Number getStartXValue(final int series, final int item) {
		return this.intervalDelegate.getStartXValue(series, item);
	}

	/**
	 * Returns the ending X value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The ending X value.
	 */
	public Number getEndXValue(final int series, final int item) {
		return this.intervalDelegate.getEndXValue(series, item);
	}

	/**
	 * Returns the y-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the index of the item of interest (zero-based).
	 * @return the y-value for the specified series and item (possibly <code>null</code>).
	 */
	public Number getYValue(final int series, final int item) {
		return this.values.getValue(item, series);
	}

	/**
	 * Returns the starting Y value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The starting Y value.
	 */
	public Number getStartYValue(final int series, final int item) {
		return getYValue(series, item);
	}

	/**
	 * Returns the ending Y value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The ending Y value.
	 */
	public Number getEndYValue(final int series, final int item) {
		return getYValue(series, item);
	}

	/**
	 * @return the domain range
	 */
	public Range getDomainRange() {
		return this.intervalDelegate.getDomainRange();
	}

	/**
	 * @return the maximum domain value.
	 */
	public Number getMaximumDomainValue() {
		return this.intervalDelegate.getMaximumDomainValue();
	}

	/**
	 * @return the minimum domain value.
	 */
	public Number getMinimumDomainValue() {
		return this.intervalDelegate.getMinimumDomainValue();
	}

	/**
	 * Returns the interval position factor.
	 * 
	 * @return the interval position factor.
	 */
	public double getIntervalPositionFactor() {
		return this.intervalDelegate.getIntervalPositionFactor();
	}

	/**
	 * Sets the interval position factor. Must be between 0.0 and 1.0 inclusive.
	 * If the factor is 0.5, the gap is in the middle of the x values. If it
	 * is lesser than 0.5, the gap is farther to the left and if greater than
	 * 0.5 it gets farther to the right.
	 * 
	 * @param d
	 *           the new interval position factor.
	 */
	public void setIntervalPositionFactor(final double d) {
		this.intervalDelegate.setIntervalPositionFactor(d);
		fireDatasetChanged();
	}

	/**
	 * returns the full interval width.
	 * 
	 * @return the interval width to use.
	 */
	public double getIntervalWidth() {
		return this.intervalDelegate.getIntervalWidth();
	}

	/**
	 * Sets the interval width manually.
	 * 
	 * @param d
	 *           the new interval width.
	 */
	public void setIntervalWidth(final double d) {
		this.intervalDelegate.setIntervalWidth(d);
		fireDatasetChanged();
	}

	/**
	 * Returns wether the interval width is automatically calculated or not.
	 * 
	 * @return wether the width is automatically calcualted or not.
	 */
	public boolean isAutoWidth() {
		return this.intervalDelegate.isAutoWidth();
	}

	/**
	 * Sets the flag that indicates wether the interval width is automatically
	 * calculated or not.
	 * 
	 * @param b
	 *           the flag.
	 */
	public void setAutoWidth(final boolean b) {
		this.intervalDelegate.setAutoWidth(b);
		fireDatasetChanged();
	}
}
