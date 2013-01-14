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
 * ------------------------
 * PopulationChartDemo.java
 * ------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: PopulationChartDemo.java,v 1.1 2011-01-31 09:01:56 klukas Exp $
 * Changes
 * -------
 * 23-Apr-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultKeyedValues2DDataset;
import org.jfree.data.KeyedValues2DDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A population pyramid demo.
 */
public class PopulationChartDemo extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public PopulationChartDemo(final String title) {

		super(title);
		final CategoryDataset dataset = createDataset();

		// create the chart...
		final JFreeChart chart = ChartFactory.createStackedBarChart(
							"Population Chart Demo",
							"Age Group", // domain axis label
				"Population (millions)", // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL,
							true, // include legend
				true, // tooltips
				false // urls
				);

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
	 * Creates a dataset.
	 * 
	 * @return A dataset.
	 */
	private KeyedValues2DDataset createDataset() {

		final DefaultKeyedValues2DDataset data = new DefaultKeyedValues2DDataset();
		data.addValue(-6.0, "Male", "70+");
		data.addValue(-8.0, "Male", "60-69");
		data.addValue(-11.0, "Male", "50-59");
		data.addValue(-13.0, "Male", "40-49");
		data.addValue(-14.0, "Male", "30-39");
		data.addValue(-15.0, "Male", "20-29");
		data.addValue(-19.0, "Male", "10-19");
		data.addValue(-21.0, "Male", "0-9");
		data.addValue(10.0, "Female", "70+");
		data.addValue(12.0, "Female", "60-69");
		data.addValue(13.0, "Female", "50-59");
		data.addValue(14.0, "Female", "40-49");
		data.addValue(15.0, "Female", "30-39");
		data.addValue(17.0, "Female", "20-29");
		data.addValue(19.0, "Female", "10-19");
		data.addValue(20.0, "Female", "0-9");
		return data;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final PopulationChartDemo demo = new PopulationChartDemo("Population Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
