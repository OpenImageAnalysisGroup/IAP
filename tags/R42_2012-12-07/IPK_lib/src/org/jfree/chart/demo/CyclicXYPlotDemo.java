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
 * CyclicXYPlotDemo.java
 * ---------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: Nicolas Brodu
 * Contributor(s): -;
 * $Id: CyclicXYPlotDemo.java,v 1.1 2011-01-31 09:01:45 klukas Exp $
 * Changes
 * -------
 * 20-Nov-2003 : Creation Date (NB)
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CyclicNumberAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.CyclicXYItemRenderer;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * Demo for an XY plot, with a cyclic axis and renderer
 * 
 * @author Nicolas Brodu
 */
public class CyclicXYPlotDemo extends ApplicationFrame implements ActionListener {

	/** The series. */
	XYSeries series;

	/** The x value. */
	long x = 0;

	/** The y value. */
	double y = 50;

	/** A timer. */
	Timer timer;

	/**
	 * A demonstration application showing an XY plot, with a cyclic axis and renderer
	 * 
	 * @param title
	 *           the frame title.
	 */
	public CyclicXYPlotDemo(final String title) {

		super(title);

		this.series = new XYSeries("Random Data");
		this.series.setMaximumItemCount(50); // Only 50 items are visible at the same time.
		// Keep more as a mean to test this.
		final XYSeriesCollection data = new XYSeriesCollection(this.series);

		final JFreeChart chart = ChartFactory.createXYLineChart(
							"Cyclic XY Plot Demo",
							"X",
							"Y",
							data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);

		final XYPlot plot = chart.getXYPlot();
		plot.setDomainAxis(new CyclicNumberAxis(10, 0));
		plot.setRenderer(new CyclicXYItemRenderer());

		final NumberAxis axis = (NumberAxis) plot.getRangeAxis();
		axis.setAutoRangeIncludesZero(false);
		axis.setAutoRangeMinimumSize(1.0);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(400, 300));
		final JPanel content = new JPanel(new BorderLayout());
		content.add(chartPanel, BorderLayout.CENTER);

		final JButton button1 = new JButton("Start");
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				timer.start();
			}
		});

		final JButton button2 = new JButton("Stop");
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				timer.stop();
			}
		});

		final JButton button3 = new JButton("Step by step");
		button3.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				CyclicXYPlotDemo.this.actionPerformed(null);
			}
		});

		final JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(button1);
		buttonPanel.add(button2);
		buttonPanel.add(button3);

		content.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(content);

		this.timer = new Timer(200, this);
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final CyclicXYPlotDemo demo = new CyclicXYPlotDemo("Cyclic XY Plot Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

		// .start(); // Calls ourselves each half of a second
	}

	/**
	 * Receives notification of an action event.
	 * 
	 * @param e
	 *           the event.
	 */
	public void actionPerformed(final ActionEvent e) {
		double delta = Math.random() * 10 - 5;
		if (delta == -5.0) {
			delta = 0; // balance chances
		}
		this.y += delta;
		this.series.add(this.x++ / 4.0, this.y);
	}

}
