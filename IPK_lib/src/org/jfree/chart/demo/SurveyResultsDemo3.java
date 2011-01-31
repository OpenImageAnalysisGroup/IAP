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
 * SurveyResultsDemo3.java
 * -----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: SurveyResultsDemo3.java,v 1.1 2011-01-31 09:01:47 klukas Exp $
 * Changes
 * -------
 * 31-Oct-2003 : Version 1 (DG);
 */
package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

/**
 * A chart showing...
 */
public class SurveyResultsDemo3 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public SurveyResultsDemo3(final String title) {

		super(title);

		final CategoryDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(300, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a dataset.
	 * 
	 * @return The dataset.
	 */
	private CategoryDataset createDataset() {

		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(2.61, "Results", "Sm.");
		dataset.addValue(2.70, "Results", "Med.");
		dataset.addValue(2.90, "Results", "Lg.");
		dataset.addValue(2.74, "Results", "All");
		return dataset;

	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final CategoryDataset dataset) {

		final JFreeChart chart = ChartFactory.createBarChart(
							null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // orientation
				false, // include legend
				true,
							false
							);

		chart.setBackgroundPaint(Color.white);
		chart.getPlot().setOutlinePaint(null);
		final TextTitle title = new TextTitle("Figure 6 | Overall SEO Rating");
		title.setHorizontalAlignment(HorizontalAlignment.LEFT);
		title.setBackgroundPaint(Color.red);
		title.setPaint(Color.white);

		chart.setTitle(title);
		final CategoryPlot plot = chart.getCategoryPlot();

		final ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setRange(0.0, 4.0);
		rangeAxis.setVisible(false);

		final ExtendedCategoryAxis domainAxis = new ExtendedCategoryAxis(null);
		domainAxis.setTickLabelFont(new Font("SansSerif", Font.BOLD, 12));
		domainAxis.setCategoryMargin(0.30);
		domainAxis.addSubLabel("Sm.", "(10)");
		domainAxis.addSubLabel("Med.", "(10)");
		domainAxis.addSubLabel("Lg.", "(10)");
		domainAxis.addSubLabel("All", "(10)");
		final CategoryLabelPositions p = domainAxis.getCategoryLabelPositions();
		final CategoryLabelPosition left = new CategoryLabelPosition(
							RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT
							);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.replaceLeftPosition(p, left));
		plot.setDomainAxis(domainAxis);

		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, new Color(0x9C, 0xA4, 0x4A));
		renderer.setBaseOutlineStroke(null);

		final StandardCategoryLabelGenerator generator = new StandardCategoryLabelGenerator(
							"{2}", new DecimalFormat("0.00")
							);
		renderer.setLabelGenerator(generator);
		renderer.setItemLabelsVisible(true);
		renderer.setItemLabelFont(new Font("SansSerif", Font.PLAIN, 18));
		final ItemLabelPosition position = new ItemLabelPosition(
							ItemLabelAnchor.INSIDE3, TextAnchor.CENTER_RIGHT
							);
		renderer.setPositiveItemLabelPosition(position);
		renderer.setPositiveItemLabelPositionFallback(new ItemLabelPosition());

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

		final SurveyResultsDemo3 demo = new SurveyResultsDemo3("Survey Results Demo 3");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
