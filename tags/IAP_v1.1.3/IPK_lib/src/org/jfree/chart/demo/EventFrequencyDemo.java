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
 * -----------------------
 * EventFrequencyDemo.java
 * -----------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: EventFrequencyDemo.java,v 1.1 2011-01-31 09:01:48 klukas Exp $
 * Changes (from 10-Oct-2002)
 * --------------------------
 * 10-Oct-2002 : Added standard header and Javadocs (DG);
 * 11-Feb-2003 : Fixed 0.9.5 bug (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.text.DateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.CategoryItemRenderer;
import org.jfree.chart.renderer.LineAndShapeRenderer;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.date.SerialDate;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo application showing how to display category data against a date axis.
 */
public class EventFrequencyDemo extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public EventFrequencyDemo(final String title) {

		super(title);

		// create a dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		// initialise the data...
		final Day d1 = new Day(12, SerialDate.JUNE, 2002);
		final Day d2 = new Day(14, SerialDate.JUNE, 2002);
		final Day d3 = new Day(15, SerialDate.JUNE, 2002);
		final Day d4 = new Day(10, SerialDate.JULY, 2002);
		final Day d5 = new Day(20, SerialDate.JULY, 2002);
		final Day d6 = new Day(22, SerialDate.AUGUST, 2002);

		dataset.setValue(new Long(d1.getMiddleMillisecond()), "Series 1", "Requirement 1");
		dataset.setValue(new Long(d1.getMiddleMillisecond()), "Series 1", "Requirement 2");
		dataset.setValue(new Long(d2.getMiddleMillisecond()), "Series 1", "Requirement 3");
		dataset.setValue(new Long(d3.getMiddleMillisecond()), "Series 2", "Requirement 1");
		dataset.setValue(new Long(d4.getMiddleMillisecond()), "Series 2", "Requirement 3");
		dataset.setValue(new Long(d5.getMiddleMillisecond()), "Series 3", "Requirement 2");
		dataset.setValue(new Long(d6.getMiddleMillisecond()), "Series 1", "Requirement 4");

		// create the chart...
		final JFreeChart chart = ChartFactory.createBarChart(
							"Event Frequency Demo", // title
				"Category", // domain axis label
				"Value", // range axis label
				dataset, // dataset
				PlotOrientation.HORIZONTAL, // orientation
				true, // include legend
				true, // tooltips
				false // URLs
				);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(new Color(0xFF, 0xFF, 0xCC));

		final StandardLegend legend = (StandardLegend) chart.getLegend();
		legend.setDisplaySeriesShapes(true);

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
		plot.setRangeAxis(new DateAxis("Date"));
		final CategoryToolTipGenerator toolTipGenerator = new StandardCategoryToolTipGenerator(
							"", DateFormat.getDateInstance()
							);
		final CategoryItemRenderer renderer = new LineAndShapeRenderer(LineAndShapeRenderer.SHAPES);
		renderer.setToolTipGenerator(toolTipGenerator);
		plot.setRenderer(renderer);

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

		// OPTIONAL CUSTOMISATION COMPLETED.

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final EventFrequencyDemo demo = new EventFrequencyDemo("Event Frequency Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
