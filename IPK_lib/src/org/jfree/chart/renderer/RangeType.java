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
 * RangeType.java
 * --------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: RangeType.java,v 1.1 2011-01-31 09:02:48 klukas Exp $
 * Changes:
 * --------
 * 07-May-2003 : Version 1 (DG);
 */

package org.jfree.chart.renderer;

/**
 * An enumeration of the 'range types' for a renderer. This is used when calculating the axis
 * range required to display all the data in a dataset...the result will depend on whether the
 * renderer plots the values directly (STANDARD) or stacks them (STACKED).
 */
public final class RangeType {

	/**
	 * The overall range is determined by looking at the individual values.
	 */
	public static final RangeType STANDARD = new RangeType("RangeType.STANDARD");

	/**
	 * The overall range is determined by looking at the sums of the values within each
	 * category.
	 */
	public static final RangeType STACKED = new RangeType("RangeType.STACKED");

	/**
	 * The overall range is determined by looking at the running total within each series.
	 */
	public static final RangeType SERIES_CUMULATIVE = new RangeType("RangeType.SERIES_CUMULATIVE");

	/** The name. */
	private String name;

	/**
	 * Private constructor.
	 * 
	 * @param name
	 *           the name.
	 */
	private RangeType(String name) {
		this.name = name;
	}

	/**
	 * Returns a string representing the object.
	 * 
	 * @return The string.
	 */
	public String toString() {
		return this.name;
	}

	/**
	 * Returns <code>true</code> if this object is equal to the specified object, and <code>false</code> otherwise.
	 * 
	 * @param o
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (!(o instanceof RangeType)) {
			return false;
		}

		final RangeType order = (RangeType) o;
		if (!this.name.equals(order.toString())) {
			return false;
		}

		return true;

	}

}
