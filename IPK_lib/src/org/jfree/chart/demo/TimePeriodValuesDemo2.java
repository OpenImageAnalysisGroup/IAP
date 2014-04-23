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
 * TimePeriodValuesDemo2.java
 * --------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TimePeriodValuesDemo2.java,v 1.1 2011-01-31 09:01:51 klukas Exp $
 * Changes
 * -------
 * 30-Jul-2002 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.XYBarRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * An example of....
 */
public class TimePeriodValuesDemo2 extends ApplicationFrame {

	/**
	 * A demonstration application showing how to....
	 * 
	 * @param title
	 *           the frame title.
	 */
	public TimePeriodValuesDemo2(final String title) {

		super(title);

		final XYDataset data1 = createDataset();
		final XYItemRenderer renderer1 = new XYBarRenderer();

		final DateAxis domainAxis = new DateAxis("Date");
		final ValueAxis rangeAxis = new NumberAxis("Value");

		final XYPlot plot = new XYPlot(data1, domainAxis, rangeAxis, renderer1);

		final JFreeChart chart = new JFreeChart("Time Period Values Demo", plot);
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
		final Day d1 = new Day();
		final Day d2 = (Day) d1.next();
		final Day d3 = (Day) d2.next();
		final Day d4 = (Day) d3.next();
		final Day d5 = (Day) d4.next();
		final Day d6 = (Day) d5.next();
		final Day d7 = (Day) d6.next();

		s1.add(new SimpleTimePeriod(d6.getStart(), d6.getEnd()), 74.95);
		s1.add(new SimpleTimePeriod(d1.getStart(), d2.getEnd()), 55.75);
		s1.add(new SimpleTimePeriod(d7.getStart(), d7.getEnd()), 90.45);
		s1.add(new SimpleTimePeriod(d3.getStart(), d5.getEnd()), 105.75);

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

		final TimePeriodValuesDemo2 demo = new TimePeriodValuesDemo2("Time Period Values Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
