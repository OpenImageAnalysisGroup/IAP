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
 * -----------------
 * OHLCDataItem.java
 * -----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: OHLCDataItem.java,v 1.1 2011-01-31 09:02:18 klukas Exp $
 * Changes
 * -------
 * 03-Dec-2003 : Version 1 (DG);
 */

package org.jfree.data;

import java.util.Date;

/**
 * Represents a single (open-high-low-close) data item in an {@link OHLCDataset}. This data item
 * is commonly used to summarise the trading activity of a financial commodity for a fixed period
 * (most often one day).
 */
public class OHLCDataItem implements Comparable {

	/** The date. */
	private Date date;

	/** The open value. */
	private Number open;

	/** The high value. */
	private Number high;

	/** The low value. */
	private Number low;

	/** The close value. */
	private Number close;

	/** The trading volume (number of shares, contracts or whatever). */
	private Number volume;

	/**
	 * Creates a new item.
	 * 
	 * @param date
	 *           the date.
	 * @param open
	 *           the open value.
	 * @param high
	 *           the high value.
	 * @param low
	 *           the low value.
	 * @param close
	 *           the close value.
	 * @param volume
	 *           the volume.
	 */
	public OHLCDataItem(final Date date,
								final double open,
								final double high,
								final double low,
								final double close,
								final double volume) {
		this.date = date;
		this.open = new Double(open);
		this.high = new Double(high);
		this.low = new Double(low);
		this.close = new Double(close);
		this.volume = new Double(volume);
	}

	/**
	 * Returns the date.
	 * 
	 * @return the date.
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * Returns the open value.
	 * 
	 * @return the open value.
	 */
	public Number getOpen() {
		return this.open;
	}

	/**
	 * Returns the high value.
	 * 
	 * @return the high value.
	 */
	public Number getHigh() {
		return this.high;
	}

	/**
	 * Returns the low value.
	 * 
	 * @return the low value.
	 */
	public Number getLow() {
		return this.low;
	}

	/**
	 * Returns the close value.
	 * 
	 * @return the close value.
	 */
	public Number getClose() {
		return this.close;
	}

	/**
	 * Returns the volume.
	 * 
	 * @return the volume.
	 */
	public Number getVolume() {
		return this.volume;
	}

	/**
	 * Compares this object with the specified object for order. Returns a negative integer, zero,
	 * or a positive integer as this object is less than, equal to, or greater than the specified
	 * object.
	 * 
	 * @param object
	 *           the object to compare to.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal
	 *         to, or greater than the specified object.
	 */
	public int compareTo(final Object object) {
		if (object instanceof OHLCDataItem) {
			final OHLCDataItem item = (OHLCDataItem) object;
			return this.date.compareTo(item.date);
		} else {
			throw new ClassCastException("OHLCDataItem.compareTo(...).");
		}
	}

}
