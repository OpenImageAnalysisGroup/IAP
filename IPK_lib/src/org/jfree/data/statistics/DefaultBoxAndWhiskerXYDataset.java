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
 * ----------------------------------
 * DefaultBoxAndWhiskerXYDataset.java
 * ----------------------------------
 * (C) Copyright 2003, by David Browning and Contributors.
 * Original Author: David Browning (for Australian Institute of Marine Science);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultBoxAndWhiskerXYDataset.java,v 1.1 2011-01-31 09:02:05 klukas Exp $
 * Changes
 * -------
 * 05-Aug-2003 : Version 1, contributed by David Browning (DG);
 * 08-Aug-2003 : Minor changes to comments (DB)
 * Allow average to be null - average is a perculiar AIMS requirement
 * which probably should be stripped out and overlaid if required...
 * Added a number of methods to allow the max and min non-outlier and
 * non-farout values to be calculated
 * 12-Aug-2003 Changed the getYValue to return the highest outlier value
 * Added getters and setters for outlier and farout coefficients
 * 27-Aug-2003 : Renamed DefaultBoxAndWhiskerDataset --> DefaultBoxAndWhiskerXYDataset (DG);
 * 06-May-2004 : Now extends AbstractXYDataset (DG);
 */

package org.jfree.data.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.data.AbstractXYDataset;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;

/**
 * A simple implementation of the {@link BoxAndWhiskerXYDataset}.
 * 
 * @author David Browning
 */
