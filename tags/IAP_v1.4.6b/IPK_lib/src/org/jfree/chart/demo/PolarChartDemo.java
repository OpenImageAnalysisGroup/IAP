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
 * -------------------
 * PolarChartDemo.java
 * -------------------
 * (C) Copyright 2004, by Solution Engineering, Inc. and Contributors.
 * Original Author: Daniel Bridenbecker, Solution Engineering, Inc.;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: PolarChartDemo.java,v 1.1 2011-01-31 09:01:48 klukas Exp $
 * Changes
 * -------
 * 19-Jan-2004 : Version 1, contributed by DB with minor changes by DG (DG);
 */

package org.jfree.chart.demo;

import java.awt.Dimension;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.PolarChartPanel;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * <code>PolarChartDemo</code> demonstrates the capabilities of the {@link PolarPlot}.
 * 
 * @author Daniel Bridenbecker, Solution Engineering, Inc.
 */
public class PolarChartDemo extends ApplicationFrame {

	/**
	 * Creates a new instance of the demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public PolarChartDemo(final String title) {
		super(title);
		final XYDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new PolarChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(500, 270));
		chartPanel.setEnforceFileExtensions(false);
		setContentPane(chartPanel);
	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return A sample dataset.
	 */
	private XYDataset createDataset() {
		final XYSeriesCollection data = new XYSeriesCollection();
		final XYSeries series1 = createRandomData("Series 1", 75.0, 10.0);
		final XYSeries series2 = createRandomData("Series 2", 50.0, 5.0);
		final XYSeries series3 = createRandomData("Series 3", 25.0, 1.0);
		data.addSeries(series1);
		data.addSeries(series2);
		data.addSeries(series3);
		return data;
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return A sample chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createPolarChart(
							"Polar Chart Demo", dataset, true, true, false
							);
		final PolarPlot plot = (PolarPlot) chart.getPlot();
		final DefaultPolarItemRenderer renderer = (DefaultPolarItemRenderer) plot.getRenderer();
		renderer.setSeriesFilled(2, true);
		return chart;
	}

	/**
	 * Creates a series containing random data.
	 * 
	 * @param name
	 *           the series name.
	 * @param baseRadius
	 *           the base radius.
	 * @param thetaInc
	 *           the angle increment.
	 * @return The series.
	 */
	private static XYSeries createRandomData(final String name,
															final double baseRadius,
															final double thetaInc) {
		final XYSeries series = new XYSeries(name);
		for (double theta = 0.0; theta < 360.0; theta += thetaInc) {
			final double radius = baseRadius * (1.0 + Math.random());
			series.add(theta, radius);
		}
		return series;
	}

	/**
	 * Main program that creates a thermometer and places it into
	 * a JFrame.
	 * 
	 * @param argv
	 *           Command line arguements - none used.
	 */
	public static void main(final String[] argv) {

		final PolarChartDemo demo = new PolarChartDemo("Polar Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
