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
 * --------------------------------
 * DynamicTimeSeriesCollection.java
 * --------------------------------
 * (C) Copyright 2002, 2003, by I. H. Thomae and Contributors.
 * Original Author: I. H. Thomae (ithomae@ists.dartmouth.edu);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DynamicTimeSeriesCollection.java,v 1.1 2011-01-31 09:03:01 klukas Exp $
 * Changes
 * -------
 * 22-Nov-2002 : Initial version completed
 * Jan 2003 : Optimized advanceTime(), added implemnt'n of RangeInfo intfc
 * (using cached values for min, max, and range); also added
 * getOldestIndex() and getNewestIndex() ftns so client classes
 * can use this class as the master "index authority".
 * 22-Jan-2003 : Made this class stand on its own, rather than extending
 * class FastTimeSeriesCollection
 * 31-Jan-2003 : Changed TimePeriod --> RegularTimePeriod (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package (DG);
 * 29-Apr-2003 : Added small change to appendData method, from Irv Thomae (DG);
 * 19-Sep-2003 : Added new appendData method, from Irv Thomae (DG);
 * 05-May-2004 : Now extends AbstractIntervalXYDataset. This also required a
 * change to the return type of the getY() method - I'm slightly
 * unsure of the implications of this, so it might require some
 * further amendment (DG);
 */

package org.jfree.data.time;

import java.util.Calendar;
import java.util.TimeZone;

import org.jfree.data.AbstractIntervalXYDataset;
import org.jfree.data.DomainInfo;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.SeriesChangeEvent;

/**
 * A dynamic dataset.
 * <p>
 * Like FastTimeSeriesCollection, this class is a functional replacement for JFreeChart's TimeSeriesCollection _and_ TimeSeries classes.
 * FastTimeSeriesCollection is appropriate for a fixed time range; for real-time applications this subclass adds the ability to append new data and discard the
 * oldest. In this class, the arrays used in FastTimeSeriesCollection become FIFO's. NOTE:As presented here, all data is assumed >= 0, an assumption which is
 * embodied only in methods associated with interface RangeInfo.
 * 
 * @author Irv Thomae.
 */
public class DynamicTimeSeriesCollection extends AbstractIntervalXYDataset
														implements IntervalXYDataset,
																		DomainInfo,
																		RangeInfo {

	/** Useful constant for controlling the x-value returned for a time period. */
	public static final int START = 0;

	/** Useful constant for controlling the x-value returned for a time period. */
	public static final int MIDDLE = 1;

	/** Useful constant for controlling the x-value returned for a time period. */
	public static final int END = 2;

	/** The maximum number of items for each series (can be overridden). */
	private int maximumItemCount = 2000; // an arbitrary safe default value

	/** The history count. */
	protected int historyCount;

	/** Storage for the series names. */
	private String[] seriesNames;

	/** The time period class - barely used, and could be removed (DG). */
	private Class timePeriodClass = Minute.class; // default value;

	/** Storage for the x-values. */
	protected RegularTimePeriod[] pointsInTime;

	/** The number of series. */
	private int seriesCount;

	/**
	 * A wrapper for a fixed array of float values.
	 */
	protected class ValueSequence {

		/** Storage for the float values. */
		float[] dataPoints;

		/**
		 * Default constructor:
		 */
		public ValueSequence() {
			this(DynamicTimeSeriesCollection.this.maximumItemCount);
		}

		/**
		 * Creates a sequence with the specified length.
		 * 
		 * @param length
		 *           the length.
		 */
		public ValueSequence(final int length) {
			this.dataPoints = new float[length];
			for (int i = 0; i < length; i++) {
				this.dataPoints[i] = 0.0f;
			}
		}

		/**
		 * Enters data into the storage array.
		 * 
		 * @param index
		 *           the index.
		 * @param value
		 *           the value.
		 */
		public void enterData(final int index, final float value) {
			this.dataPoints[index] = value;
		}

		/**
		 * Returns a value from the storage array.
		 * 
		 * @param index
		 *           the index.
		 * @return The value.
		 */
		public float getData(final int index) {
			return this.dataPoints[index];
		}
	}

	/** An array for storing the objects that represent each series. */
	protected ValueSequence[] valueHistory;

	/** A working calendar (to recycle) */
	protected Calendar workingCalendar;

	/** The position within a time period to return as the x-value (START, MIDDLE or END). */
	private int position;

	/**
	 * A flag that indicates that the domain is 'points in time'. If this flag is true, only
	 * the x-value is used to determine the range of values in the domain, the start and end
	 * x-values are ignored.
	 */
	private boolean domainIsPointsInTime;

	/** index for mapping: points to the oldest valid time & data. */
	private int oldestAt; // as a class variable, initializes == 0

	/** Index of the newest data item. */
	private int newestAt;

	// cached values used for interface DomainInfo:

	/** the # of msec by which time advances. */
	private long deltaTime;

	/** Cached domain start (for use by DomainInfo). */
	private Long domainStart;

	/** Cached domain end (for use by DomainInfo). */
	private Long domainEnd;

	/** Cached domain range (for use by DomainInfo). */
	private Range domainRange;

	// Cached values used for interface RangeInfo: (note minValue pinned at 0)
	// A single set of extrema covers the entire SeriesCollection

	/** The minimum value. */
	private Float minValue = new Float(0.0f);

	/** The maximum value. */
	private Float maxValue = null;

	/** The value range. */
	private Range valueRange; // autoinit's to null.

	/**
	 * Constructs a dataset with capacity for N series, tied to default timezone.
	 * 
	 * @param nSeries
	 *           the number of series to be accommodated.
	 * @param nMoments
	 *           the number of TimePeriods to be spanned.
	 */
	public DynamicTimeSeriesCollection(final int nSeries, final int nMoments) {

		this(nSeries, nMoments, new Millisecond(), TimeZone.getDefault());
		this.newestAt = nMoments - 1;

	}

	/**
	 * Constructs an empty dataset, tied to a specific timezone.
	 * 
	 * @param nSeries
	 *           the number of series to be accommodated
	 * @param nMoments
	 *           the number of TimePeriods to be spanned
	 * @param zone
	 *           the timezone.
	 */
	public DynamicTimeSeriesCollection(final int nSeries, final int nMoments, final TimeZone zone) {
		this(nSeries, nMoments, new Millisecond(), zone);
		this.newestAt = nMoments - 1;
	}

	/**
	 * Creates a new dataset.
	 * 
	 * @param nSeries
	 *           the number of series.
	 * @param nMoments
	 *           the number of items per series.
	 * @param timeSample
	 *           a time period sample.
	 */
	public DynamicTimeSeriesCollection(final int nSeries,
													final int nMoments,
													final RegularTimePeriod timeSample) {
		this(nSeries, nMoments, timeSample, TimeZone.getDefault());
	}

	/**
	 * Creates a new dataset.
	 * 
	 * @param nSeries
	 *           the number of series.
	 * @param nMoments
	 *           the number of items per series.
	 * @param timeSample
	 *           a time period sample.
	 * @param zone
	 *           the time zone.
	 */
	public DynamicTimeSeriesCollection(final int nSeries,
													final int nMoments,
													final RegularTimePeriod timeSample,
													final TimeZone zone) {

		// the first initialization must precede creation of the ValueSet array:
		this.maximumItemCount = nMoments; // establishes length of each array
		this.historyCount = nMoments;
		this.seriesNames = new String[nSeries];
		// initialize the members of "seriesNames" array so they won't be null:
		for (int i = 0; i < nSeries; i++) {
			this.seriesNames[i] = "";
		}
		this.newestAt = nMoments - 1;
		this.valueHistory = new ValueSequence[nSeries];
		this.timePeriodClass = timeSample.getClass();

		// / Expand the following for all defined TimePeriods:
		if (this.timePeriodClass == Second.class) {
			this.pointsInTime = new Second[nMoments];
		} else
			if (this.timePeriodClass == Minute.class) {
				this.pointsInTime = new Minute[nMoments];
			} else
				if (this.timePeriodClass == Hour.class) {
					this.pointsInTime = new Hour[nMoments];
				}
		// / .. etc....
		this.workingCalendar = Calendar.getInstance(zone);
		this.position = START;
		this.domainIsPointsInTime = true;
	}

	/**
	 * Fill the pointsInTime with times using TimePeriod.next():
	 * Will silently return if the time array was already populated.
	 * Also computes the data cached for later use by
	 * methods implementing the DomainInfo interface:
	 * 
	 * @param start
	 *           the start.
	 * @return ??.
	 */
	public synchronized long setTimeBase(final RegularTimePeriod start) {

		if (this.pointsInTime[0] == null) {
			this.pointsInTime[0] = start;
			for (int i = 1; i < this.historyCount; i++) {
				this.pointsInTime[i] = this.pointsInTime[i - 1].next();
			}
		}
		final long oldestL = this.pointsInTime[0].getFirstMillisecond(this.workingCalendar);
		final long nextL = this.pointsInTime[1].getFirstMillisecond(this.workingCalendar);
		this.deltaTime = nextL - oldestL;
		this.oldestAt = 0;
		this.newestAt = this.historyCount - 1;
		findDomainLimits();
		return this.deltaTime;

	}

	/**
	 * Finds the domain limits.
	 * <p>
	 * Note: this doesn't need to be synchronized because it's called from within another method that already is.
	 */
	protected void findDomainLimits() {

		final long startL = getOldestTime().getFirstMillisecond(this.workingCalendar);
		final long endL;
		if (this.domainIsPointsInTime) {
			endL = getNewestTime().getFirstMillisecond(this.workingCalendar);
		} else {
			endL = getNewestTime().getLastMillisecond(this.workingCalendar);
		}
		this.domainStart = new Long(startL);
		this.domainEnd = new Long(endL);
		this.domainRange = new Range(startL, endL);

	}

	/**
	 * Returns the x position type (START, MIDDLE or END).
	 * 
	 * @return The x position type.
	 */
	public int getPosition() {
		return this.position;
	}

	/**
	 * Sets the x position type (START, MIDDLE or END).
	 * 
	 * @param position
	 *           The x position type.
	 */
	public void setPosition(final int position) {
		this.position = position;
	}

	/**
	 * Adds a series to the dataset. Only the y-values are supplied, the x-values are specified
	 * elsewhere.
	 * 
	 * @param values
	 *           the y-values.
	 * @param seriesNumber
	 *           the series index (zero-based).
	 * @param seriesName
	 *           the seriesName.
	 *           Use this as-is during setup only, or add the synchronized keyword around the copy loop.
	 */
	public void addSeries(final float[] values,
									final int seriesNumber, final String seriesName) {

		invalidateRangeInfo();
		int i;
		if (values == null) {
			throw new IllegalArgumentException("TimeSeriesDataset.addSeries(...): "
								+ "cannot add null array of values.");
		}
		if (seriesNumber >= this.valueHistory.length) {
			throw new IllegalArgumentException("TimeSeriesDataset.addSeries(...): "
								+ "cannot add more series than specified in c'tor");
		}
		if (this.valueHistory[seriesNumber] == null) {
			this.valueHistory[seriesNumber] = new ValueSequence(this.historyCount);
			this.seriesCount++;
		} // But if that series array already exists, just overwrite its contents

		// Avoid IndexOutOfBoundsException:
		final int srcLength = values.length;
		int copyLength = this.historyCount;
		boolean fillNeeded = false;
		if (srcLength < this.historyCount) {
			fillNeeded = true;
			copyLength = srcLength;
		}
		// {
		for (i = 0; i < copyLength; i++) { // deep copy from values[], caller can safely discard
			// that array
			this.valueHistory[seriesNumber].enterData(i, values[i]);
		}
		if (fillNeeded) {
			for (i = copyLength; i < this.historyCount; i++) {
				this.valueHistory[seriesNumber].enterData(i, 0.0f);
			}
		}
		// }
		if (seriesName != null) {
			this.seriesNames[seriesNumber] = seriesName;
		}
		fireSeriesChanged();

	}

	/**
	 * Sets the name of a series.
	 * <p>
	 * If planning to add values individually.
	 * 
	 * @param seriesNumber
	 *           the series.
	 * @param newName
	 *           the new name.
	 */
	public void setSeriesName(final int seriesNumber, final String newName) {
		this.seriesNames[seriesNumber] = newName;
	}

	/**
	 * Adds a value to a series.
	 * 
	 * @param seriesNumber
	 *           the series index.
	 * @param index
	 *           ??.
	 * @param value
	 *           the value.
	 */
	public void addValue(final int seriesNumber, final int index, final float value) {

		invalidateRangeInfo();
		if (seriesNumber >= this.valueHistory.length) {
			throw new IllegalArgumentException("TimeSeriesDataset.addValue(...): series #"
								+ seriesNumber + "unspecified in c'tor");
		}
		if (this.valueHistory[seriesNumber] == null) {
			this.valueHistory[seriesNumber] = new ValueSequence(this.historyCount);
			this.seriesCount++;
		} // But if that series array already exists, just overwrite its contents
		// synchronized(this)
		// {
		this.valueHistory[seriesNumber].enterData(index, value);
		// }
		fireSeriesChanged();
	}

	/**
	 * Returns the number of series in the collection.
	 * 
	 * @return the series count.
	 */
	public int getSeriesCount() {
		return this.seriesCount;
	}

	/**
	 * Returns the number of items in a series.
	 * <p>
	 * For this implementation, all series have the same number of items.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return The item count.
	 */
	public int getItemCount(final int series) { // all arrays equal length, so ignore argument:
		return this.historyCount;
	}

	// Methods for managing the FIFO's:

	/**
	 * Re-map an index, for use in retrieving data.
	 * 
	 * @param toFetch
	 *           the index.
	 * @return The translated index.
	 */
	protected int translateGet(final int toFetch) {
		if (this.oldestAt == 0) {
			return toFetch; // no translation needed
		}
		// else [implicit here]
		int newIndex = toFetch + this.oldestAt;
		if (newIndex >= this.historyCount) {
			newIndex -= this.historyCount;
		}
		return newIndex;
	}

	/**
	 * Returns the actual index to a time offset by "delta" from newestAt.
	 * 
	 * @param delta
	 *           the delta.
	 * @return The offset.
	 */
	public int offsetFromNewest(final int delta) {
		return wrapOffset(this.newestAt + delta);
	}

	/**
	 * ??
	 * 
	 * @param delta
	 *           ??
	 * @return The offset.
	 */
	public int offsetFromOldest(final int delta) {
		return wrapOffset(this.oldestAt + delta);
	}

	/**
	 * ??
	 * 
	 * @param protoIndex
	 *           the index.
	 * @return The offset.
	 */
	protected int wrapOffset(final int protoIndex) {
		int tmp = protoIndex;
		if (tmp >= this.historyCount) {
			tmp -= this.historyCount;
		} else
			if (tmp < 0) {
				tmp += this.historyCount;
			}
		return tmp;
	}

	/**
	 * Adjust the array offset as needed when a new time-period is added:
	 * Increments the indices "oldestAt" and "newestAt", mod(array length),
	 * zeroes the series values at newestAt, returns the new TimePeriod.
	 * 
	 * @return The new time period.
	 */
	public synchronized RegularTimePeriod advanceTime() {
		final RegularTimePeriod nextInstant = this.pointsInTime[this.newestAt].next();
		this.newestAt = this.oldestAt; // newestAt takes value previously held by oldestAT
		/*** The next 10 lines or so should be expanded if data can be negative ***/
		// if the oldest data contained a maximum Y-value, invalidate the stored
		// Y-max and Y-range data:
		boolean extremaChanged = false;
		float oldMax = 0.0f;
		if (this.maxValue != null) {
			oldMax = this.maxValue.floatValue();
		}
		for (int s = 0; s < getSeriesCount(); s++) {
			if (this.valueHistory[s].getData(this.oldestAt) == oldMax) {
				extremaChanged = true;
			}
			if (extremaChanged) {
				break;
			}
		}
		/*** If data can be < 0, add code here to check the minimum **/
		if (extremaChanged) {
			invalidateRangeInfo();
		}
		// wipe the next (about to be used) set of data slots
		final float wiper = (float) 0.0;
		for (int s = 0; s < getSeriesCount(); s++) {
			this.valueHistory[s].enterData(this.newestAt, wiper);
		}
		// Update the array of TimePeriods:
		this.pointsInTime[this.newestAt] = nextInstant;
		// Now advance "oldestAt", wrapping at end of the array
		this.oldestAt++;
		if (this.oldestAt >= this.historyCount) {
			this.oldestAt = 0;
		}
		// Update the domain limits:
		final long startL = this.domainStart.longValue(); // (time is kept in msec)
		this.domainStart = new Long(startL + this.deltaTime);
		final long endL = this.domainEnd.longValue();
		this.domainEnd = new Long(endL + this.deltaTime);
		this.domainRange = new Range(startL, endL);
		fireSeriesChanged();
		return nextInstant;
	}

	// If data can be < 0, the next 2 methods should be modified

	/**
	 * Invalidates the range info.
	 */
	public void invalidateRangeInfo() {
		this.maxValue = null;
		this.valueRange = null;
	}

	/**
	 * Returns the maximum value.
	 * 
	 * @return The maximum value.
	 */
	protected double findMaxValue() {
		double max = 0.0f;
		for (int s = 0; s < getSeriesCount(); s++) {
			for (int i = 0; i < this.historyCount; i++) {
				final double tmp = getY(s, i);
				if (tmp > max) {
					max = tmp;
				}
			}
		}
		return max;
	}

	/** End, positive-data-only code **/

	/**
	 * Returns the index of the oldest data item.
	 * 
	 * @return The index.
	 */
	public int getOldestIndex() {
		return this.oldestAt;
	}

	/**
	 * Returns the index of the newest data item.
	 * 
	 * @return The index.
	 */
	public int getNewestIndex() {
		return this.newestAt;
	}

	// appendData() writes new data at the index position given by newestAt/
	// When adding new data dynamically, use advanceTime(), followed by this:
	/**
	 * Appends new data.
	 * 
	 * @param newData
	 *           the data.
	 */
	public void appendData(final float[] newData) {
		final int nDataPoints = newData.length;
		if (nDataPoints > this.valueHistory.length) {
			throw new IllegalArgumentException(
								"DynamicTimeSeriesCollection.appendData(...): more data than series to put them in");
		}
		int s; // index to select the "series"
		for (s = 0; s < nDataPoints; s++) {
			// check whether the "valueHistory" array member exists; if not, create them:
			if (this.valueHistory[s] == null) {
				this.valueHistory[s] = new ValueSequence(this.historyCount);
			}
			this.valueHistory[s].enterData(this.newestAt, newData[s]);
		}
		fireSeriesChanged();
	}

	/**
	 * Appends data at specified index, for loading up with data from file(s).
	 * 
	 * @param newData
	 *           the data
	 * @param insertionIndex
	 *           the index value at which to put it
	 * @param refresh
	 *           value of n in "refresh the display on every nth call"
	 *           (ignored if <= 0 )
	 */
	public void appendData(final float[] newData, int insertionIndex, final int refresh) {
		final int nDataPoints = newData.length;
		if (nDataPoints > this.valueHistory.length) {
			throw new IllegalArgumentException(
								"DynamicTimeSeriesCollection.appendData(...): more data than series to put them "
													+ "in");
		}
		for (int s = 0; s < nDataPoints; s++) {
			if (this.valueHistory[s] == null) {
				this.valueHistory[s] = new ValueSequence(this.historyCount);
			}
			this.valueHistory[s].enterData(insertionIndex, newData[s]);
		}
		if (refresh > 0) {
			insertionIndex++;
			if (insertionIndex % refresh == 0) {
				fireSeriesChanged();
			}
		}
	}

	/**
	 * Returns the newest time.
	 * 
	 * @return The newest time.
	 */
	public RegularTimePeriod getNewestTime() {
		return this.pointsInTime[this.newestAt];
	}

	/**
	 * Returns the oldest time.
	 * 
	 * @return The oldest time.
	 */
	public RegularTimePeriod getOldestTime() {
		return this.pointsInTime[this.oldestAt];
	}

	/**
	 * Returns the x-value.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return The value.
	 */
	// getXxx() ftns can ignore the "series" argument:
	// Don't synchronize this!! Instead, synchronize the loop that calls it.
	public Number getXValue(final int series, final int item) {
		final RegularTimePeriod tp = this.pointsInTime[translateGet(item)];
		return new Long(getX(tp));
	}

	/**
	 * Returns the y-value.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return The value.
	 */
	public double getY(final int series, final int item) { // Don't synchronize this!!
		final ValueSequence values = this.valueHistory[series]; // Instead, synchronize the loop
		return values.getData(translateGet(item)); // that calls it.
	}

	/**
	 * Returns the y-value.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return The value.
	 */
	public Number getYValue(final int series, final int item) {
		return new Float(getY(series, item));
	}

	/**
	 * Returns the start x-value.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return The value.
	 */
	public Number getStartXValue(final int series, final int item) {
		final RegularTimePeriod tp = this.pointsInTime[translateGet(item)];
		return new Long(tp.getFirstMillisecond(this.workingCalendar));
	}

	/**
	 * Returns the end x-value.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return The value.
	 */
	public Number getEndXValue(final int series, final int item) {
		final RegularTimePeriod tp = this.pointsInTime[translateGet(item)];
		return new Long(tp.getLastMillisecond(this.workingCalendar));
	}

	/**
	 * Returns the start y-value.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return The value.
	 */
	public Number getStartYValue(final int series, final int item) {
		return getYValue(series, item);
	}

	/**
	 * Returns the end y-value.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return The value.
	 */
	public Number getEndYValue(final int series, final int item) {
		return getYValue(series, item);
	}

	/*
	 * // "Extras" found useful when analyzing/verifying class behavior:
	 * public Number getUntranslatedXValue(int series, int item)
	 * {
	 * return super.getXValue(series, item);
	 * }
	 * public float getUntranslatedY(int series, int item)
	 * {
	 * return super.getY(series, item);
	 * }
	 */

	/**
	 * Returns the name of a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return The name.
	 */
	public String getSeriesName(final int series) {
		return this.seriesNames[series];
	}

	/**
	 * Sends a {@link SeriesChangeEvent} to all registered listeners.
	 */
	protected void fireSeriesChanged() {
		seriesChanged(new SeriesChangeEvent(this));
	}

	// The next 3 functions override the base-class implementation of
	// the DomainInfo interface. Using saved limits (updated by
	// each updateTime() call), improves performance.
	//

	/**
	 * Returns the range of values for the domain.
	 * 
	 * @return The range.
	 */
	public Range getDomainRange() {
		if (this.domainRange == null) {
			findDomainLimits();
		}
		return this.domainRange;
	}

	/**
	 * Returns the minimum value in the dataset (or null if all the values in
	 * the domain are null).
	 * 
	 * @return the minimum value.
	 */
	public Number getMinimumDomainValue() {
		return this.domainStart; // a Long kept updated by advanceTime()
	}

	/**
	 * Returns the maximum value in the dataset (or null if all the values in
	 * the domain are null).
	 * 
	 * @return the maximum value.
	 */
	public Number getMaximumDomainValue() {
		return this.domainEnd; // a Long kept updated by advanceTime()
	}

	/**
	 * Returns the x-value for a time period.
	 * 
	 * @param period
	 *           the period.
	 * @return The x-value.
	 */
	private long getX(final RegularTimePeriod period) {
		switch (this.position) {
			case (START):
				return period.getFirstMillisecond(this.workingCalendar);
			case (MIDDLE):
				return period.getMiddleMillisecond(this.workingCalendar);
			case (END):
				return period.getLastMillisecond(this.workingCalendar);
			default:
				return period.getMiddleMillisecond(this.workingCalendar);
		}
	}

	// The next 3 functions implement the RangeInfo interface.
	// Using saved limits (updated by each updateTime() call) significantly
	// improves performance. WARNING: this code makes the simplifying assumption
	// that data is never negative. Expand as needed for the general case.

	/**
	 * Returns the minimum range value.
	 * 
	 * @return The minimum range value.
	 */
	public Number getMinimumRangeValue() {
		return this.minValue;
	}

	/**
	 * Returns the maximum range value.
	 * 
	 * @return The maximum range value.
	 */
	public Number getMaximumRangeValue() {
		if (this.maxValue == null) {
			this.maxValue = new Float(findMaxValue());
		}
		return this.maxValue;
	}

	/**
	 * Returns the value range.
	 * 
	 * @return The range.
	 */
	public Range getValueRange() {
		if (this.valueRange == null) {
			final Float maxV = (Float) getMaximumRangeValue();
			final double max = maxV.doubleValue();
			this.valueRange = new Range(0.0, max);
		}
		return this.valueRange;
	}
}
