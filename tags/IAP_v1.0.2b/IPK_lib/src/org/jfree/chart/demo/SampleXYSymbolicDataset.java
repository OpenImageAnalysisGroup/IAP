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
 * SampleXYSymbolicDataset.java
 * ----------------------------
 * (C) Copyright 2000-2004, by Anthony Boulestreau and Contributors;
 * Original Author: Anthony Boulestreau.
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Changes
 * -------
 * 29-Mar-2002 : Version 1 (AB);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Vector;

import org.jfree.data.AbstractSeriesDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.XisSymbolic;
import org.jfree.data.YisSymbolic;

/**
 * Random data for a symbolic plot demo.
 * 
 * @author Anthony Boulestreau
 */
public class SampleXYSymbolicDataset extends AbstractSeriesDataset
													implements XYDataset, XisSymbolic, YisSymbolic {

	/** Series names. */
	private String[] seriesName;

	/** Items. */
	private int[] item;

	/** A series index. */
	private int serie;

	/** X values. */
	private Integer[][] xValues;

	/** Y values. */
	private Integer[][] yValues;

	/** X symbolic values. */
	private String[] xSymbolicValues;

	/** Y symbolic values. */
	private String[] ySymbolicValues;

	/** The dataset name. */
	private String datasetName;

	/**
	 * Creates a new dataset.
	 * 
	 * @param datasetName
	 *           the dataset name.
	 * @param xValues
	 *           the x values.
	 * @param yValues
	 *           the y values.
	 * @param xSymbolicValues
	 *           the x symbols.
	 * @param ySymbolicValues
	 *           the y symbols.
	 * @param seriesName
	 *           the series name.
	 */
	public SampleXYSymbolicDataset(final String datasetName,
												final Integer[][] xValues,
												final Integer[][] yValues,
												final String[] xSymbolicValues,
												final String[] ySymbolicValues,
												final String[] seriesName) {

		this.datasetName = datasetName;
		this.xValues = xValues;
		this.yValues = yValues;
		this.xSymbolicValues = xSymbolicValues;
		this.ySymbolicValues = ySymbolicValues;
		this.serie = xValues.length;
		this.item = new int[this.serie];
		for (int i = 0; i < this.serie; i++) {
			this.item[i] = xValues[i].length;
		}
		this.seriesName = seriesName;

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
	 * Sets the x-value for the specified series and item with the specified
	 * new <CODE>Number</CODE> value. Series are numbered 0, 1, ...
	 * <P>
	 * This method is used by combineXSymbolicDataset to modify the reference to the symbolic value ...
	 * 
	 * @param series
	 *           the index (zero-based) of the series.
	 * @param item
	 *           the index (zero-based) of the required item.
	 * @param newValue
	 *           the value to set.
	 */
	public void setXValue(final int series, final int item, final Number newValue) {
		this.xValues[series][item] = (Integer) newValue;
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
		if (this.seriesName != null) {
			return this.seriesName[series];
		} else {
			return this.datasetName + series;
		}
	}

	/**
	 * Returns the number of items in the specified series.
	 * 
	 * @param series
	 *           the index (zero-based) of the series.
	 * @return the number of items in the specified series.
	 */
	public int getItemCount(final int series) {
		return this.item[series];
	}

	/**
	 * Returns the list of X symbolic values.
	 * 
	 * @return array of symbolic value.
	 */
	public String[] getXSymbolicValues() {
		return this.xSymbolicValues;
	}

	/**
	 * Returns the list of Y symbolic values.
	 * 
	 * @return array of symbolic value.
	 */
	public String[] getYSymbolicValues() {
		return this.ySymbolicValues;
	}

	/**
	 * Sets the list of X symbolic values.
	 * 
	 * @param sValues
	 *           the new list of symbolic value.
	 */
	public void setXSymbolicValues(final String[] sValues) {
		this.xSymbolicValues = sValues;
	}

	/**
	 * Sets the list of Y symbolic values.
	 * 
	 * @param sValues
	 *           the new list of symbolic value.
	 */
	public void setYSymbolicValues(final String[] sValues) {
		this.ySymbolicValues = sValues;
	}

	/**
	 * Returns the X symbolic value of the data set specified by <CODE>series</CODE> and <CODE>item</CODE> parameters.
	 * 
	 * @param series
	 *           value of the serie.
	 * @param item
	 *           value of the item.
	 * @return the symbolic value.
	 */
	public String getXSymbolicValue(final int series, final int item) {
		final Integer intValue = (Integer) getXValue(series, item);
		return getXSymbolicValue(intValue);
	}

	/**
	 * Returns the Y symbolic value of the data set specified by <CODE>series</CODE> and <CODE>item</CODE> parameters.
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
	 * Returns the X symbolic value linked with the specified <CODE>Integer</CODE>.
	 * 
	 * @param val
	 *           value of the integer linked with the symbolic value.
	 * @return the symbolic value.
	 */
	public String getXSymbolicValue(final Integer val) {
		return this.xSymbolicValues[val.intValue()];
	}

	/**
	 * Returns the Y symbolic value linked with the specified <CODE>Integer</CODE>.
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
	 * order that they share the same Y symbolic value list.
	 * <P>
	 * The sharing Y symbolic value list is obtained adding the Y symbolic data list of the fist data set to the Y symbolic data list of the second data set.
	 * <P>
	 * This function is use with the <I>combined plot</I> functions of JFreeChart.
	 * 
	 * @param dataset1
	 *           the first data set to combine.
	 * @param dataset2
	 *           the second data set to combine.
	 * @return the shared Y symbolic array.
	 */
	public static String[] combineYSymbolicDataset(final YisSymbolic dataset1,
																	final YisSymbolic dataset2) {

		final SampleXYSymbolicDataset sDataset1 = (SampleXYSymbolicDataset) dataset1;
		final SampleXYSymbolicDataset sDataset2 = (SampleXYSymbolicDataset) dataset2;
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
	 * This function modify <CODE>dataset1</CODE> and <CODE>dataset1</CODE> in
	 * order that they share the same X symbolic value list.
	 * <P>
	 * The sharing X symbolic value list is obtained adding the X symbolic data list of the fist data set to the X symbolic data list of the second data set.
	 * <P>
	 * This function is use with the <I>combined plot</I> functions of JFreeChart.
	 * 
	 * @param dataset1
	 *           the first data set to combine.
	 * @param dataset2
	 *           the second data set to combine.
	 * @return the shared X symbolic array.
	 */
	public static String[] combineXSymbolicDataset(final XisSymbolic dataset1,
																	final XisSymbolic dataset2) {
		final SampleXYSymbolicDataset sDataset1 = (SampleXYSymbolicDataset) dataset1;
		final SampleXYSymbolicDataset sDataset2 = (SampleXYSymbolicDataset) dataset2;
		final String[] sDatasetSymbolicValues1 = sDataset1.getXSymbolicValues();
		final String[] sDatasetSymbolicValues2 = sDataset2.getXSymbolicValues();

		// Combine the two list of symbolic value of the two data set
		final int s1length = sDatasetSymbolicValues1.length;
		final int s2length = sDatasetSymbolicValues2.length;
		final List xSymbolicValuesCombined = new Vector();
		for (int i = 0; i < s1length; i++) {
			xSymbolicValuesCombined.add(sDatasetSymbolicValues1[i]);
		}
		for (int i = 0; i < s2length; i++) {
			if (!xSymbolicValuesCombined.contains(sDatasetSymbolicValues2[i])) {
				xSymbolicValuesCombined.add(sDatasetSymbolicValues2[i]);
			}
		}

		// Change the Integer reference of the second data set
		int newIndex;
		for (int i = 0; i < sDataset2.getSeriesCount(); i++) {
			for (int j = 0; j < sDataset2.getItemCount(i); j++) {
				newIndex = xSymbolicValuesCombined.indexOf(sDataset2.getXSymbolicValue(i, j));
				sDataset2.setXValue(i, j, new Integer(newIndex));
			}
		}

		// Set the new list of symbolic value on the two data sets
		final String[] xSymbolicValuesCombinedA = new String[xSymbolicValuesCombined.size()];
		xSymbolicValuesCombined.toArray(xSymbolicValuesCombinedA);
		sDataset1.setXSymbolicValues(xSymbolicValuesCombinedA);
		sDataset2.setXSymbolicValues(xSymbolicValuesCombinedA);

		return xSymbolicValuesCombinedA;
	}

	/**
	 * Clone the SampleXYSymbolicDataset object
	 * 
	 * @return the cloned object.
	 */
	public Object clone() {
		final String nDatasetName = new String(this.datasetName);
		final Integer[][] nXValues = (Integer[][]) cloneArray(this.xValues);
		final Integer[][] nYValues = (Integer[][]) cloneArray(this.yValues);
		final String[] nXSymbolicValues = (String[]) cloneArray(this.xSymbolicValues);
		final String[] nYSymbolicValues = (String[]) cloneArray(this.ySymbolicValues);
		final String[] sName = (String[]) cloneArray(this.seriesName);
		return new SampleXYSymbolicDataset(
							nDatasetName, nXValues, nYValues, nXSymbolicValues, nYSymbolicValues, sName);
	}

	/**
	 * Returns a clone of the array.
	 * 
	 * @param arr
	 *           the array.
	 * @return a clone.
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
