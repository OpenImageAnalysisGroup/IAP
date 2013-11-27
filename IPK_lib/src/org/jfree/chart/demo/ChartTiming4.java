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
 * -----------------
 * ChartTiming4.java
 * -----------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ChartTiming4.java,v 1.1 2011-01-31 09:01:44 klukas Exp $
 * Changes
 * -------
 * 29-Oct-2002 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.Plot;

/**
 * Draws a scatter plot over and over for 10 seconds. Reports on how many redraws were achieved.
 * <p>
 * On my PC (SuSE Linux 8.2, JDK 1.4, 256mb RAM, 2.66ghz Pentium) I get 31 charts per second.
 */
public class ChartTiming4 implements ActionListener {

	/** A flag that indicates when time is up. */
	private boolean finished;

	/** Storage for the data. */
	private float[][] data = new float[2][1440];

	/**
	 * Creates a new application.
	 */
	public ChartTiming4() {
		// nothing to do
	}

	/**
	 * Runs the test.
	 */
	public void run() {

		this.finished = false;

		// create a dataset...
		populateData();

		// create a fast scatter chart...
		final Plot plot = new FastScatterPlot(this.data, new NumberAxis("X"), new NumberAxis("Y"));
		final JFreeChart chart = new JFreeChart(
							"Fast Scatter Plot Timing",
							JFreeChart.DEFAULT_TITLE_FONT,
							plot,
							true
							);

		final BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = image.createGraphics();
		final Rectangle2D chartArea = new Rectangle2D.Double(0, 0, 400, 300);

		// set up the timer...
		final Timer timer = new Timer(10000, this);
		timer.setRepeats(false);
		int count = 0;
		timer.start();
		while (!this.finished) {
			chart.draw(g2, chartArea, null, null);
			System.out.println("Charts drawn..." + count);
			if (!this.finished) {
				count++;
			}
		}
		System.out.println("DONE");

	}

	/**
	 * Receives notification of action events (in this case, from the Timer).
	 * 
	 * @param event
	 *           the event.
	 */
	public void actionPerformed(final ActionEvent event) {
		this.finished = true;
	}

	/**
	 * Populates the data array with random values.
	 */
	private void populateData() {

		for (int i = 0; i < this.data[0].length; i++) {

			final float x = i;
			this.data[0][i] = x;
			this.data[1][i] = 100 + (2 * x) + (float) Math.random() * 1440;
		}

	}

	/**
	 * Starting point for the application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final ChartTiming4 app = new ChartTiming4();
		app.run();

	}

}
