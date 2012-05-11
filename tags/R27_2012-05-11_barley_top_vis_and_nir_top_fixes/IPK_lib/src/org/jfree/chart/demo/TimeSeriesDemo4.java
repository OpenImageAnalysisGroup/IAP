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
 * TimeSeriesDemo4.java
 * --------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimeSeriesDemo4.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes
 * -------
 * 08-Apr-2002 : Version 1 (DG);
 * 25-Jun-2002 : Removed unnecessary import (DG);
 * 28-Aug-2002 : Centered frame on screen (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Insets;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * An example of a time series chart using hourly data and including a null value. The plot
 * has an image set for the background, and a blue range marker is added to the plot.
 */
public class TimeSeriesDemo4 extends ApplicationFrame {

	/**
	 * A demonstration application showing a quarterly time series containing a null value.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimeSeriesDemo4(final String title) {

		super(title);
		final TimeSeries series = new TimeSeries("Random Data", Hour.class);
		final Day today = new Day();
		series.add(new Hour(1, today), 500.2);
		series.add(new Hour(2, today), 694.1);
		series.add(new Hour(3, today), 734.4);
		series.add(new Hour(4, today), 453.2);
		series.add(new Hour(7, today), 500.2);
		series.add(new Hour(8, today), null);
		series.add(new Hour(12, today), 734.4);
		series.add(new Hour(16, today), 453.2);
		final TimeSeriesCollection dataset = new TimeSeriesCollection(series);

		// create a title with Unicode characters (currency symbols in this case)...
		final String chartTitle = "\u20A2\u20A2\u20A3\u20A4\u20A5\u20A6\u20A7\u20A8\u20A9\u20AA";
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							chartTitle,
							"Time",
							"Value",
							dataset,
							true,
							true,
							false
							);

		final XYPlot plot = chart.getXYPlot();
		plot.setInsets(new Insets(0, 0, 0, 20));
		final Marker marker = new ValueMarker(700.0);
		marker.setPaint(Color.blue);
		marker.setAlpha(0.8f);
		plot.addRangeMarker(marker);
		plot.setBackgroundPaint(null);
		plot.setBackgroundImage(JFreeChart.INFO.getLogo());
		final XYItemRenderer renderer = plot.getRenderer();
		if (renderer instanceof StandardXYItemRenderer) {
			final StandardXYItemRenderer r = (StandardXYItemRenderer) renderer;
			r.setPlotShapes(true);
			r.setShapesFilled(true);
		}
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMouseZoomable(true, false);
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

		final TimeSeriesDemo4 demo = new TimeSeriesDemo4("Time Series Demo 4");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
