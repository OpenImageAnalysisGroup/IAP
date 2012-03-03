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
 * WaterfallChartDemo.java
 * -----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: WaterfallChartDemo.java,v 1.1 2011-01-31 09:01:47 klukas Exp $
 * Changes
 * -------
 * 21-Oct-2003 : Version 1 (DG);
 * 06-Nov-2003 : Modified to use ChartFactory (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * A sample waterfall chart.
 */
public class WaterfallChartDemo extends ApplicationFrame {

	/**
	 * Creates a new WaterFall Chart demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public WaterfallChartDemo(final String title) {

		super(title);

		final CategoryDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setEnforceFileExtensions(false);
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample dataset for the demo.
	 * 
	 * @return A sample dataset.
	 */
	private CategoryDataset createDataset() {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(15.76, "Product 1", "Labour");
		dataset.addValue(8.66, "Product 1", "Administration");
		dataset.addValue(4.71, "Product 1", "Marketing");
		dataset.addValue(3.51, "Product 1", "Distribution");
		dataset.addValue(32.64, "Product 1", "Total Expense");
		return dataset;
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
	 * @param dataset
	 *           the dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final CategoryDataset dataset) {

		final JFreeChart chart = ChartFactory.createWaterfallChart(
							"Product Cost Breakdown",
							"Expense Category",
							"Cost Per Unit",
							dataset,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);
		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.white);
		plot.setRangeGridlinesVisible(true);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));

		final ValueAxis rangeAxis = plot.getRangeAxis();

		// create a custom tick unit collection...
		final DecimalFormat formatter = new DecimalFormat("##,###");
		formatter.setNegativePrefix("(");
		formatter.setNegativeSuffix(")");
		final TickUnits standardUnits = new TickUnits();
		standardUnits.add(new NumberTickUnit(5, formatter));
		standardUnits.add(new NumberTickUnit(10, formatter));
		standardUnits.add(new NumberTickUnit(20, formatter));
		standardUnits.add(new NumberTickUnit(50, formatter));
		standardUnits.add(new NumberTickUnit(100, formatter));
		standardUnits.add(new NumberTickUnit(200, formatter));
		standardUnits.add(new NumberTickUnit(500, formatter));
		standardUnits.add(new NumberTickUnit(1000, formatter));
		standardUnits.add(new NumberTickUnit(2000, formatter));
		standardUnits.add(new NumberTickUnit(5000, formatter));
		rangeAxis.setStandardTickUnits(standardUnits);

		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		final DecimalFormat labelFormatter = new DecimalFormat("$##,###.00");
		labelFormatter.setNegativePrefix("(");
		labelFormatter.setNegativeSuffix(")");
		renderer.setLabelGenerator(
							new StandardCategoryLabelGenerator("{2}", labelFormatter)
							);
		renderer.setItemLabelsVisible(true);

		return chart;
	}

	/**
	 * Starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {
		final WaterfallChartDemo demo = new WaterfallChartDemo("Waterfall Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

}
