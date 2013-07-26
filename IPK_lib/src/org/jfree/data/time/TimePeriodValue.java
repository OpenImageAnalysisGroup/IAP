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
 * --------------------
 * TimePeriodValue.java
 * --------------------
 * (C) Copyright 2003, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimePeriodValue.java,v 1.1 2011-01-31 09:03:01 klukas Exp $
 * Changes
 * -------
 * 22-Apr-2003 : Version 1 (DG);
 */

package org.jfree.data.time;

import java.io.Serializable;

/**
 * Represents a time period and an associated value.
 */
public class TimePeriodValue implements Cloneable, Serializable {

	/** The time period. */
	private TimePeriod period;

	/** The value associated with the time period. */
	private Number value;

	/**
	 * Constructs a new data item.
	 * 
	 * @param period
	 *           the time period.
	 * @param value
	 *           the value associated with the time period.
	 */
	public TimePeriodValue(final TimePeriod period, final Number value) {

		this.period = period;
		this.value = value;

	}

	/**
	 * Constructs a new data pair.
	 * 
	 * @param period
	 *           the time period.
	 * @param value
	 *           the value associated with the time period.
	 */
	public TimePeriodValue(final TimePeriod period, final double value) {

		this(period, new Double(value));

	}

	/**
	 * Returns the time period.
	 * 
	 * @return the time period.
	 */
	public TimePeriod getPeriod() {
		return this.period;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value.
	 */
	public Number getValue() {
		return this.value;
	}

	/**
	 * Sets the value for this data item.
	 * 
	 * @param value
	 *           the new value.
	 */
	public void setValue(final Number value) {
		this.value = value;
	}

	/**
	 * Tests this object for equality with the target object.
	 * 
	 * @param o
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TimePeriodValue)) {
			return false;
		}

		final TimePeriodValue timePeriodValue = (TimePeriodValue) o;

		if (this.period != null ? !this.period.equals(timePeriodValue.period)
							: timePeriodValue.period != null) {
			return false;
		}
		if (this.value != null ? !this.value.equals(timePeriodValue.value)
							: timePeriodValue.value != null) {
			return false;
		}

		return true;
	}

	/**
	 * Returns a hash code value for the object.
	 * 
	 * @return the hashcode
	 */
	public int hashCode() {
		int result;
		result = (this.period != null ? this.period.hashCode() : 0);
		result = 29 * result + (this.value != null ? this.value.hashCode() : 0);
		return result;
	}

	/**
	 * Clones the data pair.
	 * <P>
	 * Notes: --> no need to clone the period or value since they are immutable classes.
	 * 
	 * @return a clone of this data pair.
	 */
	public Object clone() {

		Object clone = null;

		try {
			clone = super.clone();
		} catch (CloneNotSupportedException e) { // won't get here...
			System.err.println("TimePeriodValue.clone(): operation not supported.");
		}

		return clone;

	}

}
