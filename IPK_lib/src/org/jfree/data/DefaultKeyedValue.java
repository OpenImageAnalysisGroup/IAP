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
 * DefaultKeyedValue.java
 * ----------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DefaultKeyedValue.java,v 1.1 2011-01-31 09:02:15 klukas Exp $
 * Changes:
 * --------
 * 31-Oct-2002 : Version 1 (DG);
 * 13-Mar-2003 : Added equals(...) method, and implemented Serializable (DG);
 * 18-Aug-2003 : Implemented Cloneable (DG);
 */

package org.jfree.data;

import java.io.Serializable;

/**
 * A (key, value) pair.
 * <P>
 * This class provides a default implementation of the {@link KeyedValue} interface.
 */
public class DefaultKeyedValue implements KeyedValue, Cloneable, Serializable {

	/** The key. */
	private Comparable key;

	/** The value. */
	private Number value;

	/**
	 * Creates a new (key, value) pair.
	 * 
	 * @param key
	 *           the key.
	 * @param value
	 *           the value (<code>null</code> permitted).
	 */
	public DefaultKeyedValue(final Comparable key, final Number value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Returns the key.
	 * 
	 * @return the key.
	 */
	public Comparable getKey() {
		return this.key;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value.
	 */
	public Number getValue() {
		return this.value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *           the value.
	 */
	public synchronized void setValue(final Number value) {
		this.value = value;
	}

	/**
	 * Tests if this object is equal to another.
	 * 
	 * @param o
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DefaultKeyedValue)) {
			return false;
		}

		final DefaultKeyedValue defaultKeyedValue = (DefaultKeyedValue) o;

		if (this.key != null ? !this.key.equals(defaultKeyedValue.key)
							: defaultKeyedValue.key != null) {
			return false;
		}
		if (this.value != null ? !this.value.equals(defaultKeyedValue.value)
							: defaultKeyedValue.value != null) {
			return false;
		}

		return true;
	}

	/**
	 * Returns a hash code.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		int result;
		result = (this.key != null ? this.key.hashCode() : 0);
		result = 29 * result + (this.value != null ? this.value.hashCode() : 0);
		return result;
	}

	/**
	 * Returns a clone. It is assumed that both the key and value are immutable objects,
	 * so only the references are cloned, not the objects themselves.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            Not thrown by this class, but subclasses (if any) might.
	 */
	public Object clone() throws CloneNotSupportedException {
		final DefaultKeyedValue clone = (DefaultKeyedValue) super.clone();
		return clone;
	}
}
