// ==============================================================================
//
// LineEdgeShape.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LineEdgeShape.java,v 1.2 2012-11-07 14:42:20 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DockingAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.graphics.PortAttribute;
import org.graffiti.graphics.PortsAttribute;
import org.graffiti.plugin.view.ArrowShape;
import org.graffiti.plugin.view.EdgeShape;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

/**
 * Class representing an edge as one straight line.
 * 
 * @version $Revision: 1.2 $
 */
public abstract class LineEdgeShape implements EdgeShape {
	// ~ Instance fields ========================================================
	
	public boolean hollowTargetArrowShape = false;
	public boolean hollowSourceArrowShape = false;
	
	/** The graphicsAttribute of the edge this shape represents. */
	protected EdgeGraphicAttribute graphicsAttr;
	
	/**
	 * The <code>Line</code> that is represented by this <code>EdgeShape</code>
	 */
	protected GeneralPath linePath;
	
	/**
	 * The <code>Line2D</code> that might represent this <code>LineEdgeShape</code> or used for intersection purposes.
	 */
	protected Line2D line2D;
	
	// /**
	// * Largest distance between mouseclick and line so that line still gets
	// * selected.
	// */
	// protected final double LINE_TOLERANCE = 2d;
	
	/**
	 * The real bounding box with coordinates relative to the view of this shape.
	 */
	protected Rectangle2D realBounds;
	
	/** The shape of the arrow on the source side. */
	protected Shape headArrow;
	
	/** The shape of the arrow on the source side. */
	protected Shape tailArrow;
	
	// ~ Constructors ===========================================================
	
