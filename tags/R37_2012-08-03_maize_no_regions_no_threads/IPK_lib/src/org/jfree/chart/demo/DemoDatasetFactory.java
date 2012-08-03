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
 * DemoDatasetFactory.java
 * -----------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Bryan Scott;
 * Bill Kelemen;
 * David Browning;
 * Robert Redburn;
 * $Id: DemoDatasetFactory.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes
 * -------
 * 10-Dec-2001 : Version 1 (DG);
 * 15-Mar-2002 : Added createHighLowOpenCloseDataset() method (DG);
 * 20-Jun-2002 : Added createMeterDataset() method (BRS);
 * 24-Jun-2002 : Moved createGanttDataset() method from GanttDemo (BRS);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 24-May-2003 : Added createSegmentedHighLowDataset(..) (BK);
 * 05-Aug-2003 : Added createBoxAndWhiskerDataset() method (DB);
 * 08-Aug-2003 : Refined createBoxAndWhiskerDataset() method (DB);
 * 19-Jan-2004 : Added createWaferMapDataset() and
 * createRandomWaferMapDataset() methods (RR);
 */

package org.jfree.chart.demo;

import java.util.Date;
import java.util.Random;

import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.DefaultHighLowDataset;
import org.jfree.data.DefaultWindDataset;
import org.jfree.data.HighLowDataset;
import org.jfree.data.IntervalCategoryDataset;
import org.jfree.data.SignalsDataset;
import org.jfree.data.WaferMapDataset;
import org.jfree.data.WindDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;
import org.jfree.date.DateUtilities;
import org.jfree.date.MonthConstants;

/**
 * A utility class for generating sample datasets for the demos.
 * <p>
 * These datasets are hard-coded so that they are easily accessible for the demonstration applications. In a real application, you would create datasets
 * dynamically by reading data from a file, a database, or some other source.
 */
public abstract class DemoDatasetFactory {

	/**
	 * Creates and returns a {@link CategoryDataset} for the demo charts.
	 * 
	 * @return a sample dataset.
	 */
	public static CategoryDataset createCategoryDataset() {

		final double[][] data = new double[][]
				{ { 10.0, 4.0, 15.0, 14.0 },
									{ -5.0, -7.0, 14.0, -3.0 },
									{ 6.0, 17.0, -12.0, 7.0 },
									{ 7.0, 15.0, 11.0, 0.0 },
									{ -8.0, -6.0, 10.0, -9.0 },
									{ 9.0, 8.0, 0.0, 6.0 },
									{ -10.0, 9.0, 7.0, 7.0 },
									{ 11.0, 13.0, 9.0, 9.0 },
									{ -3.0, 7.0, 11.0, -10.0 } };

		return DatasetUtilities.createCategoryDataset("Series ", "Category ", data);

	}

	/**
	 * Creates and returns a category dataset with JUST ONE CATEGORY for the demo charts.
	 * 
	 * @return a sample category dataset.
	 */
	public static CategoryDataset createSingleCategoryDataset() {

		final Number[][] data = new Integer[][]
				{ { new Integer(10) },
									{ new Integer(-5) },
									{ new Integer(6) },
									{ new Integer(7) },
									{ new Integer(-8) },
									{ new Integer(9) },
									{ new Integer(-10) },
									{ new Integer(11) },
									{ new Integer(-3) } };

		return DatasetUtilities.createCategoryDataset("Series ", "Category ", data);

	}

	/**
	 * Creates and returns a category dataset for the demo charts.
	 * 
	 * @return a sample category dataset.
	 */
	public static CategoryDataset createSingleSeriesCategoryDataset() {

		final double[][] data = new double[][] { { 10.0, -4.0, 15.0, 14.0 } };

		return DatasetUtilities.createCategoryDataset("Series ", "Category ", data);

	}

	/**
	 * Returns a null interval category dataset.
	 * 
	 * @return null.
	 */
	public static IntervalCategoryDataset createIntervalCategoryDataset() {

		return null;

	}

	/**
	 * Returns a sample XY dataset.
	 * 
	 * @return a sample XY dataset.
	 */
	public static XYDataset createSampleXYDataset() {
		return new SampleXYDataset();
	}

