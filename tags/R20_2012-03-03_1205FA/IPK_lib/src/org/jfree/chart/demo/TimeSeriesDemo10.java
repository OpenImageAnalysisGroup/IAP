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
 * ---------------------
 * TimeSeriesDemo10.java
 * ---------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimeSeriesDemo10.java,v 1.1 2011-01-31 09:01:45 klukas Exp $
 * Changes
 * -------
 * 21-Feb-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo showing a time series with per minute data.
 */
public class TimeSeriesDemo10 extends ApplicationFrame {

	/**
	 * A demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimeSeriesDemo10(final String title) {

		super(title);

		final TimeSeries series = new TimeSeries("Per Minute Data", Minute.class);
		final Hour hour = new Hour();
		series.add(new Minute(1, hour), 10.2);
		series.add(new Minute(3, hour), 17.3);
		series.add(new Minute(9, hour), 14.6);
		series.add(new Minute(11, hour), 11.9);
		series.add(new Minute(15, hour), 13.5);
		series.add(new Minute(19, hour), 10.9);
		final TimeSeriesCollection dataset = new TimeSeriesCollection(series);
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							"Time Series Demo 10",
							"Time",
							"Value",
							dataset,
							true,
							true,
							false
							);
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

		final TimeSeriesDemo10 demo = new TimeSeriesDemo10("Time Series Demo 10");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
