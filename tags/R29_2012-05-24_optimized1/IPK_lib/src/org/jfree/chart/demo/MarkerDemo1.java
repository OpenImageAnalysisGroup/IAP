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
 * ----------------
 * MarkerDemo1.java
 * ----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MarkerDemo1.java,v 1.1 2011-01-31 09:01:58 klukas Exp $
 * Changes
 * -------
 * 21-May-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

/**
 * A demo application.
 */
public class MarkerDemo1 extends ApplicationFrame {

	/**
	 * Creates a new instance.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MarkerDemo1(final String title) {

		super(title);
		final XYDataset data = createDataset();
		final JFreeChart chart = createChart(data);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setVerticalZoom(true);
		chartPanel.setHorizontalZoom(true);
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param data
	 *           the sample data.
	 * @return A configured chart.
	 */
	private JFreeChart createChart(final XYDataset data) {

		final JFreeChart chart = ChartFactory.createScatterPlot(
							"Marker Demo 1",
							"X",
							"Y",
							data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);
		chart.getLegend().setAnchor(Legend.EAST);

		// customise...
		final XYPlot plot = chart.getXYPlot();
		plot.getRenderer().setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());

		// set axis margins to allow space for marker labels...
		final DateAxis domainAxis = new DateAxis("Time");
		domainAxis.setUpperMargin(0.50);
		plot.setDomainAxis(domainAxis);

		final ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setUpperMargin(0.30);
		rangeAxis.setLowerMargin(0.50);

		// add a labelled marker for the bid start price...
		final Marker start = new ValueMarker(200.0);
		start.setPaint(Color.green);
		start.setLabel("Bid Start Price");
		start.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
		start.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		plot.addRangeMarker(start);

		// add a labelled marker for the target price...
		final Marker target = new ValueMarker(175.0);
		target.setPaint(Color.red);
		target.setLabel("Target Price");
		target.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		target.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
		plot.addRangeMarker(target);

		// add a labelled marker for the original closing time...
		final Hour hour = new Hour(2, new Day(22, 5, 2003));
		double millis = hour.getFirstMillisecond();
		final Marker originalEnd = new ValueMarker(millis);
		originalEnd.setPaint(Color.orange);
		originalEnd.setLabel("Original Close (02:00)");
		originalEnd.setLabelAnchor(RectangleAnchor.TOP_LEFT);
		originalEnd.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		plot.addDomainMarker(originalEnd);

		// add a labelled marker for the current closing time...
		final Minute min = new Minute(15, hour);
		millis = min.getFirstMillisecond();
		final Marker currentEnd = new ValueMarker(millis);
		currentEnd.setPaint(Color.red);
		currentEnd.setLabel("Close Date (02:15)");
		currentEnd.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		currentEnd.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		plot.addDomainMarker(currentEnd);

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

		// label the best bid with an arrow and label...
		final Hour h = new Hour(2, new Day(22, 5, 2003));
		final Minute m = new Minute(10, h);
		millis = m.getFirstMillisecond();
		final CircleDrawer cd = new CircleDrawer(Color.red, new BasicStroke(1.0f), null);
		final XYAnnotation bestBid = new XYDrawableAnnotation(millis, 163.0, 11, 11, cd);
		plot.addAnnotation(bestBid);
		final XYPointerAnnotation pointer = new XYPointerAnnotation("Best Bid", millis, 163.0,
																					3.0 * Math.PI / 4.0);
		pointer.setBaseRadius(35.0);
		pointer.setTipRadius(10.0);
		pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
		pointer.setPaint(Color.blue);
		pointer.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
		plot.addAnnotation(pointer);

		return chart;

	}

	/**
	 * Returns a sample dataset.
	 * 
	 * @return A sample dataset.
	 */
	private XYDataset createDataset() {

		final TimeSeriesCollection result = new TimeSeriesCollection();
		result.addSeries(createSupplier1Bids());
		result.addSeries(createSupplier2Bids());
		return result;

	}

	/**
	 * Returns a sample data series (for supplier 1).
	 * 
	 * @return A sample data series.
	 */
	private TimeSeries createSupplier1Bids() {

		final Hour hour = new Hour(1, new Day(22, 5, 2003));

		final TimeSeries series1 = new TimeSeries("Supplier 1", Minute.class);
		series1.add(new Minute(13, hour), 200.0);
		series1.add(new Minute(14, hour), 195.0);
		series1.add(new Minute(45, hour), 190.0);
		series1.add(new Minute(46, hour), 188.0);
		series1.add(new Minute(47, hour), 185.0);
		series1.add(new Minute(52, hour), 180.0);

		return series1;

	}

	/**
	 * Returns a sample data series (for supplier 2).
	 * 
	 * @return A sample data series.
	 */
	private TimeSeries createSupplier2Bids() {

		final Hour hour1 = new Hour(1, new Day(22, 5, 2003));
		final Hour hour2 = (Hour) hour1.next();

		final TimeSeries series2 = new TimeSeries("Supplier 2", Minute.class);
		series2.add(new Minute(25, hour1), 185.0);
		series2.add(new Minute(0, hour2), 175.0);
		series2.add(new Minute(5, hour2), 170.0);
		series2.add(new Minute(6, hour2), 168.0);
		series2.add(new Minute(9, hour2), 165.0);
		series2.add(new Minute(10, hour2), 163.0);

		return series2;

	}

	/**
	 * Starting point for the demo application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final MarkerDemo1 demo = new MarkerDemo1("Marker Demo 1");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
