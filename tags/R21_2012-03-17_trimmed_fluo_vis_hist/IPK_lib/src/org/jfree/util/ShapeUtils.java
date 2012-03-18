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
 * ---------------
 * ShapeUtils.java
 * ---------------
 * (C)opyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $$
 * Changes
 * -------
 * 13-Aug-2003 : Version 1 (DG);
 * 16-Mar-2004 : Moved rotateShape() from RefineryUtilities.java to here (DG);
 * 13-May-2004 : Added new shape creation methods (DG);
 */

package org.jfree.util;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.util.Arrays;

/**
 * Utility methods for {@link Shape} objects.
 */
public abstract class ShapeUtils {

	/**
	 * Rotates a shape about the specified coordinates.
	 * 
	 * @param base
	 *           the shape (<code>null</code> permitted, returns <code>null</code>).
	 * @param angle
	 *           the angle (in radians).
	 * @param x
	 *           the x coordinate for the rotation point (in Java2D space).
	 * @param y
	 *           the y coordinate for the rotation point (in Java2D space).
	 * @return the rotated shape.
	 */
	public static Shape rotateShape(final Shape base,
												final double angle, final float x, final float y) {
		if (base == null) {
			return null;
		}
		final AffineTransform rotate = AffineTransform.getRotateInstance(angle, x, y);
		final Shape result = rotate.createTransformedShape(base);
		return result;
	}

	/**
	 * Returns a clone of the specified shape, or <code>null</code>. At the current time, this
	 * method supports cloning for instances of <code>Line2D</code>, <code>RectangularShape</code>, <code>Area</code> and <code>GeneralPath</code>.
	 * <p>
	 * <code>RectangularShape</code> includes <code>Arc2D</code>, <code>Ellipse2D</code>, <code>Rectangle2D</code>, <code>RoundRectangle2D</code>.
	 * 
	 * @param shape
	 *           the shape to clone (<code>null</code> permitted, returns <code>null</code>).
	 * @return A clone or <code>null</code>.
	 */
	public static Shape clone(final Shape shape) {
		Shape result = null;
		if (shape instanceof Line2D) {
			final Line2D line = (Line2D) shape;
			result = (Shape) line.clone();
		}
		// RectangularShape includes: Arc2D, Ellipse2D, Rectangle2D, RoundRectangle2D.
		else
			if (shape instanceof RectangularShape) {
				final RectangularShape rectangle = (RectangularShape) shape;
				result = (Shape) rectangle.clone();
			} else
				if (shape instanceof Area) {
					final Area area = (Area) shape;
					result = (Shape) area.clone();
				} else
					if (shape instanceof GeneralPath) {
						final GeneralPath path = (GeneralPath) shape;
						result = (Shape) path.clone();
					}
		return result;
	}

	/**
	 * Tests two polygons for equality. If both are <code>null</code> this method
	 * returns <code>true</code>.
	 * 
	 * @param p1
	 *           polygon 1 (<code>null</code> permitted).
	 * @param p2
	 *           polygon 2 (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public static boolean equal(final Polygon p1, final Polygon p2) {
		if (p1 == null) {
			return (p2 == null);
		}
		if (p2 == null) {
			return false;
		}
		if (p1.npoints != p2.npoints) {
			return false;
		}
		if (!Arrays.equals(p1.xpoints, p2.xpoints)) {
			return false;
		}
		if (!Arrays.equals(p1.ypoints, p2.ypoints)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a translated shape.
	 * 
	 * @param shape
	 *           the shape (<code>null</code> not permitted).
	 * @param transX
	 *           the x translation.
	 * @param transY
	 *           the y translation.
	 * @return The translated shape.
	 */
	public static Shape translateShape(final Shape shape,
													final double transX, final double transY) {
		if (shape == null) {
			throw new IllegalArgumentException("Null 'shape' argument.");
		}
		final AffineTransform transform = AffineTransform.getTranslateInstance(transX, transY);
		return transform.createTransformedShape(shape);
	}

	/** A useful constant used internally. */
	private static final float SQRT2 = (float) Math.pow(2.0, 0.5);

	/**
	 * Creates a diagonal cross shape.
	 * 
	 * @param l
	 *           the length of each 'arm'.
	 * @param t
	 *           the thickness.
	 * @return A diagonal cross shape.
	 */
	public static Shape createDiagonalCross(float l, float t) {
		GeneralPath p0 = new GeneralPath();
		p0.moveTo(-l - t, -l + t);
		p0.lineTo(-l + t, -l - t);
		p0.lineTo(0.0f, -t * SQRT2);
		p0.lineTo(l - t, -l - t);
		p0.lineTo(l + t, -l + t);
		p0.lineTo(t * SQRT2, 0.0f);
		p0.lineTo(l + t, l - t);
		p0.lineTo(l - t, l + t);
		p0.lineTo(0.0f, t * SQRT2);
		p0.lineTo(-l + t, l + t);
		p0.lineTo(-l - t, l - t);
		p0.lineTo(-t * SQRT2, 0.0f);
		p0.closePath();
		return p0;
	}

	/**
	 * Creates a diagonal cross shape.
	 * 
	 * @param l
	 *           the length of each 'arm'.
	 * @param t
	 *           the thickness.
	 * @return A diagonal cross shape.
	 */
	public static Shape createRegularCross(float l, float t) {
		GeneralPath p0 = new GeneralPath();
		p0.moveTo(-l, t);
		p0.lineTo(-t, t);
		p0.lineTo(-t, l);
		p0.lineTo(t, l);
		p0.lineTo(t, t);
		p0.lineTo(l, t);
		p0.lineTo(l, -t);
		p0.lineTo(t, -t);
		p0.lineTo(t, -l);
		p0.lineTo(-t, -l);
		p0.lineTo(-t, -t);
		p0.lineTo(-l, -t);
		p0.closePath();
		return p0;
	}

	/**
	 * Creates a diamond shape.
	 * 
	 * @param s
	 *           the size factor (equal to half the height of the diamond).
	 * @return A diamond shape.
	 */
	public static Shape createDiamond(float s) {
		GeneralPath p0 = new GeneralPath();
		p0.moveTo(0.0f, -s);
		p0.lineTo(s, 0.0f);
		p0.lineTo(0.0f, s);
		p0.lineTo(-s, 0.0f);
		p0.closePath();
		return p0;
	}

	/**
	 * Creates a triangle shape that points upwards.
	 * 
	 * @param s
	 *           the size factor (equal to half the height of the triangle).
	 * @return A triangle shape.
	 */
	public static Shape createUpTriangle(float s) {
		GeneralPath p0 = new GeneralPath();
		p0.moveTo(0.0f, -s);
		p0.lineTo(s, s);
		p0.lineTo(-s, s);
		p0.closePath();
		return p0;
	}

	/**
	 * Creates a triangle shape that points downwards.
	 * 
	 * @param s
	 *           the size factor (equal to half the height of the triangle).
	 * @return A triangle shape.
	 */
	public static Shape createDownTriangle(float s) {
		GeneralPath p0 = new GeneralPath();
		p0.moveTo(0.0f, s);
		p0.lineTo(s, -s);
		p0.lineTo(-s, -s);
		p0.closePath();
		return p0;
	}

}
