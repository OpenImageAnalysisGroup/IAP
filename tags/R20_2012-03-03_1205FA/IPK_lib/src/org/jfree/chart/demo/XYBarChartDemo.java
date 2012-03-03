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
 * XYBarChartDemo.java
 * -------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XYBarChartDemo.java,v 1.1 2011-01-31 09:01:52 klukas Exp $
 * Changes
 * -------
 * 20-Jun-2002 : Version 1 (DG);
 * 02-Jul-2002 : Removed unnecessary imports (DG);
 * 24-Aug-2002 : Set preferred size for ChartPanel (DG);
 * 11-Oct-2002 : Fixed issues reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.GradientPaint;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a vertical bar chart.
 */
public class XYBarChartDemo extends ApplicationFrame {

	/**
	 * Constructs the demo application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public XYBarChartDemo(final String title) {

		super(title);

		final TimeSeriesCollection data = DemoDatasetFactory.createTimeSeriesCollection1();
		data.setDomainIsPointsInTime(false);
		final JFreeChart chart = ChartFactory.createXYBarChart(
							title,
							"X",
							true,
							"Y",
							data,
							PlotOrientation.VERTICAL,
							true,
							false,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.blue));

		final XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		final StandardXYToolTipGenerator generator = new StandardXYToolTipGenerator(
							"{1} = {2}", new SimpleDateFormat("yyyy"), new DecimalFormat("0.00")
							);
		renderer.setToolTipGenerator(generator);

		final XYPlot plot = chart.getXYPlot();
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
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

		final XYBarChartDemo demo = new XYBarChartDemo("XY Bar Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
