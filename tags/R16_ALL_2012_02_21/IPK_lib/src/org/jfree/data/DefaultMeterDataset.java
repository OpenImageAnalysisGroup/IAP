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
 * ------------------------
 * DefaultMeterDataset.java
 * ------------------------
 * (C) Copyright 2002-2004, by Hari and Contributors.
 * Original Author: Hari;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultMeterDataset.java,v 1.1 2011-01-31 09:02:12 klukas Exp $
 * Changes
 * -------
 * 02-Apr-2002 : Version 1, based on code contributed by Hari (DG);
 * 16-Apr-2002 : Updated to the latest version from Hari (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 19-May-2004 : Fixed bug 939500 (interface now extends ValueDataset) and also deprecated this
 * class (should use DefaultValueDataset) (DG)
 */

package org.jfree.data;

import java.io.Serializable;

import org.jfree.chart.plot.MeterPlot;

/**
 * A default implementation of the {@link MeterDataset} interface.
 * 
 * @deprecated Use ValueDataset instead, this interface mixes data and presentation items.
 */
public class DefaultMeterDataset extends AbstractDataset implements MeterDataset, Serializable {

	/** The default adjustment. */
	private static final double DEFAULT_ADJ = 1.0;

	/** The current value. */
	private Number value;

	/** The lower bound of the overall range. */
	private Number min;

	/** The upper bound of the overall range. */
	private Number max;

	/** The lower bound of the 'normal' range. */
	private Number minNormal;

	/** The upper bound of the 'normal' range. */
	private Number maxNormal;

	/** The lower bound of the 'warning' range. */
	private Number minWarning;

	/** The upper bound of the 'warning' range. */
	private Number maxWarning;

	/** The lower bound of the 'critical' range. */
	private Number minCritical;

	/** The upper bound of the 'critical' range. */
	private Number maxCritical;

	/** The border type. */
	private int borderType;

	/** The units. */
	private String units;

	/**
	 * Default constructor.
	 */
	public DefaultMeterDataset() {
		this(new Double(0), new Double(0), null, null);
	}

	/**
	 * Creates a new dataset.
	 * 
	 * @param min
	 *           the minimum value.
	 * @param max
	 *           the maximum value.
	 * @param value
	 *           the current value.
	 * @param units
	 *           the unit description.
	 */
	public DefaultMeterDataset(final Number min,
											final Number max,
											final Number value,
											final String units) {
		this(
							min, max, value, units, null, null, null, null, null, null, MeterPlot.FULL_DATA_RANGE);
	}

	/**
	 * Creates a new dataset.
	 * 
	 * @param min
	 *           the lower bound for the overall range.
	 * @param max
	 *           the upper bound for the overall range.
	 * @param value
	 *           the current value.
	 * @param units
	 *           the unit description.
	 * @param minCritical
	 *           the minimum critical value.
	 * @param maxCritical
	 *           the maximum critical value.
	 * @param minWarning
	 *           the minimum warning value.
	 * @param maxWarning
	 *           the maximum warning value.
	 * @param minNormal
	 *           the minimum normal value.
	 * @param maxNormal
	 *           the maximum normal value.
	 * @param borderType
	 *           the border type.
	 */
	public DefaultMeterDataset(final Number min, final Number max, final Number value,
											final String units,
											final Number minCritical, final Number maxCritical,
											final Number minWarning, final Number maxWarning,
											final Number minNormal, final Number maxNormal,
											final int borderType) {

		setRange(min, max);
		setValue(value);
		setUnits(units);
		setCriticalRange(minCritical, maxCritical);
		setWarningRange(minWarning, maxWarning);
		setNormalRange(minNormal, maxNormal);
		setBorderType(borderType);

	}

	/**
	 * Returns <code>true</code> if the value is valid, and <code>false</code> otherwise.
	 * 
	 * @return A boolean.
	 */
	public boolean isValueValid() {
		return (this.value != null);
	}

	/**
	 * Returns the value.
	 * 
	 * @return The value.
	 */
	public Number getValue() {
		return this.value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *           the new value.
	 */
	public void setValue(final double value) {
		setValue(new Double(value));
	}

	/**
	 * Sets the value for the dataset.
	 * 
	 * @param value
	 *           the new value.
	 */
	public void setValue(final Number value) {

		if (value != null && this.min != null && this.max != null) {
			if (value.doubleValue() < this.min.doubleValue()
								|| value.doubleValue() > this.max.doubleValue()) {

				throw new IllegalArgumentException("Value is out of range for min/max");

			}
		}
		this.value = value;
		if (value != null && this.min != null && this.max != null) {
			if (this.min.doubleValue() == this.max.doubleValue()) {
				this.min = new Double(value.doubleValue() - DEFAULT_ADJ);
				this.max = new Double(value.doubleValue() + DEFAULT_ADJ);
			}
		}
		fireDatasetChanged();

	}

	/**
	 * Returns the minimum value.
	 * 
	 * @return The minimum value.
	 */
	public Number getMinimumValue() {
		return this.min;
	}

	/**
	 * Returns the maximum value.
	 * 
	 * @return The maximum value.
	 */
	public Number getMaximumValue() {
		return this.max;
	}

	/**
	 * Returns the minimum normal value.
	 * 
	 * @return The minimum normal value.
	 */
	public Number getMinimumNormalValue() {
		return this.minNormal;
	}

	/**
	 * Returns the maximum normal value.
	 * 
	 * @return The maximum normal value.
	 */
	public Number getMaximumNormalValue() {
		return this.maxNormal;
	}

	/**
	 * Returns the minimum warning value.
	 * 
	 * @return The minimum warning value.
	 */
	public Number getMinimumWarningValue() {
		return this.minWarning;
	}

	/**
	 * Returns the maximum warning value.
	 * 
	 * @return The maximum warning value.
	 */
	public Number getMaximumWarningValue() {
		return this.maxWarning;
	}

	/**
	 * Returns the minimum critical value.
	 * 
	 * @return The minimum critical value.
	 */
	public Number getMinimumCriticalValue() {
		return this.minCritical;
	}

	/**
	 * Returns the maximum critical value.
	 * 
	 * @return The maximum critical value.
	 */
	public Number getMaximumCriticalValue() {
		return this.maxCritical;
	}

	/**
	 * Sets the range for the dataset. Registered listeners are notified of the change.
	 * 
	 * @param min
	 *           the new minimum.
	 * @param max
	 *           the new maximum.
	 */
	public void setRange(Number min, Number max) {

		if (min == null || max == null) {
			throw new IllegalArgumentException("Min/Max should not be null");
		}

		// swap min and max if necessary...
		if (min.doubleValue() > max.doubleValue()) {
			final Number temp = min;
			min = max;
			max = temp;
		}

		if (this.value != null) {
			if (min.doubleValue() == max.doubleValue()) {
				min = new Double(this.value.doubleValue() - DEFAULT_ADJ);
				max = new Double(this.value.doubleValue() + DEFAULT_ADJ);
			}
		}
		this.min = min;
		this.max = max;
		fireDatasetChanged();

	}

	/**
	 * Sets the normal range for the dataset. Registered listeners are
	 * notified of the change.
	 * 
	 * @param minNormal
	 *           the new minimum.
	 * @param maxNormal
	 *           the new maximum.
	 */
	public void setNormalRange(final Number minNormal, final Number maxNormal) {

		this.minNormal = minNormal;
		this.maxNormal = maxNormal;

		if (this.minNormal != null && this.minNormal.doubleValue() < this.min.doubleValue()) {
			this.min = this.minNormal;
		}
		if (this.maxNormal != null && this.maxNormal.doubleValue() > this.max.doubleValue()) {
			this.max = this.maxNormal;
		}
		fireDatasetChanged();
	}

	/**
	 * Sets the warning range for the dataset. Registered listeners are
	 * notified of the change.
	 * 
	 * @param minWarning
	 *           the new minimum.
	 * @param maxWarning
	 *           the new maximum.
	 */
	public void setWarningRange(final Number minWarning, final Number maxWarning) {

		this.minWarning = minWarning;
		this.maxWarning = maxWarning;

		if (this.minWarning != null && this.minWarning.doubleValue() < this.min.doubleValue()) {
			this.min = this.minWarning;
		}
		if (this.maxWarning != null && this.maxWarning.doubleValue() > this.max.doubleValue()) {
			this.max = this.maxWarning;
		}
		fireDatasetChanged();

	}

	/**
	 * Sets the critical range for the dataset. Registered listeners are
	 * notified of the change.
	 * 
	 * @param minCritical
	 *           the new minimum.
	 * @param maxCritical
	 *           the new maximum.
	 */
	public void setCriticalRange(final Number minCritical, final Number maxCritical) {

		this.minCritical = minCritical;
		this.maxCritical = maxCritical;

		if (this.minCritical != null && this.minCritical.doubleValue() < this.min.doubleValue()) {
			this.min = this.minCritical;
		}
		if (this.maxCritical != null && this.maxCritical.doubleValue() > this.max.doubleValue()) {
			this.max = this.maxCritical;
		}
		fireDatasetChanged();

	}

	/**
	 * Returns the measurement units for the data.
	 * 
	 * @return The measurement units.
	 */
	public String getUnits() {
		return this.units;
	}

	/**
	 * Sets the measurement unit description.
	 * 
	 * @param units
	 *           the new description.
	 */
	public void setUnits(final String units) {
		this.units = units;
		fireDatasetChanged();
	}

	/**
	 * Returns the border type.
	 * 
	 * @return The border type.
	 */
	public int getBorderType() {
		return this.borderType;
	}

	/**
	 * Sets the border type.
	 * 
	 * @param borderType
	 *           the new border type.
	 */
	public void setBorderType(final int borderType) {
		this.borderType = borderType;
		fireDatasetChanged();
	}

}
