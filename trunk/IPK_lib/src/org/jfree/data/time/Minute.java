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
 * -----------
 * Minute.java
 * -----------
 * (C) Copyright 2001-2003, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: Minute.java,v 1.1 2011-01-31 09:03:01 klukas Exp $
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 18-Dec-2001 : Changed order of parameters in constructor (DG);
 * 19-Dec-2001 : Added a new constructor as suggested by Paul English (DG);
 * 14-Feb-2002 : Fixed bug in Minute(Date) constructor, and changed the range to start from zero
 * instead of one (DG);
 * 26-Feb-2002 : Changed getStart(), getMiddle() and getEnd() methods to evaluate with reference
 * to a particular time zone (DG);
 * 13-Mar-2002 : Added parseMinute() method (DG);
 * 19-Mar-2002 : Changed API, the minute is now defined in relation to an Hour (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 07-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Jan-2003 : Changed base class and method names (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package and implemented Serializable (DG);
 * 21-Oct-2003 : Added hashCode() method, and new constructor for convenience (DG);
 */

package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents a minute.
 * <P>
 * This class is immutable, which is a requirement for all {@link RegularTimePeriod} subclasses.
 */
public class Minute extends RegularTimePeriod implements Serializable {

	/** Useful constant for the first minute in a day. */
	public static final int FIRST_MINUTE_IN_HOUR = 0;

	/** Useful constant for the last minute in a day. */
	public static final int LAST_MINUTE_IN_HOUR = 59;

	/** The hour in which the minute falls. */
	private Hour hour;

	/** The minute. */
	private int minute;

	/**
	 * Constructs a new Minute, based on the system date/time.
	 */
	public Minute() {

		this(new Date());

	}

	/**
	 * Constructs a new Minute.
	 * 
	 * @param minute
	 *           the minute (0 to 59).
	 * @param hour
	 *           the hour.
	 */
	public Minute(final int minute, final Hour hour) {

		this.minute = minute;
		this.hour = hour;

	}

	/**
	 * Constructs a new Minute, based on the supplied date/time.
	 * 
	 * @param time
	 *           the time.
	 */
	public Minute(final Date time) {
		this(time, RegularTimePeriod.DEFAULT_TIME_ZONE);
	}

	/**
	 * Constructs a new Minute, based on the supplied date/time and timezone.
	 * 
	 * @param time
	 *           the time.
	 * @param zone
	 *           the time zone.
	 */
	public Minute(final Date time, final TimeZone zone) {

		final Calendar calendar = Calendar.getInstance(zone);
		calendar.setTime(time);
		final int min = calendar.get(Calendar.MINUTE);
		this.minute = min;
		this.hour = new Hour(time, zone);

	}

	/**
	 * Creates a new minute.
	 * 
	 * @param minute
	 *           the minute (0-59).
	 * @param hour
	 *           the hour (0-23).
	 * @param day
	 *           the day (1-31).
	 * @param month
	 *           the month (1-12).
	 * @param year
	 *           the year (1900-9999).
	 */
	public Minute(final int minute,
						final int hour,
						final int day,
						final int month,
						final int year) {
		this(minute, new Hour(hour, new Day(day, month, year)));
	}

	/**
	 * Returns the hour.
	 * 
	 * @return the hour.
	 */
	public Hour getHour() {
		return this.hour;
	}

	/**
	 * Returns the minute.
	 * 
	 * @return the minute.
	 */
	public int getMinute() {
		return this.minute;
	}

	/**
	 * Returns the minute preceding this one.
	 * 
	 * @return the minute preceding this one.
	 */
	public RegularTimePeriod previous() {

		final Minute result;
		if (this.minute != FIRST_MINUTE_IN_HOUR) {
			result = new Minute(this.minute - 1, this.hour);
		} else { // we are at the first minute in the hour...
			final Hour prevHour = (Hour) this.hour.previous();
			if (prevHour != null) {
				result = new Minute(LAST_MINUTE_IN_HOUR, prevHour);
			} else {
				result = null;
			}
		}
		return result;

	}

	/**
	 * Returns the minute following this one.
	 * 
	 * @return the minute following this one.
	 */
	public RegularTimePeriod next() {

		final Minute result;
		if (this.minute != LAST_MINUTE_IN_HOUR) {
			result = new Minute(this.minute + 1, this.hour);
		} else { // we are at the last minute in the hour...
			final Hour nextHour = (Hour) this.hour.next();
			if (nextHour != null) {
				result = new Minute(FIRST_MINUTE_IN_HOUR, nextHour);
			} else {
				result = null;
			}
		}
		return result;

	}

	/**
	 * Returns a serial index number for the minute.
	 * 
	 * @return the serial index number.
	 */
	public long getSerialIndex() {
		return this.hour.getSerialIndex() * 60L + this.minute;
	}

	/**
	 * Returns the first millisecond of the minute.
	 * 
	 * @param calendar
	 *           the calendar and timezone.
	 * @return the first millisecond.
	 */
	public long getFirstMillisecond(final Calendar calendar) {

		final int year = this.hour.getDay().getYear();
		final int month = this.hour.getDay().getMonth() - 1;
		final int day = this.hour.getDay().getDayOfMonth();

		calendar.clear();
		calendar.set(year, month, day, this.hour.getHour(), this.minute, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime().getTime();

	}

	/**
	 * Returns the last millisecond of the minute.
	 * 
	 * @param calendar
	 *           the calendar and timezone.
	 * @return the last millisecond.
	 */
	public long getLastMillisecond(final Calendar calendar) {

		final int year = this.hour.getDay().getYear();
		final int month = this.hour.getDay().getMonth() - 1;
		final int day = this.hour.getDay().getDayOfMonth();

		calendar.clear();
		calendar.set(year, month, day, this.hour.getHour(), this.minute, 59);
		calendar.set(Calendar.MILLISECOND, 999);

		return calendar.getTime().getTime();

	}

	/**
	 * Tests the equality of this object against an arbitrary Object.
	 * <P>
	 * This method will return true ONLY if the object is a Minute object representing the same minute as this instance.
	 * 
	 * @param object
	 *           the object to compare.
	 * @return <code>true</code> if the minute and hour value of this and the
	 *         object are the same.
	 */
	public boolean equals(final Object object) {
		if (object instanceof Minute) {
			final Minute m = (Minute) object;
			return ((this.minute == m.getMinute()) && (this.hour.equals(m.getHour())));
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
		int result = 17;
		result = 37 * result + this.minute;
		result = 37 * result + this.hour.hashCode();
		return result;
	}

	/**
	 * Returns an integer indicating the order of this Minute object relative
	 * to the specified object:
	 * negative == before, zero == same, positive == after.
	 * 
	 * @param o1
	 *           object to compare.
	 * @return negative == before, zero == same, positive == after.
	 */
	public int compareTo(final Object o1) {

		int result;

		// CASE 1 : Comparing to another Minute object
		// -------------------------------------------
		if (o1 instanceof Minute) {
			final Minute m = (Minute) o1;
			result = getHour().compareTo(m.getHour());
			if (result == 0) {
				result = this.minute - m.getMinute();
			}
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
	 * Creates a Minute instance by parsing a string. The string is assumed to
	 * be in the format "YYYY-MM-DD HH:MM", perhaps with leading or trailing
	 * whitespace.
	 * 
	 * @param s
	 *           the minute string to parse.
	 * @return <code>null</code>, if the string is not parseable, the minute
	 *         otherwise.
	 */
	public static Minute parseMinute(String s) {

		Minute result = null;
		s = s.trim();

		final String daystr = s.substring(0, Math.min(10, s.length()));
		final Day day = Day.parseDay(daystr);
		if (day != null) {
			String hmstr = s.substring(Math.min(daystr.length() + 1, s.length()), s.length());
			hmstr = hmstr.trim();

			final String hourstr = hmstr.substring(0, Math.min(2, hmstr.length()));
			final int hour = Integer.parseInt(hourstr);

			if ((hour >= 0) && (hour <= 23)) {
				final String minstr = hmstr.substring(
									Math.min(hourstr.length() + 1, hmstr.length()), hmstr.length()
									);
				final int minute = Integer.parseInt(minstr);
				if ((minute >= 0) && (minute <= 59)) {
					result = new Minute(minute, new Hour(hour, day));
				}
			}
		}

		return result;

	}

}
