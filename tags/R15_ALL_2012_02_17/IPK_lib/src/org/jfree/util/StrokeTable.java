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
 * ----------------
 * StrokeTable.java
 * ----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: StrokeTable.java,v 1.1 2011-01-31 09:01:42 klukas Exp $
 * Changes
 * -------
 * 17-Jan-2003 : Version 1 (DG);
 * 14-Feb-2003 : Fixed bug in setShape() method (DG);
 * 21-May-2003 : Implementation now uses ObjectTable as backend (TM).
 */

package org.jfree.util;

import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.io.SerialUtilities;

/**
 * A lookup table for <code>Stroke</code> objects.
 */
public class StrokeTable extends ObjectTable implements Serializable {

	/**
	 * Creates a new stroke table.
	 */
	public StrokeTable() {
		super();
	}

	/**
	 * Returns the stroke object from a particular cell in the table.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The stroke.
	 */
	public Stroke getStroke(final int row, final int column) {

		return (Stroke) getObject(row, column);
	}

	/**
	 * Sets the stroke for an item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @param stroke
	 *           the stroke.
	 */
	public void setStroke(final int row, final int column, final Stroke stroke) {
		setObject(row, column, stroke);
	}

	/**
	 * Tests this stroke table for equality with another object (typically also a stroke table).
	 * 
	 * @param o
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(final Object o) {

		if (o instanceof StrokeTable) {
			// table content is compared in the base class
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

	/**
	 * Handles the deserialization of a single element of the table.
	 * 
	 * @param stream
	 *           the object input stream from which to read the object.
	 * @return the deserialized object
	 * @exception ClassNotFoundException
	 *               Class of a serialized object
	 *               cannot be found.
	 * @exception IOException
	 *               Any of the usual Input/Output related exceptions.
	 */
	protected Object readSerializedData(final ObjectInputStream stream)
						throws ClassNotFoundException, IOException {
		return SerialUtilities.readStroke(stream);
	}

	/**
	 * Handles the serialization of an single element of this table.
	 * 
	 * @param stream
	 *           the stream which should write the object
	 * @param o
	 *           the object that should be serialized
	 * @throws IOException
	 *            if an IO error occured
	 */
	protected void writeSerializedData(final ObjectOutputStream stream, final Object o)
						throws IOException {
		SerialUtilities.writeStroke((Stroke) o, stream);
	}
}
