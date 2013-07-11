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
 * --------------------------------
 * ChartPanelSerializationTest.java
 * --------------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ChartPanelSerializationTest.java,v 1.1 2011-01-31 09:01:50 klukas Exp $
 * Changes
 * -------
 * 20-May-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;
import org.jfree.util.Log;
import org.jfree.util.PrintStreamLogTarget;

/**
 * This demo tests the ability to serialize/deserialize a {@link ChartPanel}. The
 * basics are working, but more testing is needed to iron out the remaining bugs.
 */
public class ChartPanelSerializationTest extends ApplicationFrame {

	/**
	 * A demonstration application showing how to create a simple time series chart. This
	 * example uses monthly data.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public ChartPanelSerializationTest(final String title) {

		super(title);
		final XYDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel1 = new ChartPanel(chart);
		chartPanel1.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel1.setMouseZoomable(true, false);

		ChartPanel chartPanel2 = null;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(buffer);
			out.writeObject(chartPanel1);
			out.close();

			ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
			chartPanel2 = (ChartPanel) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		setContentPane(chartPanel2);

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
							"Legal & General Unit Trust Prices",
							"Date", "Price Per Unit",
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
			rr.setItemLabelsVisible(true);
		}

		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

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
	private XYDataset createDataset() {

		final TimeSeries s1 = new TimeSeries("L&G European Index Trust", Month.class);
		s1.add(new Month(2, 2001), 181.8);
		s1.add(new Month(3, 2001), 167.3);
		s1.add(new Month(4, 2001), 153.8);
		s1.add(new Month(5, 2001), 167.6);
		s1.add(new Month(6, 2001), 158.8);
		s1.add(new Month(7, 2001), 148.3);
		s1.add(new Month(8, 2001), 153.9);
		s1.add(new Month(9, 2001), 142.7);
		s1.add(new Month(10, 2001), 123.2);
		s1.add(new Month(11, 2001), 131.8);
		s1.add(new Month(12, 2001), 139.6);
		s1.add(new Month(1, 2002), 142.9);
		s1.add(new Month(2, 2002), 138.7);
		s1.add(new Month(3, 2002), 137.3);
		s1.add(new Month(4, 2002), 143.9);
		s1.add(new Month(5, 2002), 139.8);
		s1.add(new Month(6, 2002), 137.0);
		s1.add(new Month(7, 2002), 132.8);

		final TimeSeries s2 = new TimeSeries("L&G UK Index Trust", Month.class);
		s2.add(new Month(2, 2001), 129.6);
		s2.add(new Month(3, 2001), 123.2);
		s2.add(new Month(4, 2001), 117.2);
		s2.add(new Month(5, 2001), 124.1);
		s2.add(new Month(6, 2001), 122.6);
		s2.add(new Month(7, 2001), 119.2);
		s2.add(new Month(8, 2001), 116.5);
		s2.add(new Month(9, 2001), 112.7);
		s2.add(new Month(10, 2001), 101.5);
		s2.add(new Month(11, 2001), 106.1);
		s2.add(new Month(12, 2001), 110.3);
		s2.add(new Month(1, 2002), 111.7);
		s2.add(new Month(2, 2002), 111.0);
		s2.add(new Month(3, 2002), 109.6);
		s2.add(new Month(4, 2002), 113.2);
		s2.add(new Month(5, 2002), 111.6);
		s2.add(new Month(6, 2002), 108.8);
		s2.add(new Month(7, 2002), 101.6);

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(s1);
		dataset.addSeries(s2);

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

		Log.getInstance().addTarget(new PrintStreamLogTarget());
		final ChartPanelSerializationTest demo = new ChartPanelSerializationTest(
							"Serialization Test"
							);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
