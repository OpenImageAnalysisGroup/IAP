// ==============================================================================
//
// SmoothLineEdgeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SmoothLineEdgeShape.java,v 1.1 2011-01-31 09:03:28 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.ShapeNotFoundException;

/**
 * A class that represents line shapes that are "smooth" in the sense GML uses
 * it.
 * 
 * @version $Revision: 1.1 $
 */
public class SmoothLineEdgeShape
					extends QuadCurveEdgeShape {
	// ~ Instance fields ========================================================
	
	/** Saves if the bends have to be modified or not. */
	private boolean mustExpandBends;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Default constructor. Used to ensure that next time <code>buildShape</code> is called, the bends are modified.
	 */
	public SmoothLineEdgeShape() {
		super();
		this.mustExpandBends = true;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * This method sets all necessary properties of an edge using the values
	 * contained within the <code>CollectionAttribute</code> (like
	 * coordinates etc.). It also uses information about ports. It attaches
	 * arrows if there are any. When <code>mustExpandBends</code> is true,
	 * i.e. it is started the very first time for an object, all line segments
	 * except the first and last ones are divided into two equally long
	 * segments. That is to ensure that the single quadric splines fit
	 * together.
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
	@SuppressWarnings("unchecked")
	@Override
	public void buildShape(EdgeGraphicAttribute edgeAttr,
						NodeShape sourceShape, NodeShape targetShape)
						throws ShapeNotFoundException {
		// System.out.println("buildshape");
		this.graphicsAttr = edgeAttr;
		
		CollectionAttribute bendsCollection = edgeAttr.getBends();
		this.bends = bendsCollection.getCollection().values();
		
		// add centers of segments (except first and last)
		if (this.mustExpandBends) {
			// System.out.println("expanding bends");
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			
			int cnt = 0;
			
			if (!this.bends.isEmpty()) {
				cnt++;
				
				Iterator<Object> it = this.bends.iterator();
				CoordinateAttribute coordAttr = (CoordinateAttribute) it.next();
				map.put(coordAttr.getId(), coordAttr.copy());
				
				CoordinateAttribute sndCoordAttr;
				
				while (it.hasNext()) {
					sndCoordAttr = (CoordinateAttribute) it.next();
					
					Point2D start = coordAttr.getCoordinate();
					Point2D end = sndCoordAttr.getCoordinate();
					Point2D center = new Point2D.Double((end.getX() +
										start.getX()) / 2d, (end.getY() + start.getY()) / 2d);
					
					// System.out.println("start: " + start);
					// System.out.println("center: " + center);
					// System.out.println("end: " + end);
					coordAttr = new CoordinateAttribute("auxBend" + cnt);
					coordAttr.setCoordinate(center);
					map.put(coordAttr.getId(), coordAttr.copy());
					map.put(sndCoordAttr.getId(), sndCoordAttr.copy());
					cnt++;
					coordAttr = sndCoordAttr;
				}
			}
			
			// ////////////////// edgeAttr.getBends().setCollection(map);
			this.bends = map.values();
			
			// System.out.println("#bends: "+map.values().size());
			// //DEBUG ONLY: draw bends
			// Iterator it = this.bends.iterator();
			// double SIZE = 10;
			// while (it.hasNext()) {
			// Point2D bend = ((CoordinateAttribute)it.next()).getCoordinate();
			// this.linePath.append
			// (new java.awt.geom.Ellipse2D.Double
			// (bend.getX()-SIZE/2d, bend.getY()-SIZE/2d, SIZE, SIZE), false);
			// }
			// ////////////////// this.mustExpandBends = false;
			
			/*
			 * this is a little bit strange isn't it ...., but otherwise the
			 * center points would be generated everytime someone calls
			 * createNewShape or something similar ....
			 */

			// ////////////////// edgeAttr.setShape("org.graffiti.plugins.views.defaults.QuadCurveEdgeShape");
		}
		
		// finished adding centers
		// docking
		Point2D start = getSourceDockingCoords(edgeAttr, sourceShape);
		Point2D end = getTargetDockingCoords(edgeAttr, targetShape);
		
		if (this.bends.isEmpty()) {
			this.line2D.setLine(start, end);
			
			Point2D newStart = null;
			if (!(start instanceof Point2Dfix))
				newStart = sourceShape.getIntersection(this.line2D);
			this.line2D.setLine(end, start); // reverse line; see "getIntersection"
			
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
			Iterator<Object> it = this.bends.iterator();
			Point2D bend = ((CoordinateAttribute) it.next()).getCoordinate();
			
			// adjust start of edge
			this.line2D.setLine(start, bend);
			
			Point2D newStart = null;
			if (!(start instanceof Point2Dfix))
				newStart = sourceShape.getIntersection(this.line2D);
			
			if (newStart != null) {
				start = newStart;
			}
			
			start = attachSourceArrow(edgeAttr, start, bend);
			
			this.linePath.moveTo((float) start.getX(), (float) start.getY());
			
			if (!it.hasNext()) {
				// only one bend
				this.line2D.setLine(end, bend);
				
				Point2D newEnd = null;
				if (!(end instanceof Point2Dfix))
					newEnd = targetShape.getIntersection(this.line2D);
				
				if (newEnd != null) {
					end = newEnd;
				}
				
				end = attachTargetArrow(edgeAttr, end, bend);
				this.linePath.quadTo((float) bend.getX(), (float) bend.getY(),
									(float) end.getX(), (float) end.getY());
			} else {
				// more than one bend
				Point2D bend2 = ((CoordinateAttribute) it.next()).getCoordinate();
				this.linePath.quadTo((float) bend.getX(), (float) bend.getY(),
									(float) bend2.getX(), (float) bend2.getY());
				
				while (it.hasNext()) {
					bend = ((CoordinateAttribute) it.next()).getCoordinate();
					
					if (it.hasNext()) {
						bend2 = ((CoordinateAttribute) it.next()).getCoordinate();
					} else {
						// last bend
						// adjust end of edge
						this.line2D.setLine(bend, end);
						
						Point2D newEnd = null;
						if (!(end instanceof Point2Dfix))
							newEnd = targetShape.getIntersection(this.line2D);
						if (newEnd != null) {
							end = newEnd;
						}
						
						end = attachTargetArrow(edgeAttr, end, bend);
						
						bend2 = end;
					}
					
					this.linePath.quadTo((float) bend.getX(),
										(float) bend.getY(), (float) bend2.getX(),
										(float) bend2.getY());
				}
			}
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
	
	// /**
	// * Returns true if the edge has been hit.
	// *
	// * @param x x coordinate relative to the coordinates of this shape.
	// * @param y y coordinate relative to the coordinates of this shape.
	// *
	// * @return true if the edge has been hit else false.
	// */
	// public boolean contains(double x, double y) {
	// return false;
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
