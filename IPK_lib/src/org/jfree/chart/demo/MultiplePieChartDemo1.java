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
 * MultiplePieChartDemo1.java
 * --------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MultiplePieChartDemo1.java,v 1.1 2011-01-31 09:01:52 klukas Exp $
 * Changes
 * -------
 * 29-Jan-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Font;

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
 * A simple demonstration application showing how to create a chart consisting of multiple
 * pie charts.
 */
public class MultiplePieChartDemo1 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MultiplePieChartDemo1(final String title) {

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
				TableOrder.BY_ROW,
							true, // include legend
				true,
							false
							);
		final MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
		final JFreeChart subchart = plot.getPieChart();
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

		final MultiplePieChartDemo1 demo = new MultiplePieChartDemo1("Multiple Pie Chart Demo 1");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
