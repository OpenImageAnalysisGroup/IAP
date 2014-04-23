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
 * TimeSeriesDemo13.java
 * ---------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimeSeriesDemo13.java,v 1.1 2011-01-31 09:01:43 klukas Exp $
 * Changes
 * -------
 * 11-Feb-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * This demo shows two charts that use weekly data. A custom tick unit collection is defined to
 * control the domain axis formatting.
 */
public class TimeSeriesDemo13 extends ApplicationFrame {

	/**
	 * Creates a new demo instance.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimeSeriesDemo13(final String title) {

		super(title);

		final XYDataset dataset1 = createDataset(26);
		final JFreeChart chart1 = createChart(dataset1);
		final ChartPanel chartPanel1 = new ChartPanel(chart1);

		final XYDataset dataset2 = createDataset(1);
		final JFreeChart chart2 = createChart(dataset2);
		final ChartPanel chartPanel2 = new ChartPanel(chart2);

		final JTabbedPane tabs = new JTabbedPane();
		tabs.add("Chart 1", chartPanel1);
		tabs.add("Chart 2", chartPanel2);
		final JPanel content = new JPanel(new BorderLayout());
		content.setPreferredSize(new java.awt.Dimension(500, 270));
		content.add(tabs);
		setContentPane(content);

	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           a dataset.
	 * @return A chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							"Weekly Data",
							"Date",
							"Value",
							dataset,
							true,
							true,
							false
							);

		chart.setBackgroundPaint(Color.white);

		final StandardLegend sl = (StandardLegend) chart.getLegend();
		sl.setDisplaySeriesShapes(true);

		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		final XYItemRenderer renderer = plot.getRenderer();
		if (renderer instanceof StandardXYItemRenderer) {
			final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
			rr.setPlotShapes(true);
			rr.setShapesFilled(true);
		}

		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		final TickUnits standardUnits = new TickUnits();
		standardUnits.add(
							new DateTickUnit(DateTickUnit.DAY, 1, new SimpleDateFormat("MMM dd ''yy"))
							);
		standardUnits.add(
							new DateTickUnit(DateTickUnit.DAY, 7, new SimpleDateFormat("MMM dd ''yy"))
							);
		standardUnits.add(
							new DateTickUnit(DateTickUnit.MONTH, 1, new SimpleDateFormat("MMM ''yy"))
							);
		axis.setStandardTickUnits(standardUnits);

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
	 * Creates a dataset containing random values at weekly intervals.
	 * 
	 * @param items
	 *           the number of items in the dataset.
	 * @return the dataset.
	 */
	private XYDataset createDataset(final int items) {

		final TimeSeries s1 = new TimeSeries("Random Data", Week.class);
		RegularTimePeriod t = new Week();
		double v = 100.0;
		for (int i = 0; i < items; i++) {
			s1.add(t, v);
			v = v * (1 + ((Math.random() - 0.499) / 100.0));
			t = t.next();
		}

		final TimeSeriesCollection dataset = new TimeSeriesCollection(s1);
		dataset.setDomainIsPointsInTime(true);

		return dataset;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final TimeSeriesDemo13 demo = new TimeSeriesDemo13("Time Series Demo 13");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
