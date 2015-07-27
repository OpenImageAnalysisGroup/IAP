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
 * ValueMarker.java
 * ----------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ValueMarker.java,v 1.1 2011-01-31 09:02:09 klukas Exp $
 * Changes
 * -------
 * 09-Feb-2004 : Version 1 (DG);
 */

package org.jfree.chart.plot;

import java.awt.Paint;
import java.awt.Stroke;

/**
 * A marker that represents a single fixed value.
 */
public class ValueMarker extends Marker {

	/** The constant value. */
	private double value;

	/**
	 * Creates a new marker.
	 * 
	 * @param value
	 *           the value.
	 */
	public ValueMarker(double value) {
		super();
		this.value = value;
	}

	/**
	 * Creates a new value marker.
	 * 
	 * @param value
	 *           the value.
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 * @param outlinePaint
	 *           the outline paint (<code>null</code> permitted).
	 * @param outlineStroke
	 *           the outline stroke (<code>null</code> permitted).
	 * @param alpha
	 *           the alpha transparency.
	 */
	public ValueMarker(double value, Paint paint, Stroke stroke,
								Paint outlinePaint, Stroke outlineStroke, float alpha) {
		super(paint, stroke, paint, stroke, alpha);
		this.value = value;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value.
	 */
	public double getValue() {
		return this.value;
	}

}
