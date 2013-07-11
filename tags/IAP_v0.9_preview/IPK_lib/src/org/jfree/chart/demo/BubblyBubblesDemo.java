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
 * BubblyBubblesDemo.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Barak Naveh and Contributors.
 * Original Author: Barak Naveh;;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: BubblyBubblesDemo.java,v 1.1 2011-01-31 09:01:47 klukas Exp $
 * Changes
 * -------
 * 10-Jul-2003 : Version 1 contributed by Barak Naveh (DG);
 * 29-Mar-2004 : Elimintated compiler warnings while keeping JFreeChart conventions (BN);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.GradientPaint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.MatrixSeriesCollection;
import org.jfree.data.NormalizedMatrixSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo that shows how matrix series can be used for charts that follow a
 * constantly changing grid input.
 * 
 * @author Barak Naveh
 * @since Jun 25, 2003
 */
public class BubblyBubblesDemo extends ApplicationFrame {

	/** The default size. */
	private static final int SIZE = 10;

	/** The default title. */
	private static final String TITLE = "Population count at grid locations";

	/**
	 * The normalized matrix series is used here to represent a changing
	 * population on a grid.
	 */
	NormalizedMatrixSeries series;

	/**
	 * A demonstration application showing a bubble chart using matrix series.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public BubblyBubblesDemo(final String title) {
		super(title);

		this.series = createInitialSeries();

		final MatrixSeriesCollection dataset = new MatrixSeriesCollection(this.series);

		final JFreeChart chart = ChartFactory.createBubbleChart(
							TITLE, "X", "Y", dataset,
							PlotOrientation.VERTICAL,
							true,
							true, false);

		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0,
							1000, Color.blue));

		final XYPlot plot = chart.getXYPlot();
		plot.setForegroundAlpha(0.5f);

		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setLowerBound(-0.5);

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

		// rangeAxis.setInverted(true); // uncoment to reproduce a bug in jFreeChart
		rangeAxis.setLowerBound(-0.5);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setVerticalZoom(true);
		chartPanel.setHorizontalZoom(true);
		setContentPane(chartPanel);
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {
		final BubblyBubblesDemo demo = new BubblyBubblesDemo(TITLE);
		demo.pack();
		demo.setSize(800, 600);
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

		final Thread updater = demo.new UpdaterThread();
		updater.setDaemon(true);
		updater.start();
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
	 * Creates a series.
	 * 
	 * @return The series.
	 */
	private NormalizedMatrixSeries createInitialSeries() {
		final NormalizedMatrixSeries newSeries =
							new NormalizedMatrixSeries("Sample Grid 1", SIZE, SIZE);

		// seed a few random bubbles
		for (int count = 0; count < SIZE; count++) {
			final int i = (int) (Math.random() * SIZE);
			final int j = (int) (Math.random() * SIZE);

			final int mij = (int) (Math.random() * SIZE);
			newSeries.update(i, j, mij);
		}

		newSeries.setScaleFactor(newSeries.getItemCount());

		return newSeries;
	}

	/**
	 * A thread for updating the dataset.
	 */
	private class UpdaterThread extends Thread {
		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			setPriority(MIN_PRIORITY); // be nice

			while (true) {
				final int i = (int) (Math.random() * SIZE);
				final int j = (int) (Math.random() * SIZE);

				series.update(i, j, series.get(i, j) + 1);

				try {
					sleep(50);
				} catch (InterruptedException e) {
					// suppress
				}
			}
		}
	}
}
