/*
 * ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jcommon/index.html
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
 * ------------------------
 * HorizontalAlignment.java
 * ------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: HorizontalAlignment.java,v 1.1 2011-01-31 09:02:24 klukas Exp $
 * Changes:
 * --------
 * 08-Jan-2004 : Version 1 (DG);
 */

package org.jfree.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * An enumeration of the horizontal alignment types (<code>LEFT</code>, <code>RIGHT</code> and <code>CENTER</code>).
 */
public final class HorizontalAlignment implements Serializable {

	/** Left alignment. */
	public static final HorizontalAlignment LEFT = new HorizontalAlignment("HorizontalAlignment.LEFT");

	/** Right alignment. */
	public static final HorizontalAlignment RIGHT = new HorizontalAlignment("HorizontalAlignment.RIGHT");

	/** Center alignment. */
	public static final HorizontalAlignment CENTER = new HorizontalAlignment("HorizontalAlignment.CENTER");

	/** The name. */
	private String name;

	/**
	 * Private constructor.
	 * 
	 * @param name
	 *           the name.
	 */
	private HorizontalAlignment(final String name) {
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
	 * Returns <code>true</code> if this object is equal to the specified object, and <code>false</code> otherwise.
	 * 
	 * @param o
	 *           the other object.
	 * @return a boolean.
	 */
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}
		if (!(o instanceof HorizontalAlignment)) {
			return false;
		}

		final HorizontalAlignment alignment = (HorizontalAlignment) o;
		if (!this.name.equals(alignment.name)) {
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
	 * @return The object.
	 * @throws ObjectStreamException
	 *            if there is a problem.
	 */
	private Object readResolve() throws ObjectStreamException {
		HorizontalAlignment result = null;
		if (this.equals(HorizontalAlignment.LEFT)) {
			result = HorizontalAlignment.LEFT;
		} else
			if (this.equals(HorizontalAlignment.RIGHT)) {
				result = HorizontalAlignment.RIGHT;
			} else
				if (this.equals(HorizontalAlignment.CENTER)) {
					result = HorizontalAlignment.CENTER;
				}
		return result;
	}

}
