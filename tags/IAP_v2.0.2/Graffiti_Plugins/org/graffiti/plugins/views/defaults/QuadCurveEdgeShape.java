// ==============================================================================
//
// QuadCurveEdgeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: QuadCurveEdgeShape.java,v 1.1 2011-01-31 09:03:27 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.ShapeNotFoundException;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 */
public class QuadCurveEdgeShape
					extends PolyLineEdgeShape {
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
		
		// Point2D first = null;
		// Point2D last = null;
		if (this.bends.isEmpty()) {
			// no bends
			// can we use StraightLineEdgeShape here in some way?
			// StraightLineEdgeShape straight = new StraightLineEdgeShape();
			// straight.buildShape(edgeAttr, sourceShape, targetShape);
			// first = end;
			// last = start;
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
				Point2D bend2;
				
				while (it.hasNext()) {
					bend2 = ((CoordinateAttribute) it.next()).getCoordinate();
					this.linePath.quadTo((float) bend.getX(),
										(float) bend.getY(), (float) bend2.getX(),
										(float) bend2.getY());
					
					if (!it.hasNext()) {
						// "bend2" is last bend
						// adjust end of edge
						this.line2D.setLine(end, bend2);
						
						Point2D newEnd = null;
						if (!(end instanceof Point2Dfix))
							newEnd = targetShape.getIntersection(this.line2D);
						
						if (newEnd != null) {
							end = newEnd;
						}
						
						end = attachTargetArrow(edgeAttr, end, bend2);
						this.linePath.lineTo((float) end.getX(),
											(float) end.getY());
					} else {
						// have additional bends
						bend = ((CoordinateAttribute) it.next()).getCoordinate();
						
						if (!it.hasNext()) {
							// "bend" is last bend
							// adjust end of edge
							this.line2D.setLine(end, bend);
							
							Point2D newEnd = null;
							
							if (!(end instanceof Point2Dfix))
								newEnd = targetShape.getIntersection(this.line2D);
							
							if (newEnd != null) {
								end = newEnd;
							}
							
							end = attachTargetArrow(edgeAttr, end, bend);
							
							this.linePath.quadTo((float) bend.getX(),
												(float) bend.getY(), (float) end.getX(),
												(float) end.getY());
						}
					}
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
		return pathContains(this.linePath, x + 1, y + 1);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
