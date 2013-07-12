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
 * ---------------------
 * ScatterPlotDemo2.java
 * ---------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ScatterPlotDemo2.java,v 1.1 2011-01-31 09:01:48 klukas Exp $
 * Changes
 * -------
 * 14-Oct-2002 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.XYDotRenderer;
import org.jfree.data.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo scatter plot.
 */
public class ScatterPlotDemo2 extends ApplicationFrame {

	/**
	 * A demonstration application showing a scatter plot.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public ScatterPlotDemo2(final String title) {

		super(title);
		final XYDataset dataset = new SampleXYDataset2();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setVerticalAxisTrace(true);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setVerticalZoom(true);
		chartPanel.setHorizontalZoom(true);
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
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createScatterPlot(
							"Scatter Plot Demo",
							"X", "Y", dataset,
							PlotOrientation.HORIZONTAL,
							true, // legend
				false, // tooltips
				false // urls
				);
		final XYPlot plot = chart.getXYPlot();
		plot.setRenderer(new XYDotRenderer());
		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
		return chart;
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final ScatterPlotDemo2 demo = new ScatterPlotDemo2("Scatter Plot Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
