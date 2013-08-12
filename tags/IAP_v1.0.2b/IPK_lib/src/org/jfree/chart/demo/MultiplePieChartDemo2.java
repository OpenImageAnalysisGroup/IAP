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
 * MultiplePieChartDemo2.java
 * --------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MultiplePieChartDemo2.java,v 1.1 2011-01-31 09:01:44 klukas Exp $
 * Changes
 * -------
 * 23-Jan-2003 : Version 1 (DG);
 * 30-Jan-2004 : Renamed MultiPieChartDemo2 --> MultiplePieChartDemo2, and refactored to
 * use the new MultiplePiePlot class (DG);
 */

package org.jfree.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieItemLabelGenerator;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetUtilities;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.TableOrder;

/**
 * This example is similar to {@link MultiplePieChartDemo1}, but slices the dataset by column
 * rather than by row.
 */
public class MultiplePieChartDemo2 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MultiplePieChartDemo2(final String title) {

		super(title);
		final CategoryDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart, true, true, true, false, true);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 380));
		setContentPane(chartPanel);

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
	 * Creates a sample dataset.
	 * 
	 * @return A sample dataset.
	 */
	private CategoryDataset createDataset() {
		final double[][] data = new double[][] {
							{ 3.0, 4.0, 3.0, 5.0 },
							{ 5.0, 7.0, 6.0, 8.0 },
							{ 5.0, 7.0, 3.0, 8.0 },
							{ 1.0, 2.0, 3.0, 4.0 },
							{ 2.0, 3.0, 2.0, 3.0 }
			};
		final CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
							"Region ",
							"Sales/Q",
							data
							);
		return dataset;
	}

	/**
	 * Creates a sample chart with the given dataset.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return A sample chart.
	 */
	private JFreeChart createChart(final CategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createMultiplePieChart(
							"Multiple Pie Chart", // chart title
				dataset, // dataset
				TableOrder.BY_COLUMN,
							true, // include legend
				true,
							false
							);
		final MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setOutlineStroke(new BasicStroke(1.0f));
		final JFreeChart subchart = plot.getPieChart();
		final PiePlot p = (PiePlot) subchart.getPlot();
		p.setBackgroundPaint(null);
		p.setOutlineStroke(null);
		p.setLabelGenerator(new StandardPieItemLabelGenerator(
							"{0} ({2})", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()
							));
		p.setMaximumLabelWidth(0.35);
		p.setLabelFont(new Font("SansSerif", Font.PLAIN, 9));
		p.setInteriorGap(0.30);
		return chart;
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final MultiplePieChartDemo2 demo = new MultiplePieChartDemo2("Multiple Pie Chart Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
