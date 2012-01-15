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
 * ----------------------------------
 * StandardPieItemLabelGenerator.java
 * ----------------------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Richard Atkinson;
 * Andreas Schroeder;
 * $Id: StandardPieItemLabelGenerator.java,v 1.1 2011-01-31 09:02:38 klukas Exp $
 * Changes
 * -------
 * 13-Dec-2001 : Version 1 (DG);
 * 16-Jan-2002 : Completed Javadocs (DG);
 * 29-Aug-2002 : Changed to format numbers using default locale (RA);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 30-Oct-2002 : Changed PieToolTipGenerator interface (DG);
 * 21-Mar-2003 : Implemented Serializable (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 19-Aug-2003 : Renamed StandardPieToolTipGenerator --> StandardPieItemLabelGenerator (DG);
 * 10-Mar-2004 : Modified to use MessageFormat class (DG);
 * 31-Mar-2004 : Added javadocs for the MessageFormat usage (AS);
 * 15-Apr-2004 : Split PieItemLabelGenerator interface into PieSectionLabelGenerator and
 * PieToolTipGenerator (DG);
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.jfree.data.DatasetUtilities;
import org.jfree.data.PieDataset;

/**
 * <p>
 * A standard item label generator for plots that use data from a {@link PieDataset}.
 * <p>
 * For the label format, use {0} where the pie section key should be inserted, {1} for the absolute section value and {2} for the percent amount of the pie
 * section, e.g. <code>"{0} = {1} ({2})"</code> will display as <code>apple = 120 (5%)</code>.
 */
public class StandardPieItemLabelGenerator implements PieToolTipGenerator,
																		PieSectionLabelGenerator,
																		Cloneable,
																		Serializable {

	/** The default tooltip format. */
	public static final String DEFAULT_TOOLTIP_FORMAT = "{0}: ({1}, {2})";

	/** The default section label format. */
	public static final String DEFAULT_SECTION_LABEL_FORMAT = "{0} = {1}";

	/** The label format string. */
	private String labelFormat;

	/** A number formatter for the value. */
	private NumberFormat numberFormat;

	/** A number formatter for the percentage. */
	private NumberFormat percentFormat;

	/**
	 * Creates an item label generator using default number formatters.
	 */
	public StandardPieItemLabelGenerator() {
		this(
							DEFAULT_SECTION_LABEL_FORMAT,
							NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance());
	}

	/**
	 * Creates an item label generator.
	 * 
	 * @param labelFormat
	 *           the label format.
	 */
	public StandardPieItemLabelGenerator(String labelFormat) {
		this(labelFormat, NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance());
	}

	/**
	 * Creates an item label generator using the specified number formatters.
	 * 
	 * @param labelFormat
	 *           the label format string (<code>null</code> not permitted).
	 * @param numberFormat
	 *           the format object for the values (<code>null</code> not permitted).
	 * @param percentFormat
	 *           the format object for the percentages (<code>null</code> not
	 *           permitted).
	 */
	public StandardPieItemLabelGenerator(String labelFormat,
														NumberFormat numberFormat,
														NumberFormat percentFormat) {

		if (labelFormat == null) {
			throw new IllegalArgumentException("Null 'itemLabelFormat' argument.");
		}
		if (numberFormat == null) {
			throw new IllegalArgumentException("Null 'numberFormat' argument.");
		}
		if (percentFormat == null) {
			throw new IllegalArgumentException("Null 'percentFormat' argument.");
		}
		this.labelFormat = labelFormat;
		this.numberFormat = numberFormat;
		this.percentFormat = percentFormat;

	}

	/**
	 * Returns the number formatter.
	 * 
	 * @return The formatter (never <code>null</code>).
	 */
	public NumberFormat getNumberFormat() {
		return this.numberFormat;
	}

	/**
	 * Returns the percent formatter.
	 * 
	 * @return The formatter (never <code>null</code>).
	 */
	public NumberFormat getPercentFormat() {
		return this.percentFormat;
	}

	/**
	 * Generates a label for a pie section.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> not permitted).
	 * @param key
	 *           the section key (<code>null</code> not permitted).
	 * @return The label (possibly <code>null</code>).
	 */
	public String generateSectionLabel(PieDataset dataset, Comparable key) {
		String result = null;
		if (dataset != null) {
			Object[] items = createItemArray(dataset, key);
			result = MessageFormat.format(this.labelFormat, items);
		}
		return result;
	}

	/**
	 * Generates a tool tip text item for one section in a pie chart.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> not permitted).
	 * @param key
	 *           the section key (<code>null</code> not permitted).
	 * @return The tool tip text (possibly <code>null</code>).
	 */
	public String generateToolTip(PieDataset dataset, Comparable key) {
		return generateSectionLabel(dataset, key);
	}

	/**
	 * Creates the array of items that can be passed to the {@link MessageFormat} class
	 * for creating labels.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> not permitted).
	 * @param key
	 *           the key.
	 * @return The items (never <code>null</code>).
	 */
	protected Object[] createItemArray(PieDataset dataset, Comparable key) {
		Object[] result = new Object[3];
		result[0] = key.toString();
		Number value = dataset.getValue(key);
		result[1] = this.numberFormat.format(value);
		double percent = 0.0;
		if (value != null) {
			double v = value.doubleValue();
			if (v > 0.0) {
				percent = v / DatasetUtilities.calculatePieDatasetTotal(dataset);
			}
		}
		result[2] = this.percentFormat.format(percent);
		return result;
	}

	/**
	 * Tests the generator for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object to test against (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}

		if (obj instanceof StandardPieItemLabelGenerator) {
			StandardPieItemLabelGenerator generator = (StandardPieItemLabelGenerator) obj;
			if (!this.labelFormat.equals(generator.labelFormat)) {
				return false;
			}
			if (!this.numberFormat.equals(generator.numberFormat)) {
				return false;
			}
			if (!this.percentFormat.equals(generator.percentFormat)) {
				return false;
			}
			return true;
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

		StandardPieItemLabelGenerator clone = (StandardPieItemLabelGenerator) super.clone();
		if (this.numberFormat != null) {
			clone.numberFormat = (NumberFormat) this.numberFormat.clone();
		}
		return clone;

	}

}
