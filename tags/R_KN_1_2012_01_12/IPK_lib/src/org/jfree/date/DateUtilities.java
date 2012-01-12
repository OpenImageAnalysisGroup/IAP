/*
 * ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 * Project Info: http://www.jfree.org/jcommon/index.html
 * Project Lead: David Gilbert (david.gilbert@object-refinery.com);
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * ------------------
 * DateUtilities.java
 * ------------------
 * (C) Copyright 2002, 2003, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DateUtilities.java,v 1.1 2011-01-31 09:02:04 klukas Exp $
 * Changes
 * -------
 * 11-Oct-2002 : Version 1 (DG);
 * 03-Apr-2003 : Added clear() method call (DG)
 */

package org.jfree.date;

import java.util.Calendar;
import java.util.Date;

/**
 * Some useful date methods.
 */
public class DateUtilities {

	/** A working calendar. */
	private static final Calendar CALENDAR = Calendar.getInstance();

	/**
	 * Creates a date.
	 * 
	 * @param yyyy
	 *           the year.
	 * @param month
	 *           the month (1 - 12).
	 * @param day
	 *           the day.
	 * @return a date.
	 */
	public static synchronized Date createDate(final int yyyy, final int month, final int day) {
		CALENDAR.clear();
		CALENDAR.set(yyyy, month - 1, day);
		return CALENDAR.getTime();
	}

	/**
	 * Creates a date.
	 * 
	 * @param yyyy
	 *           the year.
	 * @param month
	 *           the month (1 - 12).
	 * @param day
	 *           the day.
	 * @param hour
	 *           the hour.
	 * @param min
	 *           the minute.
	 * @return a date.
	 */
	public static synchronized Date createDate(final int yyyy, final int month, final int day, final int hour, final int min) {

		CALENDAR.clear();
		CALENDAR.set(yyyy, month - 1, day, hour, min);
		return CALENDAR.getTime();

	}

}
