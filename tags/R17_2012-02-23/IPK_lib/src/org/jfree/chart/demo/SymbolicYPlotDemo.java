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
 * SymbolicYPlotDemo.java
 * ----------------------
 * (C) Copyright 2002-2004, by Anthony Boulestreau and Contributors.
 * Original Author: Anthony Boulestreau;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Changes
 * -------
 * 29-Mar-2002 : Version 1 (AB);
 * 23-Apr-2002 : Updated to reflect revisions in combined plot classes (DG);
 * 25-Jun-2002 : Removed unnecessary imports (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.GradientPaint;

import javax.swing.JFrame;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.SymbolicXYItemLabelGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.CombinedDataset;
import org.jfree.data.SubSeriesDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.YisSymbolic;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration application for the symbolic axis plots.
 * 
 * @author Anthony Boulestreau
 */
public class SymbolicYPlotDemo {

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
	 * Displays an XYPlot with Y symbolic data.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 * @param data
	 *           the data.
	 * @param chartTitle
	 *           the chart title.
	 * @param xAxisLabel
	 *           the x-axis label.
	 * @param yAxisLabel
	 *           the y-axis label.
	 */
	private static void displayYSymbolic(final String frameTitle,
														final XYDataset data, final String chartTitle,
														final String xAxisLabel, final String yAxisLabel) {

		final JFreeChart chart = createYSymbolicPlot(
							chartTitle, xAxisLabel, yAxisLabel, data, true
							);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.green));

		final JFrame frame = new ChartFrame(frameTitle, chart);
		frame.pack();
		RefineryUtilities.positionFrameRandomly(frame);
		frame.show();

	}

	/**
	 * Create and display an overlaid chart.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 * @param data1
	 *           dataset1.
	 * @param data2
	 *           dataset2.
	 */
	private static void displayYSymbolicOverlaid(final String frameTitle,
																	final XYDataset data1, final XYDataset data2) {

		final String title = "Animals Overlaid";
		final String xAxisLabel = "Miles";
		final String yAxisLabel = "Animal";

		// combine the y symbolic values of the two data sets...
		final String[] combinedYSymbolicValues = SampleYSymbolicDataset.combineYSymbolicDataset((YisSymbolic) data1,
																					(YisSymbolic) data2);

		// make master dataset...
		final CombinedDataset data = new CombinedDataset();
		data.add(data1);
		data.add(data2);

		// decompose data...
		final XYDataset series0 = new SubSeriesDataset(data, 0);
		final XYDataset series1 = new SubSeriesDataset(data, 1);
		final XYDataset series2 = new SubSeriesDataset(data, 2);
		final XYDataset series3 = new SubSeriesDataset(data, 3);
		final XYDataset series4 = new SubSeriesDataset(data, 4);
		final XYDataset series5 = new SubSeriesDataset(data, 5);
		final XYDataset series6 = new SubSeriesDataset(data, 6);
		final XYDataset series7 = new SubSeriesDataset(data, 7);

		// create main plot...
		final ValueAxis valueAxis = new NumberAxis(xAxisLabel);
		final SymbolicAxis symbolicAxis = new SymbolicAxis(yAxisLabel, combinedYSymbolicValues);
		final XYItemRenderer renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot plot = new XYPlot(series0, valueAxis, symbolicAxis, renderer);

		plot.setDataset(1, series1);
		final XYItemRenderer renderer1 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		plot.setRenderer(1, renderer1);

		plot.setDataset(2, series2);
		final XYItemRenderer renderer2 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		plot.setRenderer(2, renderer2);

		plot.setDataset(3, series3);
		final XYItemRenderer renderer3 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		plot.setRenderer(3, renderer3);

		plot.setDataset(4, series4);
		final XYItemRenderer renderer4 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		plot.setRenderer(4, renderer4);

		plot.setDataset(5, series5);
		final XYItemRenderer renderer5 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		plot.setRenderer(5, renderer5);

		plot.setDataset(6, series6);
		final XYItemRenderer renderer6 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		plot.setRenderer(6, renderer6);

		plot.setDataset(7, series7);
		final XYItemRenderer renderer7 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		plot.setRenderer(7, renderer7);

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
	 * Create and display a multi XY plot with horizontal layout.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 * @param data1
	 *           dataset1.
	 * @param data2
	 *           dataset2.
	 */
	private static void displayYSymbolicCombinedHorizontally(final String frameTitle,
																					final SampleYSymbolicDataset data1,
																					final SampleYSymbolicDataset data2) {

		final String title = "Animals Horizontally Combined";
		final String xAxisLabel = "Miles";
		final String yAxisLabel = null;

		// combine the y symbolic values of the two data sets
		final String[] combinedYSymbolicValues = SampleYSymbolicDataset.combineYSymbolicDataset(data1, data2);

		// make master dataset...
		final CombinedDataset data = new CombinedDataset();
		data.add(data1);
		data.add(data2);

		// decompose data...
		final XYDataset series0 = new SubSeriesDataset(data, 0);
		final XYDataset series1 = new SubSeriesDataset(data, 1);
		final XYDataset series2 = new SubSeriesDataset(data, 2);
		final XYDataset series3 = new SubSeriesDataset(data, 3);
		final XYDataset series4 = new SubSeriesDataset(data, 4);
		final XYDataset series5 = new SubSeriesDataset(data, 5);
		final XYDataset series6 = new SubSeriesDataset(data, 6);
		final XYDataset series7 = new SubSeriesDataset(data, 7);

		// create axes...
		final ValueAxis valueAxis0 = new NumberAxis(xAxisLabel);
		final ValueAxis valueAxis1 = new NumberAxis(xAxisLabel);
		final ValueAxis valueAxis2 = new NumberAxis(xAxisLabel);
		final ValueAxis valueAxis3 = new NumberAxis(xAxisLabel);
		final ValueAxis valueAxis4 = new NumberAxis(xAxisLabel);
		final ValueAxis valueAxis5 = new NumberAxis(xAxisLabel);
		final ValueAxis valueAxis6 = new NumberAxis(xAxisLabel);
		final ValueAxis valueAxis7 = new NumberAxis(xAxisLabel);
		final SymbolicAxis symbolicAxis = new SymbolicAxis(yAxisLabel, combinedYSymbolicValues);

		// make a combined plot
		final CombinedRangeXYPlot mainPlot = new CombinedRangeXYPlot(symbolicAxis);

		// add the sub-plots
		final XYItemRenderer renderer0 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot0 = new XYPlot(series0, valueAxis0, null, renderer0);
		final XYItemRenderer renderer1 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot1 = new XYPlot(series1, valueAxis1, null, renderer1);
		final XYItemRenderer renderer2 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot2 = new XYPlot(series2, valueAxis2, null, renderer2);
		final XYItemRenderer renderer3 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot3 = new XYPlot(series3, valueAxis3, null, renderer3);
		final XYItemRenderer renderer4 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot4 = new XYPlot(series4, valueAxis4, null, renderer4);
		final XYItemRenderer renderer5 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot5 = new XYPlot(series5, valueAxis5, null, renderer5);
		final XYItemRenderer renderer6 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot6 = new XYPlot(series6, valueAxis6, null, renderer6);
		final XYItemRenderer renderer7 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot7 = new XYPlot(series7, valueAxis7, null, renderer7);

		mainPlot.add(subplot0, 1);
		mainPlot.add(subplot1, 1);
		mainPlot.add(subplot2, 1);
		mainPlot.add(subplot3, 1);
		mainPlot.add(subplot4, 1);
		mainPlot.add(subplot5, 1);
		mainPlot.add(subplot6, 1);
		mainPlot.add(subplot7, 1);

		// make the top level JFreeChart object
		final JFreeChart chart = new JFreeChart(
							title, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		// and present it in a frame...
		final JFrame ySymbolicFrame = new ChartFrame(frameTitle, chart);
		ySymbolicFrame.pack();
		RefineryUtilities.positionFrameRandomly(ySymbolicFrame);
		ySymbolicFrame.show();

	}

	/**
	 * Displays a vertically combined symbolic plot.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 * @param data1
	 *           dataset 1.
	 * @param data2
	 *           dataset 2.
	 */
	private static void displayYSymbolicCombinedVertically(final String frameTitle,
																				final SampleYSymbolicDataset data1,
																				final SampleYSymbolicDataset data2) {

		final String title = "Animals Vertically Combined";
		final String xAxisLabel = "Miles";
		final String yAxisLabel = null;

		// create master dataset...
		final CombinedDataset data = new CombinedDataset();
		data.add(data1);
		data.add(data2);

		// decompose data...
		final XYDataset series0 = new SubSeriesDataset(data, 0);
		final XYDataset series1 = new SubSeriesDataset(data, 1);
		final XYDataset series2 = new SubSeriesDataset(data, 2);
		final XYDataset series3 = new SubSeriesDataset(data, 3);
		final XYDataset series4 = new SubSeriesDataset(data, 4);
		final XYDataset series5 = new SubSeriesDataset(data, 5);
		final XYDataset series6 = new SubSeriesDataset(data, 6);
		final XYDataset series7 = new SubSeriesDataset(data, 7);

		// common horizontal and vertical axes
		final ValueAxis valueAxis = new NumberAxis(xAxisLabel);
		final SymbolicAxis symbolicAxis1 = new SymbolicAxis(yAxisLabel, ((YisSymbolic) data1).getYSymbolicValues());
		final SymbolicAxis symbolicAxis2 = new SymbolicAxis(yAxisLabel, ((YisSymbolic) data2).getYSymbolicValues());

		// create the main plot...
		final CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(valueAxis);

		// and the sub-plots...
		final XYItemRenderer renderer0 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot0 = new XYPlot(series0, null, symbolicAxis1, renderer0);
		final XYItemRenderer renderer1 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot1 = new XYPlot(series1, null, symbolicAxis1, renderer1);
		final XYItemRenderer renderer2 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot2 = new XYPlot(series2, null, symbolicAxis1, renderer2);
		final XYItemRenderer renderer3 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot3 = new XYPlot(series3, null, symbolicAxis1, renderer3);
		final XYItemRenderer renderer4 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot4 = new XYPlot(series4, null, symbolicAxis2, renderer4);
		final XYItemRenderer renderer5 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot5 = new XYPlot(series5, null, symbolicAxis2, renderer5);
		final XYItemRenderer renderer6 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot6 = new XYPlot(series6, null, symbolicAxis2, renderer6);
		final XYItemRenderer renderer7 = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, null
							);
		final XYPlot subplot7 = new XYPlot(series7, null, symbolicAxis2, renderer7);

		// add the subplots to the main plot...
		mainPlot.add(subplot0, 1);
		mainPlot.add(subplot1, 1);
		mainPlot.add(subplot2, 1);
		mainPlot.add(subplot3, 1);
		mainPlot.add(subplot4, 1);
		mainPlot.add(subplot5, 1);
		mainPlot.add(subplot6, 1);
		mainPlot.add(subplot7, 1);

		// construct the chart...
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
	 * Creates a XY graph with symbolic value on Y axis.
	 * 
	 * @param title
	 *           the chart title.
	 * @param xAxisLabel
	 *           the x-axis label.
	 * @param yAxisLabel
	 *           the y-axis label.
	 * @param data
	 *           the data.
	 * @param legend
	 *           a flag controlling whether or not the legend is created for the chart.
	 * @return the chart.
	 */
	public static JFreeChart createYSymbolicPlot(final String title, final String xAxisLabel,
																	final String yAxisLabel, final XYDataset data,
																	final boolean legend) {

		final ValueAxis valueAxis = new NumberAxis(xAxisLabel);
		final SymbolicAxis symbolicAxis = new SymbolicAxis(yAxisLabel, ((YisSymbolic) data).getYSymbolicValues());

		final XYPlot plot = new XYPlot(data, valueAxis, symbolicAxis, null);
		final XYItemRenderer renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, new SymbolicXYItemLabelGenerator()
							);
		plot.setRenderer(renderer);
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
		return chart;

	}

	/**
	 * Creates a sample symbolic dataset.
	 * 
	 * @return the dataset.
	 */
	public static SampleYSymbolicDataset createYSymbolicSample1() {

		final String[] sData = { "Lion", "Elephant", "Monkey", "Hippopotamus", "Giraffe" };
		final SampleYSymbolicDataset data = new SampleYSymbolicDataset("AY Sample", 20, sData, 4, 20,
							new String[] { "A Fall", "A Spring", "A Summer", "A Winter" });
		return data;

	}

	/**
	 * Creates a sample symbolic dataset.
	 * 
	 * @return The dataset.
	 */
	public static SampleYSymbolicDataset createYSymbolicSample2() {

		final String[] sData = { "Giraffe", "Gazelle", "Zebra", "Gnu" };
		final SampleYSymbolicDataset data = new SampleYSymbolicDataset("BY Sample", 40, sData, 4, 20,
							new String[] { "B Fall", "B Spring", "B Summer", "B Winter" });
		return data;

	}

	/**
	 * The starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final SampleYSymbolicDataset s1 = createYSymbolicSample1();
		final SampleYSymbolicDataset s2 = createYSymbolicSample2();

		displayYSymbolic("Example 1", s1, "Animal A", "Miles", "Animal");

		displayYSymbolic("Example 2", s2, "Animal B", "Miles", "Animal");

		displayYSymbolicCombinedHorizontally("Example 3", (SampleYSymbolicDataset) s1.clone(),
																				(SampleYSymbolicDataset) s2.clone());

		displayYSymbolicCombinedVertically("Example 4", (SampleYSymbolicDataset) s1.clone(),
																			(SampleYSymbolicDataset) s2.clone());

		displayYSymbolicOverlaid("Example 5", (SampleYSymbolicDataset) s1.clone(),
																(SampleYSymbolicDataset) s2.clone());
	}

}
