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
 * -----------------
 * Performance2.java
 * -----------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: Performance2.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes (since 11-Oct-2002)
 * ---------------------------
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */
package org.jfree.chart.demo;

import java.util.Date;

/**
 * A basic performance test for a couple of common operations.
 */
public class Performance2 {

	/** A double primitive. */
	private double primitive = 42.0;

	/** A number object. */
	private Number object = new Double(42.0);

	/**
	 * Default constructor.
	 */
	public Performance2() {
		super();
	}

	/**
	 * Just use double value - should be fast.
	 * 
	 * @return the double value.
	 */
	public double getPrimitive() {
		return this.primitive;
	}

	/**
	 * Creates a Number object every time the primitive is accessed - should be really slow.
	 * 
	 * @return creates and returns a Number object.
	 */
	public Number getPrimitiveAsObject() {
		return new Double(this.primitive);
	}

	/**
	 * Returns the object - caller has to use doubleValue() method.
	 * 
	 * @return an existing Number object.
	 */
	public Number getObject() {
		return this.object;
	}

	/**
	 * Returns a double value generated from the Object - should be similar to previous method,
	 * but is not!
	 * 
	 * @return the doubleValue() for the Number.
	 */
	public double getObjectAsPrimitive() {
		return this.object.doubleValue();
	}

	/**
	 * Cycles through accessing the primitive.
	 * 
	 * @param count
	 *           the number of times to access.
	 */
	public void getPrimitiveLoop(final int count) {

		double d = 0.0;
		for (int i = 0; i < count; i++) {
			d = getPrimitive();
		}
		System.out.println(d);

	}

	/**
	 * Cycles through accessing the primitive as an object.
	 * 
	 * @param count
	 *           the number of times to access.
	 */
	public void getPrimitiveAsObjectLoop(final int count) {

		double d = 0.0;
		for (int i = 0; i < count; i++) {
			d = getPrimitiveAsObject().doubleValue();
		}
		System.out.println(d);

	}

	/**
	 * Cycles through accessing the object as a primitive.
	 * 
	 * @param count
	 *           the number of times to access.
	 */
	public void getObjectAsPrimitiveLoop(final int count) {

		double d = 0.0;
		for (int i = 0; i < count; i++) {
			d = getObjectAsPrimitive();
		}
		System.out.println(d);

	}

	/**
	 * Cycles through accessing the object.
	 * 
	 * @param count
	 *           the number of times to access.
	 */
	public void getObjectLoop(final int count) {

		double d = 0.0;
		for (int i = 0; i < count; i++) {
			d = getObject().doubleValue();
		}
		System.out.println(d);

	}

	/**
	 * Outputs the current status to the console.
	 * 
	 * @param label
	 *           the label.
	 * @param start
	 *           the start time.
	 * @param end
	 *           the end time.
	 */
	public void status(final String label, final Date start, final Date end) {
		final long elapsed = end.getTime() - start.getTime();
		System.out.println(label + start.getTime() + "-->" + end.getTime() + " = " + elapsed);
	}

	/**
	 * The starting point for the performance test.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final Performance2 performance = new Performance2();
		final int count = 10000000;

		for (int repeat = 0; repeat < 3; repeat++) { // repeat a few times just to make
			// sure times are consistent
			final Date s1 = new Date();
			performance.getPrimitiveLoop(count);
			final Date e1 = new Date();
			performance.status("getPrimitive() : ", s1, e1);

			final Date s2 = new Date();
			performance.getPrimitiveAsObjectLoop(count);
			final Date e2 = new Date();
			performance.status("getPrimitiveAsObject() : ", s2, e2);

			final Date s3 = new Date();
			performance.getObjectLoop(count);
			final Date e3 = new Date();
			performance.status("getObject() : ", s3, e3);

			final Date s4 = new Date();
			performance.getObjectAsPrimitiveLoop(count);
			final Date e4 = new Date();
			performance.status("getObjectAsPrimitive() : ", s4, e4);

			System.out.println("-------------------");
		}
	}

}
