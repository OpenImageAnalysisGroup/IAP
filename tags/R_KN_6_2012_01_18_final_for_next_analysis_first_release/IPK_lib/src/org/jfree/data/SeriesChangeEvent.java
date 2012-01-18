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
 * ----------------------
 * SeriesChangeEvent.java
 * ----------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: SeriesChangeEvent.java,v 1.1 2011-01-31 09:02:11 klukas Exp $
 * Changes
 * -------
 * 15-Nov-2001 : Version 1 (DG);
 * 18-Aug-2003 : Implemented Serializable (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.EventObject;

/**
 * An event with details of a change to a series.
 */
public class SeriesChangeEvent extends EventObject implements Serializable {

	/**
	 * Constructs a new event.
	 * 
	 * @param source
	 *           the source of the change event.
	 */
	public SeriesChangeEvent(final Object source) {
		super(source);
	}

}
