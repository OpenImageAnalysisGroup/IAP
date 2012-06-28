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
 * ImageMapDemo7.java
 * ------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ImageMapDemo7.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes
 * -------
 * 21-Apr-2004 : Version 1 (DG);
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
import org.jfree.chart.Legend;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.XYDataset;

/**
 * A demo showing how to create an HTML image map for a scatter plot.
 */
public class ImageMapDemo7 {

	/**
	 * Default constructor.
	 */
	public ImageMapDemo7() {
		super();
	}

	/**
	 * Starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final XYDataset data = new SampleXYDataset2();
		final JFreeChart chart = ChartFactory.createScatterPlot(
							"Scatter Plot Demo",
							"X", "Y",
							data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);
		final Legend legend = chart.getLegend();
		if (legend instanceof StandardLegend) {
			final StandardLegend sl = (StandardLegend) legend;
			sl.setDisplaySeriesShapes(true);
		}
		final NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
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
			final File file1 = new File("scatter100.png");
			ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);

			// write an HTML page incorporating the image with an image map
			final File file2 = new File("scatter100.html");
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(file2));
			final PrintWriter writer = new PrintWriter(out);
			writer.println("<HTML>");
			writer.println("<HEAD><TITLE>JFreeChart Image Map Demo</TITLE></HEAD>");
			writer.println("<BODY>");
			ChartUtilities.writeImageMap(writer, "chart", info);
			writer.println("<IMG SRC=\"scatter100.png\" "
									+ "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\">");
			writer.println("</BODY>");
			writer.println("</HTML>");
			writer.close();

		} catch (IOException e) {
			System.out.println(e.toString());
		}

	}

}
