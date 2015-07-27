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
 * YIntervalChartDemo.java
 * -----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: YIntervalChartDemo.java,v 1.1 2011-01-31 09:01:53 klukas Exp $
 * Changes
 * -------
 * 27-Jun-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.YIntervalRenderer;
import org.jfree.data.IntervalXYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a Y Interval Chart.
 */
public class YIntervalChartDemo extends ApplicationFrame {

	/**
	 * Constructs the demo application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public YIntervalChartDemo(final String title) {

		super(title);

		// create a dataset...
		final IntervalXYDataset dataset = new SimpleIntervalXYDataset2(100);

		// create the chart...
		final JFreeChart chart = createChart(dataset);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a new chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final IntervalXYDataset dataset) {

		final JFreeChart chart = ChartFactory.createScatterPlot(
							"Y Interval Chart Demo", // chart title
				"X", // domain axis label
				"Y", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL,
							true, // include legend
				true,
							false
							);

		final XYPlot plot = chart.getXYPlot();
		plot.setRenderer(new YIntervalRenderer());

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

		final YIntervalChartDemo demo = new YIntervalChartDemo("Y Interval Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
