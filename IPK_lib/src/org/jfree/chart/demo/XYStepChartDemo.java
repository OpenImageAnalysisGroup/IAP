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
 * XYStepChartDemo.java
 * --------------------
 * (C) Copyright 2002-2004, by Roger Studner and Contributors.
 * Original Author: Roger Studner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: XYStepChartDemo.java,v 1.1 2011-01-31 09:01:48 klukas Exp $
 * Changes
 * -------
 * 13-May-2002 : Version 1, contributed by Roger Studner (DG);
 * 11-Oct-2002 : Moved create method to ChartFactory class, and fixed issues reported by
 * Checkstyle (DG);
 */
package org.jfree.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.XYDataset;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.date.DateUtilities;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration of the {@link org.jfree.chart.renderer.XYStepRenderer} class.
 * 
 * @author Roger Studner
 */
public class XYStepChartDemo {

	/** A frame for displaying the chart. */
	private ChartFrame frame = null;

	/**
	 * Displays a sample chart in its own frame.
	 */
	private void displayChart() {

		if (this.frame == null) {

			// create a default chart based on some sample data...
			final String title = "LCACs in use at given time";
			final String xAxisLabel = "Time";
			final String yAxisLabel = "Number of Transports";

			final XYDataset data = createStepXYDataset();

			final JFreeChart chart = ChartFactory.createXYStepChart(
								title,
								xAxisLabel, yAxisLabel,
								data,
								PlotOrientation.VERTICAL,
								true, // legend
					true, // tooltips
					false // urls
					);

			// then customise it a little...
			chart.setBackgroundPaint(new Color(216, 216, 216));
			final XYPlot plot = chart.getXYPlot();
			plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
			plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.0f));

			// and present it in a frame...
			this.frame = new ChartFrame("Plan Comparison", chart);
			this.frame.pack();
			RefineryUtilities.positionFrameRandomly(this.frame);
			this.frame.setVisible(true);

		} else {
			this.frame.setVisible(true);
			this.frame.requestFocus();
		}

	}

	/**
	 * Creates and returns a sample dataset for the XY 'Step' chart.
	 * 
	 * @return the sample dataset.
	 */
	public static XYDataset createStepXYDataset() {

		final int feb = 2;
		final XYSeries s1 = new XYSeries("Plan 1", false, true);
		s1.add(DateUtilities.createDate(2002, feb, 19, 8, 0).getTime(), 0);
		s1.add(DateUtilities.createDate(2002, feb, 19, 8, 1).getTime(), 2);
		s1.add(DateUtilities.createDate(2002, feb, 19, 9, 5).getTime(), 4);
		s1.add(DateUtilities.createDate(2002, feb, 19, 10, 6).getTime(), 4);
		s1.add(DateUtilities.createDate(2002, feb, 19, 11, 6).getTime(), 5);
		s1.add(DateUtilities.createDate(2002, feb, 19, 12, 6).getTime(), 3);
		s1.add(DateUtilities.createDate(2002, feb, 19, 13, 6).getTime(), 6);
		s1.add(DateUtilities.createDate(2002, feb, 19, 14, 6).getTime(), 6);
		s1.add(DateUtilities.createDate(2002, feb, 19, 15, 30).getTime(), 2);
		s1.add(DateUtilities.createDate(2002, feb, 19, 16, 7).getTime(), 0);

		final XYSeries s2 = new XYSeries("Plan 2");
		s2.add(DateUtilities.createDate(2002, feb, 19, 8, 40).getTime(), 0);
		s2.add(DateUtilities.createDate(2002, feb, 19, 8, 45).getTime(), 1);
		s2.add(DateUtilities.createDate(2002, feb, 19, 9, 0).getTime(), 6);
		s2.add(DateUtilities.createDate(2002, feb, 19, 10, 6).getTime(), 2);
		s2.add(DateUtilities.createDate(2002, feb, 19, 10, 45).getTime(), 4);
		s2.add(DateUtilities.createDate(2002, feb, 19, 12, 0).getTime(), 7);
		s2.add(DateUtilities.createDate(2002, feb, 19, 13, 0).getTime(), 5);
		s2.add(DateUtilities.createDate(2002, feb, 19, 14, 6).getTime(), 4);
		s2.add(DateUtilities.createDate(2002, feb, 19, 15, 15).getTime(), 4);
		s2.add(DateUtilities.createDate(2002, feb, 19, 16, 0).getTime(), 0);

		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(s1);
		dataset.addSeries(s2);
		return dataset;
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
	 * The starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final XYStepChartDemo demo = new XYStepChartDemo();
		demo.displayChart();

	}

}
