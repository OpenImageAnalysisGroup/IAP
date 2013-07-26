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
 * ThermometerDemo2.java
 * ---------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ThermometerDemo2.java,v 1.1 2011-01-31 09:01:54 klukas Exp $
 * Changes
 * -------
 * 17-Sep-2002 : Version 1 (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Insets;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.DefaultValueDataset;
import org.jfree.ui.ApplicationFrame;

/**
 * A simple demonstration application showing how to create a thermometer.
 */
public class ThermometerDemo2 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public ThermometerDemo2(final String title) {

		super(title);

		// create a dataset...
		final DefaultValueDataset dataset = new DefaultValueDataset(new Double(43.0));

		// create the chart...
		final ThermometerPlot plot = new ThermometerPlot(dataset);
		final JFreeChart chart = new JFreeChart("Thermometer Demo 2", // chart title
				JFreeChart.DEFAULT_TITLE_FONT,
														plot, // plot
				false); // include legend

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		plot.setInsets(new Insets(5, 5, 5, 5));
		// plot.setRangeInfo(ThermometerPlot.NORMAL, 0.0, 55.0, 0.0, 100.0);
		// plot.setRangeInfo(ThermometerPlot.WARNING, 55.0, 75.0, 0.0, 100.0);
		// plot.setRangeInfo(ThermometerPlot.CRITICAL, 75.0, 100.0, 0.0, 100.0);

		plot.setThermometerStroke(new BasicStroke(2.0f));
		plot.setThermometerPaint(Color.lightGray);
		// OPTIONAL CUSTOMISATION COMPLETED.

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
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

		final ThermometerDemo2 demo = new ThermometerDemo2("Thermometer Demo 2");
		demo.pack();
		demo.setVisible(true);

	}

}
