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
 * ---------------------------
 * MatrixSeriesCollection.java
 * ---------------------------
 * (C) Copyright 2003, 2004, by Barak Naveh and Contributors.
 * Original Author: Barak Naveh;;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: MatrixSeriesCollection.java,v 1.1 2011-01-31 09:02:14 klukas Exp $
 * Changes
 * -------
 * 10-Jul-2003 : Version 1 contributed by Barak Naveh (DG);
 * 05-May-2004 : Now extends AbstractXYZDataset (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.List;

import org.jfree.util.ObjectUtils;

/**
 * Represents a collection of {@link MatrixSeries} that can be used as a dataset.
 * 
 * @author Barak Naveh
 * @see org.jfree.data.MatrixSeries
 */
public class MatrixSeriesCollection extends AbstractXYZDataset
												implements XYZDataset, Serializable {

	/** The series that are included in the collection. */
	private List seriesList;

	/**
	 * Constructs an empty dataset.
	 */
	public MatrixSeriesCollection() {
		this(null);
	}

	/**
	 * Constructs a dataset and populates it with a single matrix series.
	 * 
	 * @param series
	 *           the time series.
	 */
	public MatrixSeriesCollection(final MatrixSeries series) {
		this.seriesList = new java.util.ArrayList();

		if (series != null) {
			this.seriesList.add(series);
			series.addChangeListener(this);
		}
	}

	/**
	 * Returns the number of items in the specified series.
	 * 
	 * @param seriesIndex
	 *           zero-based series index.
	 * @return the number of items in the specified series.
	 */
	public int getItemCount(final int seriesIndex) {
		return getSeries(seriesIndex).getItemCount();
	}

	/**
	 * Returns the series having the specified index.
	 * 
	 * @param seriesIndex
	 *           zero-based series index.
	 * @return The series.
	 * @throws IllegalArgumentException
	 */
	public MatrixSeries getSeries(final int seriesIndex) {
		if ((seriesIndex < 0) || (seriesIndex > getSeriesCount())) {
			throw new IllegalArgumentException(
								"MatrixSeriesCollection.getSeries(...): index outside valid range.");
		}

		final MatrixSeries series = (MatrixSeries) this.seriesList.get(seriesIndex);

		return series;
	}

	/**
	 * Returns the number of series in the collection.
	 * 
	 * @return the number of series in the collection.
	 */
	public int getSeriesCount() {
		return this.seriesList.size();
	}

	/**
	 * Returns the name of a series.
	 * 
	 * @param seriesIndex
	 *           zero-based series index.
	 * @return the name of a series.
	 */
	public String getSeriesName(final int seriesIndex) {
		return getSeries(seriesIndex).getName();
	}

	/**
	 * Returns the j index value of the specified Mij matrix item in the
	 * specified matrix series.
	 * 
	 * @param seriesIndex
	 *           zero-based series index.
	 * @param itemIndex
	 *           zero-based item index.
	 * @return the j index value for the specified matrix item.
	 * @see org.jfree.data.XYDataset#getXValue(int, int)
	 */
	public Number getXValue(final int seriesIndex, final int itemIndex) {
		final MatrixSeries series = (MatrixSeries) this.seriesList.get(seriesIndex);
		final int x = series.getItemColumn(itemIndex);

		return new Integer(x); // I know it's bad to create object. better idea?
	}

	/**
	 * Returns the i index value of the specified Mij matrix item in the
	 * specified matrix series.
	 * 
	 * @param seriesIndex
	 *           zero-based series index.
	 * @param itemIndex
	 *           zero-based item index.
	 * @return the i index value for the specified matrix item.
	 * @see org.jfree.data.XYDataset#getYValue(int, int)
	 */
	public Number getYValue(final int seriesIndex, final int itemIndex) {
		final MatrixSeries series = (MatrixSeries) this.seriesList.get(seriesIndex);
		final int y = series.getItemRow(itemIndex);

		return new Integer(y); // I know it's bad to create object. better idea?
	}

	/**
	 * Returns the Mij item value of the specified Mij matrix item in the
	 * specified matrix series.
	 * 
	 * @param seriesIndex
	 *           the series (zero-based index).
	 * @param itemIndex
	 *           zero-based item index.
	 * @return the Mij item value for the specified matrix item.
	 * @see org.jfree.data.XYZDataset#getZValue(int, int)
	 */
	public Number getZValue(final int seriesIndex, final int itemIndex) {
		final MatrixSeries series = (MatrixSeries) this.seriesList.get(seriesIndex);
		final Number z = series.getItem(itemIndex);

		return z;
	}

	/**
	 * Adds a series to the collection.
	 * <P>
	 * Notifies all registered listeners that the dataset has changed.
	 * </p>
	 * 
	 * @param series
	 *           the series.
	 * @throws IllegalArgumentException
	 */
	public void addSeries(final MatrixSeries series) {
		// check arguments...
		if (series == null) {
			throw new IllegalArgumentException(
								"MatrixSeriesCollection.addSeries(...): cannot add null series.");
		}

		// add the series...
		this.seriesList.add(series);
		series.addChangeListener(this);
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
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof MatrixSeriesCollection) {
			final MatrixSeriesCollection c = (MatrixSeriesCollection) obj;

			return ObjectUtils.equal(this.seriesList, c.seriesList);
		}

		return false;
	}

	/**
	 * Returns a hash code.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		return (this.seriesList != null ? this.seriesList.hashCode() : 0);
	}

	/**
	 * Removes all the series from the collection.
	 * <P>
	 * Notifies all registered listeners that the dataset has changed.
	 * </p>
	 */
	public void removeAllSeries() {
		// Unregister the collection as a change listener to each series inmthe collection.
		for (int i = 0; i < this.seriesList.size(); i++) {
			final MatrixSeries series = (MatrixSeries) this.seriesList.get(i);
			series.removeChangeListener(this);
		}

		// Remove all the series from the collection and notify listeners.
		this.seriesList.clear();
		fireDatasetChanged();
	}

	/**
	 * Removes a series from the collection.
	 * <P>
	 * Notifies all registered listeners that the dataset has changed.
	 * </p>
	 * 
	 * @param series
	 *           the series.
	 * @throws IllegalArgumentException
	 */
	public void removeSeries(final MatrixSeries series) {
		// check arguments...
		if (series == null) {
			throw new IllegalArgumentException(
								"MatrixSeriesCollection.removeSeries(...): cannot remove null series.");
		}

		// remove the series...
		if (this.seriesList.contains(series)) {
			series.removeChangeListener(this);
			this.seriesList.remove(series);
			fireDatasetChanged();
		}
	}

	/**
	 * Removes a series from the collection.
	 * <P>
	 * Notifies all registered listeners that the dataset has changed.
	 * </p>
	 * 
	 * @param seriesIndex
	 *           the series (zero based index).
	 * @throws IllegalArgumentException
	 */
	public void removeSeries(final int seriesIndex) {
		// check arguments...
		if ((seriesIndex < 0) || (seriesIndex > getSeriesCount())) {
			throw new IllegalArgumentException(
								"MatrixSeriesCollection.removeSeries(...): index outside valid range.");
		}

		// fetch the series, remove the change listener, then remove the series.
		final MatrixSeries series = (MatrixSeries) this.seriesList.get(seriesIndex);
		series.removeChangeListener(this);
		this.seriesList.remove(seriesIndex);
		fireDatasetChanged();
	}

}
