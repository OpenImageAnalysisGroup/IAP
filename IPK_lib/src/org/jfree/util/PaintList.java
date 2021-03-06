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
 * --------------
 * PaintList.java
 * --------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: PaintList.java,v 1.1 2011-01-31 09:01:40 klukas Exp $
 * Changes
 * -------
 * 13-Aug-2003 : Version 1 (DG);
 */

package org.jfree.util;

import java.awt.Paint;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.io.SerialUtilities;

/**
 * A table of {@link Paint} objects.
 */
public class PaintList extends AbstractObjectList implements Cloneable, Serializable {

	/**
	 * Creates a new list.
	 */
	public PaintList() {
		super();
	}

	/**
	 * Returns a {@link Paint} object from the list.
	 * 
	 * @param index
	 *           the index (zero-based).
	 * @return The object.
	 */
	public Paint getPaint(final int index) {
		return (Paint) get(index);
	}

	/**
	 * Sets the {@link Paint} for an item in the list. The list is expanded if necessary.
	 * 
	 * @param index
	 *           the index (zero-based).
	 * @param paint
	 *           the {@link Paint}.
	 */
	public void setPaint(final int index, final Paint paint) {
		set(index, paint);
	}

	/**
	 * Returns an independent copy of the list.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if an item in the list cannot be cloned.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Tests the list for equality with another object (typically also a list).
	 * 
	 * @param o
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(final Object o) {

		if (o == null) {
			return false;
		}

		if (o == this) {
			return true;
		}

		if (o instanceof PaintList) {
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
	 * Provides serialization support.
	 * 
	 * @param stream
	 *           the output stream.
	 * @throws IOException
	 *            if there is an I/O error.
	 */
	private void writeObject(final ObjectOutputStream stream) throws IOException {

		stream.defaultWriteObject();
		final int count = size();
		stream.writeInt(count);
		for (int i = 0; i < count; i++) {
			final Paint paint = getPaint(i);
			if (paint != null) {
				stream.writeInt(i);
				SerialUtilities.writePaint(paint, stream);
			} else {
				stream.writeInt(-1);
			}
		}

	}

	/**
	 * Provides serialization support.
	 * 
	 * @param stream
	 *           the input stream.
	 * @throws IOException
	 *            if there is an I/O error.
	 * @throws ClassNotFoundException
	 *            if there is a classpath problem.
	 */
	private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {

		stream.defaultReadObject();
		final int count = stream.readInt();
		for (int i = 0; i < count; i++) {
			final int index = stream.readInt();
			if (index != -1) {
				setPaint(index, SerialUtilities.readPaint(stream));
			}
		}

	}

}
