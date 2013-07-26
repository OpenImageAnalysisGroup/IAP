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
 * BoxAndWhiskerDemo.java
 * ----------------------
 * (C) Copyright 2003, 2004, by David Browning and Contributors.
 * Original Author: David Browning (for the Australian Institute of Marine Science);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: BoxAndWhiskerDemo.java,v 1.1 2011-01-31 09:01:50 klukas Exp $
 * Changes
 * -------
 * 21-Aug-2003 : Version 1, contributed by David Browning (for the Australian Institute of
 * Marine Science);
 * 27-Aug-2003 : Renamed BoxAndWhiskerCategoryDemo --> BoxAndWhiskerDemo, moved dataset creation
 * into the demo (DG);
 */

package org.jfree.chart.demo;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Log;
import org.jfree.util.LogContext;

/**
 * Demonstration of a box-and-whisker chart using a {@link CategoryPlot}.
 * 
 * @author David Browning
 */
public class BoxAndWhiskerDemo extends ApplicationFrame {

	/** Access to logging facilities. */
	private static final LogContext LOGGER = Log.createContext(BoxAndWhiskerDemo.class);

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public BoxAndWhiskerDemo(final String title) {

		super(title);

		final BoxAndWhiskerCategoryDataset dataset = createSampleDataset();

		final CategoryAxis xAxis = new CategoryAxis("Type");
		final NumberAxis yAxis = new NumberAxis("Value");
		yAxis.setAutoRangeIncludesZero(false);
		final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
		renderer.setFillBox(false);
		renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

		final JFreeChart chart = new JFreeChart(
							"Box-and-Whisker Demo",
							new Font("SansSerif", Font.BOLD, 14),
							plot,
							true
							);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return A sample dataset.
	 */
	private BoxAndWhiskerCategoryDataset createSampleDataset() {

		final int seriesCount = 3;
		final int categoryCount = 4;
		final int entityCount = 22;

		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		for (int i = 0; i < seriesCount; i++) {
			for (int j = 0; j < categoryCount; j++) {
				final List list = new ArrayList();
				// add some values...
				for (int k = 0; k < entityCount; k++) {
					final double value1 = 10.0 + Math.random() * 3;
					list.add(new Double(value1));
					final double value2 = 11.25 + Math.random(); // concentrate values in the middle
					list.add(new Double(value2));
				}
				LOGGER.debug("Adding series " + i);
				LOGGER.debug(list.toString());
				dataset.add(list, "Series " + i, " Type " + j);
			}

		}

		return dataset;
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
	 * For testing from the command line.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		// Log.getInstance().addTarget(new PrintStreamLogTarget(System.out));
		final BoxAndWhiskerDemo demo = new BoxAndWhiskerDemo("Box-and-Whisker Chart Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
