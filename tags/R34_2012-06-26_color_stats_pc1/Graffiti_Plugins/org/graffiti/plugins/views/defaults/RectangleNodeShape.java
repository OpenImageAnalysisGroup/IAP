// ==============================================================================
//
// RectangleNodeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: RectangleNodeShape.java,v 1.1 2011-01-31 09:03:29 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.util.GraphicHelper;

/**
 * Class representing a rectangle.
 * 
 * @version $Revision: 1.1 $
 */
public class RectangleNodeShape
					extends RectangularNodeShape {
	// ~ Instance fields ========================================================
	
	/**
	 * The <code>Rectangle2D</code> that is represented by this <code>NodeShape</code>.
	 */
	private RectangularShape rect2D;
	protected int offX = 0;
	protected int offY = 0;
	protected int addSx = 0;
	protected int addSy = 0;
	
	// ~ Constructors ===========================================================
	
	/**
	 * The constructor creates a rectangle using default values.
	 */
	public RectangleNodeShape() {
		this.rect2D = new Rectangle2D.Double(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		this.thickShape = new Rectangle2D.Double();
		((RectangularShape) this.thickShape).setFrame(rect2D.getX(), rect2D.getY(), rect2D.getWidth(), rect2D.getHeight());
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.view.NodeShape#getIntersection(Line2D)
	 */
	public Point2D getIntersection(Line2D line) {
		Rectangle2D rect = getRealBounds2D();
		
		double rounding = getRounding() / 2;
		
		return getIntersectionOfRoundRectangleAndLine(line, rect, rounding);
	}
	
	protected double getRounding() {
		return nodeAttr.getRoundedEdges();
	}
	
	public static Point2D getIntersectionOfRoundRectangleAndLine(Line2D line,
						Rectangle2D rect, double rounding) {
		
		rounding = Math.abs(rounding);
		
		double width = rect.getWidth();
		double height = rect.getHeight();
		
		// the four corners of the encapsulated rectangle
		Point2D upperLeft = new Point2D.Double(rect.getX(), rect.getY());
		Point2D lowerLeft = new Point2D.Double(rect.getX(),
							upperLeft.getY() + height);
		Point2D upperRight = new Point2D.Double(upperLeft.getX() + width,
							rect.getY());
		Point2D lowerRight = new Point2D.Double(upperLeft.getX() + width,
							upperLeft.getY() + height);
		
		// turn the rectangle into 4 lines
		// and test which one intersects with the given line
		Line2D left = new Line2D.Double(upperLeft, lowerLeft);
		Line2D bottom = new Line2D.Double(lowerLeft, lowerRight);
		Line2D right = new Line2D.Double(upperRight, lowerRight);
		Line2D top = new Line2D.Double(upperLeft, upperRight);
		
		// testing with which side of the rectangle the given line intersects
		// and then computing the intersection point
		if (left.intersectsLine(line) && (left.getX1() != line.getX1())) {
			return checkEllipsesIntersections(GraphicHelper.getIntersection(left, line), line, Line.LEFT, rect, rounding);
		} else
			if (bottom.intersectsLine(line) &&
								(bottom.getY1() != line.getY1())) {
				return checkEllipsesIntersections(GraphicHelper.getIntersection(bottom, line), line, Line.BOTTOM, rect, rounding);
			} else
				if (right.intersectsLine(line) && (right.getX1() != line.getX1())) {
					return checkEllipsesIntersections(GraphicHelper.getIntersection(right, line), line, Line.RIGHT, rect, rounding);
				} else
					if (top.intersectsLine(line) && (top.getY1() != line.getY1())) {
						return checkEllipsesIntersections(GraphicHelper.getIntersection(top, line), line, Line.TOP, rect, rounding);
					} else {
						return null;
					}
	}
	
	private static Point2D checkEllipsesIntersections(Point2D intersection, Line2D line, Line position, Rectangle2D rect, double rounding) {
		// identify ellipse (tl, tr, bl, br)
		double ix, iy;
		ix = intersection.getX();
		iy = intersection.getY();
		
		if (ix >= rect.getMinX() + rounding && ix <= rect.getMaxX() - rounding)
			return intersection;
		if (iy >= rect.getMinY() + rounding && iy <= rect.getMaxY() - rounding)
			return intersection;
		// System.out.println("INTERSECT "+rect.toString()+" with "+line.toString()+" => "+intersection.toString());
		if (position == Line.LEFT || position == Line.RIGHT) {
			// System.out.println("L or R");
			if (iy < rect.getCenterY()) {
				// ellipse top left or top right
				// System.out.println("TL or TR");
				if (ix < rect.getCenterX()) {
					// TOP LEFT
					// System.out.println(" - TL");
					return getEllipseIntersection(intersection, line, Corner.TOP_LEFT, rect, rounding);
				} else {
					// TOP RIGHT
					// System.out.println(" - TR");
					return getEllipseIntersection(intersection, line, Corner.TOP_RIGHT, rect, rounding);
				}
			} else {
				// ellipse bottom left or bottom right
				// System.out.println("BL or BR");
				if (ix < rect.getCenterX()) {
					// BOTTOM LEFT
					// System.out.println(" - BL");
					return getEllipseIntersection(intersection, line, Corner.BOTTOM_LEFT, rect, rounding);
				} else {
					// BOTTOM RIGHT
					// System.out.println(" - BR");
					return getEllipseIntersection(intersection, line, Corner.BOTTOM_RIGHT, rect, rounding);
				}
			}
		} else {
			// Line.TOP or Line.BOTTOM
			// System.out.println("T or B");
			if (ix < rect.getCenterX()) {
				// ellipse top left or bottom left
				if (iy < rect.getCenterY()) {
					// TOP LEFT
					// System.out.println("TL");
					return getEllipseIntersection(intersection, line, Corner.TOP_LEFT, rect, rounding);
				} else {
					// BOTTOM LEFT
					// System.out.println("BL");
					return getEllipseIntersection(intersection, line, Corner.BOTTOM_LEFT, rect, rounding);
				}
			} else {
				// ellipse top right or bottom right
				if (iy < rect.getCenterY()) {
					// TOP RIGHT
					// System.out.println("TR");
					return getEllipseIntersection(intersection, line, Corner.TOP_RIGHT, rect, rounding);
				} else {
					// BOTTOM RIGHT
					// System.out.println("BR");
					return getEllipseIntersection(intersection, line, Corner.BOTTOM_RIGHT, rect, rounding);
				}
			}
		}
	}
	
	private static Point2D getEllipseIntersection(Point2D intersection, Line2D line, Corner corner, Rectangle2D rect, double rounding) {
		double xp = Double.NaN;
		double yp = Double.NaN;
		switch (corner) {
			case TOP_LEFT:
				xp = rect.getMinX() + rounding;
				yp = rect.getMinY() + rounding;
				break;
			case TOP_RIGHT:
				xp = rect.getMaxX() - rounding;
				yp = rect.getMinY() + rounding;
				break;
			case BOTTOM_LEFT:
				xp = rect.getMinX() + rounding;
				yp = rect.getMaxY() - rounding;
				break;
			case BOTTOM_RIGHT:
				xp = rect.getMaxX() - rounding;
				yp = rect.getMaxY() - rounding;
				break;
		}
		Ellipse2D e = new Ellipse2D.Double(xp - rounding, yp - rounding, rounding * 2, rounding * 2);
		Point2D[] points = CircularNodeShape.getIntersectionsWithCircle(e, line);
		if (points == null || points.length == 0)
			return intersection;
		if (points.length == 1)
			return points[0];
		if (points.length == 2) {
			// calculate distance to center of rectangle
			// return hit more far away
			double d1 = points[0].distance(rect.getCenterX(), rect.getCenterY());
			double d2 = points[1].distance(rect.getCenterX(), rect.getCenterY());
			if (d2 > d1)
				return points[1];
			else
				return points[0];
		}
		return intersection;
	}
	
	@Override
	public PathIterator getPathIterator(AffineTransform t, double d) {
		return this.rect2D.getPathIterator(t, d);
	}
	
	@Override
	public PathIterator getPathIterator(AffineTransform t) {
		return this.rect2D.getPathIterator(t);
	}
	
	/**
	 * This method sets all necessary properties using the values contained
	 * within the <code>CollectionAttribute</code>. This includes
	 * 
	 * @param graphics
	 *           The attribute that contains all necessary information to
	 *           construct a rectangle.
	 */
	public void buildShape(NodeGraphicAttribute graphics) {
		this.nodeAttr = graphics;
		
		DimensionAttribute dim = graphics.getDimension();
		double w = dim.getWidth();
		double h = dim.getHeight();
		
		double ft = Math.floor(graphics.getFrameThickness());
		double offset = ft / 2d;
		double x = Math.floor(offset);
		double y = Math.floor(offset);
		
		double rounding = getRounding();
		if (Math.abs(rounding) > 0.0001 && !(rect2D instanceof RoundRectangle2D)) {
			rect2D = new RoundRectangle2D.Double(x, y, w, h, rounding, rounding);
		} else
			if (Math.abs(rounding) < 0.0001 && (rect2D instanceof RoundRectangle2D)) {
				rect2D = new Rectangle2D.Double(x, y, w, h);
			} else
				if (rect2D instanceof RoundRectangle2D)
					((RoundRectangle2D) rect2D).setRoundRect(x, y, w, h, rounding, rounding);
				else
					rect2D.setFrame(x, y, w, h);
		
		rect2D.setFrame(rect2D.getX() + offX, rect2D.getY() + offY, rect2D.getWidth(), rect2D.getHeight());
		
		double corwidth = w + ft;
		double corheight = h + ft;
		
		if (Math.floor(offset) == offset) {
			corwidth = w + ft + 1;
			corheight = h + ft + 1;
		}
		
		((RectangularShape) this.thickShape).setFrame(0, 0, corwidth + addSx, corheight + addSy);
	}
	
	@Override
	public boolean contains(double a, double b, double c, double d) {
		return this.thickShape.contains(a, b, c, d);
	}
	
	@Override
	public boolean contains(double a, double b) {
		return this.thickShape.contains(a, b);
	}
	
	public int shapeHeightCorrection() {
		return 0;
	}
	
	public int shapeWidthCorrection() {
		return 0;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
