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
 * -----------------------
 * SymbolicXYPlotDemo.java
 * -----------------------
 * (C) Copyright 2002-2004, by Anthony Boulestreau and Contributors.
 * Original Author: Anthony Boulestreau;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Changes
 * -------
 * 29-Mar-2002 : Version 1 (AB);
 * 09-Apr-2002 : Minor changes reflecting the API change for XYItemRenderer (DG);
 * 23-Apr-2002 : Updated to reflect changes in the combined plot classes (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.GradientPaint;
import java.lang.reflect.Array;

import javax.swing.JFrame;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolicAxis;
import org.jfree.chart.labels.SymbolicXYItemLabelGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.CombinedDataset;
import org.jfree.data.SubSeriesDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.XisSymbolic;
import org.jfree.data.YisSymbolic;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration application for the symbolic XY plots.
 * 
 * @author Anthony Boulestreau
 */
public class SymbolicXYPlotDemo {

	/**
	 * Creates a XY graph with symbolic value on X and Y axis.
	 * 
	 * @param title
	 *           the title.
	 * @param xAxisLabel
	 *           the x axis label.
	 * @param yAxisLabel
	 *           the y axis label.
	 * @param data
	 *           the dataset.
	 * @param legend
	 *           create a legend?
	 * @return a chart.
	 */
	public static JFreeChart createXYSymbolicPlot(final String title, final String xAxisLabel,
																	final String yAxisLabel, final XYDataset data,
																	final boolean legend) {

		final SymbolicAxis xSymbolicAxis = new SymbolicAxis(xAxisLabel, ((XisSymbolic) data).getXSymbolicValues());

		final SymbolicAxis ySymbolicAxis = new SymbolicAxis(yAxisLabel, ((YisSymbolic) data).getYSymbolicValues());

		final XYPlot plot = new XYPlot(data, xSymbolicAxis, ySymbolicAxis, null);
		final XYItemRenderer renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, new SymbolicXYItemLabelGenerator()
							);
		plot.setRenderer(renderer);
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
		return chart;

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a dataset.
	 */
	public static SampleYSymbolicDataset createYSymbolicSample1() {

		final String[] sData = { "Lion", "Elephant", "Monkey", "Hippopotamus", "Giraffe" };
		final SampleYSymbolicDataset data = new SampleYSymbolicDataset(
							"AY Sample", 20, sData, 4, 20, new String[] { "Fall", "Spring", "Summer", "Winter" }
							);
		return data;

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a dataset.
	 */
	public static SampleYSymbolicDataset createYSymbolicSample2() {

		final String[] sData = { "Giraffe", "Gazelle", "Zebra", "Gnu" };
		final SampleYSymbolicDataset data = new SampleYSymbolicDataset(
							"BY Sample", 40, sData, 4, 10, new String[] { "Fall", "Spring", "Summer", "Winter" }
							);
		return data;

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a dataset.
	 */
	public static SampleXYSymbolicDataset createXYSymbolicSample1() {

		final String[] xsData = { "Atmosphere", "Continental Ecosystem",
									"Limnic Ecosystem", "Marine Ecosystem" };
		final String[] ysData = { "Ionizing radiations", "Thermic pollutants", "Hydrocarbon",
									"Synthetic materials", "Pesticides", "Detergent",
									"Synthetic organic materials", "Sulphur", "Nitrate", "Phosphate",
									"Heavy metals", "Fluors", "Aerosols", "Dead organic materials",
									"Pathogen micro-organisms" };

		final int[][] xd = {
				{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2,
									2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 }
			};
		final int[][] yd = {
				{ 0, 2, 3, 7, 10, 11, 12, 14, 0, 2, 3, 4, 7, 8, 9, 10, 11, 12, 14, 0, 1, 2, 3,
									4, 5, 7, 8, 9, 10, 13, 14, 0, 1, 2, 3, 4, 5, 8, 9, 10, 13, 14 }
			};
		final Integer[][] xData = (Integer[][]) toArray(xd);
		final Integer[][] yData = (Integer[][]) toArray(yd);
		final SampleXYSymbolicDataset xySymbolicData = new SampleXYSymbolicDataset(
							"AXY Sample", xData, yData, xsData, ysData, new String[] { "A" }
							);
		return xySymbolicData;

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a dataset.
	 */
	public static SampleXYSymbolicDataset createXYSymbolicSample2() {

		final String[] xsData = { "Physic pollutant", "Chemical pollutant", "Biological pollutant" };
		final String[] ysData = { "Ionizing radiations", "Thermic pollutants", "Hydrocarbon",
										"Synthetic materials", "Pesticides", "Detergent",
										"Synthetic organic materials", "Sulphur", "Nitrate", "Phosphate",
										"Heavy metals", "Fluors", "Aerosols", "Dead organic materials",
										"Pathogen micro-organisms" };

		final int[][] xd = { { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2 } };
		final int[][] yd = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 } };
		final Integer[][] xData = (Integer[][]) toArray(xd);
		final Integer[][] yData = (Integer[][]) toArray(yd);
		final SampleXYSymbolicDataset xySymbolicData = new SampleXYSymbolicDataset("BXY Sample",
							xData, yData, xsData, ysData, new String[] { "B" });
		return xySymbolicData;

	}

	/**
	 * Displays an XYPlot with symbolic axes.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 * @param data
	 *           the dataset.
	 * @param chartTitle
	 *           the chart title.
	 * @param xAxisLabel
	 *           the x axis label.
	 * @param yAxisLabel
	 *           the y axis label.
	 */
	private static void displayXYSymbolic(final String frameTitle,
														final XYDataset data, final String chartTitle,
														final String xAxisLabel, final String yAxisLabel) {

		final JFreeChart chart = createXYSymbolicPlot(
							chartTitle, xAxisLabel, yAxisLabel, data, true
							);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.green));
		final JFrame frame = new ChartFrame(frameTitle, chart);
		frame.pack();
		RefineryUtilities.positionFrameRandomly(frame);
		frame.show();

	}

	/**
	 * Displays an overlaid XYPlot with X and Y symbolic data.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 * @param data1
	 *           the dataset 1.
	 * @param data2
	 *           the dataset 2.
	 */
	private static void displayXYSymbolicOverlaid(final String frameTitle,
																	final XYDataset data1, final XYDataset data2) {

		final String title = "Pollutant Overlaid";
		final String xAxisLabel = "Contamination and Type";
		final String yAxisLabel = "Pollutant";

		// combine the x symbolic values of the two data sets
		final String[] combinedXSymbolicValues = SampleXYSymbolicDataset.combineXSymbolicDataset((XisSymbolic) data1,
																					(XisSymbolic) data2);

		// combine the y symbolic values of the two data sets
		final String[] combinedYSymbolicValues = SampleXYSymbolicDataset.combineYSymbolicDataset((YisSymbolic) data1,
																					(YisSymbolic) data2);

		// make master dataset...
		final CombinedDataset data = new CombinedDataset();
		data.add(data1);
		data.add(data2);

		// decompose data...
		final XYDataset series0 = new SubSeriesDataset(data, 0);
		final XYDataset series1 = new SubSeriesDataset(data, 1);

		// create overlaid plot...
		final SymbolicAxis hsymbolicAxis = new SymbolicAxis(xAxisLabel, combinedXSymbolicValues);
		final SymbolicAxis vsymbolicAxis = new SymbolicAxis(yAxisLabel, combinedYSymbolicValues);

		final XYItemRenderer renderer1 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot plot = new XYPlot(series0, hsymbolicAxis, vsymbolicAxis, renderer1);

		final XYItemRenderer renderer2 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		plot.setDataset(1, series1);
		plot.setRenderer(1, renderer2);

		// make the chart...
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		// and present it in a frame...
		final JFrame frame = new ChartFrame(frameTitle, chart);
		frame.pack();
		RefineryUtilities.positionFrameRandomly(frame);
		frame.show();

	}

	/**
	 * Displays an horizontally combined XYPlot with X and Y symbolic data.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 * @param data1
	 *           the dataset 1.
	 * @param data2
	 *           the dataset 2.
	 */
	private static void displayXYSymbolicCombinedHorizontally(final String frameTitle,
																					final XYDataset data1,
																					final XYDataset data2) {

		final String title = "Pollutant Horizontally Combined";
		final String x1AxisLabel = "Contamination";
		final String x2AxisLabel = "Type";
		final String yAxisLabel = "Pollutant";

		// combine the y symbolic values of the two data sets
		final String[] combinedYSymbolicValues =
							SampleXYSymbolicDataset.combineYSymbolicDataset((YisSymbolic) data1,
																				(YisSymbolic) data2);

		// make master dataset...
		final CombinedDataset data = new CombinedDataset();
		data.add(data1);
		data.add(data2);

		// decompose data...
		final XYDataset series0 = new SubSeriesDataset(data, 0);
		final XYDataset series1 = new SubSeriesDataset(data, 1);

		JFreeChart chart = null;

		// common horizontal and vertical axes
		final SymbolicAxis hsymbolicAxis0 = new SymbolicAxis(x1AxisLabel, ((XisSymbolic) data1).getXSymbolicValues());
		final SymbolicAxis hsymbolicAxis1 = new SymbolicAxis(x2AxisLabel, ((XisSymbolic) data2).getXSymbolicValues());
		final SymbolicAxis symbolicAxis = new SymbolicAxis(yAxisLabel, combinedYSymbolicValues);

		// create main plot...
		final CombinedRangeXYPlot mainPlot = new CombinedRangeXYPlot(symbolicAxis);

		// add subplots...
		final XYItemRenderer renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot0 = new XYPlot(series0, hsymbolicAxis0, null, renderer);
		final XYPlot subplot1 = new XYPlot(series1, hsymbolicAxis1, null, renderer);
		mainPlot.add(subplot0, 1);
		mainPlot.add(subplot1, 1);

		// make the top level JFreeChart object
		chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		// and present it in a frame...
		final JFrame frame = new ChartFrame(frameTitle, chart);
		frame.pack();
		RefineryUtilities.positionFrameRandomly(frame);
		frame.show();

	}

	// ****************************************************************************
	// * JFREECHART DEVELOPER GUIDE *
	// * The JFreeChart Developer Guide, written by David Gilbert, is available *
	// * to purchase from Object Refinery Limited: *
	// * *
	// * http://www.object-refinery.com/jfreechart/guide.html *
	// * *
	// * Sales are used to provide funding for the JFreeChart project - please *
	// * support us so that we can continue developing free software. *
	// ****************************************************************************

	/**
	 * Vertically combined sample1 and sample2 and display it.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 * @param data1
	 *           the dataset 1.
	 * @param data2
	 *           the dataset 2.
	 */
	private static void displayXYSymbolicCombinedVertically(final String frameTitle,
																				final XYDataset data1,
																				final XYDataset data2) {

		final String title = "Pollutant Vertically Combined";
		final String xAxisLabel = "Contamination and Type";
		final String yAxisLabel = "Pollutant";

		// combine the x symbolic values of the two data sets
		final String[] combinedXSymbolicValues = SampleXYSymbolicDataset.combineXSymbolicDataset((XisSymbolic) data1,
																					(XisSymbolic) data2);

		// make master dataset...
		final CombinedDataset data = new CombinedDataset();
		data.add(data1);
		data.add(data2);

		// decompose data...
		final XYDataset series0 = new SubSeriesDataset(data, 0);
		final XYDataset series1 = new SubSeriesDataset(data, 1);

		// common horizontal and vertical axes
		final SymbolicAxis hsymbolicAxis = new SymbolicAxis(xAxisLabel, combinedXSymbolicValues);

		final SymbolicAxis vsymbolicAxis0 = new SymbolicAxis(yAxisLabel, ((YisSymbolic) data1).getYSymbolicValues());

		final SymbolicAxis vsymbolicAxis1 = new SymbolicAxis(yAxisLabel, ((YisSymbolic) data2).getYSymbolicValues());

		// create the main plot...
		final CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(hsymbolicAxis);

		// add the sub-plots...
		final XYItemRenderer renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot0 = new XYPlot(series0, null, vsymbolicAxis0, renderer);
		final XYPlot subplot1 = new XYPlot(series1, null, vsymbolicAxis1, renderer);

		mainPlot.add(subplot0, 1);
		mainPlot.add(subplot1, 1);

		// make the chart...
		final JFreeChart chart = new JFreeChart(
							title, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true
							);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		// and present it in a frame...
		final JFrame frame = new ChartFrame(frameTitle, chart);
		frame.pack();
		RefineryUtilities.positionFrameRandomly(frame);
		frame.show();

	}

	/**
	 * Transform an primitive array to an object array.
	 * 
	 * @param arr
	 *           the array.
	 * @return an array.
	 */
	private static Object toArray(final Object arr) {

		if (arr == null) {
			return arr;
		}

		final Class cls = arr.getClass();
		if (!cls.isArray()) {
			return arr;
		}

		Class compType = cls.getComponentType();
		int dim = 1;
		while (!compType.isPrimitive()) {
			if (!compType.isArray()) {
				return arr;
			} else {
				dim++;
				compType = compType.getComponentType();
			}
		}

		final int[] length = new int[dim];
		length[0] = Array.getLength(arr);
		Object[] newarr = null;

		try {
			if (compType.equals(Integer.TYPE)) {
				newarr = (Object[]) Array.newInstance(Class.forName("java.lang.Integer"), length);
			} else
				if (compType.equals(Double.TYPE)) {
					newarr = (Object[]) Array.newInstance(Class.forName("java.lang.Double"), length);
				} else
					if (compType.equals(Long.TYPE)) {
						newarr = (Object[]) Array.newInstance(Class.forName("java.lang.Long"), length);
					} else
						if (compType.equals(Float.TYPE)) {
							newarr = (Object[]) Array.newInstance(Class.forName("java.lang.Float"), length);
						} else
							if (compType.equals(Short.TYPE)) {
								newarr = (Object[]) Array.newInstance(Class.forName("java.lang.Short"), length);
							} else
								if (compType.equals(Byte.TYPE)) {
									newarr = (Object[]) Array.newInstance(Class.forName("java.lang.Byte"), length);
								} else
									if (compType.equals(Character.TYPE)) {
										newarr = (Object[]) Array.newInstance(Class.forName("java.lang.Character"), length);
									} else
										if (compType.equals(Boolean.TYPE)) {
											newarr = (Object[]) Array.newInstance(Class.forName("java.lang.Boolean"), length);
										}
		} catch (ClassNotFoundException ex) {
			System.out.println(ex);
		}

		for (int i = 0; i < length[0]; i++) {
			if (dim != 1) {
				newarr[i] = toArray(Array.get(arr, i));
			} else {
				newarr[i] = Array.get(arr, i);
			}
		}
		return newarr;
	}

	/**
	 * Starting point for the application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final SampleXYSymbolicDataset s1 = createXYSymbolicSample1();
		final SampleXYSymbolicDataset s2 = createXYSymbolicSample2();

		displayXYSymbolic("Example 1", s1, "Pollutant", "contamination", "pollutant");

		displayXYSymbolic("Example 2", s2, "Pollutant", "type", "pollutant");

		displayXYSymbolicCombinedHorizontally("Example 3", (SampleXYSymbolicDataset) s1.clone(),
																				(SampleXYSymbolicDataset) s2.clone());

		displayXYSymbolicCombinedVertically("Example 4", (SampleXYSymbolicDataset) s1.clone(),
																			(SampleXYSymbolicDataset) s2.clone());

		displayXYSymbolicOverlaid("Example 5", (SampleXYSymbolicDataset) s1.clone(),
																(SampleXYSymbolicDataset) s2.clone());

	}

}
