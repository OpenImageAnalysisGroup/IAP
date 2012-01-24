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
 * -------------
 * HashNMap.java
 * -------------
 * (C)opyright 2002-2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: HashNMap.java,v 1.1 2011-01-31 09:01:40 klukas Exp $
 * Changes
 * -------
 * 20-May-2002 : Initial version
 * 10-Dec-2002 : Minor Javadoc updates (DG);
 * 29-Jul-2004 : Replaced 'enum' variable name (reserved word in JDK 1.5) (DG);
 */

package org.jfree.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * The HashNMap can be used to store multiple values by a single key value. The values stored
 * can be retrieved using a direct query or by creating an enumeration over the stored elements.
 * 
 * @author Thomas Morgner
 */
public class HashNMap implements Serializable, Cloneable {

	/**
	 * An helper class to implement an empty iterator. This iterator will always
	 * return false when <code>hasNext</code> is called.
	 */
	private static final class EmptyIterator implements Iterator {

		/**
		 * DefaultConstructor.
		 */
		private EmptyIterator() {
			super();
		}

		/**
		 * Returns <tt>true</tt> if the iteration has more elements. (In other
		 * words, returns <tt>true</tt> if <tt>next</tt> would return an element
		 * rather than throwing an exception.)
		 * 
		 * @return <tt>true</tt> if the iterator has more elements.
		 */
		public boolean hasNext() {
			return false;
		}

		/**
		 * Returns the next element in the iteration.
		 * 
		 * @return the next element in the iteration.
		 */
		// @exception NoSuchElementException iteration has no more elements.
		public Object next() {
			throw new NoSuchElementException("This iterator is empty.");
		}

		/**
		 * Removes from the underlying collection the last element returned by the
		 * iterator (optional operation). This method can be called only once per
		 * call to <tt>next</tt>. The behavior of an iterator is unspecified if
		 * the underlying collection is modified while the iteration is in
		 * progress in any way other than by calling this method.
		 */
		// @exception UnsupportedOperationException if the <tt>remove</tt>
		// operation is not supported by this Iterator.
		// @exception IllegalStateException if the <tt>next</tt> method has not
		// yet been called, or the <tt>remove</tt> method has already
		// been called after the last call to the <tt>next</tt>
		// method.
		public void remove() {
			throw new UnsupportedOperationException("This iterator is empty, no remove supported.");
		}
	}

	/** A singleton instance of the empty iterator. This object can be safely shared. */
	private static final Iterator EMPTY_ITERATOR = new EmptyIterator();

	/** The underlying storage. */
	private HashMap table = null;

	/**
	 * Default constructor.
	 */
	public HashNMap() {
		this.table = new HashMap();
	}

	/**
	 * Inserts a new key/value pair into the map. If such a pair already exists, it gets replaced
	 * with the given values.
	 * 
	 * @param key
	 *           the key.
	 * @param val
	 *           the value.
	 */
	public void put(final Object key, final Object val) {
		final ArrayList v = new ArrayList();
		v.add(val);
		this.table.put(key, v);
	}

	/**
	 * Adds a new key/value pair into this map. If the key is not yet in the map, it gets added
	 * to the map and the call is equal to put(Object,Object).
	 * 
	 * @param key
	 *           the key.
	 * @param val
	 *           the value.
	 */
	public void add(final Object key, final Object val) {
		final ArrayList v = (ArrayList) this.table.get(key);
		if (v == null) {
			put(key, val);
		} else {
			v.add(val);
		}
	}

	/**
	 * Retrieves the first value registered for an key or null if there was no such key
	 * in the list.
	 * 
	 * @param key
	 *           the key.
	 * @return the value.
	 */
	public Object getFirst(final Object key) {
		return get(key, 0);
	}

	/**
	 * Retrieves the n-th value registered for an key or null if there was no such key
	 * in the list. An index out of bounds exception is thrown if there are less than
	 * n elements registered to this key.
	 * 
	 * @param key
	 *           the key.
	 * @param n
	 *           the index.
	 * @return the object.
	 */
	public Object get(final Object key, final int n) {
		final ArrayList v = (ArrayList) this.table.get(key);
		if (v == null) {
			return null;
		}
		return v.get(n);
	}

	/**
	 * Returns an iterator over all elements registered to the given key.
	 * 
	 * @param key
	 *           the key.
	 * @return an iterator.
	 */
	public Iterator getAll(final Object key) {
		final ArrayList v = (ArrayList) this.table.get(key);
		if (v == null) {
			return EMPTY_ITERATOR;
		}
		return v.iterator();
	}

