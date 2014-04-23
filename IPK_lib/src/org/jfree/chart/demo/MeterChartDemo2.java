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
 * MeterChartDemo2.java
 * --------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: MeterChartDemo2.java,v 1.1 2011-01-31 09:01:50 klukas Exp $
 * Changes
 * -------
 * 09-Feb-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.DefaultValueDataset;
import org.jfree.data.ValueDataset;

/**
 * In this demo, a meter chart is saved to a scaled image file.
 */
public class MeterChartDemo2 {

	/**
	 * Default constructor.
	 */
	public MeterChartDemo2() {
		super();
	}

	/**
	 * Starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final ValueDataset dataset = new DefaultValueDataset(75.0);
		final MeterPlot plot = new MeterPlot(dataset);
		final JFreeChart chart = new JFreeChart("Scaled Image Test", plot);

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

		// save it to an image
		try {
			final File file1 = new File("meterchart100.png");
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(file1));
			final BufferedImage image = chart.createBufferedImage(200, 200, 400, 400, null);
			ChartUtilities.writeBufferedImageAsPNG(out, image);
		} catch (IOException e) {
			System.out.println(e.toString());
		}

	}

}
