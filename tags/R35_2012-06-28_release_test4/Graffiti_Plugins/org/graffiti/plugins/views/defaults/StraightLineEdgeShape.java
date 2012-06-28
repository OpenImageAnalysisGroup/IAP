// ==============================================================================
//
// StraightLineEdgeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StraightLineEdgeShape.java,v 1.1 2011-01-31 09:03:28 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.ShapeNotFoundException;

/**
 * Concrete class representing an edge as one straight line.
 * 
 * @version $Revision: 1.1 $
 */
public class StraightLineEdgeShape
					extends LineEdgeShape {
	// ~ Methods ================================================================
	
	// /**
	// * Returns the coordinates of the default port of a node.
	// * (standard implementation is the center of the node).
	// *
	// * @param nodeAttr the graphics attribute of the node. Needed to get the
	// * (center) coordinates of the node.
	// * @param shape the shape of the node. May be needed to to more
	// * sophisticated calculation of default ports.
	// * @return absolute coordinates of default port.
	// */
	// private Point2D calculateDefaultDocking
	// (NodeGraphicAttribute nodeAttr, NodeShape shape) {
	//
	// Point2D point = new Point2D.Double();
	// CoordinateAttribute coord = nodeAttr.getCoordinate();
	// point.setLocation(coord.getX(), coord.getY());
	//
	// return point;
	// }
	//
	// /**
	// * Returns the coordinates of the port named <code>portName</code>.
	// *
	// * @param portName the name of the port the edge wants to dock to.
	// * @param shape the shape of the node the edge wants to dock to. Needed to
	// * calculate the absolute coordinates of the ports.
	// * @param node the node the edge wants to dock to. Needed to get to its
	// * port attributes.
	// * @param out <code>true</code> if only common and outgoing ports should be
	// * searched, <code>false</code> if only common and ingoing ports should be
	// * searched.
	// * @return a <code>Point2D</code> representing the coordinates of the port
	// * the edge wants to dock to or those of the default port when there was no
	// * or wrong port information int the edge.
	// */
	// private Point2D getDockingCoords
	// (String portName, NodeShape shape, Node node, boolean out) {
	//
	// Rectangle2D snRect = shape.getBounds2D();
	// Point2D point = new Point2D.Double();
	//
	// NodeGraphicAttribute nodeAttr = (NodeGraphicAttribute)node.
	// getAttribute(GraphicAttributeConstants.GRAPHICS);
	//
	// if (portName.equals("")) {
	// point = calculateDefaultDocking(nodeAttr, shape);
	//
	// } else {
	// boolean found = false;
	//
	// PortsAttribute ports = nodeAttr.getPorts();
	// PortAttribute port = ports.getPort(portName, out);
	//
	// if (port == null) {
	// // specified port not found
	// point = calculateDefaultDocking(nodeAttr, shape);
	// } else {
	// CoordinateAttribute coords = port.getCoordinate();
	// point.setLocation
	// (snRect.getCenterX() + coords.getX() * snRect.getWidth(),
	// snRect.getCenterY() + coords.getY() * snRect.getHeight());
	// }
	// }
	// return point;
	// }
	
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
		
		this.line2D.setLine(start, end);
		
		// clipping
		// if no intersection was found, just draw from / to docking
		
		Point2D nStart = null;
		if (!(start instanceof Point2Dfix))
			nStart = (sourceShape != null ? sourceShape.getIntersection(this.line2D) : null);
		Point2D nEnd = null;
		if (!(end instanceof Point2Dfix))
			nEnd = (targetShape != null ? targetShape.getIntersection(this.line2D) : null);
		
		if (nStart != null)
			start = nStart;
		if (nEnd != null)
			end = nEnd;
		
		start = attachSourceArrow(edgeAttr, start, end);
		end = attachTargetArrow(edgeAttr, end, start);
		
		line2D = new Line2D.Double(start, end);
		
		realBounds = getThickBounds(this.line2D, edgeAttr);
		
		if (getHeadArrow() != null) {
			this.realBounds.add(
								StandardArrowShape.addThickness(getHeadArrow().getBounds2D(), getFrameThickness()));
		}
		
		if (tailArrow != null) {
			this.realBounds.add(
								StandardArrowShape.addThickness(tailArrow.getBounds2D(), getFrameThickness()));
		}
		
		AffineTransform at = new AffineTransform();
		at.setToTranslation(-realBounds.getX(), -realBounds.getY());
		if (headArrow != null)
			headArrow = at.createTransformedShape(headArrow);
		if (tailArrow != null)
			tailArrow = at.createTransformedShape(tailArrow);
		this.line2D = new Line2D.Double(at.transform(start, null), at.transform(end, null));
		this.linePath = new GeneralPath(this.line2D);
	}
	
	/**
	 * Decides whether or not a point lies within this shape.
	 * 
	 * @param x
	 *           the x-coordinate of the point to check.
	 * @param y
	 *           the y-coordinate of the point to check.
	 * @return true if the point lies within this shape.
	 */
	@Override
	public boolean contains(double x, double y) {
		// TODO: check why this method is called to often
		return lineContains(this.line2D, x, y);
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
