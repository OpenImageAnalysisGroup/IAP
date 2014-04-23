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
 * ---------------------------
 * DefaultCategoryDataset.java
 * ---------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DefaultCategoryDataset.java,v 1.1 2011-01-31 09:02:18 klukas Exp $
 * Changes
 * -------
 * 21-Jan-2003 : Added standard header, and renamed DefaultCategoryDataset (DG);
 * 13-Mar-2003 : Inserted DefaultKeyedValues2DDataset into class hierarchy (DG);
 * 06-Oct-2003 : Added incrementValue() method (DG);
 * 05-Apr-2004 : Added clear() method (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.List;

/**
 * A default implementation of the {@link CategoryDataset} interface.
 */
public class DefaultCategoryDataset extends AbstractDataset
												implements CategoryDataset, Serializable {

	/** A storage structure for the data. */
	private DefaultKeyedValues2D data;

	/**
	 * Creates a new (empty) dataset.
	 */
	public DefaultCategoryDataset() {
		this.data = new DefaultKeyedValues2D();
	}

	/**
	 * Returns the number of rows in the table.
	 * 
	 * @return the row count.
	 */
	public int getRowCount() {
		return this.data.getRowCount();
	}

	/**
	 * Returns the number of columns in the table.
	 * 
	 * @return the column count.
	 */
	public int getColumnCount() {
		return this.data.getColumnCount();
	}

	/**
	 * Returns a value from the table.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return the value (possibly null).
	 */
	public Number getValue(final int row, final int column) {
		return this.data.getValue(row, column);
	}

	/**
	 * Returns a row key.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @return the row key.
	 */
	public Comparable getRowKey(final int row) {
		return this.data.getRowKey(row);
	}

	/**
	 * Returns the row index for a given key.
	 * 
	 * @param key
	 *           the row key.
	 * @return the row index.
	 */
	public int getRowIndex(final Comparable key) {
		return this.data.getRowIndex(key);
	}

	/**
	 * Returns the row keys.
	 * 
	 * @return the keys.
	 */
	public List getRowKeys() {
		return this.data.getRowKeys();
	}

	/**
	 * Returns a column key.
	 * 
	 * @param column
	 *           the column index (zero-based).
	 * @return the column key.
	 */
	public Comparable getColumnKey(final int column) {
		return this.data.getColumnKey(column);
	}

	/**
	 * Returns the column index for a given key.
	 * 
	 * @param key
	 *           the column key.
	 * @return the column index.
	 */
	public int getColumnIndex(final Comparable key) {
		return this.data.getColumnIndex(key);
	}

	/**
	 * Returns the column keys.
	 * 
	 * @return the keys.
	 */
	public List getColumnKeys() {
		return this.data.getColumnKeys();
	}

	/**
	 * Returns the value for a pair of keys.
	 * <P>
	 * This method should return <code>null</code> if either of the keys is not found.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @return the value.
	 */
	public Number getValue(final Comparable rowKey, final Comparable columnKey) {
		return this.data.getValue(rowKey, columnKey);
	}

	/**
	 * Adds a value to the table. Performs the same function as setValue(...).
	 * 
	 * @param value
	 *           the value.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void addValue(final Number value, final Comparable rowKey, final Comparable columnKey) {
		this.data.addValue(value, rowKey, columnKey);
		fireDatasetChanged();
	}

	/**
	 * Adds a value to the table.
	 * 
	 * @param value
	 *           the value.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void addValue(final double value, final Comparable rowKey, final Comparable columnKey) {
		addValue(new Double(value), rowKey, columnKey);
	}

	/**
	 * Adds or updates a value in the table.
	 * 
	 * @param value
	 *           the value.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void setValue(final Number value, final Comparable rowKey, final Comparable columnKey) {
		this.data.setValue(value, rowKey, columnKey);
		fireDatasetChanged();
	}

	/**
	 * Adds or updates a value in the table.
	 * 
	 * @param value
	 *           the value.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void setValue(final double value, final Comparable rowKey, final Comparable columnKey) {
		setValue(new Double(value), rowKey, columnKey);
	}

	/**
	 * Adds the specified value to an existing value in the dataset (if the existing value is <code>null</code>, it is treated as if it were 0.0).
	 * 
	 * @param value
	 *           the value.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void incrementValue(final double value,
											final Comparable rowKey,
											final Comparable columnKey) {
		double existing = 0.0;
		final Number n = getValue(rowKey, columnKey);
		if (n != null) {
			existing = n.doubleValue();
		}
		setValue(existing + value, rowKey, columnKey);
	}

	/**
	 * Removes a value from the dataset.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 */
	public void removeValue(final Comparable rowKey, final Comparable columnKey) {
		this.data.removeValue(rowKey, columnKey);
		fireDatasetChanged();
	}

	/**
	 * Removes a row from the dataset.
	 * 
	 * @param rowIndex
	 *           the row index.
	 */
	public void removeRow(final int rowIndex) {
		this.data.removeRow(rowIndex);
		fireDatasetChanged();
	}

	/**
	 * Removes a row from the dataset.
	 * 
	 * @param rowKey
	 *           the row key.
	 */
	public void removeRow(final Comparable rowKey) {
		this.data.removeRow(rowKey);
		fireDatasetChanged();
	}

	/**
	 * Removes a column from the dataset.
	 * 
	 * @param columnIndex
	 *           the column index.
	 */
	public void removeColumn(final int columnIndex) {
		this.data.removeColumn(columnIndex);
		fireDatasetChanged();
	}

	/**
	 * Removes a column from the dataset.
	 * 
	 * @param columnKey
	 *           the column key.
	 */
	public void removeColumn(final Comparable columnKey) {
		this.data.removeColumn(columnKey);
		fireDatasetChanged();
	}

	/**
	 * Clears all data from the dataset and sends a {@link DatasetChangeEvent} to all registered
	 * listeners.
	 */
	public void clear() {
		this.data.clear();
		fireDatasetChanged();
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

		if (!(o instanceof CategoryDataset)) {
			return false;
		}

		final CategoryDataset cd = (CategoryDataset) o;
		if (!getRowKeys().equals(cd.getRowKeys())) {
			return false;
		}

		if (!getColumnKeys().equals(cd.getColumnKeys())) {
			return false;
		}

		final int rowCount = getRowCount();
		final int colCount = getColumnCount();
		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < colCount; c++) {
				final Number v1 = getValue(r, c);
				final Number v2 = cd.getValue(r, c);
				if (v1 == null) {
					if (v2 != null) {
						return false;
					}
				} else
					if (!v1.equals(v2)) {
						return false;
					}
			}
		}
		return true;
	}

	/**
	 * Returns a hash code for the dataset.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		return this.data.hashCode();
	}

}
