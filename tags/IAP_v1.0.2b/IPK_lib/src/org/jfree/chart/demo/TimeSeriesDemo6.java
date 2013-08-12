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
 * TimeSeriesDemo6.java
 * --------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimeSeriesDemo6.java,v 1.1 2011-01-31 09:01:54 klukas Exp $
 * Changes
 * -------
 * 08-Apr-2002 : Version 1 (DG);
 * 25-Jun-2002 : Removed unnecessary import (DG);
 */

package org.jfree.chart.demo;

import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A time series chart with all zero data. When the data range is zero, you may want to modify
 * the default behaviour of the range axis.
 */
public class TimeSeriesDemo6 extends ApplicationFrame {

	/**
	 * Creates a new instance.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimeSeriesDemo6(final String title) {

		super(title);
		final XYDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return a chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							"Time Series Demo 6",
							"Date",
							"Value",
							dataset,
							true,
							true,
							false
							);

		final XYPlot plot = chart.getXYPlot();
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
		final ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setAutoRangeMinimumSize(1.0);
		return chart;

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
	 * Creates a dataset, consisting of two series of monthly data.
	 * 
	 * @return the dataset.
	 */
	public XYDataset createDataset() {

		final double value = 0.0;
		final TimeSeries s1 = new TimeSeries("Series 1", Month.class);
		s1.add(new Month(2, 2001), value);
		s1.add(new Month(3, 2001), value);
		s1.add(new Month(4, 2001), value);
		s1.add(new Month(5, 2001), value);
		s1.add(new Month(6, 2001), value);
		s1.add(new Month(7, 2001), value);
		s1.add(new Month(8, 2001), value);
		s1.add(new Month(9, 2001), value);
		s1.add(new Month(10, 2001), value);
		s1.add(new Month(11, 2001), value);
		s1.add(new Month(12, 2001), value);
		s1.add(new Month(1, 2002), value);
		s1.add(new Month(2, 2002), value);
		s1.add(new Month(3, 2002), value);
		s1.add(new Month(4, 2002), value);
		s1.add(new Month(5, 2002), value);
		s1.add(new Month(6, 2002), value);
		s1.add(new Month(7, 2002), value);

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(s1);

		return dataset;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final TimeSeriesDemo6 demo = new TimeSeriesDemo6("Time Series Demo 6");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