	/**
	 * The constructor creates a line using default values. The shapes of the
	 * source and target nodes are given to enable the edge to paint itself
	 * correctly between those nodes. The line is painted between the centers of
	 * the source and target shape. No arrows are painted.
	 */
	public LineEdgeShape() {
		this.linePath = new GeneralPath();
		this.realBounds = new Rectangle2D.Double(0d, 0d, 0d, 0d);
		this.line2D = new Line2D.Double();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Rectangle getBounds() {
		Rectangle result = getBounds2D().getBounds();
		return result;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Rectangle2D getBounds2D() {
		Rectangle2D result = linePath.getBounds2D();
		if (getHeadArrow() != null) {
			result.add(getHeadArrow().getBounds2D());
			if (hollowTargetArrowShape) {
				
			}
		}
		if (getTailArrow() != null) {
			result.add(getTailArrow().getBounds2D());
			if (hollowSourceArrowShape) {
				
			}
		}
		return result;
	}
	
	/**
	 * Returns the arrow at the target side.
	 * 
	 * @return the arrow at the target side.
	 */
	public Shape getHeadArrow() {
		return headArrow;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param t
	 *           DOCUMENT ME!
	 * @param d
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public PathIterator getPathIterator(AffineTransform t, double d) {
		return linePath.getPathIterator(t, d);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param t
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public PathIterator getPathIterator(AffineTransform t) {
		return linePath.getPathIterator(t);
	}
	
	/**
	 * Returns the correct bounding box with coordinates relative to the view.
	 * 
	 * @return the correct bounding box with coordinates relative to the view.
	 */
	public Rectangle2D getRealBounds2D() {
		return realBounds;
	}
	
	/**
	 * Called when one of the nodes belonging to this edge has changed.
	 * 
	 * @param graphics
	 *           the attribute that has changed
	 * @param source
	 *           the <code>NodeShape</code> of the source node
	 * @param target
	 *           the <code>NodeShape</code> of the target node
	 */
	public abstract void buildShape(EdgeGraphicAttribute graphics, NodeShape source, NodeShape target) throws ShapeNotFoundException;
	
	/**
	 * Returns the arrow at the source side.
	 * 
	 * @return the arrow at the source side.
	 */
	public Shape getTailArrow() {
		return this.tailArrow;
	}
	
	/**
	 * Checks whether or not a rectangle lies entirely within this shape.
	 * 
	 * @param x
	 *           the x-coordinate of the point to check.
	 * @param y
	 *           the y-coordinate of the point to check.
	 * @param w
	 *           width
	 * @param h
	 *           height
	 * @return true if the point lies within this shape.
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	public boolean contains(double x, double y, double w, double h) {
		throw new RuntimeException();
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
	public abstract boolean contains(double x, double y);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param p
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
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
	public boolean contains(Rectangle2D r) {
		return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
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
	public boolean intersects(double x, double y, double w, double h) {
		return linePath.intersects(x, y, w, h);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param r
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public boolean intersects(Rectangle2D r) {
		return this.linePath.intersects(r);
	}
	
	/**
	 * Checks whether or not a point is said to be locateds on a line. It uses
	 * the field <code>LINE_TOLERANCE</code> as a certain tolerance, i.e. it
	 * really checks whether or not the point lies inside a rectangle around the
	 * line.
	 * 
	 * @param line
	 * @param x
	 * @param y
	 * @return DOCUMENT ME!
	 */
	public boolean lineContains(Line2D line, double x, double y) {
		double maxDist = CLICK_TOLERANCE;
		double lineBreadth = getEdgeThickness() + getFrameThickness();
		
		if (lineBreadth > maxDist) {
			maxDist = lineBreadth / 2d;
		}
		
		double dist = line.ptSegDist(x, y);
		
		if (dist < maxDist) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the coordinates of the port the edge belongs to. It returns
	 * default values if no port could be found.
	 * 
	 * @param edgeAttr
	 *           the graphic attribute of the edge
	 * @param sourceShape
	 *           the shape of the source node
	 * @return a <code>Point2D</code> representing the coordinates of the port
	 *         the edge wants to dock to or those of the default port when there
	 *         was no or wrong port information int the edge.
	 */
	protected Point2D getSourceDockingCoords(EdgeGraphicAttribute edgeAttr, NodeShape sourceShape) {
		DockingAttribute docking = edgeAttr.getDocking();
		String sourcePortName = docking.getSource();
		
		Edge edge = (Edge) edgeAttr.getAttributable();
		Node sourceNode = edge.getSource();
		
		return getDockingCoords(sourcePortName, sourceShape, sourceNode, true);
	}
	
	/**
	 * Returns the coordinates of the port the edge belongs to. It returns
	 * default values if no port could be found.
	 * 
	 * @param edgeAttr
	 *           the graphic attribute of the edge
	 * @param targetShape
	 *           the shape of the target node
	 * @return a <code>Point2D</code> representing the coordinates of the port
	 *         the edge wants to dock to or those of the default port when there
	 *         was no or wrong port information int the edge.
	 */
	protected Point2D getTargetDockingCoords(EdgeGraphicAttribute edgeAttr, NodeShape targetShape) {
		DockingAttribute docking = edgeAttr.getDocking();
		String targetPortName = docking.getTarget();
		
		Edge edge = (Edge) edgeAttr.getAttributable();
		Node targetNode = edge.getTarget();
		
		return getDockingCoords(targetPortName, targetShape, targetNode, false);
	}
	
	/**
	 * Set and get bounds taking line width into account.
	 * 
	 * @return bounding rectangle including line width.
	 */
	protected Rectangle2D addThickBounds(GeneralPath path, EdgeGraphicAttribute edgeAttr) {
		double thickness = getFrameThickness() / 2;
		
		AffineTransform at = new AffineTransform();
		
		at.setToTranslation(thickness, thickness);
		Shape shape = path.createTransformedShape(at);
		realBounds.add(shape.getBounds2D());
		
		at.setToTranslation(-thickness, -thickness);
		shape = path.createTransformedShape(at);
		realBounds.add(shape.getBounds2D());
		
		return realBounds;
	}
	
	/**
	 * Set and get bounds taking line width into account.
	 * 
	 * @param line
	 *           DOCUMENT ME!
	 * @param edgeAttr
	 *           DOCUMENT ME!
	 * @return bounding rectangle including line width.
	 */
	protected Rectangle2D getThickBounds(Line2D line, EdgeGraphicAttribute edgeAttr) {
		this.realBounds = line.getBounds2D();
		
		double thickness = getFrameThickness(); // /2 ???
		
		if (thickness > 0) {
			Ellipse2D e1 = new Ellipse2D.Double(line.getX1(), line.getY1(), thickness, thickness);
			realBounds.add(e1.getBounds2D());
			Ellipse2D e2 = new Ellipse2D.Double(line.getX2(), line.getY2(), thickness, thickness);
			realBounds.add(e2.getBounds2D());
			realBounds.add(realBounds.getX() - thickness / 2, realBounds.getY() - thickness / 2);
		}
		return realBounds;
	}
	
	/**
	 * Creates an arrow from the classname found in the graphics attribute (if
	 * there is one specified) and affixes it. Uses <code>getArrowTail</code> The
	 * arrow is attached at the <code>target</code> point; it points in the
	 * direction from <code>other</code> to <code>target</code>. The arrow shape
	 * is saved in the according member variable. Since the edge should (if there
	 * are arrow(s)) not any longer be attached to its intersection point with
	 * the node but rather to the arrow anchor, this method returns the new (or
	 * old if no arrow(s)) point where the edge should be attached at the node.
	 * 
	 * @param edgeAttr
	 *           the graphics attribute.
	 * @param target
	 *           the point where to attach the arrow.
	 * @param other
	 *           indicates the direction for the arrow.
	 * @return (new) attachment point for the edge.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	protected Point2D attachSourceArrow(EdgeGraphicAttribute edgeAttr, Point2D target, Point2D other) throws ShapeNotFoundException {
		if (target.distance(other) < 0.0000001)
			return other;
		
		Point2D newTarget = target;
		ArrowShape tailShape = null;
		
		String shapeClass = edgeAttr.getArrowtail();
		
		if (!shapeClass.equals("")) {
			hollowSourceArrowShape = shapeClass.contains("Thin");
			try {
				shapeClass = StringManipulationTools.stringReplace(shapeClass, "Thin", "");
				tailShape = (ArrowShape) InstanceLoader.createInstance(shapeClass);
			} catch (InstanceCreationException ie) {
				throw new ShapeNotFoundException(ie.toString());
			}
			((AbstractArrowShape) tailShape).lineWidth = (float) edgeAttr.getFrameThickness();
			if (tailShape instanceof SupportsHollowDrawing)
				((SupportsHollowDrawing) tailShape).setHollow(hollowSourceArrowShape);
			tailShape.updateSize(getEdgeThickness());
			
			// glue arrow on the line at the correct spot and rotation
			this.tailArrow = tailShape.affix(target, other, getEdgeThickness() + edgeAttr.getFrameThickness());
			newTarget = tailShape.getAnchor();
		}
		return newTarget;
	}
	
	protected double getEdgeThickness() {
		return graphicsAttr.getThickness();
	}
	
	protected double getFrameThickness() {
		return graphicsAttr.getFrameThickness();
	}
	
	/**
	 * Creates an arrow from the classname found in the graphics attribute (if
	 * there is one specified) and affixes it. Uses <code>getArrowHead</code> The
	 * arrow is attached at the <code>target</code> point; it points in the
	 * direction from <code>other</code> to <code>target</code>. The arrow shape
	 * is saved in the according member variable. Since the edge should (if there
	 * are arrow(s)) not any longer be attached to its intersection point with
	 * the node but rather to the arrow anchor, this method returns the new (or
	 * old if no arrow(s)) point where the edge should be attached at the node.
	 * 
	 * @param edgeAttr
	 *           the graphics attribute.
	 * @param target
	 *           the point where to attach the arrow.
	 * @param other
	 *           indicates the direction for the arrow.
	 * @return (new) attachment point for the edge.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	protected Point2D attachTargetArrow(EdgeGraphicAttribute edgeAttr, Point2D target, Point2D other) throws ShapeNotFoundException {
		
		if (target.distance(other) < 0.0000001)
			return other;
		
		Point2D newTarget = target;
		ArrowShape headShape = null;
		
		String shapeClass = edgeAttr.getArrowhead();
		
		if (!shapeClass.equals("")) {
			hollowTargetArrowShape = shapeClass.contains("Thin");
			try {
				shapeClass = StringManipulationTools.stringReplace(shapeClass, "Thin", "");
				headShape = (ArrowShape) InstanceLoader.createInstance(shapeClass);
			} catch (InstanceCreationException ie) {
				throw new ShapeNotFoundException(ie.toString());
			}
			((AbstractArrowShape) headShape).lineWidth = (float) edgeAttr.getFrameThickness();
			if (headShape instanceof SupportsHollowDrawing)
				((SupportsHollowDrawing) headShape).setHollow(hollowTargetArrowShape);
			headShape.updateSize(getEdgeThickness());
			
			// glue arrow on the line at the correct spot and rotation
			headArrow = headShape.affix(target, other, getEdgeThickness() + getFrameThickness());
			newTarget = headShape.getAnchor();
		}
		
		return newTarget;
	}
	
	private static HashMap<String, Point2D> defaultPorts = getDefaultPorts();
	
	/**
	 * Returns the coordinates of the port named <code>portName</code>.
	 * 
	 * @param portName
	 *           the name of the port the edge wants to dock to.
	 * @param shape
	 *           the shape of the node the edge wants to dock to. Needed to
	 *           calculate the absolute coordinates of the ports.
	 * @param node
	 *           the node the edge wants to dock to. Needed to get to its port
	 *           attributes.
	 * @param out
	 *           <code>true</code> if only common and outgoing ports should be
	 *           searched, <code>false</code> if only common and ingoing ports
	 *           should be searched.
	 * @return a <code>Point2D</code> representing the coordinates of the port
	 *         the edge wants to dock to or those of the default port when there
	 *         was no or wrong port information int the edge.
	 */
	private Point2D getDockingCoords(String portName, NodeShape shape, Node node, boolean out) {
		if (shape == null)
			return AttributeHelper.getPosition(node);
		Rectangle2D sRect = shape.getRealBounds2D();
		Point2D point = new Point2D.Double();
		
		NodeGraphicAttribute nodeAttr = (NodeGraphicAttribute) node.getAttribute(GraphicAttributeConstants.GRAPHICS);
		
		if (portName.equals("")) {
			point = calculateDefaultDocking(nodeAttr, shape);
		} else {
			PortsAttribute ports = nodeAttr.getPorts();
			PortAttribute port = ports.getPort(portName, out);
			
			if (port == null) {
				if (defaultPorts.containsKey(portName)) {
					point = defaultPorts.get(portName);
					double px = point.getX();
					double py = point.getY();
					
					point = new Point2Dfix(
										sRect.getCenterX() + (px * sRect.getWidth() / 2d),
										sRect.getCenterY() + (py * sRect.getHeight() / 2d));
				} else {
					if (portName != null && portName.indexOf(";") > 0) {
						try {
							String a = portName.substring(0, portName.indexOf(";"));
							String b = portName.substring(portName.indexOf(";") + 1);
							double px = Double.parseDouble(a);
							double py = Double.parseDouble(b);
							double xexcess = 0;
							double yexcess = 0;
							if (px > 1 || px < -1) {
								xexcess = px;
								px = px > 0 ? 1 : -1;
							}
							if (py > 1 || py < -1) {
								yexcess = py;
								py = py > 0 ? 1 : -1;
							}
							point = new Point2Dfix(
												xexcess + sRect.getCenterX() + ((px * sRect.getWidth()) / 2d),
												yexcess + sRect.getCenterY() + ((py * sRect.getHeight()) / 2d));
						} catch (Exception e) {
							point = calculateDefaultDocking(nodeAttr, shape);
						}
					} else
						point = calculateDefaultDocking(nodeAttr, shape);
				}
			} else {
				CoordinateAttribute coords = port.getCoordinate();
				point.setLocation(sRect.getCenterX() + ((coords.getX() * sRect.getWidth()) / 2d), sRect.getCenterY()
									+ ((coords.getY() * sRect.getHeight()) / 2d));
				point = new Point2Dfix(point.getX(), point.getY());
			}
		}
		
		return point;
	}
	
	private static HashMap<String, Point2D> getDefaultPorts() {
		HashMap<String, Point2D> result = new HashMap<String, Point2D>();
		result.put("tl", new Point2Dfix(-1, -1));
		result.put("tr", new Point2Dfix(+1, -1));
		result.put("bl", new Point2Dfix(-1, +1));
		result.put("br", new Point2Dfix(+1, +1));
		
		result.put("top", new Point2Dfix(0, -1));
		result.put("bottom", new Point2Dfix(0, +1));
		result.put("left", new Point2Dfix(-1, 0));
		result.put("right", new Point2Dfix(+1, 0));
		
		return result;
	}
	
	/**
	 * Returns the coordinates of the default port of a node. (standard
	 * implementation is the center of the node).
	 * 
	 * @param nodeAttr
	 *           the graphics attribute of the node. Needed to get the (center)
	 *           coordinates of the node.
	 * @param shape
	 *           the shape of the node. May be needed to to more sophisticated
	 *           calculation of default ports.
	 * @return absolute coordinates of default port.
	 */
	private Point2D calculateDefaultDocking(NodeGraphicAttribute nodeAttr, NodeShape shape) {
		Point2D point = new Point2D.Double();
		CoordinateAttribute coord = nodeAttr.getCoordinate();
		point.setLocation(coord.getX(), coord.getY());
		
		return point;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
