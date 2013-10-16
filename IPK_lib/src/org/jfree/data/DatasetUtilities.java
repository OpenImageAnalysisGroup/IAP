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
 * ---------------------
 * DatasetUtilities.java
 * ---------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Andrzej Porebski (bug fix);
 * Jonathan Nash (bug fix);
 * Richard Atkinson;
 * Andreas Schroeder (beatification)
 * $Id: DatasetUtilities.java,v 1.1 2011-01-31 09:02:14 klukas Exp $
 * Changes (from 18-Sep-2001)
 * --------------------------
 * 18-Sep-2001 : Added standard header and fixed DOS encoding problem (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 15-Nov-2001 : Moved to package com.jrefinery.data.* in the JCommon class library (DG);
 * Changed to handle null values from datasets (DG);
 * Bug fix (thanks to Andrzej Porebski) - initial value now set to positive or
 * negative infinity when iterating (DG);
 * 22-Nov-2001 : Datasets with containing no data now return null for min and max calculations (DG);
 * 13-Dec-2001 : Extended to handle HighLowDataset and IntervalXYDataset (DG);
 * 15-Feb-2002 : Added getMinimumStackedRangeValue() and getMaximumStackedRangeValue() (DG);
 * 28-Feb-2002 : Renamed Datasets.java --> DatasetUtilities.java (DG);
 * 18-Mar-2002 : Fixed bug in min/max domain calculation for datasets that implement the
 * CategoryDataset interface AND the XYDataset interface at the same time. Thanks
 * to Jonathan Nash for the fix (DG);
 * 23-Apr-2002 : Added getDomainExtent() and getRangeExtent() methods (DG);
 * 13-Jun-2002 : Modified range measurements to handle IntervalCategoryDataset (DG);
 * 12-Jul-2002 : Method name change in DomainInfo interface (DG);
 * 30-Jul-2002 : Added pie dataset summation method (DG);
 * 01-Oct-2002 : Added a method for constructing an XYDataset from a Function2D instance (DG);
 * 24-Oct-2002 : Amendments required following changes to the CategoryDataset interface (DG);
 * 18-Nov-2002 : Changed CategoryDataset to TableDataset (DG);
 * 04-Mar-2003 : Added isEmpty(XYDataset) method (DG);
 * 05-Mar-2003 : Added a method for creating a CategoryDataset from a KeyedValues instance (DG);
 * 15-May-2003 : Renamed isEmpty --> isEmptyOrNull (DG);
 * 25-Jun-2003 : Added limitPieDataset methods (RA);
 * 26-Jun-2003 : Modified getDomainExtent(...) method to accept null datasets (DG);
 * 27-Jul-2003 : Added getStackedRangeExtent(TableXYDataset data) (RA);
 * 18-Aug-2003 : getStackedRangeExtent(TableXYDataset data) now handles null values (RA);
 * 02-Sep-2003 : Added method to check for null or empty PieDataset (DG);
 * 18-Sep-2003 : Fix for bug 803660 (getMaximumRangeValue for CategoryDataset) (DG);
 * 20-Oct-2003 : Added getCumulativeRangeExtent(...) method (DG);
 * 09-Jan-2003 : Added argument checking code to the createCategoryDataset(...) method (DG);
 * 23-Mar-2004 : Fixed bug in getMaximumStackedRangeValue() method (DG);
 * 31-Mar-2004 : Exposed the extent iteration algorithms to use one of them and applied
 * noninstantiation pattern (AS);
 * 11-May-2004 : Renamed getPieDatasetTotal --> calculatePieDatasetTotal (DG);
 */

package org.jfree.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of useful static methods relating to datasets.
 */
public final class DatasetUtilities {

	/**
	 * Private constructor for non-instanceability.
	 */
	private DatasetUtilities() {
		// now try to instanciate this ;-)
	}

	/**
	 * Constructs an array of <code>Number</code> objects from an array of <code>double</code> primitives.
	 * 
	 * @param data
	 *           the data.
	 * @return an array of <code>Double</code>.
	 */
	public static Number[] createNumberArray(final double[] data) {

		final Number[] result = new Number[data.length];

		for (int i = 0; i < data.length; i++) {
			result[i] = new Double(data[i]);
		}

		return result;

	}

	/**
	 * Constructs an array of arrays of <code>Number</code> objects from a corresponding
	 * structure containing <code>double</code> primitives.
	 * 
	 * @param data
	 *           the data.
	 * @return an array of <code>Double</code>.
	 */
	public static Number[][] createNumberArray2D(final double[][] data) {

		final int l1 = data.length;
		final int l2 = data[0].length;

		final Number[][] result = new Number[l1][l2];

		for (int i = 0; i < l1; i++) {
			result[i] = createNumberArray(data[i]);
		}

		return result;

	}

	/**
	 * Returns the range of values in the domain for the dataset.
	 * <P>
	 * If the supplied dataset is <code>null</code>, the range returned is <code>null</code>.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 * @return The range of values (possibly <code>null</code>).
	 */
	public static Range getDomainExtent(final Dataset dataset) {

		// check parameters...
		if (dataset == null) {
			return null;
		}

		if ((dataset instanceof CategoryDataset) && !(dataset instanceof XYDataset)) {
			throw new IllegalArgumentException(
								"The dataset does not have a numerical domain.");
		}

		// work out the minimum value...
		if (dataset instanceof DomainInfo) {
			final DomainInfo info = (DomainInfo) dataset;
			return info.getDomainRange();
		}

		// hasn't implemented DomainInfo, so iterate...
		else
			if (dataset instanceof XYDataset) {
				return iterateDomainExtent((XYDataset) dataset);
			} else {
				return null; // unrecognised dataset...how should this be handled?
			}
	}

	/**
	 * Iterates over the data item of the xy dataset to find
	 * the domain extent.
	 * 
	 * @param data
	 *           the xy dataset to iterate over.
	 * @return the domain extent of the data within the dataset.
	 */
	public static Range iterateDomainExtent(final XYDataset data) {
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = Double.NEGATIVE_INFINITY;
		final int seriesCount = data.getSeriesCount();
		for (int series = 0; series < seriesCount; series++) {
			final int itemCount = data.getItemCount(series);
			for (int item = 0; item < itemCount; item++) {

				final Number lvalue;
				final Number uvalue;
				if (data instanceof IntervalXYDataset) {
					final IntervalXYDataset intervalXYData = (IntervalXYDataset) data;
					lvalue = intervalXYData.getStartXValue(series, item);
					uvalue = intervalXYData.getEndXValue(series, item);
				} else {
					lvalue = data.getXValue(series, item);
					uvalue = lvalue;
				}
				if (lvalue != null) {
					minimum = Math.min(minimum, lvalue.doubleValue());
				}
				if (uvalue != null) {
					maximum = Math.max(maximum, uvalue.doubleValue());
				}

			}
		}
		if (minimum == Double.POSITIVE_INFINITY) {
			return null;
		} else {
			return new Range(minimum, maximum);
		}
	}

	/**
	 * Returns the range of values in the range for the dataset. This method
	 * is the partner for the getDomainExtent method.
	 * 
	 * @param data
	 *           the dataset.
	 * @return the range of values in the range for the dataset.
	 */
	public static Range getRangeExtent(final Dataset data) {

		// check parameters...
		if (data == null) {
			return null;
		}

		// work out the minimum value...
		if (data instanceof RangeInfo) {
			final RangeInfo info = (RangeInfo) data;
			return info.getValueRange();
		}

		// hasn't implemented RangeInfo, so we'll have to iterate...
		else
			if (data instanceof CategoryDataset) {
				return iterateCategoryRangeExtent((CategoryDataset) data);
			}
			// hasn't implemented RangeInfo, so we'll have to iterate...
			else
				if (data instanceof XYDataset) {
					return iterateXYRangeExtent((XYDataset) data);
				} else {
					return null;
				}
	}

	/**
	 * Iterates over the data item of the category dataset to find
	 * the range extent.
	 * 
	 * @param data
	 *           the category dataset to iterate over.
	 * @return the range extent of the data within the dataset.
	 */
	public static Range iterateCategoryRangeExtent(final CategoryDataset data) {
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = Double.NEGATIVE_INFINITY;
		final int rowCount = data.getRowCount();
		final int columnCount = data.getColumnCount();
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				final Number lvalue;
				final Number uvalue;
				if (data instanceof IntervalCategoryDataset) {
					final IntervalCategoryDataset icd = (IntervalCategoryDataset) data;
					lvalue = icd.getStartValue(row, column);
					uvalue = icd.getEndValue(row, column);
				} else {
					lvalue = data.getValue(row, column);
					uvalue = lvalue;
				}
				if (lvalue != null) {
					minimum = Math.min(minimum, lvalue.doubleValue());
				}
				if (uvalue != null) {
					maximum = Math.max(maximum, uvalue.doubleValue());
				}
			}
		}
		if (minimum == Double.POSITIVE_INFINITY) {
			return null;
		} else {
			return new Range(minimum, maximum);
		}
	}

	/**
	 * Iterates over the data item of the xy dataset to find
	 * the range extent.
	 * 
	 * @param data
	 *           the xy dataset to iterate over.
	 * @return the range extent of the data within the dataset.
	 */
	public static Range iterateXYRangeExtent(final XYDataset data) {
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = Double.NEGATIVE_INFINITY;
		final int seriesCount = data.getSeriesCount();
		for (int series = 0; series < seriesCount; series++) {
			final int itemCount = data.getItemCount(series);
			for (int item = 0; item < itemCount; item++) {

				final Number lvalue;
				final Number uvalue;
				if (data instanceof IntervalXYDataset) {
					final IntervalXYDataset intervalXYData = (IntervalXYDataset) data;
					lvalue = intervalXYData.getStartYValue(series, item);
					uvalue = intervalXYData.getEndYValue(series, item);
				} else
					if (data instanceof HighLowDataset) {
						final HighLowDataset highLowData = (HighLowDataset) data;
						lvalue = highLowData.getLowValue(series, item);
						uvalue = highLowData.getHighValue(series, item);
					} else {
						lvalue = data.getYValue(series, item);
						uvalue = lvalue;
					}
				if (lvalue != null) {
					minimum = Math.min(minimum, lvalue.doubleValue());
				}
				if (uvalue != null) {
					maximum = Math.max(maximum, uvalue.doubleValue());
				}

			}
		}
		if (minimum == Double.POSITIVE_INFINITY) {
			return null;
		} else {
			return new Range(minimum, maximum);
		}
	}

	/**
	 * Returns the minimum domain value for the specified dataset.
	 * <P>
	 * This is easy if the dataset implements the DomainInfo interface (a good idea if there is an efficient way to determine the minimum value). Otherwise, it
	 * involves iterating over the entire data-set.
	 * <p>
	 * Returns null if all the data values in the dataset are null.
	 * 
	 * @param data
	 *           the dataset.
	 * @return the minimum domain value in the dataset (or null).
	 */
	public static Number getMinimumDomainValue(final Dataset data) {

		// check parameters...
		if (data == null) {
			throw new IllegalArgumentException(
								"DatasetUtilities.getMinimumDomainValue: null dataset not allowed.");
		}

		if ((data instanceof CategoryDataset) && !(data instanceof XYDataset)) {
			throw new IllegalArgumentException("DatasetUtilities.getMinimumDomainValue(...): "
								+ "TableDataset does not have numerical domain.");
		}

		// work out the minimum value...
		if (data instanceof DomainInfo) {
			final DomainInfo info = (DomainInfo) data;
			return info.getMinimumDomainValue();
		}

		// hasn't implemented DomainInfo, so iterate...
		else
			if (data instanceof XYDataset) {
				double minimum = Double.POSITIVE_INFINITY;
				final XYDataset xyData = (XYDataset) data;
				final int seriesCount = xyData.getSeriesCount();
				for (int series = 0; series < seriesCount; series++) {
					final int itemCount = xyData.getItemCount(series);
					for (int item = 0; item < itemCount; item++) {

						final Number value;
						if (data instanceof IntervalXYDataset) {
							final IntervalXYDataset intervalXYData = (IntervalXYDataset) data;
							value = intervalXYData.getStartXValue(series, item);
						} else {
							value = xyData.getXValue(series, item);
						}
						if (value != null) {
							minimum = Math.min(minimum, value.doubleValue());
						}

					}
				}
				if (minimum == Double.POSITIVE_INFINITY) {
					return null;
				} else {
					return new Double(minimum);
				}
			}

			else {
				return null; // unrecognised dataset...how should this be handled?
			}

	}

	/**
	 * Returns the maximum domain value for the specified dataset.
	 * <P>
	 * This is easy if the dataset implements the DomainInfo interface (a good idea if there is an efficient way to determine the maximum value). Otherwise, it
	 * involves iterating over the entire data-set.
	 * <p>
	 * Returns null if all the data values in the dataset are null.
	 * 
	 * @param data
	 *           the dataset.
	 * @return the maximum domain value in the dataset (or null).
	 */
	public static Number getMaximumDomainValue(final Dataset data) {

		// check parameters...
		if (data == null) {
			throw new IllegalArgumentException(
								"Datasets.getMaximumDomainValue: null dataset not allowed.");
		}

		if ((data instanceof CategoryDataset) && !(data instanceof XYDataset)) {
			throw new IllegalArgumentException("Datasets.getMaximumDomainValue(...): "
								+ "CategoryDataset does not have numerical domain.");
		}

		// work out the maximum value...
		if (data instanceof DomainInfo) {
			final DomainInfo info = (DomainInfo) data;
			return info.getMaximumDomainValue();
		}

		// hasn't implemented DomainInfo, so iterate...
		else
			if (data instanceof XYDataset) {
				final XYDataset xyData = (XYDataset) data;
				double maximum = Double.NEGATIVE_INFINITY;
				final int seriesCount = xyData.getSeriesCount();
				for (int series = 0; series < seriesCount; series++) {
					final int itemCount = xyData.getItemCount(series);
					for (int item = 0; item < itemCount; item++) {

						final Number value;
						if (data instanceof IntervalXYDataset) {
							final IntervalXYDataset intervalXYData = (IntervalXYDataset) data;
							value = intervalXYData.getEndXValue(series, item);
						} else {
							value = xyData.getXValue(series, item);
						}
						if (value != null) {
							maximum = Math.max(maximum, value.doubleValue());
						}
					}
				}
				if (maximum == Double.NEGATIVE_INFINITY) {
					return null;
				} else {
					return new Double(maximum);
				}

			} else {
				return null; // unrecognised dataset...how should this be handled?
			}

	}

	/**
	 * Returns the minimum range value for the specified dataset.
	 * <P>
	 * This is easy if the dataset implements the RangeInfo interface (a good idea if there is an efficient way to determine the minimum value). Otherwise, it
	 * involves iterating over the entire data-set.
	 * <p>
	 * Returns null if all the data values in the dataset are null.
	 * 
	 * @param data
	 *           the dataset.
	 * @return the minimum range value in the dataset (or null).
	 */
	public static Number getMinimumRangeValue(final Dataset data) {

		// check parameters...
		if (data == null) {
			throw new IllegalArgumentException(
								"Datasets.getMinimumRangeValue: null dataset not allowed.");
		}

		// work out the minimum value...
		if (data instanceof RangeInfo) {
			final RangeInfo info = (RangeInfo) data;
			return info.getMinimumRangeValue();
		}

		// hasn't implemented RangeInfo, so we'll have to iterate...
		else
			if (data instanceof CategoryDataset) {

				final CategoryDataset categoryData = (CategoryDataset) data;
				double minimum = Double.POSITIVE_INFINITY;
				final int seriesCount = categoryData.getRowCount();
				final int itemCount = categoryData.getColumnCount();
				for (int series = 0; series < seriesCount; series++) {
					for (int item = 0; item < itemCount; item++) {
						final Number value;
						if (data instanceof IntervalCategoryDataset) {
							final IntervalCategoryDataset icd = (IntervalCategoryDataset) data;
							value = icd.getStartValue(series, item);
						} else {
							value = categoryData.getValue(series, item);
						}
						if (value != null) {
							minimum = Math.min(minimum, value.doubleValue());
						}
					}
				}
				if (minimum == Double.POSITIVE_INFINITY) {
					return null;
				} else {
					return new Double(minimum);
				}

			} else
				if (data instanceof XYDataset) {

					// hasn't implemented RangeInfo, so we'll have to iterate...
					final XYDataset xyData = (XYDataset) data;
					double minimum = Double.POSITIVE_INFINITY;
					final int seriesCount = xyData.getSeriesCount();
					for (int series = 0; series < seriesCount; series++) {
						final int itemCount = xyData.getItemCount(series);
						for (int item = 0; item < itemCount; item++) {

							final Number value;
							if (data instanceof IntervalXYDataset) {
								final IntervalXYDataset intervalXYData = (IntervalXYDataset) data;
								value = intervalXYData.getStartYValue(series, item);
							} else
								if (data instanceof HighLowDataset) {
									final HighLowDataset highLowData = (HighLowDataset) data;
									value = highLowData.getLowValue(series, item);
								} else {
									value = xyData.getYValue(series, item);
								}
							if (value != null) {
								minimum = Math.min(minimum, value.doubleValue());
							}

						}
					}
					if (minimum == Double.POSITIVE_INFINITY) {
						return null;
					} else {
						return new Double(minimum);
					}

				} else {
					return null;
				}

	}

	/**
	 * Returns the maximum range value for the specified dataset.
	 * <P>
	 * This is easy if the dataset implements the RangeInfo interface (a good idea if there is an efficient way to determine the maximum value). Otherwise, it
	 * involves iterating over the entire data-set.
	 * <p>
	 * Returns null if all the data values are null.
	 * 
	 * @param data
	 *           the dataset.
	 * @return the maximum range value in the dataset (or null).
	 */
	public static Number getMaximumRangeValue(final Dataset data) {

		// check parameters...
		if (data == null) {
			throw new IllegalArgumentException(
								"Datasets.getMinimumRangeValue: null dataset not allowed.");
		}

		// work out the minimum value...
		if (data instanceof RangeInfo) {
			final RangeInfo info = (RangeInfo) data;
			return info.getMaximumRangeValue();
		}

		// hasn't implemented RangeInfo, so we'll have to iterate...
		else
			if (data instanceof CategoryDataset) {

				final CategoryDataset categoryData = (CategoryDataset) data;
				double maximum = Double.NEGATIVE_INFINITY;
				final int seriesCount = categoryData.getRowCount();
				final int itemCount = categoryData.getColumnCount();
				for (int series = 0; series < seriesCount; series++) {
					for (int item = 0; item < itemCount; item++) {
						final Number value;
						if (data instanceof IntervalCategoryDataset) {
							final IntervalCategoryDataset icd = (IntervalCategoryDataset) data;
							value = icd.getEndValue(series, item);
						} else {
							value = categoryData.getValue(series, item);
						}
						if (value != null) {
							maximum = Math.max(maximum, value.doubleValue());
						}
					}
				}
				if (maximum == Double.NEGATIVE_INFINITY) {
					return null;
				} else {
					return new Double(maximum);
				}

			} else
				if (data instanceof XYDataset) {

					final XYDataset xyData = (XYDataset) data;
					double maximum = Double.NEGATIVE_INFINITY;
					final int seriesCount = xyData.getSeriesCount();
					for (int series = 0; series < seriesCount; series++) {
						final int itemCount = xyData.getItemCount(series);
						for (int item = 0; item < itemCount; item++) {
							final Number value;
							if (data instanceof IntervalXYDataset) {
								final IntervalXYDataset intervalXYData = (IntervalXYDataset) data;
								value = intervalXYData.getEndYValue(series, item);
							} else
								if (data instanceof HighLowDataset) {
									final HighLowDataset highLowData = (HighLowDataset) data;
									value = highLowData.getHighValue(series, item);
								} else {
									value = xyData.getYValue(series, item);
								}
							if (value != null) {
								maximum = Math.max(maximum, value.doubleValue());
							}
						}
					}
					if (maximum == Double.NEGATIVE_INFINITY) {
						return null;
					} else {
						return new Double(maximum);
					}

				} else {
					return null;
				}

	}

	/**
	 * Creates a pie dataset from a table dataset by taking all the values
	 * for a single row.
	 * 
	 * @param data
	 *           the data.
	 * @param rowKey
	 *           the row key.
	 * @return a pie dataset.
	 */
	public static PieDataset createPieDatasetForRow(final CategoryDataset data,
																		final Comparable rowKey) {

		final int row = data.getRowIndex(rowKey);
		return createPieDatasetForRow(data, row);

	}

	/**
	 * Creates a pie dataset from a table dataset by taking all the values
	 * for a single row.
	 * 
	 * @param data
	 *           the data.
	 * @param row
	 *           the row (zero-based index).
	 * @return a pie dataset.
	 */
	public static PieDataset createPieDatasetForRow(final CategoryDataset data, final int row) {

		final DefaultPieDataset result = new DefaultPieDataset();
		final int columnCount = data.getColumnCount();
		for (int current = 0; current < columnCount; current++) {
			final Comparable columnKey = data.getColumnKey(current);
			result.setValue(columnKey, data.getValue(row, current));
		}
		return result;

	}

	/**
	 * Creates a pie dataset from a table dataset by taking all the values
	 * for a single column.
	 * 
	 * @param data
	 *           the data.
	 * @param columnKey
	 *           the column key.
	 * @return a pie dataset.
	 */
	public static PieDataset createPieDatasetForColumn(final CategoryDataset data,
																			final Comparable columnKey) {

		final int column = data.getColumnIndex(columnKey);
		return createPieDatasetForColumn(data, column);

	}

	/**
	 * Creates a pie dataset from a table dataset by taking all the values
	 * for a single column.
	 * 
	 * @param data
	 *           the data.
	 * @param column
	 *           the column (zero-based index).
	 * @return a pie dataset.
	 */
	public static PieDataset createPieDatasetForColumn(final CategoryDataset data,
																			final int column) {

		final DefaultPieDataset result = new DefaultPieDataset();
		final int rowCount = data.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			final Comparable rowKey = data.getRowKey(i);
			result.setValue(rowKey, data.getValue(i, column));
		}
		return result;

	}

	/**
	 * Calculates the total of all the values in a {@link PieDataset}. If the dataset contains
	 * negative or <code>null</code> values, they are ignored.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> not permitted).
	 * @return The total.
	 */
	public static double calculatePieDatasetTotal(final PieDataset dataset) {
		if (dataset == null) {
			throw new IllegalArgumentException("Null 'dataset' argument.");
		}
		final List keys = dataset.getKeys();
		double totalValue = 0;
		final Iterator iterator = keys.iterator();
		while (iterator.hasNext()) {
			final Comparable current = (Comparable) iterator.next();
			if (current != null) {
				final Number value = dataset.getValue(current);
				double v = 0.0;
				if (value != null) {
					v = value.doubleValue();
				}
				if (v > 0) {
					totalValue = totalValue + v;
				}
			}
		}
		return totalValue;
	}

	/**
	 * Returns the minimum and maximum values for the dataset's range (as in domain/range),
	 * assuming that the series in one category are stacked.
	 * 
	 * @param data
	 *           the dataset.
	 * @return the value range.
	 */
	public static Range getStackedRangeExtent(final CategoryDataset data) {

		Range result = null;
		if (data != null) {
			double minimum = 0.0;
			double maximum = 0.0;
			final int categoryCount = data.getColumnCount();
			for (int item = 0; item < categoryCount; item++) {
				double positive = 0.0;
				double negative = 0.0;
				final int seriesCount = data.getRowCount();
				for (int series = 0; series < seriesCount; series++) {
					final Number number = data.getValue(series, item);
					if (number != null) {
						final double value = number.doubleValue();
						if (value > 0.0) {
							positive = positive + value;
						}
						if (value < 0.0) {
							negative = negative + value; // '+', remember value is negative
						}
					}
				}
				minimum = Math.min(minimum, negative);
				maximum = Math.max(maximum, positive);
			}
			result = new Range(minimum, maximum);
		}
		return result;

	}

	/**
	 * Returns the minimum and maximum values for the dataset's range (as in domain/range),
	 * assuming that the series in one category are stacked.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @param map
	 *           a structure that maps series to groups.
	 * @return the value range.
	 */
	public static Range getStackedRangeExtent(final CategoryDataset dataset,
																final KeyToGroupMap map) {

		Range result = null;
		if (dataset != null) {

			// create an array holding the group indices...
			int[] groupIndex = new int[dataset.getRowCount()];
			for (int i = 0; i < dataset.getRowCount(); i++) {
				groupIndex[i] = map.getGroupIndex(map.getGroup(dataset.getRowKey(i)));
			}

			// minimum and maximum for each group...
			int groupCount = map.getGroupCount();
			double[] minimum = new double[groupCount];
			double[] maximum = new double[groupCount];

			final int categoryCount = dataset.getColumnCount();
			for (int item = 0; item < categoryCount; item++) {
				double[] positive = new double[groupCount];
				double[] negative = new double[groupCount];
				final int seriesCount = dataset.getRowCount();
				for (int series = 0; series < seriesCount; series++) {
					final Number number = dataset.getValue(series, item);
					if (number != null) {
						final double value = number.doubleValue();
						if (value > 0.0) {
							positive[groupIndex[series]] = positive[groupIndex[series]] + value;
						}
						if (value < 0.0) {
							negative[groupIndex[series]] = negative[groupIndex[series]] + value;
							// '+', remember value is negative
						}
					}
				}
				for (int g = 0; g < groupCount; g++) {
					minimum[g] = Math.min(minimum[g], negative[g]);
					maximum[g] = Math.max(maximum[g], positive[g]);
				}
			}
			for (int j = 0; j < groupCount; j++) {
				result = Range.combine(result, new Range(minimum[j], maximum[j]));
			}
		}
		return result;

	}

	/**
	 * Returns the minimum value in the dataset range, assuming that values in
	 * each category are "stacked".
	 * 
	 * @param data
	 *           the dataset.
	 * @return the minimum value.
	 */
	public static Number getMinimumStackedRangeValue(final CategoryDataset data) {

		Number result = null;

		if (data != null) {

			double minimum = 0.0;

			final int categoryCount = data.getRowCount();
			for (int item = 0; item < categoryCount; item++) {
				double total = 0.0;

				final int seriesCount = data.getColumnCount();
				for (int series = 0; series < seriesCount; series++) {
					final Number number = data.getValue(series, item);
					if (number != null) {
						final double value = number.doubleValue();
						if (value < 0.0) {
							total = total + value; // '+', remember value is negative
						}
					}
				}
				minimum = Math.min(minimum, total);

			}

			result = new Double(minimum);

		}

		return result;

	}

	/**
	 * Returns the maximum value in the dataset range, assuming that values in
	 * each category are "stacked".
	 * 
	 * @param data
	 *           the dataset (<code>null</code> permitted).
	 * @return The maximum value (possibly <code>null</code>).
	 */
	public static Number getMaximumStackedRangeValue(final CategoryDataset data) {

		Number result = null;

		if (data != null) {
			double maximum = 0.0;
			final int categoryCount = data.getColumnCount();
			for (int item = 0; item < categoryCount; item++) {
				double total = 0.0;
				final int seriesCount = data.getRowCount();
				for (int series = 0; series < seriesCount; series++) {
					final Number number = data.getValue(series, item);
					if (number != null) {
						final double value = number.doubleValue();
						if (value > 0.0) {
							total = total + value;
						}
					}
				}
				maximum = Math.max(maximum, total);
			}
			result = new Double(maximum);
		}

		return result;

	}

	/**
	 * Creates an {@link XYDataset} by sampling the specified function over a fixed range.
	 * 
	 * @param f
	 *           the function (<code>null</code> not permitted).
	 * @param start
	 *           the start value for the range.
	 * @param end
	 *           the end value for the range.
	 * @param samples
	 *           the number of sample points (must be > 1).
	 * @param seriesName
	 *           the name to give the resulting series (<code>null</code> not permitted).
	 * @return A dataset.
	 */
	public static XYDataset sampleFunction2D(final Function2D f,
															final double start,
															final double end,
															final int samples,
															final String seriesName) {

		if (f == null) {
			throw new IllegalArgumentException("Null 'f' argument.");
		}
		if (seriesName == null) {
			throw new IllegalArgumentException("Null 'seriesName' argument.");
		}
		if (start >= end) {
			throw new IllegalArgumentException("Requires 'start' < 'end'.");
		}
		if (samples < 2) {
			throw new IllegalArgumentException("Requires 'samples' > 1");
		}

		final XYSeries series = new XYSeries(seriesName);
		final double step = (end - start) / samples;
		for (int i = 0; i <= samples; i++) {
			final double x = start + (step * i);
			series.add(x, f.getValue(x));
		}
		final XYSeriesCollection collection = new XYSeriesCollection(series);
		return collection;

	}

	/**
	 * Creates a {@link CategoryDataset} that contains a copy of the data in an array
	 * (instances of <code>Double</code> are created to represent the data items).
	 * <p>
	 * Row and column keys are created by appending 0, 1, 2, ... to the supplied prefixes.
	 * 
	 * @param rowKeyPrefix
	 *           the row key prefix.
	 * @param columnKeyPrefix
	 *           the column key prefix.
	 * @param data
	 *           the data.
	 * @return the dataset.
	 */
	public static CategoryDataset createCategoryDataset(final String rowKeyPrefix,
																			final String columnKeyPrefix,
																			final double[][] data) {

		final DefaultCategoryDataset result = new DefaultCategoryDataset();
		for (int r = 0; r < data.length; r++) {
			final String rowKey = rowKeyPrefix + (r + 1);
			for (int c = 0; c < data[r].length; c++) {
				final String columnKey = columnKeyPrefix + (c + 1);
				result.addValue(new Double(data[r][c]), rowKey, columnKey);
			}
		}
		return result;

	}

	/**
	 * Creates a {@link CategoryDataset} that contains a copy of the data in an array.
	 * <p>
	 * Row and column keys are created by appending 0, 1, 2, ... to the supplied prefixes.
	 * 
	 * @param rowKeyPrefix
	 *           the row key prefix.
	 * @param columnKeyPrefix
	 *           the column key prefix.
	 * @param data
	 *           the data.
	 * @return the dataset.
	 */
	public static CategoryDataset createCategoryDataset(final String rowKeyPrefix,
																			final String columnKeyPrefix,
																			final Number[][] data) {

		final DefaultCategoryDataset result = new DefaultCategoryDataset();
		for (int r = 0; r < data.length; r++) {
			final String rowKey = rowKeyPrefix + (r + 1);
			for (int c = 0; c < data[r].length; c++) {
				final String columnKey = columnKeyPrefix + (c + 1);
				result.addValue(data[r][c], rowKey, columnKey);
			}
		}
		return result;

	}

	/**
	 * Creates a {@link CategoryDataset} that contains a copy of the data in an array
	 * (instances of <code>Double</code> are created to represent the data items).
	 * <p>
	 * Row and column keys are taken from the supplied arrays.
	 * 
	 * @param rowKeys
	 *           the row keys.
	 * @param columnKeys
	 *           the column keys.
	 * @param data
	 *           the data.
	 * @return The dataset.
	 */
	public static CategoryDataset createCategoryDataset(final String[] rowKeys,
																			final String[] columnKeys,
																			final double[][] data) {

		// check arguments...
		if (rowKeys == null) {
			throw new IllegalArgumentException("Argument 'rowKeys' cannot be null.");
		}
		if (columnKeys == null) {
			throw new IllegalArgumentException("Argument 'columnKeys' cannot be null.");
		}
		if (rowKeys.length != data.length) {
			throw new IllegalArgumentException(
								"The number of row keys does not match the number of rows in the data array.");
		}
		int columnCount = 0;
		for (int r = 0; r < data.length; r++) {
			columnCount = Math.max(columnCount, data[r].length);
		}
		if (columnKeys.length != columnCount) {
			throw new IllegalArgumentException(
								"The number of column keys does not match the number of columns in the data array.");
		}

		// now do the work...
		final DefaultCategoryDataset result = new DefaultCategoryDataset();
		for (int r = 0; r < data.length; r++) {
			final String rowKey = rowKeys[r];
			for (int c = 0; c < data[r].length; c++) {
				final String columnKey = columnKeys[c];
				result.addValue(new Double(data[r][c]), rowKey, columnKey);
			}
		}
		return result;

	}

	/**
	 * Creates a {@link CategoryDataset} by copying the data from the supplied {@link KeyedValues} instance.
	 * 
	 * @param rowKey
	 *           the row key.
	 * @param rowData
	 *           the row data.
	 * @return A dataset.
	 */
	public static CategoryDataset createCategoryDataset(final String rowKey,
																			final KeyedValues rowData) {

		final DefaultCategoryDataset result = new DefaultCategoryDataset();
		for (int i = 0; i < rowData.getItemCount(); i++) {
			result.addValue(rowData.getValue(i), rowKey, rowData.getKey(i));
		}
		return result;

	}

	/**
	 * Returns <code>true</code> if the dataset is empty (or <code>null</code>), and <code>false</code> otherwise.
	 * 
	 * @param data
	 *           the dataset (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public static boolean isEmptyOrNull(final XYDataset data) {

		boolean result = true;

		if (data != null) {
			for (int s = 0; s < data.getSeriesCount(); s++) {
				if (data.getItemCount(s) > 0) {
					result = false;
					continue;
				}
			}
		}

		return result;

	}

	/**
	 * Returns <code>true</code> if the dataset is empty (or <code>null</code>), and <code>false</code> otherwise.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 * @return a boolean.
	 */
	public static boolean isEmptyOrNull(final PieDataset dataset) {

		if (dataset == null) {
			return true;
		}

		final int itemCount = dataset.getItemCount();
		if (itemCount == 0) {
			return true;
		}

		for (int item = 0; item < itemCount; item++) {
			final Number y = dataset.getValue(item);
			if (y != null) {
				final double yy = y.doubleValue();
				if (yy > 0.0) {
					return false;
				}
			}
		}

		return true;

	}

	/**
	 * Returns <code>true</code> if the dataset is empty (or <code>null</code>), and <code>false</code> otherwise.
	 * 
	 * @param data
	 *           the dataset (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public static boolean isEmptyOrNull(final CategoryDataset data) {

		if (data == null) {
			return true;
		}

		final int rowCount = data.getRowCount();
		final int columnCount = data.getColumnCount();
		if (rowCount == 0 || columnCount == 0) {
			return true;
		}

		for (int r = 0; r < rowCount; r++) {
			for (int c = 0; c < columnCount; c++) {
				if (data.getValue(r, c) != null) {
					return false;
				}

			}
		}

		return true;

	}

	/**
	 * Creates an "Other" slice for percentages below the percent threshold.
	 * 
	 * @param dataset
	 *           the PieDataset.
	 * @param percentThreshold
	 *           the percent threshold.
	 * @return A PieDataset.
	 */
	public static PieDataset limitPieDataset(final PieDataset dataset,
															final double percentThreshold) {
		return DatasetUtilities.limitPieDataset(dataset, percentThreshold, 2, "Other");
	}

	/**
	 * Create an "Other" slice for percentages below the percent threshold providing there
	 * are more slices below the percent threshold than specified in the slice threshold.
	 * 
	 * @param dataset
	 *           the source dataset.
	 * @param percentThreshold
	 *           the percent threshold (ten percent is 0.10).
	 * @param minItems
	 *           only aggregate low values if there are at least this many.
	 * @return A PieDataset.
	 */
	public static PieDataset limitPieDataset(final PieDataset dataset,
															final double percentThreshold,
															final int minItems) {
		return DatasetUtilities.limitPieDataset(dataset, percentThreshold, minItems, "Other");
	}

	/**
	 * Creates a new pie dataset based on the supplied dataset, but modified by aggregating all
	 * the low value items (those whose value is lower than the percentThreshold) into a single
	 * item. The aggregated items are assigned the specified key. Aggregation only occurs if
	 * there are at least minItems items to aggregate.
	 * 
	 * @param dataset
	 *           the source dataset.
	 * @param percentThreshold
	 *           the percent threshold (ten percent is 0.10).
	 * @param minItems
	 *           only aggregate low values if there are at least this many.
	 * @param key
	 *           the key to represent the aggregated items.
	 * @return The pie dataset with (possibly) aggregated items.
	 */
	public static PieDataset limitPieDataset(final PieDataset dataset,
															final double percentThreshold,
															final int minItems,
															final Comparable key) {

		final DefaultPieDataset result = new DefaultPieDataset();
		final double total = DatasetUtilities.calculatePieDatasetTotal(dataset);

		// Iterate and find all keys below threshold percentThreshold
		final List keys = dataset.getKeys();
		final ArrayList otherKeys = new ArrayList();
		Iterator iterator = keys.iterator();
		while (iterator.hasNext()) {
			final Comparable currentKey = (Comparable) iterator.next();
			final Number dataValue = dataset.getValue(currentKey);
			if (dataValue != null) {
				final double value = dataValue.doubleValue();
				if (value / total < percentThreshold) {
					otherKeys.add(currentKey);
				}
			}
		}

		// Create new dataset with keys above threshold percentThreshold
		iterator = keys.iterator();
		double otherValue = 0;
		while (iterator.hasNext()) {
			final Comparable currentKey = (Comparable) iterator.next();
			final Number dataValue = dataset.getValue(currentKey);
			if (dataValue != null) {
				if (otherKeys.contains(currentKey) && otherKeys.size() >= minItems) {
					// Do not add key to dataset
					otherValue += dataValue.doubleValue();
				} else {
					// Add key to dataset
					result.setValue(currentKey, dataValue);
				}
			}
		}
		// Add other category if applicable
		if (otherKeys.size() >= minItems) {
			result.setValue(key, otherValue);
		}
		return result;
	}

	/**
	 * Returns the minimum and maximum values for the dataset's range,
	 * assuming that the series are stacked.
	 * 
	 * @param data
	 *           the dataset.
	 * @return the value range.
	 */
	public static Range getStackedRangeExtent(final TableXYDataset data) {
		// check parameters...
		if (data == null) {
			return null;
		}
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = Double.NEGATIVE_INFINITY;
		for (int itemNo = 0; itemNo < data.getItemCount(); itemNo++) {
			double value = 0;
			for (int seriesNo = 0; seriesNo < data.getSeriesCount(); seriesNo++) {
				if (data.getYValue(seriesNo, itemNo) != null) {
					value += (data.getYValue(seriesNo, itemNo).doubleValue());
				}
			}
			if (value > maximum) {
				maximum = value;
			}
			if (value < minimum) {
				minimum = value;
			}
		}
		if (minimum == Double.POSITIVE_INFINITY) {
			return null;
		} else {
			return new Range(minimum, maximum);
		}
	}

	/**
	 * Calculates the range of values for a dataset where each item is the running total of
	 * the items for the current series.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return The range.
	 */
	public static Range getCumulativeRangeExtent(final CategoryDataset dataset) {

		if (dataset == null) {
			return null;
		}

		boolean allItemsNull = true; // we'll set this to false if there is at least one
		// non-null data item...
		double minimum = 0.0;
		double maximum = 0.0;
		for (int row = 0; row < dataset.getRowCount(); row++) {
			double runningTotal = 0.0;
			for (int column = 0; column < dataset.getColumnCount() - 1; column++) {
				final Number n = dataset.getValue(row, column);
				if (n != null) {
					allItemsNull = false;
					final double value = n.doubleValue();
					runningTotal = runningTotal + value;
					minimum = Math.min(minimum, runningTotal);
					maximum = Math.max(maximum, runningTotal);
				}
			}
		}
		if (!allItemsNull) {
			return new Range(minimum, maximum);
		} else {
			return null;
		}

	}
}
