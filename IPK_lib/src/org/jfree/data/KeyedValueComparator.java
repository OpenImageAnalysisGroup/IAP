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
 * -------------------------
 * KeyedValueComparator.java
 * -------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: KeyedValueComparator.java,v 1.1 2011-01-31 09:02:18 klukas Exp $
 * Changes:
 * --------
 * 05-Mar-2003 : Version 1 (DG);
 * 27-Aug-2003 : Moved SortOrder from org.jfree.data --> org.jfree.util (DG);
 */

package org.jfree.data;

import java.util.Comparator;

import org.jfree.util.SortOrder;

/**
 * A utility class that can compare and order two {@link KeyedValue} instances and sort them
 * into ascending or descending order by key or by value.
 */
public class KeyedValueComparator implements Comparator {

	/** The comparator type. */
	private KeyedValueComparatorType type;

	/** The sort order. */
	private SortOrder order;

	/**
	 * Creates a new comparator.
	 * 
	 * @param type
	 *           the type (<code>BY_KEY</code> or <code>BY_VALUE</code>).
	 * @param order
	 *           the order (ascending or descending).
	 */
	public KeyedValueComparator(final KeyedValueComparatorType type, final SortOrder order) {
		this.type = type;
		this.order = order;
	}

	/**
	 * Compares two {@link KeyedValue} instances and returns an <code>int</code> that indicates the
	 * relative order of the two objects.
	 * 
	 * @param o1
	 *           object 1.
	 * @param o2
	 *           object 2.
	 * @return An int indicating the relative order of the objects.
	 */
	public int compare(final Object o1, final Object o2) {

		if (o2 == null) {
			return -1;
		}
		if (o1 == null) {
			return 1;
		}

		final int result;

		final KeyedValue kv1 = (KeyedValue) o1;
		final KeyedValue kv2 = (KeyedValue) o2;

		if (this.type == KeyedValueComparatorType.BY_KEY) {
			if (this.order.equals(SortOrder.ASCENDING)) {
				result = kv1.getKey().compareTo(kv2.getKey());
			} else
				if (this.order.equals(SortOrder.DESCENDING)) {
					result = kv2.getKey().compareTo(kv1.getKey());
				} else {
					throw new IllegalArgumentException("KeyedValueComparator.compare(...) : "
																	+ "unrecognised sort order.");
				}
		} else
			if (this.type == KeyedValueComparatorType.BY_VALUE) {
				final Number n1 = kv1.getValue();
				final Number n2 = kv2.getValue();
				if (n2 == null) {
					return -1;
				}
				if (n1 == null) {
					return 1;
				}
				final double d1 = n1.doubleValue();
				final double d2 = n2.doubleValue();
				if (this.order.equals(SortOrder.ASCENDING)) {
					if (d1 > d2) {
						result = 1;
					} else
						if (d1 < d2) {
							result = -1;
						} else {
							result = 0;
						}
				} else
					if (this.order.equals(SortOrder.DESCENDING)) {
						if (d1 > d2) {
							result = -1;
						} else
							if (d1 < d2) {
								result = 1;
							} else {
								result = 0;
							}
					} else {
						throw new IllegalArgumentException("KeyedValueComparator.compare(...) : "
																	+ "unrecognised sort order.");
					}
			} else {
				throw new IllegalArgumentException("KeyedValueComparator.compare(...) : "
																+ "unrecognised type.");
			}

		return result;
	}

}
