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
 * ----------
 * Range.java
 * ----------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Chuanhao Chiu;
 * Bill Kelemen;
 * Nicolas Brodu;
 * $Id: Range.java,v 1.1 2011-01-31 09:02:14 klukas Exp $
 * Changes (from 23-Jun-2001)
 * --------------------------
 * 22-Apr-2002 : Version 1, loosely based by code by Bill Kelemen (DG);
 * 30-Apr-2002 : Added getLength() and getCentralValue() methods. Changed argument check in
 * constructor (DG);
 * 13-Jun-2002 : Added contains(double) method (DG);
 * 22-Aug-2002 : Added fix to combine method where both ranges are null, thanks to Chuanhao Chiu
 * for reporting and fixing this (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 14-Aug-2003 : Added equals(...) method (DG);
 * 27-Aug-2003 : Added toString(...) method (BK);
 * 11-Sep-2003 : Added Clone Support (NB);
 * 23-Sep-2003 : Fixed Checkstyle issues (DG);
 * 25-Sep-2003 : Oops, Range immutable, clone not necessary (NB);
 * 05-May-2004 : Added constrain() and intersects() methods (DG);
 * 18-May-2004 : Added expand() method (DG);
 */

package org.jfree.data;

import java.io.Serializable;

/**
 * Represents an immutable range of values.
 */
public strictfp class Range implements Serializable {

	/** The lower bound of the range. */
	private double lower;

	/** The upper bound of the range. */
	private double upper;

	/**
	 * Creates a new range.
	 * 
	 * @param lower
	 *           the lower bound (must be <= upper bound).
	 * @param upper
	 *           the upper bound (must be >= lower bound).
	 */
	public Range(final double lower, final double upper) {
		if (lower > upper) {
			throw new IllegalArgumentException("Range(double, double): require lower<=upper.");
		}
		this.lower = lower;
		this.upper = upper;
	}

	/**
	 * Returns the lower bound for the range.
	 * 
	 * @return The lower bound.
	 */
	public double getLowerBound() {
		return this.lower;
	}

	/**
	 * Returns the upper bound for the range.
	 * 
	 * @return The upper bound.
	 */
	public double getUpperBound() {
		return this.upper;
	}

	/**
	 * Returns the length of the range.
	 * 
	 * @return The length.
	 */
	public double getLength() {
		return this.upper - this.lower;
	}

	/**
	 * Returns the central value for the range.
	 * 
	 * @return The central value.
	 */
	public double getCentralValue() {
		return this.lower / 2.0 + this.upper / 2.0;
	}

	/**
	 * Returns <code>true</code> if the range contains the specified value and <code>false</code> otherwise.
	 * 
	 * @param value
	 *           the value to lookup.
	 * @return <code>true</code> if the range contains the specified value.
	 */
	public boolean contains(final double value) {
		return (value >= this.lower && value <= this.upper);
	}

	/**
	 * Returns <code>true</code> if the range intersects with the specified range, and <code>false</code> otherwise.
	 * 
	 * @param b0
	 *           the lower bound.
	 * @param b1
	 *           the upper bound.
	 * @return A boolean.
	 */
	public boolean intersects(final double b0, final double b1) {
		if (b0 <= this.lower) {
			return (b1 > this.lower);
		} else {
			return (b0 < this.upper && b1 >= b0);
		}
	}

	/**
	 * Returns the value within the range that is closest to the specified value.
	 * 
	 * @param value
	 *           the value.
	 * @return The constrained value.
	 */
	public double constrain(final double value) {
		double result = value;
		if (!contains(value)) {
			if (value > this.upper) {
				result = this.upper;
			} else
				if (value < this.lower) {
					result = this.lower;
				}
		}
		return result;
	}

	/**
	 * Creates a new range by combining two existing ranges.
	 * <P>
	 * Note that:
	 * <ul>
	 * <li>either range can be <code>null</code>, in which case the other range is returned;</li>
	 * <li>if both ranges are <code>null</code> the return value is <code>null</code>.</li>
	 * </ul>
	 * 
	 * @param range1
	 *           the first range (<code>null</code> permitted).
	 * @param range2
	 *           the second range (<code>null</code> permitted).
	 * @return A new range (possibly <code>null</code>).
	 */
	public static Range combine(final Range range1, final Range range2) {
		if (range1 == null) {
			return range2;
		} else {
			if (range2 == null) {
				return range1;
			} else {
				final double l = Math.min(range1.getLowerBound(), range2.getLowerBound());
				final double u = Math.max(range1.getUpperBound(), range2.getUpperBound());
				return new Range(l, u);
			}
		}
	}

	/**
	 * Creates a new range by adding margins to an existing range.
	 * 
	 * @param range
	 *           the range (<code>null</code> not permitted).
	 * @param lowerMargin
	 *           the lower margin (expressed as a percentage of the range length).
	 * @param upperMargin
	 *           the upper margin (expressed as a percentage of the range length).
	 * @return The expanded range.
	 */
	public static Range expand(Range range, double lowerMargin, double upperMargin) {
		if (range == null) {
			throw new IllegalArgumentException("Null 'range' argument.");
		}
		double length = range.getLength();
		double lower = length * lowerMargin;
		double upper = length * upperMargin;
		return new Range(range.getLowerBound() - lower, range.getUpperBound() + upper);
	}

	/**
	 * Tests this object for equality with an arbitrary object.
	 * 
	 * @param object
	 *           the object to test against (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(final Object object) {
		if (!(object instanceof Range)) {
			return false;
		}
		final Range range = (Range) object;
		if (!(this.lower == range.lower)) {
			return false;
		}
		if (!(this.upper == range.upper)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a hash code.
	 * 
	 * @return A hash code.
	 */
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(this.lower);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.upper);
		result = 29 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Returns a string representation of this Range.
	 * 
	 * @return A String "Range[lower,upper]" where lower=lower range and upper=upper range.
	 */
	public String toString() {
		return ("Range[" + this.lower + "," + this.upper + "]");
	}

}
