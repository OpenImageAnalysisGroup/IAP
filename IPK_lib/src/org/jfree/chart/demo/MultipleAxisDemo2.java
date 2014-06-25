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
 * MultipleAxisDemo2.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MultipleAxisDemo2.java,v 1.1 2011-01-31 09:01:58 klukas Exp $
 * Changes
 * -------
 * 15-Jul-2002 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * An example of....
 */
public class MultipleAxisDemo2 extends ApplicationFrame {

	/**
	 * A demonstration application showing how to create a time series chart with muliple axes.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MultipleAxisDemo2(final String title) {

		super(title);
		final JFreeChart chart = createChart();
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates the demo chart.
	 * 
	 * @return The chart.
	 */
	private JFreeChart createChart() {

		final XYDataset dataset1 = createDataset("Series 1", 100.0, new Minute(), 200);

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							"Multiple Axis Demo 2",
							"Time of Day",
							"Primary Range Axis",
							dataset1,
							true,
							true,
							false
							);

		chart.setBackgroundPaint(Color.white);
		final XYPlot plot = chart.getXYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));

		final StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
		renderer.setPaint(Color.black);

		// DOMAIN AXIS 2
		final NumberAxis xAxis2 = new NumberAxis("Domain Axis 2");
		xAxis2.setAutoRangeIncludesZero(false);
		plot.setDomainAxis(1, xAxis2);

		// RANGE AXIS 2
		final NumberAxis yAxis2 = new NumberAxis("Range Axis 2");
		plot.setRangeAxis(1, yAxis2);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

		final XYDataset dataset2 = createDataset("Series 2", 1000.0, new Minute(), 170);
		plot.setDataset(1, dataset2);
		plot.mapDatasetToDomainAxis(1, 1);
		plot.mapDatasetToRangeAxis(1, 1);

		return chart;

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
	 * Creates a sample dataset.
	 * 
	 * @param name
	 *           the dataset name.
	 * @param base
	 *           the starting value.
	 * @param start
	 *           the starting period.
	 * @param count
	 *           the number of values to generate.
	 * @return The dataset.
	 */
	private XYDataset createDataset(final String name, final double base,
												final RegularTimePeriod start, final int count) {

		final TimeSeries series = new TimeSeries(name, start.getClass());
		RegularTimePeriod period = start;
		double value = base;
		for (int i = 0; i < count; i++) {
			series.add(period, value);
			period = period.next();
			value = value * (1 + (Math.random() - 0.495) / 10.0);
		}

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(series);

		return dataset;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final MultipleAxisDemo2 demo = new MultipleAxisDemo2("Multiple Axis Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
