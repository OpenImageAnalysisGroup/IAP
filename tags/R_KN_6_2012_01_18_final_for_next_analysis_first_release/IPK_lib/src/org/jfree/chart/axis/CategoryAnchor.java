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
 * -------------------
 * CategoryAnchor.java
 * -------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: CategoryAnchor.java,v 1.1 2011-01-31 09:01:39 klukas Exp $
 * Changes:
 * --------
 * 04-Jul-2003 : Version 1 (DG);
 */

package org.jfree.chart.axis;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate one of three positions within a category: <code>START</code>, <code>MIDDLE</code> and <code>END</code>.
 */
public final class CategoryAnchor implements Serializable {

	/** Start of period. */
	public static final CategoryAnchor START = new CategoryAnchor("CategoryAnchor.START");

	/** Middle of period. */
	public static final CategoryAnchor MIDDLE = new CategoryAnchor("CategoryAnchor.MIDDLE");

	/** End of period. */
	public static final CategoryAnchor END = new CategoryAnchor("CategoryAnchor.END");

	/** The name. */
	private String name;

	/**
	 * Private constructor.
	 * 
	 * @param name
	 *           the name.
	 */
	private CategoryAnchor(String name) {
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
		if (!(o instanceof CategoryAnchor)) {
			return false;
		}

		final CategoryAnchor position = (CategoryAnchor) o;
		if (!this.name.equals(position.toString())) {
			return false;
		}

		return true;

	}

	/**
	 * Ensures that serialization returns the unique instances.
	 * 
	 * @return The object.
	 * @throws ObjectStreamException
	 *            if there is a problem.
	 */
	private Object readResolve() throws ObjectStreamException {
		if (this.equals(CategoryAnchor.START)) {
			return CategoryAnchor.START;
		} else
			if (this.equals(CategoryAnchor.MIDDLE)) {
				return CategoryAnchor.MIDDLE;
			} else
				if (this.equals(CategoryAnchor.END)) {
					return CategoryAnchor.END;
				}
		return null;
	}

}