public class DefaultBoxAndWhiskerXYDataset extends AbstractXYDataset
															implements BoxAndWhiskerXYDataset,
																		RangeInfo {

	/** The series name. */
	private String seriesName;

	/** Storage for the dates. */
	private List dates;

	/** Storage for the box and whisker statistics. */
	private List items;

	/** The minimum range value. */
	private Number minimumRangeValue;

	/** The maximum range value. */
	private Number maximumRangeValue;

	/** The range of values. */
	private Range valueRange;

	/**
	 * The coefficient used to calculate outliers. Tukey's default value is 1.5 (see EDA)
	 * Any value which is greater than Q3 + (interquartile range * outlier coefficient) is
	 * considered to be an outlier.
	 * Can be altered if the data is particularly skewed.
	 */
	private double outlierCoefficient = 1.5;

	/**
	 * The coefficient used to calculate farouts. Tukey's default value is 2 (see EDA)
	 * Any value which is greater than Q3 + (interquartile range * farout coefficient) is
	 * considered to be a farout.
	 * Can be altered if the data is particularly skewed.
	 */
	private double faroutCoefficient = 2.0;

	/**
	 * Constructs a new box and whisker dataset.
	 * <p>
	 * The current implementation allows only one series in the dataset. This may be extended in a future version.
	 * 
	 * @param seriesName
	 *           the name of the series.
	 */
	public DefaultBoxAndWhiskerXYDataset(final String seriesName) {

		this.seriesName = seriesName;
		this.dates = new ArrayList();
		this.items = new ArrayList();
		this.minimumRangeValue = null;
		this.maximumRangeValue = null;
		this.valueRange = null;

	}

	/**
	 * Adds an item to the dataset.
	 * 
	 * @param date
	 *           the date.
	 * @param item
	 *           the item.
	 */
	public void add(final Date date, final BoxAndWhiskerItem item) {
		this.dates.add(date);
		this.items.add(item);
		if (this.minimumRangeValue == null) {
			this.minimumRangeValue = item.getMinRegularValue();
		} else {
			if (item.getMinRegularValue().doubleValue() < this.minimumRangeValue.doubleValue()) {
				this.minimumRangeValue = item.getMinRegularValue();
			}
		}
		if (this.maximumRangeValue == null) {
			this.maximumRangeValue = item.getMaxRegularValue();
		} else {
			if (item.getMaxRegularValue().doubleValue() > this.maximumRangeValue.doubleValue()) {
				this.maximumRangeValue = item.getMaxRegularValue();
			}
		}
		this.valueRange = new Range(this.minimumRangeValue.doubleValue(),
												this.maximumRangeValue.doubleValue());
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
		return new Long(((Date) this.dates.get(item)).getTime());
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
		return (Date) this.dates.get(item);
	}

	/**
	 * Returns the y-value for one item in a series.
	 * <p>
	 * This method (from the XYDataset interface) is mapped to the getMaxNonOutlierValue(...) method.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the y-value.
	 */
	public Number getYValue(final int series, final int item) {
		return new Double(this.getMeanValue(series, item).doubleValue());
	}

	/**
	 * Returns the mean for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the mean for the specified series and item.
	 */
	public Number getMeanValue(final int series, final int item) {
		Number result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getMean();
		}
		return result;
	}

	/**
	 * Returns the median-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the median-value for the specified series and item.
	 */
	public Number getMedianValue(final int series, final int item) {
		Number result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getMedian();
		}
		return result;
	}

	/**
	 * Returns the Q1 median-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the Q1 median-value for the specified series and item.
	 */
	public Number getQ1Value(final int series, final int item) {
		Number result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getQ1();
		}
		return result;
	}

	/**
	 * Returns the Q3 median-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the Q3 median-value for the specified series and item.
	 */
	public Number getQ3Value(final int series, final int item) {
		Number result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getQ3();
		}
		return result;
	}

	/**
	 * Returns the min-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the min-value for the specified series and item.
	 */
	public Number getMinRegularValue(final int series, final int item) {
		Number result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getMinRegularValue();
		}
		return result;
	}

	/**
	 * Returns the max-value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the max-value for the specified series and item.
	 */
	public Number getMaxRegularValue(final int series, final int item) {
		Number result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getMaxRegularValue();
		}
		return result;
	}

	/**
	 * Returns the minimum value which is not a farout.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return a <code>Number</code> representing the maximum non-farout value.
	 */
	public Number getMinOutlier(final int series, final int item) {
		Number result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getMinOutlier();
		}
		return result;
	}

	/**
	 * Returns the maximum value which is not a farout, ie Q3 + (interquartile range * farout
	 * coefficient).
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return a <code>Number</code> representing the maximum non-farout value.
	 */
	public Number getMaxOutlier(final int series, final int item) {
		Number result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getMaxOutlier();
		}
		return result;
	}

	/**
	 * Returns an array of outliers for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the array of outliers for the specified series and item.
	 */
	public List getOutliers(final int series, final int item) {
		List result = null;
		final BoxAndWhiskerItem stats = (BoxAndWhiskerItem) this.items.get(item);
		if (stats != null) {
			result = stats.getOutliers();
		}
		return result;
	}

	/**
	 * Returns the value used as the outlier coefficient. The outlier coefficient
	 * gives an indication of the degree of certainty in an unskewed distribution.
	 * Increasing the coefficient increases the number of values included.
	 * Currently only used to ensure farout coefficient is greater than the outlier coefficient
	 * 
	 * @return a <code>double</code> representing the value used to calculate outliers
	 */
	public double getOutlierCoefficient() {
		return this.outlierCoefficient;
	}

	/**
	 * Returns the value used as the farout coefficient. The farout coefficient
	 * allows the calculation of which values will be off the graph.
	 * 
	 * @return a <code>double</code> representing the value used to calculate farouts
	 */
	public double getFaroutCoefficient() {
		return this.faroutCoefficient;
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
		return this.dates.size();
	}

	/**
	 * Sets the value used as the outlier coefficient
	 * 
	 * @param outlierCoefficient
	 *           being a <code>double</code> representing the value used to
	 *           calculate outliers
	 */
	public void setOutlierCoefficient(final double outlierCoefficient) {
		this.outlierCoefficient = outlierCoefficient;
	}

	/**
	 * Sets the value used as the farouts coefficient. The farout coefficient must b greater than
	 * the outlier coefficient.
	 * 
	 * @param faroutCoefficient
	 *           being a <code>double</code> representing the value used to
	 *           calculate farouts
	 */
	public void setFaroutCoefficient(final double faroutCoefficient) {

		if (faroutCoefficient > this.getOutlierCoefficient()) {
			this.faroutCoefficient = faroutCoefficient;
		} else {
			throw new IllegalArgumentException("Farout value must be greater "
								+ "than the outlier value, which is currently set at: ("
								+ getOutlierCoefficient() + ")");
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

}
