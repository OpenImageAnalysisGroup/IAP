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
 * XYBarChartDemo2.java
 * --------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XYBarChartDemo2.java,v 1.1 2011-01-31 09:01:57 klukas Exp $
 * Changes
 * -------
 * 20-Jun-2002 : Version 1 (DG);
 * 02-Jul-2002 : Removed unnecessary imports (DG);
 * 24-Aug-2002 : Set preferred size for ChartPanel (DG);
 * 11-Oct-2002 : Fixed issues reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.ClusteredXYBarRenderer;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a vertical bar chart.
 */
public class XYBarChartDemo2 extends ApplicationFrame {

	/**
	 * Constructs the demo application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public XYBarChartDemo2(final String title) {

		super(title);

		// create a dataset...
		final TimeSeries series1 = new TimeSeries("Series 1", Day.class);
		series1.add(new Day(1, 1, 2003), 54.3);
		series1.add(new Day(2, 1, 2003), 20.3);
		series1.add(new Day(3, 1, 2003), 43.4);
		series1.add(new Day(4, 1, 2003), -12.0);

		final TimeSeries series2 = new TimeSeries("Series 2", Day.class);
		series2.add(new Day(1, 1, 2003), 8.0);
		series2.add(new Day(2, 1, 2003), 16.0);
		series2.add(new Day(3, 1, 2003), 21.0);
		series2.add(new Day(4, 1, 2003), 5.0);

		final TimeSeriesCollection data = new TimeSeriesCollection();
		data.setDomainIsPointsInTime(false);
		data.addSeries(series1);
		data.addSeries(series2);

		final JFreeChart chart = createChart(data);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
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
	private JFreeChart createChart(final IntervalXYDataset dataset) {
		final JFreeChart chart = ChartFactory.createXYBarChart(
							"XY Bar Chart Demo 2", // chart title
				"Date", // domain axis label
				true,
							"Y", // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL,
							true, // include legend
				true,
							false
							);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		final XYPlot plot = chart.getXYPlot();
		plot.setRenderer(new ClusteredXYBarRenderer());
		// OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final XYBarChartDemo2 demo = new XYBarChartDemo2("XY Bar Chart Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
