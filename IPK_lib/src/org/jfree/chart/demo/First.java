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
 * ----------
 * First.java
 * ----------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: First.java,v 1.1 2011-01-31 09:01:47 klukas Exp $
 * Changes
 * -------
 * 16-Jan-2002 : Version 1 (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.DefaultPieDataset;

/**
 * A simple introduction to using JFreeChart. This demo is described in the
 * JFreeChart Developer Guide, available to purchasers of commercial support
 * packages for JFreeChart.
 */
public class First {

	/**
	 * The starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		// create a dataset...
		final DefaultPieDataset data = new DefaultPieDataset();
		data.setValue("Category 1", 43.2);
		data.setValue("Category 2", 27.9);
		data.setValue("Category 3", 79.5);

		// create a chart...
		final JFreeChart chart = ChartFactory.createPieChart(
							"Sample Pie Chart",
							data,
							true, // legend?
				true, // tooltips?
				false // URLs?
				);

		// create and display a frame...
		final ChartFrame frame = new ChartFrame("First", chart);
		frame.pack();
		frame.setVisible(true);

	}

}
