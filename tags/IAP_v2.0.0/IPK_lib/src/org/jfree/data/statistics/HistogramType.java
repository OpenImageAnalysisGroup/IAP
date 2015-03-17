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
 * ------------------
 * HistogramType.java
 * ------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: HistogramType.java,v 1.1 2011-01-31 09:02:05 klukas Exp $
 * Changes
 * -------
 * 05-Mar-2004 : Version 1 (DG);
 */

package org.jfree.data.statistics;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A class for creating constants to represent the histogram type. See Bloch's enum tip in
 * 'Effective Java'
 */
public class HistogramType implements Serializable {

	/** Frequency histogram. */
	public static final HistogramType FREQUENCY = new HistogramType("FREQUENCY");

	/** Relative frequency. */
	public static final HistogramType RELATIVE_FREQUENCY = new HistogramType("RELATIVE_FREQUENCY");

	/** Scale area to one. */
	public static final HistogramType SCALE_AREA_TO_1 = new HistogramType("SCALE_AREA_TO_1");

	/** The type name. */
	private String name;

	/**
	 * Creates a new type.
	 * 
	 * @param name
	 *           the name.
	 */
	private HistogramType(final String name) {
		this.name = name;
	}

	/**
	 * Returns a string representing the object.
	 * 
	 * @return the string.
	 */
	public String toString() {
		return this.name;
	}

	/**
	 * Tests this type for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object to test against.
	 * @return a boolean.
	 */
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof HistogramType)) {
			return false;
		}

		final HistogramType t = (HistogramType) obj;
		if (!this.name.equals(t.name)) {
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
		return this.name.hashCode();
	}

	/**
	 * Ensures that serialization returns the unique instances.
	 * 
	 * @return the object.
	 * @throws ObjectStreamException
	 *            if there is a problem.
	 */
	private Object readResolve() throws ObjectStreamException {
		if (this.equals(HistogramType.FREQUENCY)) {
			return HistogramType.FREQUENCY;
		} else
			if (this.equals(HistogramType.RELATIVE_FREQUENCY)) {
				return HistogramType.RELATIVE_FREQUENCY;
			} else
				if (this.equals(HistogramType.SCALE_AREA_TO_1)) {
					return HistogramType.SCALE_AREA_TO_1;
				}
		return null;
	}

}
