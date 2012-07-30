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
 * ----------------
 * KeyedObject.java
 * ----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: KeyedObject.java,v 1.1 2011-01-31 09:02:17 klukas Exp $
 * Changes:
 * --------
 * 05-Feb-2003 : Version 1 (DG);
 * 27-Jan-2003 : Implemented Cloneable and Serializable, and added an equals() method (DG);
 */

package org.jfree.data;

import java.io.Serializable;

import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * A (key, object) pair.
 */
public class KeyedObject implements Cloneable, PublicCloneable, Serializable {

	/** The key. */
	private Comparable key;

	/** The object. */
	private Object object;

	/**
	 * Creates a new (key, object) pair.
	 * 
	 * @param key
	 *           the key.
	 * @param object
	 *           the object (<code>null</code> permitted).
	 */
	public KeyedObject(final Comparable key, final Object object) {
		this.key = key;
		this.object = object;
	}

	/**
	 * Returns the key.
	 * 
	 * @return The key.
	 */
	public Comparable getKey() {
		return this.key;
	}

	/**
	 * Returns the object.
	 * 
	 * @return The object (possibly <code>null</code>).
	 */
	public Object getObject() {
		return this.object;
	}

	/**
	 * Sets the object.
	 * 
	 * @param object
	 *           the object (<code>null</code> permitted).
	 */
	public void setObject(final Object object) {
		this.object = object;
	}

	/**
	 * Returns a clone of this object. It is assumed that the key is an immutable object,
	 * so it is not deep-cloned. The object is deep-cloned if it implements {@link PublicCloneable}, otherwise a shallow clone is made.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if there is a problem cloning.
	 */
	public Object clone() throws CloneNotSupportedException {
		final KeyedObject clone = (KeyedObject) super.clone();
		if (this.object instanceof PublicCloneable) {
			final PublicCloneable pc = (PublicCloneable) this.object;
			clone.object = pc.clone();
		}
		return clone;
	}

	/**
	 * Tests if this object is equal to another.
	 * 
	 * @param object
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}

		if (!(object instanceof KeyedObject)) {
			return false;
		}
		final KeyedObject ko = (KeyedObject) object;
		if (!ObjectUtils.equal(this.key, ko.key)) {
			return false;
		}

		if (!ObjectUtils.equal(this.object, ko.object)) {
			return false;
		}

		return true;
	}

}
