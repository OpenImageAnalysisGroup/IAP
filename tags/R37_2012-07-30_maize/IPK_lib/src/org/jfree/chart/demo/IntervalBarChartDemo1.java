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
 * --------------------------
 * IntervalBarChartDemo1.java
 * --------------------------
 * (C) Copyright 2002-2004, by Jeremy Bowman and Contributors.
 * Original Author: Jeremy Bowman.
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: IntervalBarChartDemo1.java,v 1.1 2011-01-31 09:01:54 klukas Exp $
 * Changes
 * -------
 * 29-Apr-2002 : Version 1, contributed by Jeremy Bowman. Name changed to
 * IntervalBarChartDemo, and the chart is displayed in a frame rather than
 * saved to a file (DG);
 * 25-Jun-2002 : Removed unnecessary imports (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.IntervalCategoryLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.IntervalBarRenderer;
import org.jfree.data.DefaultIntervalCategoryDataset;
import org.jfree.ui.TextAnchor;

/**
 * An interval bar chart.
 * 
 * @author Jeremy Bowman
 */
public class IntervalBarChartDemo1 {

	/** The categories. */
	private static final String[] CATEGORIES = { "1", "3", "5", "10", "20" };

	/** The label font. */
	private static Font labelFont = null;

	/** The title font. */
	private static Font titleFont = null;

	/** The chart. */
	private JFreeChart chart = null;

	static {
		labelFont = new Font("Helvetica", Font.PLAIN, 10);
		titleFont = new Font("Helvetica", Font.BOLD, 14);
	}

	/**
	 * Creates a new demo.
	 */
	public IntervalBarChartDemo1() {

		DefaultIntervalCategoryDataset data = null;
		final double[][] lows = { { -.0315, .0159, .0306, .0453, .0557 } };
		final double[][] highs = { { .1931, .1457, .1310, .1163, .1059 } };
		data = new DefaultIntervalCategoryDataset(lows, highs);
		data.setCategoryKeys(CATEGORIES);

		final String title = "Strategie Sicherheit";
		final String xTitle = "Zeitraum (in Jahren)";
		final String yTitle = "Performance";
		final CategoryAxis xAxis = new CategoryAxis(xTitle);
		xAxis.setLabelFont(titleFont);
		xAxis.setTickLabelFont(labelFont);
		xAxis.setTickMarksVisible(false);
		final NumberAxis yAxis = new NumberAxis(yTitle);
		yAxis.setLabelFont(titleFont);
		yAxis.setTickLabelFont(labelFont);
		yAxis.setRange(-0.2, 0.40);
		final DecimalFormat formatter = new DecimalFormat("0.##%");
		yAxis.setTickUnit(new NumberTickUnit(0.05, formatter));

		final IntervalBarRenderer renderer = new IntervalBarRenderer();
		renderer.setSeriesPaint(0, new Color(51, 102, 153));
		renderer.setLabelGenerator(new IntervalCategoryLabelGenerator());
		renderer.setItemLabelsVisible(true);
		renderer.setItemLabelPaint(Color.white);
		final ItemLabelPosition p = new ItemLabelPosition(
							ItemLabelAnchor.CENTER, TextAnchor.CENTER
							);
		renderer.setPositiveItemLabelPosition(p);

		final CategoryPlot plot = new CategoryPlot(data, xAxis, yAxis, renderer);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setOutlinePaint(Color.white);
		plot.setOrientation(PlotOrientation.VERTICAL);

		this.chart = new JFreeChart(title, titleFont, plot, false);
		this.chart.setBackgroundPaint(Color.white);
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
	 * Returns the chart.
	 * 
	 * @return the chart.
	 */
	public JFreeChart getChart() {
		return this.chart;
	}

	/**
	 * Starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {
		final IntervalBarChartDemo1 sample = new IntervalBarChartDemo1();
		final JFreeChart chart = sample.getChart();
		final ChartFrame frame = new ChartFrame("Interval Bar Chart Demo", chart);
		frame.pack();
		frame.setVisible(true);
	}
}
