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
 * --------------------
 * TextAnchorTable.java
 * --------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TextAnchorTable.java,v 1.1 2011-01-31 09:02:24 klukas Exp $
 * Changes
 * -------
 * 11-Jun-2003 : Version 1 (DG);
 * 19-Aug-2003 : Moved to 'org.jfree.ui' (DG);
 */

package org.jfree.ui;

import java.io.Serializable;

import org.jfree.util.ObjectTable;

/**
 * A table of text anchors.
 * 
 * @deprecated No longer used.
 */
public class TextAnchorTable extends ObjectTable implements Serializable {

	/**
	 * Creates a new text anchor table.
	 */
	public TextAnchorTable() {
		super();
	}

	/**
	 * Returns the TextAnchor object from a particular cell in the table.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The anchor.
	 */
	public TextAnchor getAnchor(final int row, final int column) {

		return (TextAnchor) getObject(row, column);

	}

	/**
	 * Sets the anchor for a cell in the table. The table is expanded if necessary.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @param anchor
	 *           the anchor.
	 */
	public void setAnchor(final int row, final int column, final TextAnchor anchor) {

		setObject(row, column, anchor);

	}

	/**
	 * Tests this font table for equality with another object (typically also a font table).
	 * 
	 * @param o
	 *           the other object.
	 * @return A font.
	 */
	public boolean equals(final Object o) {

		if (o instanceof TextAnchorTable) {
			return super.equals(o);
		}

		return false;
	}

	/**
	 * Returns a hash code value for the object.
	 * 
	 * @return the hashcode
	 */
	public int hashCode() {
		return super.hashCode();
	}
}
