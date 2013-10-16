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
 * ------------------
 * KeyedValues2D.java
 * ------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: KeyedValues2D.java,v 1.1 2011-01-31 09:02:14 klukas Exp $
 * Changes
 * -------
 * 28-Oct-2002 : Version 1 (DG);
 */

package org.jfree.data;

import java.util.List;

/**
 * An extension of the {@link Values2D} interface where a unique key is associated with the row
 * and column indices.
 */
public interface KeyedValues2D extends Values2D {

	/**
	 * Returns a row key.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @return the row key.
	 */
	public Comparable getRowKey(int row);

	/**
	 * Returns the row index for a given key.
	 * 
	 * @param key
	 *           the row key.
	 * @return the row index.
	 */
	public int getRowIndex(Comparable key);

	/**
	 * Returns the row keys.
	 * 
	 * @return the keys.
	 */
	public List getRowKeys();

	/**
	 * Returns a column key.
	 * 
	 * @param column
	 *           the column index (zero-based).
	 * @return the column key.
	 */
	public Comparable getColumnKey(int column);

	/**
	 * Returns the column index for a given key.
	 * 
	 * @param key
	 *           the column key.
	 * @return the column index.
	 */
	public int getColumnIndex(Comparable key);

	/**
	 * Returns the column keys.
	 * 
	 * @return the keys.
	 */
	public List getColumnKeys();

	/**
	 * Returns the value for a pair of keys.
	 * <P>
	 * This method should return null if either of the keys is not found.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return the value.
	 */
	public Number getValue(Comparable rowKey, Comparable columnKey);

}
