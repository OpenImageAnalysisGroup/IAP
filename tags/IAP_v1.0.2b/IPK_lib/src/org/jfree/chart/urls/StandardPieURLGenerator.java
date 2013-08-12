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
 * ----------------------------
 * StandardPieURLGenerator.java
 * ----------------------------
 * (C) Copyright 2002, 2003, by Richard Atkinson and Contributors.
 * Original Author: Richard Atkinson (richard_c_atkinson@ntlworld.com);
 * Contributors: David Gilbert (for Object Refinery Limited);
 * $Id: StandardPieURLGenerator.java,v 1.1 2011-01-31 09:02:21 klukas Exp $
 * Changes:
 * --------
 * 05-Aug-2002 : Version 1, contributed by Richard Atkinson;
 * 09-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Mar-2003 : Modified to use KeyedValuesDataset and added pieIndex parameter (DG);
 * 21-Mar-2003 : Implemented Serializable (DG);
 * 24-Apr-2003 : Switched around PieDataset and KeyedValuesDataset (DG);
 * 31-Mar-2004 : Added an optional 'pieIndex' parameter (DG);
 */
package org.jfree.chart.urls;

import java.io.Serializable;

import org.jfree.data.PieDataset;

/**
 * A URL generator for pie charts.
 * 
 * @author Richard Atkinson
 */
public class StandardPieURLGenerator implements PieURLGenerator, Serializable {

	/** The prefix. */
	private String prefix = "index.html";

	/** The category parameter name. */
	private String categoryParameterName = "category";

	/** The pie index parameter name. */
	private String indexParameterName = "pieIndex";

	/**
	 * Default constructor.
	 */
	public StandardPieURLGenerator() {
		super();
	}

	/**
	 * Creates a new generator.
	 * 
	 * @param prefix
	 *           the prefix.
	 */
	public StandardPieURLGenerator(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Creates a new generator.
	 * 
	 * @param prefix
	 *           the prefix.
	 * @param categoryParameterName
	 *           the category parameter name.
	 */
	public StandardPieURLGenerator(String prefix, String categoryParameterName) {
		this.prefix = prefix;
		this.categoryParameterName = categoryParameterName;
	}

	/**
	 * Creates a new generator.
	 * 
	 * @param prefix
	 *           the prefix.
	 * @param categoryParameterName
	 *           the category parameter name.
	 * @param indexParameterName
	 *           the index parameter name (<code>null</code> permitted).
	 */
	public StandardPieURLGenerator(String prefix,
												String categoryParameterName,
												String indexParameterName) {
		this.prefix = prefix;
		this.categoryParameterName = categoryParameterName;
		this.indexParameterName = indexParameterName;
	}

	/**
	 * Generates a URL.
	 * 
	 * @param data
	 *           the dataset.
	 * @param key
	 *           the item key.
	 * @param pieIndex
	 *           the pie index (ignored).
	 * @return a string containing the generated URL.
	 */
	public String generateURL(PieDataset data, Comparable key, int pieIndex) {

		String url = this.prefix;
		if (url.indexOf("?") > -1) {
			url += "&" + this.categoryParameterName + "=" + key.toString();
		} else {
			url += "?" + this.categoryParameterName + "=" + key.toString();
		}
		if (this.indexParameterName != null) {
			url += "&" + this.indexParameterName + "=" + String.valueOf(pieIndex);
		}
		return url;

	}

	/**
	 * Tests if this object is equal to another.
	 * 
	 * @param o
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}

		if ((o instanceof StandardPieURLGenerator) == false) {
			return false;
		}

		StandardPieURLGenerator generator = (StandardPieURLGenerator) o;
		return (this.categoryParameterName.equals(generator.categoryParameterName))
							&& (this.prefix.equals(generator.prefix));

	}
}
