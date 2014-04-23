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
 * -------------------------
 * PlotOrientationDemo2.java
 * -------------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: PlotOrientationDemo2.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes
 * -------
 * 03-Jun-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.Layer;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * A demo showing eight plots with various inverted axis and plot orientation combinations.
 */
public class PlotOrientationDemo2 extends ApplicationFrame {

	/** The number of charts. */
	private static final int CHART_COUNT = 8;

	/** The datasets. */
	private XYDataset[] datasets = new XYDataset[CHART_COUNT];

	/** The charts. */
	private JFreeChart[] charts = new JFreeChart[CHART_COUNT];

	/** The chart panels. */
	private ChartPanel[] panels = new ChartPanel[CHART_COUNT];

	/**
	 * Creates a new demo instance.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public PlotOrientationDemo2(String title) {

		super(title);
		JPanel panel = new JPanel(new GridLayout(2, 4));
		for (int i = 0; i < CHART_COUNT; i++) {
			this.datasets[i] = createDataset(i);
			this.charts[i] = createChart(i, this.datasets[i]);
			XYPlot plot = this.charts[i].getXYPlot();
			XYShapeAnnotation a1 = new XYShapeAnnotation(
								new Rectangle2D.Double(1.0, 2.0, 2.0, 3.0), new BasicStroke(1.0f), Color.blue
								);
			XYLineAnnotation a2 = new XYLineAnnotation(0.0, -5.0, 10.0, -5.0);

			plot.addAnnotation(a1);
			plot.addAnnotation(a2);
			plot.addDomainMarker(new IntervalMarker(5.0, 10.0), Layer.BACKGROUND);
			plot.addRangeMarker(new IntervalMarker(-2.0, 0.0), Layer.BACKGROUND);

			this.panels[i] = new ChartPanel(this.charts[i]);
		}
		this.charts[1].getXYPlot().getDomainAxis().setInverted(true);
		this.charts[2].getXYPlot().getRangeAxis().setInverted(true);
		this.charts[3].getXYPlot().getDomainAxis().setInverted(true);
		this.charts[3].getXYPlot().getRangeAxis().setInverted(true);

		this.charts[5].getXYPlot().getDomainAxis().setInverted(true);
		this.charts[6].getXYPlot().getRangeAxis().setInverted(true);
		this.charts[4].getXYPlot().getDomainAxis().setInverted(true);
		this.charts[4].getXYPlot().getRangeAxis().setInverted(true);

		this.charts[4].getXYPlot().setOrientation(PlotOrientation.HORIZONTAL);
		this.charts[5].getXYPlot().setOrientation(PlotOrientation.HORIZONTAL);
		this.charts[6].getXYPlot().setOrientation(PlotOrientation.HORIZONTAL);
		this.charts[7].getXYPlot().setOrientation(PlotOrientation.HORIZONTAL);

		panel.add(this.panels[0]);
		panel.add(this.panels[1]);
		panel.add(this.panels[4]);
		panel.add(this.panels[5]);
		panel.add(this.panels[2]);
		panel.add(this.panels[3]);
		panel.add(this.panels[6]);
		panel.add(this.panels[7]);

		panel.setPreferredSize(new Dimension(800, 600));
		setContentPane(panel);

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @param index
	 *           the dataset index.
	 * @return A dataset.
	 */
	private XYDataset createDataset(int index) {
		XYSeries series1 = new XYSeries("Series " + (index + 1));
		series1.add(-10.0, -5.0);
		series1.add(10.0, 5.0);
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);
		return dataset;
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param index
	 *           the chart index.
	 * @param dataset
	 *           the dataset.
	 * @return A chart.
	 */
	private JFreeChart createChart(int index, XYDataset dataset) {

		// create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
							"Chart " + (index + 1), // chart title
				"X", // x axis label
				"Y", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL,
							false, // include legend
				false, // tooltips
				false // urls
				);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
		renderer.setPlotShapes(true);
		renderer.setShapesFilled(true);
		// change the auto tick unit selection to integer units only...
		ValueAxis domainAxis = plot.getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;

	}

	/**
	 * The starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(String[] args) {
		PlotOrientationDemo2 demo = new PlotOrientationDemo2("Plot Orientation Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

}
