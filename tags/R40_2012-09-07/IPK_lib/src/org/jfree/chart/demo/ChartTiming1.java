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
 * ChartTiming1.java
 * -----------------
 * (C) Copyright 2001-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ChartTiming1.java,v 1.1 2011-01-31 09:01:43 klukas Exp $
 * Changes (from 24-Apr-2002)
 * --------------------------
 * 24-Apr-2002 : Added standard header (DG);
 * 29-Oct-2002 : Modified to use javax.swing.Timer (DG);
 */

package org.jfree.chart.demo;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.DefaultPieDataset;

/**
 * Draws a pie chart over and over for 10 seconds. Reports on how many redraws were achieved.
 * <p>
 * On my PC (SuSE Linux 8.2, JDK 1.4, 256mb RAM, 2.66ghz Pentium) I get 90-95 charts per second.
 */
public class ChartTiming1 implements ActionListener {

	/** A flag that indicates when time is up. */
	private boolean finished;

	/**
	 * Creates a new application.
	 */
	public ChartTiming1() {
		// nothing to do
	}

	/**
	 * Runs the timing.
	 */
	public void run() {
		this.finished = false;

		// create a dataset...
		final DefaultPieDataset data = new DefaultPieDataset();
		data.setValue("One", new Double(10.3));
		data.setValue("Two", new Double(8.5));
		data.setValue("Three", new Double(3.9));
		data.setValue("Four", new Double(3.9));
		data.setValue("Five", new Double(3.9));
		data.setValue("Six", new Double(3.9));

		// create a pie chart...
		final boolean withLegend = true;
		final JFreeChart chart = ChartFactory.createPieChart(
							"Testing",
							data,
							withLegend,
							true,
							false
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
	 * Starting point for the application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final ChartTiming1 app = new ChartTiming1();
		app.run();

	}

}
