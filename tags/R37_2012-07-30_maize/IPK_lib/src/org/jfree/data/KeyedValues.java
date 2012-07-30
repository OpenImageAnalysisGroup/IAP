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
 * KeyedValues.java
 * ----------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: KeyedValues.java,v 1.1 2011-01-31 09:02:12 klukas Exp $
 * Changes:
 * --------
 * 23-Oct-2002 : Version 1 (DG);
 */

package org.jfree.data;

import java.util.List;

/**
 * A collection of values where each value is associated with a key.
 * 
 * @see Values
 * @see DefaultKeyedValues
 */
public interface KeyedValues extends Values {

	/**
	 * Returns the key associated with an item (value).
	 * 
	 * @param index
	 *           the item index (zero-based).
	 * @return the key.
	 */
	public Comparable getKey(int index);

	/**
	 * Returns the index for a given key.
	 * 
	 * @param key
	 *           the key.
	 * @return the index (-1 indicates that the key is not in the collection).
	 */
	public int getIndex(Comparable key);

	/**
	 * Returns the keys for the values in the collection. Note that you can access the values in
	 * this collection by key or by index. For this reason, the key order is important - this
	 * method should return the keys in order.
	 * 
	 * @return The keys (never <code>null</code>).
	 */
	public List getKeys();

	/**
	 * Returns the value (possibly <code>null</code>) for a given key.
	 * <P>
	 * If the key is not recognised, the method should return <code>null</code>.
	 * 
	 * @param key
	 *           the key.
	 * @return the value.
	 */
	public Number getValue(Comparable key);

}
