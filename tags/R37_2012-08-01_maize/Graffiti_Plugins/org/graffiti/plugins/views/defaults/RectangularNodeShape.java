// ==============================================================================
//
// RectangularNodeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: RectangularNodeShape.java,v 1.1 2011-01-31 09:03:28 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.NodeShape;

/**
 * An implementation of <code>NodeShape</code> representing rectangular shapes.
 */
public abstract class RectangularNodeShape
					extends AbstractArrowShape
					implements NodeShape {
	// ~ Instance fields ========================================================
	
	/** The standard height of the recangular shape. */
	protected final double DEFAULT_HEIGHT = SIZE;
	
	/** The standard width of the recangular shape. */
	protected final double DEFAULT_WIDTH = SIZE;
	
	/** The graphic attribute of the node this shape represents. */
	protected NodeGraphicAttribute nodeAttr;
	
	/** The bounds including frameThickness. */
	protected Shape thickShape = new RoundRectangle2D.Double(0, 0, DEFAULT_WIDTH,
						DEFAULT_HEIGHT, 0, 0);
	
	private CoordinateSystem coordinateSystem = CoordinateSystem.XY;
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Rectangle getBounds() {
		return thickShape.getBounds();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Rectangle2D getBounds2D() {
		return thickShape.getBounds2D();
	}
	
	/**
	 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform)
	 */
	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return thickShape.getPathIterator(at);
	}
	
	/**
	 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform, double)
	 */
	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return thickShape.getPathIterator(at, flatness);
	}
	
	/**
	 * Returns a <code>Rectangle2D</code> that represents the encapsulated <code>Rectangle2D</code>, but has coordinates transformed relative to
	 * the view (instead of relative to the <code>NodeComponent</code>).
	 * 
	 * @return a copy of the encapsulated rectangle but which has the real
	 *         coordinates in the view.
	 */
	public Rectangle2D getRealBounds2D() {
		Point2D coord = this.nodeAttr.getCoordinate().getCoordinate();
		Rectangle2D rect = this.getBounds2D();
		double w = rect.getWidth();
		double h = rect.getHeight();
		
		// double ft = this.nodeAttr.getFrameThickness();
		// double realX = coord.getX() - w/2d - ft/2d;
		// double realY = coord.getY() - h/2d - ft/2d;
		
		double realX;
		double realY;
		double z;
		
		switch (coordinateSystem) {
			case XZ:
				z = AttributeHelper.getPositionZ((Node) nodeAttr.getAttributable(), 0d, true);
				realX = coord.getX() - (w / 2d);
				realY = z - (h / 2d);
				break;
			case ZY:
				z = AttributeHelper.getPositionZ((Node) nodeAttr.getAttributable(), 0d, true);
				realX = z - (h / 2d);
				realY = coord.getY() - (w / 2d);
				break;
			default: // XY
				realX = coord.getX() - (w / 2d);
				realY = coord.getY() - (h / 2d);
				break;
		}
		
		// return new Rectangle2D.Double(realX, realY, w + ft, h + ft);
		return new Rectangle2D.Double(realX, realY, w, h);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param p
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean contains(Point2D p) {
		return this.contains(p.getX(), p.getY());
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param r
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean contains(Rectangle2D r) {
		return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	
	/**
	 * @see java.awt.Shape#contains(double, double)
	 */
	@Override
	public boolean contains(double x, double y) {
		return thickShape.contains(x, y);
	}
	
	/**
	 * @see java.awt.Shape#contains(double, double, double, double)
	 */
	@Override
	public boolean contains(double x, double y, double w, double h) {
		return thickShape.contains(x, y, w, h);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param x
	 *           DOCUMENT ME!
	 * @param y
	 *           DOCUMENT ME!
	 * @param w
	 *           DOCUMENT ME!
	 * @param h
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return this.thickShape.intersects(x, y, w, h);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param r
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean intersects(Rectangle2D r) {
		return this.thickShape.intersects(r);
	}
	
	public void setCoordinateSystem(CoordinateSystem coordinates) {
		this.coordinateSystem = coordinates;
	}
	
	public double getXexcess() {
		return 0;
	}
	
	public double getYexcess() {
		return 0;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
