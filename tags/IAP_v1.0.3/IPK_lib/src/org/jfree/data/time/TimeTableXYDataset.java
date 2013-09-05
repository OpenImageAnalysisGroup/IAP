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
 * TimeTableXYDataset.java
 * -----------------------
 * (C) Copyright 2004, by Andreas Schroeder and Contributors.
 * Original Author: Andreas Schroeder;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: TimeTableXYDataset.java,v 1.1 2011-01-31 09:03:01 klukas Exp $
 * Changes
 * -------
 * 01-Apr-2004 : Version 1 (AS);
 * 05-May-2004 : Now implements AbstractIntervalXYDataset (DG);
 */

package org.jfree.data.time;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.data.AbstractIntervalXYDataset;
import org.jfree.data.DefaultKeyedValues2D;
import org.jfree.data.DomainInfo;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.Range;
import org.jfree.data.TableXYDataset;

/**
 * A dataset for regular time periods that implements the TableXYDataset
 * interface.
 * 
 * @see org.jfree.data.TableXYDataset
 * @author andreas.schroeder
 */
public class TimeTableXYDataset extends AbstractIntervalXYDataset
											implements IntervalXYDataset,
															DomainInfo,
															TableXYDataset {

	/**
	 * The data structure to store the values.
	 */
	private DefaultKeyedValues2D values;

	/**
	 * A flag that indicates that the domain is 'points in time'. If this flag is true, only
	 * the x-value is used to determine the range of values in the domain, the start and end
	 * x-values are ignored.
	 */
	private boolean domainIsPointsInTime;

	/** A working calendar (to recycle) */
	private Calendar workingCalendar;

	/**
	 * The point within each time period that is used for the X value when this collection is used
	 * as an {@link org.jfree.data.XYDataset}. This can be the start, middle or end of the
	 * time period.
	 */
	private TimePeriodAnchor xPosition;

	/**
	 * Creates a new TimeTableDataset.
	 */
	public TimeTableXYDataset() {
		this(TimeZone.getDefault(), Locale.getDefault());
	}

	/**
	 * Creates a new TimeTableDataset with the given time zone.
	 * 
	 * @param zone
	 *           the time zone to use.
	 */
	public TimeTableXYDataset(final TimeZone zone) {
		this(zone, Locale.getDefault());
	}

	/**
	 * Creates a new TimeTableDataset with the given time zone and locale.
	 * 
	 * @param zone
	 *           the time zone to use.
	 * @param locale
	 *           the locale to use.
	 */
	public TimeTableXYDataset(final TimeZone zone, final Locale locale) {
		this.values = new DefaultKeyedValues2D(true);
		this.workingCalendar = Calendar.getInstance(zone, locale);
		this.xPosition = TimePeriodAnchor.START;
	}

	/**
	 * Adds a new data item to the dataset.
	 * 
	 * @param period
	 *           the time period.
	 * @param y
	 *           the value for this period.
	 * @param seriesName
	 *           the name of the series to add the value.
	 */
	public void add(final RegularTimePeriod period, final double y, final String seriesName) {
		add(period, new Double(y), seriesName, true);
	}

	/**
	 * Adds a new data item to the dataset.
	 * 
	 * @param period
	 *           the time period.
	 * @param y
	 *           the value for this period.
	 * @param seriesName
	 *           the name of the series to add the value.
	 * @param notify
	 *           wether dataset listener are notified or not.
	 */
	public void add(final RegularTimePeriod period, final Number y,
							final String seriesName, final boolean notify) {
		this.values.addValue(y, period, seriesName);

		if (notify) {
			fireDatasetChanged();
		}
	}

	/**
	 * Removes an existing data item from the dataset.
	 * 
	 * @param period
	 *           the (existing!) time period of the value to remove.
	 * @param seriesName
	 *           the (existing!) series name to remove the value.
	 */
	public void remove(final RegularTimePeriod period, final String seriesName) {
		remove(period, seriesName, true);
	}

	/**
	 * Removes an existing data item from the dataset.
	 * 
	 * @param period
	 *           the (existing!) time period of the value to remove.
	 * @param seriesName
	 *           the (existing!) series name to remove the value.
	 * @param notify
	 *           wether dataset listener are notified or not.
	 */
	public void remove(final RegularTimePeriod period, final String seriesName,
								final boolean notify) {
		this.values.removeValue(period, seriesName);

		if (notify) {
			fireDatasetChanged();
		}
	}

	/**
	 * Returns the number of items every series.
	 * 
	 * @return the item count.
	 */
	public int getItemCount() {
		return this.values.getRowCount();
	}

	/**
	 * Returns the number of items in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the number of items within the series.
	 */
	public int getItemCount(final int series) {
		return getItemCount();
	}

	/**
	 * Returns the number of series in the dataset.
	 * 
	 * @return the series count.
	 */
	public int getSeriesCount() {
		return this.values.getColumnCount();
	}

	/**
	 * Returns the name of a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return the name of the series.
	 */
	public String getSeriesName(final int series) {
		return this.values.getColumnKey(series).toString();
	}

	/**
	 * Returns the x-value for an item within a series. The x-values may or may not be returned
	 * in ascending order, that is up to the class implementing the interface.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the x-value.
	 */
	public Number getXValue(final int series, final int item) {
		final RegularTimePeriod period = (RegularTimePeriod) this.values.getRowKey(item);
		return new Long(getX(period));
	}

	/**
	 * Returns the starting X value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item within a series (zero-based index).
	 * @return the starting X value for the specified series and item.
	 */
	public Number getStartXValue(final int series, final int item) {
		final RegularTimePeriod period = (RegularTimePeriod) this.values.getRowKey(item);
		return new Long(period.getFirstMillisecond(this.workingCalendar));
	}

	/**
	 * Returns the ending X value for the specified series and item.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item within a series (zero-based index).
	 * @return the ending X value for the specified series and item.
	 */
	public Number getEndXValue(final int series, final int item) {
		final RegularTimePeriod period = (RegularTimePeriod) this.values.getRowKey(item);
		return new Long(period.getLastMillisecond(this.workingCalendar));
	}

	/**
	 * Returns the y-value for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return the y-value (possibly <code>null</code>).
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
	 *           the item within a series (zero-based index).
	 * @return starting Y value for the specified series and item.
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
	 *           the item within a series (zero-based index).
	 * @return the ending Y value for the specified series and item.
	 */
	public Number getEndYValue(final int series, final int item) {
		return getYValue(series, item);
	}

	/**
	 * Returns the x-value for a time period.
	 * 
	 * @param period
	 *           the time period.
	 * @return the x-value.
	 */
	private long getX(final RegularTimePeriod period) {

		long result = 0L;
		if (this.xPosition == TimePeriodAnchor.START) {
			result = period.getFirstMillisecond(this.workingCalendar);
		} else
			if (this.xPosition == TimePeriodAnchor.MIDDLE) {
				result = period.getMiddleMillisecond(this.workingCalendar);
			} else
				if (this.xPosition == TimePeriodAnchor.END) {
					result = period.getLastMillisecond(this.workingCalendar);
				}
		return result;
	}

	/**
	 * Returns the minimum value in the dataset (or <code>null</code> if all the values in
	 * the domain are <code>null</code>).
	 * 
	 * @return The minimum value.
	 */
	public Number getMinimumDomainValue() {
		return new Double(getDomainRange().getLowerBound());
	}

	/**
	 * Returns the maximum value in the dataset (or <code>null</code> if all the values in
	 * the domain are <code>null</code>).
	 * 
	 * @return The maximum value.
	 */
	public Number getMaximumDomainValue() {
		return new Double(getDomainRange().getUpperBound());
	}

	/**
	 * Returns the range of the values in this dataset's domain.
	 * 
	 * @return The range.
	 */
	public Range getDomainRange() {
		final List keys = this.values.getRowKeys();
		if (keys.isEmpty()) {
			return null;
		}

		final RegularTimePeriod first = (RegularTimePeriod) keys.get(0);
		final RegularTimePeriod last = (RegularTimePeriod) keys.get(keys.size() - 1);

		if (this.domainIsPointsInTime) {
			return new Range(getX(first), getX(last));
		} else {
			return new Range(first.getFirstMillisecond(this.workingCalendar),
								last.getLastMillisecond(this.workingCalendar));
		}
	}

	/**
	 * Returns a flag that controls whether the domain is treated as 'points in time'.
	 * <P>
	 * This flag is used when determining the max and min values for the domain. If true, then only the x-values are considered for the max and min values. If
	 * false, then the start and end x-values will also be taken into consideration
	 * 
	 * @return the flag.
	 */
	public boolean getDomainIsPointsInTime() {
		return this.domainIsPointsInTime;
	}

	/**
	 * Sets a flag that controls whether the domain is treated as 'points in time', or time
	 * periods.
	 * 
	 * @param flag
	 *           The new value of the flag.
	 */
	public void setDomainIsPointsInTime(final boolean flag) {
		this.domainIsPointsInTime = flag;
	}

}
