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
 * ScatterPlotDemo3.java
 * ---------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ScatterPlotDemo3.java,v 1.1 2011-01-31 09:01:45 klukas Exp $
 * Changes
 * -------
 * 11-May-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo scatter plot with some code showing how to convert between Java2D coordinates and
 * (x, y) coordinates.
 */
public class ScatterPlotDemo3 extends ApplicationFrame implements ChartMouseListener {

	/** The panel used to display the chart. */
	private ChartPanel chartPanel;

	/**
	 * A demonstration application showing a scatter plot.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public ScatterPlotDemo3(final String title) {
		super(title);
		final XYDataset dataset = new SampleXYDataset2();
		JFreeChart chart = createChart(dataset);
		chartPanel = new ChartPanel(chart);
		chartPanel.addChartMouseListener(this);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setVerticalAxisTrace(true);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setVerticalZoom(true);
		chartPanel.setHorizontalZoom(true);
		setContentPane(chartPanel);
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return A dataset.
	 */
	private JFreeChart createChart(XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createScatterPlot(
							"Scatter Plot Demo",
							"X", "Y",
							dataset,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);
		final Legend legend = chart.getLegend();
		if (legend instanceof StandardLegend) {
			final StandardLegend sl = (StandardLegend) legend;
			sl.setDisplaySeriesShapes(true);
		}
		final NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
		return chart;
	}

	/**
	 * Callback method for receiving notification of a mouse click on a chart.
	 * 
	 * @param event
	 *           information about the event.
	 */
	public void chartMouseClicked(ChartMouseEvent event) {
		int x = event.getTrigger().getX();
		int y = event.getTrigger().getY();

		// the following translation takes account of the fact that the chart image may
		// have been scaled up or down to fit the panel...
		Point2D p = chartPanel.translateScreenToJava2D(new Point(x, y));

		// now convert the Java2D coordinate to axis coordinates...
		XYPlot plot = chartPanel.getChart().getXYPlot();
		Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
		double xx = plot.getDomainAxis().java2DToValue(
							p.getX(), dataArea, plot.getDomainAxisEdge()
							);
		double yy = plot.getRangeAxis().java2DToValue(
							p.getY(), dataArea, plot.getRangeAxisEdge()
							);

		// just for fun, lets convert the axis coordinates back to component coordinates...
		double xxx = plot.getDomainAxis().valueToJava2D(xx, dataArea, plot.getDomainAxisEdge());
		double yyy = plot.getRangeAxis().valueToJava2D(yy, dataArea, plot.getRangeAxisEdge());

		Point2D p2 = chartPanel.translateJava2DToScreen(new Point2D.Double(xxx, yyy));
		System.out.println("Mouse coordinates are (" + x + ", " + y
							+ "), in data space = (" + xx + ", " + yy + ").");
		System.out.println("--> (" + p2.getX() + ", " + p2.getY() + ")");
	}

	/**
	 * Callback method for receiving notification of a mouse movement on a chart.
	 * 
	 * @param event
	 *           information about the event.
	 */
	public void chartMouseMoved(ChartMouseEvent event) {
		// ignore
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
		final ScatterPlotDemo3 demo = new ScatterPlotDemo3("Scatter Plot Demo 3");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

}
