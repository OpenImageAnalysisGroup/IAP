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
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Aaron Metzger;
 * $Id: XYSeriesCollection.java,v 1.1 2011-01-31 09:02:12 klukas Exp $
 * Changes
 * -------
 * 15-Nov-2001 : Version 1 (DG);
 * 03-Apr-2002 : Added change listener code (DG);
 * 29-Apr-2002 : Added removeSeries, removeAllSeries methods (ARM);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 04-Aug-2003 : Added getSeries() method (DG);
 * 31-Mar-2004 : Modified to use an XYIntervalDelegate.
 * 05-May-2004 : Now extends AbstractIntervalXYDataset (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jfree.util.ObjectUtils;

/**
 * Represents a collection of {@link XYSeries} objects that can be used as a dataset.
 */
public class XYSeriesCollection extends AbstractIntervalXYDataset
											implements IntervalXYDataset, DomainInfo, Serializable {

	/** The series that are included in the collection. */
	private List data;

	/** The interval delegate (used to calculate the start and end x-values). */
	private IntervalXYDelegate intervalDelegate;

	/**
	 * Constructs an empty dataset.
	 */
	public XYSeriesCollection() {
		this(null);
	}

	/**
	 * Constructs a dataset and populates it with a single time series.
	 * 
	 * @param series
	 *           the time series (<code>null</code> ignored).
	 */
	public XYSeriesCollection(final XYSeries series) {
		this.data = new java.util.ArrayList();
		this.intervalDelegate = new IntervalXYDelegate(this, false);
		if (series != null) {
			this.data.add(series);
			series.addChangeListener(this);
		}
	}

	/**
	 * Returns the interval width. This is used to calculate the start and end x-values, if
	 * they are used.
	 * 
	 * @return The interval width.
	 */
	public double getIntervalWidth() {
		return this.intervalDelegate.getIntervalWidth();
	}

	/**
	 * Sets the interval width.
	 * 
	 * @param width
	 *           the width.
	 */
	public void setIntervalWidth(final double width) {
		this.intervalDelegate.setIntervalWidth(width);
		fireDatasetChanged();
	}

	/**
	 * Returns the interval position factor.
	 * 
	 * @return The interval position factor.
	 */
	public double getIntervalPositionFactor() {
		return this.intervalDelegate.getIntervalPositionFactor();
	}

	/**
	 * Sets the interval position factor. This controls where the x-value is in relation to
	 * the interval surrounding the x-value (0.0 means the x-value will be positioned at the start,
	 * 0.5 in the middle, and 1.0 at the end).
	 * 
	 * @param factor
	 *           the factor.
	 */
	public void setIntervalPositionFactor(final double factor) {
		this.intervalDelegate.setIntervalPositionFactor(factor);
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
	 *           a boolean.
	 */
	public void setAutoWidth(final boolean b) {
		this.intervalDelegate.setAutoWidth(b);
		fireDatasetChanged();
	}

	/**
	 * Adds a series to the collection and sends a {@link DatasetChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param series
	 *           the series (<code>null</code> not permitted).
	 */
	public void addSeries(final XYSeries series) {

		// check arguments...
		if (series == null) {
			throw new IllegalArgumentException(
								"XYSeriesCollection.addSeries(...): cannot add null series.");
		}

		// add the series...
		this.data.add(series);
		this.intervalDelegate.seriesAdded(this.data.size() - 1);
		series.addChangeListener(this);
		fireDatasetChanged();

	}

	/**
	 * Returns the number of series in the collection.
	 * 
	 * @return The series count.
	 */
	public int getSeriesCount() {
		return this.data.size();
	}

	/**
	 * Returns a list of all the series in the collection.
	 * 
	 * @return The list (which is unmodifiable).
	 */
	public List getSeries() {
		return Collections.unmodifiableList(this.data);
	}

	/**
	 * Returns a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The series.
	 */
	public XYSeries getSeries(final int series) {

		// check arguments...
		if ((series < 0) || (series > getSeriesCount())) {
			throw new IllegalArgumentException(
								"XYSeriesCollection.getSeries(...): index outside valid range.");
		}

		// fetch the series...
		final XYSeries ts = (XYSeries) this.data.get(series);
		return ts;

	}

	/**
	 * Returns the name of a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the name of a series.
	 */
	public String getSeriesName(final int series) {

		// check arguments...delegated

		// fetch the result...
		return getSeries(series).getName();

	}

	/**
	 * Returns the number of items in the specified series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the number of items in the specified series.
	 */
	public int getItemCount(final int series) {

		// check arguments...delegated

		// fetch the result...
		return getSeries(series).getItemCount();

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

		final XYSeries ts = (XYSeries) this.data.get(series);
		final XYDataItem xyItem = ts.getDataItem(item);
		return xyItem.getX();

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
	 * @param index
	 *           the index of the item of interest (zero-based).
	 * @return the y-value for the specified series and item.
	 */
	public Number getYValue(final int series, final int index) {

		final XYSeries ts = (XYSeries) this.data.get(series);
		final XYDataItem xyItem = ts.getDataItem(index);
		return xyItem.getY();

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
	 * Removes all the series from the collection.
	 * <P>
	 * Notifies all registered listeners that the dataset has changed.
	 */
	public void removeAllSeries() {

		// Unregister the collection as a change listener to each series in the collection.
		for (int i = 0; i < this.data.size(); i++) {
			final XYSeries series = (XYSeries) this.data.get(i);
			series.removeChangeListener(this);
		}

		// Remove all the series from the collection and notify listeners.
		this.data.clear();
		this.intervalDelegate.seriesRemoved();
		fireDatasetChanged();
	}

	/**
	 * Removes a series from the collection.
	 * <P>
	 * Notifies all registered listeners that the dataset has changed.
	 * 
	 * @param series
	 *           the series.
	 */
	public void removeSeries(final XYSeries series) {

		// check arguments...
		if (series == null) {
			throw new IllegalArgumentException(
								"XYSeriesCollection.removeSeries(...): cannot remove null series.");
		}

		// remove the series...
		if (this.data.contains(series)) {
			series.removeChangeListener(this);
			this.data.remove(series);
			this.intervalDelegate.seriesRemoved();
			fireDatasetChanged();
		}

	}

	/**
	 * Removes a series from the collection.
	 * <P>
	 * Notifies all registered listeners that the dataset has changed.
	 * 
	 * @param series
	 *           the series (zero based index).
	 */
	public void removeSeries(final int series) {

		// check arguments...
		if ((series < 0) || (series > getSeriesCount())) {
			throw new IllegalArgumentException(
								"XYSeriesCollection.removeSeries(...): index outside valid range.");
		}

		// fetch the series, remove the change listener, then remove the series.
		final XYSeries ts = (XYSeries) this.data.get(series);
		ts.removeChangeListener(this);
		this.data.remove(series);
		this.intervalDelegate.seriesRemoved();
		fireDatasetChanged();

	}

	/**
	 * Tests this collection for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object.
	 * @return A boolean.
	 */
	public boolean equals(final Object obj) {
		/*
		 * XXX
		 * what about the interval delegate...?
		 * The interval width etc wasn't considered
		 * before, hence i did not add it here (AS)
		 */

		/*
		 * you doesn't have to check this because it's ensured by the
		 * instanceof check. I prefer using
		 * if (obj == this)
		 * return true;
		 * if (!(obj instanceof MyClass)) // includes null check.
		 * return false;
		 * MyClass that = (MyClass) obj;
		 * ...
		 * (AS)
		 */
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof XYSeriesCollection) {
			final XYSeriesCollection c = (XYSeriesCollection) obj;
			return ObjectUtils.equal(this.data, c.data);
		}

		return false;
	}

	/**
	 * Returns a hash code.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		// Same question as for equals (AS)
		return (this.data != null ? this.data.hashCode() : 0);
	}

	/**
	 * Returns the range of the dataset on the domain.
	 * 
	 * @return the range of the domain.
	 */
	public Range getDomainRange() {
		return this.intervalDelegate.getDomainRange();
	}

	/**
	 * Returns the maximum value of the dataset on the domain.
	 * 
	 * @return the maxiumum value on the domain.
	 */
	public Number getMaximumDomainValue() {
		return this.intervalDelegate.getMaximumDomainValue();
	}

	/**
	 * Returns the minimum value of the dataset on the domain.
	 * 
	 * @return the minimum value on the domain.
	 */
	public Number getMinimumDomainValue() {
		return this.intervalDelegate.getMinimumDomainValue();
	}
}
