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
 * --------------
 * TickUnits.java
 * --------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TickUnits.java,v 1.1 2011-01-31 09:01:37 klukas Exp $
 * Changes
 * -------
 * 23-Nov-2001 : Version 1 (DG);
 * 18-Feb-2002 : Fixed bug in getNearestTickUnit (thanks to Mario Inchiosa for reporting this,
 * SourceForge bug id 518073) (DG);
 * 25-Feb-2002 : Moved createStandardTickUnits() method from NumberAxis, and added
 * createIntegerTickUnits() method (DG);
 * 01-May-2002 : Updated for changes to the TickUnit class (DG);
 * 18-Sep-2002 : Added standardTickUnit methods which take a Locale instance (AS);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 23-Sep-2003 : Implemented TickUnitSource interface (DG);
 * 03-Dec-2003 : Adding null values now throws exceptions (TM);
 */

package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A collection of tick units.
 * <P>
 * Used by the {@link DateAxis} and {@link NumberAxis} classes.
 */
public class TickUnits implements TickUnitSource, Cloneable, Serializable {

	/** Storage for the tick units. */
	private List tickUnits;

	/**
	 * Constructs a new collection of tick units.
	 */
	public TickUnits() {
		this.tickUnits = new ArrayList();
	}

	/**
	 * Adds a tick unit to the collection.
	 * <P>
	 * The tick units are maintained in ascending order.
	 * 
	 * @param unit
	 *           the tick unit to add.
	 */
	public void add(TickUnit unit) {

		if (unit == null) {
			throw new NullPointerException("TickUnits.add(..): Null not permitted.");
		}
		this.tickUnits.add(unit);
		Collections.sort(this.tickUnits);

	}

	/**
	 * Returns the number of tick units in this collection.
	 * <P>
	 * This method is required for the XML writer.
	 * 
	 * @return the number of units in this collection
	 */
	public int size() {
		return this.tickUnits.size();
	}

	/**
	 * Returns the tickunit on the given position.
	 * <P>
	 * This method is required for the XML writer.
	 * 
	 * @param pos
	 *           the position in the list.
	 * @return the tickunit.
	 */
	public TickUnit get(int pos) {
		return (TickUnit) this.tickUnits.get(pos);
	}

	/**
	 * Returns a tick unit that is larger than the supplied unit.
	 * 
	 * @param unit
	 *           the unit.
	 * @return a tick unit that is larger than the supplied unit.
	 */
	public TickUnit getLargerTickUnit(TickUnit unit) {

		int index = Collections.binarySearch(this.tickUnits, unit);
		if (index >= 0) {
			index = index + 1;
		} else {
			index = -index;
		}

		return (TickUnit) this.tickUnits.get(Math.min(index, this.tickUnits.size() - 1));

	}

	/**
	 * Returns the tick unit in the collection that is greater than or equal
	 * to (in size) the specified unit.
	 * 
	 * @param unit
	 *           the unit.
	 * @return a unit from the collection.
	 */
	public TickUnit getCeilingTickUnit(TickUnit unit) {

		int index = Collections.binarySearch(this.tickUnits, unit);
		if (index >= 0) {
			return (TickUnit) this.tickUnits.get(index);
		} else {
			index = -(index + 1);
			return (TickUnit) this.tickUnits.get(Math.min(index, this.tickUnits.size() - 1));
		}

	}

	/**
	 * Returns the tick unit in the collection that is greater than or equal
	 * to the specified size.
	 * 
	 * @param size
	 *           the size.
	 * @return a unit from the collection.
	 */
	public TickUnit getCeilingTickUnit(double size) {

		return getCeilingTickUnit(new NumberTickUnit(size, null));

	}

