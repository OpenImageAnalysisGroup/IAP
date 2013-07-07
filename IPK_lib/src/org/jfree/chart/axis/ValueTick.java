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
 * --------------
 * ValueTick.java
 * --------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ValueTick.java,v 1.1 2011-01-31 09:01:37 klukas Exp $
 * Changes
 * -------
 * 07-Nov-2003 : Version 1 (DG);
 */
package org.jfree.chart.axis;

import org.jfree.ui.TextAnchor;

/**
 * A value tick.
 */
public abstract class ValueTick extends Tick {

	/** The value. */
	private double value;

	/**
	 * Creates a new value tick.
	 * 
	 * @param value
	 *           the value.
	 * @param label
	 *           the label.
	 * @param textAnchor
	 *           the part of the label that is aligned to the anchor point.
	 * @param rotationAnchor
	 *           defines the rotation point relative to the label.
	 * @param angle
	 *           the rotation angle (in radians).
	 */
	public ValueTick(double value, String label,
							TextAnchor textAnchor, TextAnchor rotationAnchor, double angle) {

		super(label, textAnchor, rotationAnchor, angle);
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

	/**
	 * Tests this tick for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object to test (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ValueTick && super.equals(obj)) {
			ValueTick vt = (ValueTick) obj;
			if (!(this.value == vt.value)) {
				return false;
			}
			return true;
		}
		return false;
	}

}
