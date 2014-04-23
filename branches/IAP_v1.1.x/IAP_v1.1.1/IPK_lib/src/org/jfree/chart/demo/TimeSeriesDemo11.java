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
 * TimeSeriesDemo11.java
 * ---------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimeSeriesDemo11.java,v 1.1 2011-01-31 09:01:47 klukas Exp $
 * Changes
 * -------
 * 12-Aug-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.date.SerialDate;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * An example of....
 */
public class TimeSeriesDemo11 extends ApplicationFrame {

	/**
	 * A demonstration application showing how to...
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimeSeriesDemo11(final String title) {

		super(title);
		final JPanel panel = new JPanel(new GridLayout(2, 2));
		panel.setPreferredSize(new java.awt.Dimension(800, 600));

		final Day today = new Day();
		final XYDataset dataset = createDataset("Series 1", 100.0, today, 365);

		final JFreeChart chart1 = createChart("Chart 1 : 1 Year", dataset);
		final ChartPanel chartPanel1 = new ChartPanel(chart1);
		panel.add(chartPanel1);

		final JFreeChart chart2 = createChart("Chart 2 : 6 Months", dataset);
		final SerialDate t = today.getSerialDate();
		final SerialDate t6m = SerialDate.addMonths(-6, t);
		final Day sixMonthsAgo = new Day(t6m);
		final DateAxis axis2 = (DateAxis) chart2.getXYPlot().getDomainAxis();
		axis2.setRange(sixMonthsAgo.getStart(), today.getEnd());
		final ChartPanel chartPanel2 = new ChartPanel(chart2);
		panel.add(chartPanel2);

		final JFreeChart chart3 = createChart("Chart 3 : 3 Months", dataset);
		final SerialDate t3m = SerialDate.addMonths(-3, t);
		final Day threeMonthsAgo = new Day(t3m);
		final DateAxis axis3 = (DateAxis) chart3.getXYPlot().getDomainAxis();
		axis3.setRange(threeMonthsAgo.getStart(), today.getEnd());
		final ChartPanel chartPanel3 = new ChartPanel(chart3);
		panel.add(chartPanel3);

		final JFreeChart chart4 = createChart("Chart 4 : 1 Month", dataset);
		final SerialDate t1m = SerialDate.addMonths(-1, t);
		final Day oneMonthsAgo = new Day(t1m);
		final DateAxis axis4 = (DateAxis) chart4.getXYPlot().getDomainAxis();
		axis4.setRange(oneMonthsAgo.getStart(), today.getEnd());
		final ChartPanel chartPanel4 = new ChartPanel(chart4);
		panel.add(chartPanel4);

		setContentPane(panel);

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
	 * Creates the demo chart.
	 * 
	 * @param title
	 *           the title.
	 * @param dataset
	 *           the dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final String title, final XYDataset dataset) {

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							title,
							"Date",
							"Price",
							dataset,
							true,
							true,
							false
							);

		chart.setBackgroundPaint(Color.white);
		final XYPlot plot = chart.getXYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));

		final StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
		renderer.setPaint(Color.blue);

		return chart;

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @param name
	 *           the dataset name.
	 * @param base
	 *           the starting value.
	 * @param start
	 *           the starting period.
	 * @param count
	 *           the number of values to generate.
	 * @return The dataset.
	 */
	private XYDataset createDataset(final String name,
												final double base,
												final RegularTimePeriod start,
												final int count) {

		final TimeSeries series = new TimeSeries(name, start.getClass());
		RegularTimePeriod period = start;
		double value = base;
		for (int i = 0; i < count; i++) {
			series.add(period, value);
			period = period.previous();
			value = value * (1 + (Math.random() - 0.495) / 10.0);
		}

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(series);

		return dataset;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final TimeSeriesDemo11 demo = new TimeSeriesDemo11("Time Series Demo 11");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
