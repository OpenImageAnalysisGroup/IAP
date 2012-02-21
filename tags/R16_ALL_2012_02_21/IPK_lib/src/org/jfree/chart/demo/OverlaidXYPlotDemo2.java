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
 * OverlaidXYPlotDemo2.java
 * ------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: OverlaidXYPlotDemo2.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes
 * -------
 * 03-Jul-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYBarRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.date.SerialDate;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration application showing an overlaid XY plot with dual range axes.
 */
public class OverlaidXYPlotDemo2 extends ApplicationFrame {

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public OverlaidXYPlotDemo2(final String title) {

		super(title);
		final JFreeChart chart = createOverlaidChart();
		final ChartPanel panel = new ChartPanel(chart, true, true, true, true, true);
		panel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(panel);

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
	 * Creates an overlaid chart.
	 * 
	 * @return The chart.
	 */
	private JFreeChart createOverlaidChart() {

		final DateAxis domainAxis = new DateAxis("Date");
		domainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
		final ValueAxis rangeAxis = new NumberAxis("Value");

		// create plot...
		final IntervalXYDataset data1 = createDataset1();
		final XYItemRenderer renderer1 = new XYBarRenderer(0.20);
		renderer1.setToolTipGenerator(
							new StandardXYToolTipGenerator(
												StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
												new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")
							)
							);
		final XYPlot plot = new XYPlot(data1, domainAxis, rangeAxis, renderer1);
		final double x = new Day(9, SerialDate.MARCH, 2002).getMiddleMillisecond();
		final XYTextAnnotation annotation = new XYTextAnnotation("Hello!", x, 10000.0);
		annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
		plot.addAnnotation(annotation);

		final ValueAxis rangeAxis2 = new NumberAxis("Value 2");
		plot.setRangeAxis(1, rangeAxis2);

		// create subplot 2...
		final XYDataset data2A = createDataset2A();
		final XYItemRenderer renderer2A = new StandardXYItemRenderer();
		renderer2A.setToolTipGenerator(
							new StandardXYToolTipGenerator(
												StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
												new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")
							)
							);
		plot.setDataset(1, data2A);
		plot.setRenderer(1, renderer2A);

		final XYDataset data2B = createDataset2B();
		plot.setDataset(2, data2B);
		plot.setRenderer(2, new StandardXYItemRenderer());
		plot.mapDatasetToRangeAxis(2, 1);

		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.setOrientation(PlotOrientation.VERTICAL);

		// return a new chart containing the overlaid plot...
		return new JFreeChart("Overlaid Plot Example", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return The dataset.
	 */
	private IntervalXYDataset createDataset1() {

		// create dataset 1...
		final TimeSeries series1 = new TimeSeries("Series 1", Day.class);
		series1.add(new Day(1, SerialDate.MARCH, 2002), 12353.3);
		series1.add(new Day(2, SerialDate.MARCH, 2002), 13734.4);
		series1.add(new Day(3, SerialDate.MARCH, 2002), 14525.3);
		series1.add(new Day(4, SerialDate.MARCH, 2002), 13984.3);
		series1.add(new Day(5, SerialDate.MARCH, 2002), 12999.4);
		series1.add(new Day(6, SerialDate.MARCH, 2002), 14274.3);
		series1.add(new Day(7, SerialDate.MARCH, 2002), 15943.5);
		series1.add(new Day(8, SerialDate.MARCH, 2002), 14845.3);
		series1.add(new Day(9, SerialDate.MARCH, 2002), 14645.4);
		series1.add(new Day(10, SerialDate.MARCH, 2002), 16234.6);
		series1.add(new Day(11, SerialDate.MARCH, 2002), 17232.3);
		series1.add(new Day(12, SerialDate.MARCH, 2002), 14232.2);
		series1.add(new Day(13, SerialDate.MARCH, 2002), 13102.2);
		series1.add(new Day(14, SerialDate.MARCH, 2002), 14230.2);
		series1.add(new Day(15, SerialDate.MARCH, 2002), 11235.2);

		final TimeSeriesCollection result = new TimeSeriesCollection(series1);
		return result;

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return The dataset.
	 */
	private XYDataset createDataset2A() {

		// create dataset 2...
		final TimeSeries series2 = new TimeSeries("Series 2", Day.class);

		series2.add(new Day(3, SerialDate.MARCH, 2002), 16853.2);
		series2.add(new Day(4, SerialDate.MARCH, 2002), 19642.3);
		series2.add(new Day(5, SerialDate.MARCH, 2002), 18253.5);
		series2.add(new Day(6, SerialDate.MARCH, 2002), 15352.3);
		series2.add(new Day(7, SerialDate.MARCH, 2002), 13532.0);
		series2.add(new Day(8, SerialDate.MARCH, 2002), 12635.3);
		series2.add(new Day(9, SerialDate.MARCH, 2002), 13998.2);
		series2.add(new Day(10, SerialDate.MARCH, 2002), 11943.2);
		series2.add(new Day(11, SerialDate.MARCH, 2002), 16943.9);
		series2.add(new Day(12, SerialDate.MARCH, 2002), 17843.2);
		series2.add(new Day(13, SerialDate.MARCH, 2002), 16495.3);
		series2.add(new Day(14, SerialDate.MARCH, 2002), 17943.6);
		series2.add(new Day(15, SerialDate.MARCH, 2002), 18500.7);
		series2.add(new Day(16, SerialDate.MARCH, 2002), 19595.9);

		final TimeSeriesCollection result = new TimeSeriesCollection(series2);
		result.setXPosition(TimePeriodAnchor.MIDDLE);
		return result;

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return The dataset.
	 */
	private XYDataset createDataset2B() {

		// create dataset 2...
		final TimeSeries series2 = new TimeSeries("Series 2B", Day.class);

		series2.add(new Day(3, SerialDate.MARCH, 2002), 43.9);
		series2.add(new Day(4, SerialDate.MARCH, 2002), 72.6);
		series2.add(new Day(5, SerialDate.MARCH, 2002), 89.4);
		series2.add(new Day(6, SerialDate.MARCH, 2002), 23.8);
		series2.add(new Day(7, SerialDate.MARCH, 2002), 45.0);
		series2.add(new Day(8, SerialDate.MARCH, 2002), 65.8);
		series2.add(new Day(9, SerialDate.MARCH, 2002), 92.1);
		series2.add(new Day(10, SerialDate.MARCH, 2002), 84.7);
		series2.add(new Day(11, SerialDate.MARCH, 2002), 77.2);
		series2.add(new Day(12, SerialDate.MARCH, 2002), 65.1);
		series2.add(new Day(13, SerialDate.MARCH, 2002), 78.5);
		series2.add(new Day(14, SerialDate.MARCH, 2002), 75.3);
		series2.add(new Day(15, SerialDate.MARCH, 2002), 69.9);
		series2.add(new Day(20, SerialDate.MARCH, 2002), 56.6);

		final TimeSeriesCollection result = new TimeSeriesCollection(series2);
		result.setXPosition(TimePeriodAnchor.MIDDLE);
		return result;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final OverlaidXYPlotDemo2 demo = new OverlaidXYPlotDemo2("Overlaid XYPlot Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
