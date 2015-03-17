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
 * CombinedXYPlotDemo5.java
 * ------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id $
 * Changes
 * -------
 * 15-Sep-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration application showing how to create a combined chart using...
 */
public class CombinedXYPlotDemo5 extends ApplicationFrame {

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public CombinedXYPlotDemo5(final String title) {

		super(title);
		final JFreeChart chart = createCombinedChart();
		final ChartPanel panel = new ChartPanel(chart, true, true, true, false, true);
		panel.setPreferredSize(new java.awt.Dimension(500, 270));
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
	 * Creates a combined XYPlot chart.
	 * 
	 * @return the combined chart.
	 */
	private JFreeChart createCombinedChart() {

		// create a default chart based on some sample data...
		final TimeSeriesCollection dataset0 = new TimeSeriesCollection();
		final TimeSeries eur = DemoDatasetFactory.createEURTimeSeries();
		dataset0.addSeries(eur);

		final TimeSeriesCollection dataset1 = new TimeSeriesCollection();
		final TimeSeries mav = MovingAverage.createMovingAverage(
							eur, "EUR/GBP (30 Day MA)", 30, 30
							);
		dataset1.addSeries(eur);
		dataset1.addSeries(mav);

		final TimeSeriesCollection dataset2 = new TimeSeriesCollection();
		dataset2.addSeries(eur);

		JFreeChart chart = null;

		// make a common vertical axis for all the sub-plots
		final NumberAxis valueAxis = new NumberAxis("Value");
		valueAxis.setAutoRangeIncludesZero(false); // override default

		// make a horizontally combined plot
		final CombinedRangeXYPlot parent = new CombinedRangeXYPlot(valueAxis);

		// add subplot 1...
		final XYPlot subplot1 = new XYPlot(dataset0, new DateAxis("Date 1"), null,
													new StandardXYItemRenderer());
		subplot1.setDomainCrosshairVisible(true);
		subplot1.setRangeCrosshairVisible(true);
		parent.add(subplot1, 1);

		// add subplot 2...
		final XYPlot subplot2 = new XYPlot(dataset1, new DateAxis("Date 2"), null,
													new StandardXYItemRenderer());
		subplot2.setDomainCrosshairVisible(true);
		subplot2.setRangeCrosshairVisible(true);
		parent.add(subplot2, 1);

		// add subplot 3...
		final XYPlot subplot3 = new XYPlot(dataset2, new DateAxis("Date 3"),
													null, new XYBarRenderer(0.20));
		subplot3.setDomainCrosshairVisible(true);
		subplot3.setRangeCrosshairVisible(true);
		parent.add(subplot3, 1);

		// now make the top level JFreeChart
		chart = new JFreeChart("Demo Chart", JFreeChart.DEFAULT_TITLE_FONT, parent, true);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle("This is a subtitle",
															new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final CombinedXYPlotDemo5 demo = new CombinedXYPlotDemo5("Combined XY Plot Demo 5");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
