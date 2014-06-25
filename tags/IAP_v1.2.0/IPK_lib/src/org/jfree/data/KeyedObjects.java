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
 * -----------------
 * KeyedObjects.java
 * -----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: KeyedObjects.java,v 1.1 2011-01-31 09:02:14 klukas Exp $
 * Changes:
 * --------
 * 31-Oct-2002 : Version 1 (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jfree.util.PublicCloneable;

/**
 * A collection of (key, object) pairs.
 */
public class KeyedObjects implements Cloneable, PublicCloneable, Serializable {

	/** Storage for the data. */
	private List data;

	/**
	 * Creates a new collection (initially empty).
	 */
	public KeyedObjects() {
		this.data = new java.util.ArrayList();
	}

	/**
	 * Returns the number of items (values) in the collection.
	 * 
	 * @return the item count.
	 */
	public int getItemCount() {
		return this.data.size();
	}

	/**
	 * Returns an object.
	 * 
	 * @param item
	 *           the item index (zero-based).
	 * @return The object (<code>null</code> if the index is out of range).
	 */
	public Object getObject(final int item) {
		Object result = null;
		if (item >= 0 && item < this.data.size()) {
			final KeyedObject kobj = (KeyedObject) this.data.get(item);
			if (kobj != null) {
				result = kobj.getObject();
			}
		}
		return result;
	}

	/**
	 * Returns a key.
	 * 
	 * @param index
	 *           the item index (zero-based).
	 * @return the row key.
	 */
	public Comparable getKey(final int index) {
		Comparable result = null;
		if (index >= 0 && index < this.data.size()) {
			final KeyedObject item = (KeyedObject) this.data.get(index);
			if (item != null) {
				result = item.getKey();
			}
		}
		return result;

	}

	HashMap<Comparable, Integer> key2idx = new HashMap<Comparable, Integer>();

	/**
	 * Returns the index for a given key.
	 * 
	 * @param key
	 *           the key.
	 * @return the index.
	 */
	public int getIndex(final Comparable key) {

		if (key2idx.containsKey(key))
			return key2idx.get(key);

		int result = -1;
		int i = 0;
		final Iterator iterator = this.data.iterator();
		while (iterator.hasNext()) {
			final KeyedObject ko = (KeyedObject) iterator.next();
			if (ko.getKey().equals(key)) {
				result = i;
				key2idx.put(key, result);
			}
			i++;
		}
		return result;

	}

	/**
	 * Returns the keys.
	 * 
	 * @return the keys.
	 */
	public List getKeys() {

		final List result = new java.util.ArrayList();
		final Iterator iterator = this.data.iterator();
		while (iterator.hasNext()) {
			final KeyedObject ko = (KeyedObject) iterator.next();
			result.add(ko.getKey());
		}
		return result;

	}

	/**
	 * Returns the object for a given key. If the key is not recognised, the method should
	 * return <code>null</code>.
	 * 
	 * @param key
	 *           the key.
	 * @return The object (possibly <code>null</code>).
	 */
	public Object getObject(final Comparable key) {
		return getObject(getIndex(key));
	}

	/**
	 * Adds a new object to the collection, or overwrites an existing object.
	 * <P>
	 * This is the same as the setObject(...) method.
	 * 
	 * @param key
	 *           the key.
	 * @param object
	 *           the object.
	 */
	public void addObject(final Comparable key, final Object object) {
		setObject(key, object);
	}

	/**
	 * Replaces an existing object, or adds a new object to the collection.
	 * <P>
	 * This is the same as the addObject(...) method.
	 * 
	 * @param key
	 *           the key.
	 * @param object
	 *           the object.
	 */
	public void setObject(final Comparable key, final Object object) {
		final int keyIndex = getIndex(key);
		if (keyIndex >= 0) {
			final KeyedObject ko = (KeyedObject) this.data.get(keyIndex);
			ko.setObject(object);
		} else {
			final KeyedObject ko = new KeyedObject(key, object);
			this.data.add(ko);
		}
	}

	/**
	 * Removes a value from the collection.
	 * 
	 * @param index
	 *           the index of the item to remove.
	 */
	public void removeValue(final int index) {
		this.data.remove(index);
	}

	/**
	 * Removes a value from the collection.
	 * 
	 * @param key
	 *           the key of the item to remove.
	 */
	public void removeValue(final Comparable key) {
		removeValue(getIndex(key));
	}

	/**
	 * Returns a clone of this object.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if there is a problem cloning.
	 */
	public Object clone() throws CloneNotSupportedException {
		final KeyedObjects clone = (KeyedObjects) super.clone();
		clone.data = new java.util.ArrayList();
		final Iterator iterator = this.data.iterator();
		while (iterator.hasNext()) {
			final KeyedObject ko = (KeyedObject) iterator.next();
			clone.data.add(ko.clone());
		}
		return clone;
	}

	/**
	 * Tests if this object is equal to another.
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

		if (!(o instanceof KeyedObjects)) {
			return false;
		}

		final KeyedObjects kos = (KeyedObjects) o;
		final int count = getItemCount();
		if (count != kos.getItemCount()) {
			return false;
		}

		for (int i = 0; i < count; i++) {
			final Comparable k1 = getKey(i);
			final Comparable k2 = kos.getKey(i);
			if (!k1.equals(k2)) {
				return false;
			}
			final Object o1 = getObject(i);
			final Object o2 = kos.getObject(i);
			if (o1 == null) {
				if (o2 != null) {
					return false;
				}
			} else {
				if (!o1.equals(o2)) {
					return false;
				}
			}
		}
		return true;

	}

}
