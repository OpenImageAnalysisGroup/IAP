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
 * -----------------------------------
 * DefaultIntervalCategoryDataset.java
 * -----------------------------------
 * (C) Copyright 2002-2004, by Jeremy Bowman and Contributors.
 * Original Author: Jeremy Bowman;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultIntervalCategoryDataset.java,v 1.1 2011-01-31 09:02:17 klukas Exp $
 * Changes
 * -------
 * 29-Apr-2002 : Version 1, contributed by Jeremy Bowman (DG);
 * 24-Oct-2002 : Amendments for changes made to the dataset interface (DG);
 */

package org.jfree.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A convenience class that provides a default implementation of the {@link IntervalCategoryDataset} interface.
 * <p>
 * The standard constructor accepts data in a two dimensional array where the first dimension is the series, and the second dimension is the category.
 * 
 * @author Jeremy Bowman
 */
public class DefaultIntervalCategoryDataset extends AbstractSeriesDataset
															implements IntervalCategoryDataset {

	/** The series keys. */
	private Comparable[] seriesKeys;

	/** The category keys. */
	private Comparable[] categoryKeys;

	/** Storage for the start value data. */
	private Number[][] startData;

	/** Storage for the end value data. */
	private Number[][] endData;

	/**
	 * Creates a new dataset.
	 * 
	 * @param starts
	 *           the starting values for the intervals.
	 * @param ends
	 *           the ending values for the intervals.
	 */
	public DefaultIntervalCategoryDataset(final double[][] starts, final double[][] ends) {

		this(DatasetUtilities.createNumberArray2D(starts),
							DatasetUtilities.createNumberArray2D(ends));

	}

	/**
	 * Constructs a dataset and populates it with data from the array.
	 * <p>
	 * The arrays are indexed as data[series][category]. Series and category names are automatically generated - you can change them using the setSeriesName(...)
	 * and setCategory(...) methods.
	 * 
	 * @param starts
	 *           the start values data.
	 * @param ends
	 *           the end values data.
	 */
	public DefaultIntervalCategoryDataset(final Number[][] starts, final Number[][] ends) {

		this(null, null, starts, ends);

	}

	/**
	 * Constructs a DefaultIntervalCategoryDataset, populates it with data
	 * from the arrays, and uses the supplied names for the series.
	 * <p>
	 * Category names are generated automatically ("Category 1", "Category 2", etc).
	 * 
	 * @param seriesNames
	 *           the series names.
	 * @param starts
	 *           the start values data, indexed as data[series][category].
	 * @param ends
	 *           the end values data, indexed as data[series][category].
	 */
	public DefaultIntervalCategoryDataset(final String[] seriesNames,
														final Number[][] starts,
														final Number[][] ends) {

		this(seriesNames, null, starts, ends);

	}

	/**
	 * Constructs a DefaultIntervalCategoryDataset, populates it with data
	 * from the arrays, and uses the supplied names for the series and the
	 * supplied objects for the categories.
	 * 
	 * @param seriesKeys
	 *           the series keys.
	 * @param categoryKeys
	 *           the categories.
	 * @param starts
	 *           the start values data, indexed as data[series][category].
	 * @param ends
	 *           the end values data, indexed as data[series][category].
	 */
	public DefaultIntervalCategoryDataset(final Comparable[] seriesKeys,
														final Comparable[] categoryKeys,
														final Number[][] starts,
														final Number[][] ends) {

		this.startData = starts;
		this.endData = ends;

		if (starts != null && ends != null) {

			final String baseName = "org.jfree.data.resources.DataPackageResources";
			final ResourceBundle resources = ResourceBundle.getBundle(baseName);

			final int seriesCount = starts.length;
			if (seriesCount != ends.length) {
				final String errMsg = "DefaultIntervalCategoryDataset: the number "
									+ "of series in the start value dataset does "
									+ "not match the number of series in the end "
									+ "value dataset.";
				throw new IllegalArgumentException(errMsg);
			}
			if (seriesCount > 0) {

				// set up the series names...
				if (seriesKeys != null) {

					if (seriesKeys.length != seriesCount) {
						throw new IllegalArgumentException(
											"DefaultIntervalCategoryDataset: the number of series keys does "
																+ "not match the number of series in the data.");
					}

					this.seriesKeys = seriesKeys;
				} else {
					final String prefix = resources.getString("series.default-prefix") + " ";
					this.seriesKeys = generateKeys(seriesCount, prefix);
				}

				// set up the category names...
				final int categoryCount = starts[0].length;
				if (categoryCount != ends[0].length) {
					final String errMsg = "DefaultIntervalCategoryDataset: the "
											+ "number of categories in the start value "
											+ "dataset does not match the number of "
											+ "categories in the end value dataset.";
					throw new IllegalArgumentException(errMsg);
				}
				if (categoryKeys != null) {
					if (categoryKeys.length != categoryCount) {
						throw new IllegalArgumentException(
											"DefaultIntervalCategoryDataset: the number of category keys does "
																+ "not match the number of categories in the data.");
					}
					this.categoryKeys = categoryKeys;
				} else {
					final String prefix = resources.getString("categories.default-prefix") + " ";
					this.categoryKeys = generateKeys(categoryCount, prefix);
				}

			} else {
				this.seriesKeys = null;
				this.categoryKeys = null;
			}
		}

	}

	/**
	 * Returns the number of series in the dataset (possibly zero).
	 * 
	 * @return The number of series in the dataset.
	 */
	public int getSeriesCount() {

		int result = 0;
		if (this.startData != null) {
			result = this.startData.length;
		}
		return result;

	}

	/**
	 * Returns the item count.
	 * 
	 * @return The item count.
	 */
	public int getItemCount() {
		return this.categoryKeys.length;
	}

	/**
	 * Returns a category key.
	 * 
	 * @param item
	 *           the category index.
	 * @return The category key.
	 */
	public Comparable getCategory(final int item) {
		return this.categoryKeys[item];
	}

	/**
	 * Returns an item.
	 * 
	 * @param category
	 *           the category key.
	 * @return The item index.
	 */
	public int getItem(final Object category) {
		final List categories = getCategories();
		return categories.indexOf(category);
	}

	/**
	 * Returns a series index.
	 * 
	 * @param series
	 *           the series.
	 * @return The series index.
	 */
	public int getSeriesIndex(final Object series) {
		final List seriesKeys = getSeries();
		return seriesKeys.indexOf(series);
	}

	/**
	 * Returns the name of the specified series.
	 * 
	 * @param series
	 *           the index of the required series (zero-based).
	 * @return the name of the specified series.
	 */
	public Comparable getSeries(final int series) {

		// check argument...
		if ((series >= getSeriesCount()) || (series < 0)) {
			throw new IllegalArgumentException(
								"DefaultCategoryDataset.getSeriesName(int): no such series.");
		}

		// return the value...
		return this.seriesKeys[series];

	}

	/**
	 * Returns the name of the specified series.
	 * 
	 * @param series
	 *           The index of the required series (zero-based).
	 * @return the name of the specified series.
	 */
	public String getSeriesName(final int series) {

		// check argument...
		if ((series >= getSeriesCount()) || (series < 0)) {

			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.getSeriesName(int): no such series.");
		}

		// return the value...
		return this.seriesKeys[series].toString();

	}

	/**
	 * Sets the names of the series in the dataset.
	 * 
	 * @param seriesKeys
	 *           the keys of the series in the dataset.
	 */
	public void setSeriesKeys(final Comparable[] seriesKeys) {

		// check argument...
		if (seriesKeys == null) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.setSeriesKeys(): null not permitted.");
		}

		if (seriesKeys.length != getSeriesCount()) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.setSeriesKeys(): "
													+ "the number of series keys does not match the data.");
		}

		// make the change...
		this.seriesKeys = seriesKeys;
		fireDatasetChanged();

	}

	/**
	 * Returns the number of categories in the dataset.
	 * <P>
	 * This method is part of the CategoryDataset interface.
	 * 
	 * @return the number of categories in the dataset.
	 */
	public int getCategoryCount() {

		int result = 0;

		if (this.startData != null) {
			if (getSeriesCount() > 0) {
				result = this.startData[0].length;
			}
		}

		return result;

	}

	/**
	 * Returns a list of the series in the dataset.
	 * <P>
	 * Supports the CategoryDataset interface.
	 * 
	 * @return a list of the series in the dataset.
	 */
	public List getSeries() {

		// the CategoryDataset interface expects a list of series, but
		// we've stored them in an array...
		if (this.seriesKeys == null) {
			return new java.util.ArrayList();
		} else {
			return Collections.unmodifiableList(Arrays.asList(this.seriesKeys));
		}

	}

	/**
	 * Returns a list of the categories in the dataset.
	 * <P>
	 * Supports the CategoryDataset interface.
	 * 
	 * @return a list of the categories in the dataset.
	 */
	public List getCategories() {
		return getColumnKeys();
	}

	/**
	 * Returns a list of the categories in the dataset.
	 * <P>
	 * Supports the CategoryDataset interface.
	 * 
	 * @return a list of the categories in the dataset.
	 */
	public List getColumnKeys() {

		// the CategoryDataset interface expects a list of categories, but
		// we've stored them in an array...
		if (this.categoryKeys == null) {
			return new ArrayList();
		} else {
			return Collections.unmodifiableList(Arrays.asList(this.categoryKeys));
		}

	}

	/**
	 * Sets the categories for the dataset.
	 * 
	 * @param categoryKeys
	 *           An array of objects representing the categories in the dataset.
	 */
	public void setCategoryKeys(final Comparable[] categoryKeys) {

		// check arguments...
		if (categoryKeys == null) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.setCategories(...): null not permitted.");
		}

		if (categoryKeys.length != this.startData[0].length) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.setCategoryKeys(...): "
													+ "the number of categories does not match the data.");
		}

		for (int i = 0; i < categoryKeys.length; i++) {
			if (categoryKeys[i] == null) {
				throw new IllegalArgumentException(
									"DefaultIntervalCategoryDataset.setCategoryKeys(...): "
														+ "null category not permitted.");
			}
		}

		// make the change...
		this.categoryKeys = categoryKeys;
		fireDatasetChanged();

	}

	/**
	 * Returns the data value for one category in a series.
	 * <P>
	 * This method is part of the CategoryDataset interface. Not particularly meaningful for this class...returns the end value.
	 * 
	 * @param series
	 *           The required series (zero based index).
	 * @param category
	 *           The required category.
	 * @return The data value for one category in a series (null possible).
	 */
	public Number getValue(final Comparable series, final Comparable category) {
		final int seriesIndex = getSeriesIndex(series);
		final int itemIndex = getItem(category);
		return getValue(seriesIndex, itemIndex);
	}

	/**
	 * Returns the data value for one category in a series.
	 * <P>
	 * This method is part of the CategoryDataset interface. Not particularly meaningful for this class...returns the end value.
	 * 
	 * @param series
	 *           The required series (zero based index).
	 * @param category
	 *           The required category.
	 * @return The data value for one category in a series (null possible).
	 */
	public Number getValue(final int series, final int category) {

		return getEndValue(series, category);

	}

	/**
	 * Returns the start data value for one category in a series.
	 * <P>
	 * This method is part of the IntervalTableDataset interface.
	 * 
	 * @param series
	 *           The required series.
	 * @param category
	 *           The required category.
	 * @return The start data value for one category in a series (null possible).
	 */
	public Number getStartValue(final Comparable series, final Comparable category) {
		final int seriesIndex = getSeriesIndex(series);
		final int itemIndex = getItem(category);
		return getStartValue(seriesIndex, itemIndex);
	}

	/**
	 * Returns the start data value for one category in a series.
	 * <P>
	 * This method is part of the IntervalTableDataset interface.
	 * 
	 * @param series
	 *           The required series (zero based index).
	 * @param category
	 *           The required category.
	 * @return The start data value for one category in a series (null possible).
	 */
	public Number getStartValue(final int series, final int category) {

		// check arguments...
		if ((series < 0) || (series >= getSeriesCount())) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.getValue(...): "
													+ "series index out of range.");
		}

		if ((category < 0) || (category >= getCategoryCount())) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.getValue(...): "
													+ "category index out of range.");
		}

		// fetch the value...
		return this.startData[series][category];

	}

	/**
	 * Returns the end data value for one category in a series.
	 * <P>
	 * This method is part of the IntervalTableDataset interface.
	 * 
	 * @param series
	 *           the required series.
	 * @param category
	 *           the required category.
	 * @return the end data value for one category in a series (null possible).
	 */
	public Number getEndValue(final Comparable series, final Comparable category) {
		final int seriesIndex = getSeriesIndex(series);
		final int itemIndex = getItem(category);
		return getEndValue(seriesIndex, itemIndex);
	}

	/**
	 * Returns the end data value for one category in a series.
	 * <P>
	 * This method is part of the IntervalTableDataset interface.
	 * 
	 * @param series
	 *           the required series (zero based index).
	 * @param category
	 *           the required category.
	 * @return the end data value for one category in a series (null possible).
	 */
	public Number getEndValue(final int series, final int category) {

		// check arguments...
		if ((series < 0) || (series >= getSeriesCount())) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.getValue(...): "
													+ "series index out of range.");
		}

		if ((category < 0) || (category >= getCategoryCount())) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.getValue(...): "
													+ "category index out of range.");
		}

		// fetch the value...
		return this.endData[series][category];

	}

	/**
	 * Sets the start data value for one category in a series.
	 * 
	 * @param series
	 *           The series (zero-based index).
	 * @param category
	 *           The category.
	 * @param value
	 *           The value.
	 */
	public void setStartValue(final int series, final Object category, final Number value) {

		// does the series exist?
		if ((series < 0) || (series > getSeriesCount())) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.setValue: "
													+ "series outside valid range.");
		}

		// is the category valid?
		final int categoryIndex = getCategoryIndex(category);
		if (categoryIndex < 0) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.setValue: "
													+ "unrecognised category.");
		}

		// update the data...
		this.startData[series][categoryIndex] = value;
		fireDatasetChanged();

	}

	/**
	 * Sets the end data value for one category in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param category
	 *           the category.
	 * @param value
	 *           the value.
	 */
	public void setEndValue(final int series, final Object category, final Number value) {

		// does the series exist?
		if ((series < 0) || (series > getSeriesCount())) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.setValue: "
													+ "series outside valid range.");
		}

		// is the category valid?
		final int categoryIndex = getCategoryIndex(category);
		if (categoryIndex < 0) {
			throw new IllegalArgumentException(
								"DefaultIntervalCategoryDataset.setValue: "
													+ "unrecognised category.");
		}

		// update the data...
		this.endData[series][categoryIndex] = value;
		fireDatasetChanged();

	}

	/**
	 * Returns the index for the given category.
	 * 
	 * @param category
	 *           the category.
	 * @return the index.
	 */
	private int getCategoryIndex(final Object category) {

		int result = -1;
		for (int i = 0; i < this.categoryKeys.length; i++) {
			if (category.equals(this.categoryKeys[i])) {
				result = i;
				break;
			}
		}
		return result;

	}

	/**
	 * Generates an array of keys, by appending a space plus an integer
	 * (starting with 1) to the supplied prefix string.
	 * 
	 * @param count
	 *           the number of keys required.
	 * @param prefix
	 *           the name prefix.
	 * @return an array of <i>prefixN</i> with N = { 1 .. count}.
	 */
	private Comparable[] generateKeys(final int count, final String prefix) {

		final Comparable[] result = new Comparable[count];
		String name;
		for (int i = 0; i < count; i++) {
			name = prefix + (i + 1);
			result[i] = name;
		}
		return result;

	}

	/**
	 * Returns a column key.
	 * 
	 * @param item
	 *           the column index.
	 * @return The column key.
	 */
	public Comparable getColumnKey(final int item) {
		return this.categoryKeys[item];
	}

	/**
	 * Returns a column index.
	 * 
	 * @param columnKey
	 *           the column key.
	 * @return The column index.
	 */
	public int getColumnIndex(final Comparable columnKey) {
		final List categories = getCategories();
		return categories.indexOf(columnKey);
	}

	/**
	 * Returns a row index.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @return The row index.
	 */
	public int getRowIndex(final Comparable rowKey) {
		final List seriesKeys = getSeries();
		return seriesKeys.indexOf(rowKey);
	}

	/**
	 * Returns a list of the series in the dataset.
	 * <P>
	 * Supports the CategoryDataset interface.
	 * 
	 * @return a list of the series in the dataset.
	 */
	public List getRowKeys() {

		// the CategoryDataset interface expects a list of series, but
		// we've stored them in an array...
		if (this.seriesKeys == null) {
			return new java.util.ArrayList();
		} else {
			return Collections.unmodifiableList(Arrays.asList(this.seriesKeys));
		}

	}

	/**
	 * Returns the name of the specified series.
	 * 
	 * @param series
	 *           the index of the required series (zero-based).
	 * @return the name of the specified series.
	 */
	public Comparable getRowKey(final int series) {

		// check argument...
		if ((series >= getSeriesCount()) || (series < 0)) {
			throw new IllegalArgumentException(
								"DefaultCategoryDataset.getSeriesName(int): no such series.");
		}

		// return the value...
		return this.seriesKeys[series];

	}

	/**
	 * Returns the number of categories in the dataset.
	 * <P>
	 * This method is part of the CategoryDataset interface.
	 * 
	 * @return the number of categories in the dataset.
	 */
	public int getColumnCount() {

		int result = 0;

		if (this.startData != null) {
			if (getSeriesCount() > 0) {
				result = this.startData[0].length;
			}
		}

		return result;

	}

	/**
	 * Returns the number of series in the dataset (possibly zero).
	 * 
	 * @return the number of series in the dataset.
	 */
	public int getRowCount() {

		int result = 0;
		if (this.startData != null) {
			result = this.startData.length;
		}
		return result;

	}

}
