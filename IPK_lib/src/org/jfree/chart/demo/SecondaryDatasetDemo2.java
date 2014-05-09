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
 * SecondaryDatasetDemo2.java
 * --------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: SecondaryDatasetDemo2.java,v 1.1 2011-01-31 09:01:56 klukas Exp $
 * Changes
 * -------
 * 30-Jan-2004 : Version 1 (DG);
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
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.LineAndShapeRenderer;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * A demo showing the addition and removal of secondary datasets / renderers.
 */
public class SecondaryDatasetDemo2 extends ApplicationFrame implements ActionListener {

	/** The plot. */
	private CategoryPlot plot;

	/** The index of the last dataset added. */
	private int secondaryDatasetIndex = 0;

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public SecondaryDatasetDemo2(final String title) {

		super(title);
		final CategoryDataset dataset1 = createRandomDataset("Series 1");
		final JFreeChart chart = ChartFactory.createLineChart(
							"Secondary Dataset Demo 2", "Category", "Value",
							dataset1, PlotOrientation.VERTICAL, true, true, false
							);
		chart.setBackgroundPaint(Color.white);

		this.plot = chart.getCategoryPlot();
		this.plot.setBackgroundPaint(Color.lightGray);
		this.plot.setDomainGridlinePaint(Color.white);
		this.plot.setRangeGridlinePaint(Color.white);
		this.plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 4, 4, 4, 4));

		final NumberAxis rangeAxis = (NumberAxis) this.plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);

		final JPanel content = new JPanel(new BorderLayout());

		final ChartPanel chartPanel = new ChartPanel(chart);
		content.add(chartPanel);

		final JButton button1 = new JButton("Add Dataset");
		button1.setActionCommand("ADD_DATASET");
		button1.addActionListener(this);

		final JButton button2 = new JButton("Remove Dataset");
		button2.setActionCommand("REMOVE_DATASET");
		button2.addActionListener(this);

		final JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(button1);
		buttonPanel.add(button2);

		content.add(buttonPanel, BorderLayout.SOUTH);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(content);

	}

	/**
	 * Creates a random dataset.
	 * 
	 * @param name
	 *           the series name.
	 * @return A random dataset.
	 */
	private CategoryDataset createRandomDataset(final String name) {
		final DefaultCategoryDataset result = new DefaultCategoryDataset();
		double value = 100.0;
		for (int i = 0; i < 10; i++) {
			final String key = "T" + i;
			value = value * (1.0 + Math.random() / 100);
			result.addValue(value, name, key);
		}
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
	 * Handles a click on the button by adding new (random) data.
	 * 
	 * @param e
	 *           the action event.
	 */
	public void actionPerformed(final ActionEvent e) {

		if (e.getActionCommand().equals("ADD_DATASET")) {
			if (this.secondaryDatasetIndex < 20) {
				this.secondaryDatasetIndex++;
				this.plot.setDataset(
									this.secondaryDatasetIndex,
									createRandomDataset("S" + this.secondaryDatasetIndex)
									);
				this.plot.setRenderer(
									this.secondaryDatasetIndex, new LineAndShapeRenderer(LineAndShapeRenderer.LINES)
									);
			}
		} else
			if (e.getActionCommand().equals("REMOVE_DATASET")) {
				if (this.secondaryDatasetIndex > 0) {
					this.plot.setDataset(this.secondaryDatasetIndex, null);
					this.plot.setRenderer(this.secondaryDatasetIndex, null);
					this.secondaryDatasetIndex--;
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

		final SecondaryDatasetDemo2 demo = new SecondaryDatasetDemo2("Secondary Dataset Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
