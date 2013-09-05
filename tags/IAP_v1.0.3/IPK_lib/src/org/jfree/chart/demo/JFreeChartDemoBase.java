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
 * JFreeChartDemoBase.java
 * -----------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Andrzej Porebski;
 * Matthew Wright;
 * Serge V. Grachov;
 * Bill Kelemen;
 * Achilleus Mantzios;
 * Bryan Scott;
 * Robert Redburn;
 * $Id: JFreeChartDemoBase.java,v 1.1 2011-01-31 09:01:52 klukas Exp $
 * Changes
 * -------
 * 27-Jul-2002 : Created (BRS);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 19-Jan-2004 : Added createWaferMapChart(), createWaferMapChartValueIndexed(),
 * createWaferMapChartPositionIndexed() methods (RR);
 * 25-Feb-2004 : Renamed XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 27-Apr-2004 : Modified for changes to XYPlot class (DG);
 * 27-Apr-2004 : added createPieChartThree for comparable pie dataset (Benoit Xhenseval)
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardPieItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.plot.WaferMapPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.HighLowRenderer;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.WaferMapRenderer;
import org.jfree.chart.renderer.XYBarRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.CategoryDataset;
import org.jfree.data.CombinedDataset;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.DefaultValueDataset;
import org.jfree.data.HighLowDataset;
import org.jfree.data.IntervalCategoryDataset;
import org.jfree.data.MovingAverage;
import org.jfree.data.PieDataset;
import org.jfree.data.SignalsDataset;
import org.jfree.data.SubSeriesDataset;
import org.jfree.data.WaferMapDataset;
import org.jfree.data.WindDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Spacer;

/**
 * A simple class that allows the swing and servlet chart demonstrations
 * to share chart generating code.
 * If you would like to add a chart to the swing and/or servlet demo do so here.
 */
public class JFreeChartDemoBase {

	/**
	 * CHART_COMMANDS holds information on charts that can be created
	 * Format is
	 * Name, Creation Method, Resource file prefix
	 * Steps To add a chart
	 * 1) Create a createChart method which returns a JFreeChart
	 * 2) Append details to CHART_COMMANDS
	 * 3) Append details to DemoResources
	 */
	public static final String[][] CHART_COMMANDS = {
						{ "HORIZONTAL_BAR_CHART", "createHorizontalBarChart", "chart1" },
						{ "HORIZONTAL_STACKED_BAR_CHART", "createStackedHorizontalBarChart", "chart2" },
						{ "VERTICAL_BAR_CHART", "createVerticalBarChart", "chart3" },
						{ "VERTICAL_3D_BAR_CHART", "createVertical3DBarChart", "chart4" },
						{ "VERTICAL_STACKED_BAR_CHART", "createVerticalStackedBarChart", "chart5" },
						{ "VERTICAL_STACKED_3D_BAR_CHART", "createVerticalStacked3DBarChart", "chart6" },
						{ "PIE_CHART_1", "createPieChartOne", "chart7" },
						{ "PIE_CHART_2", "createPieChartTwo", "chart8" },
						{ "PIE_CHART_3", "createPieChartThree", "chart39" },
						{ "XY_PLOT", "createXYPlot", "chart9" },
						{ "TIME_SERIES_1_CHART", "createTimeSeries1Chart", "chart10" },
						{ "TIME_SERIES_2_CHART", "createTimeSeries2Chart", "chart11" },
						{ "TIME_SERIES_WITH_MA_CHART", "createTimeSeriesWithMAChart", "chart12" },
						{ "HIGH_LOW_CHART", "createHighLowChart", "chart13" },
						{ "CANDLESTICK_CHART", "createCandlestickChart", "chart14" },
						{ "SIGNAL_CHART", "createSignalChart", "chart15" },
						{ "WIND_PLOT", "createWindPlot", "chart16" },
						{ "SCATTER_PLOT", "createScatterPlot", "chart17" },
						{ "LINE_CHART", "createLineChart", "chart18" },
						{ "VERTICAL_XY_BAR_CHART", "createVerticalXYBarChart", "chart19" },
						{ "XY_PLOT_NULL", "createNullXYPlot", "chart20" },
						{ "XY_PLOT_ZERO", "createXYPlotZeroData", "chart21" },
						{ "TIME_SERIES_CHART_SCROLL", "createTimeSeriesChartInScrollPane", "chart22" },
						{ "SINGLE_SERIES_BAR_CHART", "createSingleSeriesBarChart", "chart23" },
						{ "DYNAMIC_CHART", "createDynamicXYChart", "chart24" },
						{ "OVERLAID_CHART", "createOverlaidChart", "chart25" },
						{ "HORIZONTALLY_COMBINED_CHART", "createHorizontallyCombinedChart", "chart26" },
						{ "VERTICALLY_COMBINED_CHART", "createVerticallyCombinedChart", "chart27" },
						{ "COMBINED_OVERLAID_CHART", "createCombinedAndOverlaidChart1", "chart28" },
						{ "COMBINED_OVERLAID_DYNAMIC_CHART", "createCombinedAndOverlaidDynamicXYChart", "chart29" },
						{ "THERMOMETER_CHART", "createThermometerChart", "chart30" },
						{ "METER_CHART", "createMeterChartCircle", "chart31" },
						{ "GANTT_CHART", "createGanttChart", "chart32" },
						{ "METER_CHART2", "createMeterChartPie", "chart33" },
						{ "METER_CHART3", "createMeterChartChord", "chart34" },
						{ "COMPASS_CHART", "createCompassChart", "chart35" },
						{ "WAFERMAP_CHART", "createWaferMapChart", "chart36" },
						{ "WAFERMAP_VALUE_CHART", "createWaferMapChartValueIndexed", "chart37" },
						{ "WAFERMAP_POSITION_CHART", "createWaferMapChartPositionIndexed", "chart38" },
	};

	/** Base class name for localised resources. */
	public static final String BASE_RESOURCE_CLASS = "org.jfree.chart.demo.resources.DemoResources";

	/** Localised resources. */
	private ResourceBundle resources;

	/** An array of charts. */
	private JFreeChart[] charts = new JFreeChart[CHART_COMMANDS.length];

	/**
	 * Default constructor.
	 */
	public JFreeChartDemoBase() {
		this.resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
	}

