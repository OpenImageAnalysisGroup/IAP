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
 * -----------
 * Spacer.java
 * -----------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: Spacer.java,v 1.1 2011-01-31 09:02:22 klukas Exp $
 * Changes
 * -------
 * 07-Feb-2002 : Version 1 (DG);
 * 18-Sep-2002 : Added trim(..) method, completed Javadocs and fixed Checkstyle issues (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 01-Apr-2004 : Added trimWidth() method (DG);
 */

package org.jfree.ui;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * Represents an amount of blank space inside (or sometimes outside) a
 * rectangle. This class is similar in function to the Insets class, but
 * allows for the space to be specified in relative terms as well as absolute
 * terms.
 * <P>
 * Instances of this class are immutable.
 */
public class Spacer implements Serializable {

	/** A constant for 'relative' spacing. */
	public static final int RELATIVE = 0;

	/** A constant for 'absolute' spacing. */
	public static final int ABSOLUTE = 1;

	/** The spacing type (relative or absolute). */
	private int type;

	/** The space on the left. */
	private double left;

	/** The space on the right. */
	private double right;

	/** The space at the top. */
	private double top;

	/** The space at the bottom. */
	private double bottom;

	/**
	 * Creates a new Spacer object.
	 * <p>
	 * The space can be specified in relative or absolute terms (using the constants <code>RELATIVE</code> and <code>ABSOLUTE</code> for the <code>type</code>
	 * argument. For relative spacing, the margins are specified as percentages (of the overall height or width). For absolute spacing, the margins are specified
	 * in points (1/72 inch).
	 * 
	 * @param type
	 *           the type of spacing (relative or absolute).
	 * @param left
	 *           the left margin.
	 * @param top
	 *           the top margin.
	 * @param right
	 *           the right margin.
	 * @param bottom
	 *           the bottom margin.
	 */
	public Spacer(final int type,
						final double left,
						final double top,
						final double right,
						final double bottom) {

		this.type = type;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;

	}

	/**
	 * Returns the amount of space for the left hand side of a rectangular area.
	 * <p>
	 * The width argument is only used for calculating 'relative' spacing.
	 * 
	 * @param width
	 *           the overall width of the rectangular area.
	 * @return the space (in points).
	 */
	public double getLeftSpace(final double width) {

		double result = 0.0;

		if (this.type == ABSOLUTE) {
			result = this.left;
		} else
			if (this.type == RELATIVE) {
				result = this.left * width;
			}

		return result;

	}

	/**
	 * Returns the amount of space for the right hand side of a rectangular area.
	 * <p>
	 * The width argument is only used for calculating 'relative' spacing.
	 * 
	 * @param width
	 *           the overall width of the rectangular area.
	 * @return the space (in points).
	 */
	public double getRightSpace(final double width) {

		double result = 0.0;

		if (this.type == ABSOLUTE) {
			result = this.right;
		} else
			if (this.type == RELATIVE) {
				result = this.right * width;
			}

		return result;

	}

	/**
	 * Returns the amount of space for the top of a rectangular area.
	 * <p>
	 * The height argument is only used for calculating 'relative' spacing.
	 * 
	 * @param height
	 *           the overall height of the rectangular area.
	 * @return the space (in points).
	 */
	public double getTopSpace(final double height) {

		double result = 0.0;

		if (this.type == ABSOLUTE) {
			result = this.top;
		} else
			if (this.type == RELATIVE) {
				result = this.top * height;
			}

		return result;

	}

	/**
	 * Returns the amount of space for the bottom of a rectangular area.
	 * <p>
	 * The height argument is only used for calculating 'relative' spacing.
	 * 
	 * @param height
	 *           the overall height of the rectangular area.
	 * @return the space (in points).
	 */
	public double getBottomSpace(final double height) {

		double result = 0.0;

		if (this.type == ABSOLUTE) {
			result = this.bottom;
		} else
			if (this.type == RELATIVE) {
				result = this.bottom * height;
			}

		return result;

	}

	/**
	 * Returns the width after adding the left and right spacing amounts.
	 * 
	 * @param width
	 *           the original width.
	 * @return the adjusted width.
	 */
	public double getAdjustedWidth(final double width) {

		double result = width;

		if (this.type == ABSOLUTE) {
			result = result + this.left + this.right;
		} else
			if (this.type == RELATIVE) {
				result = result + (this.left * width) + (this.right * width);
			}

		return result;

	}

	/**
	 * Returns the height after adding the top and bottom spacing amounts.
	 * 
	 * @param height
	 *           the original height.
	 * @return the adjusted height.
	 */
	public double getAdjustedHeight(final double height) {

		double result = height;

		if (this.type == ABSOLUTE) {
			result = result + this.top + this.bottom;
		} else
			if (this.type == RELATIVE) {
				result = result + (this.top * height) + (this.bottom * height);
			}

		return result;

	}

	/**
	 * Calculates the margins and trims them from the supplied area.
	 * 
	 * @param area
	 *           the area to be trimmed.
	 */
	public void trim(final Rectangle2D area) {
		final double x = area.getX();
		final double y = area.getY();
		final double h = area.getHeight();
		final double w = area.getWidth();
		final double l = getLeftSpace(w);
		final double r = getRightSpace(w);
		final double t = getTopSpace(h);
		final double b = getBottomSpace(h);
		area.setRect(x + l, y + t, w - l - r, h - t - b);
	}

	/**
	 * Reduces the specified width according to the left and right space settings.
	 * 
	 * @param w
	 *           the width.
	 * @return The reduced width.
	 */
	public double trimWidth(final double w) {
		return w - getLeftSpace(w) - getRightSpace(w);
	}

	/**
	 * Tests this object for equality with another object.
	 * 
	 * @param obj
	 *           the other object.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof Spacer) {
			final Spacer s = (Spacer) obj;
			final boolean b0 = (this.type == s.type);
			final boolean b1 = (this.left == s.left);
			final boolean b2 = (this.right == s.right);
			final boolean b3 = (this.top == s.top);
			final boolean b4 = (this.bottom == s.bottom);
			return b0 && b1 && b2 && b3 && b4;
		}

		return false;
	}

}
