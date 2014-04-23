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
 * --------------------
 * TimeSeriesDemo3.java
 * --------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimeSeriesDemo3.java,v 1.1 2011-01-31 09:01:52 klukas Exp $
 * Changes
 * -------
 * 06-Aug-2002 : Version 1 (DG);
 * 11-Oct-2002 : Fixes issues reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A time series demo, with monthly data, where the tick unit on the axis is set to
 * one month also (this switches off the auto tick unit selection, and *can* result in
 * overlapping labels).
 */
public class TimeSeriesDemo3 extends ApplicationFrame {

	/**
	 * A demonstration application showing a quarterly time series containing a null value.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimeSeriesDemo3(final String title) {

		super(title);

		final TimeSeries series1 = new TimeSeries("Series 1", Month.class);
		series1.add(new Month(1, 2002), 500.2);
		series1.add(new Month(2, 2002), 694.1);
		series1.add(new Month(3, 2002), 734.4);
		series1.add(new Month(4, 2002), 453.2);
		series1.add(new Month(5, 2002), 500.2);
		series1.add(new Month(6, 2002), 345.6);
		series1.add(new Month(7, 2002), 500.2);
		series1.add(new Month(8, 2002), 694.1);
		series1.add(new Month(9, 2002), 734.4);
		series1.add(new Month(10, 2002), 453.2);
		series1.add(new Month(11, 2002), 500.2);
		series1.add(new Month(12, 2002), 345.6);

		final TimeSeries series2 = new TimeSeries("Series 2", Month.class);
		series2.add(new Month(1, 2002), 234.1);
		series2.add(new Month(2, 2002), 623.7);
		series2.add(new Month(3, 2002), 642.5);
		series2.add(new Month(4, 2002), 651.4);
		series2.add(new Month(5, 2002), 643.5);
		series2.add(new Month(6, 2002), 785.6);
		series2.add(new Month(7, 2002), 234.1);
		series2.add(new Month(8, 2002), 623.7);
		series2.add(new Month(9, 2002), 642.5);
		series2.add(new Month(10, 2002), 651.4);
		series2.add(new Month(11, 2002), 643.5);
		series2.add(new Month(12, 2002), 785.6);

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(series1);
		dataset.addSeries(series2);

		final JFreeChart chart = createChart(dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

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
	 * Creates a new chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return The dataset.
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							"Time Series Demo 3",
							"Time",
							"Value",
							dataset,
							true,
							true,
							false
							);
		final XYPlot plot = chart.getXYPlot();
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setTickUnit(new DateTickUnit(DateTickUnit.MONTH, 1,
														new SimpleDateFormat("MMM-yyyy")));
		axis.setVerticalTickLabels(true);

		final StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
		renderer.setPlotShapes(true);
		renderer.setSeriesShapesFilled(0, Boolean.TRUE);
		renderer.setSeriesShapesFilled(1, Boolean.FALSE);

		return chart;
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final TimeSeriesDemo3 demo = new TimeSeriesDemo3("Time Series Demo 3");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
