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
 * -------------------
 * BubblePlotDemo.java
 * -------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: BubblePlotDemo.java,v 1.1 2011-01-31 09:01:43 klukas Exp $
 * Changes
 * -------
 * 28-Jan-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.XYZDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A bubble chart demo.
 */
public class BubblePlotDemo extends ApplicationFrame {

	/**
	 * A demonstration application showing a bubble chart.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public BubblePlotDemo(final String title) {

		super(title);
		final XYZDataset data = new SampleXYZDataset();
		final JFreeChart chart = createChart(data);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setVerticalZoom(true);
		chartPanel.setHorizontalZoom(true);
		setContentPane(chartPanel);

	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final XYZDataset dataset) {
		final JFreeChart chart = ChartFactory.createBubbleChart(
							"Bubble Plot Demo",
							"X",
							"Y",
							dataset,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);
		final XYPlot plot = chart.getXYPlot();
		plot.setForegroundAlpha(0.65f);

		// increase the margins to account for the fact that the auto-range doesn't take into
		// account the bubble size...
		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setLowerMargin(0.15);
		domainAxis.setUpperMargin(0.15);
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLowerMargin(0.15);
		rangeAxis.setUpperMargin(0.15);
		return chart;
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

		final BubblePlotDemo demo = new BubblePlotDemo("Bubble Plot Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
