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
 * ---------------------
 * DynamicDataDemo3.java
 * ---------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: DynamicDataDemo3.java,v 1.1 2011-01-31 09:01:43 klukas Exp $
 * Changes
 * -------
 * 02-Mar-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * A demonstration application showing a time series chart where you can dynamically add
 * (random) data by clicking on a button.
 */
public class DynamicDataDemo3 extends ApplicationFrame implements ActionListener {

	/** The number of subplots. */
	public static final int SUBPLOT_COUNT = 3;

	/** The datasets. */
	private TimeSeriesCollection[] datasets;

	/** The most recent value added to series 1. */
	private double[] lastValue = new double[SUBPLOT_COUNT];

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public DynamicDataDemo3(final String title) {

		super(title);

		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Time"));
		this.datasets = new TimeSeriesCollection[SUBPLOT_COUNT];

		for (int i = 0; i < SUBPLOT_COUNT; i++) {
			this.lastValue[i] = 100.0;
			final TimeSeries series = new TimeSeries("Random " + i, Millisecond.class);
			this.datasets[i] = new TimeSeriesCollection(series);
			final NumberAxis rangeAxis = new NumberAxis("Y" + i);
			rangeAxis.setAutoRangeIncludesZero(false);
			final XYPlot subplot = new XYPlot(
								this.datasets[i], null, rangeAxis, new StandardXYItemRenderer()
								);
			subplot.setBackgroundPaint(Color.lightGray);
			subplot.setDomainGridlinePaint(Color.white);
			subplot.setRangeGridlinePaint(Color.white);
			plot.add(subplot);
		}

		final JFreeChart chart = new JFreeChart("Dynamic Data Demo 3", plot);
		chart.getLegend().setAnchor(Legend.EAST);
		chart.setBorderPaint(Color.black);
		chart.setBorderVisible(true);
		chart.setBackgroundPaint(Color.white);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 4, 4, 4, 4));
		final ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60000.0); // 60 seconds

		final JPanel content = new JPanel(new BorderLayout());

		final ChartPanel chartPanel = new ChartPanel(chart);
		content.add(chartPanel);

		final JPanel buttonPanel = new JPanel(new FlowLayout());

		for (int i = 0; i < SUBPLOT_COUNT; i++) {
			final JButton button = new JButton("Series " + i);
			button.setActionCommand("ADD_DATA_" + i);
			button.addActionListener(this);
			buttonPanel.add(button);
		}
		final JButton buttonAll = new JButton("ALL");
		buttonAll.setActionCommand("ADD_ALL");
		buttonAll.addActionListener(this);
		buttonPanel.add(buttonAll);

		content.add(buttonPanel, BorderLayout.SOUTH);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 470));
		chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setContentPane(content);

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
	 * Handles a click on the button by adding new (random) data.
	 * 
	 * @param e
	 *           the action event.
	 */
	public void actionPerformed(final ActionEvent e) {

		for (int i = 0; i < SUBPLOT_COUNT; i++) {
			if (e.getActionCommand().endsWith(String.valueOf(i))) {
				final Millisecond now = new Millisecond();
				System.out.println("Now = " + now.toString());
				this.lastValue[i] = this.lastValue[i] * (0.90 + 0.2 * Math.random());
				this.datasets[i].getSeries(0).add(new Millisecond(), this.lastValue[i]);
			}
		}

		if (e.getActionCommand().equals("ADD_ALL")) {
			final Millisecond now = new Millisecond();
			System.out.println("Now = " + now.toString());
			for (int i = 0; i < SUBPLOT_COUNT; i++) {
				this.lastValue[i] = this.lastValue[i] * (0.90 + 0.2 * Math.random());
				this.datasets[i].getSeries(0).add(new Millisecond(), this.lastValue[i]);
			}
		}

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final DynamicDataDemo3 demo = new DynamicDataDemo3("Dynamic Data Demo 3");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
