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
 * ---------------------------
 * MinMaxCategoryPlotDemo.java
 * ---------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MinMaxCategoryPlotDemo.java,v 1.1 2011-01-31 09:01:44 klukas Exp $
 * Changes
 * -------
 * 08-Aug-2002 : Demo for a renderer contributed by Tomer Peretz (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.MinMaxCategoryRenderer;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a min/max category plot.
 */
public class MinMaxCategoryPlotDemo extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MinMaxCategoryPlotDemo(final String title) {

		super(title);

		// create a dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(1.0, "First", "Category 1");
		dataset.addValue(4.0, "First", "Category 2");
		dataset.addValue(3.0, "First", "Category 3");
		dataset.addValue(5.0, "First", "Category 4");
		dataset.addValue(5.0, "First", "Category 5");
		dataset.addValue(7.0, "First", "Category 6");
		dataset.addValue(7.0, "First", "Category 7");
		dataset.addValue(8.0, "First", "Category 8");
		dataset.addValue(5.0, "Second", "Category 1");
		dataset.addValue(7.0, "Second", "Category 2");
		dataset.addValue(6.0, "Second", "Category 3");
		dataset.addValue(8.0, "Second", "Category 4");
		dataset.addValue(4.0, "Second", "Category 5");
		dataset.addValue(4.0, "Second", "Category 6");
		dataset.addValue(2.0, "Second", "Category 7");
		dataset.addValue(1.0, "Second", "Category 8");
		dataset.addValue(4.0, "Third", "Category 1");
		dataset.addValue(3.0, "Third", "Category 2");
		dataset.addValue(2.0, "Third", "Category 3");
		dataset.addValue(3.0, "Third", "Category 4");
		dataset.addValue(6.0, "Third", "Category 5");
		dataset.addValue(3.0, "Third", "Category 6");
		dataset.addValue(4.0, "Third", "Category 7");
		dataset.addValue(3.0, "Third", "Category 8");

		// create the chart...
		final JFreeChart chart = ChartFactory.createBarChart(
							"Min/Max Category Plot", // chart title
				"Category", // domain axis label
				"Value", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL,
							true, // include legend
				true, // tooltips
				false // urls
				);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.yellow);

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setRenderer(new MinMaxCategoryRenderer());
		// OPTIONAL CUSTOMISATION COMPLETED.

		// add the chart to a panel...
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
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final MinMaxCategoryPlotDemo demo = new MinMaxCategoryPlotDemo(
							"Min/Max Category Chart Demo"
							);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
