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
 * StandardTickUnitSource.java
 * ---------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: StandardTickUnitSource.java,v 1.1 2011-01-31 09:01:39 klukas Exp $
 * Changes
 * -------
 * 23-Sep-2003 : Version 1 (DG);
 */

package org.jfree.chart.axis;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A source that can used by the {@link NumberAxis} class to obtain a
 * suitable {@link TickUnit}.
 */
public class StandardTickUnitSource implements TickUnitSource {

	/** Constant for log(10.0). */
	private static final double LOG_10_VALUE = Math.log(10.0);

	/**
	 * Returns a tick unit that is larger than the supplied unit.
	 * 
	 * @param unit
	 *           the unit.
	 * @return A tick unit that is larger than the supplied unit.
	 */
	public TickUnit getLargerTickUnit(TickUnit unit) {
		double x = unit.getSize();
		double log = Math.log(x) / LOG_10_VALUE;
		double higher = Math.ceil(log);
		return new NumberTickUnit(Math.pow(10, higher), getDecimalFormat("0.0E0"));
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
	 * Returns the tick unit in the collection that is greater than or equal
	 * to (in size) the specified unit.
	 * 
	 * @param unit
	 *           the unit.
	 * @return A unit from the collection.
	 */
	public TickUnit getCeilingTickUnit(TickUnit unit) {
		return getLargerTickUnit(unit);
	}

	/**
	 * Returns the tick unit in the collection that is greater than or equal
	 * to the specified size.
	 * 
	 * @param size
	 *           the size.
	 * @return A unit from the collection.
	 */
	public TickUnit getCeilingTickUnit(double size) {
		double log = Math.log(size) / LOG_10_VALUE;
		double higher = Math.ceil(log);
		return new NumberTickUnit(Math.pow(10, higher), getDecimalFormat("0.0E0"));
	}

}