	/**
	 * Returns a chart.
	 * 
	 * @param i
	 *           the chart index.
	 * @return a chart.
	 */
	public JFreeChart getChart(int i) {

		if ((i < 0) && (i >= this.charts.length)) {
			i = 0;
		}

		if (this.charts[i] == null) {
			// / Utilise reflection to invoke method to create new chart if required.
			try {
				final Method method = getClass().getDeclaredMethod(CHART_COMMANDS[i][1], (Class) null);
				this.charts[i] = (JFreeChart) method.invoke(this, (Object[]) null);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return this.charts[i];
	}

	/**
	 * This makes the resources bundle available. Basically an optimisation so
	 * the demo servlet can access the same resource file.
	 * 
	 * @return the resources bundle.
	 */
	public ResourceBundle getResources() {
		return this.resources;
	}

	/**
	 * Create a horizontal bar chart.
	 * 
	 * @return a horizontal bar chart.
	 */
	public JFreeChart createHorizontalBarChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("bar.horizontal.title");
		final String domain = this.resources.getString("bar.horizontal.domain");
		final String range = this.resources.getString("bar.horizontal.range");

		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final JFreeChart chart = ChartFactory.createBarChart(title, domain, range, data,
							PlotOrientation.HORIZONTAL,
							true,
							true,
							false);

		// then customise it a little...
		chart.getLegend().setAnchor(Legend.EAST);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.orange));
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setRangeCrosshairVisible(false);
		final NumberAxis axis = (NumberAxis) plot.getRangeAxis();
		axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		axis.setInverted(true);

