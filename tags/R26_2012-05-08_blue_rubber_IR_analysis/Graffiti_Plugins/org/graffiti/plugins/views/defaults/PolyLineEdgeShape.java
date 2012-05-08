// ==============================================================================
//
// PolyLineEdgeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PolyLineEdgeShape.java,v 1.1 2011-01-31 09:03:28 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.ShapeNotFoundException;

/**
 * Represents an edge with several segments separated by bends.
 * 
 * @version $Revision: 1.1 $
 */
public class PolyLineEdgeShape
					extends LineEdgeShape {
	// ~ Instance fields ========================================================
	
	/** the <code>Collection</code> of bends of this edge. */
	@SuppressWarnings("unchecked")
	protected Collection bends;
	
	/**
	 * Flatness value used for the <code>PathIterator</code> used to calculate
	 * contains method for non-linear edge shapes like splines.
	 */
	protected final double flatness = 1.0d;
	
	// ~ Methods ================================================================
	
	/**
	 * This method sets all necessary properties of an edge using the values
	 * contained within the <code>CollectionAttribute</code> (like
	 * coordinates etc.). It also uses information about ports. It attaches
	 * arrows if there are any.
	 * 
	 * @param edgeAttr
	 *           the attribute that contains all necessary information to
	 *           construct a line.
	 * @param sourceShape
	 *           DOCUMENT ME!
	 * @param targetShape
	 *           DOCUMENT ME!
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	@Override
	public void buildShape(EdgeGraphicAttribute edgeAttr,
						NodeShape sourceShape, NodeShape targetShape)
						throws ShapeNotFoundException {
		this.graphicsAttr = edgeAttr;
		
		// docking
		Point2D start = getSourceDockingCoords(edgeAttr, sourceShape);
		Point2D end = getTargetDockingCoords(edgeAttr, targetShape);
		
		LinkedHashMapAttribute bendsCollection = (LinkedHashMapAttribute) edgeAttr.getBends();
		this.bends = ((LinkedHashMap<?, ?>) bendsCollection.getCollection()).values();
		
		Point2D first = null;
		Point2D last = null;
		
		if (this.bends.isEmpty()) {
			// can we use StraightLineEdgeShape here in some way?
			// StraightLineEdgeShape straight = new StraightLineEdgeShape();
			// straight.buildShape(edgeAttr, sourceShape, targetShape);
			first = end;
			last = start;
			this.line2D.setLine(start, end);
			
			Point2D newStart = null;
			if (!(start instanceof Point2Dfix))
				newStart = sourceShape.getIntersection(this.line2D);
			this.line2D.setLine(end, start);
			
			Point2D newEnd = null;
			if (!(end instanceof Point2Dfix))
				newEnd = targetShape.getIntersection(this.line2D);
			
			if (newStart != null) {
				start = newStart;
			}
			
			if (newEnd != null) {
				end = newEnd;
			}
			
			start = attachSourceArrow(edgeAttr, start, end);
			end = attachTargetArrow(edgeAttr, end, start);
			
			this.linePath = new GeneralPath(new Line2D.Double(start, end));
		} else {
			// have some bend(s)
			Iterator<?> it = this.bends.iterator();
			first = ((CoordinateAttribute) it.next()).getCoordinate();
			this.line2D.setLine(start, first);
			
			Point2D newStart = null;
			
			if (!(start instanceof Point2Dfix))
				newStart = sourceShape.getIntersection(this.line2D);
			
			if (newStart != null) {
				start = newStart;
			}
			
			start = attachSourceArrow(edgeAttr, start, first);
			
			this.linePath.moveTo((float) start.getX(), (float) start.getY());
			this.linePath.lineTo((float) first.getX(), (float) first.getY());
			
			if (!it.hasNext()) {
				// only one bend
				this.line2D.setLine(end, first);
				
				Point2D newEnd = null;
				if (!(end instanceof Point2Dfix))
					newEnd = targetShape.getIntersection(this.line2D);
				
				if (newEnd != null) {
					end = newEnd;
				}
				
				end = attachTargetArrow(edgeAttr, end, first);
			} else {
				Point2D bend;
				
				while (it.hasNext()) {
					bend = ((CoordinateAttribute) it.next()).getCoordinate();
					
					if (!it.hasNext()) {
						// last bend
						last = bend;
						this.line2D.setLine(end, last);
						
						Point2D newEnd = null;
						if (!(end instanceof Point2Dfix))
							newEnd = targetShape.getIntersection(this.line2D);
						
						if (newEnd != null) {
							end = newEnd;
						}
						
						end = attachTargetArrow(edgeAttr, end, last);
					}
					
					// still to go
					this.linePath.lineTo((float) bend.getX(),
										(float) bend.getY());
				}
			}
			
			this.linePath.lineTo((float) end.getX(), (float) end.getY());
		}
		
		realBounds = this.linePath.getBounds2D();
		realBounds = addThickBounds(this.linePath, edgeAttr);
		
		if (getHeadArrow() != null) {
			this.realBounds.add(
								StandardArrowShape.addThickness(getHeadArrow().getBounds2D(), edgeAttr.getFrameThickness()));
		}
		
		if (getTailArrow() != null) {
			this.realBounds.add(
								StandardArrowShape.addThickness(getTailArrow().getBounds2D(), edgeAttr.getFrameThickness()));
		}
		
		AffineTransform at = new AffineTransform();
		at.setToTranslation(-realBounds.getX(), -realBounds.getY());
		this.headArrow = at.createTransformedShape(this.headArrow);
		this.tailArrow = at.createTransformedShape(this.tailArrow);
		this.linePath = new GeneralPath(this.linePath.createTransformedShape(at));
	}
	
	/**
	 * Returns true if the edge has been hit.
	 * 
	 * @param x
	 *           x coordinate relative to the coordinates of this shape.
	 * @param y
	 *           y coordinate relative to the coordinates of this shape.
	 * @return true if the edge has been hit else false.
	 */
	@Override
	public boolean contains(double x, double y) {
		// could probably be optimized ...
		return pathContains(this.linePath, x + 1, y + 1);
		
		// // TODO: FIX nullpointerexception and general check
		// double maxDist = LINE_TOLERANCE;
		// double lineBreadth = graphicsAttr.getThickness() +
		// graphicsAttr.getFrameThickness();
		// if (lineBreadth > maxDist) {
		// maxDist = lineBreadth/2d;
		// }
		//
		// PathIterator pit = this.linePath.getPathIterator(null);
		// double[] seg = new double[6];
		// pit.currentSegment(seg);
		// Point2D beforeNext = new Point2D.Double(seg[0], seg[1]);
		// Point2D next = null;
		// Line2D line = null;
		//
		// try {
		// while (!pit.isDone()) {
		// pit.next();
		// pit.currentSegment(seg);
		// next = new Point2D.Double(seg[0], seg[1]);
		// line.setLine(beforeNext, next);
		// if (line.ptLineDist(x, y) < maxDist) {
		// return true;
		// }
		// beforeNext = next;
		// }
		// } catch (NoSuchElementException nsee) {
		// } catch (NullPointerException npe) {}
		//
		// return false;
		// // return this.linePath.contains(x, y);
	}
	
	// /**
	// * Returns the <code>CoordinateAttribute</code> that represents the bend
	// * that is near the coordinates <code>x</code>, <code>y</code>.
	// * It returns null if no bend is near.
	// *
	// * @param x x coordinate relative to the coordinates of this shape.
	// * @param y y coordinate relative to the coordinates of this shape.
	// *
	// * @return the <code>CoordinateAttribute</code> of the bend hit or null if
	// * no bend was hit.
	// */
	// public CoordinateAttribute bendHit(double x, double y) {
	// // TODO: FIX nullpointerexception and general check
	// double maxDist = LINE_TOLERANCE;
	//
	// Iterator it = this.bends.iterator();
	// Point2D bend;
	// while (it.hasNext()) {
	// CoordinateAttribute coordAttr = (CoordinateAttribute) it.next();
	// bend = coordAttr.getCoordinate();
	// if (bend.distance(x, y) < maxDist) {
	// return coordAttr;
	// }
	// }
	// return null;
	// }
	
	/**
	 * Calculates if a given <code>GeneralPath</code> object contains the given
	 * point. It approximates the path using a <code>FlatteningPathIterator</code> and uses the method <code>lineContains</code> that uses a certain tolerance.
	 * 
	 * @param path
	 * @param x
	 * @param y
	 * @return true is point is near to the <code>path</code> object
	 */
	protected boolean pathContains(GeneralPath path, double x, double y) {
		// System.out.println("pc----------------------------");
		PathIterator pi = path.getPathIterator(null, 10d);
		double[] seg = new double[6];
		int type = pi.currentSegment(seg);
		
		Point2D veryfirst = new Point2D.Double(seg[0], seg[1]);
		
		Point2D start = veryfirst;
		Point2D end = null;
		
		// GeneralPath newGP = new GeneralPath(path);
		try {
			while (!pi.isDone()) {
				// System.out.println("type " + type + "  ");
				switch (type) {
					case PathIterator.SEG_MOVETO:
						start = new Point2D.Double(seg[0], seg[1]);
						
						end = null;
						
						break;
					
					case PathIterator.SEG_LINETO:
						end = new Point2D.Double(seg[0], seg[1]);
						
						break;
					
					case PathIterator.SEG_QUADTO:
						end = new Point2D.Double(seg[2], seg[3]);
						
						break;
					
					case PathIterator.SEG_CUBICTO:
						end = new Point2D.Double(seg[4], seg[5]);
						
						break;
					
					case PathIterator.SEG_CLOSE:
						end = veryfirst;
						
						break;
				}
				
				if (end != null) {
					if (lineContains(new Line2D.Double(start, end), x, y)) {
						return true;
					}
					
					start = end;
				}
				
				pi.next();
				type = pi.currentSegment(seg);
			}
		} catch (NoSuchElementException nsee) {
		}
		
		// this.linePath = newGP;
		return false;
	}
	
	public int getIndexOfPathWhichContains(double x, double y) {
		
		GeneralPath path = linePath;
		
		x -= realBounds.getX();
		y -= realBounds.getY();
		
		// System.out.println("pc----------------------------");
		PathIterator pi = path.getPathIterator(null, 10d);
		double[] seg = new double[6];
		int type = pi.currentSegment(seg);
		
		Point2D veryfirst = new Point2D.Double(seg[0], seg[1]);
		
		Point2D start = veryfirst;
		Point2D end = null;
		
		int res = 0;
		
		// GeneralPath newGP = new GeneralPath(path);
		try {
			while (!pi.isDone()) {
				// System.out.println("type " + type + "  ");
				switch (type) {
					case PathIterator.SEG_MOVETO:
						start = new Point2D.Double(seg[0], seg[1]);
						
						end = null;
						
						break;
					
					case PathIterator.SEG_LINETO:
						end = new Point2D.Double(seg[0], seg[1]);
						
						break;
					
					case PathIterator.SEG_QUADTO:
						end = new Point2D.Double(seg[2], seg[3]);
						
						break;
					
					case PathIterator.SEG_CUBICTO:
						end = new Point2D.Double(seg[4], seg[5]);
						
						break;
					
					case PathIterator.SEG_CLOSE:
						end = veryfirst;
						
						break;
				}
				
				if (end != null) {
					if (lineContains(new Line2D.Double(start, end), x, y)) {
						return res - 1;
					}
					
					start = end;
				}
				
				pi.next();
				type = pi.currentSegment(seg);
				res++;
			}
		} catch (NoSuchElementException nsee) {
		}
		
		// this.linePath = newGP;
		return -1;
	}
	
	public void setCoordinateSystem(CoordinateSystem coordinates) {
		//
		
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
