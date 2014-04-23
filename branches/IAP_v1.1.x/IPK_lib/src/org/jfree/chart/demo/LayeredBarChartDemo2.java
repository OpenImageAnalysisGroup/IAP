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
 * -------------------------
 * LayeredBarChartDemo2.java
 * -------------------------
 * (C) Copyright 2003, 2004, by Arnaud Lelievre and Contributors.
 * Original Author: Arnaud Lelievre (for Garden);
 * Contributor(s): -;
 * Changes
 * -------
 * 28-Aug-2003 : Version 1 (AL);
 */

package org.jfree.chart.demo;

import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.LayeredBarRenderer;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetUtilities;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a superimposed vertical bar chart.
 * 
 * @author Arnaud Lelievre
 */
public class LayeredBarChartDemo2 extends ApplicationFrame {

	/**
	 * Creates a new demo instance.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public LayeredBarChartDemo2(final String title) {

		super(title);

		// create a dataset...
		final double[][] data = new double[][] {
							{ 41.0, 33.0, 22.0, 64.0, 42.0, 62.0, 22.0, 14.0 },
							{ 55.0, 63.0, 55.0, 48.0, 54.0, 37.0, 41.0, 39.0 },
							{ 57.0, 75.0, 43.0, 33.0, 63.0, 46.0, 57.0, 33.0 }
			};

		final CategoryDataset dataset = DatasetUtilities.createCategoryDataset("Series ",
																									"Factor ",
																									data);

		// create the chart...
		final CategoryAxis categoryAxis = new CategoryAxis("Category");
		final ValueAxis valueAxis = new NumberAxis("Score (%)");

		final CategoryPlot plot = new CategoryPlot(dataset,
															categoryAxis,
															valueAxis,
															new LayeredBarRenderer());

		plot.setOrientation(PlotOrientation.VERTICAL);
		final JFreeChart chart = new JFreeChart(
							"Layered Bar Chart Demo 2",
							JFreeChart.DEFAULT_TITLE_FONT,
							plot,
							true
							);

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.lightGray);

		final LayeredBarRenderer renderer = (LayeredBarRenderer) plot.getRenderer();

		// we can set each series bar width individually or let the renderer manage a standard view.
		// the width is set in percentage, where 1.0 is the maximum (100%).
		renderer.setSeriesBarWidth(0, 1.0);
		renderer.setSeriesBarWidth(1, 0.7);
		renderer.setSeriesBarWidth(2, 0.5);

		renderer.setItemMargin(0.01);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryMargin(0.25);
		domainAxis.setUpperMargin(0.05);
		domainAxis.setLowerMargin(0.05);

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

		final LayeredBarChartDemo2 demo = new LayeredBarChartDemo2("Layered Bar Chart Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
