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
 * -----------------
 * BoundsAnchor.java
 * -----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: BoundsAnchor.java,v 1.1 2011-01-31 09:02:23 klukas Exp $
 * Changes:
 * --------
 * 21-May-2003 : Version 1 (DG);
 * 12-Nov-2003 : Deprecated - no longer used (DG);
 */

package org.jfree.ui;

import java.io.Serializable;

/**
 * Used to indicate the position of an anchor point for a bounding rectangle.
 * 
 * @deprecated No longer required (see also RectangleAnchor).
 */
public final class BoundsAnchor implements Serializable {

	/** Top/left. */
	public static final BoundsAnchor TOP_LEFT = new BoundsAnchor("BoundsAnchor.TOP_LEFT");

	/** Top/center. */
	public static final BoundsAnchor TOP_CENTER = new BoundsAnchor("BoundsAnchor.TOP_CENTER");

	/** Top/right. */
	public static final BoundsAnchor TOP_RIGHT = new BoundsAnchor("BoundsAnchor.TOP_RIGHT");

	/** Middle/left. */
	public static final BoundsAnchor MIDDLE_LEFT = new BoundsAnchor("BoundsAnchor.MIDDLE_LEFT");

	/** Middle/center. */
	public static final BoundsAnchor CENTER = new BoundsAnchor("BoundsAnchor.CENTER");

	/** Middle/right. */
	public static final BoundsAnchor MIDDLE_RIGHT = new BoundsAnchor("BoundsAnchor.MIDDLE_RIGHT");

	/** Bottom/left. */
	public static final BoundsAnchor BOTTOM_LEFT = new BoundsAnchor("BoundsAnchor.BOTTOM_LEFT");

	/** Bottom/center. */
	public static final BoundsAnchor BOTTOM_CENTER = new BoundsAnchor("BoundsAnchor.BOTTOM_CENTER");

	/** Bottom/right. */
	public static final BoundsAnchor BOTTOM_RIGHT = new BoundsAnchor("BoundsAnchor.BOTTOM_RIGHT");

	/** The name. */
	private String name;

	/**
	 * Private constructor.
	 * 
	 * @param name
	 *           the name.
	 */
	private BoundsAnchor(final String name) {
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
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BoundsAnchor)) {
			return false;
		}

		final BoundsAnchor boundsAnchor = (BoundsAnchor) o;
		if (!this.name.equals(boundsAnchor.name)) {
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
}
