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
 * -------------------------
 * SampleSIgnalDataset.java
 * ------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: SampleSignalDataset.java,v 1.1 2011-01-31 09:01:46 klukas Exp $
 * Changes (since 11-Oct-2002)
 * ---------------------------
 * 11-Oct-2002 : Added Javadocs;
 * 06-May-2004 : Now extends AbstractXYDataset (DG);
 */

package org.jfree.chart.demo;

import org.jfree.data.AbstractXYDataset;
import org.jfree.data.DatasetChangeListener;
import org.jfree.data.HighLowDataset;
import org.jfree.data.SignalsDataset;

/**
 * A sample signal dataset.
 */
public class SampleSignalDataset extends AbstractXYDataset implements SignalsDataset {

	/** The data. */
	private HighLowDataset data;

	/**
	 * Default constructor.
	 */
	public SampleSignalDataset() {
		this.data = DemoDatasetFactory.createHighLowDataset();
	}

	/**
	 * Returns the number of items in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the number of items within the series.
	 */
	public int getItemCount(final int series) {
		return this.data.getItemCount(series);
	}

	/**
	 * Returns the number of series in the dataset.
	 * 
	 * @return the series count.
	 */
	public int getSeriesCount() {
		return this.data.getSeriesCount();
	}

	/**
	 * Returns the name of a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the name of the series.
	 */
	public String getSeriesName(final int series) {
		return this.data.getSeriesName(series);
	}

	/**
	 * Returns the x-value for an item within a series.
	 * <P>
	 * The implementation is responsible for ensuring that the x-values are presented in ascending order.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the x-value.
	 */
	public Number getXValue(final int series, final int item) {
		return this.data.getXValue(series, item);
	}

	/**
	 * Returns the y-value for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the y-value.
	 */
	public Number getYValue(final int series, final int item) {
		return this.data.getYValue(series, item);
	}

	/**
	 * Returns the type.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the type.
	 */
	public int getType(final int series, final int item) {
		return SignalsDataset.ENTER_LONG;
	}

	/**
	 * Returns the level.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the level.
	 */
	public double getLevel(final int series, final int item) {
		return getXValue(series, item).doubleValue();
	}

	/**
	 * Registers an object to receive notification of changes to the dataset.
	 * 
	 * @param listener
	 *           the object to register.
	 */
	public void addChangeListener(final DatasetChangeListener listener) {
		this.data.addChangeListener(listener);
	}

	/**
	 * Deregisters an object so that it no longer receives notification of changes to the dataset.
	 * 
	 * @param listener
	 *           the object to deregister.
	 */
	public void removeChangeListener(final DatasetChangeListener listener) {
		this.data.removeChangeListener(listener);
	}

}
