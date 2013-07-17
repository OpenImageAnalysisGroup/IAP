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
 * ----------------------------
 * StackedXYAreaChartDemo2.java
 * ----------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: StackedXYAreaChartDemo2.java,v 1.1 2011-01-31 09:01:55 klukas Exp $
 * Changes
 * -------
 * 28-Jan-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.DefaultTableXYDataset;
import org.jfree.data.TableXYDataset;
import org.jfree.data.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * This demo shows the creation of a stacked XY area chart. I created this demo while
 * investigating bug report 882890.
 */
public class StackedXYAreaChartDemo2 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public StackedXYAreaChartDemo2(final String title) {
		super(title);
		final TableXYDataset dataset = createDataset();
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
	private TableXYDataset createDataset() {

		final DefaultTableXYDataset dataset = new DefaultTableXYDataset();

		final XYSeries s1 = new XYSeries("Series 1", true, false);
		s1.add(5.0, 5.0);
		s1.add(10.0, 15.5);
		s1.add(15.0, 9.5);
		s1.add(20.0, 7.5);
		dataset.addSeries(s1);

		final XYSeries s2 = new XYSeries("Series 2", true, false);
		s2.add(5.0, 5.0);
		s2.add(10.0, 15.5);
		s2.add(15.0, 9.5);
		s2.add(20.0, 3.5);
		dataset.addSeries(s2);

		return dataset;

	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *           the dataset for the chart.
	 * @return a sample chart.
	 */
	private JFreeChart createChart(final TableXYDataset dataset) {

		final JFreeChart chart = ChartFactory.createStackedXYAreaChart(
							"Stacked XY Area Chart Demo 2", // chart title
				"Category", // domain axis label
				"Value", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // the plot orientation
				true, // legend
				true, // tooltips
				false // urls
				);
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
		final StackedXYAreaChartDemo2 demo = new StackedXYAreaChartDemo2(
							"Stacked XY Area Chart Demo 2"
							);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

}
