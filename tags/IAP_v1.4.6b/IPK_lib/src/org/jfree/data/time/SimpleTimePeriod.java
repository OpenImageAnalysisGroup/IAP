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
 * ---------------------
 * SimpleTimePeriod.java
 * ---------------------
 * (C) Copyright 2002, 2003 by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: SimpleTimePeriod.java,v 1.1 2011-01-31 09:03:01 klukas Exp $
 * Changes
 * -------
 * 07-Oct-2002 : Added Javadocs (DG);
 * 10-Jan-2003 : Renamed TimeAllocation --> SimpleTimePeriod (DG);
 * 13-Mar-2003 : Added equals(...) method, and Serializable interface (DG);
 * 21-Oct-2003 : Added hashCode() method (DG);
 */

package org.jfree.data.time;

import java.io.Serializable;
import java.util.Date;

/**
 * An arbitrary period of time, measured to millisecond precision using <code>java.util.Date</code>.
 * <p>
 * This class is intentionally immutable (that is, once constructed, you cannot alter the start and end attributes).
 */
public class SimpleTimePeriod implements TimePeriod, Serializable {

	/** The start date/time. */
	private Date start;

	/** The end date/time. */
	private Date end;

	/**
	 * Creates a new time allocation.
	 * 
	 * @param start
	 *           the start date/time.
	 * @param end
	 *           the end date/time.
	 */
	public SimpleTimePeriod(final Date start, final Date end) {

		// check arguments...
		if (start.getTime() > end.getTime()) {
			throw new IllegalArgumentException("SimpleTimePeriod: requires end >= start.");
		}

		this.start = start;
		this.end = end;

	}

	/**
	 * Returns the start date/time.
	 * 
	 * @return the start date/time.
	 */
	public Date getStart() {
		return this.start;
	}

	/**
	 * Returns the end date/time.
	 * 
	 * @return the end date/time.
	 */
	public Date getEnd() {
		return this.end;
	}

	/**
	 * Returns <code>true</code> if this time period is equal to another object, and <code>false</code> otherwise.
	 * <P>
	 * The test for equality looks only at the start and end values for the time period.
	 * 
	 * @param obj
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(final Object obj) {

		boolean result = false;

		if (obj instanceof TimePeriod) {
			final TimePeriod p = (TimePeriod) obj;
			result = this.start.equals(p.getStart()) && this.end.equals(p.getEnd());
		}

		return result;

	}

	/**
	 * Returns a hash code for this object instance.
	 * <p>
	 * The approach described by Joshua Bloch in "Effective Java" has been used here:
	 * <p>
	 * <code>http://developer.java.sun.com/developer/Books/effectivejava/Chapter3.pdf</code>
	 * 
	 * @return A hash code.
	 */
	public int hashCode() {
		int result = 17;
		result = 37 * result + this.start.hashCode();
		result = 37 * result + this.end.hashCode();
		return result;
	}

}
