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
 * ------------------
 * PieChartDemo1.java
 * ------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: PieChartDemo1.java,v 1.1 2011-01-31 09:01:55 klukas Exp $
 * Changes
 * -------
 * 18-Apr-2002 : Version 1 (DG);
 * 25-Jun-2002 : Removed unnecessary import (DG);
 * 28-Aug-2002 : Centered frame on screen (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Mar-2004 : Updated for rewritten pie chart classes (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.DefaultPieDataset;
import org.jfree.data.PieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Log;
import org.jfree.util.PrintStreamLogTarget;

/**
 * A simple demonstration application showing how to create a pie chart using data from a {@link DefaultPieDataset}.
 */
public class PieChartDemo1 extends ApplicationFrame {

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public PieChartDemo1(final String title) {
		super(title);
		final PieDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a sample dataset.
	 */
	private PieDataset createDataset() {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		dataset.setValue("One", new Double(43.2));
		dataset.setValue("Two", new Double(10.0));
		dataset.setValue("Three", new Double(27.5));
		dataset.setValue("Four", new Double(17.5));
		dataset.setValue("Five", new Double(11.0));
		dataset.setValue("Six", new Double(19.4));
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
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return a chart.
	 */
	private JFreeChart createChart(final PieDataset dataset) {

		final JFreeChart chart = ChartFactory.createPieChart(
							"Pie Chart Demo 1", // chart title
				dataset, // data
				true, // include legend
				true,
							false
							);

		final PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		plot.setNoDataMessage("No data available");
		plot.setCircular(false);
		plot.setLabelLinkPaint(Color.red);
		plot.setLabelGap(0.02);
		return chart;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		Log.getInstance().addTarget(new PrintStreamLogTarget());
		final PieChartDemo1 demo = new PieChartDemo1("Pie Chart Demo 1");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
