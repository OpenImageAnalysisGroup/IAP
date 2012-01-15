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
 * MatrixSeries.java
 * -----------------
 * (C) Copyright 2003, 2004, by Barak Naveh and Contributors.
 * Original Author: Barak Naveh;;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Zhitao Wang;
 * $Id: MatrixSeries.java,v 1.1 2011-01-31 09:02:19 klukas Exp $
 * Changes
 * -------
 * 10-Jul-2003 : Version 1 contributed by Barak Naveh (DG);
 * 10-Feb-2004 : Fixed Checkstyle complaints (DG);
 * 21-May-2004 : Fixed bug 940188 - problem in getItemColumn() and getItemRow() (DG);
 */

package org.jfree.data;

import java.io.Serializable;

/**
 * Represents a dense matrix M[i,j] where each Mij item of the matrix has a
 * value (default is 0).
 * 
 * @author Barak Naveh
 */
public class MatrixSeries extends Series implements Serializable {

	/** Series matrix values */
	protected double[][] data;

	/**
	 * Constructs a new matrix series.
	 * <p>
	 * By default, all matrix items are initialzed to 0.
	 * </p>
	 * 
	 * @param name
	 *           series name (<code>null</code> not permitted).
	 * @param rows
	 *           the number of rows.
	 * @param columns
	 *           the number of columns.
	 */
	public MatrixSeries(final String name, final int rows, final int columns) {
		super(name);
		this.data = new double[rows][columns];
		zeroAll();
	}

	/**
	 * Returns the number of columns in this matrix series.
	 * 
	 * @return the number of columns in this matrix series.
	 */
	public int getColumnsCount() {
		return this.data[0].length;
	}

	/**
	 * Return the matrix item at the specified index.
	 * 
	 * @param itemIndex
	 *           item index.
	 * @return matrix item at the specified index.
	 */
	public Number getItem(final int itemIndex) {
		final int i = getItemRow(itemIndex);
		final int j = getItemColumn(itemIndex);

		final Number n = new Double(get(i, j));

		return n;
	}

	/**
	 * Returns the column of the specified item.
	 * 
	 * @param itemIndex
	 *           the index of the item.
	 * @return the column of the specified item.
	 */
	public int getItemColumn(final int itemIndex) {
		// assert itemIndex >= 0 && itemIndex < getItemCount();
		return itemIndex % getColumnsCount();
	}

	/**
	 * Returns the number of items in the series.
	 * 
	 * @return The item count.
	 */
	public int getItemCount() {
		return getRowCount() * getColumnsCount();
	}

	/**
	 * Returns the row of the specified item.
	 * 
	 * @param itemIndex
	 *           the index of the item.
	 * @return the row of the specified item.
	 */
	public int getItemRow(final int itemIndex) {
		// assert itemIndex >= 0 && itemIndex < getItemCount();
		return itemIndex / getColumnsCount();
	}

	/**
	 * Returns the number of rows in this matrix series.
	 * 
	 * @return the number of rows in this matrix series.
	 */
	public int getRowCount() {
		return this.data.length;
	}

	/**
	 * Returns the value of the specified item in this matrix series.
	 * 
	 * @param i
	 *           the row of the item.
	 * @param j
	 *           the column of the item.
	 * @return the value of the specified item in this matrix series.
	 */
	public double get(final int i, final int j) {
		return this.data[i][j];
	}

	/**
	 * Updates the value of the specified item in this matrix series.
	 * 
	 * @param i
	 *           the row of the item.
	 * @param j
	 *           the column of the item.
	 * @param mij
	 *           the new value for the item.
	 */
	public void update(final int i, final int j, final double mij) {
		this.data[i][j] = mij;
		fireSeriesChanged();
	}

	/**
	 * Sets all matrix values to zero and sends a {@link org.jfree.data.SeriesChangeEvent} to all registered listeners.
	 */
	public void zeroAll() {
		final int rows = getRowCount();
		final int columns = getColumnsCount();

		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				this.data[row][column] = 0.0;
			}
		}
		fireSeriesChanged();
	}

	/**
	 * Tests this object instance for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof MatrixSeries && super.equals(obj)) {
			MatrixSeries m = (MatrixSeries) obj;
			if (!(this.getRowCount() == m.getRowCount())) {
				return false;
			}
			if (!(this.getColumnsCount() == m.getColumnsCount())) {
				return false;
			}
			return true;
		}
		return false;
	}

}
