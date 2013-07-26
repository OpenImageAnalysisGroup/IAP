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
 * ItemLabelDemo1.java
 * -------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ItemLabelDemo1.java,v 1.1 2011-01-31 09:01:43 klukas Exp $
 * Changes
 * -------
 * 19-Feb-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Dimension;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
 * A simple demo showing a label generator that only displays labels for items
 * with a value that is greater than some threshold.
 */
public class ItemLabelDemo1 extends ApplicationFrame {

	/**
	 * A custom label generator.
	 */
	static class LabelGenerator implements CategoryLabelGenerator {

		/** The threshold. */
		private double threshold;

		/**
		 * Creates a new generator that only displays labels that are greater
		 * than or equal to the threshold value.
		 * 
		 * @param threshold
		 *           the threshold value.
		 */
		public LabelGenerator(final double threshold) {
			this.threshold = threshold;
		}

		/**
		 * Generates a label for the specified item. The label is typically a
		 * formatted version of the data value, but any text can be used.
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
			final Number value = dataset.getValue(series, category);
			if (value != null) {
				final double v = value.doubleValue();
				if (v > this.threshold) {
					result = value.toString(); // could apply formatting here
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
	public ItemLabelDemo1(final String title) {

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
		dataset.addValue(11.0, "S1", "C1");
		dataset.addValue(44.3, "S1", "C2");
		dataset.addValue(93.0, "S1", "C3");
		dataset.addValue(35.6, "S1", "C4");
		dataset.addValue(75.1, "S1", "C5");
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
							"Item Label Demo 1", // chart title
				"Category", // domain axis label
				"Value", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				false, // include legend
				true, // tooltips?
				false // URLs?
				);

		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setUpperMargin(0.15);

		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setLabelGenerator(new LabelGenerator(50.0));
		renderer.setItemLabelsVisible(true);

		return chart;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final ItemLabelDemo1 demo = new ItemLabelDemo1("Item Label Demo 1");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
