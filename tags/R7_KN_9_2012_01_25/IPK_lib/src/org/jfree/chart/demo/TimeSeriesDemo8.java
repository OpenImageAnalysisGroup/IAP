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
 * TimeSeriesDemo8.java
 * --------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimeSeriesDemo8.java,v 1.1 2011-01-31 09:01:53 klukas Exp $
 * Changes
 * -------
 * 05-Feb-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.MovingAverage;
import org.jfree.data.XYDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A time series chart.
 */
public class TimeSeriesDemo8 extends ApplicationFrame {

	/**
	 * A demonstration application showing how to create a simple time series chart.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimeSeriesDemo8(final String title) {
		super(title);
		final XYDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMouseZoomable(true, false);
		setContentPane(chartPanel);
	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a sample dataset.
	 */
	private XYDataset createDataset() {
		final TimeSeries eur = DemoDatasetFactory.createEURTimeSeries();
		final TimeSeries mav = MovingAverage.createMovingAverage(
							eur, "30 day moving average", 30, 30
							);
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(eur);
		dataset.addSeries(mav);
		return dataset;
	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return a chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							"Time Series Demo 8",
							"Date",
							"Value",
							dataset,
							true,
							true,
							false
							);
		final XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		final StandardXYToolTipGenerator g = new StandardXYToolTipGenerator(
							StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
							new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")
							);
		renderer.setToolTipGenerator(g);
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
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final TimeSeriesDemo8 demo = new TimeSeriesDemo8("Time Series Demo 8");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
