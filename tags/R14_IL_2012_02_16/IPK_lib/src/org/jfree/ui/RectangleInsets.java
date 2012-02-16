/*
 * ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jcommon/index.html
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
 * RectangleInsets.java
 * --------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: RectangleInsets.java,v 1.1 2011-01-31 09:02:25 klukas Exp $
 * Changes:
 * --------
 * 11-Feb-2004 : Version 1 (DG);
 * 14-Jun-2004 : Implemented Serializable (DG);
 */

package org.jfree.ui;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.util.UnitType;

/**
 * Represents the insets for a rectangle, specified in absolute or relative terms. This class is
 * immutable.
 */
public class RectangleInsets implements Serializable {

	/** Absolute or relative units. */
	private UnitType unitType;

	/** The top insets. */
	private double top;

	/** The bottom insets. */
	private double bottom;

	/** The left insets. */
	private double left;

	/** The right insets. */
	private double right;

	/**
	 * Creates a new instance.
	 * 
	 * @param unitType
	 *           absolute or relative units (<code>null</code> not permitted).
	 * @param top
	 *           the top insets.
	 * @param bottom
	 *           the bottom insets.
	 * @param left
	 *           the left insets.
	 * @param right
	 *           the right insets.
	 */
	public RectangleInsets(final UnitType unitType,
									final double top, final double bottom,
									final double left, final double right) {
		if (unitType == null) {
			throw new IllegalArgumentException("Null 'unitType' argument.");
		}
		this.unitType = unitType;
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns the unit type (absolute or relative). This specifies whether the insets are
	 * measured as Java2D units or percentages.
	 * 
	 * @return The unit type (never <code>null</code>).
	 */
	public UnitType getUnitType() {
		return this.unitType;
	}

	/**
	 * Returns the top insets.
	 * 
	 * @return The top insets.
	 */
	public double getTop() {
		return this.top;
	}

	/**
	 * Returns the bottom insets.
	 * 
	 * @return The bottom insets.
	 */
	public double getBottom() {
		return this.bottom;
	}

	/**
	 * Returns the left insets.
	 * 
	 * @return The left insets.
	 */
	public double getLeft() {
		return this.left;
	}

	/**
	 * Returns the right insets.
	 * 
	 * @return The right insets.
	 */
	public double getRight() {
		return this.right;
	}

	/**
	 * Tests this instance for equality with an arbitrary object.
	 * 
	 * @param object
	 *           the object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof RectangleInsets) {
			RectangleInsets r = (RectangleInsets) object;
			if (r.unitType != this.unitType) {
				return false;
			}
			if (r.left != this.left) {
				return false;
			}
			if (r.right != this.right) {
				return false;
			}
			if (r.top != this.top) {
				return false;
			}
			if (r.bottom != this.bottom) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Creates an 'inset' rectangle.
	 * 
	 * @param base
	 *           the base rectangle (<code>null</code> not permitted).
	 * @return The inset rectangle.
	 */
	public Rectangle2D createInsetRectangle(final Rectangle2D base) {
		return createInsetRectangle(base, true, true);
	}

	/**
	 * Creates an 'inset' rectangle.
	 * 
	 * @param base
	 *           the base rectangle (<code>null</code> not permitted).
	 * @param horizontal
	 *           apply horizontal insets?
	 * @param vertical
	 *           apply vertical insets?
	 * @return The inset rectangle.
	 */
	public Rectangle2D createInsetRectangle(final Rectangle2D base,
															final boolean horizontal, final boolean vertical) {
		if (base == null) {
			throw new IllegalArgumentException("Null 'base' argument.");
		}
		double topMargin = 0.0;
		double bottomMargin = 0.0;
		if (vertical) {
			topMargin = calculateTopMargin(base.getHeight());
			bottomMargin = calculateBottomMargin(base.getHeight());
		}
		double leftMargin = 0.0;
		double rightMargin = 0.0;
		if (horizontal) {
			leftMargin = calculateLeftMargin(base.getWidth());
			rightMargin = calculateRightMargin(base.getWidth());
		}
		return new Rectangle2D.Double(
							base.getX() + leftMargin,
							base.getY() + topMargin,
							base.getWidth() - leftMargin - rightMargin,
							base.getHeight() - topMargin - bottomMargin);
	}

	/**
	 * Creates an outset rectangle.
	 * 
	 * @param base
	 *           the base rectangle (<code>null</code> not permitted).
	 * @return An outset rectangle.
	 */
	public Rectangle2D createOutsetRectangle(final Rectangle2D base) {
		return createOutsetRectangle(base, true, true);
	}

	/**
	 * Creates an outset rectangle.
	 * 
	 * @param base
	 *           the base rectangle (<code>null</code> not permitted).
	 * @param horizontal
	 *           apply horizontal insets?
	 * @param vertical
	 *           apply vertical insets?
	 * @return An outset rectangle.
	 */
	public Rectangle2D createOutsetRectangle(final Rectangle2D base,
															final boolean horizontal, final boolean vertical) {
		if (base == null) {
			throw new IllegalArgumentException("Null 'base' argument.");
		}
		double topMargin = 0.0;
		double bottomMargin = 0.0;
		if (vertical) {
			topMargin = calculateTopMargin(base.getHeight());
			bottomMargin = calculateBottomMargin(base.getHeight());
		}
		double leftMargin = 0.0;
		double rightMargin = 0.0;
		if (horizontal) {
			leftMargin = calculateLeftMargin(base.getWidth());
			rightMargin = calculateRightMargin(base.getWidth());
		}
		return new Rectangle2D.Double(
							base.getX() - leftMargin,
							base.getY() - topMargin,
							base.getWidth() + leftMargin + rightMargin,
							base.getHeight() + topMargin + bottomMargin);
	}

	/**
	 * Returns the top margin.
	 * 
	 * @param height
	 *           the height of the base rectangle.
	 * @return The top margin (in Java2D units).
	 */
	public double calculateTopMargin(final double height) {
		double result = this.top;
		if (this.unitType == UnitType.RELATIVE) {
			result = (this.top * height);
		}
		return result;
	}

	/**
	 * Returns the bottom margin.
	 * 
	 * @param height
	 *           the height of the base rectangle.
	 * @return The bottom margin (in Java2D units).
	 */
	public double calculateBottomMargin(final double height) {
		double result = this.bottom;
		if (this.unitType == UnitType.RELATIVE) {
			result = (this.bottom * height);
		}
		return result;
	}

	/**
	 * Returns the left margin.
	 * 
	 * @param width
	 *           the width of the base rectangle.
	 * @return The left margin (in Java2D units).
	 */
	public double calculateLeftMargin(final double width) {
		double result = this.left;
		if (this.unitType == UnitType.RELATIVE) {
			result = (this.left * width);
		}
		return result;
	}

	/**
	 * Returns the right margin.
	 * 
	 * @param width
	 *           the width of the base rectangle.
	 * @return The right margin (in Java2D units).
	 */
	public double calculateRightMargin(final double width) {
		double result = this.right;
		if (this.unitType == UnitType.RELATIVE) {
			result = (this.right * width);
		}
		return result;
	}

}
