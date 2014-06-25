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
 * -----------------------
 * DefaultKeyedValues.java
 * -----------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DefaultKeyedValues.java,v 1.1 2011-01-31 09:02:18 klukas Exp $
 * Changes:
 * --------
 * 31-Oct-2002 : Version 1 (DG);
 * 11-Feb-2003 : Fixed bug in getValue(key) method for unrecognised key (DG);
 * 05-Mar-2003 : Added methods to sort stored data 'by key' or 'by value' (DG);
 * 13-Mar-2003 : Implemented Serializable (DG);
 * 08-Apr-2003 : Modified removeValue(Comparable) method to fix bug 717049 (DG);
 * 18-Aug-2003 : Implemented Cloneable (DG);
 * 27-Aug-2003 : Moved SortOrder from org.jfree.data --> org.jfree.util (DG);
 * 09-Feb-2004 : Modified getIndex() method - see bug report 893256 (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jfree.util.SortOrder;

/**
 * A collection of (key, value) pairs. This class provides a default implementation of the {@link KeyedValues} interface.
 */
public class DefaultKeyedValues implements KeyedValues, Cloneable, Serializable {

	/** Storage for the data. */
	private List data;

	/**
	 * Creates a new collection (initially empty).
	 */
	public DefaultKeyedValues() {
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
	 * Returns a value.
	 * 
	 * @param item
	 *           the item of interest (zero-based index).
	 * @return the value.
	 */
	public Number getValue(final int item) {

		Number result = null;
		final KeyedValue kval = (KeyedValue) this.data.get(item);
		if (kval != null) {
			result = kval.getValue();
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
		final KeyedValue item = (KeyedValue) this.data.get(index);
		if (item != null) {
			result = item.getKey();
		}
		return result;

	}

	/**
	 * Returns the index for a given key or -1 if the key is not found.
	 * 
	 * @param key
	 *           the key.
	 * @return the index.
	 */
	public int getIndex(final Comparable key) {

		int i = 0;
		final Iterator iterator = this.data.iterator();
		while (iterator.hasNext()) {
			final KeyedValue kv = (KeyedValue) iterator.next();
			if (kv.getKey().equals(key)) {
				return i;
			}
			i++;
		}
		return -1; // key not found

	}

	/**
	 * Returns the keys for the values in the collection.
	 * 
	 * @return The keys (never <code>null</code>).
	 */
	public List getKeys() {

		final List result = new java.util.ArrayList();
		final Iterator iterator = this.data.iterator();
		while (iterator.hasNext()) {
			final KeyedValue kv = (KeyedValue) iterator.next();
			result.add(kv.getKey());
		}
		return result;

	}

	/**
	 * Returns the value (possibly <code>null</code>) for a given key. If the key is not
	 * recognised, the method returns <code>null</code>.
	 * 
	 * @param key
	 *           the key.
	 * @return the value.
	 */
	public Number getValue(final Comparable key) {

		Number result = null;
		final int index = getIndex(key);
		if (index >= 0) {
			result = getValue(index);
		}
		return result;

	}

	/**
	 * Adds a new value to the collection, or updates an existing value.
	 * <P>
	 * This is the same as the setValue(...) method.
	 * 
	 * @param key
	 *           the key.
	 * @param value
	 *           the value.
	 */
	public void addValue(final Comparable key, final Number value) {
		setValue(key, value);
	}

	/**
	 * Updates an existing value, or adds a new value to the collection.
	 * <P>
	 * This is the same as the addValue(...) method.
	 * 
	 * @param key
	 *           the key.
	 * @param value
	 *           the value.
	 */
	public void setValue(final Comparable key, final Number value) {
		final int keyIndex = getIndex(key);
		if (keyIndex >= 0) {
			final DefaultKeyedValue kv = (DefaultKeyedValue) this.data.get(keyIndex);
			kv.setValue(value);
		} else {
			final KeyedValue kv = new DefaultKeyedValue(key, value);
			this.data.add(kv);
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
	 * Removes a value from the collection. If there is no value with the specified key,
	 * then this method does nothing.
	 * 
	 * @param key
	 *           the key of the item to remove.
	 */
	public void removeValue(final Comparable key) {
		final int index = getIndex(key);
		if (index >= 0) {
			removeValue(index);
		}
	}

	/**
	 * Sorts the items in the list by key.
	 * 
	 * @param order
	 *           the sort order (ascending or descending).
	 */
	public void sortByKeys(final SortOrder order) {

		final Comparator comparator = new KeyedValueComparator(
							KeyedValueComparatorType.BY_KEY, order
							);
		Collections.sort(this.data, comparator);
	}

	/**
	 * Sorts the items in the list by value. If the list contains <code>null</code> values, they
	 * will sort to the end of the list, irrespective of the sort order.
	 * 
	 * @param order
	 *           the sort order (ascending or descending).
	 */
	public void sortByValues(final SortOrder order) {
		final Comparator comparator = new KeyedValueComparator(
							KeyedValueComparatorType.BY_VALUE, order
							);
		Collections.sort(this.data, comparator);
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

		if (!(o instanceof KeyedValues)) {
			return false;
		}

		final KeyedValues kvs = (KeyedValues) o;
		final int count = getItemCount();
		if (count != kvs.getItemCount()) {
			return false;
		}

		for (int i = 0; i < count; i++) {
			final Comparable k1 = getKey(i);
			final Comparable k2 = kvs.getKey(i);
			if (!k1.equals(k2)) {
				return false;
			}
			final Number v1 = getValue(i);
			final Number v2 = kvs.getValue(i);
			if (v1 == null) {
				if (v2 != null) {
					return false;
				}
			} else {
				if (!v1.equals(v2)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns a hash code.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		return (this.data != null ? this.data.hashCode() : 0);
	}

	/**
	 * Returns a clone.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            this class will not throw this exception, but subclasses
	 *            might.
	 */
	public Object clone() throws CloneNotSupportedException {
		final DefaultKeyedValues clone = (DefaultKeyedValues) super.clone();
		clone.data = new java.util.ArrayList();
		final Iterator iterator = this.data.iterator();
		while (iterator.hasNext()) {
			final DefaultKeyedValue kv = (DefaultKeyedValue) iterator.next();
			clone.data.add(kv.clone());
		}
		return clone;
	}

}
