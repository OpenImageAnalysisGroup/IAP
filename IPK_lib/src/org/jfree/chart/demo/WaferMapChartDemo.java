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
 * WaferMapChartDemo.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Robert Redburn and Contributors.
 * Original Author: Robert Redburn;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: WaferMapChartDemo.java,v 1.1 2011-01-31 09:01:54 klukas Exp $
 * Changes
 * -------
 * 08-Nov-2003 : Version 1 (RR);
 * 04-Dec-2003 : Added standard header and Javadocs (DG);
 * 19-Jan-2004 : Moved waferdata() method to DemoDatasetFactory (RR);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.WaferMapDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demo showing a wafer map chart.
 */
public class WaferMapChartDemo extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public WaferMapChartDemo(final String title) {
		super(title);
		final WaferMapDataset dataset = DemoDatasetFactory.createRandomWaferMapDataset(5);
		final JFreeChart chart = ChartFactory.createWaferMapChart(
							"Wafer Map Demo", // title
				dataset, // wafermapdataset
				PlotOrientation.VERTICAL, // vertical = notchdown
				true, // legend
				false, // tooltips
				false
							);

		final Legend legend = chart.getLegend();
		legend.setAnchor(Legend.EAST);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		final TextTitle copyright = new TextTitle(
							"JFreeChart WaferMapPlot", new Font("SansSerif", Font.PLAIN, 9)
							);
		copyright.setPosition(RectangleEdge.BOTTOM);
		copyright.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		chart.addSubtitle(copyright);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 400));
		setContentPane(chartPanel);
	}

	/**
	 * Starting point for the demo application.
	 * 
	 * @param args
	 *           command line arguments (ignored).
	 */
	public static void main(final String[] args) {
		final WaferMapChartDemo demo = new WaferMapChartDemo("Wafer Map Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

} // end class wafermapchartdemo
