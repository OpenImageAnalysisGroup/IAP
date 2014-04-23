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
 * ---------------------------
 * StackedXYAreaChartDemo.java
 * ---------------------------
 * (C) Copyright 2003, 2004, by Richard Atkinson.
 * Original Author: Richard Atkinson;
 * Contributor(s): -;
 * $Id: StackedXYAreaChartDemo.java,v 1.1 2011-01-31 09:01:51 klukas Exp $
 * Changes:
 * --------
 * 27-Jul-2003 : Initial version (RA);
 * 22-Sep-2003 : Updated to use new DefaultTableXYDataset (RA);
 * 28-Jan-2003 : Renamed StackedAreaXYChartDemo --> StackedXYAreaChartDemo for consistency (DG);
 */
package org.jfree.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StackedXYAreaRenderer;
import org.jfree.chart.renderer.XYAreaRenderer;
import org.jfree.data.DefaultTableXYDataset;
import org.jfree.data.TableXYDataset;
import org.jfree.data.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a
 * stacked XY area chart.
 */
public class StackedXYAreaChartDemo extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public StackedXYAreaChartDemo(final String title) {

		super(title);

		final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.UK);
		final XYSeries series1 = new XYSeries("Series 1", true, false);
		final XYSeries series2 = new XYSeries("Series 2", true, false);
		final XYSeries series3 = new XYSeries("Series 3", true, false);
		try {
			series1.add(sdf.parse("03-Jul-2003").getTime(), 115);
			series1.add(sdf.parse("04-Jul-2003").getTime(), 120);
			series1.add(sdf.parse("07-Jul-2003").getTime(), 125);
			series1.add(sdf.parse("08-Jul-2003").getTime(), 160);
			series1.add(sdf.parse("09-Jul-2003").getTime(), 175);
			series1.add(sdf.parse("10-Jul-2003").getTime(), 140);
			series1.add(sdf.parse("11-Jul-2003").getTime(), 145);
			series1.add(sdf.parse("14-Jul-2003").getTime(), 150);
			series1.add(sdf.parse("15-Jul-2003").getTime(), 155);
			series1.add(sdf.parse("16-Jul-2003").getTime(), 160);
			series1.add(sdf.parse("17-Jul-2003").getTime(), 165);
			series1.add(sdf.parse("18-Jul-2003").getTime(), 170);

			series2.add(sdf.parse("30-Jun-2003").getTime(), 50);
			series2.add(sdf.parse("01-Jul-2003").getTime(), 60);
			series2.add(sdf.parse("02-Jul-2003").getTime(), 70);
			series2.add(sdf.parse("03-Jul-2003").getTime(), 80);
			series2.add(sdf.parse("04-Jul-2003").getTime(), 90);
			series2.add(sdf.parse("07-Jul-2003").getTime(), 100);
			series2.add(sdf.parse("08-Jul-2003").getTime(), 110);
			series2.add(sdf.parse("09-Jul-2003").getTime(), 120);
			series2.add(sdf.parse("10-Jul-2003").getTime(), 130);
			series2.add(sdf.parse("11-Jul-2003").getTime(), 140);
			series2.add(sdf.parse("14-Jul-2003").getTime(), 150);
			series2.add(sdf.parse("15-Jul-2003").getTime(), 160);
			series2.add(sdf.parse("16-Jul-2003").getTime(), 170);
			series2.add(sdf.parse("17-Jul-2003").getTime(), 180);
			series2.add(sdf.parse("18-Jul-2003").getTime(), 190);

			series3.add(sdf.parse("30-Jun-2003").getTime(), 100);
			series3.add(sdf.parse("01-Jul-2003").getTime(), 120);
			series3.add(sdf.parse("02-Jul-2003").getTime(), 110);
			series3.add(sdf.parse("03-Jul-2003").getTime(), 120);
			series3.add(sdf.parse("04-Jul-2003").getTime(), 130);
			series3.add(sdf.parse("07-Jul-2003").getTime(), 135);
			series3.add(sdf.parse("08-Jul-2003").getTime(), 140);
			series3.add(sdf.parse("09-Jul-2003").getTime(), 155);
			series3.add(sdf.parse("10-Jul-2003").getTime(), 130);
			series3.add(sdf.parse("11-Jul-2003").getTime(), 135);
			series3.add(sdf.parse("14-Jul-2003").getTime(), 140);
			series3.add(sdf.parse("15-Jul-2003").getTime(), 165);
			series3.add(sdf.parse("16-Jul-2003").getTime(), 170);
			series3.add(sdf.parse("17-Jul-2003").getTime(), 165);
			series3.add(sdf.parse("18-Jul-2003").getTime(), 140);
		} catch (ParseException e) {
			// Not likely, given that the dates are hard-coded
			e.printStackTrace();
		}
		final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);

		final JFreeChart chart = createChart(dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
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
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return A chart.
	 */
	private JFreeChart createChart(final TableXYDataset dataset) {

		final StandardXYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator(
							StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
							new SimpleDateFormat("dd-MMM-yyyy", Locale.UK), NumberFormat.getInstance()
							);
		final DateAxis xAxis = new DateAxis("Domain (X)");
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);

		final NumberAxis yAxis = new NumberAxis("Range (Y)");
		yAxis.setAutoRangeIncludesZero(true);
		final StackedXYAreaRenderer renderer = new StackedXYAreaRenderer(
							XYAreaRenderer.AREA_AND_SHAPES, toolTipGenerator, null
							);
		renderer.setOutline(true);
		renderer.setSeriesPaint(0, new Color(255, 255, 206));
		renderer.setSeriesPaint(1, new Color(206, 230, 255));
		renderer.setSeriesPaint(2, new Color(255, 230, 230));
		renderer.setShapePaint(Color.gray);
		renderer.setShapeStroke(new BasicStroke(0.5f));
		renderer.setShape(new Ellipse2D.Double(-3, -3, 6, 6));
		final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

		final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

		return chart;
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {
		final StackedXYAreaChartDemo demo = new StackedXYAreaChartDemo(
							"Stacked Area XY Chart Demo"
							);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

}
