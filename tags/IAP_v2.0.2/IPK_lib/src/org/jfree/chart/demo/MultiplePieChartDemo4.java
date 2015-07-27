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
 * MultiplePieChartDemo4.java
 * --------------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MultiplePieChartDemo4.java,v 1.1 2011-01-31 09:01:54 klukas Exp $
 * Changes
 * -------
 * 30-Jan-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.labels.StandardPieItemLabelGenerator;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.TableOrder;

/**
 * A simple demonstration application showing how to create a chart consisting of multiple
 * pie charts.
 */
public class MultiplePieChartDemo4 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MultiplePieChartDemo4(final String title) {

		super(title);
		final CategoryDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart, true, true, true, false, true);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 380));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return A sample dataset.
	 */
	private CategoryDataset createDataset() {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(5.6, "Row 0", "Column 0");
		dataset.addValue(3.2, "Row 0", "Column 1");
		dataset.addValue(1.8, "Row 0", "Column 2");
		dataset.addValue(0.2, "Row 0", "Column 3");
		dataset.addValue(4.1, "Row 0", "Column 4");

		dataset.addValue(9.8, "Row 1", "Column 0");
		dataset.addValue(6.3, "Row 1", "Column 1");
		dataset.addValue(0.1, "Row 1", "Column 2");
		dataset.addValue(1.9, "Row 1", "Column 3");
		dataset.addValue(9.6, "Row 1", "Column 4");

		dataset.addValue(7.0, "Row 2", "Column 0");
		dataset.addValue(5.2, "Row 2", "Column 1");
		dataset.addValue(2.8, "Row 2", "Column 2");
		dataset.addValue(8.8, "Row 2", "Column 3");
		dataset.addValue(7.2, "Row 2", "Column 4");

		dataset.addValue(9.5, "Row 3", "Column 0");
		dataset.addValue(1.2, "Row 3", "Column 1");
		dataset.addValue(4.5, "Row 3", "Column 2");
		dataset.addValue(4.4, "Row 3", "Column 3");
		dataset.addValue(0.2, "Row 3", "Column 4");

		dataset.addValue(3.5, "Row 4", "Column 0");
		dataset.addValue(6.7, "Row 4", "Column 1");
		dataset.addValue(9.0, "Row 4", "Column 2");
		dataset.addValue(1.0, "Row 4", "Column 3");
		dataset.addValue(5.2, "Row 4", "Column 4");

		dataset.addValue(5.1, "Row 5", "Column 0");
		dataset.addValue(6.7, "Row 5", "Column 1");
		dataset.addValue(0.9, "Row 5", "Column 2");
		dataset.addValue(3.3, "Row 5", "Column 3");
		dataset.addValue(3.9, "Row 5", "Column 4");

		dataset.addValue(5.6, "Row 6", "Column 0");
		dataset.addValue(5.6, "Row 6", "Column 1");
		dataset.addValue(5.6, "Row 6", "Column 2");
		dataset.addValue(5.6, "Row 6", "Column 3");
		dataset.addValue(5.6, "Row 6", "Column 4");

		dataset.addValue(7.5, "Row 7", "Column 0");
		dataset.addValue(9.0, "Row 7", "Column 1");
		dataset.addValue(3.4, "Row 7", "Column 2");
		dataset.addValue(4.1, "Row 7", "Column 3");
		dataset.addValue(0.5, "Row 7", "Column 4");

		return dataset;
	}

	/**
	 * Creates a sample chart for the given dataset.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return A sample chart.
	 */
	private JFreeChart createChart(final CategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createMultiplePieChart3D(
							"Multiple Pie Chart Demo 4", dataset, TableOrder.BY_COLUMN, false, true, false
							);
		chart.setBackgroundPaint(new Color(216, 255, 216));
		final MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
		final JFreeChart subchart = plot.getPieChart();
		final StandardLegend legend = new StandardLegend();
		legend.setItemFont(new Font("SansSerif", Font.PLAIN, 8));
		legend.setAnchor(Legend.SOUTH);
		subchart.setLegend(legend);
		plot.setLimit(0.10);
		final PiePlot p = (PiePlot) subchart.getPlot();
		p.setLabelGenerator(new StandardPieItemLabelGenerator("{0}"));
		p.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		p.setInteriorGap(0.30);

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

		final MultiplePieChartDemo4 demo = new MultiplePieChartDemo4("Multiple Pie Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
