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
 * ----------------
 * Performance.java
 * ----------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: Performance.java,v 1.1 2011-01-31 09:01:44 klukas Exp $
 * Changes (since 11-Oct-2002)
 * ---------------------------
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */
package org.jfree.chart.demo;

import java.awt.geom.Line2D;
import java.util.Date;

/**
 * A basic performance test for a couple of common operations.
 */
public class Performance {

	/** The value. */
	private double value = 2.0;

	/** The number. */
	private Double number = new Double(this.value);

	/**
	 * Default constructor.
	 */
	public Performance() {
		super();
	}

	/**
	 * Creates lines in a loop.
	 * 
	 * @param count
	 *           the number of lines to create.
	 */
	public void createLines(final int count) {

		Line2D line = new Line2D.Double();
		for (int i = 0; i < count; i++) {
			line = new Line2D.Double(1.0, 1.0, 1.0, 1.0);
		}
		System.out.println(line.toString());

	}

	/**
	 * Creates one line, then repeatedly calls the setLine method.
	 * 
	 * @param count
	 *           the number of times to call the setLine method.
	 */
	public void setLines(final int count) {

		final Line2D line = new Line2D.Double(0.0, 0.0, 0.0, 0.0);
		for (int i = 0; i < count; i++) {
			line.setLine(1.0, 1.0, 1.0, 1.0);
		}

	}

	/**
	 * Repeatedly grabs a value from a Number instance.
	 * 
	 * @param count
	 *           the number of times to call doubleValue().
	 */
	public void getNumber(final int count) {

		double d = 0.0;
		for (int i = 0; i < count; i++) {
			d = this.number.doubleValue();
		}
		System.out.println(d);

	}

	/**
	 * Repeatedly grabs a value from a double.
	 * 
	 * @param count
	 *           the number of times to fetch the value.
	 */
	public void getValue(final int count) {

		double d = 0.0;
		for (int i = 0; i < count; i++) {
			d = this.value;
		}
		System.out.println(d);

	}

	/**
	 * Writes the current time to the console.
	 * 
	 * @param text
	 *           the prefix.
	 * @param time
	 *           the time.
	 */
	public void writeTime(final String text, final Date time) {

		System.out.println(text + " : " + time.getTime());

	}

	/**
	 * Starting point for the application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final Performance p = new Performance();
		System.out.println("Simple performance tests.");

		final Date start1 = new Date();
		p.createLines(100000);
		final Date end1 = new Date();

		final Date start2 = new Date();
		p.setLines(100000);
		final Date end2 = new Date();

		p.writeTime("Start create lines", start1);
		p.writeTime("Finish create lines", end1);
		p.writeTime("Start set lines", start2);
		p.writeTime("Finish set lines", end2);

		final Date start3 = new Date();
		p.getNumber(1000000);
		final Date end3 = new Date();

		final Date start4 = new Date();
		p.getValue(1000000);
		final Date end4 = new Date();

		p.writeTime("Start get number", start3);
		p.writeTime("Finish get number", end3);
		p.writeTime("Start get value", start4);
		p.writeTime("Finish get value", end4);

	}

}
