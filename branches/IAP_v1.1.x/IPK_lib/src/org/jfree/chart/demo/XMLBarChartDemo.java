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
 * XMLBarChartDemo.java
 * --------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XMLBarChartDemo.java,v 1.1 2011-01-31 09:01:53 klukas Exp $
 * Changes
 * -------
 * 28-Oct-2002 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.CategoryDataset;
import org.jfree.data.xml.DatasetReader;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a bar chart using data from an
 * XML data file.
 */
public class XMLBarChartDemo extends ApplicationFrame {

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public XMLBarChartDemo(final String title) {

		super(title);

		// create a dataset...
		CategoryDataset dataset = null;
		final URL url = getClass().getResource("/org/jfree/chart/demo/categorydata.xml");

		try {
			final InputStream in = url.openStream();
			dataset = DatasetReader.readCategoryDatasetFromXML(in);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

		// create the chart...
		final JFreeChart chart = ChartFactory.createBarChart(
							"Bar Chart", // chart title
				"Domain",
							"Range",
							dataset, // data
				PlotOrientation.VERTICAL,
							true, // include legend
				true,
							false
							);

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.yellow);

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

		final XMLBarChartDemo demo = new XMLBarChartDemo("XML Bar Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
