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
 * SampleYSymbolicDataset.java
 * ---------------------------
 * Original Author: Anthony Boulestreau.
 * Contributor(s): -;
 * Changes
 * -------
 * 29-Mar-2002 : Version 1 (AB);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 05-May-2004 : Added getX() and getY() methods (DG);
 */

package org.jfree.chart.demo;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Vector;

import org.jfree.data.AbstractSeriesDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.YisSymbolic;

/**
 * A dataset implementation that provides random data for a symbolic plot demo. This sample
 * dataset has numerical x-values, and symbolic y-values.
 * 
 * @author Anthony Boulestreau
 */
public class SampleYSymbolicDataset extends AbstractSeriesDataset
												implements XYDataset, YisSymbolic {

	/** The series count. */
	private static final int DEFAULT_SERIES_COUNT = 1;

	/** The item count. */
	private static final int DEFAULT_ITEM_COUNT = 50;

	/** The series index. */
	private int serie;

	/** The item index. */
	private int item;

	/** The series names. */
	private String[] serieNames;

	/** The x values. */
	private Double[][] xValues;

	/** The y values. */
	private Integer[][] yValues;

	/** The y symbolic values. */
	private String[] ySymbolicValues;

	/** The dataset name. */
	private String datasetName;

	/**
	 * Creates a new sample dataset.
	 * 
	 * @param datasetName
	 *           the dataset name.
	 * @param xRange
	 *           the upper limit of the (random) x-values.
	 * @param tabString
	 *           the symbols for the y-values.
	 */
	public SampleYSymbolicDataset(final String datasetName,
												final int xRange,
												final String[] tabString) {
		this(datasetName, xRange, tabString, DEFAULT_SERIES_COUNT, DEFAULT_ITEM_COUNT, null);
	}

	/**
	 * Creates a new sample dataset.
	 * 
	 * @param datasetName
	 *           the dataset name.
	 * @param xRange
	 *           the upper limit of the (random) x-values.
	 * @param tabString
	 *           the symbolic y-values.
	 * @param seriesCount
	 *           the number of series to create.
	 * @param itemCount
	 *           the number of items to create for each series.
	 * @param serieNames
	 *           the series names.
	 */
	public SampleYSymbolicDataset(final String datasetName,
												final int xRange,
												final String[] tabString,
												final int seriesCount,
												final int itemCount,
												final String[] serieNames) {

		this.datasetName = datasetName;
		this.ySymbolicValues = tabString;
		this.serie = seriesCount;
		this.item = itemCount;
		this.serieNames = serieNames;
		this.xValues = new Double[seriesCount][itemCount];
		this.yValues = new Integer[seriesCount][itemCount];

		for (int s = 0; s < seriesCount; s++) {
			for (int i = 0; i < itemCount; i++) {
				final double x = Math.random() * xRange;
				final double y = Math.random() * tabString.length;
				this.xValues[s][i] = new Double(x);
				this.yValues[s][i] = new Integer((int) y);
			}
		}
	}

	/**
	 * Creates a new sample dataset.
	 * 
	 * @param datasetName
	 *           the dataset name.
	 * @param xValues
	 *           the x values.
	 * @param yValues
	 *           the y values.
	 * @param ySymbolicValues
	 *           the y symbols
	 * @param seriesCount
	 *           the series count.
	 * @param itemCount
	 *           the item count.
	 * @param serieNames
	 *           the series names.
	 */
	public SampleYSymbolicDataset(final String datasetName,
												final Double[][] xValues,
												final Integer[][] yValues,
												final String[] ySymbolicValues,
												final int seriesCount,
												final int itemCount,
												final String[] serieNames) {

		this.datasetName = datasetName;
		this.xValues = xValues;
		this.yValues = yValues;
		this.ySymbolicValues = ySymbolicValues;
		this.serie = seriesCount;
		this.item = itemCount;
		this.serieNames = serieNames;

	}

	/**
	 * Returns the x-value for the specified series and item. Series are
	 * numbered 0, 1, ...
	 * 
	 * @param series
	 *           the index (zero-based) of the series.
	 * @param item
	 *           the index (zero-based) of the required item.
	 * @return the x-value for the specified series and item.
	 */
	public Number getXValue(final int series, final int item) {
		return this.xValues[series][item];
	}

	/**
	 * Returns the x-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The x-value.
	 */
	public double getX(int series, int item) {
		double result = Double.NaN;
		Number x = getXValue(series, item);
		if (x != null) {
			result = x.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the y-value for the specified series and item. Series are
	 * numbered 0, 1, ...
	 * 
	 * @param series
	 *           the index (zero-based) of the series.
	 * @param item
	 *           the index (zero-based) of the required item.
	 * @return the y-value for the specified series and item.
	 */
	public Number getYValue(final int series, final int item) {
		return this.yValues[series][item];
	}

	/**
	 * Returns the y-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The y-value.
	 */
	public double getY(int series, int item) {
		double result = Double.NaN;
		Number y = getYValue(series, item);
		if (y != null) {
			result = y.doubleValue();
		}
		return result;
	}

	/**
	 * Sets the y-value for the specified series and item with the specified
	 * new <CODE>Number</CODE> value. Series are numbered 0, 1, ...
	 * <P>
	 * This method is used by combineYSymbolicDataset to modify the reference to the symbolic value ...
	 * 
	 * @param series
	 *           the index (zero-based) of the series.
	 * @param item
	 *           the index (zero-based) of the required item.
	 * @param newValue
	 *           the value to set.
	 */
	public void setYValue(final int series, final int item, final Number newValue) {
		this.yValues[series][item] = (Integer) newValue;
	}

	/**
	 * Returns the number of series in the dataset.
	 * 
	 * @return The number of series in the dataset.
	 */
	public int getSeriesCount() {
		return this.serie;
	}

	/**
	 * Returns the name of the series.
	 * 
	 * @param series
	 *           the index (zero-based) of the series.
	 * @return the name of the series.
	 */
	public String getSeriesName(final int series) {
		if (this.serieNames != null) {
			return this.serieNames[series];
		} else {
			return this.datasetName + series;
		}
	}

	/**
	 * Returns the number of items in the specified series.
	 * 
	 * @param series
	 *           The index (zero-based) of the series.
	 * @return the number of items in the specified series.
	 */
	public int getItemCount(final int series) {
		return this.item;
	}

	/**
	 * Returns the list of symbolic values.
	 * 
	 * @return array of symbolic value.
	 */
	public String[] getYSymbolicValues() {
		return this.ySymbolicValues;
	}

	/**
	 * Sets the list of symbolic values.
	 * 
	 * @param sValues
	 *           the new list of symbolic value.
	 */
	public void setYSymbolicValues(final String[] sValues) {
		this.ySymbolicValues = sValues;
	}

	/**
	 * Returns the symbolic value of the data set specified by <CODE>series</CODE> and <CODE>item</CODE> parameters.
	 * 
	 * @param series
	 *           value of the serie.
	 * @param item
	 *           value of the item.
	 * @return the symbolic value.
	 */
	public String getYSymbolicValue(final int series, final int item) {
		final Integer intValue = (Integer) getYValue(series, item);
		return getYSymbolicValue(intValue);
	}

	/**
	 * Returns the symbolic value linked with the specified <CODE>Integer</CODE>.
	 * 
	 * @param val
	 *           value of the integer linked with the symbolic value.
	 * @return the symbolic value.
	 */
	public String getYSymbolicValue(final Integer val) {
		return this.ySymbolicValues[val.intValue()];
	}

	/**
	 * This function modify <CODE>dataset1</CODE> and <CODE>dataset1</CODE> in
	 * order that they share the same symbolic value list.
	 * <P>
	 * The sharing symbolic value list is obtained adding the symbolic data list of the fist data set to the symbolic data list of the second data set.
	 * <P>
	 * This function is use with the <I>combined plot</I> functions of JFreeChart.
	 * 
	 * @param dataset1
	 *           the first data set to combine.
	 * @param dataset2
	 *           the second data set to combine.
	 * @return the shared symbolic array.
	 */
	public static String[] combineYSymbolicDataset(final YisSymbolic dataset1,
																	final YisSymbolic dataset2) {

		final SampleYSymbolicDataset sDataset1 = (SampleYSymbolicDataset) dataset1;
		final SampleYSymbolicDataset sDataset2 = (SampleYSymbolicDataset) dataset2;
		final String[] sDatasetSymbolicValues1 = sDataset1.getYSymbolicValues();
		final String[] sDatasetSymbolicValues2 = sDataset2.getYSymbolicValues();

		// Combine the two list of symbolic value of the two data set
		final int s1length = sDatasetSymbolicValues1.length;
		final int s2length = sDatasetSymbolicValues2.length;
		final List ySymbolicValuesCombined = new Vector();
		for (int i = 0; i < s1length; i++) {
			ySymbolicValuesCombined.add(sDatasetSymbolicValues1[i]);
		}
		for (int i = 0; i < s2length; i++) {
			if (!ySymbolicValuesCombined.contains(sDatasetSymbolicValues2[i])) {
				ySymbolicValuesCombined.add(sDatasetSymbolicValues2[i]);
			}
		}

		// Change the Integer reference of the second data set
		int newIndex;
		for (int i = 0; i < sDataset2.getSeriesCount(); i++) {
			for (int j = 0; j < sDataset2.getItemCount(i); j++) {
				newIndex = ySymbolicValuesCombined.indexOf(sDataset2.getYSymbolicValue(i, j));
				sDataset2.setYValue(i, j, new Integer(newIndex));
			}
		}

		// Set the new list of symbolic value on the two data sets
		final String[] ySymbolicValuesCombinedA = new String[ySymbolicValuesCombined.size()];
		ySymbolicValuesCombined.toArray(ySymbolicValuesCombinedA);
		sDataset1.setYSymbolicValues(ySymbolicValuesCombinedA);
		sDataset2.setYSymbolicValues(ySymbolicValuesCombinedA);

		return ySymbolicValuesCombinedA;
	}

	/**
	 * Clone the SampleYSymbolicDataset object
	 * 
	 * @return the cloned object.
	 */
	public Object clone() {
		final String nDatasetName = new String(this.datasetName);
		final Double[][] nXValues = (Double[][]) cloneArray(this.xValues);
		final Integer[][] nYValues = (Integer[][]) cloneArray(this.yValues);
		final String[] nYSymbolicValues = (String[]) cloneArray(this.ySymbolicValues);
		final int serie = this.serie;
		final int item = this.item;
		final String[] serieNames = (String[]) cloneArray(this.serieNames);
		return new SampleYSymbolicDataset(nDatasetName, nXValues, nYValues,
							nYSymbolicValues, serie, item, serieNames);
	}

	/**
	 * Clones the array.
	 * 
	 * @param arr
	 *           the array.
	 * @return an array.
	 */
	private static Object cloneArray(final Object arr) {

		if (arr == null) {
			return arr;
		}

		final Class cls = arr.getClass();
		if (!cls.isArray()) {
			return arr;
		}

		final int length = Array.getLength(arr);
		final Object[] newarr = (Object[]) Array.newInstance(cls.getComponentType(), length);

		Object obj;

		for (int i = 0; i < length; i++) {
			obj = Array.get(arr, i);
			if (obj.getClass().isArray()) {
				newarr[i] = cloneArray(obj);
			} else {
				newarr[i] = obj;
			}
		}

		return newarr;
	}

}
