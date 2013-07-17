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
 * ContourPlotDemo.java
 * --------------------
 * (C) Copyright 2002-2004, by David M. O'Donnell and Contributors.
 * Original Author: David M. O'Donnell;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: ContourPlotDemo.java,v 1.1 2011-01-31 09:01:52 klukas Exp $
 * Changes
 * -------
 * 26-Nov-2002 : Version 1, contributed by David M. O'Donnell (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.GradientPaint;
import java.util.Date;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ColorBar;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ContourPlot;
import org.jfree.data.ContourDataset;
import org.jfree.data.DefaultContourDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demonstration application to illustrate ContourPlot.
 * Command line options exist to control different plot properties
 * such as colorbar orientation, etc. List of options are available
 * by launching with the -? option, e.g., ContourPlotDemo -?
 * 
 * @author David M. O'Donnell
 */
public class ContourPlotDemo extends ApplicationFrame {

	/** The x-axis. */
	private ValueAxis xAxis = null;

	/** The y-axis. */
	private NumberAxis yAxis = null;

	/** The z-axis. */
	private ColorBar zColorBar = null;

	/** Flag for vertical z-axis. */
	// private static boolean zIsVertical = false;

	/** Flag for x is date axis. */
	private static boolean xIsDate = false;

	/** Flag for x is log. */
	private static boolean xIsLog = false;

	/** Flag for y is log. */
	private static boolean yIsLog = false;

	/** Flag for z is log. */
	private static boolean zIsLog = false;

	/** Flag for x is inverted. */
	private static boolean xIsInverted = false;

	/** Flag for y is inverted. */
	private static boolean yIsInverted = false;

	/** Flag for z is inverted. */
	private static boolean zIsInverted = false;

	/** Flag for make holes. */
	private static boolean makeHoles = false;

	/** The number of x values in the dataset. */
	private static int numX = 10;

	/** The number of y values in the dataset. */
	private static int numY = 20;

	/** The ratio. */
	private static double ratio = 0.0;

	/** The panel. */
	public ChartPanel panel = null;

	/**
	 * Constructs a new demonstration application.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public ContourPlotDemo(final String title) {

		super(title);

		final JFreeChart chart = createContourPlot();
		this.panel = new ChartPanel(chart, true, true, true, true, true);
		this.panel.setPreferredSize(new java.awt.Dimension(500, 270));
		this.panel.setMaximumDrawHeight(100000); // stop ChartPanel from scaling output
		this.panel.setMaximumDrawWidth(100000); // stop ChartPanel from scaling output
		this.panel.setHorizontalZoom(true);
		this.panel.setVerticalZoom(true);
		this.panel.setFillZoomRectangle(true);

	}

	// ****************************************************************************
	// * COMMERCIAL SUPPORT / JFREECHART DEVELOPER GUIDE *
	// * Please note that commercial support and documentation is available from: *
	// * *
	// * http://www.object-refinery.com/jfreechart/support.html *
	// * *
	// * This is not only a great service for developers, but is a VERY IMPORTANT *
	// * source of funding for the JFreeChart project. Please support us so that *
	// * we can continue developing free software. *
	// ****************************************************************************

	/**
	 * Creates a ContourPlot chart.
	 * 
	 * @return the chart.
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

		this.zColorBar.getAxis().setInverted(zIsInverted);
		this.zColorBar.getAxis().setTickMarksVisible(true);

		final ContourDataset data = createDataset();

		final ContourPlot plot = new ContourPlot(data, this.xAxis, this.yAxis, this.zColorBar);

		if (xIsDate) {
			ratio = Math.abs(ratio); // don't use plot units for ratios when x axis is date
		}
		plot.setDataAreaRatio(ratio);

		final JFreeChart chart = new JFreeChart(title, null, plot, false);
		chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000, Color.green));

		return chart;

	}

	/**
	 * Creates a ContourDataset.
	 * 
	 * @return ContourDataset.
	 */
	private ContourDataset createDataset() {

		final int numValues = numX * numY;
		final Date[] tmpDateX = new Date[numValues];
		final double[] tmpDoubleX = new double[numValues];
		final double[] tmpDoubleY = new double[numValues];

		final Double[] oDoubleX = new Double[numValues];
		final Double[] oDoubleY = new Double[numValues];
		final Double[] oDoubleZ = new Double[numValues];

		int j = 0;
		int z = 0;
		int i = 0;
		int last = 0;
		double zmult = 1.0;
		for (int k = 0; k < numValues; k++) {
			i = k / numX;
			if (last != i) {
				last = i;
				z = 0;
				zmult = 1.005 * zmult;
			}
			tmpDateX[k] = new Date((long) ((i + 100) * 1.e8));
			tmpDoubleX[k] = i + 2;
			tmpDoubleY[k] = zmult * (z++);
			oDoubleX[k] = new Double(tmpDoubleX[k]);
			oDoubleY[k] = new Double(tmpDoubleY[k]);
			final double rad = Math.random();
			if (makeHoles && (rad > 0.4 && rad < 0.6)) {
				oDoubleZ[k] = null;
			} else {
				// tmpDoubleZ[k] = 3.0 * ((tmpDoubleX[k] + 1) * (tmpDoubleY[k] + 1) + 100);
				oDoubleZ[k] = new Double(3.0 * ((tmpDoubleX[k] + 1) * (tmpDoubleY[k] + 1) + 100));
			}
			j++;
		}
		ContourDataset data = null;

		if (xIsDate) {
			data = new DefaultContourDataset("Contouring", tmpDateX, oDoubleY, oDoubleZ);
		} else {
			data = new DefaultContourDataset("Contouring", oDoubleX, oDoubleY, oDoubleZ);
		}
		return data;

	}

	/**
	 * Sets options passed via the command line
	 * 
	 * @param args
	 *           the command line arguments.
	 * @return Flag indicating whether program should continue.
	 */
	protected static boolean processArgs(final String[] args) {

		final String[] options = { "-?",
										"-invert",
										"-log",
										"-date",
										"-vertical",
										"-holes",
										"-ratio:",
										"-numX:",
										"-numY:" };

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
							xIsInverted = true;
							yIsInverted = true;
							zIsInverted = true;
							break;
						case 2:
							xIsLog = true;
							yIsLog = true;
							zIsLog = true;
							break;
						case 3:
							xIsDate = true;
							break;
						case 4:
							// zIsVertical = true;
							break;
						case 5:
							makeHoles = true;
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
						default:
							System.out.println("Only 9 options available, update options array");
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
	 * Prints usage information.
	 */
	public static void usage() {
		System.out.println("Usage:");
		System.out.println("ContourPlotDemo -? -invert -log -date -vertical -holes -ratio:value "
									+ "-numX:value -numY:value");
		System.out.println("Where:");
		System.out.println("-? displays usage and exits");
		System.out.println("-invert cause axes to be inverted");
		System.out.println("-log all axes will be logcale");
		System.out.println("-date the X axis will be a date");
		System.out.println("-vertical the colorbar will be drawn vertically");
		System.out.println("-holes demos plotting data with missing values");
		System.out.println("-ratio forces plot to maintain aspect ratio (Y/X) indicated by value");
		System.out.println("       positive values are in pixels, while negative is in plot units");
		System.out.println("-numX number of values to generate along the X axis");
		System.out.println("-numY number of values to generate along the X axis");

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
		final ContourPlotDemo demo = new ContourPlotDemo("ContourPlot Demo");
		demo.setContentPane(demo.panel);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
