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
 * TimePeriodValuesDemo3.java
 * --------------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimePeriodValuesDemo3.java,v 1.1 2011-01-31 09:01:55 klukas Exp $
 * Changes
 * -------
 * 30-Jan-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.text.DateFormat;
import java.util.Date;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.XYBarRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * This demo shows a bar chart with time based data where the time periods are slightly
 * irregular.
 */
public class TimePeriodValuesDemo3 extends ApplicationFrame {

	/**
	 * Creates a new demo instance.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimePeriodValuesDemo3(final String title) {

		super(title);

		final XYDataset data1 = createDataset();
		final XYItemRenderer renderer1 = new XYBarRenderer();

		final DateAxis domainAxis = new DateAxis("Date");
		final ValueAxis rangeAxis = new NumberAxis("Value");

		final XYPlot plot = new XYPlot(data1, domainAxis, rangeAxis, renderer1);

		final JFreeChart chart = new JFreeChart("Time Period Values Demo 3", plot);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMouseZoomable(true, false);
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

		final TimePeriodValues s1 = new TimePeriodValues("Series 1");

		final DateFormat df = DateFormat.getInstance();
		try {
			final Date d0 = df.parse("11/5/2003 0:00:00.000");
			final Date d1 = df.parse("11/5/2003 0:15:00.000");
			final Date d2 = df.parse("11/5/2003 0:30:00.000");
			final Date d3 = df.parse("11/5/2003 0:45:00.000");
			final Date d4 = df.parse("11/5/2003 1:00:00.001");
			final Date d5 = df.parse("11/5/2003 1:14:59.999");
			final Date d6 = df.parse("11/5/2003 1:30:00.000");
			final Date d7 = df.parse("11/5/2003 1:45:00.000");
			final Date d8 = df.parse("11/5/2003 2:00:00.000");
			final Date d9 = df.parse("11/5/2003 2:15:00.000");

			s1.add(new SimpleTimePeriod(d0, d1), 0.39);
			// s1.add(new SimpleTimePeriod(d1, d2), 0.338);
			s1.add(new SimpleTimePeriod(d2, d3), 0.225);
			s1.add(new SimpleTimePeriod(d3, d4), 0.235);
			s1.add(new SimpleTimePeriod(d4, d5), 0.238);
			s1.add(new SimpleTimePeriod(d5, d6), 0.236);
			s1.add(new SimpleTimePeriod(d6, d7), 0.25);
			s1.add(new SimpleTimePeriod(d7, d8), 0.238);
			s1.add(new SimpleTimePeriod(d8, d9), 0.215);
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		final TimePeriodValuesCollection dataset = new TimePeriodValuesCollection();
		dataset.addSeries(s1);
		dataset.setDomainIsPointsInTime(false);

		return dataset;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final TimePeriodValuesDemo3 demo = new TimePeriodValuesDemo3("Time Period Values Demo 3");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
