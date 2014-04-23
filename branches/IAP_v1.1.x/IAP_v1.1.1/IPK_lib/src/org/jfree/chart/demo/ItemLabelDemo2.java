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
 * ItemLabelDemo2.java
 * -------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ItemLabelDemo2.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes
 * -------
 * 19-Feb-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.CategoryItemRenderer;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demo showing a label generator that displays labels that include
 * a percentage calculation.
 */
public class ItemLabelDemo2 extends ApplicationFrame {

	/**
	 * A custom label generator.
	 */
	static class LabelGenerator implements CategoryLabelGenerator {

		/**
		 * The index of the category on which to base the percentage
		 * (null = use series total).
		 */
		private Integer category;

		/** A percent formatter. */
		private NumberFormat formatter = NumberFormat.getPercentInstance();

		/**
		 * Creates a new label generator that displays the item value and a
		 * percentage relative to the value in the same series for the
		 * specified category.
		 * 
		 * @param category
		 *           the category index (zero-based).
		 */
		public LabelGenerator(final int category) {
			this(new Integer(category));
		}

		/**
		 * Creates a new label generator that displays the item value and
		 * a percentage relative to the value in the same series for the
		 * specified category. If the category index is <code>null</code>,
		 * the total of all items in the series is used.
		 * 
		 * @param category
		 *           the category index (<code>null</code> permitted).
		 */
		public LabelGenerator(final Integer category) {
			this.category = category;
		}

		/**
		 * Generates a label for the specified item. The label is typically
		 * a formatted version of the data value, but any text can be used.
		 * 
		 * @param dataset
		 *           the dataset (<code>null</code> not permitted).
		 * @param series
		 *           the series index (zero-based).
		 * @param category
		 *           the category index (zero-based).
		 * @return the label (possibly <code>null</code>).
		 */
		public String generateLabel(final CategoryDataset dataset,
												final int series,
												final int category) {

			String result = null;
			double base = 0.0;
			if (this.category != null) {
				final Number b = dataset.getValue(series, this.category.intValue());
				base = b.doubleValue();
			} else {
				base = calculateSeriesTotal(dataset, series);
			}
			final Number value = dataset.getValue(series, category);
			if (value != null) {
				final double v = value.doubleValue();
				// you could apply some formatting here
				result = value.toString()
									+ " (" + this.formatter.format(v / base) + ")";
			}
			return result;

		}

		/**
		 * Calculates a series total.
		 * 
		 * @param dataset
		 *           the dataset.
		 * @param series
		 *           the series index.
		 * @return The total.
		 */
		private double calculateSeriesTotal(final CategoryDataset dataset, final int series) {
			double result = 0.0;
			for (int i = 0; i < dataset.getColumnCount(); i++) {
				final Number value = dataset.getValue(series, i);
				if (value != null) {
					result = result + value.doubleValue();
				}
			}
			return result;
		}

	}

	/**
	 * Creates a new demo instance.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public ItemLabelDemo2(final String title) {

		super(title);
		final CategoryDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Returns a sample dataset.
	 * 
	 * @return the dataset.
	 */
	private CategoryDataset createDataset() {

		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(100.0, "S1", "C1");
		dataset.addValue(44.3, "S1", "C2");
		dataset.addValue(93.0, "S1", "C3");
		dataset.addValue(80.0, "S2", "C1");
		dataset.addValue(75.1, "S2", "C2");
		dataset.addValue(15.1, "S2", "C3");
		return dataset;

	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return the chart.
	 */
	private JFreeChart createChart(final CategoryDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createBarChart(
							"Item Label Demo 2", // chart title
				"Category", // domain axis label
				"Value", // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setUpperMargin(0.25);

		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setItemLabelsVisible(true);

		// use one or the other of the following lines to see the different modes for
		// the label generator...
		renderer.setLabelGenerator(new LabelGenerator(null));
		// renderer.setLabelGenerator(new LabelGenerator(0));

		return chart;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final ItemLabelDemo2 demo = new ItemLabelDemo2("Item Label Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
