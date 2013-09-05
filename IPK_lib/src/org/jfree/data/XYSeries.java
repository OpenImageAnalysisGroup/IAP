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
 * -------------
 * XYSeries.java
 * -------------
 * (C) Copyright 2001-2004, Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Aaron Metzger;
 * Jonathan Gabbai;
 * Richard Atkinson;
 * Michel Santos;
 * $Id: XYSeries.java,v 1.1 2011-01-31 09:02:17 klukas Exp $
 * Changes
 * -------
 * 15-Nov-2001 : Version 1 (DG);
 * 03-Apr-2002 : Added an add(double, double) method (DG);
 * 29-Apr-2002 : Added a clear() method (ARM);
 * 06-Jun-2002 : Updated Javadoc comments (DG);
 * 29-Aug-2002 : Modified to give user control over whether or not duplicate x-values are
 * allowed (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 11-Nov-2002 : Added maximum item count, code contributed by Jonathan Gabbai (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 04-Aug-2003 : Added getItems() method (DG);
 * 15-Aug-2003 : Changed 'data' from private to protected, added new add(...) methods with a
 * 'notify' argument (DG);
 * 22-Sep-2003 : Added getAllowDuplicateXValues() method (RA);
 * 29-Jan-2004 : Added autoSort attribute, based on a contribution by Michel Santos - see
 * patch 886740 (DG);
 * 03-Feb-2004 : Added indexOf() method (DG);
 * 16-Feb-2004 : Added remove() method (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jfree.util.ObjectUtils;

/**
 * Represents a sequence of zero or more data items in the form (x, y). Items in the series will
 * be sorted into ascending order by X-value, and duplicate X-values are permitted. Both the
 * sorting and duplicate defaults can be changed in the constructor. Y-values can be <code>null</code> to represent missing values.
 */
public class XYSeries extends Series implements Cloneable, Serializable {

	/** Storage for the data items in the series. */
	protected List data;

	// In version 0.9.12, in response to several developer requests, I changed the 'data' attribute
	// from 'private' to 'protected', so that others can make subclasses that work directly with
	// the underlying data structure.

	/** The maximum number of items for the series. */
	private int maximumItemCount = Integer.MAX_VALUE;

	/** A flag that controls whether the items are automatically sorted. */
	private boolean autoSort;

	/** A flag that controls whether or not duplicate x-values are allowed. */
	private boolean allowDuplicateXValues;

	/**
	 * Creates a new empty series. By default, items added to the series will be sorted into
	 * ascending order by x-value, and duplicate x-values will be allowed (these defaults can be
	 * modified with another constructor.
	 * 
	 * @param name
	 *           the series name (<code>null</code> not permitted).
	 */
	public XYSeries(final String name) {
		this(name, true, true);
	}

	/**
	 * Constructs a new xy-series that contains no data. You can specify whether or not
	 * duplicate x-values are allowed for the series.
	 * 
	 * @param name
	 *           the series name (<code>null</code> not permitted).
	 * @param autoSort
	 *           a flag that controls whether or not the items in the series are sorted.
	 * @param allowDuplicateXValues
	 *           a flag that controls whether duplicate x-values are allowed.
	 */
	public XYSeries(final String name,
							final boolean autoSort,
							final boolean allowDuplicateXValues) {
		super(name);
		this.data = new java.util.ArrayList();
		this.autoSort = autoSort;
		this.allowDuplicateXValues = allowDuplicateXValues;
	}

	/**
	 * Returns the flag that controls whether the items in the series are automatically sorted.
	 * There is no setter for this flag, it must be defined in the series constructor.
	 * 
	 * @return a boolean.
	 */
	public boolean getAutoSort() {
		return this.autoSort;
	}

	/**
	 * Returns a flag that controls whether duplicate x-values are allowed. This flag can only
	 * be set in the constructor.
	 * 
	 * @return a boolean.
	 */
	public boolean getAllowDuplicateXValues() {
		return this.allowDuplicateXValues;
	}

	/**
	 * Returns the number of items in the series.
	 * 
	 * @return the item count.
	 */
	public int getItemCount() {
		return this.data.size();
	}

	/**
	 * Returns the list of data items for the series (the list contains {@link XYDataItem} objects and is unmodifiable).
	 * 
	 * @return the list of data items.
	 */
	public List getItems() {
		return Collections.unmodifiableList(this.data);
	}

	/**
	 * Returns the maximum number of items that will be retained in the series.
	 * <P>
	 * The default value is <code>Integer.MAX_VALUE</code>).
	 * 
	 * @return the maximum item count.
	 */
	public int getMaximumItemCount() {
		return this.maximumItemCount;
	}

	/**
	 * Sets the maximum number of items that will be retained in the series.
	 * <P>
	 * If you add a new item to the series such that the number of items will exceed the maximum item count, then the FIRST element in the series is
	 * automatically removed, ensuring that the maximum item count is not exceeded.
	 * 
	 * @param maximum
	 *           the maximum.
	 */
	public void setMaximumItemCount(final int maximum) {
		this.maximumItemCount = maximum;
	}

	/**
	 * Adds a data item to the series and sends a {@link SeriesChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param item
	 *           the (x, y) item (<code>null</code> not permitted).
	 */
	public void add(final XYDataItem item) {
		// argument checking delegated...
		add(item, true);
	}

	/**
	 * Adds a data item to the series and, if requested, sends a {@link SeriesChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param item
	 *           the (x, y) item (<code>null</code> not permitted).
	 * @param notify
	 *           a flag that controls whether or not a {@link SeriesChangeEvent} is sent to
	 *           all registered listeners.
	 */
	public void add(final XYDataItem item, final boolean notify) {

		if (item == null) {
			throw new IllegalArgumentException("Null 'item' argument.");
		}

		final int index = Collections.binarySearch(this.data, item);
		if (index < 0) {
			if (this.autoSort) {
				this.data.add(-index - 1, item);
			} else {
				this.data.add(item);
			}

			if (getItemCount() > this.maximumItemCount) {
				this.data.remove(0);
			}
		} else {
			if (this.allowDuplicateXValues) {
				if (this.autoSort) {
					this.data.add(index, item);
				} else {
					this.data.add(item);
				}
				if (getItemCount() > this.maximumItemCount) {
					this.data.remove(0);
				}
			} else {
				throw new SeriesException("XYSeries.add(...): x-value already exists.");
			}
		}

		if (notify) {
			fireSeriesChanged();
		}

	}

	/**
	 * Adds a data item to the series and sends a {@link SeriesChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param x
	 *           the x value.
	 * @param y
	 *           the y value.
	 */
	public void add(final double x, final double y) {
		add(new Double(x), new Double(y), true);
	}

	/**
	 * Adds a data item to the series and, if requested, sends a {@link SeriesChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param x
	 *           the x value.
	 * @param y
	 *           the y value.
	 * @param notify
	 *           a flag that controls whether or not a {@link SeriesChangeEvent} is sent to
	 *           all registered listeners.
	 */
	public void add(final double x, final double y, final boolean notify) {
		add(new Double(x), new Double(y), notify);
	}

	/**
	 * Adds a data item to the series and sends a {@link SeriesChangeEvent} to all registered
	 * listeners.
	 * <P>
	 * The unusual pairing of parameter types is to make it easier to add <code>null</code> y-values.
	 * 
	 * @param x
	 *           the x value.
	 * @param y
	 *           the y value (<code>null</code> permitted).
	 */
	public void add(final double x, final Number y) {
		add(new Double(x), y);
	}

	/**
	 * Adds a data item to the series and, if requested, sends a {@link SeriesChangeEvent} to
	 * all registered listeners.
	 * <P>
	 * The unusual pairing of parameter types is to make it easier to add null y-values.
	 * 
	 * @param x
	 *           the x value.
	 * @param y
	 *           the y value (<code>null</code> permitted).
	 * @param notify
	 *           a flag that controls whether or not a {@link SeriesChangeEvent} is sent to
	 *           all registered listeners.
	 */
	public void add(final double x, final Number y, final boolean notify) {
		add(new Double(x), y, notify);
	}

	/**
	 * Adds new data to the series and sends a {@link SeriesChangeEvent} to all registered
	 * listeners.
	 * <P>
	 * Throws an exception if the x-value is a duplicate AND the allowDuplicateXValues flag is false.
	 * 
	 * @param x
	 *           the x-value (<code>null</code> not permitted).
	 * @param y
	 *           the y-value (<code>null</code> permitted).
	 */
	public void add(final Number x, final Number y) {
		// argument checking delegated...
		add(x, y, true);
	}

	/**
	 * Adds new data to the series and, if requested, sends a {@link SeriesChangeEvent} to all
	 * registered listeners.
	 * <P>
	 * Throws an exception if the x-value is a duplicate AND the allowDuplicateXValues flag is false.
	 * 
	 * @param x
	 *           the x-value (<code>null</code> not permitted).
	 * @param y
	 *           the y-value (<code>null</code> permitted).
	 * @param notify
	 *           a flag the controls whether or not a {@link SeriesChangeEvent} is sent to
	 *           all registered listeners.
	 */
	public void add(final Number x, final Number y, final boolean notify) {
		if (x == null) {
			throw new IllegalArgumentException("Null 'x' argument.");
		}
		final XYDataItem item = new XYDataItem(x, y);
		add(item, notify);
	}

	/**
	 * Deletes a range of items from the series and sends a {@link SeriesChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param start
	 *           the start index (zero-based).
	 * @param end
	 *           the end index (zero-based).
	 */
	public void delete(final int start, final int end) {
		for (int i = start; i <= end; i++) {
			this.data.remove(start);
		}
		fireSeriesChanged();
	}

	/**
	 * Removes the item at the specified index.
	 * 
	 * @param index
	 *           the index.
	 * @return the item removed.
	 */
	public XYDataItem remove(final int index) {
		return (XYDataItem) this.data.remove(index);
	}

	/**
	 * Removes the item(s) with the specified x-value.
	 * 
	 * @param x
	 *           the x-value.
	 * @return the item removed.
	 */
	public XYDataItem remove(final Number x) {
		return remove(indexOf(x));
	}

	/**
	 * Removes all data items from the series.
	 */
	public void clear() {
		if (this.data.size() > 0) {
			this.data.clear();
			fireSeriesChanged();
		}
	}

	/**
	 * Return the data item with the specified index.
	 * 
	 * @param index
	 *           the index.
	 * @return the data item with the specified index.
	 */
	public XYDataItem getDataItem(final int index) {
		return (XYDataItem) this.data.get(index);
	}

	/**
	 * Returns the x-value at the specified index.
	 * 
	 * @param index
	 *           the index (zero-based).
	 * @return the x-value (never <code>null</code>).
	 */
	public Number getXValue(final int index) {
		return getDataItem(index).getX();
	}

	/**
	 * Returns the y-value at the specified index.
	 * 
	 * @param index
	 *           the index (zero-based).
	 * @return the y-value (possibly <code>null</code>).
	 */
	public Number getYValue(final int index) {
		return getDataItem(index).getY();
	}

	/**
	 * Updates the value of an item in the series and sends a {@link SeriesChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param index
	 *           the item (zero based index).
	 * @param y
	 *           the new value (<code>null</code> permitted).
	 */
	public void update(final int index, final Number y) {
		final XYDataItem item = getDataItem(index);
		item.setY(y);
		fireSeriesChanged();
	}

	/**
	 * Returns the index of the item with the specified x-value. Note: if the series is not sorted
	 * in order of ascending x-values, the result is undefined.
	 * 
	 * @param x
	 *           the x-value (<code>null</code> not permitted).
	 * @return the index.
	 */
	public int indexOf(final Number x) {
		return Collections.binarySearch(this.data, new XYDataItem(x, null));
	}

	/**
	 * Returns a clone of the series.
	 * 
	 * @return a clone of the time series.
	 * @throws CloneNotSupportedException
	 *            if there is a cloning problem.
	 */
	public Object clone() throws CloneNotSupportedException {
		final Object clone = createCopy(0, getItemCount() - 1);
		return clone;
	}

	/**
	 * Creates a new series by copying a subset of the data in this time series.
	 * 
	 * @param start
	 *           the index of the first item to copy.
	 * @param end
	 *           the index of the last item to copy.
	 * @return a series containing a copy of this series from start until end.
	 * @throws CloneNotSupportedException
	 *            if there is a cloning problem.
	 */
	public XYSeries createCopy(final int start, final int end) throws CloneNotSupportedException {

		final XYSeries copy = (XYSeries) super.clone();

		copy.data = new java.util.ArrayList();
		if (this.data.size() > 0) {
			for (int index = start; index <= end; index++) {
				final XYDataItem item = (XYDataItem) this.data.get(index);
				final XYDataItem clone = (XYDataItem) item.clone();
				try {
					copy.add(clone);
				} catch (SeriesException e) {
					System.err.println("XYSeries.createCopy(): unable to add cloned data pair.");
				}
			}
		}

		return copy;

	}

	/**
	 * Tests this series for equality with an arbitrary object.
	 * 
	 * @param object
	 *           the object to test against for equality (<code>null</code> permitted).
	 * @return a boolean.
	 */
	public boolean equals(final Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (!super.equals(object)) {
			return false;
		}
		if (!(object instanceof XYSeries)) {
			return false;
		}
		final XYSeries s = (XYSeries) object;
		if (this.maximumItemCount != s.maximumItemCount) {
			return false;
		}
		if (this.autoSort != s.autoSort) {
			return false;
		}
		if (this.allowDuplicateXValues != s.allowDuplicateXValues) {
			return false;
		}
		if (!ObjectUtils.equal(this.data, s.data)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a hash code.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		int result = super.hashCode();
		result = 29 * result + (this.data != null ? this.data.hashCode() : 0);
		result = 29 * result + this.maximumItemCount;
		result = 29 * result + (this.autoSort ? 1 : 0);
		result = 29 * result + (this.allowDuplicateXValues ? 1 : 0);
		return result;
	}

	// // DEPRECATED METHODS ///////////////////////////////////////////////////////////////////////

	/**
	 * Constructs a new xy-series that contains no data. You can specify whether or not
	 * duplicate x-values are allowed for the series.
	 * 
	 * @param name
	 *           the series name.
	 * @param allowDuplicateXValues
	 *           a flag that controls whether duplicate x-values are allowed.
	 * @deprecated Use a XYSeries(String, boolean, boolean) instead.
	 */
	public XYSeries(final String name, final boolean allowDuplicateXValues) {
		this(name, true, allowDuplicateXValues);
	}

	/**
	 * Return the data pair with the specified index.
	 * 
	 * @param index
	 *           the index.
	 * @return the data pair with the specified index.
	 * @deprecated Use getDataItem(index).
	 */
	public XYDataPair getDataPair(final int index) {
		return (XYDataPair) this.data.get(index);
	}

}
