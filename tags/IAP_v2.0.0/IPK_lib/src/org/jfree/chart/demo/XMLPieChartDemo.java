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
 * XMLPieChartDemo.java
 * --------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XMLPieChartDemo.java,v 1.1 2011-01-31 09:01:52 klukas Exp $
 * Changes
 * -------
 * 20-Nov-2002 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieItemLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.PieDataset;
import org.jfree.data.xml.DatasetReader;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a pie chart from data in an
 * XML file.
 */
public class XMLPieChartDemo extends ApplicationFrame {

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public XMLPieChartDemo(final String title) {

		super(title);

		// create a dataset...
		PieDataset dataset = null;
		final URL url = getClass().getResource("/org/jfree/chart/demo/piedata.xml");

		try {
			final InputStream in = url.openStream();
			dataset = DatasetReader.readPieDatasetFromXML(in);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

		// create the chart...
		final JFreeChart chart = ChartFactory.createPieChart(
							"Pie Chart Demo 1", // chart title
				dataset, // data
				true, // include legend
				true,
							false
							);

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.yellow);
		final PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(new StandardPieItemLabelGenerator(
							"{0} = {2}", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()
							));
		plot.setNoDataMessage("No data available");

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

		final XMLPieChartDemo demo = new XMLPieChartDemo("XML Pie Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
