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
 * TimeSeriesDemo9.java
 * --------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimeSeriesDemo9.java,v 1.1 2011-01-31 09:01:51 klukas Exp $
 * Changes
 * -------
 * 11-Feb-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * An example of a time series chart.
 */
public class TimeSeriesDemo9 extends ApplicationFrame {

	/**
	 * A demonstration application showing how to create a simple time series chart. This
	 * example uses monthly data.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimeSeriesDemo9(final String title) {

		super(title);

		// create a title...
		final String chartTitle = "Test";
		final XYDataset dataset = createDataset();

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							chartTitle,
							"Date",
							"Price Per Unit",
							dataset,
							true,
							true,
							false
							);

		final StandardLegend sl = (StandardLegend) chart.getLegend();
		sl.setDisplaySeriesShapes(true);

		final XYPlot plot = chart.getXYPlot();
		final XYItemRenderer r = plot.getRenderer();
		if (r instanceof StandardXYItemRenderer) {
			final StandardXYItemRenderer renderer = (StandardXYItemRenderer) r;
			renderer.setPlotShapes(true);
			renderer.setShapesFilled(true);
			renderer.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
			renderer.setSeriesShape(1, new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0));
			final GeneralPath s2 = new GeneralPath();
			s2.moveTo(0.0f, -3.0f);
			s2.lineTo(3.0f, 3.0f);
			s2.lineTo(-3.0f, 3.0f);
			s2.closePath();
			renderer.setSeriesShape(2, s2);
			final GeneralPath s3 = new GeneralPath();
			s3.moveTo(-1.0f, -3.0f);
			s3.lineTo(1.0f, -3.0f);
			s3.lineTo(1.0f, -1.0f);
			s3.lineTo(3.0f, -1.0f);
			s3.lineTo(3.0f, 1.0f);
			s3.lineTo(1.0f, 1.0f);
			s3.lineTo(1.0f, 3.0f);
			s3.lineTo(-1.0f, 3.0f);
			s3.lineTo(-1.0f, 1.0f);
			s3.lineTo(-3.0f, 1.0f);
			s3.lineTo(-3.0f, -1.0f);
			s3.lineTo(-1.0f, -1.0f);
			s3.closePath();
			renderer.setSeriesShape(3, s3);
		}

		plot.getDomainAxis().setVisible(false);
		plot.getRangeAxis().setVisible(false);
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
	 * Creates a sample dataset.
	 * 
	 * @return The dataset.
	 */
	public XYDataset createDataset() {

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		for (int i = 0; i < 4; i++) {
			dataset.addSeries(createTimeSeries(i, 10));
		}
		return dataset;

	}

	/**
	 * Creates a time series containing random daily data.
	 * 
	 * @param series
	 *           the series index.
	 * @param count
	 *           the number of items for the series.
	 * @return the dataset.
	 */
	public TimeSeries createTimeSeries(final int series, final int count) {

		final TimeSeries result = new TimeSeries("Series " + series, Day.class);

		Day start = new Day();
		for (int i = 0; i < count; i++) {
			result.add(start, Math.random());
			start = (Day) start.next();
		}

		return result;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final TimeSeriesDemo9 demo = new TimeSeriesDemo9("Time Series Demo 9");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
