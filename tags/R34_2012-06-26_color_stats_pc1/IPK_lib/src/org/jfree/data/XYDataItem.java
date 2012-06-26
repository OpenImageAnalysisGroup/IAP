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
 * ---------------
 * XYDataItem.java
 * ---------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XYDataItem.java,v 1.1 2011-01-31 09:02:14 klukas Exp $
 * Changes
 * -------
 * 05-Aug-2003 : Renamed XYDataPair --> XYDataItem (DG);
 * 03-Feb-2004 : Fixed bug in equals() method (DG);
 */

package org.jfree.data;

import java.io.Serializable;

import org.jfree.util.ObjectUtils;

/**
 * Represents one (x, y) data item for an {@link XYSeries}.
 */
public class XYDataItem implements Cloneable, Comparable, Serializable {

	/** The x-value. */
	private Number x;

	/** The y-value. */
	private Number y;

	/**
	 * Constructs a new data item.
	 * 
	 * @param x
	 *           the x-value (<code>null</code> NOT permitted).
	 * @param y
	 *           the y-value (<code>null</code> permitted).
	 */
	public XYDataItem(final Number x, final Number y) {
		if (x == null) {
			throw new IllegalArgumentException("Null 'x' argument.");
		}
		this.x = x;
		this.y = y;
	}

	/**
	 * Constructs a new data pair.
	 * 
	 * @param x
	 *           the x-value.
	 * @param y
	 *           the y-value.
	 */
	public XYDataItem(final double x, final double y) {
		this(new Double(x), new Double(y));
	}

	/**
	 * Returns the x-value.
	 * 
	 * @return the x-value (never <code>null</code>).
	 */
	public Number getX() {
		return this.x;
	}

	/**
	 * Returns the y-value.
	 * 
	 * @return the y-value (possibly <code>null</code>).
	 */
	public Number getY() {
		return this.y;
	}

	/**
	 * Sets the y-value for this data pair.
	 * <P>
	 * Note that there is no corresponding method to change the x-value.
	 * 
	 * @param y
	 *           the new y-value (<code>null</code> permitted).
	 */
	public void setY(final Number y) {
		this.y = y;
	}

	/**
	 * Returns an integer indicating the order of this object relative to another object.
	 * <P>
	 * For the order we consider only the x-value: negative == "less-than", zero == "equal", positive == "greater-than".
	 * 
	 * @param o1
	 *           the object being compared to.
	 * @return an integer indicating the order of this data pair object
	 *         relative to another object.
	 */
	public int compareTo(final Object o1) {

		final int result;

		// CASE 1 : Comparing to another TimeSeriesDataPair object
		// -------------------------------------------------------
		if (o1 instanceof XYDataItem) {
			final XYDataItem dataItem = (XYDataItem) o1;
			final double compare = this.x.doubleValue() - dataItem.getX().doubleValue();
			if (compare > 0.0) {
				result = 1;
			} else {
				if (compare < 0.0) {
					result = -1;
				} else {
					result = 0;
				}
			}
		}

		// CASE 2 : Comparing to a general object
		// ---------------------------------------------
		else {
			// consider time periods to be ordered after general objects
			result = 1;
		}

		return result;

	}

	/**
	 * Returns a clone of this object.
	 * 
	 * @return a clone.
	 * @throws CloneNotSupportedException
	 *            not thrown by this class, but subclasses may differ.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Tests if this object is equal to another.
	 * 
	 * @param o
	 *           the object to test against for equality (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(final Object o) {

		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}

		if (o instanceof XYDataItem) {
			final XYDataItem item = (XYDataItem) o;
			if (!this.x.equals(item.x)) {
				return false;
			}
			if (!ObjectUtils.equal(this.y, item.y)) {
				return false;
			}

			return true;
		}

		return false;

	}

	/**
	 * Returns a hash code.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		int result;
		result = this.x.hashCode();
		result = 29 * result + (this.y != null ? this.y.hashCode() : 0);
		return result;
	}
}
