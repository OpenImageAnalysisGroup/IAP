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
 * WaterTemperatureDemo.java
 * -------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: WaterTemperatureDemo.java,v 1.1 2011-01-31 09:01:55 klukas Exp $
 * Changes
 * -------
 * 17-Jan-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * This demo shows a plot of water temperature at various depths.
 */
public class WaterTemperatureDemo extends ApplicationFrame {

	/**
	 * A demonstration application showing an XY series containing a null value.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public WaterTemperatureDemo(final String title) {

		super(title);
		final XYDataset dataset = createDataset();

		final NumberAxis rangeAxis = new NumberAxis("Temperature");
		rangeAxis.setRange(-0.55, -0.15);

		final NumberAxis domainAxis = new NumberAxis("Depth");
		domainAxis.setInverted(true);
		domainAxis.setRange(0.0, 35.0);
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		final XYItemRenderer renderer = new StandardXYItemRenderer();

		final XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
		plot.setOrientation(PlotOrientation.HORIZONTAL);
		final JFreeChart chart = new JFreeChart("Water Temperature By Depth", plot);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return The dataset.
	 */
	private XYDataset createDataset() {

		final XYSeries series = new XYSeries("Zone 1");
		series.add(1.0, -0.5);
		series.add(5.0, -0.5);
		series.add(10.0, -0.4);
		series.add(15.0, -0.4);
		series.add(20.0, -0.3);
		series.add(25.0, -0.3);
		series.add(30.0, -0.2);
		series.add(35.0, -0.2);

		return new XYSeriesCollection(series);

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

		final WaterTemperatureDemo demo = new WaterTemperatureDemo("Water Temperature Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
