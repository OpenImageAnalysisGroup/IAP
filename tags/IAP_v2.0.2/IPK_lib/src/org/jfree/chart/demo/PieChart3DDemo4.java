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
 * PieChart3DDemo4.java
 * --------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: PieChart3DDemo4.java,v 1.1 2011-01-31 09:01:47 klukas Exp $
 * Changes
 * -------
 * 31-Mar-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.DefaultPieDataset;
import org.jfree.data.PieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Rotation;

/**
 * A pie chart with a custom label generator.
 */
public class PieChart3DDemo4 extends ApplicationFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public PieChart3DDemo4(final String title) {

		super(title);
		final PieDataset dataset = createSampleDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample dataset for the demo.
	 * 
	 * @return A sample dataset.
	 */
	private PieDataset createSampleDataset() {

		final DefaultPieDataset result = new DefaultPieDataset();
		result.setValue("Java", new Double(43.2));
		result.setValue("Visual Basic", new Double(10.0));
		result.setValue("C/C++", new Double(17.5));
		result.setValue("PHP", new Double(32.5));
		result.setValue("Perl", new Double(1.0));
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
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @return A chart.
	 */
	private JFreeChart createChart(final PieDataset dataset) {

		final JFreeChart chart = ChartFactory.createPieChart3D(
							"Pie Chart 3D Demo 4", // chart title
				dataset, // data
				true, // include legend
				true,
							false
							);

		final PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.5f);
		plot.setNoDataMessage("No data to display");
		plot.setLabelGenerator(new CustomLabelGenerator());
		return chart;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final PieChart3DDemo4 demo = new PieChart3DDemo4("Pie Chart 3D Demo 4");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

	/**
	 * A custom label generator (returns null for one item as a test).
	 */
	static class CustomLabelGenerator implements PieSectionLabelGenerator {

		/**
		 * Generates a label for a pie section.
		 * 
		 * @param dataset
		 *           the dataset (<code>null</code> not permitted).
		 * @param key
		 *           the section key (<code>null</code> not permitted).
		 * @return the label (possibly <code>null</code>).
		 */
		public String generateSectionLabel(final PieDataset dataset, final Comparable key) {
			String result = null;
			if (dataset != null) {
				if (!key.equals("PHP")) {
					result = key.toString();
				}
			}
			return result;
		}

	}

}
