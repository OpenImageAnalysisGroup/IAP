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
 * NormalizedMatrixSeries.java
 * ---------------------------
 * (C) Copyright 2003, 2004, by Barak Naveh and Contributors.
 * Original Author: Barak Naveh;;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: NormalizedMatrixSeries.java,v 1.1 2011-01-31 09:02:14 klukas Exp $
 * Changes
 * -------
 * 10-Jul-2003 : Version 1 contributed by Barak Naveh (DG);
 */

package org.jfree.data;

/**
 * Represents a dense normalized matrix M[i,j] where each Mij item of the
 * matrix has a value (default is 0). When a matrix item is observed using <code>getItem</code> method, it is normalized, that is, divided by the
 * total sum of all items. It can be also be scaled by setting a scale factor.
 * 
 * @author Barak Naveh
 * @since Jun 18, 2003
 */
public class NormalizedMatrixSeries extends MatrixSeries {

	/** The default scale factor. */
	public static final double DEFAULT_SCALE_FACTOR = 1.0;

	/**
	 * A factor that multiplies each item in this series when observed using getItem method.
	 */
	private double m_scaleFactor = DEFAULT_SCALE_FACTOR;

	/** The sum of all items in this matrix */
	private double m_totalSum;

	/**
	 * Constructor for NormalizedMatrixSeries.
	 * 
	 * @param name
	 *           the series name.
	 * @param rows
	 *           the number of rows.
	 * @param columns
	 *           the number of columns.
	 */
	public NormalizedMatrixSeries(final String name, final int rows, final int columns) {
		super(name, rows, columns);

		/*
		 * we assum super is always initialized to all-zero matrix, so the
		 * total sum should be 0 upon initialization. However, we set it to
		 * Double.MIN_VALUE to get the same effect and yet avoid division by 0
		 * upon initialization.
		 */
		this.m_totalSum = Double.MIN_VALUE;
	}

	/**
	 * Returns an item.
	 * 
	 * @param itemIndex
	 *           the index.
	 * @return The value.
	 * @see org.jfree.data.MatrixSeries#getItem(int)
	 */
	public Number getItem(final int itemIndex) {
		final int i = getItemRow(itemIndex);
		final int j = getItemColumn(itemIndex);

		final double mij = get(i, j) * this.m_scaleFactor;
		final Number n = new Double(mij / this.m_totalSum);

		return n;
	}

	/**
	 * Sets the factor that multiplies each item in this series when observed
	 * using getItem mehtod.
	 * 
	 * @param factor
	 *           new factor to set.
	 * @see #DEFAULT_SCALE_FACTOR
	 */
	public void setScaleFactor(final double factor) {
		this.m_scaleFactor = factor;
	}

	/**
	 * Returns the factor that multiplies each item in this series when
	 * observed using getItem mehtod.
	 * 
	 * @return the factor
	 */
	public double getScaleFactor() {
		return this.m_scaleFactor;
	}

	/**
	 * @see org.jfree.data.MatrixSeries#update(int, int, double)
	 */
	public void update(final int i, final int j, final double mij) {
		this.m_totalSum -= get(i, j);
		this.m_totalSum += mij;

		super.update(i, j, mij);
	}

	/**
	 * @see org.jfree.data.MatrixSeries#zeroAll()
	 */
	public void zeroAll() {
		this.m_totalSum = 0;
		super.zeroAll();
	}
}
