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
 * ----------------------
 * BoxAndWhiskerItem.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: BoxAndWhiskerItem.java,v 1.1 2011-01-31 09:02:06 klukas Exp $
 * Changes
 * -------
 * 27-Aug-2003 : Version 1 (DG);
 * 01-Mar-2004 : Added equals() method and implemented Serializable (DG);
 */

package org.jfree.data.statistics;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jfree.util.ObjectUtils;

/**
 * Represents one data item within a box-and-whisker dataset. This class is immutable.
 */
public class BoxAndWhiskerItem implements Serializable {

	/** The mean. */
	private Number mean;

	/** The median. */
	private Number median;

	/** The first quarter. */
	private Number q1;

	/** The third quarter. */
	private Number q3;

	/** The minimum regular value. */
	private Number minRegularValue;

	/** The maximum regular value. */
	private Number maxRegularValue;

	/** The minimum outlier. */
	private Number minOutlier;

	/** The maximum outlier. */
	private Number maxOutlier;

	/** The outliers. */
	private List outliers;

	/**
	 * Creates a new box-and-whisker item.
	 * 
	 * @param mean
	 *           the mean (<code>null</code> permitted).
	 * @param median
	 *           the median (<code>null</code> permitted).
	 * @param q1
	 *           the first quartile (<code>null</code> permitted).
	 * @param q3
	 *           the third quartile (<code>null</code> permitted).
	 * @param minRegularValue
	 *           the minimum regular value (<code>null</code> permitted).
	 * @param maxRegularValue
	 *           the maximum regular value (<code>null</code> permitted).
	 * @param minOutlier
	 *           the minimum outlier (<code>null</code> permitted).
	 * @param maxOutlier
	 *           the maximum outlier (<code>null</code> permitted).
	 * @param outliers
	 *           the outliers (<code>null</code> permitted).
	 */
	public BoxAndWhiskerItem(final Number mean,
										final Number median,
										final Number q1,
										final Number q3,
										final Number minRegularValue,
										final Number maxRegularValue,
										final Number minOutlier,
										final Number maxOutlier,
										final List outliers) {

		this.mean = mean;
		this.median = median;
		this.q1 = q1;
		this.q3 = q3;
		this.minRegularValue = minRegularValue;
		this.maxRegularValue = maxRegularValue;
		this.minOutlier = minOutlier;
		this.maxOutlier = maxOutlier;
		this.outliers = outliers;

	}

	/**
	 * Returns the mean.
	 * 
	 * @return the mean (possibly <code>null</code>).
	 */
	public Number getMean() {
		return this.mean;
	}

	/**
	 * Returns the median.
	 * 
	 * @return the median (possibly <code>null</code>).
	 */
	public Number getMedian() {
		return this.median;
	}

	/**
	 * Returns the first quartile.
	 * 
	 * @return the first quartile (possibly <code>null</code>).
	 */
	public Number getQ1() {
		return this.q1;
	}

	/**
	 * Returns the third quartile.
	 * 
	 * @return the third quartile (possibly <code>null</code>).
	 */
	public Number getQ3() {
		return this.q3;
	}

	/**
	 * Returns the minimum regular value.
	 * 
	 * @return the minimum regular value (possibly <code>null</code>).
	 */
	public Number getMinRegularValue() {
		return this.minRegularValue;
	}

	/**
	 * Returns the maximum regular value.
	 * 
	 * @return the maximum regular value (possibly <code>null</code>).
	 */
	public Number getMaxRegularValue() {
		return this.maxRegularValue;
	}

	/**
	 * Returns the minimum outlier.
	 * 
	 * @return the minimum outlier (possibly <code>null</code>).
	 */
	public Number getMinOutlier() {
		return this.minOutlier;
	}

	/**
	 * Returns the maximum outlier.
	 * 
	 * @return the maximum outlier (possibly <code>null</code>).
	 */
	public Number getMaxOutlier() {
		return this.maxOutlier;
	}

	/**
	 * Returns a list of outliers.
	 * 
	 * @return a list of outliers (possibly <code>null</code>).
	 */
	public List getOutliers() {
		return Collections.unmodifiableList(this.outliers);
	}

	/**
	 * Tests this object for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object to test against (<code>null</code> permitted).
	 * @return a boolean.
	 */
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof BoxAndWhiskerItem) {
			final BoxAndWhiskerItem item = (BoxAndWhiskerItem) obj;
			final boolean b0 = ObjectUtils.equal(this.mean, item.mean);
			final boolean b1 = ObjectUtils.equal(this.median, item.median);
			final boolean b2 = ObjectUtils.equal(this.q1, item.q1);
			final boolean b3 = ObjectUtils.equal(this.q3, item.q3);
			final boolean b4 = ObjectUtils.equal(this.minRegularValue, item.minRegularValue);
			final boolean b5 = ObjectUtils.equal(this.maxRegularValue, item.maxRegularValue);
			final boolean b6 = ObjectUtils.equal(this.minOutlier, item.minOutlier);
			final boolean b7 = ObjectUtils.equal(this.maxOutlier, item.maxOutlier);
			final boolean b8 = ObjectUtils.equal(this.outliers, item.outliers);
			return b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8;
		}

		return false;
	}

}
