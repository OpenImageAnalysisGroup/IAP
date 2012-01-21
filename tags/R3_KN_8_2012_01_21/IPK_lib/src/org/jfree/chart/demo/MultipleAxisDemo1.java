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
 * MultipleAxisDemo1.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MultipleAxisDemo1.java,v 1.1 2011-01-31 09:01:55 klukas Exp $
 * Changes
 * -------
 * 15-Jul-2002 : Version 1 (DG);
 * 27-Apr-2004 : Updated for changes to XYPlot class (DG);
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
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * This demo shows a time series chart that has multiple range axes.
 */
public class MultipleAxisDemo1 extends ApplicationFrame {

	/**
	 * A demonstration application showing how to create a time series chart with muliple axes.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MultipleAxisDemo1(final String title) {

		super(title);
		final JFreeChart chart = createChart();
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 270));
		chartPanel.setHorizontalZoom(true);
		chartPanel.setVerticalZoom(true);
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
							"Multiple Axis Demo 1",
							"Time of Day",
							"Primary Range Axis",
							dataset1,
							true,
							true,
							false
							);

		chart.setBackgroundPaint(Color.white);
		chart.addSubtitle(new TextTitle("Four datasets and four range axes."));
		final XYPlot plot = chart.getXYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));

		final StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
		renderer.setPaint(Color.black);

		// AXIS 2
		final NumberAxis axis2 = new NumberAxis("Range Axis 2");
		axis2.setAutoRangeIncludesZero(false);
		axis2.setLabelPaint(Color.red);
		axis2.setTickLabelPaint(Color.red);
		plot.setRangeAxis(1, axis2);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);

		final XYDataset dataset2 = createDataset("Series 2", 1000.0, new Minute(), 170);
		plot.setDataset(1, dataset2);
		plot.mapDatasetToRangeAxis(1, 1);
		XYItemRenderer renderer2 = new StandardXYItemRenderer();
		renderer2.setSeriesPaint(0, Color.red);
		plot.setRenderer(1, renderer2);

		// AXIS 3
		final NumberAxis axis3 = new NumberAxis("Range Axis 3");
		axis3.setLabelPaint(Color.blue);
		axis3.setTickLabelPaint(Color.blue);
		plot.setRangeAxis(2, axis3);

		final XYDataset dataset3 = createDataset("Series 3", 10000.0, new Minute(), 170);
		plot.setDataset(2, dataset3);
		plot.mapDatasetToRangeAxis(2, 2);
		XYItemRenderer renderer3 = new StandardXYItemRenderer();
		renderer3.setSeriesPaint(0, Color.blue);
		plot.setRenderer(2, renderer3);

		// AXIS 4
		final NumberAxis axis4 = new NumberAxis("Range Axis 4");
		axis4.setLabelPaint(Color.green);
		axis4.setTickLabelPaint(Color.green);
		plot.setRangeAxis(3, axis4);

		final XYDataset dataset4 = createDataset("Series 4", 25.0, new Minute(), 200);
		plot.setDataset(3, dataset4);
		plot.mapDatasetToRangeAxis(3, 3);

		XYItemRenderer renderer4 = new StandardXYItemRenderer();
		renderer4.setSeriesPaint(0, Color.green);
		plot.setRenderer(3, renderer4);

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

		final MultipleAxisDemo1 demo = new MultipleAxisDemo1("Multiple Axis Demo 1");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
