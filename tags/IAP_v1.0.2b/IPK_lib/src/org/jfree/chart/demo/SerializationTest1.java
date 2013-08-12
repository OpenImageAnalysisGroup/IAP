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
 * -----------------------
 * SerializationTest1.java
 * -----------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: SerializationTest1.java,v 1.1 2011-01-31 09:01:56 klukas Exp $
 * Changes
 * -------
 * 02-Mar-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * Based on the DynamicDataDemo class, this demo serializes and deserializes the chart
 * before displaying it - the idea is that this confirms that the serialization process
 * returns a working chart.
 */
public class SerializationTest1 extends ApplicationFrame implements ActionListener {

	/** The time series data. */
	private TimeSeries series;

	/** The most recent value added. */
	private double lastValue = 100.0;

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public SerializationTest1(final String title) {

		super(title);
		this.series = new TimeSeries("Random Data", Millisecond.class);
		TimeSeriesCollection dataset = new TimeSeriesCollection(this.series);
		JFreeChart chart = createChart(dataset);

		// SERIALIZE - DESERIALIZE for testing purposes
		JFreeChart deserializedChart = null;

		try {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			final ObjectOutput out = new ObjectOutputStream(buffer);
			out.writeObject(chart);
			out.close();
			chart = null;
			dataset = null;
			this.series = null;
			System.gc();

			final ObjectInput in = new ObjectInputStream(
								new ByteArrayInputStream(buffer.toByteArray())
								);
			deserializedChart = (JFreeChart) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		final TimeSeriesCollection c = (TimeSeriesCollection) deserializedChart.getXYPlot().getDataset();
		this.series = c.getSeries(0);
		// FINISHED TEST

		final ChartPanel chartPanel = new ChartPanel(deserializedChart);
		final JButton button = new JButton("Add New Data Item");
		button.setActionCommand("ADD_DATA");
		button.addActionListener(this);

		final JPanel content = new JPanel(new BorderLayout());
		content.add(chartPanel);
		content.add(button, BorderLayout.SOUTH);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(content);

	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return A sample chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createTimeSeriesChart(
							"Serialization Test 1",
							"Time",
							"Value",
							dataset,
							true,
							true,
							false
							);
		final XYPlot plot = result.getXYPlot();
		final ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60000.0); // 60 seconds
		return result;
	}

	/**
	 * Handles a click on the button by adding new (random) data.
	 * 
	 * @param e
	 *           the action event.
	 */
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("ADD_DATA")) {
			final double factor = 0.90 + 0.2 * Math.random();
			this.lastValue = this.lastValue * factor;
			final Millisecond now = new Millisecond();
			System.out.println("Now = " + now.toString());
			this.series.add(new Millisecond(), this.lastValue);
		}
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final SerializationTest1 demo = new SerializationTest1("Serialization Test 1");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
