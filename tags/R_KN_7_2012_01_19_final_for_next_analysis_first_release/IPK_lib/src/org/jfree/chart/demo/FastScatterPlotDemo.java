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
 * ------------------------
 * FastScatterPlotDemo.java
 * ------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: FastScatterPlotDemo.java,v 1.1 2011-01-31 09:01:45 klukas Exp $
 * Changes (from 29-Oct-2002)
 * --------------------------
 * 29-Oct-2002 : Added standard header and Javadocs (DG);
 * 12-Nov-2003 : Enabled zooming (DG);
 */

package org.jfree.chart.demo;

import java.awt.RenderingHints;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo of the fast scatter plot.
 */
public class FastScatterPlotDemo extends ApplicationFrame {

	/** A constant for the number of items in the sample dataset. */
	private static final int COUNT = 500000;

	/** The data. */
	private float[][] data = new float[2][COUNT];

	/**
	 * Creates a new fast scatter plot demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public FastScatterPlotDemo(final String title) {

		super(title);
		populateData();
		final NumberAxis domainAxis = new NumberAxis("X");
		domainAxis.setAutoRangeIncludesZero(false);
		final NumberAxis rangeAxis = new NumberAxis("Y");
		rangeAxis.setAutoRangeIncludesZero(false);
		final FastScatterPlot plot = new FastScatterPlot(this.data, domainAxis, rangeAxis);
		final JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);
		chart.setLegend(null);

		// force aliasing of the rendered content..
		chart.getRenderingHints().put
							(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final ChartPanel panel = new ChartPanel(chart, true);
		panel.setPreferredSize(new java.awt.Dimension(500, 270));
		panel.setHorizontalZoom(true);
		panel.setVerticalZoom(true);
		panel.setMinimumDrawHeight(10);
		panel.setMaximumDrawHeight(2000);
		panel.setMinimumDrawWidth(20);
		panel.setMaximumDrawWidth(2000);

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
	 * Populates the data array with random values.
	 */
	private void populateData() {

		for (int i = 0; i < this.data[0].length; i++) {
			final float x = (float) i + 100000;
			this.data[0][i] = x;
			this.data[1][i] = 100000 + (float) Math.random() * COUNT;
		}

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final FastScatterPlotDemo demo = new FastScatterPlotDemo("Fast Scatter Plot Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
