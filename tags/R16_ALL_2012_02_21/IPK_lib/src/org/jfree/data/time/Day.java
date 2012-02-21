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
 * --------
 * Day.java
 * --------
 * (C) Copyright 2001-2003, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: Day.java,v 1.1 2011-01-31 09:03:00 klukas Exp $
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 15-Nov-2001 : Updated Javadoc comments (DG);
 * 04-Dec-2001 : Added static method to parse a string into a Day object (DG);
 * 19-Dec-2001 : Added new constructor as suggested by Paul English (DG);
 * 29-Jan-2002 : Changed getDay() method to getSerialDate() (DG);
 * 26-Feb-2002 : Changed getStart(), getMiddle() and getEnd() methods to evaluate with reference
 * to a particular time zone (DG);
 * 19-Mar-2002 : Changed the API for the TimePeriod classes (DG);
 * 29-May-2002 : Fixed bug in equals method (DG);
 * 24-Jun-2002 : Removed unnecessary imports (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Jan-2003 : Changed base class and method names (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package, and implemented Serializable (DG);
 * 21-Oct-2003 : Added hashCode() method (DG);
 */

package org.jfree.data.time;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.jfree.date.SerialDate;

/**
 * Represents a single day in the range 1-Jan-1900 to 31-Dec-9999.
 * <P>
 * This class is immutable, which is a requirement for all {@link RegularTimePeriod} subclasses.
 */
public class Day extends RegularTimePeriod implements Serializable {

	/** A standard date formatter. */
	protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	/** A date formatter for the default locale. */
	protected static final DateFormat DATE_FORMAT_SHORT = DateFormat.getDateInstance(DateFormat.SHORT);

	/** A date formatter for the default locale. */
	protected static final DateFormat DATE_FORMAT_MEDIUM = DateFormat.getDateInstance(DateFormat.MEDIUM);

	/** A date formatter for the default locale. */
	protected static final DateFormat DATE_FORMAT_LONG = DateFormat.getDateInstance(DateFormat.LONG);

	/** The day (uses SerialDate for convenience). */
	private SerialDate serialDate;

	/**
	 * Constructs a Day, based on the system date/time.
	 */
	public Day() {
		this(new Date());
	}

	/**
	 * Constructs a new one day time period.
	 * 
	 * @param day
	 *           the day-of-the-month.
	 * @param month
	 *           the month (1 to 12).
	 * @param year
	 *           the year (1900 <= year <= 9999).
	 */
	public Day(final int day, final int month, final int year) {
		this.serialDate = SerialDate.createInstance(day, month, year);
	}

	/**
	 * Constructs a new one day time period.
	 * 
	 * @param serialDate
	 *           the day.
	 */
	public Day(final SerialDate serialDate) {
		this.serialDate = serialDate;
	}

	/**
	 * Constructs a new Day, based on a particular date/time and the default
	 * time zone.
	 * 
	 * @param time
	 *           the time.
	 */
	public Day(final Date time) {
		this(time, RegularTimePeriod.DEFAULT_TIME_ZONE);
	}

	/**
	 * Constructs a Day, based on a particular date/time and time zone.
	 * 
	 * @param time
	 *           the date/time.
	 * @param zone
	 *           the time zone.
	 */
	public Day(final Date time, final TimeZone zone) {

		final Calendar calendar = Calendar.getInstance(zone);
		calendar.setTime(time);
		final int d = calendar.get(Calendar.DAY_OF_MONTH);
		final int m = calendar.get(Calendar.MONTH) + 1;
		final int y = calendar.get(Calendar.YEAR);
		this.serialDate = SerialDate.createInstance(d, m, y);

	}

	/**
	 * Returns the day as a SerialDate.
	 * <P>
	 * Note: the reference that is returned should be an instance of an immutable SerialDate (otherwise the caller could use the reference to alter the state of
	 * this Day instance, and Day is supposed to be immutable).
	 * 
	 * @return the day as a SerialDate.
	 */
	public SerialDate getSerialDate() {
		return this.serialDate;
	}

	/**
	 * Returns the year.
	 * 
	 * @return the year.
	 */
	public int getYear() {
		return this.serialDate.getYYYY();
	}

	/**
	 * Returns the month.
	 * 
	 * @return the month.
	 */
	public int getMonth() {
		return this.serialDate.getMonth();
	}

	/**
	 * Returns the day of the month.
	 * 
	 * @return the day of the month.
	 */
	public int getDayOfMonth() {
		return this.serialDate.getDayOfMonth();
	}

	/**
	 * Returns the day preceding this one.
	 * 
	 * @return the day preceding this one.
	 */
	public RegularTimePeriod previous() {

		final Day result;
		final int serial = this.serialDate.toSerial();
		if (serial > SerialDate.SERIAL_LOWER_BOUND) {
			final SerialDate yesterday = SerialDate.createInstance(serial - 1);
			return new Day(yesterday);
		} else {
			result = null;
		}
		return result;

	}

	/**
	 * Returns the day following this one, or null if some limit has been reached.
	 * 
	 * @return the day following this one, or <code>null</code> if some limit has been reached.
	 */
	public RegularTimePeriod next() {

		final Day result;
		final int serial = this.serialDate.toSerial();
		if (serial < SerialDate.SERIAL_UPPER_BOUND) {
			final SerialDate tomorrow = SerialDate.createInstance(serial + 1);
			return new Day(tomorrow);
		} else {
			result = null;
		}
		return result;

	}

	/**
	 * Returns a serial index number for the day.
	 * 
	 * @return the serial index number.
	 */
	public long getSerialIndex() {
		return this.serialDate.toSerial();
	}

	/**
	 * Returns the first millisecond of the day, evaluated using the supplied
	 * calendar (which determines the time zone).
	 * 
	 * @param calendar
	 *           calendar to use.
	 * @return the start of the day as milliseconds since 01-01-1970.
	 */
	public long getFirstMillisecond(final Calendar calendar) {

		final int year = this.serialDate.getYYYY();
		final int month = this.serialDate.getMonth();
		final int day = this.serialDate.getDayOfMonth();

		calendar.clear();
		calendar.set(year, month - 1, day, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime().getTime();

	}

	/**
	 * Returns the last millisecond of the day, evaluated using the supplied
	 * calendar (which determines the time zone).
	 * 
	 * @param calendar
	 *           calendar to use.
	 * @return the end of the day as milliseconds since 01-01-1970.
	 */
	public long getLastMillisecond(final Calendar calendar) {

		final int year = this.serialDate.getYYYY();
		final int month = this.serialDate.getMonth();
		final int day = this.serialDate.getDayOfMonth();

		calendar.clear();
		calendar.set(year, month - 1, day, 23, 59, 59);
		calendar.set(Calendar.MILLISECOND, 999);

		return calendar.getTime().getTime();

	}

	/**
	 * Tests the equality of this Day object to an arbitrary object. Returns
	 * true if the target is a Day instance or a SerialDate instance
	 * representing the same day as this object. In all other cases,
	 * returns false.
	 * 
	 * @param object
	 *           the object.
	 * @return a flag indicating whether or not an object is equal to this day.
	 */
	public boolean equals(final Object object) {

		if (object != null) {
			if (object instanceof Day) {
				final Day d = (Day) object;
				return (this.serialDate.equals(d.getSerialDate()));
			} else {
				return (this.serialDate.equals(object));
			}
		} else {
			return false;
		}

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
		return this.serialDate.hashCode();
	}

	/**
	 * Returns an integer indicating the order of this Day object relative to
	 * the specified object:
	 * negative == before, zero == same, positive == after.
	 * 
	 * @param o1
	 *           the object to compare.
	 * @return negative == before, zero == same, positive == after.
	 */
	public int compareTo(final Object o1) {

		final int result;

		// CASE 1 : Comparing to another Day object
		// ----------------------------------------
		if (o1 instanceof Day) {
			final Day d = (Day) o1;
			result = -d.getSerialDate().compare(this.serialDate);
		}

		// CASE 2 : Comparing to another TimePeriod object
		// -----------------------------------------------
		else
			if (o1 instanceof RegularTimePeriod) {
				// more difficult case - evaluate later...
				result = 0;
			}

			// CASE 3 : Comparing to a non-TimePeriod object
			// ---------------------------------------------
			else {
				// consider time periods to be ordered after general objects
				result = 1;
			}

		return result;

	}

	/**
	 * Returns a string representing the day.
	 * 
	 * @return A string representing the day.
	 */
	public String toString() {
		return this.serialDate.toString();
	}

	/**
	 * Parses the string argument as a day.
	 * <P>
	 * This method is required to recognise YYYY-MM-DD as a valid format. Anything else, for now, is a bonus.
	 * 
	 * @param s
	 *           the date string to parse.
	 * @return <code>null</code> if the string does not contain any parseable
	 *         string, the day otherwise.
	 */
	public static Day parseDay(final String s) {

		try {
			return new Day(Day.DATE_FORMAT.parse(s));
		} catch (ParseException e1) {
			try {
				return new Day(Day.DATE_FORMAT_SHORT.parse(s));
			} catch (ParseException e2) {
				// ignore
			}
		}
		return null;

	}

}
