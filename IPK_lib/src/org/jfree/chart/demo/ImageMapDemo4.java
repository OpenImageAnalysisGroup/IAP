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
 * ------------------
 * ImageMapDemo4.java
 * ------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Richard Atkinson (richard_c_atkinson@ntlworld.com);
 * $Id: ImageMapDemo4.java,v 1.1 2011-01-31 09:01:48 klukas Exp $
 * Changes
 * -------
 * 22-Dec-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer3D;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetUtilities;

/**
 * A demo showing how to create an HTML image map for a 3D bar chart.
 */
public class ImageMapDemo4 {

	/**
	 * Default constructor.
	 */
	public ImageMapDemo4() {
		super();
	}

	/**
	 * Starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		// create a chart
		final double[][] data = new double[][] {
							{ 56.0, -12.0, 34.0, 76.0, 56.0, 100.0, 67.0, 45.0 },
							{ 37.0, 45.0, 67.0, 25.0, 34.0, 34.0, 100.0, 53.0 },
							{ 43.0, 54.0, 34.0, 34.0, 87.0, 64.0, 73.0, 12.0 }
			};
		final CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
							"Series ", "Type ", data
							);

		JFreeChart chart = null;
		final boolean drilldown = true;

		if (drilldown) {
			final CategoryAxis3D categoryAxis = new CategoryAxis3D("Category");
			final ValueAxis valueAxis = new NumberAxis3D("Value");
			final BarRenderer3D renderer = new BarRenderer3D();
			renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
			renderer.setItemURLGenerator(new StandardCategoryURLGenerator("bar_chart_detail.jsp"));
			final CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
			plot.setOrientation(PlotOrientation.VERTICAL);
			chart = new JFreeChart("Bar Chart", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		} else {
			chart = ChartFactory.createBarChart3D(
								"Bar Chart", // chart title
					"Category", // domain axis label
					"Value", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL,
								true, // include legend
					true,
								false
								);
		}
		chart.setBackgroundPaint(java.awt.Color.white);

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

		// save it to an image
		try {
			final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
			final File file1 = new File("barchart101.png");
			ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);

			// write an HTML page incorporating the image with an image map
			final File file2 = new File("barchart101.html");
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(file2));
			final PrintWriter writer = new PrintWriter(out);
			writer.println("<HTML>");
			writer.println("<HEAD><TITLE>JFreeChart Image Map Demo</TITLE></HEAD>");
			writer.println("<BODY>");
			ChartUtilities.writeImageMap(writer, "chart", info);
			writer.println("<IMG SRC=\"barchart100.png\" "
									+ "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\">");
			writer.println("</BODY>");
			writer.println("</HTML>");
			writer.close();

		} catch (IOException e) {
			System.out.println(e.toString());
		}

	}

}
