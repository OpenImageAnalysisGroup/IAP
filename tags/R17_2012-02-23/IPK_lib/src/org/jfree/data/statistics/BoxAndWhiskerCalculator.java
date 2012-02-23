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
 * BoxAndWhiskerCalculator.java
 * ----------------------------
 * (C) Copyright 2003 by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: BoxAndWhiskerCalculator.java,v 1.1 2011-01-31 09:02:06 klukas Exp $
 * Changes
 * -------
 * 28-Aug-2003 : Version 1 (DG);
 * 17-Nov-2003 : Fixed bug in calculations of outliers and median (DG);
 */

package org.jfree.data.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A utility class that calculates the mean, median, quartiles Q1 and Q3, plus a list of outlier
 * values...all from an arbitrary list of Number objects.
 */
public abstract class BoxAndWhiskerCalculator {

	/**
	 * Calculates the statistics required for a {@link BoxAndWhiskerItem}.
	 * <P>
	 * Any items in the list that are not instances of the <code>Number</code> class are ignored. Likewise, <code>null</code> values are ignored.
	 * 
	 * @param values
	 *           A list of numbers (a <code>null</code> list is not permitted).
	 * @return Box-and-whisker statistics.
	 */
	public static BoxAndWhiskerItem calculateBoxAndWhiskerStatistics(final List values) {

		Collections.sort(values);

		final double mean = Statistics.calculateMean(values);
		final double median = Statistics.calculateMedian(values, false);
		final double q1 = calculateQ1(values);
		final double q3 = calculateQ3(values);

		final double interQuartileRange = q3 - q1;

		final double upperOutlierThreshold = q3 + (interQuartileRange * 1.5);
		final double lowerOutlierThreshold = q1 - (interQuartileRange * 1.5);

		final double upperFaroutThreshold = q3 + (interQuartileRange * 2.0);
		final double lowerFaroutThreshold = q1 - (interQuartileRange * 2.0);

		double minRegularValue = Double.POSITIVE_INFINITY;
		double maxRegularValue = Double.NEGATIVE_INFINITY;
		double minOutlier = Double.POSITIVE_INFINITY;
		double maxOutlier = Double.NEGATIVE_INFINITY;
		final List outliers = new ArrayList();

		final Iterator iterator = values.iterator();
		while (iterator.hasNext()) {
			final Object object = iterator.next();
			if (object != null && object instanceof Number) {
				final Number number = (Number) object;
				final double value = number.doubleValue();
				if (value > upperOutlierThreshold) {
					outliers.add(number);
					if (value > maxOutlier && value <= upperFaroutThreshold) {
						maxOutlier = value;
					}
				} else
					if (value < lowerOutlierThreshold) {
						outliers.add(number);
						if (value < minOutlier && value >= lowerFaroutThreshold) {
							minOutlier = value;
						}
					} else {
						if (minRegularValue == Double.NaN) {
							minRegularValue = value;
						} else {
							minRegularValue = Math.min(minRegularValue, value);
						}
						if (maxRegularValue == Double.NaN) {
							maxRegularValue = value;
						} else {
							maxRegularValue = Math.max(maxRegularValue, value);
						}
					}

			}
		}
		minOutlier = Math.min(minOutlier, minRegularValue);
		maxOutlier = Math.max(maxOutlier, maxRegularValue);

		return new BoxAndWhiskerItem(
							new Double(mean),
							new Double(median),
							new Double(q1),
							new Double(q3),
							new Double(minRegularValue),
							new Double(maxRegularValue),
							new Double(minOutlier),
							new Double(maxOutlier),
							outliers);

	}

	/**
	 * Calculates the first quartile for a list of numbers in ascending order.
	 * 
	 * @param values
	 *           the numbers in ascending order.
	 * @return The first quartile.
	 */
	public static double calculateQ1(final List values) {
		double result = Double.NaN;
		final int count = values.size();
		if (count > 0) {
			if (count % 2 == 1) {
				if (count > 1) {
					result = Statistics.calculateMedian(values, 0, count / 2);
				} else {
					result = Statistics.calculateMedian(values, 0, 0);
				}
			} else {
				result = Statistics.calculateMedian(values, 0, count / 2);
			}

		}
		return result;
	}

	/**
	 * Calculates the third quartile for a list of numbers in ascending order.
	 * 
	 * @param values
	 *           the list of values.
	 * @return The third quartile.
	 */
	public static double calculateQ3(final List values) {
		double result = Double.NaN;
		final int count = values.size();
		if (count > 0) {
			if (count % 2 == 1) {
				if (count > 1) {
					result = Statistics.calculateMedian(values, count / 2 + 1, count - 1);
				} else {
					result = Statistics.calculateMedian(values, 0, 0);
				}
			} else {
				result = Statistics.calculateMedian(values, count / 2 + 1, count - 1);
			}

		}
		return result;
	}

	// // DEPRECATED CODE /////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the mean of a list of numbers.
	 * 
	 * @param values
	 *           a list of numbers.
	 * @return The mean.
	 * @deprecated This method has been moved to the {@link Statistics} class.
	 */
	public static double calculateMean(final List values) {

		double result = Double.NaN;
		int count = 0;
		double total = 0.0;
		final Iterator iterator = values.iterator();
		while (iterator.hasNext()) {
			final Object object = iterator.next();
			if (object != null && object instanceof Number) {
				final Number number = (Number) object;
				total = total + number.doubleValue();
				count = count + 1;
			}
		}
		if (count > 0) {
			result = total / count;
		}
		return result;

	}

	/**
	 * Calculates the median for a list of values (<code>Number</code> objects) that are in
	 * ascending order.
	 * 
	 * @param values
	 *           the values in ascending order.
	 * @return The median.
	 * @deprecated Moved to the {@link Statistics} class.
	 */
	public static double calculateMedian(final List values) {
		return calculateMedian(values, 0, values.size() - 1);
	}

	/**
	 * Calculates the median for a sublist within a list of values (Number objects) that are in
	 * ascending order.
	 * 
	 * @param values
	 *           the values in ascending order.
	 * @param start
	 *           the start index.
	 * @param end
	 *           the end index.
	 * @return The median.
	 * @deprecated Moved to the {@link Statistics} class.
	 */
	public static double calculateMedian(final List values, final int start, final int end) {

		double result = Double.NaN;
		final int count = end - start + 1;
		if (count > 0) {
			if (count % 2 == 1) {
				if (count > 1) {
					final Number value = (Number) values.get(start + (count - 1) / 2);
					result = value.doubleValue();
				} else {
					final Number value = (Number) values.get(start);
					result = value.doubleValue();
				}
			} else {
				final Number value1 = (Number) values.get(start + count / 2 - 1);
				final Number value2 = (Number) values.get(start + count / 2);
				result = (value1.doubleValue() + value2.doubleValue()) / 2.0;
			}
		}
		return result;

	}

}
