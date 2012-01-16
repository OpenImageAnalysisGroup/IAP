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
 * -----------------------
 * SortableTableModel.java
 * -----------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited;
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: SortableTableModel.java,v 1.1 2011-01-31 09:02:25 klukas Exp $
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.* (DG);
 * 20-Nov-2001 : Made constructor protected (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.ui;

import javax.swing.table.AbstractTableModel;

/**
 * The base class for a sortable table model.
 */
public abstract class SortableTableModel extends AbstractTableModel {

	/** The column on which the data is sorted (-1 for no sorting). */
	private int sortingColumn;

	/** Indicates ascending (true) or descending (false) order. */
	private boolean ascending;

	/**
	 * Constructs a sortable table model.
	 */
	public SortableTableModel() {
		this.sortingColumn = -1;
		this.ascending = true;
	}

	/**
	 * Returns the index of the sorting column, or -1 if the data is not sorted on any column.
	 * 
	 * @return the column used for sorting.
	 */
	public int getSortingColumn() {
		return this.sortingColumn;
	}

	/**
	 * Returns true if the data is sorted in ascending order, and false otherwise.
	 * 
	 * @return true if the data is sorted in ascending order, and false otherwise.
	 */
	public boolean getAscending() {
		return this.ascending;
	}

	/**
	 * Sets the flag that determines whether the sort order is ascending or descending.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setAscending(final boolean flag) {
		this.ascending = flag;
	}

	/**
	 * Sorts the table.
	 * 
	 * @param column
	 *           the column to sort on (zero-based index).
	 * @param ascending
	 *           a flag to indicate ascending order or descending order.
	 */
	public void sortByColumn(final int column, final boolean ascending) {
		if (isSortable(column)) {
			this.sortingColumn = column;
		}
	}

	/**
	 * Returns a flag indicating whether or not a column is sortable.
	 * 
	 * @param column
	 *           the column (zero-based index).
	 * @return boolean.
	 */
	public boolean isSortable(final int column) {
		return false;
	}

}
