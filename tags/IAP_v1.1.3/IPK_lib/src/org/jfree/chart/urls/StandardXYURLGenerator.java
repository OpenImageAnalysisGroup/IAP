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
 * StandardXYURLGenerator.java
 * ---------------------------
 * (C) Copyright 2002, 2003, by Richard Atkinson and Contributors.
 * Original Author: Richard Atkinson (richard_c_atkinson@ntlworld.com);
 * Contributors: David Gilbert (for Object Refinery Limited);
 * $Id: StandardXYURLGenerator.java,v 1.1 2011-01-31 09:02:22 klukas Exp $
 * Changes:
 * --------
 * 05-Aug-2002 : Version 1, contributed by Richard Atkinson;
 * 29-Aug-2002 : New constructor and member variables to customise series and item parameter
 * names (RA);
 * 09-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 23-Mar-2003 : Implemented Serializable (DG);
 * 01-Mar-2004 : Added equals() method (DG);
 */

package org.jfree.chart.urls;

import java.io.Serializable;

import org.jfree.data.XYDataset;
import org.jfree.util.ObjectUtils;

/**
 * A URL generator.
 * 
 * @author Richard Atkinson
 */
public class StandardXYURLGenerator implements XYURLGenerator, Serializable {

	/** Prefix to the URL */
	private String prefix = "index.html";

	/** Series parameter name to go in each URL */
	private String seriesParameterName = "series";

	/** Item parameter name to go in each URL */
	private String itemParameterName = "item";

	/**
	 * Blank constructor
	 */
	public StandardXYURLGenerator() {
		super();
	}

	/**
	 * Constructor that overrides default prefix to the URL.
	 * 
	 * @param sPrefix
	 *           the prefix to the URL
	 */
	public StandardXYURLGenerator(String sPrefix) {
		this.prefix = sPrefix;
	}

	/**
	 * Constructor that overrides all the defaults
	 * 
	 * @param prefix
	 *           the prefix to the URL.
	 * @param seriesParameterName
	 *           the name of the series parameter to go in each URL.
	 * @param itemParameterName
	 *           the name of the item parameter to go in each URL.
	 */
	public StandardXYURLGenerator(String prefix,
												String seriesParameterName,
												String itemParameterName) {

		this.prefix = prefix;
		this.seriesParameterName = seriesParameterName;
		this.itemParameterName = itemParameterName;

	}

	/**
	 * Generates a URL for a particular item within a series.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @param series
	 *           the series number (zero-based index).
	 * @param item
	 *           the item number (zero-based index).
	 * @return the generated URL.
	 */
	public String generateURL(XYDataset dataset, int series, int item) {
		String url = this.prefix;
		boolean firstParameter = url.indexOf("?") == -1;
		url += firstParameter ? "?" : "&";
		url += this.seriesParameterName + "=" + series
							+ "&" + this.itemParameterName + "=" + item;
		return url;
	}

	/**
	 * Tests this generator for equaliaty with an arbitrary object.
	 * 
	 * @param obj
	 *           the object (<code>null</code> permitted).
	 * @return a boolean.
	 */
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof StandardXYURLGenerator) {
			StandardXYURLGenerator g = (StandardXYURLGenerator) obj;
			boolean b0 = ObjectUtils.equal(g.prefix, this.prefix);
			boolean b1 = ObjectUtils.equal(g.seriesParameterName, this.seriesParameterName);
			boolean b2 = ObjectUtils.equal(g.itemParameterName, this.itemParameterName);
			return b0 && b1 && b2;
		}

		return false;
	}

}
