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
 * --------------------------
 * DefaultHighLowDataset.java
 * --------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DefaultHighLowDataset.java,v 1.1 2011-01-31 09:02:15 klukas Exp $
 * Changes
 * -------
 * 21-Mar-2002 : Version 1 (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-May-2004 : Now extends AbstractXYDataset and added new methods from
 * HighLowDataset (DG);
 */

package org.jfree.data;

import java.util.Date;

/**
 * A simple implementation of the {@link HighLowDataset}.
 */
public class DefaultHighLowDataset extends AbstractXYDataset implements HighLowDataset {

	/** The series name. */
	private String seriesName;

	/** Storage for the dates. */
	private Date[] date;

	/** Storage for the high values. */
	private Number[] high;

	/** Storage for the low values. */
	private Number[] low;

	/** Storage for the open values. */
	private Number[] open;

	/** Storage for the close values. */
	private Number[] close;

	/** Storage for the volume values. */
	private Number[] volume;

	/**
	 * Constructs a new high/low/open/close dataset.
	 * <p>
	 * The current implementation allows only one series in the dataset. This may be extended in a future version.
	 * 
	 * @param seriesName
	 *           the name of the series.
	 * @param date
	 *           the dates.
	 * @param high
	 *           the high values.
	 * @param low
	 *           the low values.
	 * @param open
	 *           the open values.
	 * @param close
	 *           the close values.
	 * @param volume
	 *           the volume values.
	 */
	public DefaultHighLowDataset(final String seriesName,
											final Date[] date,
											final double[] high, final double[] low,
											final double[] open, final double[] close,
											final double[] volume) {

		this.seriesName = seriesName;
		this.date = date;
		this.high = createNumberArray(high);
		this.low = createNumberArray(low);
		this.open = createNumberArray(open);
		this.close = createNumberArray(close);
		this.volume = createNumberArray(volume);

	}

	/**
	 * Returns the name of the series stored in this dataset.
	 * 
	 * @param i
	 *           the index of the series. Currently ignored.
	 * @return the name of this series.
	 */
	public String getSeriesName(final int i) {
		return this.seriesName;
	}

	/**
	 * Returns the x-value for one item in a series.
	 * <p>
	 * The value returned is a Long object generated from the underlying Date object.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the x-value.
	 */
	public Number getXValue(final int series, final int item) {
		return new Long(this.date[item].getTime());
	}

	/**
	 * Returns the x-value for one item in a series, as a Date.
	 * <p>
	 * This method is provided for convenience only.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the x-value as a Date.
	 */
	public Date getXDate(final int series, final int item) {
		return this.date[item];
	}

	/**
	 * Returns the y-value for one item in a series.
	 * <p>
	 * This method (from the XYDataset interface) is mapped to the getCloseValue(...) method.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the y-value.
	 */
	public Number getYValue(final int series, final int item) {
		return getCloseValue(series, item);
	}

	/**
	 * Returns the high-value for one item in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the high-value.
	 */
	public Number getHighValue(final int series, final int item) {
		return this.high[item];
	}

	/**
	 * Returns the high-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The high-value.
	 */
	public double getHigh(int series, int item) {
		double result = Double.NaN;
		Number high = getHighValue(series, item);
		if (high != null) {
			result = high.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the low-value for one item in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the low-value.
	 */
	public Number getLowValue(final int series, final int item) {
		return this.low[item];
	}

	/**
	 * Returns the low-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The low-value.
	 */
	public double getLow(int series, int item) {
		double result = Double.NaN;
		Number low = getLowValue(series, item);
		if (low != null) {
			result = low.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the open-value for one item in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the open-value.
	 */
	public Number getOpenValue(final int series, final int item) {
		return this.open[item];
	}

	/**
	 * Returns the open-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The open-value.
	 */
	public double getOpen(int series, int item) {
		double result = Double.NaN;
		Number open = getOpenValue(series, item);
		if (open != null) {
			result = open.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the close-value for one item in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the close-value.
	 */
	public Number getCloseValue(final int series, final int item) {
		return this.close[item];
	}

	/**
	 * Returns the close-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The close-value.
	 */
	public double getClose(int series, int item) {
		double result = Double.NaN;
		Number close = getCloseValue(series, item);
		if (close != null) {
			result = close.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the volume-value for one item in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the volume-value.
	 */
	public Number getVolumeValue(final int series, final int item) {
		return this.volume[item];
	}

	/**
	 * Returns the volume-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The volume-value.
	 */
	public double getVolume(int series, int item) {
		double result = Double.NaN;
		Number volume = getVolumeValue(series, item);
		if (volume != null) {
			result = volume.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the number of series in the dataset.
	 * <p>
	 * This implementation only allows one series.
	 * 
	 * @return the number of series.
	 */
	public int getSeriesCount() {
		return 1;
	}

	/**
	 * Returns the number of items in the specified series.
	 * 
	 * @param series
	 *           the index (zero-based) of the series.
	 * @return the number of items in the specified series.
	 */
	public int getItemCount(final int series) {
		return this.date.length;
	}

	/**
	 * Constructs an array of Number objects from an array of doubles.
	 * 
	 * @param data
	 *           the double values to convert.
	 * @return data as array of Number.
	 */
	public static Number[] createNumberArray(final double[] data) {

		final Number[] result = new Number[data.length];

		for (int i = 0; i < data.length; i++) {
			result[i] = new Double(data[i]);
		}

		return result;

	}

}
