// ==============================================================================
//
// PolygonalNodeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PolygonalNodeShape.java,v 1.1 2011-01-31 09:03:28 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.graphics.ShapeDescriptionAttribute;
import org.graffiti.util.GraphicHelper;

/**
 * DOCUMENT ME!
 */
public class PolygonalNodeShape
					extends RectangularNodeShape {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	protected Polygon polygon;
	protected HashSet<Integer> ignorePoints;
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Rectangle getBounds() {
		return this.thickShape.getBounds();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Rectangle2D getBounds2D() {
		return this.thickShape.getBounds2D();
	}
	
	/**
	 * @see org.graffiti.plugin.view.NodeShape#getIntersection(Line2D)
	 */
	public Point2D getIntersection(Line2D line) {
		Polygon workPolygon = new Polygon(polygon.xpoints, polygon.ypoints,
							polygon.npoints);
		Rectangle2D bounds = getRealBounds2D();
		workPolygon.translate((int) bounds.getX(), (int) bounds.getY());
		
		int nr = workPolygon.npoints;
		Line2D polyLine;
		List<Point2D> intPoints = new LinkedList<Point2D>();
		
		for (int pt = 0; pt < (nr - 1); pt++) {
			if (ignorePoints != null && ignorePoints.contains(pt))
				continue;
			polyLine = new Line2D.Double(workPolygon.xpoints[pt],
								workPolygon.ypoints[pt], workPolygon.xpoints[pt + 1],
								workPolygon.ypoints[pt + 1]);
			
			if (polyLine.intersectsLine(line)) {
				intPoints.add(GraphicHelper.getIntersection(polyLine, line));
			}
		}
		
		// for closed polygon:
		polyLine = new Line2D.Double(workPolygon.xpoints[nr - 1],
							workPolygon.ypoints[nr - 1], workPolygon.xpoints[0],
							workPolygon.ypoints[0]);
		
		if (polyLine.intersectsLine(line)) {
			intPoints.add(GraphicHelper.getIntersection(polyLine, line));
		}
		
		if (intPoints.isEmpty()) {
			return null;
		}
		
		Point2D pnt;
		
		// per convention, the second point is the outer one:
		Point2D outer = line.getP2();
		double minDist = Double.MAX_VALUE;
		Point2D minIntPoint = null;
		double dist;
		
		for (Iterator<Point2D> it = intPoints.iterator(); it.hasNext();) {
			pnt = (Point2D) it.next();
			dist = pnt.distance(outer);
			
			if (dist < minDist) {
				minDist = dist;
				minIntPoint = pnt;
			}
		}
		
		// if (minIntPoint!=null)
		// minIntPoint = new Point2D.Double(minIntPoint.getX()+bounds.getX()-Math.floor(bounds.getX()),
		// minIntPoint.getY()+bounds.getY()-Math.floor(bounds.getY()));
		return minIntPoint;
	}
	
	/**
	 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform)
	 */
	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return polygon.getPathIterator(at);
	}
	
	/**
	 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform, double)
	 */
	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return polygon.getPathIterator(at, flatness);
	}
	
	/**
	 * @see org.graffiti.plugin.view.NodeShape#buildShape(NodeGraphicAttribute)
	 */
	@SuppressWarnings("unchecked")
	public void buildShape(NodeGraphicAttribute graphics) {
		this.nodeAttr = graphics;
		
		DimensionAttribute dim = graphics.getDimension();
		double w = dim.getWidth();
		double h = dim.getHeight();
		
		double ft = graphics.getFrameThickness();
		double offset = ft / 2d;
		int os = (int) Math.floor(offset);
		
		ShapeDescriptionAttribute desc;
		
		try {
			desc = (ShapeDescriptionAttribute) graphics.getAttribute(GraphicAttributeConstants.SHAPEDESCRIPTION);
		} catch (AttributeNotFoundException anfe) {
			polygon = getStandardPolygon();
			setThickShape(w + ft, h + ft);
			
			return;
		} catch (ClassCastException cce) {
			polygon = getStandardPolygon();
			setThickShape(w + ft, h + ft);
			
			return;
		}
		
		Map<?, ?> map = desc.getCollection();
		int nr = map.entrySet().size();
		int[] xcoord = new int[nr];
		int[] ycoord = new int[nr];
		
		int i = 0;
		Point2D pt;
		
		for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
			pt = ((CoordinateAttribute) ((Map.Entry) it.next()).getValue()).getCoordinate();
			
			// xcoord[i] = (int)(pt.getX() + os);
			// ycoord[i] = (int)(pt.getY() + os);
			xcoord[i] = (int) (pt.getX());
			ycoord[i] = (int) (pt.getY());
			i++;
		}
		
		polygon = new Polygon(xcoord, ycoord, nr);
		polygon.translate(os, os);
		
		Rectangle bounds = polygon.getBounds();
		
		os = (int) Math.ceil(offset);
		setThickShape(bounds.getWidth() + os, bounds.getHeight() + os);
	}
	
	/**
	 * @see java.awt.Shape#contains(double, double)
	 */
	@Override
	public boolean contains(double x, double y) {
		return polygon.contains(x, y);
	}
	
	/**
	 * @see java.awt.Shape#contains(double, double, double, double)
	 */
	@Override
	public boolean contains(double x, double y, double w, double h) {
		return polygon.contains(x, y, w, h);
	}
	
	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return this.polygon.intersects(x, y, w, h);
	}
	
	@Override
	public boolean intersects(Rectangle2D r) {
		return this.polygon.intersects(r);
	}
	
	private Polygon getStandardPolygon() {
		Polygon polygon = new Polygon();
		polygon.addPoint(0, 20);
		polygon.addPoint(20, 20);
		polygon.addPoint(10, 0);
		
		return polygon;
	}
	
	protected void setThickShape(double w, double h) {
		this.thickShape = new Rectangle2D.Double(0, 0, w, h);
	}
	
	public int shapeHeightCorrection() {
		return 0;
	}
	
	public int shapeWidthCorrection() {
		return 0;
	}
	
	public Polygon getIgnorePolygon() {
		int np = ignorePoints != null ? ignorePoints.size() + 1 : 0;
		if (np == 0)
			return null;
		int[] xp = new int[np];
		int[] yp = new int[np];
		int i = 0;
		int idx = 0;
		for (int idx2 : ignorePoints) {
			idx = idx2;
			xp[i] = polygon.xpoints[idx];
			yp[i] = polygon.ypoints[idx];
			i++;
		}
		xp[i] = polygon.xpoints[idx + 1];
		yp[i] = polygon.ypoints[idx + 1];
		Polygon res = new Polygon(xp, yp, np);
		return res;
	}
}