	/**
	 * Returns all registered keys as an enumeration.
	 * 
	 * @return an enumeration of the keys.
	 */
	public Iterator keys() {
		return this.table.keySet().iterator();
	}

	/**
	 * Returns all registered keys as set.
	 * 
	 * @return a set of keys.
	 */
	public Set keySet() {
		return this.table.keySet();
	}

	/**
	 * Removes the key/value pair from the map. If the removed entry was the last entry
	 * for this key, the key gets also removed.
	 * 
	 * @param key
	 *           the key.
	 * @param value
	 *           the value.
	 * @return true, if removing the element was successfull, false otherwise.
	 */
	public boolean remove(final Object key, final Object value) {
		final ArrayList v = (ArrayList) this.table.get(key);
		if (v == null) {
			return false;
		}

		if (!v.remove(value)) {
			return false;
		}
		if (v.size() == 0) {
			this.table.remove(key);
		}
		return true;
	}

	/**
	 * Removes all elements for the given key.
	 * 
	 * @param key
	 *           the key.
	 */
	public void removeAll(final Object key) {
		this.table.remove(key);
	}

	/**
	 * Clears all keys and values of this map.
	 */
	public void clear() {
		this.table.clear();
	}

	/**
	 * Tests whether this map contains the given key.
	 * 
	 * @param key
	 *           the key.
	 * @return true if the key is contained in the map
	 */
	public boolean containsKey(final Object key) {
		return this.table.containsKey(key);
	}

	/**
	 * Tests whether this map contains the given value.
	 * 
	 * @param value
	 *           the value.
	 * @return true if the value is registered in the map for an key.
	 */
	public boolean containsValue(final Object value) {
		final Iterator e = this.table.values().iterator();
		boolean found = false;
		while (e.hasNext() && !found) {
			final ArrayList v = (ArrayList) e.next();
			found = v.contains(value);
		}
		return found;
	}

	/**
	 * Tests whether this map contains the given value.
	 * 
	 * @param value
	 *           the value.
	 * @param key
	 *           the key under which to find the value
	 * @return true if the value is registered in the map for an key.
	 */
	public boolean containsValue(final Object key, final Object value) {
		final ArrayList v = (ArrayList) this.table.get(key);
		if (v == null) {
			return false;
		}
		return v.contains(value);
	}

	/**
	 * Tests whether this map contains the given key or value.
	 * 
	 * @param value
	 *           the value.
	 * @return true if the key or value is contained in the map
	 */
	public boolean contains(final Object value) {
		if (containsKey(value)) {
			return true;
		}
		return containsValue(value);
	}

	/**
	 * Creates a deep copy of this HashNMap.
	 * 
	 * @return a clone.
	 * @throws CloneNotSupportedException
	 *            this should never happen.
	 */
	public Object clone() throws CloneNotSupportedException {
		final HashNMap map = (HashNMap) super.clone();
		map.table = new HashMap();
		final Iterator iterator = keys();
		while (iterator.hasNext()) {
			final Object key = iterator.next();
			final ArrayList list = (ArrayList) map.table.get(key);
			if (list != null) {
				map.table.put(key, list.clone());
			}
		}
		return map;
	}

	/**
	 * Returns the contents for the given key as object array. If there were
	 * no objects registered with that key, an empty object array is returned.
	 * 
	 * @param key
	 *           the key.
	 * @param data
	 *           the object array to receive the contents.
	 * @return the contents.
	 */
	public Object[] toArray(final Object key, final Object[] data) {
		if (key == null) {
			throw new NullPointerException("Key must not be null.");
		}
		final ArrayList list = (ArrayList) this.table.get(key);
		if (list != null) {
			return list.toArray(data);
		}
		return new Object[0];
	}

	/**
	 * Returns the contents for the given key as object array. If there were
	 * no objects registered with that key, an empty object array is returned.
	 * 
	 * @param key
	 *           the key.
	 * @return the contents.
	 */
	public Object[] toArray(final Object key) {
		if (key == null) {
			throw new NullPointerException("Key must not be null.");
		}
		final ArrayList list = (ArrayList) this.table.get(key);
		if (list != null) {
			return list.toArray();
		}
		return new Object[0];
	}

	/**
	 * Returns the number of elements registered with the given key.
	 * 
	 * @param key
	 *           the key.
	 * @return the number of element for this key, or 0 if there are no elements
	 *         registered.
	 */
	public int getValueCount(final Object key) {
		if (key == null) {
			throw new NullPointerException("Key must not be null.");
		}
		final ArrayList list = (ArrayList) this.table.get(key);
		if (list != null) {
			return list.size();
		}
		return 0;
	}
}
