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
 * HighLowChartDemo2.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: HighLowChartDemo2.java,v 1.1 2011-01-31 09:01:47 klukas Exp $
 * Changes
 * -------
 * 03-Dec-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.data.HighLowDataset;
import org.jfree.data.MovingAverage;
import org.jfree.data.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo showing a high-low-open-close chart with a moving average overlaid on top.
 */
public class HighLowChartDemo2 extends ApplicationFrame {

	/**
	 * A demonstration application showing a high-low-open-close chart.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public HighLowChartDemo2(final String title) {

		super(title);

		final HighLowDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a sample dataset.
	 */
	private HighLowDataset createDataset() {
		return DemoDatasetFactory.createHighLowDataset();
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *           a dataset.
	 * @return a sample chart.
	 */
	private JFreeChart createChart(final HighLowDataset dataset) {

		final JFreeChart chart = ChartFactory.createHighLowChart(
							"OHLC Demo 2",
							"Time",
							"Value",
							dataset,
							true
							);

		final DateAxis axis = (DateAxis) chart.getXYPlot().getDomainAxis();
		axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);

		final XYDataset dataset2 = MovingAverage.createMovingAverage(
							dataset, "-MAVG", 3 * 24 * 60 * 60 * 1000L, 0L
							);
		final XYPlot plot = (XYPlot) chart.getPlot();
		plot.setDataset(1, dataset2);
		plot.setRenderer(1, new StandardXYItemRenderer());

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

		final HighLowChartDemo2 demo = new HighLowChartDemo2("OHLC Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
