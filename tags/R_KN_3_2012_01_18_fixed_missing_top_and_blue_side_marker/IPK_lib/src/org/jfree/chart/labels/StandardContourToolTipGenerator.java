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
 * ------------------------------------
 * StandardContourToolTipGenerator.java
 * ------------------------------------
 * (C) Copyright 2002, 2003, by David M. O'Donnell and Contributors.
 * Original Author: David M. O'Donnell;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: StandardContourToolTipGenerator.java,v 1.1 2011-01-31 09:02:38 klukas Exp $
 * Changes
 * -------
 * 23-Jan-2003 : Added standard header (DG);
 * 21-Mar-2003 : Implemented Serializable (DG);
 */

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jfree.data.ContourDataset;

/**
 * A standard tooltip generator for plots that use data from an {@link ContourDataset}.
 * 
 * @author David M. O'Donnell
 */
public class StandardContourToolTipGenerator implements ContourToolTipGenerator, Serializable {

	/** The number formatter. */
	private DecimalFormat valueForm = (DecimalFormat) getDecimalFormat("##.###");

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
	 * Generates a tooltip text item for a particular item within a series.
	 * 
	 * @param data
	 *           the dataset.
	 * @param item
	 *           the item index (zero-based).
	 * @return The tooltip text.
	 */
	public String generateToolTip(ContourDataset data, int item) {

		Number x = data.getXValue(0, item);
		Number y = data.getYValue(0, item);
		Number z = data.getZValue(0, item);
		String xString = null;

		if (data.isDateAxis(0)) {
			SimpleDateFormat formatter = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
			StringBuffer strbuf = new StringBuffer();
			strbuf = formatter.format(new Date(x.longValue()),
													strbuf,
													new java.text.FieldPosition(0));
			xString = strbuf.toString();
		} else {
			xString = this.valueForm.format(x.doubleValue());
		}
		if (z != null) {
			return "X: " + xString
								+ ", Y: " + this.valueForm.format(y.doubleValue())
								+ ", Z: " + this.valueForm.format(z.doubleValue());
		} else {
			return "X: " + xString
								+ ", Y: " + this.valueForm.format(y.doubleValue())
								+ ", Z: no data";
		}

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

		if (o instanceof StandardContourToolTipGenerator) {
			StandardContourToolTipGenerator generator = (StandardContourToolTipGenerator) o;
			if (this.valueForm != null) {
				return this.valueForm.equals(generator.valueForm);
			}
		}

		return false;

	}

}
