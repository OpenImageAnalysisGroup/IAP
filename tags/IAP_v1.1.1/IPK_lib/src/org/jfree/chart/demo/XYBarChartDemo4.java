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
 * XYBarChartDemo4.java
 * --------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XYBarChartDemo4.java,v 1.1 2011-01-31 09:01:56 klukas Exp $
 * Changes
 * -------
 * 02-Mar-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.GradientPaint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.XYBarDataset;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration of the {@link XYBarDataset} wrapper class.
 */
public class XYBarChartDemo4 extends ApplicationFrame {

	/**
	 * Constructs the demo application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public XYBarChartDemo4(final String title) {

		super(title);

		final IntervalXYDataset dataset = createDataset();

		final JFreeChart chart = ChartFactory.createXYBarChart(
							title,
							"X",
							false,
							"Y",
							dataset,
							PlotOrientation.VERTICAL,
							true,
							false,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.blue));
		final XYPlot plot = (XYPlot) chart.getPlot();
		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return A sample dataset.
	 */
	private IntervalXYDataset createDataset() {
		final XYSeries series = new XYSeries("Series 1");
		series.add(1.0, 5.0);
		series.add(2.0, 7.8);
		series.add(3.0, 9.3);
		final XYSeriesCollection collection = new XYSeriesCollection();
		collection.addSeries(series);
		return new XYBarDataset(collection, 0.9);
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

		final XYBarChartDemo4 demo = new XYBarChartDemo4("XY Bar Chart Demo 4");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
