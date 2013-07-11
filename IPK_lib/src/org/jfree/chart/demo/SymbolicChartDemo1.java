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
 * ----------------------
 * SymbolicChartDemo.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: SymbolicChartDemo1.java,v 1.1 2011-01-31 09:01:50 klukas Exp $
 * Changes
 * -------
 * 14-Feb-2003 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.SymbolicXYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.YisSymbolic;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * An example of...
 */
public class SymbolicChartDemo1 extends ApplicationFrame {

	/**
	 * A demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public SymbolicChartDemo1(final String title) {

		super(title);

		// create a title...
		final XYDataset dataset = createDataset();

		final ValueAxis domainAxis = new NumberAxis("X");
		final SymbolicAxis symbolicAxis = new SymbolicAxis("Y", ((YisSymbolic) dataset).getYSymbolicValues());

		final XYPlot plot = new XYPlot(dataset, domainAxis, symbolicAxis, null);
		final XYItemRenderer renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES, new SymbolicXYItemLabelGenerator()
							);
		plot.setRenderer(renderer);
		final JFreeChart chart = new JFreeChart(title, plot);

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
	 * Creates a dataset, consisting of two series of monthly data.
	 * 
	 * @return the dataset.
	 */
	public XYDataset createDataset() {

		final String[] sData = { "Giraffe", "Gazelle", "Zebra", "Gnu" };
		final SampleYSymbolicDataset data = new SampleYSymbolicDataset("BY Sample", 40, sData, 4, 20,
							new String[] { "B Fall", "B Spring", "B Summer", "B Winter" });
		return data;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final SymbolicChartDemo1 demo = new SymbolicChartDemo1("Symbolic Chart Demo 1");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
