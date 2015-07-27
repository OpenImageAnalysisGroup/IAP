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
 * ---------------------
 * ContourPlotDemo2.java
 * ---------------------
 * (C) Copyright 2003, 2004, by David M. O'Donnell and Contributors.
 * Original Author: David M. O'Donnell;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: ContourPlotDemo2.java,v 1.1 2011-01-31 09:01:53 klukas Exp $
 * Changes
 * -------
 * 22-Apr-2003 : Added standard header (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.util.Date;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ClipPath;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ColorBar;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ContourPlot;
import org.jfree.data.ContourDataset;
import org.jfree.data.DefaultContourDataset;
import org.jfree.data.NonGridContourDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration application to illustrate ContourPlot.
 * Command line options exist to control different plot properties
 * such as colorbar orientation, etc. List of options are available
 * by launching with the -? option, e.g., ContourPlotDemo -?
 * 
 * @author DMO
 */
public class ContourPlotDemo2 extends ApplicationFrame {

	/** The x axis. */
	private ValueAxis xAxis = null;

	/** The y axis. */
	private NumberAxis yAxis = null;

	/** The z axis. */
	private ColorBar zColorBar = null;

	/** A flag controlling the orientation of the z axis. */
	// private static boolean zIsVertical = false;

	/** A flag indicating whether or not the x-values are dates. */
	private static boolean xIsDate = false;

	/** ??. */
	private static boolean asPoints = false;

	/** Logarithmic x-axis? */
	private static boolean xIsLog = false;

	/** Logarithmic y axis? */
	private static boolean yIsLog = false;

	/** Logarithmic z axis? */
	private static boolean zIsLog = false;

	/** Inverted x axis? */
	private static boolean xIsInverted = true;

	/** Inverted y axis? */
	private static boolean yIsInverted = false;

	/** Inverted z axis? */
	private static boolean zIsInverted = false;

	/** Annotate? */
	private static boolean annotate = false;

	/** Number of x intervals. */
	private static int numX = 10;

	/** Number of y intervals. */
	private static int numY = 20;

	/** The plot ratio. */
	private static double ratio = 0.0;

	/** Temp data storage. */
	private double[] tmpDoubleY = null;

	/** Temp data storage. */
	private double[] tmpDoubleX = null;

	/** Temp data storage. */
	private double[] tmpDoubleZ = null;

	/** X outline. */
	private double[] xOutline = null;

	/** Y outline. */
	private double[] yOutline = null;

	/** Draw the outline? */
	static boolean drawOutline = false;

	/** Fill the outline? */
	static boolean fillOutline = false;

	/** ??. */
	static int power = 4;

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public ContourPlotDemo2(final String title) {

		super(title);

		final JFreeChart chart = createContourPlot();
		final ChartPanel panel = new ChartPanel(chart, true, true, true, true, true);
		panel.setPreferredSize(new java.awt.Dimension(1000, 800));
		panel.setMaximumDrawHeight(100000); // stop chartpanel from scaling
		panel.setMaximumDrawWidth(100000); // stop chartpanel from scaling
		panel.setHorizontalZoom(true);
		panel.setVerticalZoom(true);
		panel.setFillZoomRectangle(true);
		setContentPane(panel);

	}

	/**
	 * Creates a ContourPlot chart.
	 * 
	 * @return the ContourPlot chart.
	 */
	private JFreeChart createContourPlot() {

		final String title = "Contour Plot";
		final String xAxisLabel = "X Values";
		final String yAxisLabel = "Y Values";
		final String zAxisLabel = "Color Values";

		if (xIsDate) {
			this.xAxis = new DateAxis(xAxisLabel);
			xIsLog = false; // force axis to be linear when displaying dates
		} else {
			if (xIsLog) {
				this.xAxis = new LogarithmicAxis(xAxisLabel);
			} else {
				this.xAxis = new NumberAxis(xAxisLabel);
			}
		}

		if (yIsLog) {
			this.yAxis = new LogarithmicAxis(yAxisLabel);
		} else {
			this.yAxis = new NumberAxis(yAxisLabel);
		}

		if (zIsLog) {
			this.zColorBar = new ColorBar(zAxisLabel);
		} else {
			this.zColorBar = new ColorBar(zAxisLabel);
		}

		if (this.xAxis instanceof NumberAxis) {
			((NumberAxis) this.xAxis).setAutoRangeIncludesZero(false);
			((NumberAxis) this.xAxis).setInverted(xIsInverted);
		}

		this.yAxis.setAutoRangeIncludesZero(false);

		this.yAxis.setInverted(yIsInverted);

		if (!xIsDate) {
			((NumberAxis) this.xAxis).setLowerMargin(0.0);
			((NumberAxis) this.xAxis).setUpperMargin(0.0);
		}

		this.yAxis.setLowerMargin(0.0);
		this.yAxis.setUpperMargin(0.0);

		if (!xIsDate) {
			this.xAxis.setRange(10.5, 15.0);
		}
		this.yAxis.setRange(3.5, 7.0);

		this.zColorBar.getAxis().setInverted(zIsInverted);
		this.zColorBar.getAxis().setTickMarksVisible(true);

		final ContourDataset data = createDataset();

		final ContourPlot plot = new ContourPlot(data, this.xAxis, this.yAxis, this.zColorBar);

		if (xIsDate) {
			ratio = Math.abs(ratio); // don't use plot units for ratios when x axis is date
		}

		if (asPoints) {
			plot.setRenderAsPoints(true);
		}
		plot.setDataAreaRatio(ratio);

		if (annotate) {
			if (asPoints) {
				final Number[] xValues = data.getXValues();
				final Number[] yValues = data.getYValues();
				// Number[] zValues = data.getZValues();

				final Font font = new Font("SansSerif", Font.PLAIN, 20);

				for (int i = 0; i < xValues.length; i++) {
					final XYTextAnnotation xyAnn = new XYTextAnnotation(Integer.toString(i),
																xValues[i].doubleValue(), yValues[i].doubleValue());
					xyAnn.setFont(font);
					plot.addAnnotation(xyAnn);
				}
			} else {
				final Font font = new Font("SansSerif", Font.PLAIN, 20);

				for (int i = 0; i < this.tmpDoubleX.length; i++) {
					final XYTextAnnotation xyAnn = new XYTextAnnotation(Integer.toString(i),
										this.tmpDoubleX[i], this.tmpDoubleY[i]);
					xyAnn.setFont(font);
					plot.addAnnotation(xyAnn);
				}
			}

		}

		if (fillOutline || drawOutline) {
			initShoreline();
			plot.setClipPath(
								new ClipPath(this.xOutline, this.yOutline, true, fillOutline, drawOutline)
								);
		}

		final JFreeChart chart = new JFreeChart(title, null, plot, false);

		// then customise it a little...
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.green));

		return chart;

	}

	/**
	 * Creates a ContourDataset.
	 * 
	 * @return ContourDataset.
	 */
	private ContourDataset createDataset() {
		initData();

		final Double[] oDoubleX = (Double[]) DefaultContourDataset.formObjectArray(this.tmpDoubleX);
		final Double[] oDoubleY = (Double[]) DefaultContourDataset.formObjectArray(this.tmpDoubleY);
		final Double[] oDoubleZ = (Double[]) DefaultContourDataset.formObjectArray(this.tmpDoubleZ);

		final Date[] tmpDateX = new Date[this.tmpDoubleX.length];
		for (int i = 0; i < this.tmpDoubleX.length; i++) {
			tmpDateX[i] = new Date((long) (1000.0 * this.tmpDoubleX[i]));
		}

		ContourDataset data = null;

		if (xIsDate) {
			if (asPoints) {
				data = new DefaultContourDataset("Contouring", tmpDateX, oDoubleY, oDoubleZ);
			} else {
				data = new NonGridContourDataset("Contouring", tmpDateX, oDoubleY, oDoubleZ);
			}
		} else
			if (!asPoints) {
				data = new NonGridContourDataset("Contouring", oDoubleX, oDoubleY, oDoubleZ,
															numX, numY, power);
			} else {
				data = new DefaultContourDataset("Contouring", oDoubleX, oDoubleY, oDoubleZ);
			}
		return data;

	}

	/**
	 * Sets options passed via the command line
	 * 
	 * @param args
	 *           the arguments.
	 * @return Flag indicating whether program should continue.
	 */
	protected static boolean processArgs(final String[] args) {
		final String[] options = {
							"-?", "-date", "-vertical", "-points", "-outline", "-filled", "-ratio:",
							"-numX:", "-numY:", "-power:", "-annotate"
			};

		for (int i = 0; i < args.length; i++) {
			boolean foundOption = false;
			for (int j = 0; j < options.length; j++) {
				if (args[i].startsWith(options[j])) {
					foundOption = true;
					int index = 0;
					String tmpStr = null;
					switch (j) {
						case 0: // -?
							usage();
							return false;
						case 1:
							xIsDate = true;
							break;
						case 2:
							// zIsVertical = true;
							break;
						case 3:
							asPoints = true;
							break;
						case 4:
							drawOutline = true;
							break;
						case 5:
							fillOutline = true;
							break;
						case 6:
							index = args[i].indexOf(':');
							tmpStr = args[i].substring(index + 1);
							ratio = Double.parseDouble(tmpStr);
							break;
						case 7:
							index = args[i].indexOf(':');
							tmpStr = args[i].substring(index + 1);
							numX = Integer.parseInt(tmpStr);
							break;
						case 8:
							index = args[i].indexOf(':');
							tmpStr = args[i].substring(index + 1);
							numY = Integer.parseInt(tmpStr);
							break;
						case 9:
							index = args[i].indexOf(':');
							tmpStr = args[i].substring(index + 1);
							power = Integer.parseInt(tmpStr);
							break;
						case 10:
							annotate = true;
							break;
						default:
							System.out.println("Only 11 options available, update options array");
					}
				}
			}
			if (!foundOption) {
				System.out.println("Unknown option: " + args[i]);
				usage();
				return false;
			}
		}

		return true; // continue running application
	}

	/**
	 * Prints usage options.
	 */
	public static void usage() {
		System.out.println("Usage:");
		System.out.println("ContourPlotDemo2 -? -date -vertical -points -outline -filled "
									+ "-ratio:value -numX:value -numY:value");
		System.out.println("Where:");
		System.out.println("-? displays usage and exits");
		System.out.println("-date the X axis will be a date");
		System.out.println("-vertical the colorbar will be drawn vertically");
		System.out.println("-points demos plotting data as point (not grid)");
		System.out.println("-outline draws shoreline outline and clips dataArea");
		System.out.println("-filled fills shoreline and clips dataArea");
		System.out.println("-ratio forces plot to maintain aspect ratio (Y/X) indicated by value");
		System.out.println("       positive values are in pixels, while negative is in plot units");
		System.out.println("-numX number of values to generate along the X axis");
		System.out.println("-numY number of values to generate along the Y axis");
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           command line options, launch ContourDemoPlot -? for listing of options.
	 */
	public static void main(final String[] args) {

		if (!processArgs(args)) {
			System.exit(1);
		}
		final ContourPlotDemo2 demo = new ContourPlotDemo2("ContourPlot Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

	/**
	 * Initialise data.
	 */
	private void initData() {

		final double[] tmpDoubleYY = {
							6.782, 6.79, 6.882, 6.289, 6.339,
							6.426, 6.584, 5.532, 5.788, 5.922, 6.053, 4.008, 4.185, 4.456, 4.801, 4.779,
							4.572, 5.202, 5.613, 5.893
			}; // 3.5,7}; // add values to fill entire lake surface

		final double[] tmpDoubleXX = {
							14.508, 14.413, 14.329, 14.512, 14.284, 14.085, 13.793,
							13.603, 13.492, 13.229, 12.956, 11.087, 11.062, 10.937, 11.169, 11.837, 12.182,
							12.802, 12.782, 12.687
			}; // 10.5,15}; // add values to fill entire lake surface

		final double[] tmpDoubleZZ = {
							2.03, 1.23, 0.86, 3.99, 2.38, 3, 3.08, 6.63, 6.84, 7.38,
							6.99, 10.4, 11.11, 10.97, 11.22, 11.25, 10.68, 7.93, 8.17, 7.4
			}; // 12.0, 0.0}; // add values to fill entire lake surface

		this.tmpDoubleY = new double[tmpDoubleYY.length];
		this.tmpDoubleX = new double[tmpDoubleXX.length];
		this.tmpDoubleZ = new double[tmpDoubleZZ.length];

		for (int i = 0; i < this.tmpDoubleX.length; i++) {
			this.tmpDoubleX[i] = tmpDoubleXX[i];
			this.tmpDoubleY[i] = tmpDoubleYY[i];
			this.tmpDoubleZ[i] = tmpDoubleZZ[i];
		}
	}

	/**
	 * Initialise data.
	 */
	private void initShoreline() {
		final double[] yyOutline = { 6.93E+00, 6.91E+00, 6.90E+00, 6.88E+00, 6.86E+00,
							6.85E+00, 6.83E+00, 6.85E+00, 6.86E+00, 6.88E+00, 6.90E+00, 6.90E+00, 6.90E+00,
							6.89E+00, 6.88E+00, 6.86E+00, 6.84E+00, 6.83E+00, 6.81E+00, 6.79E+00, 6.78E+00,
							6.76E+00, 6.74E+00, 6.73E+00, 6.71E+00, 6.69E+00, 6.68E+00, 6.66E+00, 6.64E+00,
							6.63E+00, 6.61E+00, 6.59E+00, 6.58E+00, 6.56E+00, 6.54E+00, 6.53E+00, 6.52E+00,
							6.50E+00, 6.49E+00, 6.47E+00, 6.45E+00, 6.44E+00, 6.42E+00, 6.40E+00, 6.39E+00,
							6.37E+00, 6.35E+00, 6.34E+00, 6.32E+00, 6.30E+00, 6.29E+00, 6.27E+00, 6.25E+00,
							6.24E+00, 6.22E+00, 6.20E+00, 6.19E+00, 6.17E+00, 6.15E+00, 6.14E+00, 6.12E+00,
							6.10E+00, 6.08E+00, 6.07E+00, 6.05E+00, 6.04E+00, 6.02E+00, 6.00E+00, 5.98E+00,
							5.97E+00, 5.95E+00, 5.93E+00, 5.92E+00, 5.90E+00, 5.88E+00, 5.87E+00, 5.85E+00,
							5.83E+00, 5.82E+00, 5.80E+00, 5.78E+00, 5.77E+00, 5.76E+00, 5.74E+00, 5.73E+00,
							5.71E+00, 5.70E+00, 5.68E+00, 5.66E+00, 5.65E+00, 5.63E+00, 5.62E+00, 5.60E+00,
							5.59E+00, 5.59E+00, 5.57E+00, 5.56E+00, 5.54E+00, 5.52E+00, 5.51E+00, 5.49E+00,
							5.47E+00, 5.46E+00, 5.44E+00, 5.42E+00, 5.41E+00, 5.39E+00, 5.37E+00, 5.36E+00,
							5.34E+00, 5.34E+00, 5.33E+00, 5.32E+00, 5.31E+00, 5.30E+00, 5.30E+00, 5.30E+00,
							5.29E+00, 5.29E+00, 5.29E+00, 5.29E+00, 5.29E+00, 5.30E+00, 5.31E+00, 5.32E+00,
							5.34E+00, 5.35E+00, 5.36E+00, 5.36E+00, 5.38E+00, 5.39E+00, 5.40E+00, 5.42E+00,
							5.42E+00, 5.42E+00, 5.42E+00, 5.42E+00, 5.42E+00, 5.41E+00, 5.41E+00, 5.41E+00,
							5.40E+00, 5.38E+00, 5.37E+00, 5.35E+00, 5.33E+00, 5.32E+00, 5.30E+00, 5.28E+00,
							5.27E+00, 5.25E+00, 5.23E+00, 5.22E+00, 5.21E+00, 5.19E+00, 5.17E+00, 5.16E+00,
							5.14E+00, 5.12E+00, 5.11E+00, 5.09E+00, 5.07E+00, 5.06E+00, 5.06E+00, 5.05E+00,
							5.04E+00, 5.04E+00, 5.03E+00, 5.02E+00, 5.00E+00, 4.99E+00, 4.97E+00, 4.95E+00,
							4.93E+00, 4.92E+00, 4.91E+00, 4.90E+00, 4.89E+00, 4.87E+00, 4.86E+00, 4.84E+00,
							4.82E+00, 4.80E+00, 4.79E+00, 4.77E+00, 4.75E+00, 4.74E+00, 4.72E+00, 4.70E+00,
							4.69E+00, 4.67E+00, 4.65E+00, 4.64E+00, 4.62E+00, 4.60E+00, 4.58E+00, 4.57E+00,
							4.55E+00, 4.54E+00, 4.52E+00, 4.50E+00, 4.49E+00, 4.47E+00, 4.46E+00, 4.44E+00,
							4.42E+00, 4.41E+00, 4.39E+00, 4.38E+00, 4.37E+00, 4.36E+00, 4.34E+00, 4.32E+00,
							4.31E+00, 4.29E+00, 4.27E+00, 4.26E+00, 4.25E+00, 4.24E+00, 4.22E+00, 4.21E+00,
							4.19E+00, 4.18E+00, 4.17E+00, 4.15E+00, 4.14E+00, 4.12E+00, 4.10E+00, 4.08E+00,
							4.07E+00, 4.05E+00, 4.04E+00, 4.02E+00, 4.01E+00, 4.01E+00, 4.01E+00, 4.00E+00,
							4.00E+00, 4.00E+00, 3.99E+00, 3.99E+00, 3.98E+00, 3.98E+00, 3.97E+00, 3.97E+00,
							3.97E+00, 3.96E+00, 3.96E+00, 3.94E+00, 3.93E+00, 3.91E+00, 3.90E+00, 3.89E+00,
							3.89E+00, 3.88E+00, 3.86E+00, 3.85E+00, 3.84E+00, 3.83E+00, 3.82E+00, 3.80E+00,
							3.79E+00, 3.77E+00, 3.75E+00, 3.74E+00, 3.72E+00, 3.71E+00, 3.69E+00, 3.69E+00,
							3.69E+00, 3.69E+00, 3.69E+00, 3.69E+00, 3.71E+00, 3.72E+00, 3.72E+00, 3.74E+00,
							3.75E+00, 3.77E+00, 3.78E+00, 3.80E+00, 3.81E+00, 3.83E+00, 3.85E+00, 3.86E+00,
							3.88E+00, 3.90E+00, 3.91E+00, 3.93E+00, 3.95E+00, 3.96E+00, 3.98E+00, 4.00E+00,
							4.01E+00, 4.03E+00, 4.04E+00, 4.06E+00, 4.08E+00, 4.09E+00, 4.11E+00, 4.13E+00,
							4.14E+00, 4.16E+00, 4.18E+00, 4.19E+00, 4.21E+00, 4.23E+00, 4.24E+00, 4.26E+00,
							4.28E+00, 4.29E+00, 4.31E+00, 4.33E+00, 4.34E+00, 4.36E+00, 4.38E+00, 4.39E+00,
							4.41E+00, 4.43E+00, 4.44E+00, 4.46E+00, 4.48E+00, 4.49E+00, 4.51E+00, 4.53E+00,
							4.54E+00, 4.56E+00, 4.58E+00, 4.59E+00, 4.61E+00, 4.62E+00, 4.64E+00, 4.66E+00,
							4.67E+00, 4.69E+00, 4.70E+00, 4.72E+00, 4.73E+00, 4.75E+00, 4.76E+00, 4.78E+00,
							4.79E+00, 4.81E+00, 4.82E+00, 4.83E+00, 4.84E+00, 4.86E+00, 4.87E+00, 4.88E+00,
							4.90E+00, 4.91E+00, 4.93E+00, 4.94E+00, 4.95E+00, 4.97E+00, 4.98E+00, 5.00E+00,
							5.01E+00, 5.02E+00, 5.04E+00, 5.06E+00, 5.07E+00, 5.09E+00, 5.10E+00, 5.12E+00,
							5.14E+00, 5.15E+00, 5.17E+00, 5.19E+00, 5.20E+00, 5.22E+00, 5.24E+00, 5.25E+00,
							5.27E+00, 5.29E+00, 5.30E+00, 5.32E+00, 5.34E+00, 5.36E+00, 5.37E+00, 5.39E+00,
							5.41E+00, 5.42E+00, 5.44E+00, 5.45E+00, 5.47E+00, 5.48E+00, 5.50E+00, 5.51E+00,
							5.52E+00, 5.54E+00, 5.55E+00, 5.57E+00, 5.58E+00, 5.60E+00, 5.61E+00, 5.63E+00,
							5.65E+00, 5.66E+00, 5.68E+00, 5.69E+00, 5.70E+00, 5.71E+00, 5.73E+00, 5.74E+00,
							5.76E+00, 5.75E+00, 5.75E+00, 5.76E+00, 5.77E+00, 5.79E+00, 5.80E+00, 5.82E+00,
							5.84E+00, 5.85E+00, 5.87E+00, 5.88E+00, 5.90E+00, 5.91E+00, 5.93E+00, 5.94E+00,
							5.96E+00, 5.97E+00, 5.99E+00, 5.99E+00, 6.00E+00, 6.00E+00, 5.98E+00, 5.98E+00,
							5.97E+00, 5.98E+00, 6.00E+00, 5.98E+00, 5.98E+00, 6.00E+00, 6.02E+00, 6.03E+00,
							6.05E+00, 6.06E+00, 6.07E+00, 6.07E+00, 6.06E+00, 6.04E+00, 6.03E+00, 6.01E+00,
							6.03E+00, 6.04E+00, 6.06E+00, 6.08E+00, 6.09E+00, 6.10E+00, 6.11E+00, 6.13E+00,
							6.13E+00, 6.14E+00, 6.15E+00, 6.16E+00, 6.17E+00, 6.18E+00, 6.18E+00, 6.18E+00,
							6.19E+00, 6.21E+00, 6.22E+00, 6.23E+00, 6.24E+00, 6.25E+00, 6.25E+00, 6.26E+00,
							6.27E+00, 6.29E+00, 6.31E+00, 6.32E+00, 6.34E+00, 6.35E+00, 6.36E+00, 6.38E+00,
							6.39E+00, 6.41E+00, 6.43E+00, 6.45E+00, 6.46E+00, 6.48E+00, 6.49E+00, 6.51E+00,
							6.52E+00, 6.54E+00, 6.55E+00, 6.56E+00, 6.57E+00, 6.59E+00, 6.60E+00, 6.62E+00,
							6.63E+00, 6.65E+00, 6.66E+00, 6.67E+00, 6.69E+00, 6.70E+00, 6.72E+00, 6.73E+00,
							6.75E+00, 6.76E+00, 6.77E+00, 6.79E+00, 6.80E+00, 6.81E+00, 6.83E+00, 6.83E+00,
							6.85E+00, 6.86E+00, 6.87E+00, 6.88E+00, 6.88E+00, 6.89E+00, 6.90E+00, 6.90E+00,
							6.91E+00, 6.91E+00, 6.91E+00, 6.90E+00, 6.91E+00, 6.92E+00, 6.92E+00, 6.93E+00,
							6.93E+00, 6.93E+00, 6.91E+00, 6.90E+00, 6.88E+00, 6.87E+00, 6.88E+00, 6.90E+00,
							6.90E+00, 6.92E+00, 6.94E+00, 6.95E+00, 6.96E+00 };

		final double[] xxOutline = { 1.46171E+01, 1.45984E+01, 1.45883E+01, 1.45818E+01,
							1.45626E+01, 1.45435E+01, 1.45257E+01, 1.45462E+01, 1.45653E+01, 1.45854E+01,
							1.46027E+01, 1.46256E+01, 1.46482E+01, 1.46707E+01, 1.46934E+01, 1.47161E+01,
							1.47312E+01, 1.47494E+01, 1.47604E+01, 1.47746E+01, 1.47856E+01, 1.47939E+01,
							1.48040E+01, 1.48141E+01, 1.48175E+01, 1.48199E+01, 1.48247E+01, 1.48244E+01,
							1.48255E+01, 1.48258E+01, 1.48215E+01, 1.48172E+01, 1.48084E+01, 1.47978E+01,
							1.47836E+01, 1.47604E+01, 1.47376E+01, 1.47193E+01, 1.47142E+01, 1.47117E+01,
							1.47074E+01, 1.47017E+01, 1.46952E+01, 1.46828E+01, 1.46722E+01, 1.46621E+01,
							1.46461E+01, 1.46378E+01, 1.46313E+01, 1.46252E+01, 1.46186E+01, 1.46013E+01,
							1.45813E+01, 1.45770E+01, 1.45736E+01, 1.45725E+01, 1.45632E+01, 1.45513E+01,
							1.45470E+01, 1.45391E+01, 1.45335E+01, 1.45152E+01, 1.44961E+01, 1.44738E+01,
							1.44619E+01, 1.44387E+01, 1.44209E+01, 1.44031E+01, 1.43934E+01, 1.43847E+01,
							1.43768E+01, 1.43698E+01, 1.43628E+01, 1.43500E+01, 1.43353E+01, 1.43148E+01,
							1.43029E+01, 1.42950E+01, 1.42826E+01, 1.42694E+01, 1.42633E+01, 1.42409E+01,
							1.42222E+01, 1.42017E+01, 1.41789E+01, 1.41566E+01, 1.41406E+01, 1.41336E+01,
							1.41249E+01, 1.41174E+01, 1.41050E+01, 1.40822E+01, 1.40599E+01, 1.40371E+01,
							1.40146E+01, 1.40009E+01, 1.39939E+01, 1.39878E+01, 1.39777E+01, 1.39662E+01,
							1.39525E+01, 1.39306E+01, 1.39156E+01, 1.39023E+01, 1.38917E+01, 1.38806E+01,
							1.38692E+01, 1.38586E+01, 1.38412E+01, 1.38189E+01, 1.37965E+01, 1.37741E+01,
							1.37509E+01, 1.37280E+01, 1.37047E+01, 1.36809E+01, 1.36580E+01, 1.36351E+01,
							1.36126E+01, 1.35896E+01, 1.35667E+01, 1.35437E+01, 1.35207E+01, 1.34981E+01,
							1.34781E+01, 1.34716E+01, 1.34480E+01, 1.34250E+01, 1.34015E+01, 1.33788E+01,
							1.33561E+01, 1.33335E+01, 1.33099E+01, 1.32865E+01, 1.32631E+01, 1.32406E+01,
							1.32176E+01, 1.31952E+01, 1.31727E+01, 1.31503E+01, 1.31278E+01, 1.31054E+01,
							1.31042E+01, 1.31121E+01, 1.31155E+01, 1.31301E+01, 1.31393E+01, 1.31449E+01,
							1.31510E+01, 1.31494E+01, 1.31465E+01, 1.31426E+01, 1.31202E+01, 1.31070E+01,
							1.30968E+01, 1.30835E+01, 1.30640E+01, 1.30547E+01, 1.30558E+01, 1.30479E+01,
							1.30297E+01, 1.30115E+01, 1.29891E+01, 1.29667E+01, 1.29442E+01, 1.29205E+01,
							1.28972E+01, 1.28748E+01, 1.28520E+01, 1.28337E+01, 1.28173E+01, 1.27973E+01,
							1.27880E+01, 1.27757E+01, 1.27534E+01, 1.27310E+01, 1.27086E+01, 1.26863E+01,
							1.26635E+01, 1.26412E+01, 1.26212E+01, 1.26115E+01, 1.26054E+01, 1.25980E+01,
							1.25933E+01, 1.25813E+01, 1.25717E+01, 1.25584E+01, 1.25460E+01, 1.25345E+01,
							1.25239E+01, 1.25205E+01, 1.25140E+01, 1.25061E+01, 1.25000E+01, 1.24971E+01,
							1.24937E+01, 1.24791E+01, 1.24662E+01, 1.24511E+01, 1.24284E+01, 1.24061E+01,
							1.23874E+01, 1.23804E+01, 1.23739E+01, 1.23723E+01, 1.23720E+01, 1.23628E+01,
							1.23395E+01, 1.23171E+01, 1.23015E+01, 1.22964E+01, 1.23105E+01, 1.23121E+01,
							1.23010E+01, 1.22837E+01, 1.22614E+01, 1.22382E+01, 1.22153E+01, 1.21931E+01,
							1.21707E+01, 1.21480E+01, 1.21252E+01, 1.21024E+01, 1.20805E+01, 1.20713E+01,
							1.20513E+01, 1.20294E+01, 1.20103E+01, 1.19939E+01, 1.19711E+01, 1.19488E+01,
							1.19265E+01, 1.19041E+01, 1.18817E+01, 1.18592E+01, 1.18368E+01, 1.18143E+01,
							1.17905E+01, 1.17680E+01, 1.17443E+01, 1.17218E+01, 1.16989E+01, 1.16756E+01,
							1.16527E+01, 1.16302E+01, 1.16078E+01, 1.15849E+01, 1.15720E+01, 1.15808E+01,
							1.15576E+01, 1.15343E+01, 1.15119E+01, 1.14891E+01, 1.14667E+01, 1.14443E+01,
							1.14202E+01, 1.13969E+01, 1.13745E+01, 1.13517E+01, 1.13349E+01, 1.13324E+01,
							1.13267E+01, 1.13198E+01, 1.13110E+01, 1.12886E+01, 1.12663E+01, 1.12467E+01,
							1.12239E+01, 1.12000E+01, 1.11761E+01, 1.11527E+01, 1.11297E+01, 1.11070E+01,
							1.10831E+01, 1.10605E+01, 1.10378E+01, 1.10142E+01, 1.09969E+01, 1.09733E+01,
							1.09507E+01, 1.09320E+01, 1.09129E+01, 1.08996E+01, 1.08899E+01, 1.08803E+01,
							1.08625E+01, 1.08398E+01, 1.08207E+01, 1.08083E+01, 1.07928E+01, 1.07836E+01,
							1.07717E+01, 1.07503E+01, 1.07357E+01, 1.07126E+01, 1.06989E+01, 1.07027E+01,
							1.06908E+01, 1.06807E+01, 1.06764E+01, 1.06767E+01, 1.06832E+01, 1.06974E+01,
							1.06976E+01, 1.06902E+01, 1.06904E+01, 1.06970E+01, 1.07013E+01, 1.06911E+01,
							1.06878E+01, 1.06763E+01, 1.06734E+01, 1.06723E+01, 1.06734E+01, 1.06701E+01,
							1.06690E+01, 1.06597E+01, 1.06519E+01, 1.06467E+01, 1.06379E+01, 1.06224E+01,
							1.06213E+01, 1.06279E+01, 1.06339E+01, 1.06378E+01, 1.06448E+01, 1.06607E+01,
							1.06830E+01, 1.07058E+01, 1.07214E+01, 1.07419E+01, 1.07587E+01, 1.07815E+01,
							1.08011E+01, 1.08234E+01, 1.08448E+01, 1.08675E+01, 1.08903E+01, 1.09126E+01,
							1.09349E+01, 1.09581E+01, 1.09805E+01, 1.10033E+01, 1.10261E+01, 1.10484E+01,
							1.10716E+01, 1.10939E+01, 1.11172E+01, 1.11400E+01, 1.11600E+01, 1.11823E+01,
							1.12046E+01, 1.12270E+01, 1.12493E+01, 1.12702E+01, 1.12930E+01, 1.13153E+01,
							1.13377E+01, 1.13609E+01, 1.13805E+01, 1.14009E+01, 1.14233E+01, 1.14438E+01,
							1.14620E+01, 1.14784E+01, 1.14944E+01, 1.15162E+01, 1.15318E+01, 1.15460E+01,
							1.15584E+01, 1.15703E+01, 1.15804E+01, 1.15897E+01, 1.16020E+01, 1.16099E+01,
							1.16178E+01, 1.16230E+01, 1.16349E+01, 1.16446E+01, 1.16498E+01, 1.16577E+01,
							1.16777E+01, 1.17005E+01, 1.17232E+01, 1.17455E+01, 1.17678E+01, 1.17906E+01,
							1.18116E+01, 1.18339E+01, 1.18562E+01, 1.18786E+01, 1.18999E+01, 1.19227E+01,
							1.19450E+01, 1.19678E+01, 1.19856E+01, 1.20024E+01, 1.20166E+01, 1.20304E+01,
							1.20531E+01, 1.20755E+01, 1.20984E+01, 1.21207E+01, 1.21344E+01, 1.21567E+01,
							1.21793E+01, 1.22023E+01, 1.22247E+01, 1.22470E+01, 1.22652E+01, 1.22740E+01,
							1.22941E+01, 1.23042E+01, 1.23265E+01, 1.23488E+01, 1.23716E+01, 1.23939E+01,
							1.24162E+01, 1.24394E+01, 1.24612E+01, 1.24844E+01, 1.25067E+01, 1.25300E+01,
							1.25524E+01, 1.25753E+01, 1.25983E+01, 1.26209E+01, 1.26434E+01, 1.26504E+01,
							1.26434E+01, 1.26549E+01, 1.26443E+01, 1.26214E+01, 1.26045E+01, 1.25962E+01,
							1.25978E+01, 1.26201E+01, 1.26425E+01, 1.26653E+01, 1.26877E+01, 1.27086E+01,
							1.27147E+01, 1.27189E+01, 1.27074E+01, 1.27180E+01, 1.27277E+01, 1.27383E+01,
							1.27498E+01, 1.27707E+01, 1.27931E+01, 1.28155E+01, 1.28382E+01, 1.28611E+01,
							1.28835E+01, 1.29059E+01, 1.29287E+01, 1.29510E+01, 1.29743E+01, 1.29968E+01,
							1.30207E+01, 1.30440E+01, 1.30640E+01, 1.30836E+01, 1.31068E+01, 1.31302E+01,
							1.31531E+01, 1.31764E+01, 1.31997E+01, 1.32220E+01, 1.32443E+01, 1.32576E+01,
							1.32695E+01, 1.32873E+01, 1.33105E+01, 1.33329E+01, 1.33552E+01, 1.33784E+01,
							1.33944E+01, 1.34113E+01, 1.34344E+01, 1.34568E+01, 1.34781E+01, 1.35004E+01,
							1.35228E+01, 1.35415E+01, 1.35610E+01, 1.35834E+01, 1.36058E+01, 1.36282E+01,
							1.36450E+01, 1.36583E+01, 1.36806E+01, 1.37030E+01, 1.37239E+01, 1.37472E+01,
							1.37695E+01, 1.37864E+01, 1.38092E+01, 1.38328E+01, 1.38538E+01, 1.38761E+01,
							1.38984E+01, 1.39212E+01, 1.39449E+01, 1.39672E+01, 1.39913E+01, 1.40141E+01,
							1.40365E+01, 1.40589E+01, 1.40816E+01, 1.41049E+01, 1.41273E+01, 1.41502E+01,
							1.41725E+01, 1.41950E+01, 1.42179E+01, 1.42408E+01, 1.42637E+01, 1.42889E+01,
							1.43115E+01, 1.43339E+01, 1.43563E+01, 1.43787E+01, 1.44021E+01, 1.44245E+01,
							1.44475E+01, 1.44702E+01, 1.44924E+01, 1.44868E+01, 1.44644E+01, 1.44868E+01,
							1.45073E+01, 1.45297E+01, 1.45515E+01, 1.45662E+01, 1.45885E+01, 1.45938E+01 };

		this.xOutline = new double[xxOutline.length];
		this.yOutline = new double[yyOutline.length];

		for (int i = 0; i < this.xOutline.length; i++) {
			this.xOutline[i] = xxOutline[i];
			this.yOutline[i] = yyOutline[i];
		}
	}

}
