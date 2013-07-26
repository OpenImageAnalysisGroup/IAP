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
 * --------------
 * DateRange.java
 * --------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Bill Kelemen;
 * $Id: DateRange.java,v 1.1 2011-01-31 09:02:16 klukas Exp $
 * Changes
 * -------
 * 22-Apr-2002 : Version 1 based on code by Bill Kelemen (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 23-Sep-2003 : Minor Javadoc update (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 * A range specified in terms of two <code>java.util.Date</code> objects. Instances of this class
 * are immutable.
 */
public class DateRange extends Range implements Serializable {

	/** The lower bound for the range. */
	private Date lowerDate;

	/** The upper bound for the range. */
	private Date upperDate;

	/**
	 * Default constructor.
	 */
	public DateRange() {
		this(new Date(0), new Date(1));
	}

	/**
	 * Constructs a new range.
	 * 
	 * @param lower
	 *           the lower bound (<code>null</code> not permitted).
	 * @param upper
	 *           the upper bound (<code>null</code> not permitted).
	 */
	public DateRange(final Date lower, final Date upper) {

		super(lower.getTime(), upper.getTime());
		this.lowerDate = lower;
		this.upperDate = upper;

	}

	/**
	 * Constructs a new range using two values that will be interpreted as "milliseconds since
	 * midnight GMT, 1-Jan-1970".
	 * 
	 * @param lower
	 *           the lower (oldest) date.
	 * @param upper
	 *           the upper (most recent) date.
	 */
	public DateRange(final double lower, final double upper) {

		super(lower, upper);
		this.lowerDate = new Date((long) lower);
		this.upperDate = new Date((long) upper);

	}

	/**
	 * Constructs a new range that is based on another {@link Range}.
	 * <P>
	 * The other range does not have to be a {@link DateRange}. If it is not, the upper and lower bounds are evaluated as milliseconds since midnight GMT,
	 * 1-Jan-1970.
	 * 
	 * @param other
	 *           the other range (<code>null</code> not permitted).
	 */
	public DateRange(final Range other) {
		this(other.getLowerBound(), other.getUpperBound());
	}

	/**
	 * Returns the lower (earlier) date for the range.
	 * 
	 * @return The lower date for the range.
	 */
	public Date getLowerDate() {
		return this.lowerDate;
	}

	/**
	 * Returns the upper (later) date for the range.
	 * 
	 * @return The upper date for the range.
	 */
	public Date getUpperDate() {
		return this.upperDate;
	}

	/**
	 * Returns a string representing the date range (useful for debugging).
	 * 
	 * @return A string representing the date range.
	 */
	public String toString() {
		final DateFormat df = DateFormat.getDateTimeInstance();
		return "[" + df.format(this.lowerDate) + " --> " + df.format(this.upperDate) + "]";
	}

}
