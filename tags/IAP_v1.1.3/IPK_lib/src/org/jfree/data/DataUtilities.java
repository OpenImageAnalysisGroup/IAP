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
 * ------------------
 * DataUtilities.java
 * ------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DataUtilities.java,v 1.1 2011-01-31 09:02:13 klukas Exp $
 * Changes
 * -------
 * 05-Mar-2003 : Version 1 (DG);
 */

package org.jfree.data;

/**
 * Utility methods for use with some of the data classes (but not the datasets, see {@link DatasetUtilities}).
 */
public abstract class DataUtilities {

	/**
	 * Returns a {@link KeyedValues} instance that contains the cumulative percentage values
	 * for the data in another {@link KeyedValues} instance.
	 * <p>
	 * The percentages are values between 0.0 and 1.0 (where 1.0 = 100%).
	 * 
	 * @param data
	 *           the data.
	 * @return The cumulative percentages.
	 */
	public static KeyedValues getCumulativePercentages(final KeyedValues data) {

		final DefaultKeyedValues result = new DefaultKeyedValues();

		double total = 0.0;
		for (int i = 0; i < data.getItemCount(); i++) {
			final Number v = data.getValue(i);
			if (v != null) {
				total = total + v.doubleValue();
			}
		}

		double runningTotal = 0.0;
		for (int i = 0; i < data.getItemCount(); i++) {
			final Number v = data.getValue(i);
			if (v != null) {
				runningTotal = runningTotal + v.doubleValue();
			}
			result.addValue(data.getKey(i), new Double(runningTotal / total));
		}

		return result;

	}

}
