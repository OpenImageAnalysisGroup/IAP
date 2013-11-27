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
 * TimePeriod.java
 * ---------------
 * (C) Copyright 2003 by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimePeriod.java,v 1.1 2011-01-31 09:03:00 klukas Exp $
 * Changes
 * -------
 * 10-Jan-2003 : Version 1 (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package (DG);
 */

package org.jfree.data.time;

import java.util.Date;

/**
 * A period of time measured to millisecond precision using <code>java.util.Date</code>.
 */
public interface TimePeriod {

	/**
	 * Returns the start date/time.
	 * 
	 * @return the start date/time.
	 */
	public Date getStart();

	/**
	 * Returns the end date/time.
	 * 
	 * @return the end date/time.
	 */
	public Date getEnd();

}