	/**
	 * Returns a dataset consisting of one annual series.
	 * 
	 * @return a sample time series collection.
	 */
	public static TimeSeriesCollection createTimeSeriesCollection1() {

		final TimeSeries t1 = new TimeSeries("Annual", "Year", "Value", Year.class);
		try {
			t1.add(new Year(1990), new Double(50.1));
			t1.add(new Year(1991), new Double(12.3));
			t1.add(new Year(1992), new Double(23.9));
			t1.add(new Year(1993), new Double(83.4));
			t1.add(new Year(1994), new Double(-34.7));
			t1.add(new Year(1995), new Double(76.5));
			t1.add(new Year(1996), new Double(10.0));
			t1.add(new Year(1997), new Double(-14.7));
			t1.add(new Year(1998), new Double(43.9));
			t1.add(new Year(1999), new Double(49.6));
			t1.add(new Year(2000), new Double(37.2));
			t1.add(new Year(2001), new Double(17.1));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return new TimeSeriesCollection(t1);

	}

	/**
	 * Creates a time series collection containing JPY/GBP exchange rates.
	 * 
	 * @return a sample time series collection.
	 */
	public static TimeSeriesCollection createTimeSeriesCollection2() {

		final TimeSeriesCollection data = new TimeSeriesCollection();
		data.addSeries(createJPYTimeSeries());
		return data;

	}

	/**
	 * Creates a time series collection containing USD/GBP and EUR/GBP exchange rates.
	 * 
	 * @return a sample time series collection.
	 */
	public static TimeSeriesCollection createTimeSeriesCollection3() {

		final TimeSeriesCollection collection = new TimeSeriesCollection();
		collection.addSeries(createUSDTimeSeries());
		collection.addSeries(createEURTimeSeries());
		return collection;

	}

	/**
	 * Returns a time series dataset using millisecond data.
	 * 
	 * @return a sample time series collection.
	 */
	public static TimeSeriesCollection createTimeSeriesCollection4() {

		final TimeSeries t4 = new TimeSeries("Test",
																	"Millisecond", "Value", FixedMillisecond.class);
		final Date now = new Date();
		try {
			t4.add(new FixedMillisecond(now.getTime() + 0), new Double(50.1));
			t4.add(new FixedMillisecond(now.getTime() + 1), new Double(12.3));
			t4.add(new FixedMillisecond(now.getTime() + 2), new Double(23.9));
			t4.add(new FixedMillisecond(now.getTime() + 3), new Double(83.4));
			t4.add(new FixedMillisecond(now.getTime() + 4), new Double(34.7));
			t4.add(new FixedMillisecond(now.getTime() + 5), new Double(76.5));
			t4.add(new FixedMillisecond(now.getTime() + 6), new Double(150.0));
			t4.add(new FixedMillisecond(now.getTime() + 7), new Double(414.7));
			t4.add(new FixedMillisecond(now.getTime() + 8), new Double(1500.9));
			t4.add(new FixedMillisecond(now.getTime() + 9), new Double(4530.6));
			t4.add(new FixedMillisecond(now.getTime() + 10), new Double(7337.2));
			t4.add(new FixedMillisecond(now.getTime() + 11), new Double(9117.1));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return new TimeSeriesCollection(t4);

	}

	/**
	 * Returns a time series of the daily USD/GBP exchange rates in 2001 (to date), for use in
	 * the JFreeChart demonstration application.
	 * <P>
	 * You wouldn't normally create a time series in this way. Typically, values would be read from a database.
	 * 
	 * @return a time series.
	 */
	public static TimeSeries createUSDTimeSeries() {

		final TimeSeries t1 = new TimeSeries("USD/GBP");
		try {
			t1.add(new Day(2, MonthConstants.JANUARY, 2001), 1.4956);
			t1.add(new Day(3, MonthConstants.JANUARY, 2001), new Double(1.5047));
			t1.add(new Day(4, MonthConstants.JANUARY, 2001), new Double(1.4931));
			t1.add(new Day(5, MonthConstants.JANUARY, 2001), new Double(1.4955));
			t1.add(new Day(8, MonthConstants.JANUARY, 2001), new Double(1.4994));
			t1.add(new Day(9, MonthConstants.JANUARY, 2001), new Double(1.4911));
			t1.add(new Day(10, MonthConstants.JANUARY, 2001), new Double(1.4903));
			t1.add(new Day(11, MonthConstants.JANUARY, 2001), new Double(1.4947));
			t1.add(new Day(12, MonthConstants.JANUARY, 2001), new Double(1.4784));
			t1.add(new Day(15, MonthConstants.JANUARY, 2001), new Double(1.4787));
			t1.add(new Day(16, MonthConstants.JANUARY, 2001), new Double(1.4702));
			t1.add(new Day(17, MonthConstants.JANUARY, 2001), new Double(1.4729));
			t1.add(new Day(18, MonthConstants.JANUARY, 2001), new Double(1.4760));
			t1.add(new Day(19, MonthConstants.JANUARY, 2001), new Double(1.4685));
			t1.add(new Day(22, MonthConstants.JANUARY, 2001), new Double(1.4609));
			t1.add(new Day(23, MonthConstants.JANUARY, 2001), new Double(1.4709));
			t1.add(new Day(24, MonthConstants.JANUARY, 2001), new Double(1.4576));
			t1.add(new Day(25, MonthConstants.JANUARY, 2001), new Double(1.4589));
			t1.add(new Day(26, MonthConstants.JANUARY, 2001), new Double(1.4568));
			t1.add(new Day(29, MonthConstants.JANUARY, 2001), new Double(1.4566));
			t1.add(new Day(30, MonthConstants.JANUARY, 2001), new Double(1.4604));
			t1.add(new Day(31, MonthConstants.JANUARY, 2001), new Double(1.4616));
			t1.add(new Day(1, MonthConstants.FEBRUARY, 2001), new Double(1.4777));
			t1.add(new Day(2, MonthConstants.FEBRUARY, 2001), new Double(1.4687));
			t1.add(new Day(5, MonthConstants.FEBRUARY, 2001), new Double(1.4753));
			t1.add(new Day(6, MonthConstants.FEBRUARY, 2001), new Double(1.4605));
			t1.add(new Day(7, MonthConstants.FEBRUARY, 2001), new Double(1.4619));
			t1.add(new Day(8, MonthConstants.FEBRUARY, 2001), new Double(1.4453));
			t1.add(new Day(9, MonthConstants.FEBRUARY, 2001), new Double(1.4463));
			t1.add(new Day(12, MonthConstants.FEBRUARY, 2001), new Double(1.4521));
			t1.add(new Day(13, MonthConstants.FEBRUARY, 2001), new Double(1.4517));
			t1.add(new Day(14, MonthConstants.FEBRUARY, 2001), new Double(1.4601));
			t1.add(new Day(15, MonthConstants.FEBRUARY, 2001), new Double(1.4500));
			t1.add(new Day(16, MonthConstants.FEBRUARY, 2001), new Double(1.4517));
			t1.add(new Day(19, MonthConstants.FEBRUARY, 2001), new Double(1.4459));
			t1.add(new Day(20, MonthConstants.FEBRUARY, 2001), new Double(1.4449));
			t1.add(new Day(21, MonthConstants.FEBRUARY, 2001), new Double(1.4447));
			t1.add(new Day(22, MonthConstants.FEBRUARY, 2001), new Double(1.4465));
			t1.add(new Day(23, MonthConstants.FEBRUARY, 2001), new Double(1.4487));
			t1.add(new Day(26, MonthConstants.FEBRUARY, 2001), new Double(1.4417));
			t1.add(new Day(27, MonthConstants.FEBRUARY, 2001), new Double(1.4420));
			t1.add(new Day(28, MonthConstants.FEBRUARY, 2001), new Double(1.4421));
			t1.add(new Day(1, MonthConstants.MARCH, 2001), new Double(1.4547));
			t1.add(new Day(2, MonthConstants.MARCH, 2001), new Double(1.4741));
			t1.add(new Day(5, MonthConstants.MARCH, 2001), new Double(1.4686));
			t1.add(new Day(6, MonthConstants.MARCH, 2001), new Double(1.4667));
			t1.add(new Day(7, MonthConstants.MARCH, 2001), new Double(1.4618));
			t1.add(new Day(8, MonthConstants.MARCH, 2001), new Double(1.4685));
			t1.add(new Day(9, MonthConstants.MARCH, 2001), new Double(1.4677));
			t1.add(new Day(12, MonthConstants.MARCH, 2001), new Double(1.4660));
			t1.add(new Day(13, MonthConstants.MARCH, 2001), new Double(1.4526));
			t1.add(new Day(14, MonthConstants.MARCH, 2001), new Double(1.4483));
			t1.add(new Day(15, MonthConstants.MARCH, 2001), new Double(1.4441));
			t1.add(new Day(16, MonthConstants.MARCH, 2001), new Double(1.4303));
			t1.add(new Day(19, MonthConstants.MARCH, 2001), new Double(1.4259));
			t1.add(new Day(20, MonthConstants.MARCH, 2001), new Double(1.4283));
			t1.add(new Day(21, MonthConstants.MARCH, 2001), new Double(1.4293));
			t1.add(new Day(22, MonthConstants.MARCH, 2001), new Double(1.4192));
			t1.add(new Day(23, MonthConstants.MARCH, 2001), new Double(1.4293));
			t1.add(new Day(26, MonthConstants.MARCH, 2001), new Double(1.4334));
			t1.add(new Day(27, MonthConstants.MARCH, 2001), new Double(1.4371));
			t1.add(new Day(28, MonthConstants.MARCH, 2001), new Double(1.4347));
			t1.add(new Day(29, MonthConstants.MARCH, 2001), new Double(1.4362));
			t1.add(new Day(30, MonthConstants.MARCH, 2001), new Double(1.4217));
			t1.add(new Day(2, MonthConstants.APRIL, 2001), new Double(1.4205));
			t1.add(new Day(3, MonthConstants.APRIL, 2001), new Double(1.4270));
			t1.add(new Day(4, MonthConstants.APRIL, 2001), new Double(1.4333));
			t1.add(new Day(5, MonthConstants.APRIL, 2001), new Double(1.4287));
			t1.add(new Day(6, MonthConstants.APRIL, 2001), new Double(1.4395));
			t1.add(new Day(9, MonthConstants.APRIL, 2001), new Double(1.4494));
			t1.add(new Day(10, MonthConstants.APRIL, 2001), new Double(1.4385));
			t1.add(new Day(11, MonthConstants.APRIL, 2001), new Double(1.4348));
			t1.add(new Day(12, MonthConstants.APRIL, 2001), new Double(1.4402));
			t1.add(new Day(17, MonthConstants.APRIL, 2001), new Double(1.4314));
			t1.add(new Day(18, MonthConstants.APRIL, 2001), new Double(1.4197));
			t1.add(new Day(19, MonthConstants.APRIL, 2001), new Double(1.4365));
			t1.add(new Day(20, MonthConstants.APRIL, 2001), new Double(1.4416));
			t1.add(new Day(23, MonthConstants.APRIL, 2001), new Double(1.4396));
			t1.add(new Day(24, MonthConstants.APRIL, 2001), new Double(1.4360));
			t1.add(new Day(25, MonthConstants.APRIL, 2001), new Double(1.4397));
			t1.add(new Day(26, MonthConstants.APRIL, 2001), new Double(1.4402));
			t1.add(new Day(27, MonthConstants.APRIL, 2001), new Double(1.4366));
			t1.add(new Day(30, MonthConstants.APRIL, 2001), new Double(1.4309));
			t1.add(new Day(1, MonthConstants.MAY, 2001), new Double(1.4324));
			t1.add(new Day(2, MonthConstants.MAY, 2001), new Double(1.4336));
			t1.add(new Day(3, MonthConstants.MAY, 2001), new Double(1.4329));
			t1.add(new Day(4, MonthConstants.MAY, 2001), new Double(1.4375));
			t1.add(new Day(8, MonthConstants.MAY, 2001), new Double(1.4321));
			t1.add(new Day(9, MonthConstants.MAY, 2001), new Double(1.4219));
			t1.add(new Day(10, MonthConstants.MAY, 2001), new Double(1.4226));
			t1.add(new Day(11, MonthConstants.MAY, 2001), new Double(1.4199));
			t1.add(new Day(14, MonthConstants.MAY, 2001), new Double(1.4183));
			t1.add(new Day(15, MonthConstants.MAY, 2001), new Double(1.4218));
			t1.add(new Day(16, MonthConstants.MAY, 2001), new Double(1.4295));
			t1.add(new Day(17, MonthConstants.MAY, 2001), new Double(1.4296));
			t1.add(new Day(18, MonthConstants.MAY, 2001), new Double(1.4296));
			t1.add(new Day(21, MonthConstants.MAY, 2001), new Double(1.4366));
			t1.add(new Day(22, MonthConstants.MAY, 2001), new Double(1.4283));
			t1.add(new Day(23, MonthConstants.MAY, 2001), new Double(1.4244));
			t1.add(new Day(24, MonthConstants.MAY, 2001), new Double(1.4102));
			t1.add(new Day(25, MonthConstants.MAY, 2001), new Double(1.4205));
			t1.add(new Day(29, MonthConstants.MAY, 2001), new Double(1.4183));
			t1.add(new Day(30, MonthConstants.MAY, 2001), new Double(1.4230));
			t1.add(new Day(31, MonthConstants.MAY, 2001), new Double(1.4201));
			t1.add(new Day(1, MonthConstants.JUNE, 2001), new Double(1.4148));
			t1.add(new Day(4, MonthConstants.JUNE, 2001), new Double(1.4142));
			t1.add(new Day(5, MonthConstants.JUNE, 2001), new Double(1.4095));
			t1.add(new Day(6, MonthConstants.JUNE, 2001), new Double(1.3938));
			t1.add(new Day(7, MonthConstants.JUNE, 2001), new Double(1.3886));
			t1.add(new Day(8, MonthConstants.JUNE, 2001), new Double(1.3798));
			t1.add(new Day(11, MonthConstants.JUNE, 2001), new Double(1.3726));
			t1.add(new Day(12, MonthConstants.JUNE, 2001), new Double(1.3788));
			t1.add(new Day(13, MonthConstants.JUNE, 2001), new Double(1.3878));
			t1.add(new Day(14, MonthConstants.JUNE, 2001), new Double(1.4002));
			t1.add(new Day(15, MonthConstants.JUNE, 2001), new Double(1.4033));
			t1.add(new Day(18, MonthConstants.JUNE, 2001), new Double(1.4038));
			t1.add(new Day(19, MonthConstants.JUNE, 2001), new Double(1.4023));
			t1.add(new Day(20, MonthConstants.JUNE, 2001), new Double(1.3952));
			t1.add(new Day(21, MonthConstants.JUNE, 2001), new Double(1.4142));
			t1.add(new Day(22, MonthConstants.JUNE, 2001), new Double(1.4114));
			t1.add(new Day(25, MonthConstants.JUNE, 2001), new Double(1.4141));
			t1.add(new Day(26, MonthConstants.JUNE, 2001), new Double(1.4157));
			t1.add(new Day(27, MonthConstants.JUNE, 2001), new Double(1.4136));
			t1.add(new Day(28, MonthConstants.JUNE, 2001), new Double(1.4089));
			t1.add(new Day(29, MonthConstants.JUNE, 2001), new Double(1.4066));
			t1.add(new Day(2, MonthConstants.JULY, 2001), new Double(1.4154));
			t1.add(new Day(3, MonthConstants.JULY, 2001), new Double(1.4072));
			t1.add(new Day(4, MonthConstants.JULY, 2001), new Double(1.4064));
			t1.add(new Day(5, MonthConstants.JULY, 2001), new Double(1.3995));
			t1.add(new Day(6, MonthConstants.JULY, 2001), new Double(1.4070));
			t1.add(new Day(9, MonthConstants.JULY, 2001), new Double(1.4094));
			t1.add(new Day(10, MonthConstants.JULY, 2001), new Double(1.4113));
			t1.add(new Day(11, MonthConstants.JULY, 2001), new Double(1.4143));
			t1.add(new Day(12, MonthConstants.JULY, 2001), new Double(1.4061));
			t1.add(new Day(13, MonthConstants.JULY, 2001), new Double(1.4008));
			t1.add(new Day(16, MonthConstants.JULY, 2001), new Double(1.3999));
			t1.add(new Day(17, MonthConstants.JULY, 2001), new Double(1.4003));
			t1.add(new Day(18, MonthConstants.JULY, 2001), new Double(1.4155));
			t1.add(new Day(19, MonthConstants.JULY, 2001), new Double(1.4165));
			t1.add(new Day(20, MonthConstants.JULY, 2001), new Double(1.4282));
			t1.add(new Day(23, MonthConstants.JULY, 2001), new Double(1.4190));
			t1.add(new Day(24, MonthConstants.JULY, 2001), new Double(1.4200));
			t1.add(new Day(25, MonthConstants.JULY, 2001), new Double(1.4276));
			t1.add(new Day(26, MonthConstants.JULY, 2001), new Double(1.4275));
			t1.add(new Day(27, MonthConstants.JULY, 2001), new Double(1.4233));
			t1.add(new Day(30, MonthConstants.JULY, 2001), new Double(1.4246));
			t1.add(new Day(31, MonthConstants.JULY, 2001), new Double(1.4254));
			t1.add(new Day(1, MonthConstants.AUGUST, 2001), new Double(1.4319));
			t1.add(new Day(2, MonthConstants.AUGUST, 2001), new Double(1.4321));
			t1.add(new Day(3, MonthConstants.AUGUST, 2001), new Double(1.4293));
			t1.add(new Day(6, MonthConstants.AUGUST, 2001), new Double(1.4190));
			t1.add(new Day(7, MonthConstants.AUGUST, 2001), new Double(1.4176));
			t1.add(new Day(8, MonthConstants.AUGUST, 2001), new Double(1.4139));
			t1.add(new Day(9, MonthConstants.AUGUST, 2001), new Double(1.4214));
			t1.add(new Day(10, MonthConstants.AUGUST, 2001), new Double(1.4266));
			t1.add(new Day(11, MonthConstants.AUGUST, 2001), new Double(1.4220));
			t1.add(new Day(12, MonthConstants.AUGUST, 2001), new Double(1.4210));
			t1.add(new Day(15, MonthConstants.AUGUST, 2001), new Double(1.4383));
			t1.add(new Day(16, MonthConstants.AUGUST, 2001), new Double(1.4431));
			t1.add(new Day(17, MonthConstants.AUGUST, 2001), new Double(1.4445));
			t1.add(new Day(20, MonthConstants.AUGUST, 2001), new Double(1.4444));
			t1.add(new Day(21, MonthConstants.AUGUST, 2001), new Double(1.4483));
			t1.add(new Day(22, MonthConstants.AUGUST, 2001), new Double(1.4556));
			t1.add(new Day(23, MonthConstants.AUGUST, 2001), new Double(1.4468));
			t1.add(new Day(24, MonthConstants.AUGUST, 2001), new Double(1.4464));
			t1.add(new Day(28, MonthConstants.AUGUST, 2001), new Double(1.4483));
			t1.add(new Day(29, MonthConstants.AUGUST, 2001), new Double(1.4519));
			t1.add(new Day(30, MonthConstants.AUGUST, 2001), new Double(1.4494));
			t1.add(new Day(31, MonthConstants.AUGUST, 2001), new Double(1.4505));
			t1.add(new Day(3, MonthConstants.SEPTEMBER, 2001), new Double(1.4519));
			t1.add(new Day(4, MonthConstants.SEPTEMBER, 2001), new Double(1.4460));
			t1.add(new Day(5, MonthConstants.SEPTEMBER, 2001), new Double(1.4526));
			t1.add(new Day(6, MonthConstants.SEPTEMBER, 2001), new Double(1.4527));
			t1.add(new Day(7, MonthConstants.SEPTEMBER, 2001), new Double(1.4617));
			t1.add(new Day(10, MonthConstants.SEPTEMBER, 2001), new Double(1.4583));
			t1.add(new Day(11, MonthConstants.SEPTEMBER, 2001), new Double(1.4693));
			t1.add(new Day(12, MonthConstants.SEPTEMBER, 2001), new Double(1.4633));
			t1.add(new Day(13, MonthConstants.SEPTEMBER, 2001), new Double(1.4690));
			t1.add(new Day(14, MonthConstants.SEPTEMBER, 2001), new Double(1.4691));
			t1.add(new Day(17, MonthConstants.SEPTEMBER, 2001), new Double(1.4668));
			t1.add(new Day(18, MonthConstants.SEPTEMBER, 2001), new Double(1.4624));
			t1.add(new Day(19, MonthConstants.SEPTEMBER, 2001), new Double(1.4678));
			t1.add(new Day(20, MonthConstants.SEPTEMBER, 2001), new Double(1.4657));
			t1.add(new Day(21, MonthConstants.SEPTEMBER, 2001), new Double(1.4575));
			t1.add(new Day(24, MonthConstants.SEPTEMBER, 2001), new Double(1.4646));
			t1.add(new Day(25, MonthConstants.SEPTEMBER, 2001), new Double(1.4699));
			t1.add(new Day(26, MonthConstants.SEPTEMBER, 2001), new Double(1.4749));
			t1.add(new Day(27, MonthConstants.SEPTEMBER, 2001), new Double(1.4756));
			t1.add(new Day(28, MonthConstants.SEPTEMBER, 2001), new Double(1.4699));
			t1.add(new Day(1, MonthConstants.OCTOBER, 2001), new Double(1.4784));
			t1.add(new Day(2, MonthConstants.OCTOBER, 2001), new Double(1.4661));
			t1.add(new Day(3, MonthConstants.OCTOBER, 2001), new Double(1.4767));
			t1.add(new Day(4, MonthConstants.OCTOBER, 2001), new Double(1.4770));
			t1.add(new Day(5, MonthConstants.OCTOBER, 2001), new Double(1.4810));
			t1.add(new Day(8, MonthConstants.OCTOBER, 2001), new Double(1.4743));
			t1.add(new Day(9, MonthConstants.OCTOBER, 2001), new Double(1.4667));
			t1.add(new Day(10, MonthConstants.OCTOBER, 2001), new Double(1.4505));
			t1.add(new Day(11, MonthConstants.OCTOBER, 2001), new Double(1.4434));
			t1.add(new Day(12, MonthConstants.OCTOBER, 2001), new Double(1.4504));
			t1.add(new Day(15, MonthConstants.OCTOBER, 2001), new Double(1.4471));
			t1.add(new Day(16, MonthConstants.OCTOBER, 2001), new Double(1.4474));
			t1.add(new Day(17, MonthConstants.OCTOBER, 2001), new Double(1.4512));
			t1.add(new Day(18, MonthConstants.OCTOBER, 2001), new Double(1.4445));
			t1.add(new Day(19, MonthConstants.OCTOBER, 2001), new Double(1.4384));
			t1.add(new Day(22, MonthConstants.OCTOBER, 2001), new Double(1.4275));
			t1.add(new Day(23, MonthConstants.OCTOBER, 2001), new Double(1.4212));
			t1.add(new Day(24, MonthConstants.OCTOBER, 2001), new Double(1.4233));
			t1.add(new Day(25, MonthConstants.OCTOBER, 2001), new Double(1.4297));
			t1.add(new Day(26, MonthConstants.OCTOBER, 2001), new Double(1.4328));
			t1.add(new Day(29, MonthConstants.OCTOBER, 2001), new Double(1.4515));
			t1.add(new Day(30, MonthConstants.OCTOBER, 2001), new Double(1.4564));
			t1.add(new Day(31, MonthConstants.OCTOBER, 2001), new Double(1.4541));
			t1.add(new Day(1, MonthConstants.NOVEMBER, 2001), new Double(1.4624));
			t1.add(new Day(2, MonthConstants.NOVEMBER, 2001), new Double(1.4632));
			t1.add(new Day(5, MonthConstants.NOVEMBER, 2001), new Double(1.4570));
			t1.add(new Day(6, MonthConstants.NOVEMBER, 2001), new Double(1.4588));
			t1.add(new Day(7, MonthConstants.NOVEMBER, 2001), new Double(1.4646));
			t1.add(new Day(8, MonthConstants.NOVEMBER, 2001), new Double(1.4552));
			t1.add(new Day(9, MonthConstants.NOVEMBER, 2001), new Double(1.4579));
			t1.add(new Day(12, MonthConstants.NOVEMBER, 2001), new Double(1.4575));
			t1.add(new Day(13, MonthConstants.NOVEMBER, 2001), new Double(1.4429));
			t1.add(new Day(14, MonthConstants.NOVEMBER, 2001), new Double(1.4425));
			t1.add(new Day(15, MonthConstants.NOVEMBER, 2001), new Double(1.4318));
			t1.add(new Day(16, MonthConstants.NOVEMBER, 2001), new Double(1.4291));
			t1.add(new Day(19, MonthConstants.NOVEMBER, 2001), new Double(1.4140));
			t1.add(new Day(20, MonthConstants.NOVEMBER, 2001), new Double(1.4173));
			t1.add(new Day(21, MonthConstants.NOVEMBER, 2001), new Double(1.4132));
			t1.add(new Day(22, MonthConstants.NOVEMBER, 2001), new Double(1.4131));
			t1.add(new Day(23, MonthConstants.NOVEMBER, 2001), new Double(1.4083));
			t1.add(new Day(26, MonthConstants.NOVEMBER, 2001), new Double(1.4122));
			t1.add(new Day(27, MonthConstants.NOVEMBER, 2001), new Double(1.4136));
			t1.add(new Day(28, MonthConstants.NOVEMBER, 2001), new Double(1.4239));
			t1.add(new Day(29, MonthConstants.NOVEMBER, 2001), new Double(1.4225));
			t1.add(new Day(30, MonthConstants.NOVEMBER, 2001), new Double(1.4260));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return t1;
	}

	/**
	 * Returns a time series of the daily EUR/GBP exchange rates in 2001 (to date), for use in
	 * the JFreeChart demonstration application.
	 * <P>
	 * You wouldn't normally create a time series in this way. Typically, values would be read from a database.
	 * 
	 * @return a time series.
	 */
	public static TimeSeries createEURTimeSeries() {

		final TimeSeries t1 = new TimeSeries("EUR/GBP");
		try {
			t1.add(new Day(2, MonthConstants.JANUARY, 2001), new Double(1.5788));
			t1.add(new Day(3, MonthConstants.JANUARY, 2001), new Double(1.5913));
			t1.add(new Day(4, MonthConstants.JANUARY, 2001), new Double(1.5807));
			t1.add(new Day(5, MonthConstants.JANUARY, 2001), new Double(1.5711));
			t1.add(new Day(8, MonthConstants.JANUARY, 2001), new Double(1.5778));
			t1.add(new Day(9, MonthConstants.JANUARY, 2001), new Double(1.5851));
			t1.add(new Day(10, MonthConstants.JANUARY, 2001), new Double(1.5846));
			t1.add(new Day(11, MonthConstants.JANUARY, 2001), new Double(1.5727));
			t1.add(new Day(12, MonthConstants.JANUARY, 2001), new Double(1.5585));
			t1.add(new Day(15, MonthConstants.JANUARY, 2001), new Double(1.5694));
			t1.add(new Day(16, MonthConstants.JANUARY, 2001), new Double(1.5629));
			t1.add(new Day(17, MonthConstants.JANUARY, 2001), new Double(1.5831));
			t1.add(new Day(18, MonthConstants.JANUARY, 2001), new Double(1.5624));
			t1.add(new Day(19, MonthConstants.JANUARY, 2001), new Double(1.5694));
			t1.add(new Day(22, MonthConstants.JANUARY, 2001), new Double(1.5615));
			t1.add(new Day(23, MonthConstants.JANUARY, 2001), new Double(1.5656));
			t1.add(new Day(24, MonthConstants.JANUARY, 2001), new Double(1.5795));
			t1.add(new Day(25, MonthConstants.JANUARY, 2001), new Double(1.5852));
			t1.add(new Day(26, MonthConstants.JANUARY, 2001), new Double(1.5797));
			t1.add(new Day(29, MonthConstants.JANUARY, 2001), new Double(1.5862));
			t1.add(new Day(30, MonthConstants.JANUARY, 2001), new Double(1.5803));
			t1.add(new Day(31, MonthConstants.JANUARY, 2001), new Double(1.5714));
			t1.add(new Day(1, MonthConstants.FEBRUARY, 2001), new Double(1.5717));
			t1.add(new Day(2, MonthConstants.FEBRUARY, 2001), new Double(1.5735));
			t1.add(new Day(5, MonthConstants.FEBRUARY, 2001), new Double(1.5691));
			t1.add(new Day(6, MonthConstants.FEBRUARY, 2001), new Double(1.5676));
			t1.add(new Day(7, MonthConstants.FEBRUARY, 2001), new Double(1.5677));
			t1.add(new Day(8, MonthConstants.FEBRUARY, 2001), new Double(1.5737));
			t1.add(new Day(9, MonthConstants.FEBRUARY, 2001), new Double(1.5654));
			t1.add(new Day(12, MonthConstants.FEBRUARY, 2001), new Double(1.5621));
			t1.add(new Day(13, MonthConstants.FEBRUARY, 2001), new Double(1.5761));
			t1.add(new Day(14, MonthConstants.FEBRUARY, 2001), new Double(1.5898));
			t1.add(new Day(15, MonthConstants.FEBRUARY, 2001), new Double(1.6045));
			t1.add(new Day(16, MonthConstants.FEBRUARY, 2001), new Double(1.5852));
			t1.add(new Day(19, MonthConstants.FEBRUARY, 2001), new Double(1.5704));
			t1.add(new Day(20, MonthConstants.FEBRUARY, 2001), new Double(1.5892));
			t1.add(new Day(21, MonthConstants.FEBRUARY, 2001), new Double(1.5844));
			t1.add(new Day(22, MonthConstants.FEBRUARY, 2001), new Double(1.5934));
			t1.add(new Day(23, MonthConstants.FEBRUARY, 2001), new Double(1.5951));
			t1.add(new Day(26, MonthConstants.FEBRUARY, 2001), new Double(1.5848));
			t1.add(new Day(27, MonthConstants.FEBRUARY, 2001), new Double(1.5706));
			t1.add(new Day(28, MonthConstants.FEBRUARY, 2001), new Double(1.5680));
			t1.add(new Day(1, MonthConstants.MARCH, 2001), new Double(1.5645));
			t1.add(new Day(2, MonthConstants.MARCH, 2001), new Double(1.5754));
			t1.add(new Day(5, MonthConstants.MARCH, 2001), new Double(1.5808));
			t1.add(new Day(6, MonthConstants.MARCH, 2001), new Double(1.5766));
			t1.add(new Day(7, MonthConstants.MARCH, 2001), new Double(1.5756));
			t1.add(new Day(8, MonthConstants.MARCH, 2001), new Double(1.5760));
			t1.add(new Day(9, MonthConstants.MARCH, 2001), new Double(1.5748));
			t1.add(new Day(12, MonthConstants.MARCH, 2001), new Double(1.5779));
			t1.add(new Day(13, MonthConstants.MARCH, 2001), new Double(1.5837));
			t1.add(new Day(14, MonthConstants.MARCH, 2001), new Double(1.5886));
			t1.add(new Day(15, MonthConstants.MARCH, 2001), new Double(1.5931));
			t1.add(new Day(16, MonthConstants.MARCH, 2001), new Double(1.5945));
			t1.add(new Day(19, MonthConstants.MARCH, 2001), new Double(1.5880));
			t1.add(new Day(20, MonthConstants.MARCH, 2001), new Double(1.5817));
			t1.add(new Day(21, MonthConstants.MARCH, 2001), new Double(1.5927));
			t1.add(new Day(22, MonthConstants.MARCH, 2001), new Double(1.6065));
			t1.add(new Day(23, MonthConstants.MARCH, 2001), new Double(1.6006));
			t1.add(new Day(26, MonthConstants.MARCH, 2001), new Double(1.6007));
			t1.add(new Day(27, MonthConstants.MARCH, 2001), new Double(1.5989));
			t1.add(new Day(28, MonthConstants.MARCH, 2001), new Double(1.6135));
			t1.add(new Day(29, MonthConstants.MARCH, 2001), new Double(1.6282));
			t1.add(new Day(30, MonthConstants.MARCH, 2001), new Double(1.6090));
			t1.add(new Day(2, MonthConstants.APRIL, 2001), new Double(1.6107));
			t1.add(new Day(3, MonthConstants.APRIL, 2001), new Double(1.6093));
			t1.add(new Day(4, MonthConstants.APRIL, 2001), new Double(1.5880));
			t1.add(new Day(5, MonthConstants.APRIL, 2001), new Double(1.5931));
			t1.add(new Day(6, MonthConstants.APRIL, 2001), new Double(1.5968));
			t1.add(new Day(9, MonthConstants.APRIL, 2001), new Double(1.6072));
			t1.add(new Day(10, MonthConstants.APRIL, 2001), new Double(1.6167));
			t1.add(new Day(11, MonthConstants.APRIL, 2001), new Double(1.6214));
			t1.add(new Day(12, MonthConstants.APRIL, 2001), new Double(1.6120));
			t1.add(new Day(17, MonthConstants.APRIL, 2001), new Double(1.6229));
			t1.add(new Day(18, MonthConstants.APRIL, 2001), new Double(1.6298));
			t1.add(new Day(19, MonthConstants.APRIL, 2001), new Double(1.6159));
			t1.add(new Day(20, MonthConstants.APRIL, 2001), new Double(1.5996));
			t1.add(new Day(23, MonthConstants.APRIL, 2001), new Double(1.6042));
			t1.add(new Day(24, MonthConstants.APRIL, 2001), new Double(1.6061));
			t1.add(new Day(25, MonthConstants.APRIL, 2001), new Double(1.6045));
			t1.add(new Day(26, MonthConstants.APRIL, 2001), new Double(1.5970));
			t1.add(new Day(27, MonthConstants.APRIL, 2001), new Double(1.6095));
			t1.add(new Day(30, MonthConstants.APRIL, 2001), new Double(1.6141));
			t1.add(new Day(1, MonthConstants.MAY, 2001), new Double(1.6076));
			t1.add(new Day(2, MonthConstants.MAY, 2001), new Double(1.6077));
			t1.add(new Day(3, MonthConstants.MAY, 2001), new Double(1.6035));
			t1.add(new Day(4, MonthConstants.MAY, 2001), new Double(1.6060));
			t1.add(new Day(8, MonthConstants.MAY, 2001), new Double(1.6178));
			t1.add(new Day(9, MonthConstants.MAY, 2001), new Double(1.6083));
			t1.add(new Day(10, MonthConstants.MAY, 2001), new Double(1.6107));
			t1.add(new Day(11, MonthConstants.MAY, 2001), new Double(1.6209));
			t1.add(new Day(14, MonthConstants.MAY, 2001), new Double(1.6228));
			t1.add(new Day(15, MonthConstants.MAY, 2001), new Double(1.6184));
			t1.add(new Day(16, MonthConstants.MAY, 2001), new Double(1.6167));
			t1.add(new Day(17, MonthConstants.MAY, 2001), new Double(1.6223));
			t1.add(new Day(18, MonthConstants.MAY, 2001), new Double(1.6305));
			t1.add(new Day(21, MonthConstants.MAY, 2001), new Double(1.6420));
			t1.add(new Day(22, MonthConstants.MAY, 2001), new Double(1.6484));
			t1.add(new Day(23, MonthConstants.MAY, 2001), new Double(1.6547));
			t1.add(new Day(24, MonthConstants.MAY, 2001), new Double(1.6444));
			t1.add(new Day(25, MonthConstants.MAY, 2001), new Double(1.6577));
			t1.add(new Day(29, MonthConstants.MAY, 2001), new Double(1.6606));
			t1.add(new Day(30, MonthConstants.MAY, 2001), new Double(1.6604));
			t1.add(new Day(31, MonthConstants.MAY, 2001), new Double(1.6772));
			t1.add(new Day(1, MonthConstants.JUNE, 2001), new Double(1.6717));
			t1.add(new Day(4, MonthConstants.JUNE, 2001), new Double(1.6685));
			t1.add(new Day(5, MonthConstants.JUNE, 2001), new Double(1.6621));
			t1.add(new Day(6, MonthConstants.JUNE, 2001), new Double(1.6460));
			t1.add(new Day(7, MonthConstants.JUNE, 2001), new Double(1.6333));
			t1.add(new Day(8, MonthConstants.JUNE, 2001), new Double(1.6265));
			t1.add(new Day(11, MonthConstants.JUNE, 2001), new Double(1.6311));
			t1.add(new Day(12, MonthConstants.JUNE, 2001), new Double(1.6238));
			t1.add(new Day(13, MonthConstants.JUNE, 2001), new Double(1.6300));
			t1.add(new Day(14, MonthConstants.JUNE, 2001), new Double(1.6289));
			t1.add(new Day(15, MonthConstants.JUNE, 2001), new Double(1.6276));
			t1.add(new Day(18, MonthConstants.JUNE, 2001), new Double(1.6299));
			t1.add(new Day(19, MonthConstants.JUNE, 2001), new Double(1.6353));
			t1.add(new Day(20, MonthConstants.JUNE, 2001), new Double(1.6378));
			t1.add(new Day(21, MonthConstants.JUNE, 2001), new Double(1.6567));
			t1.add(new Day(22, MonthConstants.JUNE, 2001), new Double(1.6523));
			t1.add(new Day(25, MonthConstants.JUNE, 2001), new Double(1.6418));
			t1.add(new Day(26, MonthConstants.JUNE, 2001), new Double(1.6429));
			t1.add(new Day(27, MonthConstants.JUNE, 2001), new Double(1.6439));
			t1.add(new Day(28, MonthConstants.JUNE, 2001), new Double(1.6605));
			t1.add(new Day(29, MonthConstants.JUNE, 2001), new Double(1.6599));
			t1.add(new Day(2, MonthConstants.JULY, 2001), new Double(1.6727));
			t1.add(new Day(3, MonthConstants.JULY, 2001), new Double(1.6620));
			t1.add(new Day(4, MonthConstants.JULY, 2001), new Double(1.6628));
			t1.add(new Day(5, MonthConstants.JULY, 2001), new Double(1.6730));
			t1.add(new Day(6, MonthConstants.JULY, 2001), new Double(1.6649));
			t1.add(new Day(9, MonthConstants.JULY, 2001), new Double(1.6603));
			t1.add(new Day(10, MonthConstants.JULY, 2001), new Double(1.6489));
			t1.add(new Day(11, MonthConstants.JULY, 2001), new Double(1.6421));
			t1.add(new Day(12, MonthConstants.JULY, 2001), new Double(1.6498));
			t1.add(new Day(13, MonthConstants.JULY, 2001), new Double(1.6447));
			t1.add(new Day(16, MonthConstants.JULY, 2001), new Double(1.6373));
			t1.add(new Day(17, MonthConstants.JULY, 2001), new Double(1.6443));
			t1.add(new Day(18, MonthConstants.JULY, 2001), new Double(1.6246));
			t1.add(new Day(19, MonthConstants.JULY, 2001), new Double(1.6295));
			t1.add(new Day(20, MonthConstants.JULY, 2001), new Double(1.6362));
			t1.add(new Day(23, MonthConstants.JULY, 2001), new Double(1.6348));
			t1.add(new Day(24, MonthConstants.JULY, 2001), new Double(1.6242));
			t1.add(new Day(25, MonthConstants.JULY, 2001), new Double(1.6241));
			t1.add(new Day(26, MonthConstants.JULY, 2001), new Double(1.6281));
			t1.add(new Day(27, MonthConstants.JULY, 2001), new Double(1.6296));
			t1.add(new Day(30, MonthConstants.JULY, 2001), new Double(1.6279));
			t1.add(new Day(31, MonthConstants.JULY, 2001), new Double(1.6300));
			t1.add(new Day(1, MonthConstants.AUGUST, 2001), new Double(1.6290));
			t1.add(new Day(2, MonthConstants.AUGUST, 2001), new Double(1.6237));
			t1.add(new Day(3, MonthConstants.AUGUST, 2001), new Double(1.6138));
			t1.add(new Day(6, MonthConstants.AUGUST, 2001), new Double(1.6121));
			t1.add(new Day(7, MonthConstants.AUGUST, 2001), new Double(1.6170));
			t1.add(new Day(8, MonthConstants.AUGUST, 2001), new Double(1.6135));
			t1.add(new Day(9, MonthConstants.AUGUST, 2001), new Double(1.5996));
			t1.add(new Day(10, MonthConstants.AUGUST, 2001), new Double(1.5931));
			t1.add(new Day(13, MonthConstants.AUGUST, 2001), new Double(1.5828));
			t1.add(new Day(14, MonthConstants.AUGUST, 2001), new Double(1.5824));
			t1.add(new Day(15, MonthConstants.AUGUST, 2001), new Double(1.5783));
			t1.add(new Day(16, MonthConstants.AUGUST, 2001), new Double(1.5810));
			t1.add(new Day(17, MonthConstants.AUGUST, 2001), new Double(1.5761));
			t1.add(new Day(20, MonthConstants.AUGUST, 2001), new Double(1.5831));
			t1.add(new Day(21, MonthConstants.AUGUST, 2001), new Double(1.5870));
			t1.add(new Day(22, MonthConstants.AUGUST, 2001), new Double(1.5808));
			t1.add(new Day(23, MonthConstants.AUGUST, 2001), new Double(1.5845));
			t1.add(new Day(24, MonthConstants.AUGUST, 2001), new Double(1.5844));
			t1.add(new Day(28, MonthConstants.AUGUST, 2001), new Double(1.5924));
			t1.add(new Day(29, MonthConstants.AUGUST, 2001), new Double(1.5950));
			t1.add(new Day(30, MonthConstants.AUGUST, 2001), new Double(1.5941));
			t1.add(new Day(31, MonthConstants.AUGUST, 2001), new Double(1.5968));
			t1.add(new Day(3, MonthConstants.SEPTEMBER, 2001), new Double(1.6020));
			t1.add(new Day(4, MonthConstants.SEPTEMBER, 2001), new Double(1.6236));
			t1.add(new Day(5, MonthConstants.SEPTEMBER, 2001), new Double(1.6352));
			t1.add(new Day(6, MonthConstants.SEPTEMBER, 2001), new Double(1.6302));
			t1.add(new Day(7, MonthConstants.SEPTEMBER, 2001), new Double(1.6180));
			t1.add(new Day(10, MonthConstants.SEPTEMBER, 2001), new Double(1.6218));
			t1.add(new Day(11, MonthConstants.SEPTEMBER, 2001), new Double(1.6182));
			t1.add(new Day(12, MonthConstants.SEPTEMBER, 2001), new Double(1.6157));
			t1.add(new Day(13, MonthConstants.SEPTEMBER, 2001), new Double(1.6171));
			t1.add(new Day(14, MonthConstants.SEPTEMBER, 2001), new Double(1.5960));
			t1.add(new Day(17, MonthConstants.SEPTEMBER, 2001), new Double(1.5952));
			t1.add(new Day(18, MonthConstants.SEPTEMBER, 2001), new Double(1.5863));
			t1.add(new Day(19, MonthConstants.SEPTEMBER, 2001), new Double(1.5790));
			t1.add(new Day(20, MonthConstants.SEPTEMBER, 2001), new Double(1.5811));
			t1.add(new Day(21, MonthConstants.SEPTEMBER, 2001), new Double(1.5917));
			t1.add(new Day(24, MonthConstants.SEPTEMBER, 2001), new Double(1.6005));
			t1.add(new Day(25, MonthConstants.SEPTEMBER, 2001), new Double(1.5915));
			t1.add(new Day(26, MonthConstants.SEPTEMBER, 2001), new Double(1.6012));
			t1.add(new Day(27, MonthConstants.SEPTEMBER, 2001), new Double(1.6032));
			t1.add(new Day(28, MonthConstants.SEPTEMBER, 2001), new Double(1.6133));
			t1.add(new Day(1, MonthConstants.OCTOBER, 2001), new Double(1.6147));
			t1.add(new Day(2, MonthConstants.OCTOBER, 2001), new Double(1.6002));
			t1.add(new Day(3, MonthConstants.OCTOBER, 2001), new Double(1.6041));
			t1.add(new Day(4, MonthConstants.OCTOBER, 2001), new Double(1.6172));
			t1.add(new Day(5, MonthConstants.OCTOBER, 2001), new Double(1.6121));
			t1.add(new Day(8, MonthConstants.OCTOBER, 2001), new Double(1.6044));
			t1.add(new Day(9, MonthConstants.OCTOBER, 2001), new Double(1.5974));
			t1.add(new Day(10, MonthConstants.OCTOBER, 2001), new Double(1.5915));
			t1.add(new Day(11, MonthConstants.OCTOBER, 2001), new Double(1.6022));
			t1.add(new Day(12, MonthConstants.OCTOBER, 2001), new Double(1.6014));
			t1.add(new Day(15, MonthConstants.OCTOBER, 2001), new Double(1.5942));
			t1.add(new Day(16, MonthConstants.OCTOBER, 2001), new Double(1.5925));
			t1.add(new Day(17, MonthConstants.OCTOBER, 2001), new Double(1.6007));
			t1.add(new Day(18, MonthConstants.OCTOBER, 2001), new Double(1.6000));
			t1.add(new Day(19, MonthConstants.OCTOBER, 2001), new Double(1.6030));
			t1.add(new Day(22, MonthConstants.OCTOBER, 2001), new Double(1.6014));
			t1.add(new Day(23, MonthConstants.OCTOBER, 2001), new Double(1.5995));
			t1.add(new Day(24, MonthConstants.OCTOBER, 2001), new Double(1.5951));
			t1.add(new Day(25, MonthConstants.OCTOBER, 2001), new Double(1.5953));
			t1.add(new Day(26, MonthConstants.OCTOBER, 2001), new Double(1.6057));
			t1.add(new Day(29, MonthConstants.OCTOBER, 2001), new Double(1.6051));
			t1.add(new Day(30, MonthConstants.OCTOBER, 2001), new Double(1.6027));
			t1.add(new Day(31, MonthConstants.OCTOBER, 2001), new Double(1.6144));
			t1.add(new Day(1, MonthConstants.NOVEMBER, 2001), new Double(1.6139));
			t1.add(new Day(2, MonthConstants.NOVEMBER, 2001), new Double(1.6189));
			t1.add(new Day(5, MonthConstants.NOVEMBER, 2001), new Double(1.6248));
			t1.add(new Day(6, MonthConstants.NOVEMBER, 2001), new Double(1.6267));
			t1.add(new Day(7, MonthConstants.NOVEMBER, 2001), new Double(1.6281));
			t1.add(new Day(8, MonthConstants.NOVEMBER, 2001), new Double(1.6310));
			t1.add(new Day(9, MonthConstants.NOVEMBER, 2001), new Double(1.6313));
			t1.add(new Day(12, MonthConstants.NOVEMBER, 2001), new Double(1.6272));
			t1.add(new Day(13, MonthConstants.NOVEMBER, 2001), new Double(1.6361));
			t1.add(new Day(14, MonthConstants.NOVEMBER, 2001), new Double(1.6323));
			t1.add(new Day(15, MonthConstants.NOVEMBER, 2001), new Double(1.6252));
			t1.add(new Day(16, MonthConstants.NOVEMBER, 2001), new Double(1.6141));
			t1.add(new Day(19, MonthConstants.NOVEMBER, 2001), new Double(1.6086));
			t1.add(new Day(20, MonthConstants.NOVEMBER, 2001), new Double(1.6055));
			t1.add(new Day(21, MonthConstants.NOVEMBER, 2001), new Double(1.6132));
			t1.add(new Day(22, MonthConstants.NOVEMBER, 2001), new Double(1.6074));
			t1.add(new Day(23, MonthConstants.NOVEMBER, 2001), new Double(1.6065));
			t1.add(new Day(26, MonthConstants.NOVEMBER, 2001), new Double(1.6061));
			t1.add(new Day(27, MonthConstants.NOVEMBER, 2001), new Double(1.6039));
			t1.add(new Day(28, MonthConstants.NOVEMBER, 2001), new Double(1.6069));
			t1.add(new Day(29, MonthConstants.NOVEMBER, 2001), new Double(1.6044));
			t1.add(new Day(30, MonthConstants.NOVEMBER, 2001), new Double(1.5928));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return t1;
	}

	/**
	 * Returns a time series of the daily EUR/GBP exchange rates in 2001 (to date), for use in
	 * the JFreeChart demonstration application.
	 * <P>
	 * You wouldn't normally create a time series in this way. Typically, values would be read from a database.
	 * 
	 * @return a sample time series.
	 */
	public static TimeSeries createJPYTimeSeries() {

		final TimeSeries t1 = new TimeSeries("JPY/GBP Exchange Rate");
		try {
			t1.add(new Day(2, MonthConstants.JANUARY, 2001), new Double(171.2612));
			t1.add(new Day(3, MonthConstants.JANUARY, 2001), new Double(172.1076));
			t1.add(new Day(4, MonthConstants.JANUARY, 2001), new Double(172.3485));
			t1.add(new Day(5, MonthConstants.JANUARY, 2001), new Double(173.7023));
			t1.add(new Day(8, MonthConstants.JANUARY, 2001), new Double(174.1253));
			t1.add(new Day(9, MonthConstants.JANUARY, 2001), new Double(173.6386));
			t1.add(new Day(10, MonthConstants.JANUARY, 2001), new Double(173.2623));
			t1.add(new Day(11, MonthConstants.JANUARY, 2001), new Double(175.7319));
			t1.add(new Day(12, MonthConstants.JANUARY, 2001), new Double(174.2442));
			t1.add(new Day(15, MonthConstants.JANUARY, 2001), new Double(175.7583));
			t1.add(new Day(16, MonthConstants.JANUARY, 2001), new Double(173.0719));
			t1.add(new Day(17, MonthConstants.JANUARY, 2001), new Double(173.0805));
			t1.add(new Day(18, MonthConstants.JANUARY, 2001), new Double(174.1975));
			t1.add(new Day(19, MonthConstants.JANUARY, 2001), new Double(172.3138));
			t1.add(new Day(22, MonthConstants.JANUARY, 2001), new Double(170.5016));
			t1.add(new Day(23, MonthConstants.JANUARY, 2001), new Double(172.1836));
			t1.add(new Day(24, MonthConstants.JANUARY, 2001), new Double(172.2154));
			t1.add(new Day(25, MonthConstants.JANUARY, 2001), new Double(170.1515));
			t1.add(new Day(26, MonthConstants.JANUARY, 2001), new Double(170.3728));
			t1.add(new Day(29, MonthConstants.JANUARY, 2001), new Double(170.2911));
			t1.add(new Day(30, MonthConstants.JANUARY, 2001), new Double(170.3995));
			t1.add(new Day(31, MonthConstants.JANUARY, 2001), new Double(169.9110));
			t1.add(new Day(1, MonthConstants.FEBRUARY, 2001), new Double(170.4084));
			t1.add(new Day(2, MonthConstants.FEBRUARY, 2001), new Double(169.8845));
			t1.add(new Day(5, MonthConstants.FEBRUARY, 2001), new Double(169.5120));
			t1.add(new Day(6, MonthConstants.FEBRUARY, 2001), new Double(167.9429));
			t1.add(new Day(7, MonthConstants.FEBRUARY, 2001), new Double(169.6096));
			t1.add(new Day(8, MonthConstants.FEBRUARY, 2001), new Double(167.8282));
			t1.add(new Day(9, MonthConstants.FEBRUARY, 2001), new Double(170.1427));
			t1.add(new Day(12, MonthConstants.FEBRUARY, 2001), new Double(170.8250));
			t1.add(new Day(13, MonthConstants.FEBRUARY, 2001), new Double(170.4005));
			t1.add(new Day(14, MonthConstants.FEBRUARY, 2001), new Double(170.1455));
			t1.add(new Day(15, MonthConstants.FEBRUARY, 2001), new Double(167.6925));
			t1.add(new Day(16, MonthConstants.FEBRUARY, 2001), new Double(167.6133));
			t1.add(new Day(19, MonthConstants.FEBRUARY, 2001), new Double(167.7099));
			t1.add(new Day(20, MonthConstants.FEBRUARY, 2001), new Double(166.9004));
			t1.add(new Day(21, MonthConstants.FEBRUARY, 2001), new Double(168.4231));
			t1.add(new Day(22, MonthConstants.FEBRUARY, 2001), new Double(168.3292));
			t1.add(new Day(23, MonthConstants.FEBRUARY, 2001), new Double(168.6142));
			t1.add(new Day(26, MonthConstants.FEBRUARY, 2001), new Double(168.2608));
			t1.add(new Day(27, MonthConstants.FEBRUARY, 2001), new Double(167.6325));
			t1.add(new Day(28, MonthConstants.FEBRUARY, 2001), new Double(169.1728));
			t1.add(new Day(1, MonthConstants.MARCH, 2001), new Double(170.5199));
			t1.add(new Day(2, MonthConstants.MARCH, 2001), new Double(175.5211));
			t1.add(new Day(5, MonthConstants.MARCH, 2001), new Double(174.9543));
			t1.add(new Day(6, MonthConstants.MARCH, 2001), new Double(174.4053));
			t1.add(new Day(7, MonthConstants.MARCH, 2001), new Double(175.1675));
			t1.add(new Day(8, MonthConstants.MARCH, 2001), new Double(175.7501));
			t1.add(new Day(9, MonthConstants.MARCH, 2001), new Double(175.5956));
			t1.add(new Day(12, MonthConstants.MARCH, 2001), new Double(176.6677));
			t1.add(new Day(13, MonthConstants.MARCH, 2001), new Double(174.4282));
			t1.add(new Day(14, MonthConstants.MARCH, 2001), new Double(175.1140));
			t1.add(new Day(15, MonthConstants.MARCH, 2001), new Double(175.8914));
			t1.add(new Day(16, MonthConstants.MARCH, 2001), new Double(175.7124));
			t1.add(new Day(19, MonthConstants.MARCH, 2001), new Double(174.2307));
			t1.add(new Day(20, MonthConstants.MARCH, 2001), new Double(175.0382));
			t1.add(new Day(21, MonthConstants.MARCH, 2001), new Double(176.1183));
			t1.add(new Day(22, MonthConstants.MARCH, 2001), new Double(176.2646));
			t1.add(new Day(23, MonthConstants.MARCH, 2001), new Double(175.3608));
			t1.add(new Day(26, MonthConstants.MARCH, 2001), new Double(176.5805));
			t1.add(new Day(27, MonthConstants.MARCH, 2001), new Double(176.8495));
			t1.add(new Day(28, MonthConstants.MARCH, 2001), new Double(174.7895));
			t1.add(new Day(29, MonthConstants.MARCH, 2001), new Double(176.6957));
			t1.add(new Day(30, MonthConstants.MARCH, 2001), new Double(178.1106));
			t1.add(new Day(2, MonthConstants.APRIL, 2001), new Double(179.5654));
			t1.add(new Day(3, MonthConstants.APRIL, 2001), new Double(179.7021));
			t1.add(new Day(4, MonthConstants.APRIL, 2001), new Double(179.5065));
			t1.add(new Day(5, MonthConstants.APRIL, 2001), new Double(177.9874));
			t1.add(new Day(6, MonthConstants.APRIL, 2001), new Double(178.3541));
			t1.add(new Day(9, MonthConstants.APRIL, 2001), new Double(181.0301));
			t1.add(new Day(10, MonthConstants.APRIL, 2001), new Double(179.0357));
			t1.add(new Day(11, MonthConstants.APRIL, 2001), new Double(178.8478));
			t1.add(new Day(12, MonthConstants.APRIL, 2001), new Double(177.7927));
			t1.add(new Day(17, MonthConstants.APRIL, 2001), new Double(177.1644));
			t1.add(new Day(18, MonthConstants.APRIL, 2001), new Double(174.1972));
			t1.add(new Day(19, MonthConstants.APRIL, 2001), new Double(174.9370));
			t1.add(new Day(20, MonthConstants.APRIL, 2001), new Double(176.8555));
			t1.add(new Day(23, MonthConstants.APRIL, 2001), new Double(175.3433));
			t1.add(new Day(24, MonthConstants.APRIL, 2001), new Double(175.4792));
			t1.add(new Day(25, MonthConstants.APRIL, 2001), new Double(175.7154));
			t1.add(new Day(26, MonthConstants.APRIL, 2001), new Double(176.1797));
			t1.add(new Day(27, MonthConstants.APRIL, 2001), new Double(177.7074));
			t1.add(new Day(30, MonthConstants.APRIL, 2001), new Double(176.8592));
			t1.add(new Day(1, MonthConstants.MAY, 2001), new Double(174.9104));
			t1.add(new Day(2, MonthConstants.MAY, 2001), new Double(174.8992));
			t1.add(new Day(3, MonthConstants.MAY, 2001), new Double(173.4239));
			t1.add(new Day(4, MonthConstants.MAY, 2001), new Double(173.9663));
			t1.add(new Day(8, MonthConstants.MAY, 2001), new Double(174.4871));
			t1.add(new Day(9, MonthConstants.MAY, 2001), new Double(173.6851));
			t1.add(new Day(10, MonthConstants.MAY, 2001), new Double(174.5957));
			t1.add(new Day(11, MonthConstants.MAY, 2001), new Double(173.6254));
			t1.add(new Day(14, MonthConstants.MAY, 2001), new Double(174.7913));
			t1.add(new Day(15, MonthConstants.MAY, 2001), new Double(175.3932));
			t1.add(new Day(16, MonthConstants.MAY, 2001), new Double(176.7291));
			t1.add(new Day(17, MonthConstants.MAY, 2001), new Double(175.8551));
			t1.add(new Day(18, MonthConstants.MAY, 2001), new Double(176.8558));
			t1.add(new Day(21, MonthConstants.MAY, 2001), new Double(176.6443));
			t1.add(new Day(22, MonthConstants.MAY, 2001), new Double(175.1953));
			t1.add(new Day(23, MonthConstants.MAY, 2001), new Double(171.6117));
			t1.add(new Day(24, MonthConstants.MAY, 2001), new Double(169.0407));
			t1.add(new Day(25, MonthConstants.MAY, 2001), new Double(171.3975));
			t1.add(new Day(29, MonthConstants.MAY, 2001), new Double(170.2811));
			t1.add(new Day(30, MonthConstants.MAY, 2001), new Double(171.2154));
			t1.add(new Day(31, MonthConstants.MAY, 2001), new Double(168.6795));
			t1.add(new Day(1, MonthConstants.JUNE, 2001), new Double(168.2339));
			t1.add(new Day(4, MonthConstants.JUNE, 2001), new Double(169.2090));
			t1.add(new Day(5, MonthConstants.JUNE, 2001), new Double(169.4501));
			t1.add(new Day(6, MonthConstants.JUNE, 2001), new Double(167.8414));
			t1.add(new Day(7, MonthConstants.JUNE, 2001), new Double(166.6042));
			t1.add(new Day(8, MonthConstants.JUNE, 2001), new Double(166.5005));
			t1.add(new Day(11, MonthConstants.JUNE, 2001), new Double(167.2925));
			t1.add(new Day(12, MonthConstants.JUNE, 2001), new Double(168.1171));
			t1.add(new Day(13, MonthConstants.JUNE, 2001), new Double(168.9091));
			t1.add(new Day(14, MonthConstants.JUNE, 2001), new Double(169.8863));
			t1.add(new Day(15, MonthConstants.JUNE, 2001), new Double(171.5254));
			t1.add(new Day(18, MonthConstants.JUNE, 2001), new Double(172.6955));
			t1.add(new Day(19, MonthConstants.JUNE, 2001), new Double(172.3427));
			t1.add(new Day(20, MonthConstants.JUNE, 2001), new Double(172.0421));
			t1.add(new Day(21, MonthConstants.JUNE, 2001), new Double(175.9830));
			t1.add(new Day(22, MonthConstants.JUNE, 2001), new Double(175.5076));
			t1.add(new Day(25, MonthConstants.JUNE, 2001), new Double(175.1080));
			t1.add(new Day(26, MonthConstants.JUNE, 2001), new Double(175.5043));
			t1.add(new Day(27, MonthConstants.JUNE, 2001), new Double(175.3712));
			t1.add(new Day(28, MonthConstants.JUNE, 2001), new Double(175.9575));
			t1.add(new Day(29, MonthConstants.JUNE, 2001), new Double(175.4734));
			t1.add(new Day(2, MonthConstants.JULY, 2001), new Double(175.9908));
			t1.add(new Day(3, MonthConstants.JULY, 2001), new Double(175.2386));
			t1.add(new Day(4, MonthConstants.JULY, 2001), new Double(175.0405));
			t1.add(new Day(5, MonthConstants.JULY, 2001), new Double(175.9451));
			t1.add(new Day(6, MonthConstants.JULY, 2001), new Double(177.3383));
			t1.add(new Day(9, MonthConstants.JULY, 2001), new Double(176.6965));
			t1.add(new Day(10, MonthConstants.JULY, 2001), new Double(177.0476));
			t1.add(new Day(11, MonthConstants.JULY, 2001), new Double(175.6136));
			t1.add(new Day(12, MonthConstants.JULY, 2001), new Double(174.1736));
			t1.add(new Day(13, MonthConstants.JULY, 2001), new Double(174.8619));
			t1.add(new Day(16, MonthConstants.JULY, 2001), new Double(175.4915));
			t1.add(new Day(17, MonthConstants.JULY, 2001), new Double(175.1916));
			t1.add(new Day(18, MonthConstants.JULY, 2001), new Double(176.0599));
			t1.add(new Day(19, MonthConstants.JULY, 2001), new Double(174.8244));
			t1.add(new Day(20, MonthConstants.JULY, 2001), new Double(175.8257));
			t1.add(new Day(23, MonthConstants.JULY, 2001), new Double(176.2682));
			t1.add(new Day(24, MonthConstants.JULY, 2001), new Double(176.1794));
			t1.add(new Day(25, MonthConstants.JULY, 2001), new Double(176.4514));
			t1.add(new Day(26, MonthConstants.JULY, 2001), new Double(176.7673));
			t1.add(new Day(27, MonthConstants.JULY, 2001), new Double(176.1476));
			t1.add(new Day(30, MonthConstants.JULY, 2001), new Double(178.3029));
			t1.add(new Day(31, MonthConstants.JULY, 2001), new Double(178.0895));
			t1.add(new Day(1, MonthConstants.AUGUST, 2001), new Double(178.6438));
			t1.add(new Day(2, MonthConstants.AUGUST, 2001), new Double(177.1364));
			t1.add(new Day(3, MonthConstants.AUGUST, 2001), new Double(176.4042));
			t1.add(new Day(6, MonthConstants.AUGUST, 2001), new Double(175.7999));
			t1.add(new Day(7, MonthConstants.AUGUST, 2001), new Double(175.5131));
			t1.add(new Day(8, MonthConstants.AUGUST, 2001), new Double(173.9804));
			t1.add(new Day(9, MonthConstants.AUGUST, 2001), new Double(174.9459));
			t1.add(new Day(10, MonthConstants.AUGUST, 2001), new Double(173.8883));
			t1.add(new Day(13, MonthConstants.AUGUST, 2001), new Double(173.8253));
			t1.add(new Day(14, MonthConstants.AUGUST, 2001), new Double(173.0352));
			t1.add(new Day(15, MonthConstants.AUGUST, 2001), new Double(172.4666));
			t1.add(new Day(16, MonthConstants.AUGUST, 2001), new Double(173.4173));
			t1.add(new Day(17, MonthConstants.AUGUST, 2001), new Double(173.6289));
			t1.add(new Day(20, MonthConstants.AUGUST, 2001), new Double(174.3824));
			t1.add(new Day(21, MonthConstants.AUGUST, 2001), new Double(173.5063));
			t1.add(new Day(22, MonthConstants.AUGUST, 2001), new Double(174.3372));
			t1.add(new Day(23, MonthConstants.AUGUST, 2001), new Double(173.8620));
			t1.add(new Day(24, MonthConstants.AUGUST, 2001), new Double(173.5825));
			t1.add(new Day(28, MonthConstants.AUGUST, 2001), new Double(174.7664));
			t1.add(new Day(29, MonthConstants.AUGUST, 2001), new Double(173.5166));
			t1.add(new Day(30, MonthConstants.AUGUST, 2001), new Double(173.8555));
			t1.add(new Day(31, MonthConstants.AUGUST, 2001), new Double(172.6675));
			t1.add(new Day(3, MonthConstants.SEPTEMBER, 2001), new Double(172.3986));
			t1.add(new Day(4, MonthConstants.SEPTEMBER, 2001), new Double(171.8860));
			t1.add(new Day(5, MonthConstants.SEPTEMBER, 2001), new Double(174.8640));
			t1.add(new Day(6, MonthConstants.SEPTEMBER, 2001), new Double(176.1399));
			t1.add(new Day(7, MonthConstants.SEPTEMBER, 2001), new Double(175.7110));
			t1.add(new Day(10, MonthConstants.SEPTEMBER, 2001), new Double(176.3085));
			t1.add(new Day(11, MonthConstants.SEPTEMBER, 2001), new Double(174.6263));
			t1.add(new Day(12, MonthConstants.SEPTEMBER, 2001), new Double(174.8058));
			t1.add(new Day(13, MonthConstants.SEPTEMBER, 2001), new Double(174.8257));
			t1.add(new Day(14, MonthConstants.SEPTEMBER, 2001), new Double(172.3107));
			t1.add(new Day(17, MonthConstants.SEPTEMBER, 2001), new Double(172.5397));
			t1.add(new Day(18, MonthConstants.SEPTEMBER, 2001), new Double(171.7004));
			t1.add(new Day(19, MonthConstants.SEPTEMBER, 2001), new Double(172.1289));
			t1.add(new Day(20, MonthConstants.SEPTEMBER, 2001), new Double(170.3143));
			t1.add(new Day(21, MonthConstants.SEPTEMBER, 2001), new Double(169.9737));
			t1.add(new Day(24, MonthConstants.SEPTEMBER, 2001), new Double(172.0319));
			t1.add(new Day(25, MonthConstants.SEPTEMBER, 2001), new Double(172.5516));
			t1.add(new Day(26, MonthConstants.SEPTEMBER, 2001), new Double(173.8612));
			t1.add(new Day(27, MonthConstants.SEPTEMBER, 2001), new Double(176.5408));
			t1.add(new Day(28, MonthConstants.SEPTEMBER, 2001), new Double(175.1092));
			t1.add(new Day(1, MonthConstants.OCTOBER, 2001), new Double(177.6150));
			t1.add(new Day(2, MonthConstants.OCTOBER, 2001), new Double(177.1049));
			t1.add(new Day(3, MonthConstants.OCTOBER, 2001), new Double(178.2525));
			t1.add(new Day(4, MonthConstants.OCTOBER, 2001), new Double(178.0819));
			t1.add(new Day(5, MonthConstants.OCTOBER, 2001), new Double(178.1643));
			t1.add(new Day(8, MonthConstants.OCTOBER, 2001), new Double(176.6654));
			t1.add(new Day(9, MonthConstants.OCTOBER, 2001), new Double(176.0773));
			t1.add(new Day(10, MonthConstants.OCTOBER, 2001), new Double(174.4806));
			t1.add(new Day(11, MonthConstants.OCTOBER, 2001), new Double(175.1855));
			t1.add(new Day(12, MonthConstants.OCTOBER, 2001), new Double(176.1221));
			t1.add(new Day(15, MonthConstants.OCTOBER, 2001), new Double(175.1425));
			t1.add(new Day(16, MonthConstants.OCTOBER, 2001), new Double(175.4683));
			t1.add(new Day(17, MonthConstants.OCTOBER, 2001), new Double(175.4936));
			t1.add(new Day(18, MonthConstants.OCTOBER, 2001), new Double(174.8134));
			t1.add(new Day(19, MonthConstants.OCTOBER, 2001), new Double(174.4492));
			t1.add(new Day(22, MonthConstants.OCTOBER, 2001), new Double(174.1978));
			t1.add(new Day(23, MonthConstants.OCTOBER, 2001), new Double(174.8360));
			t1.add(new Day(24, MonthConstants.OCTOBER, 2001), new Double(174.9378));
			t1.add(new Day(25, MonthConstants.OCTOBER, 2001), new Double(175.4385));
			t1.add(new Day(26, MonthConstants.OCTOBER, 2001), new Double(176.4207));
			t1.add(new Day(29, MonthConstants.OCTOBER, 2001), new Double(177.0540));
			t1.add(new Day(30, MonthConstants.OCTOBER, 2001), new Double(177.1128));
			t1.add(new Day(31, MonthConstants.OCTOBER, 2001), new Double(177.9818));
			t1.add(new Day(1, MonthConstants.NOVEMBER, 2001), new Double(177.9595));
			t1.add(new Day(2, MonthConstants.NOVEMBER, 2001), new Double(177.9251));
			t1.add(new Day(5, MonthConstants.NOVEMBER, 2001), new Double(177.2003));
			t1.add(new Day(6, MonthConstants.NOVEMBER, 2001), new Double(176.6169));
			t1.add(new Day(7, MonthConstants.NOVEMBER, 2001), new Double(177.3191));
			t1.add(new Day(8, MonthConstants.NOVEMBER, 2001), new Double(175.7736));
			t1.add(new Day(9, MonthConstants.NOVEMBER, 2001), new Double(175.2104));
			t1.add(new Day(12, MonthConstants.NOVEMBER, 2001), new Double(175.0749));
			t1.add(new Day(13, MonthConstants.NOVEMBER, 2001), new Double(175.2402));
			t1.add(new Day(14, MonthConstants.NOVEMBER, 2001), new Double(175.3503));
			t1.add(new Day(15, MonthConstants.NOVEMBER, 2001), new Double(175.2810));
			t1.add(new Day(16, MonthConstants.NOVEMBER, 2001), new Double(175.4077));
			t1.add(new Day(19, MonthConstants.NOVEMBER, 2001), new Double(174.3462));
			t1.add(new Day(20, MonthConstants.NOVEMBER, 2001), new Double(173.8177));
			t1.add(new Day(21, MonthConstants.NOVEMBER, 2001), new Double(174.0356));
			t1.add(new Day(22, MonthConstants.NOVEMBER, 2001), new Double(175.0548));
			t1.add(new Day(23, MonthConstants.NOVEMBER, 2001), new Double(175.2207));
			t1.add(new Day(26, MonthConstants.NOVEMBER, 2001), new Double(175.4978));
			t1.add(new Day(27, MonthConstants.NOVEMBER, 2001), new Double(175.2191));
			t1.add(new Day(28, MonthConstants.NOVEMBER, 2001), new Double(175.4236));
			t1.add(new Day(29, MonthConstants.NOVEMBER, 2001), new Double(176.2304));
			t1.add(new Day(30, MonthConstants.NOVEMBER, 2001), new Double(175.6119));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return t1;
	}

	/**
	 * Returns a sample signal dataset.
	 * 
	 * @return a sample signal dataset.
	 */
	public static SignalsDataset createSampleSignalDataset() {
		return new SampleSignalDataset();
	}

	/**
	 * Creates a sample high low dataset.
	 * 
	 * @return a sample high low dataset.
	 */
	public static HighLowDataset createHighLowDataset() {

		final Date[] date = new Date[47];
		final double[] high = new double[47];
		final double[] low = new double[47];
		final double[] open = new double[47];
		final double[] close = new double[47];
		final double[] volume = new double[47];

		final int jan = 1;
		final int feb = 2;

		date[0] = DateUtilities.createDate(2001, jan, 4, 12, 0);
		high[0] = 47.0;
		low[0] = 33.0;
		open[0] = 35.0;
		close[0] = 33.0;
		volume[0] = 100.0;

		date[1] = DateUtilities.createDate(2001, jan, 5, 12, 0);
		high[1] = 47.0;
		low[1] = 32.0;
		open[1] = 41.0;
		close[1] = 37.0;
		volume[1] = 150.0;

		date[2] = DateUtilities.createDate(2001, jan, 6, 12, 0);
		high[2] = 49.0;
		low[2] = 43.0;
		open[2] = 46.0;
		close[2] = 48.0;
		volume[2] = 70.0;

		date[3] = DateUtilities.createDate(2001, jan, 7, 12, 0);
		high[3] = 51.0;
		low[3] = 39.0;
		open[3] = 40.0;
		close[3] = 47.0;
		volume[3] = 200.0;

		date[4] = DateUtilities.createDate(2001, jan, 8, 12, 0);
		high[4] = 60.0;
		low[4] = 40.0;
		open[4] = 46.0;
		close[4] = 53.0;
		volume[4] = 120.0;

		date[5] = DateUtilities.createDate(2001, jan, 9, 12, 0);
		high[5] = 62.0;
		low[5] = 55.0;
		open[5] = 57.0;
		close[5] = 61.0;
		volume[5] = 110.0;

		date[6] = DateUtilities.createDate(2001, jan, 10, 12, 0);
		high[6] = 65.0;
		low[6] = 56.0;
		open[6] = 62.0;
		close[6] = 59.0;
		volume[6] = 70.0;

		date[7] = DateUtilities.createDate(2001, jan, 11, 12, 0);
		high[7] = 55.0;
		low[7] = 43.0;
		open[7] = 45.0;
		close[7] = 47.0;
		volume[7] = 20.0;

		date[8] = DateUtilities.createDate(2001, jan, 12, 12, 0);
		high[8] = 54.0;
		low[8] = 33.0;
		open[8] = 40.0;
		close[8] = 51.0;
		volume[8] = 30.0;

		date[9] = DateUtilities.createDate(2001, jan, 13, 12, 0);
		high[9] = 47.0;
		low[9] = 33.0;
		open[9] = 35.0;
		close[9] = 33.0;
		volume[9] = 100.0;

		date[10] = DateUtilities.createDate(2001, jan, 14, 12, 0);
		high[10] = 54.0;
		low[10] = 38.0;
		open[10] = 43.0;
		close[10] = 52.0;
		volume[10] = 50.0;

		date[11] = DateUtilities.createDate(2001, jan, 15, 12, 0);
		high[11] = 48.0;
		low[11] = 41.0;
		open[11] = 44.0;
		close[11] = 41.0;
		volume[11] = 80.0;

		date[12] = DateUtilities.createDate(2001, jan, 17, 12, 0);
		high[12] = 60.0;
		low[12] = 30.0;
		open[12] = 34.0;
		close[12] = 44.0;
		volume[12] = 90.0;

		date[13] = DateUtilities.createDate(2001, jan, 18, 12, 0);
		high[13] = 58.0;
		low[13] = 44.0;
		open[13] = 54.0;
		close[13] = 56.0;
		volume[13] = 20.0;

		date[14] = DateUtilities.createDate(2001, jan, 19, 12, 0);
		high[14] = 54.0;
		low[14] = 32.0;
		open[14] = 42.0;
		close[14] = 53.0;
		volume[14] = 70.0;

		date[15] = DateUtilities.createDate(2001, jan, 20, 12, 0);
		high[15] = 53.0;
		low[15] = 39.0;
		open[15] = 50.0;
		close[15] = 49.0;
		volume[15] = 60.0;

		date[16] = DateUtilities.createDate(2001, jan, 21, 12, 0);
		high[16] = 47.0;
		low[16] = 33.0;
		open[16] = 41.0;
		close[16] = 40.0;
		volume[16] = 30.0;

		date[17] = DateUtilities.createDate(2001, jan, 22, 12, 0);
		high[17] = 55.0;
		low[17] = 37.0;
		open[17] = 43.0;
		close[17] = 45.0;
		volume[17] = 90.0;

		date[18] = DateUtilities.createDate(2001, jan, 23, 12, 0);
		high[18] = 54.0;
		low[18] = 42.0;
		open[18] = 50.0;
		close[18] = 42.0;
		volume[18] = 150.0;

		date[19] = DateUtilities.createDate(2001, jan, 24, 12, 0);
		high[19] = 48.0;
		low[19] = 37.0;
		open[19] = 37.0;
		close[19] = 47.0;
		volume[19] = 120.0;

		date[20] = DateUtilities.createDate(2001, jan, 25, 12, 0);
		high[20] = 58.0;
		low[20] = 33.0;
		open[20] = 39.0;
		close[20] = 41.0;
		volume[20] = 80.0;

		date[21] = DateUtilities.createDate(2001, jan, 26, 12, 0);
		high[21] = 47.0;
		low[21] = 31.0;
		open[21] = 36.0;
		close[21] = 41.0;
		volume[21] = 40.0;

		date[22] = DateUtilities.createDate(2001, jan, 27, 12, 0);
		high[22] = 58.0;
		low[22] = 44.0;
		open[22] = 49.0;
		close[22] = 44.0;
		volume[22] = 20.0;

		date[23] = DateUtilities.createDate(2001, jan, 28, 12, 0);
		high[23] = 46.0;
		low[23] = 41.0;
		open[23] = 43.0;
		close[23] = 44.0;
		volume[23] = 60.0;

		date[24] = DateUtilities.createDate(2001, jan, 29, 12, 0);
		high[24] = 56.0;
		low[24] = 39.0;
		open[24] = 39.0;
		close[24] = 51.0;
		volume[24] = 40.0;

		date[25] = DateUtilities.createDate(2001, jan, 30, 12, 0);
		high[25] = 56.0;
		low[25] = 39.0;
		open[25] = 47.0;
		close[25] = 49.0;
		volume[25] = 70.0;

		date[26] = DateUtilities.createDate(2001, jan, 31, 12, 0);
		high[26] = 53.0;
		low[26] = 39.0;
		open[26] = 52.0;
		close[26] = 47.0;
		volume[26] = 60.0;

		date[27] = DateUtilities.createDate(2001, feb, 1, 12, 0);
		high[27] = 51.0;
		low[27] = 30.0;
		open[27] = 45.0;
		close[27] = 47.0;
		volume[27] = 90.0;

		date[28] = DateUtilities.createDate(2001, feb, 2, 12, 0);
		high[28] = 47.0;
		low[28] = 30.0;
		open[28] = 34.0;
		close[28] = 46.0;
		volume[28] = 100.0;

		date[29] = DateUtilities.createDate(2001, feb, 3, 12, 0);
		high[29] = 57.0;
		low[29] = 37.0;
		open[29] = 44.0;
		close[29] = 56.0;
		volume[29] = 20.0;

		date[30] = DateUtilities.createDate(2001, feb, 4, 12, 0);
		high[30] = 49.0;
		low[30] = 40.0;
		open[30] = 47.0;
		close[30] = 44.0;
		volume[30] = 50.0;

		date[31] = DateUtilities.createDate(2001, feb, 5, 12, 0);
		high[31] = 46.0;
		low[31] = 38.0;
		open[31] = 43.0;
		close[31] = 40.0;
		volume[31] = 70.0;

		date[32] = DateUtilities.createDate(2001, feb, 6, 12, 0);
		high[32] = 55.0;
		low[32] = 38.0;
		open[32] = 39.0;
		close[32] = 53.0;
		volume[32] = 120.0;

		date[33] = DateUtilities.createDate(2001, feb, 7, 12, 0);
		high[33] = 50.0;
		low[33] = 33.0;
		open[33] = 37.0;
		close[33] = 37.0;
		volume[33] = 140.0;

		date[34] = DateUtilities.createDate(2001, feb, 8, 12, 0);
		high[34] = 59.0;
		low[34] = 34.0;
		open[34] = 57.0;
		close[34] = 43.0;
		volume[34] = 70.0;

		date[35] = DateUtilities.createDate(2001, feb, 9, 12, 0);
		high[35] = 48.0;
		low[35] = 39.0;
		open[35] = 46.0;
		close[35] = 47.0;
		volume[35] = 70.0;

		date[36] = DateUtilities.createDate(2001, feb, 10, 12, 0);
		high[36] = 55.0;
		low[36] = 30.0;
		open[36] = 37.0;
		close[36] = 30.0;
		volume[36] = 30.0;

		date[37] = DateUtilities.createDate(2001, feb, 11, 12, 0);
		high[37] = 60.0;
		low[37] = 32.0;
		open[37] = 56.0;
		close[37] = 36.0;
		volume[37] = 70.0;

		date[38] = DateUtilities.createDate(2001, feb, 12, 12, 0);
		high[38] = 56.0;
		low[38] = 42.0;
		open[38] = 53.0;
		close[38] = 54.0;
		volume[38] = 40.0;

		date[39] = DateUtilities.createDate(2001, feb, 13, 12, 0);
		high[39] = 49.0;
		low[39] = 42.0;
		open[39] = 45.0;
		close[39] = 42.0;
		volume[39] = 90.0;

		date[40] = DateUtilities.createDate(2001, feb, 14, 12, 0);
		high[40] = 55.0;
		low[40] = 42.0;
		open[40] = 47.0;
		close[40] = 54.0;
		volume[40] = 70.0;

		date[41] = DateUtilities.createDate(2001, feb, 15, 12, 0);
		high[41] = 49.0;
		low[41] = 35.0;
		open[41] = 38.0;
		close[41] = 35.0;
		volume[41] = 20.0;

		date[42] = DateUtilities.createDate(2001, feb, 16, 12, 0);
		high[42] = 47.0;
		low[42] = 38.0;
		open[42] = 43.0;
		close[42] = 42.0;
		volume[42] = 10.0;

		date[43] = DateUtilities.createDate(2001, feb, 17, 12, 0);
		high[43] = 53.0;
		low[43] = 42.0;
		open[43] = 47.0;
		close[43] = 48.0;
		volume[43] = 20.0;

		date[44] = DateUtilities.createDate(2001, feb, 18, 12, 0);
		high[44] = 47.0;
		low[44] = 44.0;
		open[44] = 46.0;
		close[44] = 44.0;
		volume[44] = 30.0;

		date[45] = DateUtilities.createDate(2001, feb, 19, 12, 0);
		high[45] = 46.0;
		low[45] = 40.0;
		open[45] = 43.0;
		close[45] = 44.0;
		volume[45] = 50.0;

		date[46] = DateUtilities.createDate(2001, feb, 20, 12, 0);
		high[46] = 48.0;
		low[46] = 41.0;
		open[46] = 46.0;
		close[46] = 41.0;
		volume[46] = 100.0;

		return new DefaultHighLowDataset("Series 1", date, high, low, open, close, volume);

	}

	/**
	 * Creates a sample high low dataset for a SegmentedTimeline
	 * 
	 * @param timeline
	 *           SegmenteTimeline that will use this dataset.
	 * @param start
	 *           Date from where the dataset will be generated. Actual dates will
	 *           be generated dynamically based on the timeline.
	 * @return a sample high low dataset.
	 */
	public static HighLowDataset createSegmentedHighLowDataset(
						final SegmentedTimeline timeline, final Date start) {

		// some open-high-low-close data
		final double[][] data =
					{ { 248.1999, 249.3999, 247.0499, 247.6999 },
										{ 247.4999, 250.6499, 246.7999, 249.3999 },
										{ 249.5999, 249.7499, 247.4999, 248.5999 },
										{ 248.5999, 251.5499, 248.4999, 248.6499 },
										{ 248.8499, 249.4499, 247.8499, 248.7999 },
										{ 249.1999, 250.5499, 248.4999, 248.7999 },
										{ 249.2999, 251.1499, 248.9499, 249.1499 },
										{ 248.1499, 249.8999, 247.2999, 249.0499 },
										{ 248.5999, 248.8999, 246.2999, 246.9499 },
										{ 247.1999, 248.3999, 246.6499, 248.3499 },
										{ 246.0999, 246.5999, 244.4999, 244.5999 },
										{ 243.1999, 243.3999, 240.9499, 242.3499 },
										{ 243.5999, 243.5999, 242.2499, 242.8999 },
										{ 242.4999, 243.1499, 241.5999, 242.8499 },
										{ 244.1999, 247.0499, 243.7499, 246.9999 },
										{ 246.9499, 247.6499, 245.2999, 246.0499 },
										{ 245.5999, 248.0999, 245.1999, 247.8999 },
										{ 247.9499, 247.9499, 243.8499, 243.9499 },
										{ 242.1999, 245.9499, 242.1999, 244.7499 },
										{ 244.6499, 246.5999, 244.4999, 245.5999 },
										{ 245.4499, 249.1999, 245.0999, 249.0999 },
										{ 249.0999, 250.2999, 248.4499, 249.2499 },
										{ 249.4999, 249.8499, 246.7499, 246.8499 },
										{ 246.8499, 247.6499, 245.8999, 246.8499 },
										{ 247.6999, 250.7999, 247.6999, 250.6999 },
										{ 250.8999, 251.4499, 249.0999, 249.4999 },
										{ 249.6499, 252.4999, 249.5999, 251.6499 },
										{ 251.9499, 252.2999, 249.4999, 250.0499 },
										{ 251.2499, 251.6999, 248.7999, 248.9499 },
										{ 249.0999, 250.2499, 247.9499, 249.7499 },
										{ 250.0499, 251.1499, 249.4499, 249.9499 },
										{ 250.0499, 251.1499, 249.4499, 249.9499 },
										{ 249.9999, 250.3499, 246.5999, 246.9499 },
										{ 247.0999, 249.6999, 246.8999, 249.2999 },
										{ 249.8999, 252.9499, 249.8499, 252.3999 },
										{ 252.7999, 253.3499, 251.1999, 251.6999 },
										{ 250.4999, 251.2999, 248.9499, 249.8999 },
										{ 250.6999, 253.4499, 250.6999, 253.1999 },
										{ 252.9999, 253.8999, 252.2999, 253.2499 },
										{ 253.6999, 255.1999, 253.4999, 253.9499 },
										{ 253.4499, 254.7999, 252.7999, 254.3499 },
										{ 253.4499, 254.5999, 252.4999, 254.2999 },
										{ 253.5999, 253.8999, 251.6999, 251.7999 },
										{ 252.3499, 253.6999, 251.7999, 253.5499 },
										{ 253.5499, 254.2499, 251.1999, 251.3499 },
										{ 251.2499, 251.9499, 249.9999, 251.5999 },
										{ 251.9499, 252.5999, 250.2499, 251.9999 },
										{ 251.2499, 252.7499, 251.0999, 252.1999 },
										{ 251.6499, 252.5499, 248.8499, 248.9499 },
										{ 249.6499, 249.8999, 248.5499, 249.0999 },
										{ 249.3499, 250.4499, 248.9499, 250.0999 },
										{ 249.5499, 252.1499, 249.2999, 252.0499 },
										{ 252.1499, 252.1499, 250.2499, 250.8499 },
										{ 251.2499, 254.9499, 250.9999, 254.4499 },
										{ 254.0999, 255.1999, 253.4499, 254.5999 },
										{ 254.4999, 254.9499, 252.3999, 252.8999 },
										{ 253.2999, 253.6499, 252.1499, 252.8999 },
										{ 253.4999, 254.1499, 251.8999, 252.0499 },
										{ 252.3499, 254.4499, 252.3499, 254.2999 },
										{ 254.6499, 255.7499, 251.4499, 251.6499 },
										{ 254.6499, 255.7499, 251.4499, 251.6499 },
										{ 252.2499, 253.1499, 251.5999, 252.9499 },
										{ 253.4499, 253.9499, 251.0999, 251.4999 },
										{ 251.7499, 251.8499, 249.4499, 251.0999 },
										{ 250.8499, 251.7999, 249.9499, 251.5499 },
										{ 251.5499, 252.1499, 250.3499, 251.5999 },
										{ 252.9999, 254.9499, 252.7999, 254.8499 },
										{ 254.6999, 255.4499, 253.8999, 255.3499 },
										{ 254.9999, 256.9500, 254.9999, 256.0999 },
										{ 256.4500, 258.2499, 255.3499, 258.1499 },
										{ 257.4500, 258.6499, 257.2499, 257.9500 },
										{ 257.7499, 259.1499, 257.2000, 258.7999 },
										{ 257.8999, 258.2000, 256.7499, 257.7000 },
										{ 257.9500, 260.2999, 257.5999, 259.9500 },
										{ 259.2499, 260.4500, 258.8499, 259.4999 },
										{ 259.4500, 260.2499, 259.1499, 259.5499 },
										{ 260.0499, 260.3499, 257.4999, 257.8999 },
										{ 257.8999, 261.9999, 257.3999, 261.8999 },
										{ 261.8999, 262.5499, 259.8499, 261.6499 },
										{ 261.5499, 263.3499, 261.0999, 263.0499 },
										{ 263.1499, 264.4500, 262.3499, 263.9999 },
										{ 264.1499, 264.2999, 261.8499, 262.7999 },
										{ 262.6499, 263.2499, 261.5499, 262.9500 },
										{ 263.2999, 264.9500, 262.6499, 263.9500 },
										{ 263.5999, 264.8499, 263.4500, 264.5999 },
										{ 264.7499, 268.0999, 264.7499, 267.2499 },
										{ 266.3499, 267.7499, 265.7000, 266.8499 },
										{ 267.0999, 267.6499, 266.6499, 266.8499 },
										{ 266.6499, 267.0499, 264.7499, 265.7499 },
										{ 265.4500, 265.7499, 264.2499, 264.8999 },
										{ 265.3499, 266.4500, 265.2999, 265.5999 },
										{ 263.8499, 264.0499, 262.8499, 263.9999 },
										{ 263.9500, 264.5499, 262.9500, 264.2999 },
										{ 264.5999, 265.5499, 262.7499, 262.7999 },
										{ 263.3999, 263.5499, 261.3999, 261.8999 },
										{ 262.2000, 262.2000, 260.8499, 261.7000 },
										{ 260.2499, 263.8499, 260.0999, 263.7000 },
										{ 263.2999, 266.0999, 263.2999, 265.8999 },
										{ 266.2000, 266.9999, 264.8499, 266.6499 } };

		final int m = data.length;

		final Date[] date = new Date[m];
		final double[] high = new double[m];
		final double[] low = new double[m];
		final double[] open = new double[m];
		final double[] close = new double[m];
		final double[] volume = new double[m];

		final SegmentedTimeline.Segment segment = timeline.getSegment(start);
		for (int i = 0; i < m; i++) {
			while (!segment.inIncludeSegments()) {
				segment.inc();
			}
			date[i] = segment.getDate();
			open[i] = data[i][0];
			high[i] = data[i][1];
			low[i] = data[i][2];
			close[i] = data[i][3];

			segment.inc();
		}

		return new DefaultHighLowDataset("Series 1", date, high, low, open, close, volume);

	}

	/**
	 * Creates a sample wind dataset.
	 * 
	 * @return a sample wind dataset.
	 */
	public static WindDataset createWindDataset1() {

		final int jan = 1;
		final Object[][][] data = new Object[][][] { {
							{ DateUtilities.createDate(1999, jan, 3), new Double(0.0), new Double(10.0) },
							{ DateUtilities.createDate(1999, jan, 4), new Double(1.0), new Double(8.5) },
							{ DateUtilities.createDate(1999, jan, 5), new Double(2.0), new Double(10.0) },
							{ DateUtilities.createDate(1999, jan, 6), new Double(3.0), new Double(10.0) },
							{ DateUtilities.createDate(1999, jan, 7), new Double(4.0), new Double(7.0) },
							{ DateUtilities.createDate(1999, jan, 8), new Double(5.0), new Double(10.0) },
							{ DateUtilities.createDate(1999, jan, 9), new Double(6.0), new Double(8.0) },
							{ DateUtilities.createDate(1999, jan, 10), new Double(7.0), new Double(11.0) },
							{ DateUtilities.createDate(1999, jan, 11), new Double(8.0), new Double(10.0) },
							{ DateUtilities.createDate(1999, jan, 12), new Double(9.0), new Double(11.0) },
							{ DateUtilities.createDate(1999, jan, 13), new Double(10.0), new Double(3.0) },
							{ DateUtilities.createDate(1999, jan, 14), new Double(11.0), new Double(9.0) },
							{ DateUtilities.createDate(1999, jan, 15), new Double(12.0), new Double(11.0) },
							{ DateUtilities.createDate(1999, jan, 16), new Double(0.0), new Double(0.0) } } };

		return new DefaultWindDataset(new String[] { "Wind!!" }, data);
	}

	/**
	 * Creates a sample wafermap dataset.
	 * 
	 * @return a sample wafermap dataset
	 */
	public static WaferMapDataset createWaferMapDataset() {
		final WaferMapDataset data = new WaferMapDataset(30, 20);
		data.addValue(1, 5, 14); // (value, chipx, chipy)
		data.addValue(1, 5, 13);
		data.addValue(1, 5, 12);
		data.addValue(1, 5, 11);
		data.addValue(1, 5, 10);
		data.addValue(1, 5, 9);
		data.addValue(7, 5, 8);
		data.addValue(8, 5, 7);
		data.addValue(9, 5, 6);
		data.addValue(1, 6, 10);
		data.addValue(1, 7, 10);
		data.addValue(1, 8, 10);
		data.addValue(1, 9, 10);
		data.addValue(1, 10, 10);
		data.addValue(1, 11, 10);
		data.addValue(1, 11, 11);
		data.addValue(1, 11, 12);
		data.addValue(2, 11, 13);
		data.addValue(1, 11, 14);
		data.addValue(2, 11, 9);
		data.addValue(2, 11, 8);
		data.addValue(2, 11, 7);
		data.addValue(2, 11, 6);

		data.addValue(6, 16, 6);
		data.addValue(6, 17, 6);
		data.addValue(6, 18, 6);
		data.addValue(6, 19, 6);
		data.addValue(6, 20, 6);
		data.addValue(6, 21, 6);
		data.addValue(6, 22, 6);
		data.addValue(3, 19, 7);
		data.addValue(3, 19, 8);
		data.addValue(3, 19, 9);
		data.addValue(3, 19, 10);
		data.addValue(3, 19, 11);
		data.addValue(3, 19, 12);
		data.addValue(3, 19, 13);
		data.addValue(4, 19, 14);
		data.addValue(4, 18, 14);
		data.addValue(4, 17, 14);
		data.addValue(4, 16, 14);
		data.addValue(4, 20, 14);
		data.addValue(4, 21, 14);
		data.addValue(4, 22, 14);
		return data;
	}

	/**
	 * Creates a random wafermap dataset
	 * 
	 * @param values
	 *           Maximum number of random values
	 * @return a random wafermap dataset
	 */
	public static WaferMapDataset createRandomWaferMapDataset(final int values) {
		final int xdim = 30;
		final int ydim = 20;
		final Random random = new Random();
		final WaferMapDataset data = new WaferMapDataset(xdim, ydim);
		for (int x = 1; x <= xdim; x++) {
			for (int y = 0; y < ydim; y++) {
				data.addValue(random.nextInt(values), x, y);
			}
		}
		return data;
	}

}
