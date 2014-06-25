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
 * DynamicDataDemo2.java
 * ---------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: DynamicDataDemo2.java,v 1.1 2011-01-31 09:01:53 klukas Exp $
 * Changes
 * -------
 * 01-Sep-2003 : Version 1, based on DynamicDataDemo (DG);
 * 27-Apr-2004 : Updated for changes to the XYPlot class (DG);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.DefaultXYItemRenderer;
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
public class DynamicDataDemo2 extends ApplicationFrame implements ActionListener {

	/** Series 1. */
	private TimeSeries series1;

	/** Series 2. */
	private TimeSeries series2;

	/** The most recent value added to series 1. */
	private double lastValue1 = 100.0;

	/** The most recent value added to series 2. */
	private double lastValue2 = 500.0;

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public DynamicDataDemo2(final String title) {

		super(title);
		this.series1 = new TimeSeries("Random 1", Millisecond.class);
		this.series2 = new TimeSeries("Random 2", Millisecond.class);
		final TimeSeriesCollection dataset1 = new TimeSeriesCollection(this.series1);
		final TimeSeriesCollection dataset2 = new TimeSeriesCollection(this.series2);
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							"Dynamic Data Demo 2", "Time", "Value", dataset1, true, true, false
							);
		chart.setBackgroundPaint(Color.white);

		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 4, 4, 4, 4));
		final ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60000.0); // 60 seconds

		plot.setDataset(1, dataset2);
		final NumberAxis rangeAxis2 = new NumberAxis("Range Axis 2");
		rangeAxis2.setAutoRangeIncludesZero(false);
		plot.setRenderer(1, new DefaultXYItemRenderer());
		plot.setRangeAxis(1, rangeAxis2);
		plot.mapDatasetToRangeAxis(1, 1);

		final JPanel content = new JPanel(new BorderLayout());

		final ChartPanel chartPanel = new ChartPanel(chart);
		content.add(chartPanel);

		final JButton button1 = new JButton("Add To Series 1");
		button1.setActionCommand("ADD_DATA_1");
		button1.addActionListener(this);

		final JButton button2 = new JButton("Add To Series 2");
		button2.setActionCommand("ADD_DATA_2");
		button2.addActionListener(this);

		final JButton button3 = new JButton("Add To Both");
		button3.setActionCommand("ADD_BOTH");
		button3.addActionListener(this);

		final JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(button1);
		buttonPanel.add(button2);
		buttonPanel.add(button3);

		content.add(buttonPanel, BorderLayout.SOUTH);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
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
		boolean add1 = false;
		boolean add2 = false;
		if (e.getActionCommand().equals("ADD_DATA_1")) {
			add1 = true;
		} else
			if (e.getActionCommand().equals("ADD_DATA_2")) {
				add2 = true;
			} else
				if (e.getActionCommand().equals("ADD_BOTH")) {
					add1 = true;
					add2 = true;
				}
		if (add1) {
			final double factor = 0.90 + 0.2 * Math.random();
			this.lastValue1 = this.lastValue1 * factor;
			final Millisecond now = new Millisecond();
			System.out.println("Now = " + now.toString());
			this.series1.add(new Millisecond(), this.lastValue1);
		}
		if (add2) {
			final double factor = 0.90 + 0.2 * Math.random();
			this.lastValue2 = this.lastValue2 * factor;
			final Millisecond now = new Millisecond();
			System.out.println("Now = " + now.toString());
			this.series2.add(new Millisecond(), this.lastValue2);
		}
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final DynamicDataDemo2 demo = new DynamicDataDemo2("Dynamic Data Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
