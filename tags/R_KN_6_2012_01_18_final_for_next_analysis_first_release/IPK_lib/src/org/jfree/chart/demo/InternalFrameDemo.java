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
 * ----------------------
 * InternalFrameDemo.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: InternalFrameDemo.java,v 1.1 2011-01-31 09:01:43 klukas Exp $
 * Changes
 * -------
 * 29-Jul-2003 : Version 1 (DG);
 */
package org.jfree.chart.demo;

import java.awt.Dimension;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple internal frame demo.
 */
public class InternalFrameDemo extends ApplicationFrame {

	/**
	 * Creates a new instance of the demo.
	 * 
	 * @param title
	 *           the title.
	 */
	public InternalFrameDemo(final String title) {
		super(title);
		final JDesktopPane desktopPane = new JDesktopPane();
		desktopPane.setPreferredSize(new Dimension(600, 400));
		final JInternalFrame frame1 = createFrame1();
		desktopPane.add(frame1);
		frame1.pack();
		frame1.setVisible(true);
		final JInternalFrame frame2 = createFrame2();
		desktopPane.add(frame2);
		frame2.pack();
		frame2.setLocation(100, 200);
		frame2.setVisible(true);
		getContentPane().add(desktopPane);
	}

	/**
	 * Creates an internal frame.
	 * 
	 * @return An internal frame.
	 */
	private JInternalFrame createFrame1() {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(34.0, "Series 1", "Category 1");
		dataset.addValue(23.0, "Series 1", "Category 2");
		dataset.addValue(54.0, "Series 1", "Category 3");
		final JFreeChart chart = ChartFactory.createBarChart(
							"Bar Chart",
							"Category",
							"Series",
							dataset,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(200, 100));
		final JInternalFrame frame = new JInternalFrame("Frame 1", true);
		frame.getContentPane().add(chartPanel);
		return frame;

	}

	/**
	 * Creates an internal frame.
	 * 
	 * @return An internal frame.
	 */
	private JInternalFrame createFrame2() {
		final XYDataset dataset1 = createDataset("Series 1", 100.0, new Minute(), 200);

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							"Time Series Chart",
							"Time of Day",
							"Value",
							dataset1,
							true,
							true,
							false
							);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(200, 100));
		final JInternalFrame frame = new JInternalFrame("Frame 2", true);
		frame.getContentPane().add(chartPanel);
		return frame;
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
			period = period.next();
			value = value * (1 + (Math.random() - 0.495) / 10.0);
		}

		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(series);

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
	 * The starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {
		final InternalFrameDemo demo = new InternalFrameDemo("Internal Frame Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
