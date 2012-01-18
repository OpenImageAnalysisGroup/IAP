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
 * ---------------------------------------
 * AbstractCategoryItemLabelGenerator.java
 * ---------------------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: AbstractCategoryItemLabelGenerator.java,v 1.1 2011-01-31 09:02:38 klukas Exp $
 * Changes
 * -------
 * 11-May-2004 : Version 1, distilled from StandardCategoryItemLabelGenerator (DG);
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.jfree.data.CategoryDataset;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * A base class that can be used to create a label or tooltip generator that can be
 * assigned to a {@link org.jfree.chart.renderer.CategoryItemRenderer}.
 */
public abstract class AbstractCategoryItemLabelGenerator implements PublicCloneable,
																							Cloneable,
																							Serializable {

	/**
	 * The label format string (used by a MessageFormat object to combine the standard
	 * items. {0} = series name, {1} = category, {2} = value.
	 */
	private String labelFormat;

	/**
	 * A number formatter (used to preformat the value before it is passed to the
	 * MessageFormat object.
	 */
	private NumberFormat numberFormat;

	/**
	 * A date formatter (used to preformat the value before it is passed to the
	 * MessageFormat object.
	 */
	private DateFormat dateFormat;

	/**
	 * Creates a label generator with the specified number formatter.
	 * 
	 * @param labelFormat
	 *           the label format string (<code>null</code> not permitted).
	 * @param formatter
	 *           the number formatter (<code>null</code> not permitted).
	 */
	protected AbstractCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter) {
		if (labelFormat == null) {
			throw new IllegalArgumentException("Null 'labelFormat' argument.");
		}
		if (formatter == null) {
			throw new IllegalArgumentException("Null 'formatter' argument.");
		}
		this.labelFormat = labelFormat;
		this.numberFormat = formatter;
		this.dateFormat = null;
	}

	/**
	 * Creates a label generator with the specified date formatter.
	 * 
	 * @param labelFormat
	 *           the label format string (<code>null</code> not permitted).
	 * @param formatter
	 *           the date formatter (<code>null</code> not permitted).
	 */
	protected AbstractCategoryItemLabelGenerator(String labelFormat, DateFormat formatter) {
		if (labelFormat == null) {
			throw new IllegalArgumentException("Null 'labelFormat' argument.");
		}
		if (formatter == null) {
			throw new IllegalArgumentException("Null 'formatter' argument.");
		}
		this.labelFormat = labelFormat;
		this.numberFormat = null;
		this.dateFormat = formatter;
	}

	/**
	 * Returns the label format string.
	 * 
	 * @return The label format string (never <code>null</code>).
	 */
	public String getLabelFormat() {
		return this.labelFormat;
	}

	/**
	 * Returns the number formatter.
	 * 
	 * @return The number formatter (possibly <code>null</code>).
	 */
	public NumberFormat getNumberFormat() {
		return this.numberFormat;
	}

	/**
	 * Returns the date formatter.
	 * 
	 * @return The date formatter (possibly <code>null</code>).
	 */
	public DateFormat getDateFormat() {
		return this.dateFormat;
	}

	/**
	 * Generates a for the specified item.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> not permitted).
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The label (possibly <code>null</code>).
	 */
	protected String generateLabelString(CategoryDataset dataset, int row, int column) {
		if (dataset == null) {
			throw new IllegalArgumentException("Null 'dataset' argument.");
		}
		String result = null;
		Object[] items = createItemArray(dataset, row, column);
		result = MessageFormat.format(this.labelFormat, items);
		return result;

	}

	/**
	 * Creates the array of items that can be passed to the {@link MessageFormat} class
	 * for creating labels.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> not permitted).
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The items (never <code>null</code>).
	 */
	protected Object[] createItemArray(CategoryDataset dataset, int row, int column) {
		Object[] result = new Object[3];
		result[0] = dataset.getRowKey(row).toString();
		result[1] = dataset.getColumnKey(column).toString();
		Number value = dataset.getValue(row, column);
		if (this.numberFormat != null) {
			result[2] = this.numberFormat.format(value);
		} else
			if (this.dateFormat != null) {
				result[2] = this.dateFormat.format(value);
			}
		return result;
	}

	/**
	 * Tests this object for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the other object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof AbstractCategoryItemLabelGenerator) {
			AbstractCategoryItemLabelGenerator g = (AbstractCategoryItemLabelGenerator) obj;
			boolean b0 = this.labelFormat.equals(g.labelFormat);
			boolean b1 = ObjectUtils.equal(this.dateFormat, g.dateFormat);
			boolean b2 = ObjectUtils.equal(this.numberFormat, g.numberFormat);
			return b0 && b1 && b2;
		}
		return false;
	}

	/**
	 * Returns an independent copy of the generator.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            should not happen.
	 */
	public Object clone() throws CloneNotSupportedException {

		AbstractCategoryItemLabelGenerator clone = (AbstractCategoryItemLabelGenerator) super.clone();

		if (this.numberFormat != null) {
			clone.numberFormat = (NumberFormat) this.numberFormat.clone();
		}

		if (this.dateFormat != null) {
			clone.dateFormat = (DateFormat) this.dateFormat.clone();
		}

		return clone;

	}

}
