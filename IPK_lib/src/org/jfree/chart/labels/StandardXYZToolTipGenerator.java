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
 * --------------------------------
 * StandardXYZToolTipGenerator.java
 * --------------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: StandardXYZToolTipGenerator.java,v 1.1 2011-01-31 09:02:39 klukas Exp $
 * Changes
 * -------
 * 11-May-2003 : Version 1, split from StandardXYZItemLabelGenerator (DG);
 */

package org.jfree.chart.labels;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.jfree.data.XYZDataset;
import org.jfree.util.ObjectUtils;

/**
 * A standard item label generator for use with {@link XYZDataset} data. Each value
 * can be formatted as a number or as a date.
 * TODO: add constructors for combinations of number and date formatters.
 */
public class StandardXYZToolTipGenerator extends StandardXYToolTipGenerator
														implements XYZToolTipGenerator {

	/** The default tooltip format. */
	public static final String DEFAULT_TOOL_TIP_FORMAT = "{0}: ({1}, {2}, {3})";

	/**
	 * A number formatter for the z value - if this is null, then zDateFormat must
	 * be non-null.
	 */
	private NumberFormat zFormat;

	/**
	 * A date formatter for the z-value - if this is null, then zFormat must be
	 * non-null.
	 */
	private DateFormat zDateFormat;

	/**
	 * Creates a new tool tip generator using default number formatters for the
	 * x, y and z-values.
	 */
	public StandardXYZToolTipGenerator() {
		this(
							DEFAULT_TOOL_TIP_FORMAT,
							NumberFormat.getNumberInstance(),
							NumberFormat.getNumberInstance(),
							NumberFormat.getNumberInstance());
	}

	/**
	 * Constructs a new tool tip generator using the specified number formatters.
	 * 
	 * @param formatString
	 *           the format string.
	 * @param xFormat
	 *           the format object for the x values (<code>null</code> not permitted).
	 * @param yFormat
	 *           the format object for the y values (<code>null</code> not permitted).
	 * @param zFormat
	 *           the format object for the z values (<code>null</code> not permitted).
	 */
	public StandardXYZToolTipGenerator(String formatString,
													NumberFormat xFormat,
													NumberFormat yFormat,
													NumberFormat zFormat) {
		super(formatString, xFormat, yFormat);
		if (zFormat == null) {
			throw new IllegalArgumentException("Null 'zFormat' argument.");
		}
		this.zFormat = zFormat;
	}

	/**
	 * Constructs a new tool tip generator using the specified date formatters.
	 * 
	 * @param formatString
	 *           the format string.
	 * @param xFormat
	 *           the format object for the x values (<code>null</code> not permitted).
	 * @param yFormat
	 *           the format object for the y values (<code>null</code> not permitted).
	 * @param zFormat
	 *           the format object for the z values (<code>null</code> not permitted).
	 */
	public StandardXYZToolTipGenerator(String formatString,
													DateFormat xFormat,
													DateFormat yFormat,
													DateFormat zFormat) {
		super(formatString, xFormat, yFormat);
		if (zFormat == null) {
			throw new IllegalArgumentException("Null 'zFormat' argument.");
		}
		this.zDateFormat = zFormat;
	}

	/**
	 * Returns the number formatter for the z-values.
	 * 
	 * @return The number formatter (possibly <code>null</code>).
	 */
	public NumberFormat getZFormat() {
		return this.zFormat;
	}

	/**
	 * Returns the date formatter for the z-values.
	 * 
	 * @return The date formatter (possibly <code>null</code>).
	 */
	public DateFormat getZDateFormat() {
		return this.zDateFormat;
	}

	/**
	 * Generates a tool tip text item for a particular item within a series.
	 * 
	 * @param data
	 *           the dataset.
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return The tooltip text.
	 */
	public String generateToolTip(XYZDataset data, int series, int item) {

		String result = data.getSeriesName(series) + ": ";
		Number x = data.getXValue(series, item);
		result = result + "x: " + getXFormat().format(x);

		Number y = data.getYValue(series, item);
		result = result + "y: " + getYFormat().format(y);

		Number z = data.getZValue(series, item);
		if (z != null) {
			result = result + ", z: " + zFormat.format(z);
		} else {
			result = result + ", z: null";
		}

		return result;

	}

	/**
	 * Creates the array of items that can be passed to the {@link MessageFormat} class
	 * for creating labels.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> not permitted).
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The items (never <code>null</code>).
	 */
	protected Object[] createItemArray(XYZDataset dataset, int series, int item) {

		Object[] result = new Object[4];
		result[0] = dataset.getSeriesName(series);

		Number x = dataset.getXValue(series, item);
		DateFormat xf = getXDateFormat();
		if (xf != null) {
			result[1] = xf.format(x);
		} else {
			result[1] = getXFormat().format(x);
		}

		Number y = dataset.getYValue(series, item);
		DateFormat yf = getYDateFormat();
		if (yf != null) {
			result[2] = yf.format(y);
		} else {
			result[2] = getYFormat().format(y);
		}

		Number z = dataset.getZValue(series, item);
		if (this.zDateFormat != null) {
			result[3] = this.zDateFormat.format(z);
		} else {
			result[3] = this.zFormat.format(z);
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
		if (obj instanceof StandardXYZToolTipGenerator && super.equals(obj)) {
			StandardXYZToolTipGenerator generator = (StandardXYZToolTipGenerator) obj;
			boolean b0 = ObjectUtils.equal(this.zFormat, generator.zFormat);
			boolean b1 = ObjectUtils.equal(this.zDateFormat, generator.zDateFormat);
			return b0 && b1;
		}
		return false;

	}

}
