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
 * AxisSpace.java
 * --------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: AxisSpace.java,v 1.1 2011-01-31 09:01:39 klukas Exp $
 * Changes
 * -------
 * 03-Jul-2003 : Version 1 (DG);
 * 14-Aug-2003 : Implemented Cloneable (DG);
 * 18-Aug-2003 : Implemented Serializable (DG);
 * 17-Mar-2004 : Added a toString() method for debugging (DG);
 */

package org.jfree.chart.axis;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

/**
 * A record that contains the space required at each edge of a plot.
 */
public class AxisSpace implements Cloneable, PublicCloneable, Serializable {

	/** The top space. */
	private double top;

	/** The bottom space. */
	private double bottom;

	/** The left space. */
	private double left;

	/** The right space. */
	private double right;

	/**
	 * Creates a new axis space record.
	 */
	public AxisSpace() {
		this.top = 0.0;
		this.bottom = 0.0;
		this.left = 0.0;
		this.right = 0.0;
	}

	/**
	 * Returns the top space.
	 * 
	 * @return The top space.
	 */
	public double getTop() {
		return this.top;
	}

	/**
	 * Sets the top space.
	 * 
	 * @param space
	 *           the space.
	 */
	public void setTop(double space) {
		this.top = space;
	}

	/**
	 * Returns the bottom space.
	 * 
	 * @return The bottom space.
	 */
	public double getBottom() {
		return this.bottom;
	}

	/**
	 * Sets the bottom space.
	 * 
	 * @param space
	 *           the space.
	 */
	public void setBottom(double space) {
		this.bottom = space;
	}

	/**
	 * Returns the left space.
	 * 
	 * @return The left space.
	 */
	public double getLeft() {
		return this.left;
	}

	/**
	 * Sets the left space.
	 * 
	 * @param space
	 *           the space.
	 */
	public void setLeft(double space) {
		this.left = space;
	}

	/**
	 * Returns the right space.
	 * 
	 * @return The right space.
	 */
	public double getRight() {
		return this.right;
	}

	/**
	 * Sets the right space.
	 * 
	 * @param space
	 *           the space.
	 */
	public void setRight(double space) {
		this.right = space;
	}

	/**
	 * Adds some space to the edge corresponding to the specified axis location.
	 * 
	 * @param space
	 *           the space.
	 * @param edge
	 *           the location.
	 */
	public void add(double space, RectangleEdge edge) {
		if (edge == RectangleEdge.TOP) {
			this.top += space;
		} else
			if (edge == RectangleEdge.BOTTOM) {
				this.bottom += space;
			} else
				if (edge == RectangleEdge.LEFT) {
					this.left += space;
				} else
					if (edge == RectangleEdge.RIGHT) {
						this.right += space;
					} else {
						throw new IllegalStateException("AxisSpace.add(...): unrecognised RectangleEdge.");
					}
	}

	/**
	 * Ensures that this object reserves at least as much space as another.
	 * 
	 * @param space
	 *           the other space.
	 */
	public void ensureAtLeast(AxisSpace space) {
		this.top = Math.max(this.top, space.top);
		this.bottom = Math.max(this.bottom, space.bottom);
		this.left = Math.max(this.left, space.left);
		this.right = Math.max(this.right, space.right);
	}

	/**
	 * Ensures there is a minimum amount of space at the edge corresponding to the specified
	 * axis location.
	 * 
	 * @param space
	 *           the space.
	 * @param edge
	 *           the location.
	 */
	public void ensureAtLeast(double space, RectangleEdge edge) {
		if (edge == RectangleEdge.TOP) {
			if (this.top < space) {
				this.top = space;
			}
		} else
			if (edge == RectangleEdge.BOTTOM) {
				if (this.bottom < space) {
					this.bottom = space;
				}
			} else
				if (edge == RectangleEdge.LEFT) {
					if (this.left < space) {
						this.left = space;
					}
				} else
					if (edge == RectangleEdge.RIGHT) {
						if (this.right < space) {
							this.right = space;
						}
					} else {
						throw new IllegalStateException(
											"AxisSpace.ensureAtLeast(...): unrecognised AxisLocation.");
					}
	}

