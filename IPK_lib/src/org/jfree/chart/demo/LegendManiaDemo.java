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
 * LegendManiaDemo.java
 * --------------------
 * (C) Copyright 2004, by Barak Naveh and Contributors.
 * Original Author: Barak Naveh;
 * Contributor(s): -;
 * $Id: LegendManiaDemo.java,v 1.1 2011-01-31 09:01:46 klukas Exp $
 * Changes
 * -------
 * 26-Mar-2004 : Version 1 contributed by Barak Naveh (BN);
 * 27-Mar-2004 : Added showing off round corners of bounding box (BN);
 * 21-Apr-2004 : Added showing off legend item word-wrap (BN);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo that shows legend positions and legend bounding box options.
 * 
 * @author Barak Naveh
 * @since March 26, 2004
 */
public class LegendManiaDemo extends ApplicationFrame {

	/** The chart title. */
	private static final String CHART_TITLE = "Legend Mania Demo";

	/** The background paint. */
	private static final Paint BACKGROUND_PAINT = new Color(255, 240, 240);

	/** The chart of this demo */
	private JFreeChart demoChart;

	/**
	 * A demo application that shows legend positions and legend bounding box
	 * options.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public LegendManiaDemo(final String title) {
		super(title);
		final CategoryDataset dataset = createDataset();
		this.demoChart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(this.demoChart);
		chartPanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(chartPanel);
	}

	/**
	 * Returns a sample dataset.
	 * 
	 * @return The dataset.
	 */
	private CategoryDataset createDataset() {

		// row keys...
		final String series1 = "Birds";
		final String series2 = "Beas";
		final String series3 = "A very very very very very long snake";

		// column keys...
		final String category1 = "Shopping";
		final String category2 = "Socializing";
		final String category3 = "Sex";
		final String category4 = "TV Watching";

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(1.0, series1, category1);
		dataset.addValue(4.0, series1, category2);
		dataset.addValue(3.0, series1, category3);
		dataset.addValue(5.0, series1, category4);

		dataset.addValue(5.0, series2, category1);
		dataset.addValue(7.0, series2, category2);
		dataset.addValue(6.0, series2, category3);
		dataset.addValue(8.0, series2, category4);

		dataset.addValue(4.0, series3, category1);
		dataset.addValue(3.0, series3, category2);
		dataset.addValue(2.0, series3, category3);
		dataset.addValue(3.0, series3, category4);

		return dataset;
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final CategoryDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createBarChart(
							CHART_TITLE, // chart title
				"Activity", // domain axis label
				"Rate", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(new Color(255, 255, 180));

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(BACKGROUND_PAINT);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(
							CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

		final StandardLegend legend = (StandardLegend) chart.getLegend();
		legend.setBackgroundPaint(Color.orange);
		legend.setOutlinePaint(Color.orange);

		// activate word wrapping when legend is vertical.
		legend.setPreferredWidth(125);

		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {
		final LegendManiaDemo demo = new LegendManiaDemo(CHART_TITLE);
		demo.pack();
		demo.setSize(800, 600);
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

		final Thread updater = demo.new UpdaterThread();
		updater.setDaemon(true);
		updater.start();
	}

	/**
	 * A thread for updating the legend position in a loop.
	 */
	private class UpdaterThread extends Thread {

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			final int[] anchors = {
								Legend.NORTH_NORTHWEST,
								Legend.NORTH,
								Legend.NORTH_NORTHEAST,
								Legend.EAST_NORTHEAST,
								Legend.EAST,
								Legend.EAST_SOUTHEAST,
								Legend.SOUTH_SOUTHEAST,
								Legend.SOUTH,
								Legend.SOUTH_SOUTHWEST,
								Legend.WEST_SOUTHWEST,
								Legend.WEST,
								Legend.WEST_NORTHWEST
				};

			final String[] anchorNames = {
								"NORTH_NORTHWEST",
								"NORTH",
								"NORTH_NORTHEAST",
								"EAST_NORTHEAST",
								"EAST",
								"EAST_SOUTHEAST",
								"SOUTH_SOUTHEAST",
								"SOUTH",
								"SOUTH_SOUTHWEST",
								"WEST_SOUTHWEST",
								"WEST",
								"WEST_NORTHWEST"
				};

			setPriority(MIN_PRIORITY); // be nice
			final StandardLegend legend = (StandardLegend) demoChart.getLegend();

			int i = 0;
			while (true) {
				// set the next anchor point
				legend.setTitle(anchorNames[i]);
				legend.setAnchor(anchors[i]);
				i = (i + 1) % anchors.length;

				// set rectangular corners of bounding box and wait for a second
				legend.setBoundingBoxArcHeight(0);
				legend.setBoundingBoxArcWidth(0);

				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// ignored.
				}

				// set round corners of bounding box and wait for a second
				legend.setBoundingBoxArcHeight(10);
				legend.setBoundingBoxArcWidth(10);

				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// ignored.
				}
			}
		}
	}
}
