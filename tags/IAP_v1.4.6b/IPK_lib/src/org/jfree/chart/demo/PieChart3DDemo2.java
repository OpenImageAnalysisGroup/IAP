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
 * --------------------
 * PieChart3DDemo2.java
 * --------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: PieChart3DDemo2.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes
 * -------
 * 18-Oct-2002 : Version 1 (DG);
 * 23-Dec-2003 : Renamed Pie3DChartDemo2 --> PieChart3DDemo2 (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.DefaultPieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Rotation;

/**
 * A rotating 3D pie chart.
 */
public class PieChart3DDemo2 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public PieChart3DDemo2(final String title) {

		super(title);

		// create a dataset...
		final DefaultPieDataset data = new DefaultPieDataset();
		data.setValue("Java", new Double(43.2));
		data.setValue("Visual Basic", new Double(10.0));
		data.setValue("C/C++", new Double(17.5));
		data.setValue("PHP", new Double(32.5));
		data.setValue("Perl", new Double(12.5));

		// create the chart...
		final JFreeChart chart = ChartFactory.createPieChart3D(
							"Pie Chart 3D Demo 2", // chart title
				data, // data
				true, // include legend
				true,
							false
							);

		chart.setBackgroundPaint(Color.yellow);
		final PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(270);
		plot.setDirection(Rotation.ANTICLOCKWISE);
		plot.setForegroundAlpha(0.60f);
		plot.setInteriorGap(0.33);
		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

		final Rotator rotator = new Rotator(plot);
		rotator.start();

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final PieChart3DDemo2 demo = new PieChart3DDemo2("Pie Chart 3D Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

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
 * The rotator.
 */
class Rotator extends Timer implements ActionListener {

	/** The plot. */
	private PiePlot3D plot;

	/** The angle. */
	private int angle = 270;

	/**
	 * Constructor.
	 * 
	 * @param plot
	 *           the plot.
	 */
	Rotator(final PiePlot3D plot) {
		super(100, null);
		this.plot = plot;
		addActionListener(this);
	}

	/**
	 * Modifies the starting angle.
	 * 
	 * @param event
	 *           the action event.
	 */
	public void actionPerformed(final ActionEvent event) {
		this.plot.setStartAngle(this.angle);
		this.angle = this.angle + 1;
		if (this.angle == 360) {
			this.angle = 0;
		}
	}

}