	/**
	 * Shrinks an area by the space attributes.
	 * 
	 * @param area
	 *           the area to shrink.
	 * @param result
	 *           an optional carrier for the result.
	 * @return The result.
	 */
	public Rectangle2D shrink(Rectangle2D area, Rectangle2D result) {
		if (result == null) {
			result = new Rectangle2D.Double();
		}
		result.setRect(
							area.getX() + this.left,
							area.getY() + this.top,
							area.getWidth() - this.left - this.right,
							area.getHeight() - this.top - this.bottom
							);
		return result;
	}

	/**
	 * Shrinks an area's left and right edges by the amount of this objects left and right settings.
	 * 
	 * @param area
	 *           the area to shrink.
	 * @param result
	 *           an optional carrier for the result.
	 * @return the result.
	 * @deprecated This method is no longer required.
	 */
	public Rectangle2D shrinkLeftAndRight(Rectangle2D area, Rectangle2D result) {
		if (result == null) {
			result = new Rectangle2D.Double();
		}
		result.setRect(
							area.getX() + this.left,
							area.getY(),
							area.getWidth() - this.left - this.right,
							area.getHeight()
							);
		return result;
	}

	/**
	 * Shrinks an area's top and bottom edges by the amount of this objects top and bottom settings.
	 * 
	 * @param area
	 *           the area to shrink.
	 * @param result
	 *           an optional carrier for the result.
	 * @return the result.
	 * @deprecated This method is no longer required.
	 */
	public Rectangle2D shrinkTopAndBottom(Rectangle2D area, Rectangle2D result) {
		if (result == null) {
			result = new Rectangle2D.Double();
		}
		result.setRect(
							area.getX(),
							area.getY() + this.top,
							area.getWidth(),
							area.getHeight() - this.top - this.bottom
							);
		return result;
	}

	/**
	 * Expands an area by the amount of space represented by this object.
	 * 
	 * @param area
	 *           the area to expand.
	 * @param result
	 *           an optional carrier for the result.
	 * @return The result.
	 */
	public Rectangle2D expand(Rectangle2D area, Rectangle2D result) {
		if (result == null) {
			result = new Rectangle2D.Double();
		}
		result.setRect(
							area.getX() - this.left,
							area.getY() - this.top,
							area.getWidth() + this.left + this.right,
							area.getHeight() + this.top + this.bottom
							);
		return result;
	}

	/**
	 * Calculates the reserved area.
	 * 
	 * @param area
	 *           the area.
	 * @param edge
	 *           the edge.
	 * @return The reserved area.
	 */
	public Rectangle2D reserved(Rectangle2D area, RectangleEdge edge) {
		Rectangle2D result = null;
		if (edge == RectangleEdge.TOP) {
			result = new Rectangle2D.Double(area.getX(), area.getY(),
															area.getWidth(), this.top);
		} else
			if (edge == RectangleEdge.BOTTOM) {
				result = new Rectangle2D.Double(area.getX(), area.getMaxY() - this.top,
															area.getWidth(), this.bottom);
			} else
				if (edge == RectangleEdge.LEFT) {
					result = new Rectangle2D.Double(area.getX(), area.getY(),
															this.left, area.getHeight());
				} else
					if (edge == RectangleEdge.RIGHT) {
						result = new Rectangle2D.Double(area.getMaxX() - this.right, area.getY(),
															this.right, area.getHeight());
					}
		return result;
	}

	/**
	 * Returns a clone of the object.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            This class won't throw this exception, but subclasses
	 *            (if any) might.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Tests this object for equality with another object.
	 * 
	 * @param object
	 *           the object to compare against.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (object instanceof AxisSpace) {
			AxisSpace s = (AxisSpace) object;
			boolean b0 = (this.top == s.top);
			boolean b1 = (this.bottom == s.bottom);
			boolean b2 = (this.left == s.left);
			boolean b3 = (this.right == s.right);
			return b0 && b1 && b2 && b3;
		}

		return false;

	}

	/**
	 * Returns a string representing the object (for debugging purposes).
	 * 
	 * @return a string.
	 */
	public String toString() {
		return super.toString() + "[left=" + this.left + ",right=" + this.right
											+ ",top=" + this.top + ",bottom=" + this.bottom + "]";
	}

}
