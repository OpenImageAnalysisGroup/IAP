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
 * ------------------------
 * CombinedXYPlotDemo4.java
 * ------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id $
 * Changes
 * -------
 * 29-Jul-2003 : Version 1 (DG);
 * 27-Apr-2004 : Modified for changes to XYPlot (DG);
 */

package org.jfree.chart.demo;

import java.awt.Font;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration application showing how to create a vertical combined chart.
 */
public class CombinedXYPlotDemo4 extends ApplicationFrame {

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public CombinedXYPlotDemo4(final String title) {

		super(title);
		final JFreeChart chart = createCombinedChart();
		final ChartPanel panel = new ChartPanel(chart, true, true, true, false, true);
		panel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(panel);

	}

	/**
	 * Creates a combined chart.
	 * 
	 * @return The combined chart.
	 */
	private JFreeChart createCombinedChart() {

		// create subplot 1...
		final XYDataset data1 = createDataset1();
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("Range 1");
		final XYPlot subplot1 = new XYPlot(data1, null, rangeAxis1, renderer1);
		subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

		// add secondary axis
		subplot1.setDataset(1, createDataset2());
		final NumberAxis axis2 = new NumberAxis("Range Axis 2");
		axis2.setAutoRangeIncludesZero(false);
		subplot1.setRangeAxis(1, axis2);
		subplot1.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		subplot1.setRenderer(1, new StandardXYItemRenderer());
		subplot1.mapDatasetToRangeAxis(1, 1);

		final XYTextAnnotation annotation = new XYTextAnnotation("Hello!", 50.0, 10000.0);
		annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
		annotation.setRotationAngle(Math.PI / 4.0);
		subplot1.addAnnotation(annotation);

		// create subplot 2...
		final XYDataset data2 = createDataset2();
		final XYItemRenderer renderer2 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis2 = new NumberAxis("Range 2");
		rangeAxis2.setAutoRangeIncludesZero(false);
		final XYPlot subplot2 = new XYPlot(data2, null, rangeAxis2, renderer2);
		subplot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

		// parent plot...
		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Domain"));
		plot.setGap(10.0);

		// add the subplots...
		plot.add(subplot1, 1);
		plot.add(subplot2, 1);
		plot.setOrientation(PlotOrientation.VERTICAL);

		// return a new chart containing the overlaid plot...
		return new JFreeChart("CombinedDomainXYPlot Demo",
										JFreeChart.DEFAULT_TITLE_FONT, plot, true);

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return Series 1.
	 */
	private XYDataset createDataset1() {

		// create dataset 1...
		final XYSeries series1 = new XYSeries("Series 1a");
		series1.add(10.0, 12353.3);
		series1.add(20.0, 13734.4);
		series1.add(30.0, 14525.3);
		series1.add(40.0, 13984.3);
		series1.add(50.0, 12999.4);
		series1.add(60.0, 14274.3);
		series1.add(70.0, 15943.5);
		series1.add(80.0, 14845.3);
		series1.add(90.0, 14645.4);
		series1.add(100.0, 16234.6);
		series1.add(110.0, 17232.3);
		series1.add(120.0, 14232.2);
		series1.add(130.0, 13102.2);
		series1.add(140.0, 14230.2);
		series1.add(150.0, 11235.2);

		final XYSeries series1b = new XYSeries("Series 1b");
		series1b.add(10.0, 15000.3);
		series1b.add(20.0, 11000.4);
		series1b.add(30.0, 17000.3);
		series1b.add(40.0, 15000.3);
		series1b.add(50.0, 14000.4);
		series1b.add(60.0, 12000.3);
		series1b.add(70.0, 11000.5);
		series1b.add(80.0, 12000.3);
		series1b.add(90.0, 13000.4);
		series1b.add(100.0, 12000.6);
		series1b.add(110.0, 13000.3);
		series1b.add(120.0, 17000.2);
		series1b.add(130.0, 18000.2);
		series1b.add(140.0, 16000.2);
		series1b.add(150.0, 17000.2);

		final XYSeriesCollection collection = new XYSeriesCollection();
		collection.addSeries(series1);
		collection.addSeries(series1b);
		return collection;

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return A sample dataset.
	 */
	private XYDataset createDataset2() {

		// create dataset 2...
		final XYSeries series2 = new XYSeries("Series 2");

		series2.add(10.0, 16853.2);
		series2.add(20.0, 19642.3);
		series2.add(30.0, 18253.5);
		series2.add(40.0, 15352.3);
		series2.add(50.0, 13532.0);
		series2.add(100.0, 12635.3);
		series2.add(110.0, 13998.2);
		series2.add(120.0, 11943.2);
		series2.add(130.0, 16943.9);
		series2.add(140.0, 17843.2);
		series2.add(150.0, 16495.3);
		series2.add(160.0, 17943.6);
		series2.add(170.0, 18500.7);
		series2.add(180.0, 19595.9);

		return new XYSeriesCollection(series2);

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

		final CombinedXYPlotDemo4 demo = new CombinedXYPlotDemo4("CombinedDomainXYPlot Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
