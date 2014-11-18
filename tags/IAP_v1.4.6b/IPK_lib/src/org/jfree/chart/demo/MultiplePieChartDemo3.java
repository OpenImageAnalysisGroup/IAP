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
 * MultiplePieChartDemo3.java
 * --------------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MultiplePieChartDemo3.java,v 1.1 2011-01-31 09:01:46 klukas Exp $
 * Changes
 * -------
 * 29-Jan-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
public class MultiplePieChartDemo3 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MultiplePieChartDemo3(final String title) {

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
		dataset.addValue(5.6, "Row 0", "Column 1");
		dataset.addValue(5.6, "Row 0", "Column 2");
		dataset.addValue(5.6, "Row 0", "Column 3");
		dataset.addValue(5.6, "Row 0", "Column 4");

		dataset.addValue(5.6, "Row 1", "Column 0");
		dataset.addValue(5.6, "Row 1", "Column 1");
		dataset.addValue(5.6, "Row 1", "Column 2");
		dataset.addValue(5.6, "Row 1", "Column 3");
		dataset.addValue(5.6, "Row 1", "Column 4");

		dataset.addValue(5.6, "Row 2", "Column 0");
		dataset.addValue(5.6, "Row 2", "Column 1");
		dataset.addValue(5.6, "Row 2", "Column 2");
		dataset.addValue(5.6, "Row 2", "Column 3");
		dataset.addValue(5.6, "Row 2", "Column 4");

		dataset.addValue(5.6, "Row 3", "Column 0");
		dataset.addValue(5.6, "Row 3", "Column 1");
		dataset.addValue(5.6, "Row 3", "Column 2");
		dataset.addValue(5.6, "Row 3", "Column 3");
		dataset.addValue(5.6, "Row 3", "Column 4");

		dataset.addValue(5.6, "Row 4", "Column 0");
		dataset.addValue(5.6, "Row 4", "Column 1");
		dataset.addValue(5.6, "Row 4", "Column 2");
		dataset.addValue(5.6, "Row 4", "Column 3");
		dataset.addValue(5.6, "Row 4", "Column 4");

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
							"Multiple Pie Chart Demo 3", dataset, TableOrder.BY_COLUMN, true, true, false
							);
		chart.setBackgroundPaint(new Color(216, 255, 216));
		final MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
		final PiePlot p = (PiePlot) plot.getPieChart().getPlot();
		p.setMaximumLabelWidth(0.35);
		p.setLabelFont(new Font("SansSerif", Font.PLAIN, 9));
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

		final MultiplePieChartDemo3 demo = new MultiplePieChartDemo3("Multiple Pie Chart Demo 3");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