		return chart;

	}

	/**
	 * Creates and returns a sample stacked horizontal bar chart.
	 * 
	 * @return a sample stacked horizontal bar chart.
	 */
	public JFreeChart createStackedHorizontalBarChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("bar.horizontal-stacked.title");
		final String domain = this.resources.getString("bar.horizontal-stacked.domain");
		final String range = this.resources.getString("bar.horizontal-stacked.range");

		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final JFreeChart chart = ChartFactory.createStackedBarChart(title, domain, range,
							data,
							PlotOrientation.HORIZONTAL,
							true,
							true,
							false);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.blue));
		return chart;

	}

	/**
	 * Creates and returns a sample vertical bar chart.
	 * 
	 * @return a sample vertical bar chart.
	 */
	public JFreeChart createVerticalBarChart() {

		final String title = this.resources.getString("bar.vertical.title");
		final String domain = this.resources.getString("bar.vertical.domain");
		final String range = this.resources.getString("bar.vertical.range");

		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final JFreeChart chart = ChartFactory.createBarChart(title, domain, range, data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.red));
		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setForegroundAlpha(0.9f);
		final NumberAxis verticalAxis = (NumberAxis) plot.getRangeAxis();
		verticalAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		return chart;
	}

	/**
	 * Creates and returns a sample vertical 3D bar chart.
	 * 
	 * @return a sample vertical 3D bar chart.
	 */
	public JFreeChart createVertical3DBarChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("bar.vertical3D.title");
		final String domain = this.resources.getString("bar.vertical3D.domain");
		final String range = this.resources.getString("bar.vertical3D.range");

		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final JFreeChart chart = ChartFactory.createBarChart3D(
							title,
							domain,
							range, data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.blue));
		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setForegroundAlpha(0.75f);
		return chart;

	}

	/**
	 * Creates and returns a sample stacked vertical bar chart.
	 * 
	 * @return a sample stacked vertical bar chart.
	 */
	public JFreeChart createVerticalStackedBarChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("bar.vertical-stacked.title");
		final String domain = this.resources.getString("bar.vertical-stacked.domain");
		final String range = this.resources.getString("bar.vertical-stacked.range");

		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final JFreeChart chart = ChartFactory.createStackedBarChart(title, domain, range, data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.red));
		return chart;

	}

	/**
	 * Creates and returns a sample stacked vertical 3D bar chart.
	 * 
	 * @return a sample stacked vertical 3D bar chart.
	 */
	public JFreeChart createVerticalStacked3DBarChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("bar.vertical-stacked3D.title");
		final String domain = this.resources.getString("bar.vertical-stacked3D.domain");
		final String range = this.resources.getString("bar.vertical-stacked3D.range");
		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final JFreeChart chart = ChartFactory.createStackedBarChart3D(title, domain, range, data,
							PlotOrientation.VERTICAL,
							true, true, false);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.red));
		return chart;

	}

	/**
	 * Creates and returns a sample pie chart.
	 * 
	 * @return a sample pie chart.
	 */
	public JFreeChart createPieChartOne() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("pie.pie1.title");
		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final PieDataset extracted = DatasetUtilities.createPieDatasetForRow(data, 0);
		final JFreeChart chart = ChartFactory.createPieChart(title, extracted, true, true, false);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.orange));
		final PiePlot plot = (PiePlot) chart.getPlot();
		plot.setCircular(false);
		// make section 1 explode by 100%...
		plot.setExplodePercent(1, 1.00);
		return chart;

	}

	/**
	 * Creates and returns a sample pie chart.
	 * 
	 * @return a sample pie chart.
	 */
	public JFreeChart createPieChartTwo() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("pie.pie2.title");
		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final Comparable category = (Comparable) data.getColumnKeys().get(1);
		final PieDataset extracted = DatasetUtilities.createPieDatasetForColumn(data, category);
		final JFreeChart chart = ChartFactory.createPieChart(title, extracted, true, true, false);

		// then customise it a little...
		chart.setBackgroundPaint(Color.lightGray);
		final PiePlot pie = (PiePlot) chart.getPlot();
		pie.setLabelGenerator(new StandardPieItemLabelGenerator(
							"{0} = {2}", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()
							));
		pie.setBackgroundImage(JFreeChart.INFO.getLogo());
		pie.setBackgroundPaint(Color.white);
		pie.setBackgroundAlpha(0.6f);
		pie.setForegroundAlpha(0.75f);
		return chart;

	}

	/**
	 * Creates and returns a sample pie chart which compares 2 datasets.
	 * 
	 * @return a sample pie chart.
	 * @author <a href="mailto:opensource@objectlab.co.uk">Benoit Xhenseval</a>
	 * @since 0.9.18
	 */
	public JFreeChart createPieChartThree() {

		// create a default chart based on some sample data...
		String title = this.resources.getString("pie.pie3.title");

		final double[][] data = new double[][]
															{ { 10.0, 4.0, 14.0, 12.0, 12.0 },
																				{ 9.0, 7.0, 13.7, 15.0, 3.0 } };

		CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
							"Series ", "Category ", data
							);

		PieDataset extracted = DatasetUtilities.createPieDatasetForRow(dataset, 0);
		PieDataset extracted2 = DatasetUtilities.createPieDatasetForRow(dataset, 1);

		// generate a basic pie chart with title
		// comparing extracted with extracted2
		// a difference of 40% or more will trigger maximum brightness in red or green
		// true green is for increase
		// true for legend
		// true for tooltips
		// false for urls
		// true for subtitle
		// true for showing the difference
		JFreeChart chart = ChartFactory.createPieChart(
							title, extracted, extracted2, 40, true, true, true, false, true, true
							);

		return chart;

	}

	/**
	 * Creates and returns a sample XY plot.
	 * 
	 * @return a sample XY plot.
	 */
	public JFreeChart createXYPlot() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("xyplot.sample1.title");
		final String domain = this.resources.getString("xyplot.sample1.domain");
		final String range = this.resources.getString("xyplot.sample1.range");
		final XYDataset data = DemoDatasetFactory.createSampleXYDataset();
		final JFreeChart chart = ChartFactory.createXYLineChart(
							title,
							domain, range, data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.green));
		return chart;

	}

	/**
	 * Creates and returns a sample time series chart.
	 * 
	 * @return a sample time series chart.
	 */
	public JFreeChart createTimeSeries1Chart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("timeseries.sample1.title");
		final String subtitle = this.resources.getString("timeseries.sample1.subtitle");
		final String domain = this.resources.getString("timeseries.sample1.domain");
		final String range = this.resources.getString("timeseries.sample1.range");
		final String copyrightStr = this.resources.getString("timeseries.sample1.copyright");
		final XYDataset data = DemoDatasetFactory.createTimeSeriesCollection3();
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
							title, domain, range, data, true, true, false
							);

		// then customise it a little...
		final TextTitle title2 = new TextTitle(subtitle, new Font("SansSerif", Font.PLAIN, 12));
		title2.setSpacer(new Spacer(Spacer.RELATIVE, 0.05, 0.05, 0.05, 0.0));
		chart.addSubtitle(title2);

		final TextTitle copyright = new TextTitle(
							copyrightStr, new Font("SansSerif", Font.PLAIN, 9)
							);
		copyright.setPosition(RectangleEdge.BOTTOM);
		copyright.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		chart.addSubtitle(copyright);

		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		final XYPlot plot = chart.getXYPlot();
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setVerticalTickLabels(true);
		return chart;

	}

	/**
	 * Creates and returns a sample time series chart.
	 * 
	 * @return a sample time series chart.
	 */
	public JFreeChart createTimeSeries2Chart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("timeseries.sample2.title");
		final String subtitleStr = this.resources.getString("timeseries.sample2.subtitle");
		final String domain = this.resources.getString("timeseries.sample2.domain");
		final String range = this.resources.getString("timeseries.sample2.range");
		final XYDataset data = DemoDatasetFactory.createTimeSeriesCollection4();
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(title, domain, range, data,
							true, true, false);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		final XYPlot plot = chart.getXYPlot();
		final LogarithmicAxis rangeAxis = new LogarithmicAxis(range);
		plot.setRangeAxis(rangeAxis);
		return chart;

	}

	/**
	 * Creates and returns a sample time series chart.
	 * 
	 * @return a sample time series chart.
	 */
	public JFreeChart createTimeSeriesWithMAChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("timeseries.sample3.title");
		final String domain = this.resources.getString("timeseries.sample3.domain");
		final String range = this.resources.getString("timeseries.sample3.range");
		final String subtitleStr = this.resources.getString("timeseries.sample3.subtitle");
		final TimeSeries jpy = DemoDatasetFactory.createJPYTimeSeries();
		final TimeSeries mav = MovingAverage.createMovingAverage(
							jpy, "30 Day Moving Average", 30, 30
							);
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(jpy);
		dataset.addSeries(mav);
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(title, domain, range, dataset,
							true,
							true,
							false);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;

	}

	/**
	 * Displays a vertical bar chart in its own frame.
	 * 
	 * @return a high low chart.
	 */
	public JFreeChart createHighLowChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("timeseries.highlow.title");
		final String domain = this.resources.getString("timeseries.highlow.domain");
		final String range = this.resources.getString("timeseries.highlow.range");
		final String subtitleStr = this.resources.getString("timeseries.highlow.subtitle");
		final HighLowDataset data = DemoDatasetFactory.createHighLowDataset();
		final JFreeChart chart = ChartFactory.createHighLowChart(title, domain, range, data, true);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.magenta));
		return chart;

	}

	/**
	 * Creates a candlestick chart.
	 * 
	 * @return a candlestick chart.
	 */
	public JFreeChart createCandlestickChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("timeseries.candlestick.title");
		final String domain = this.resources.getString("timeseries.candlestick.domain");
		final String range = this.resources.getString("timeseries.candlestick.range");
		final String subtitleStr = this.resources.getString("timeseries.candlestick.subtitle");
		final HighLowDataset data = DemoDatasetFactory.createHighLowDataset();
		final JFreeChart chart = ChartFactory.createCandlestickChart(
							title, domain, range, data, false
							);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.green));
		return chart;

	}

	/**
	 * Creates and returns a sample signal chart.
	 * 
	 * @return a sample chart.
	 */
	public JFreeChart createSignalChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("timeseries.signal.title");
		final String domain = this.resources.getString("timeseries.signal.domain");
		final String range = this.resources.getString("timeseries.signal.range");
		final String subtitleStr = this.resources.getString("timeseries.signal.subtitle");
		final SignalsDataset data = DemoDatasetFactory.createSampleSignalDataset();
		final JFreeChart chart = ChartFactory.createSignalChart(title, domain, range, data, true);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;

	}

	/**
	 * Creates and returns a sample thermometer chart.
	 * 
	 * @return a sample thermometer chart.
	 */
	public JFreeChart createThermometerChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("meter.thermo.title");
		final String subtitleStr = this.resources.getString("meter.thermo.subtitle");
		final String units = this.resources.getString("meter.thermo.units");

		final DefaultValueDataset data = new DefaultValueDataset(new Double(34.0));
		final ThermometerPlot plot = new ThermometerPlot(data);
		plot.setUnits(units);
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;

	}

	/**
	 * Creates and returns a sample meter chart.
	 * 
	 * @return a meter chart.
	 */
	public JFreeChart createMeterChartCircle() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("meter.meter.title");
		final String subtitleStr = this.resources.getString("meter.meter.subtitle");
		// String units = resources.getString("meter.meter.units");
		// DefaultMeterDataset data = DemoDatasetFactory.createMeterDataset();
		final DefaultValueDataset data = new DefaultValueDataset(50.0);
		// data.setUnits(units);
		final MeterPlot plot = new MeterPlot(data);
		plot.setMeterAngle(270);
		plot.setDialShape(DialShape.CIRCLE);
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
							plot, false);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;
	}

	/**
	 * Creates and returns a sample meter chart.
	 * 
	 * @return a meter chart.
	 */
	public JFreeChart createMeterChartPie() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("meter.meter.title");
		final String subtitleStr = this.resources.getString("meter.meter.subtitle");
		// String units = resources.getString("meter.meter.units");
		// DefaultMeterDataset data = DemoDatasetFactory.createMeterDataset();
		final DefaultValueDataset data = new DefaultValueDataset(50.0);
		// data.setUnits(units);
		final MeterPlot plot = new MeterPlot(data);
		plot.setMeterAngle(270);
		plot.setDialShape(DialShape.PIE);
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;
	}

	/**
	 * Creates and returns a sample meter chart.
	 * 
	 * @return the meter chart.
	 */
	public JFreeChart createMeterChartChord() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("meter.meter.title");
		final String subtitleStr = this.resources.getString("meter.meter.subtitle");
		// String units = resources.getString("meter.meter.units");
		// DefaultMeterDataset data = DemoDatasetFactory.createMeterDataset();
		final DefaultValueDataset data = new DefaultValueDataset(45.0);
		// data.setUnits(units);
		final MeterPlot plot = new MeterPlot(data);
		plot.setMeterAngle(270);
		plot.setDialShape(DialShape.CHORD);
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;
	}

	/**
	 * Creates a compass chart.
	 * 
	 * @return a compass chart.
	 */
	public JFreeChart createCompassChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("meter.compass.title");
		final String subtitleStr = this.resources.getString("meter.compass.subtitle");
		final DefaultValueDataset data = new DefaultValueDataset(new Double(45.0));

		final Plot plot = new CompassPlot(data);
		final JFreeChart chart = new JFreeChart(
							title,
							JFreeChart.DEFAULT_TITLE_FONT,
							plot,
							false
							);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;
	}

	/**
	 * Creates and returns a sample wind plot.
	 * 
	 * @return a sample wind plot.
	 */
	public JFreeChart createWindPlot() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("other.wind.title");
		final String domain = this.resources.getString("other.wind.domain");
		final String range = this.resources.getString("other.wind.range");
		final WindDataset data = DemoDatasetFactory.createWindDataset1();
		final JFreeChart chart = ChartFactory.createWindPlot(title, domain, range, data,
							true,
							false,
							false);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.green));
		return chart;

	}

	/**
	 * Creates and returns a sample scatter plot.
	 * 
	 * @return a sample scatter plot.
	 */
	public JFreeChart createScatterPlot() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("other.scatter.title");
		final String domain = this.resources.getString("other.scatter.domain");
		final String range = this.resources.getString("other.scatter.range");
		final XYDataset data = new SampleXYDataset2();
		final JFreeChart chart = ChartFactory.createScatterPlot(
							title,
							domain,
							range,
							data,
							PlotOrientation.VERTICAL,
							true,
							true, // tooltips
				false // urls
				);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.green));

		final XYPlot plot = chart.getXYPlot();
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);
		return chart;

	}

	/**
	 * Creates and returns a sample line chart.
	 * 
	 * @return a line chart.
	 */
	public JFreeChart createLineChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("other.line.title");
		final String domain = this.resources.getString("other.line.domain");
		final String range = this.resources.getString("other.line.range");
		final CategoryDataset data = DemoDatasetFactory.createCategoryDataset();
		final JFreeChart chart = ChartFactory.createLineChart(title, domain, range, data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false);

		// then customise it a little...
		chart.setBackgroundImage(JFreeChart.INFO.getLogo());
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.green));

		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.65f);
		return chart;
	}

	/**
	 * Creates and returns a sample vertical XY bar chart.
	 * 
	 * @return a sample vertical XY bar chart.
	 */
	public JFreeChart createVerticalXYBarChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("other.xybar.title");
		final String domain = this.resources.getString("other.xybar.domain");
		final String range = this.resources.getString("other.xybar.range");
		final TimeSeriesCollection data = DemoDatasetFactory.createTimeSeriesCollection1();
		data.setDomainIsPointsInTime(false);
		final JFreeChart chart = ChartFactory.createXYBarChart(
							title,
							domain,
							true,
							range,
							data,
							PlotOrientation.VERTICAL,
							true,
							false,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.blue));

		final XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
		return chart;
	}

	/**
	 * Creates and returns a sample XY chart with null data.
	 * 
	 * @return a chart.
	 */
	public JFreeChart createNullXYPlot() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("test.null.title");
		final String domain = this.resources.getString("test.null.domain");
		final String range = this.resources.getString("test.null.range");
		final XYDataset data = null;
		final JFreeChart chart = ChartFactory.createXYLineChart(
							title, domain, range, data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.red));
		return chart;

	}

	/**
	 * Creates a sample XY plot with an empty dataset.
	 * 
	 * @return a sample XY plot with an empty dataset.
	 */
	public JFreeChart createXYPlotZeroData() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("test.zero.title");
		final String domain = this.resources.getString("test.zero.domain");
		final String range = this.resources.getString("test.zero.range");
		final XYDataset data = new EmptyXYDataset();
		final JFreeChart chart = ChartFactory.createXYLineChart(
							title, domain, range, data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.red));
		return chart;
	}

	/**
	 * Creates and returns a sample time series chart that will be displayed in a scroll pane.
	 * 
	 * @return a sample time series chart.
	 */
	public JFreeChart createTimeSeriesChartInScrollPane() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("test.scroll.title");
		final String domain = this.resources.getString("test.scroll.domain");
		final String range = this.resources.getString("test.scroll.range");
		final String subtitleStr = this.resources.getString("test.scroll.subtitle");
		final XYDataset data = DemoDatasetFactory.createTimeSeriesCollection2();
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(title, domain, range, data,
							true,
							true,
							false);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.gray));
		return chart;

	}

	/**
	 * Creates and returns a sample bar chart with just one series.
	 * 
	 * @return a sample bar chart.
	 */
	public JFreeChart createSingleSeriesBarChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("test.single.title");
		final String domain = this.resources.getString("test.single.domain");
		final String range = this.resources.getString("test.single.range");
		final String subtitle1Str = this.resources.getString("test.single.subtitle1");
		final String subtitle2Str = this.resources.getString("test.single.subtitle2");

		final CategoryDataset data = DemoDatasetFactory.createSingleSeriesCategoryDataset();

		final JFreeChart chart = ChartFactory.createBarChart(title, domain, range, data,
							PlotOrientation.HORIZONTAL,
							true,
							true,
							false);
		chart.addSubtitle(new TextTitle(subtitle1Str));
		chart.addSubtitle(new TextTitle(subtitle2Str));
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.red));
		return chart;

	}

	/**
	 * Displays an XY chart that is periodically updated by a background thread. This is to
	 * demonstrate the event notification system that automatically updates charts as required.
	 * 
	 * @return a chart.
	 */
	public JFreeChart createDynamicXYChart() {

		final String title = this.resources.getString("test.dynamic.title");
		final String domain = this.resources.getString("test.dynamic.domain");
		final String range = this.resources.getString("test.dynamic.range");

		final SampleXYDataset data = new SampleXYDataset();
		final JFreeChart chart = ChartFactory.createXYLineChart(
							title, domain, range, data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);
		final SampleXYDatasetThread update = new SampleXYDatasetThread(data);

		final Thread thread = new Thread(update);
		thread.start();

		return chart;

	}

	/**
	 * Creates and returns a sample overlaid chart.
	 * <P>
	 * Note: with the introduction of multiple secondary datasets in JFreeChart version 0.9.10, the overlaid chart facility has been removed. You can achieve the
	 * same results using a regular XYPlot with multiple datasets.
	 * 
	 * @return an overlaid chart.
	 */
	public JFreeChart createOverlaidChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("combined.overlaid.title");
		final String subtitleStr = this.resources.getString("combined.overlaid.subtitle");
		final String domainAxisLabel = this.resources.getString("combined.overlaid.domain");
		final String rangeAxisLabel = this.resources.getString("combined.overlaid.range");

		// create high-low and moving average dataset
		final HighLowDataset highLowData = DemoDatasetFactory.createHighLowDataset();

		// make an overlaid plot
		final ValueAxis domainAxis = new DateAxis(domainAxisLabel);
		final NumberAxis rangeAxis = new NumberAxis(rangeAxisLabel);
		rangeAxis.setAutoRangeIncludesZero(false);
		final XYItemRenderer renderer1 = new HighLowRenderer();
		renderer1.setToolTipGenerator(new HighLowItemLabelGenerator());

		final XYPlot plot = new XYPlot(highLowData, domainAxis, rangeAxis, renderer1);

		// overlay a moving average dataset
		final XYDataset maData = MovingAverage.createMovingAverage(
							highLowData,
							" (Moving Average)",
							5 * 24 * 60 * 60 * 1000L,
							5 * 24 * 60 * 60 * 1000L
							);
		plot.setDataset(1, maData);
		final XYItemRenderer renderer2 = new StandardXYItemRenderer();
		renderer2.setToolTipGenerator(
							new StandardXYToolTipGenerator(
												StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
												new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0,000.0")
							)
							);
		plot.setRenderer(1, renderer2);

		// make the top level JFreeChart object
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		return chart;

	}

	/**
	 * Creates a horizontally combined chart.
	 * 
	 * @return a horizontally combined chart.
	 */
	public JFreeChart createHorizontallyCombinedChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("combined.horizontal.title");
		final String subtitleStr = this.resources.getString("combined.horizontal.subtitle");
		final String[] domains = this.resources.getStringArray("combined.horizontal.domains");
		final String rangeAxisLabel = this.resources.getString("combined.horizontal.range");

		final TimeSeriesCollection dataset0 = new TimeSeriesCollection();
		final TimeSeries eur = DemoDatasetFactory.createEURTimeSeries();
		dataset0.addSeries(eur);

		final TimeSeriesCollection dataset1 = new TimeSeriesCollection();
		final TimeSeries mav = MovingAverage.createMovingAverage(
							eur, "EUR/GBP (30 Day MA)", 30, 30
							);
		dataset1.addSeries(eur);
		dataset1.addSeries(mav);

		final TimeSeriesCollection dataset2 = new TimeSeriesCollection();
		dataset2.addSeries(eur);

		// make a combined range plot
		final NumberAxis valueAxis = new NumberAxis(rangeAxisLabel);
		valueAxis.setAutoRangeIncludesZero(false); // override default
		final CombinedRangeXYPlot parent = new CombinedRangeXYPlot(valueAxis);
		parent.setRenderer(new StandardXYItemRenderer());

		// add subplots
		final int[] weight = { 1, 1, 1 }; // controls space assigned to each subplot

		// add subplot 1...
		final XYPlot subplot1 = new XYPlot(dataset0, new DateAxis(domains[0]), null,
							new StandardXYItemRenderer());
		parent.add(subplot1, weight[0]);

		// add subplot 2...
		final XYPlot subplot2 = new XYPlot(dataset1, new DateAxis(domains[1]), null,
							new StandardXYItemRenderer());
		parent.add(subplot2, weight[1]);

		// add subplot 3...
		final XYPlot subplot3 = new XYPlot(dataset2, new DateAxis(domains[2]),
							null, new XYBarRenderer(0.20));
		parent.add(subplot3, weight[2]);

		// now make the top level JFreeChart
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, parent, true);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;

	}

	/**
	 * Creates and returns a sample vertically combined chart.
	 * 
	 * @return a sample vertically combined chart.
	 */
	public JFreeChart createVerticallyCombinedChart() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("combined.vertical.title");
		final String subtitleStr = this.resources.getString("combined.vertical.subtitle");
		final String domain = this.resources.getString("combined.vertical.domain");
		final String[] ranges = this.resources.getStringArray("combined.vertical.ranges");

		final TimeSeriesCollection dataset0 = new TimeSeriesCollection();
		final TimeSeries eur = DemoDatasetFactory.createEURTimeSeries();
		dataset0.addSeries(eur);

		final TimeSeriesCollection dataset1 = new TimeSeriesCollection();
		final TimeSeries jpy = DemoDatasetFactory.createJPYTimeSeries();
		final TimeSeries mav = MovingAverage.createMovingAverage(
							jpy, "JPY/GBP (30 Day MA)", 30, 30
							);
		dataset1.addSeries(jpy);
		dataset1.addSeries(mav);

		final XYDataset dataset2 = DemoDatasetFactory.createHighLowDataset();

		final TimeSeriesCollection dataset3 = new TimeSeriesCollection();
		dataset3.addSeries(eur);

		// make one shared horizontal axis
		final ValueAxis timeAxis = new DateAxis(domain);

		// make a vertically CombinedPlot that will contain the sub-plots
		final CombinedDomainXYPlot multiPlot = new CombinedDomainXYPlot(timeAxis);

		final int[] weight = { 1, 1, 1, 1 }; // control vertical space allocated to each sub-plot

		// add subplot1...
		final XYPlot subplot1 = new XYPlot(dataset0, null, new NumberAxis(ranges[0]),
							new StandardXYItemRenderer());
		final NumberAxis range1 = (NumberAxis) subplot1.getRangeAxis();
		range1.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 7));
		range1.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		range1.setAutoRangeIncludesZero(false);
		multiPlot.add(subplot1, weight[0]);

		// add subplot2...
		final XYPlot subplot2 = new XYPlot(dataset1, null, new NumberAxis(ranges[1]),
							new StandardXYItemRenderer());
		final NumberAxis range2 = (NumberAxis) subplot2.getRangeAxis();
		range2.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 7));
		range2.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		range2.setAutoRangeIncludesZero(false);
		multiPlot.add(subplot2, weight[1]);

		// add subplot3...
		final XYPlot subplot3 = new XYPlot(dataset2, null, new NumberAxis(ranges[2]), null);
		final XYItemRenderer renderer3 = new HighLowRenderer();
		subplot3.setRenderer(renderer3);
		final NumberAxis range3 = (NumberAxis) subplot3.getRangeAxis();
		range3.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 7));
		range3.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		range3.setAutoRangeIncludesZero(false);
		multiPlot.add(subplot3, weight[2]);

		// add subplot4...
		final XYPlot subplot4 = new XYPlot(dataset3, null, new NumberAxis(ranges[3]), null);
		final XYItemRenderer renderer4 = new XYBarRenderer();
		subplot4.setRenderer(renderer4);
		final NumberAxis range4 = (NumberAxis) subplot4.getRangeAxis();
		range4.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 7));
		range4.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		range4.setAutoRangeIncludesZero(false);
		multiPlot.add(subplot4, weight[3]);

		// now make the top level JFreeChart that contains the CombinedPlot
		final JFreeChart chart = new JFreeChart(
							title, JFreeChart.DEFAULT_TITLE_FONT, multiPlot, true
							);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;

	}

	/**
	 * Creates a combined and overlaid chart.
	 * <p>
	 * Note: from version 0.9.10, the overlaid chart is no longer supported (you can achieve the same result using a regular XYPlot with multiple datasets and
	 * renderers).
	 * 
	 * @return a combined and overlaid chart.
	 */
	public JFreeChart createCombinedAndOverlaidChart1() {

		// create a default chart based on some sample data...
		final String title = this.resources.getString("combined.combined-overlaid.title");
		final String subtitleStr = this.resources.getString("combined.combined-overlaid.subtitle");
		final String domain = this.resources.getString("combined.combined-overlaid.domain");
		final String[] ranges = this.resources.getStringArray("combined.combined-overlaid.ranges");

		final TimeSeries jpy = DemoDatasetFactory.createJPYTimeSeries();
		final TimeSeries mav = MovingAverage.createMovingAverage(
							jpy, "30 Day Moving Average", 30, 30
							);

		final TimeSeriesCollection dataset0 = new TimeSeriesCollection();
		dataset0.addSeries(jpy);

		final TimeSeriesCollection dataset1 = new TimeSeriesCollection();
		dataset1.addSeries(jpy);
		dataset1.addSeries(mav);

		final HighLowDataset highLowDataset = DemoDatasetFactory.createHighLowDataset();
		final XYDataset highLowDatasetMA = MovingAverage.createMovingAverage(
							highLowDataset,
							" (MA)",
							5 * 24 * 60 * 60 * 1000L,
							5 * 24 * 60 * 60 * 1000L
							);

		// make one vertical axis for each (vertical) chart
		final NumberAxis[] valueAxis = new NumberAxis[3];
		for (int i = 0; i < valueAxis.length; i++) {
			valueAxis[i] = new NumberAxis(ranges[i]);
			if (i <= 1) {
				valueAxis[i].setAutoRangeIncludesZero(false); // override default
			}
		}

		// create CombinedPlot...
		final CombinedDomainXYPlot parent = new CombinedDomainXYPlot(new DateAxis(domain));

		final int[] weight = { 1, 2, 2 };

		// add subplot1...
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final XYPlot subplot1 = new XYPlot(dataset0, null, new NumberAxis(ranges[0]), renderer1);
		final NumberAxis axis1 = (NumberAxis) subplot1.getRangeAxis();
		axis1.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 7));
		axis1.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		axis1.setAutoRangeIncludesZero(false);
		parent.add(subplot1, weight[0]);

		// add subplot2 (an overlaid plot)...
		final XYPlot subplot2 = new XYPlot(dataset0, null, new NumberAxis(ranges[1]),
							new StandardXYItemRenderer());
		final NumberAxis axis2 = (NumberAxis) subplot2.getRangeAxis();
		axis2.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 7));
		axis2.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		axis2.setAutoRangeIncludesZero(false);
		subplot2.setDataset(1, dataset1);
		subplot2.setRenderer(1, new StandardXYItemRenderer());

		parent.add(subplot2, weight[1]);

		// add subplot3 (an overlaid plot)...
		final XYItemRenderer renderer3 = new HighLowRenderer();
		final XYPlot subplot3 = new XYPlot(
							highLowDataset, null, new NumberAxis(ranges[2]), renderer3
							);
		final NumberAxis axis3 = (NumberAxis) subplot3.getRangeAxis();
		axis3.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 7));
		axis3.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		axis3.setAutoRangeIncludesZero(false);
		subplot3.setDataset(1, highLowDatasetMA);
		subplot3.setRenderer(1, new StandardXYItemRenderer());

		parent.add(subplot3, weight[2]);

		// now create the master JFreeChart object
		final JFreeChart chart = new JFreeChart(
							title,
							new Font("SansSerif", Font.BOLD, 12),
							parent,
							true
							);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 10));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));
		return chart;

	}

	/**
	 * Displays an XY chart that is periodically updated by a background thread. This is to
	 * demonstrate the event notification system that automatically updates charts as required.
	 * 
	 * @return a chart.
	 */
	public JFreeChart createCombinedAndOverlaidDynamicXYChart() {

		// chart title and axis labels...
		final String title = this.resources.getString("combined.dynamic.title");
		final String subtitleStr = this.resources.getString("combined.dynamic.subtitle");
		final String domainAxisLabel = this.resources.getString("combined.dynamic.domain");
		final String[] ranges = this.resources.getStringArray("combined.dynamic.ranges");

		// setup sample base 2-series dataset
		final SampleXYDataset data = new SampleXYDataset();

		// create some SubSeriesDatasets and CombinedDatasets to test events
		final XYDataset series0 = new SubSeriesDataset(data, 0);
		final XYDataset series1 = new SubSeriesDataset(data, 1);

		final CombinedDataset combinedData = new CombinedDataset();
		combinedData.add(series0);
		combinedData.add(series1);

		// create common time axis
		final NumberAxis timeAxis = new NumberAxis(domainAxisLabel);
		timeAxis.setTickMarksVisible(true);
		timeAxis.setAutoRangeIncludesZero(false);

		// make one vertical axis for each (vertical) chart
		final NumberAxis[] valueAxis = new NumberAxis[4];
		for (int i = 0; i < valueAxis.length; i++) {
			valueAxis[i] = new NumberAxis(ranges[i]);
			valueAxis[i].setAutoRangeIncludesZero(false);
		}

		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(timeAxis);

		// add subplot1...
		final XYItemRenderer renderer0 = new StandardXYItemRenderer();
		final XYPlot subplot0 = new XYPlot(series0, null, valueAxis[0], renderer0);
		plot.add(subplot0, 1);

		// add subplot2...
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final XYPlot subplot1 = new XYPlot(series1, null, valueAxis[1], renderer1);
		plot.add(subplot1, 1);

		// add subplot3...
		final XYPlot subplot2 = new XYPlot(
							series0, null, valueAxis[2], new StandardXYItemRenderer()
							);
		subplot2.setDataset(1, series1);
		subplot2.setRenderer(1, new StandardXYItemRenderer());
		plot.add(subplot2, 1);

		// add subplot4...
		final XYItemRenderer renderer3 = new StandardXYItemRenderer();
		final XYPlot subplot3 = new XYPlot(data, null, valueAxis[3], renderer3);
		plot.add(subplot3, 1);

		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

		// then customise it a little...
		final TextTitle subtitle = new TextTitle(subtitleStr, new Font("SansSerif", Font.BOLD, 12));
		chart.addSubtitle(subtitle);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.cyan));

		// setup thread to update base Dataset
		final SampleXYDatasetThread update = new SampleXYDatasetThread(data);
		final Thread thread = new Thread(update);
		thread.start();

		return chart;

	}

	/**
	 * Creates a gantt chart.
	 * 
	 * @return a gantt chart.
	 */
	public JFreeChart createGanttChart() {

		final String title = this.resources.getString("gantt.task.title");
		final String domain = this.resources.getString("gantt.task.domain");
		final String range = this.resources.getString("gantt.task.range");

		final IntervalCategoryDataset data = createGanttDataset1();

		final JFreeChart chart = ChartFactory.createGanttChart(
							title, domain, range, data,
							true,
							true,
							false
							);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.blue));
		return chart;

	}

	/**
	 * Creates a sample dataset for a Gantt chart.
	 * 
	 * @return The dataset.
	 * @deprecated Moved to the demo applications that require it.
	 */
	@Deprecated
	private static IntervalCategoryDataset createGanttDataset1() {

		final TaskSeries s1 = new TaskSeries("Scheduled");
		s1.add(new Task("Write Proposal",
							new SimpleTimePeriod(date(1, Calendar.APRIL, 2001),
												date(5, Calendar.APRIL, 2001))));
		s1.add(new Task("Obtain Approval",
							new SimpleTimePeriod(date(9, Calendar.APRIL, 2001),
												date(9, Calendar.APRIL, 2001))));
		s1.add(new Task("Requirements Analysis",
							new SimpleTimePeriod(date(10, Calendar.APRIL, 2001),
												date(5, Calendar.MAY, 2001))));
		s1.add(new Task("Design Phase",
							new SimpleTimePeriod(date(6, Calendar.MAY, 2001),
												date(30, Calendar.MAY, 2001))));
		s1.add(new Task("Design Signoff",
							new SimpleTimePeriod(date(2, Calendar.JUNE, 2001),
												date(2, Calendar.JUNE, 2001))));
		s1.add(new Task("Alpha Implementation",
							new SimpleTimePeriod(date(3, Calendar.JUNE, 2001),
												date(31, Calendar.JULY, 2001))));
		s1.add(new Task("Design Review",
							new SimpleTimePeriod(date(1, Calendar.AUGUST, 2001),
												date(8, Calendar.AUGUST, 2001))));
		s1.add(new Task("Revised Design Signoff",
							new SimpleTimePeriod(date(10, Calendar.AUGUST, 2001),
												date(10, Calendar.AUGUST, 2001))));
		s1.add(new Task("Beta Implementation",
							new SimpleTimePeriod(date(12, Calendar.AUGUST, 2001),
												date(12, Calendar.SEPTEMBER, 2001))));
		s1.add(new Task("Testing",
							new SimpleTimePeriod(date(13, Calendar.SEPTEMBER, 2001),
												date(31, Calendar.OCTOBER, 2001))));
		s1.add(new Task("Final Implementation",
							new SimpleTimePeriod(date(1, Calendar.NOVEMBER, 2001),
												date(15, Calendar.NOVEMBER, 2001))));
		s1.add(new Task("Signoff",
							new SimpleTimePeriod(date(28, Calendar.NOVEMBER, 2001),
												date(30, Calendar.NOVEMBER, 2001))));

		final TaskSeries s2 = new TaskSeries("Actual");
		s2.add(new Task("Write Proposal",
							new SimpleTimePeriod(date(1, Calendar.APRIL, 2001),
												date(5, Calendar.APRIL, 2001))));
		s2.add(new Task("Obtain Approval",
							new SimpleTimePeriod(date(9, Calendar.APRIL, 2001),
												date(9, Calendar.APRIL, 2001))));
		s2.add(new Task("Requirements Analysis",
							new SimpleTimePeriod(date(10, Calendar.APRIL, 2001),
												date(15, Calendar.MAY, 2001))));
		s2.add(new Task("Design Phase",
							new SimpleTimePeriod(date(15, Calendar.MAY, 2001),
												date(17, Calendar.JUNE, 2001))));
		s2.add(new Task("Design Signoff",
							new SimpleTimePeriod(date(30, Calendar.JUNE, 2001),
												date(30, Calendar.JUNE, 2001))));
		s2.add(new Task("Alpha Implementation",
							new SimpleTimePeriod(date(1, Calendar.JULY, 2001),
												date(12, Calendar.SEPTEMBER, 2001))));
		s2.add(new Task("Design Review",
							new SimpleTimePeriod(date(12, Calendar.SEPTEMBER, 2001),
												date(22, Calendar.SEPTEMBER, 2001))));
		s2.add(new Task("Revised Design Signoff",
							new SimpleTimePeriod(date(25, Calendar.SEPTEMBER, 2001),
												date(27, Calendar.SEPTEMBER, 2001))));
		s2.add(new Task("Beta Implementation",
							new SimpleTimePeriod(date(27, Calendar.SEPTEMBER, 2001),
												date(30, Calendar.OCTOBER, 2001))));
		s2.add(new Task("Testing",
							new SimpleTimePeriod(date(31, Calendar.OCTOBER, 2001),
												date(17, Calendar.NOVEMBER, 2001))));
		s2.add(new Task("Final Implementation",
							new SimpleTimePeriod(date(18, Calendar.NOVEMBER, 2001),
												date(5, Calendar.DECEMBER, 2001))));
		s2.add(new Task("Signoff",
							new SimpleTimePeriod(date(10, Calendar.DECEMBER, 2001),
												date(11, Calendar.DECEMBER, 2001))));

		final TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s1);
		collection.add(s2);

		return collection;
	}

	/**
	 * Utility method for creating <code>Date</code> objects.
	 * 
	 * @param day
	 *           the date.
	 * @param month
	 *           the month.
	 * @param year
	 *           the year.
	 * @return a date.
	 */
	private static Date date(final int day, final int month, final int year) {

		final Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		final Date result = calendar.getTime();
		return result;

	}

	/**
	 * Creates a basic wafermap chart with a random dataset
	 * 
	 * @return a wafermap chart
	 */
	public JFreeChart createWaferMapChart() {
		final WaferMapDataset dataset = DemoDatasetFactory.createRandomWaferMapDataset(5);
		final JFreeChart chart = ChartFactory.createWaferMapChart(
							"Wafer Map Demo", // title
				dataset, // wafermapdataset
				PlotOrientation.VERTICAL, // vertical = notchdown
				true, // legend
				false, // tooltips
				false
							);

		final Legend legend = chart.getLegend();
		legend.setAnchor(Legend.EAST);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		final TextTitle copyright = new TextTitle(
							"JFreeChart WaferMapPlot", new Font("SansSerif", Font.PLAIN, 9)
							);
		copyright.setPosition(RectangleEdge.BOTTOM);
		copyright.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		chart.addSubtitle(copyright);

		return chart;
	}

	/**
	 * Creates a basic wafermap chart with a random dataset
	 * 
	 * @return a wafermap chart
	 */
	public JFreeChart createWaferMapChartValueIndexed() {
		final WaferMapDataset dataset = DemoDatasetFactory.createRandomWaferMapDataset(500);
		final JFreeChart chart = ChartFactory.createWaferMapChart(
							"Wafer Map Demo - Value Indexed", // title
				dataset, // wafermapdataset
				PlotOrientation.VERTICAL, // vertical = notchdown
				true, // legend
				false, // tooltips
				false
							);

		final Legend legend = chart.getLegend();
		legend.setAnchor(Legend.EAST);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		final TextTitle copyright = new TextTitle(
							"JFreeChart WaferMapPlot", new Font("SansSerif", Font.PLAIN, 9)
							);
		copyright.setPosition(RectangleEdge.BOTTOM);
		copyright.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		chart.addSubtitle(copyright);

		return chart;
	}

	/**
	 * Creates a basic wafermap chart with a random dataset
	 * 
	 * @return a wafermap chart
	 */
	public JFreeChart createWaferMapChartPositionIndexed() {
		final WaferMapDataset dataset = DemoDatasetFactory.createRandomWaferMapDataset(500);
		final WaferMapPlot plot = new WaferMapPlot(dataset);
		final WaferMapRenderer renderer = new WaferMapRenderer(35, WaferMapRenderer.POSITION_INDEX);
		plot.setRenderer(renderer);

		final JFreeChart chart = new JFreeChart(
							"Wafer Map Demo - Position Indexed",
							JFreeChart.DEFAULT_TITLE_FONT,
							plot,
							true
							);

		final Legend legend = chart.getLegend();
		legend.setAnchor(Legend.EAST);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.blue));

		final TextTitle copyright = new TextTitle(
							"JFreeChart WaferMapPlot", new Font("SansSerif", Font.PLAIN, 9)
							);
		copyright.setPosition(RectangleEdge.BOTTOM);
		copyright.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		chart.addSubtitle(copyright);

		return chart;
	}

}
