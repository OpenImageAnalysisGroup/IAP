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
 * ------------------
 * WindChartDemo.java
 * ------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: WindChartDemo.java,v 1.1 2011-01-31 09:01:43 klukas Exp $
 * Changes
 * -------
 * 30-Jun-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.GradientPaint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.WindDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a wind chart.
 */
public class WindChartDemo extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public WindChartDemo(final String title) {

		super(title);

		final WindDataset dataset = DemoDatasetFactory.createWindDataset1();

		// create the chart...
		final JFreeChart chart = createChart(dataset);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final WindDataset dataset) {

		final JFreeChart chart = ChartFactory.createWindPlot(
							"Wind Chart Demo",
							"Date",
							"Direction / Force",
							dataset,
							true,
							false,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.green));
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

		final WindChartDemo demo = new WindChartDemo("Wind Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
