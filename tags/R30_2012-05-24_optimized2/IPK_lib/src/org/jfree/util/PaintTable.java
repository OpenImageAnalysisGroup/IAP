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
 * ---------------
 * PaintTable.java
 * ---------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Thomas Morgner;
 * $Id: PaintTable.java,v 1.1 2011-01-31 09:01:41 klukas Exp $
 * Changes (since 8-Jan-2003)
 * --------------------------
 * 08-Jan-2002 : Added standard header and Javadocs (DG);
 * 16-Jan-2003 : Changed to class, and moved to com.jrefinery.chart.renderer (DG);
 * 14-Feb-2003 : Fixed bug in setPaint() method (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 21-May-2003 : Implementation now uses ObjectTable as backend (TM);
 */

package org.jfree.util;

import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.io.SerialUtilities;

/**
 * A list of <code>Paint</code> objects.
 */
public class PaintTable extends ObjectTable implements Serializable {

	/**
	 * Creates a new paint table.
	 */
	public PaintTable() {
		super();
	}

	/**
	 * Returns the paint object from a particular cell in the table.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The paint.
	 */
	public Paint getPaint(final int row, final int column) {

		return (Paint) getObject(row, column);

	}

	/**
	 * Sets the paint for a cell in the table. The table is expanded if necessary.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @param paint
	 *           the paint.
	 */
	public void setPaint(final int row, final int column, final Paint paint) {

		setObject(row, column, paint);

	}

	/**
	 * Tests this paint table for equality with another object (typically also a paint table).
	 * 
	 * @param o
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(final Object o) {

		if (o instanceof PaintTable) {
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
	 * Handles the serialization of an single element of this table.
	 * 
	 * @param stream
	 *           the stream which should write the object
	 * @param o
	 *           the object that should be serialized
	 * @throws IOException
	 *            if an IO error occured
	 */
	protected void writeSerializedData(final ObjectOutputStream stream, final Object o) throws IOException {
		SerialUtilities.writePaint((Paint) o, stream);
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

		return SerialUtilities.readPaint(stream);
	}

}