	/**
	 * Creates the standard tick units.
	 * <P>
	 * If you don't like these defaults, create your own instance of TickUnits and then pass it to the setStandardTickUnits(...) method in the NumberAxis class.
	 * 
	 * @return the standard tick units.
	 * @deprecated this method has been moved to the NumberAxis class.
	 */
	public static TickUnitSource createStandardTickUnits() {

		TickUnits units = new TickUnits();

		// we can add the units in any order, the TickUnits collection will sort them...
		units.add(new NumberTickUnit(0.0000001, getDecimalFormat("0.0000000")));
		units.add(new NumberTickUnit(0.000001, getDecimalFormat("0.000000")));
		units.add(new NumberTickUnit(0.00001, getDecimalFormat("0.00000")));
		units.add(new NumberTickUnit(0.0001, getDecimalFormat("0.0000")));
		units.add(new NumberTickUnit(0.001, getDecimalFormat("0.000")));
		units.add(new NumberTickUnit(0.01, getDecimalFormat("0.00")));
		units.add(new NumberTickUnit(0.1, getDecimalFormat("0.0")));
		units.add(new NumberTickUnit(1, getDecimalFormat("0")));
		units.add(new NumberTickUnit(10, getDecimalFormat("0")));
		units.add(new NumberTickUnit(100, getDecimalFormat("0")));
		units.add(new NumberTickUnit(1000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(10000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(100000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(1000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(10000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(100000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(1000000000, getDecimalFormat("#,###,###,##0")));

		units.add(new NumberTickUnit(0.00000025, getDecimalFormat("0.00000000")));
		units.add(new NumberTickUnit(0.0000025, getDecimalFormat("0.0000000")));
		units.add(new NumberTickUnit(0.000025, getDecimalFormat("0.000000")));
		units.add(new NumberTickUnit(0.00025, getDecimalFormat("0.00000")));
		units.add(new NumberTickUnit(0.0025, getDecimalFormat("0.0000")));
		units.add(new NumberTickUnit(0.025, getDecimalFormat("0.000")));
		units.add(new NumberTickUnit(0.25, getDecimalFormat("0.00")));
		units.add(new NumberTickUnit(2.5, getDecimalFormat("0.0")));
		units.add(new NumberTickUnit(25, getDecimalFormat("0")));
		units.add(new NumberTickUnit(250, getDecimalFormat("0")));
		units.add(new NumberTickUnit(2500, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(25000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(250000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(2500000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(25000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(250000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(2500000000.0, getDecimalFormat("#,###,###,##0")));

		units.add(new NumberTickUnit(0.0000005, getDecimalFormat("0.0000000")));
		units.add(new NumberTickUnit(0.000005, getDecimalFormat("0.000000")));
		units.add(new NumberTickUnit(0.00005, getDecimalFormat("0.00000")));
		units.add(new NumberTickUnit(0.0005, getDecimalFormat("0.0000")));
		units.add(new NumberTickUnit(0.005, getDecimalFormat("0.000")));
		units.add(new NumberTickUnit(0.05, getDecimalFormat("0.00")));
		units.add(new NumberTickUnit(0.5, getDecimalFormat("0.0")));
		units.add(new NumberTickUnit(5L, getDecimalFormat("0")));
		units.add(new NumberTickUnit(50L, getDecimalFormat("0")));
		units.add(new NumberTickUnit(500L, getDecimalFormat("0")));
		units.add(new NumberTickUnit(5000L, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(50000L, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(500000L, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(5000000L, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(50000000L, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(500000000L, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(5000000000L, getDecimalFormat("#,###,###,##0")));

		return units;

	}

	/**
	 * Returns a collection of tick units for integer values.
	 * 
	 * @return a collection of tick units for integer values.
	 * @deprecated this method has been moved to the NumberAxis class.
	 */
	public static TickUnitSource createIntegerTickUnits() {

		TickUnits units = new TickUnits();

		units.add(new NumberTickUnit(1, getDecimalFormat("0")));
		units.add(new NumberTickUnit(2, getDecimalFormat("0")));
		units.add(new NumberTickUnit(5, getDecimalFormat("0")));
		units.add(new NumberTickUnit(10, getDecimalFormat("0")));
		units.add(new NumberTickUnit(20, getDecimalFormat("0")));
		units.add(new NumberTickUnit(50, getDecimalFormat("0")));
		units.add(new NumberTickUnit(100, getDecimalFormat("0")));
		units.add(new NumberTickUnit(200, getDecimalFormat("0")));
		units.add(new NumberTickUnit(500, getDecimalFormat("0")));
		units.add(new NumberTickUnit(1000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(2000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(5000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(10000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(20000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(50000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(100000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(200000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(500000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(1000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(2000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(5000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(10000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(20000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(50000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(100000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(200000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(500000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(1000000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(2000000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(5000000000.0, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(10000000000.0, getDecimalFormat("#,##0")));

		return units;

	}

	/**
	 * Replace occurrences of a substring.
	 * http://ostermiller.org/utils/StringHelper.html
	 * StringHelper.replace("1-2-3", "-", "|");<br>
	 * result: "1|2|3"<br>
	 * StringHelper.replace("-1--2-", "-", "|");<br>
	 * result: "|1||2|"<br>
	 * StringHelper.replace("123", "", "|");<br>
	 * result: "123"<br>
	 * StringHelper.replace("1-2---3----4", "--", "|");<br>
	 * result: "1-2|-3||4"<br>
	 * StringHelper.replace("1-2---3----4", "--", "---");<br>
	 * result: "1-2----3------4"<br>
	 * 
	 * @param s
	 *           String to be modified.
	 * @param find
	 *           String to find.
	 * @param replace
	 *           String to replace.
	 * @return a string with all the occurrences of the string to find replaced.
	 * @throws NullPointerException
	 *            if s is null.
	 */
	public static String stringReplace(String s, String find, String replace) {
		int findLength;
		// the next statement has the side effect of throwing a null pointer
		// exception if s is null.
		int stringLength = s.length();
		if (find == null || (findLength = find.length()) == 0) {
			// If there is nothing to find, we won't try and find it.
			return s;
		}
		if (replace == null) {
			// a null string and an empty string are the same
			// for replacement purposes.
			replace = ""; //$NON-NLS-1$
		}
		int replaceLength = replace.length();

		// We need to figure out how long our resulting string will be.
		// This is required because without it, the possible resizing
		// and copying of memory structures could lead to an unacceptable runtime.
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.
		int length;
		if (findLength == replaceLength) {
			// special case in which we don't need to count the replacements
			// because the count falls out of the length formula.
			length = stringLength;
		} else {
			int count;
			int start;
			int end;

			// Scan s and count the number of times we find our target.
			count = 0;
			start = 0;
			while ((end = s.indexOf(find, start)) != -1) {
				count++;
				start = end + findLength;
			}
			if (count == 0) {
				// special case in which on first pass, we find there is nothing
				// to be replaced. No need to do a second pass or create a string buffer.
				return s;
			}
			length = stringLength - (count * (findLength - replaceLength));
		}

		int start = 0;
		int end = s.indexOf(find, start);
		if (end == -1) {
			// nothing was found in the string to replace.
			// we can get this if the find and replace strings
			// are the same length because we didn't check before.
			// in this case, we will return the original string
			return s;
		}
		// it looks like we actually have something to replace
		// *sigh* allocate memory for it.
		StringBuffer sb = new StringBuffer(length);

		// Scan s and do the replacements
		while (end != -1) {
			sb.append(s.substring(start, end).toString());
			sb.append(replace.toString());
			start = end + findLength;
			end = s.indexOf(find, start);
		}
		end = stringLength;
		sb.append(s.substring(start, end).toString());

		return (sb.toString());
	}

	public static DecimalFormat getDecimalFormat(String pattern) {
		pattern = stringReplace(pattern, ",", "");
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern(pattern);
		return df;
	}

	/**
	 * Creates the standard tick units, and uses a given Locale to create the DecimalFormats
	 * <P>
	 * If you don't like these defaults, create your own instance of TickUnits and then pass it to the setStandardTickUnits(...) method in the NumberAxis class.
	 * 
	 * @param locale
	 *           the locale to use to represent Numbers.
	 * @return the standard tick units.
	 * @deprecated this method has been moved to the NumberAxis class.
	 */
	public static TickUnitSource createStandardTickUnits(Locale locale) {

		TickUnits units = new TickUnits();

		NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

		// we can add the units in any order, the TickUnits collection will sort them...
		units.add(new NumberTickUnit(0.0000001, numberFormat));
		units.add(new NumberTickUnit(0.000001, numberFormat));
		units.add(new NumberTickUnit(0.00001, numberFormat));
		units.add(new NumberTickUnit(0.0001, numberFormat));
		units.add(new NumberTickUnit(0.001, numberFormat));
		units.add(new NumberTickUnit(0.01, numberFormat));
		units.add(new NumberTickUnit(0.1, numberFormat));
		units.add(new NumberTickUnit(1, numberFormat));
		units.add(new NumberTickUnit(10, numberFormat));
		units.add(new NumberTickUnit(100, numberFormat));
		units.add(new NumberTickUnit(1000, numberFormat));
		units.add(new NumberTickUnit(10000, numberFormat));
		units.add(new NumberTickUnit(100000, numberFormat));
		units.add(new NumberTickUnit(1000000, numberFormat));
		units.add(new NumberTickUnit(10000000, numberFormat));
		units.add(new NumberTickUnit(100000000, numberFormat));
		units.add(new NumberTickUnit(1000000000, numberFormat));

		units.add(new NumberTickUnit(0.00000025, numberFormat));
		units.add(new NumberTickUnit(0.0000025, numberFormat));
		units.add(new NumberTickUnit(0.000025, numberFormat));
		units.add(new NumberTickUnit(0.00025, numberFormat));
		units.add(new NumberTickUnit(0.0025, numberFormat));
		units.add(new NumberTickUnit(0.025, numberFormat));
		units.add(new NumberTickUnit(0.25, numberFormat));
		units.add(new NumberTickUnit(2.5, numberFormat));
		units.add(new NumberTickUnit(25, numberFormat));
		units.add(new NumberTickUnit(250, numberFormat));
		units.add(new NumberTickUnit(2500, numberFormat));
		units.add(new NumberTickUnit(25000, numberFormat));
		units.add(new NumberTickUnit(250000, numberFormat));
		units.add(new NumberTickUnit(2500000, numberFormat));
		units.add(new NumberTickUnit(25000000, numberFormat));
		units.add(new NumberTickUnit(250000000, numberFormat));
		units.add(new NumberTickUnit(2500000000.0, numberFormat));

		units.add(new NumberTickUnit(0.0000005, numberFormat));
		units.add(new NumberTickUnit(0.000005, numberFormat));
		units.add(new NumberTickUnit(0.00005, numberFormat));
		units.add(new NumberTickUnit(0.0005, numberFormat));
		units.add(new NumberTickUnit(0.005, numberFormat));
		units.add(new NumberTickUnit(0.05, numberFormat));
		units.add(new NumberTickUnit(0.5, numberFormat));
		units.add(new NumberTickUnit(5L, numberFormat));
		units.add(new NumberTickUnit(50L, numberFormat));
		units.add(new NumberTickUnit(500L, numberFormat));
		units.add(new NumberTickUnit(5000L, numberFormat));
		units.add(new NumberTickUnit(50000L, numberFormat));
		units.add(new NumberTickUnit(500000L, numberFormat));
		units.add(new NumberTickUnit(5000000L, numberFormat));
		units.add(new NumberTickUnit(50000000L, numberFormat));
		units.add(new NumberTickUnit(500000000L, numberFormat));
		units.add(new NumberTickUnit(5000000000L, numberFormat));

		return units;

	}

	/**
	 * Returns a collection of tick units for integer values.
	 * Uses a given Locale to create the DecimalFormats.
	 * 
	 * @param locale
	 *           the locale to use to represent Numbers.
	 * @return a collection of tick units for integer values.
	 * @deprecated this method has been moved to the NumberAxis class.
	 */
	public static TickUnitSource createIntegerTickUnits(Locale locale) {

		TickUnits units = new TickUnits();

		NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

		units.add(new NumberTickUnit(1, numberFormat));
		units.add(new NumberTickUnit(2, numberFormat));
		units.add(new NumberTickUnit(5, numberFormat));
		units.add(new NumberTickUnit(10, numberFormat));
		units.add(new NumberTickUnit(20, numberFormat));
		units.add(new NumberTickUnit(50, numberFormat));
		units.add(new NumberTickUnit(100, numberFormat));
		units.add(new NumberTickUnit(200, numberFormat));
		units.add(new NumberTickUnit(500, numberFormat));
		units.add(new NumberTickUnit(1000, numberFormat));
		units.add(new NumberTickUnit(2000, numberFormat));
		units.add(new NumberTickUnit(5000, numberFormat));
		units.add(new NumberTickUnit(10000, numberFormat));
		units.add(new NumberTickUnit(20000, numberFormat));
		units.add(new NumberTickUnit(50000, numberFormat));
		units.add(new NumberTickUnit(100000, numberFormat));
		units.add(new NumberTickUnit(200000, numberFormat));
		units.add(new NumberTickUnit(500000, numberFormat));
		units.add(new NumberTickUnit(1000000, numberFormat));
		units.add(new NumberTickUnit(2000000, numberFormat));
		units.add(new NumberTickUnit(5000000, numberFormat));
		units.add(new NumberTickUnit(10000000, numberFormat));
		units.add(new NumberTickUnit(20000000, numberFormat));
		units.add(new NumberTickUnit(50000000, numberFormat));
		units.add(new NumberTickUnit(100000000, numberFormat));
		units.add(new NumberTickUnit(200000000, numberFormat));
		units.add(new NumberTickUnit(500000000, numberFormat));
		units.add(new NumberTickUnit(1000000000, numberFormat));
		units.add(new NumberTickUnit(2000000000, numberFormat));
		units.add(new NumberTickUnit(5000000000.0, numberFormat));
		units.add(new NumberTickUnit(10000000000.0, numberFormat));

		return units;

	}

	/**
	 * Returns a clone of the collection.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if an item in the collection does not support cloning.
	 */
	public Object clone() throws CloneNotSupportedException {
		TickUnits clone = (TickUnits) super.clone();
		clone.tickUnits = new java.util.ArrayList(this.tickUnits);
		return clone;
	}

	/**
	 * Tests an object for equality with this instance.
	 * 
	 * @param object
	 *           the object to test.
	 * @return A boolean.
	 */
	public boolean equals(Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (object instanceof TickUnits) {
			TickUnits tu = (TickUnits) object;
			return tu.tickUnits.equals(this.tickUnits);
		}

		return false;
	}

}
