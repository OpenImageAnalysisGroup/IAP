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
 * ImageMapDemo3.java
 * ------------------
 * (C) Copyright 2002-2004, by Richard Atkinson and Contributors.
 * Original Author: Richard Atkinson (richard_c_atkinson@ntlworld.com);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: ImageMapDemo3.java,v 1.1 2011-01-31 09:01:53 klukas Exp $
 * Changes
 * -------
 * 18-Jul-2002 : Version 1 (RA);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */
package org.jfree.chart.demo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;

/**
 * A demo showing the construction of HTML image maps for a time series chart.
 * 
 * @author Richard Atkinson
 */
public class ImageMapDemo3 {

	/**
	 * Default constructor.
	 */
	public ImageMapDemo3() {
		super();
	}

	/**
	 * Starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 * @throws ParseException
	 *            if there is a problem parsing dates.
	 */
	public static void main(final String[] args) throws ParseException {

		// Create a sample dataset
		final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		final XYSeries dataSeries = new XYSeries("Curve data");
		final ArrayList toolTips = new ArrayList();
		dataSeries.add(sdf.parse("01-Jul-2002").getTime(), 5.22);
		toolTips.add("1D - 5.22");
		dataSeries.add(sdf.parse("02-Jul-2002").getTime(), 5.18);
		toolTips.add("2D - 5.18");
		dataSeries.add(sdf.parse("03-Jul-2002").getTime(), 5.23);
		toolTips.add("3D - 5.23");
		dataSeries.add(sdf.parse("04-Jul-2002").getTime(), 5.15);
		toolTips.add("4D - 5.15");
		dataSeries.add(sdf.parse("05-Jul-2002").getTime(), 5.22);
		toolTips.add("5D - 5.22");
		dataSeries.add(sdf.parse("06-Jul-2002").getTime(), 5.25);
		toolTips.add("6D - 5.25");
		dataSeries.add(sdf.parse("07-Jul-2002").getTime(), 5.31);
		toolTips.add("7D - 5.31");
		dataSeries.add(sdf.parse("08-Jul-2002").getTime(), 5.36);
		toolTips.add("8D - 5.36");
		final XYSeriesCollection xyDataset = new XYSeriesCollection(dataSeries);
		final CustomXYToolTipGenerator ttg = new CustomXYToolTipGenerator();
		ttg.addToolTipSeries(toolTips);

		// Create the chart
		final StandardXYURLGenerator urlg = new StandardXYURLGenerator("xy_details.jsp");
		final ValueAxis timeAxis = new DateAxis("");
		final NumberAxis valueAxis = new NumberAxis("");
		valueAxis.setAutoRangeIncludesZero(false); // override default
		final XYPlot plot = new XYPlot(xyDataset, timeAxis, valueAxis, null);
		final StandardXYItemRenderer sxyir = new StandardXYItemRenderer(
							StandardXYItemRenderer.LINES + StandardXYItemRenderer.SHAPES,
							ttg, urlg);
		sxyir.setShapesFilled(true);
		plot.setRenderer(sxyir);
		final JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
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
			final File file1 = new File("xychart100.png");
			ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);

			// write an HTML page incorporating the image with an image map
			final File file2 = new File("xychart100.html");
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(file2));
			final PrintWriter writer = new PrintWriter(out);
			writer.println("<HTML>");
			writer.println("<HEAD><TITLE>JFreeChart Image Map Demo</TITLE></HEAD>");
			writer.println("<BODY>");
			ChartUtilities.writeImageMap(writer, "chart", info);
			writer.println("<IMG SRC=\"xychart100.png\" "
									+ "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\">");
			writer.println("</BODY>");
			writer.println("</HTML>");
			writer.close();

		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return;
	}
}
