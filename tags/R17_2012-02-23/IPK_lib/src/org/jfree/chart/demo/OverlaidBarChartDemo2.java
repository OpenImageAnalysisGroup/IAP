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
 * OverlaidBarChartDemo2.java
 * --------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: OverlaidBarChartDemo2.java,v 1.1 2011-01-31 09:01:54 klukas Exp $
 * Changes
 * -------
 * 09-Jan-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer;
import org.jfree.chart.renderer.CategoryItemRenderer;
import org.jfree.chart.renderer.LevelRenderer;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * Another demo of an overlaid bar chart.
 */
public class OverlaidBarChartDemo2 extends ApplicationFrame {

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public OverlaidBarChartDemo2(final String title) {
		super(title);
		final JFreeChart chart = createChart();
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @return A sample chart.
	 */
	private JFreeChart createChart() {

		// create the first dataset...
		DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
		dataset1.addValue(1.0, "S1", "Category 1");
		dataset1.addValue(4.0, "S1", "Category 2");
		dataset1.addValue(3.0, "S1", "Category 3");
		dataset1.addValue(5.0, "S1", "Category 4");
		dataset1.addValue(5.0, "S1", "Category 5");
		dataset1.addValue(5.0, "S2", "Category 1");
		dataset1.addValue(7.0, "S2", "Category 2");
		dataset1.addValue(6.0, "S2", "Category 3");
		dataset1.addValue(8.0, "S2", "Category 4");
		dataset1.addValue(4.0, "S2", "Category 5");

		// create the first plot...
		final CategoryItemRenderer renderer = new BarRenderer();
		renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		final CategoryPlot plot = new CategoryPlot();
		plot.setDataset(dataset1);
		plot.setRenderer(renderer);

		plot.setDomainAxis(new CategoryAxis("Category"));
		plot.setRangeAxis(new NumberAxis("Value"));

		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setRangeGridlinesVisible(true);
		plot.setDomainGridlinesVisible(true);

		DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
		dataset2.addValue(6.0, "Prior 1", "Category 1");
		dataset2.addValue(7.0, "Prior 1", "Category 2");
		dataset2.addValue(2.0, "Prior 1", "Category 3");
		dataset2.addValue(6.0, "Prior 1", "Category 4");
		dataset2.addValue(6.0, "Prior 1", "Category 5");
		dataset2.addValue(4.0, "Prior 2", "Category 1");
		dataset2.addValue(2.0, "Prior 2", "Category 2");
		dataset2.addValue(1.0, "Prior 2", "Category 3");
		dataset2.addValue(3.0, "Prior 2", "Category 4");
		dataset2.addValue(2.0, "Prior 2", "Category 5");

		final CategoryItemRenderer renderer2 = new LevelRenderer();
		renderer2.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer2.setSeriesStroke(1, new BasicStroke(2.0f));
		plot.setDataset(1, dataset2);
		plot.setRenderer(1, renderer2);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.white);

		final JFreeChart chart = new JFreeChart(plot);
		chart.setTitle("Overlaid Bar Chart");
		chart.setLegend(new StandardLegend());
		chart.setBackgroundPaint(Color.white);
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

		final OverlaidBarChartDemo2 demo = new OverlaidBarChartDemo2("Overlaid Bar Chart Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
