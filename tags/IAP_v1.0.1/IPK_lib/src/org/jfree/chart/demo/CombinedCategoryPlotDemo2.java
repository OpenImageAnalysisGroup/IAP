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
 * ------------------------------
 * CombinedCategoryPlotDemo2.java
 * ------------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: CombinedCategoryPlotDemo2.java,v 1.1 2011-01-31 09:01:58 klukas Exp $
 * Changes
 * -------
 * 16-May-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Font;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedRangeCategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer;
import org.jfree.chart.renderer.LineAndShapeRenderer;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo for the {@link CombinedRangeCategoryPlot} class.
 */
public class CombinedCategoryPlotDemo2 extends ApplicationFrame {

	/**
	 * Creates a new demo instance.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public CombinedCategoryPlotDemo2(final String title) {

		super(title);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(createChart());
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a dataset.
	 * 
	 * @return A dataset.
	 */
	public CategoryDataset createDataset1() {

		final DefaultCategoryDataset result = new DefaultCategoryDataset();

		// row keys...
		final String series1 = "First";
		final String series2 = "Second";

		// column keys...
		final String type1 = "Type 1";
		final String type2 = "Type 2";
		final String type3 = "Type 3";
		final String type4 = "Type 4";
		final String type5 = "Type 5";
		final String type6 = "Type 6";
		final String type7 = "Type 7";
		final String type8 = "Type 8";

		result.addValue(1.0, series1, type1);
		result.addValue(4.0, series1, type2);
		result.addValue(3.0, series1, type3);
		result.addValue(5.0, series1, type4);
		result.addValue(5.0, series1, type5);
		result.addValue(7.0, series1, type6);
		result.addValue(7.0, series1, type7);
		result.addValue(8.0, series1, type8);

		result.addValue(5.0, series2, type1);
		result.addValue(7.0, series2, type2);
		result.addValue(6.0, series2, type3);
		result.addValue(8.0, series2, type4);
		result.addValue(4.0, series2, type5);
		result.addValue(4.0, series2, type6);
		result.addValue(2.0, series2, type7);
		result.addValue(1.0, series2, type8);

		return result;

	}

	/**
	 * Creates a dataset.
	 * 
	 * @return A dataset.
	 */
	public CategoryDataset createDataset2() {

		final DefaultCategoryDataset result = new DefaultCategoryDataset();

		// row keys...
		final String series1 = "Third";
		final String series2 = "Fourth";

		// column keys...
		final String sector1 = "Sector 1";
		final String sector2 = "Sector 2";
		final String sector3 = "Sector 3";
		final String sector4 = "Sector 4";

		result.addValue(11.0, series1, sector1);
		result.addValue(14.0, series1, sector2);
		result.addValue(13.0, series1, sector3);
		result.addValue(15.0, series1, sector4);

		result.addValue(15.0, series2, sector1);
		result.addValue(17.0, series2, sector2);
		result.addValue(16.0, series2, sector3);
		result.addValue(18.0, series2, sector4);

		return result;

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
	 * Creates a chart.
	 * 
	 * @return A chart.
	 */
	private JFreeChart createChart() {

		final CategoryDataset dataset1 = createDataset1();
		final CategoryAxis domainAxis1 = new CategoryAxis("Class 1");
		domainAxis1.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		domainAxis1.setMaxCategoryLabelWidthRatio(5.0f);
		final LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
		renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		final CategoryPlot subplot1 = new CategoryPlot(dataset1, domainAxis1, null, renderer1);
		subplot1.setDomainGridlinesVisible(true);

		final CategoryDataset dataset2 = createDataset2();
		final CategoryAxis domainAxis2 = new CategoryAxis("Class 2");
		domainAxis2.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		domainAxis2.setMaxCategoryLabelWidthRatio(5.0f);
		final BarRenderer renderer2 = new BarRenderer();
		renderer2.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		final CategoryPlot subplot2 = new CategoryPlot(dataset2, domainAxis2, null, renderer2);
		subplot2.setDomainGridlinesVisible(true);

		final ValueAxis rangeAxis = new NumberAxis("Value");
		final CombinedRangeCategoryPlot plot = new CombinedRangeCategoryPlot(rangeAxis);
		plot.add(subplot1, 3);
		plot.add(subplot2, 2);
		plot.setOrientation(PlotOrientation.HORIZONTAL);

		final JFreeChart result = new JFreeChart(
							"Combined Range Category Plot Demo",
							new Font("SansSerif", Font.BOLD, 12),
							plot,
							true
							);
		result.getLegend().setAnchor(Legend.SOUTH);
		return result;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final String title = "Combined Category Plot Demo 2";
		final CombinedCategoryPlotDemo2 demo = new CombinedCategoryPlotDemo2(title);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
