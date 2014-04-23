// ==============================================================================
//
// NodeComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeComponent.java,v 1.1 2011-01-31 09:03:27 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraffitiViewComponent;
import org.graffiti.plugin.view.NodeComponentInterface;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

/**
 * This component represents a <code>org.graffiti.graph.Node</code>.
 * 
 * @version $Revision: 1.1 $
 */
public class NodeComponent
					extends AbstractGraphElementComponent
					implements NodeComponentInterface {
	// ~ Constructors ===========================================================
	private static final long serialVersionUID = -303544019220632035L;
	
	private Stroke stroke = null;
	
	private NodeGraphicAttribute nodeAttr = null;
	private float lastStrokeWidth = Float.MIN_VALUE;
	
	private Paint rgp = null;
	
	/**
	 * Constructor for NodeComponent.
	 * 
	 * @param ge
	 */
	public NodeComponent(GraphElement ge) {
		super(ge);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Creates a standard NodeShape (in this case a rectangle) and draws it.
	 * 
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	@Override
	public void createStandardShape() {
		NodeShape newShape = new RectangleNodeShape();
		NodeGraphicAttribute nodeAttr;
		nodeAttr = (NodeGraphicAttribute) ((Node) graphElement).getAttribute(GRAPHICS);
		
		try {
			newShape.buildShape(nodeAttr);
			shape = newShape;
			adjustComponentSize();
		} catch (ShapeNotFoundException e) {
			throw new RuntimeException("this should never happen since the " +
								"standard node shape should always " + "exist." + e);
		}
	}
	
	/**
	 * Called when a graphic attribute of the node represented by this
	 * component has changed.
	 * 
	 * @param attr
	 *           the graphic attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	@Override
	public synchronized void graphicAttributeChanged(Attribute attr)
						throws ShapeNotFoundException {
		if (attr != null) {
			String id = attr.getId();
			if (id.equals(COORDINATE)) {
				this.adjustComponentSize();
			} else
				if (id.equals(FRAMETHICKNESS)) {
					this.adjustComponentSize();
				} else
					if (attr.getPath().startsWith(Attribute.SEPARATOR + GRAPHICS +
										Attribute.SEPARATOR + COORDINATE)) {
						adjustComponentSize();
					} else
						if (id.equals(DIMENSION) || id.equals(GraphicAttributeConstants.HEIGHT) || id.equals(GraphicAttributeConstants.WIDTH)) {
							((NodeShape) this.shape).buildShape(
												(NodeGraphicAttribute) attr.getAttributable().getAttribute(GRAPHICS));
							this.adjustComponentSize();
						} else
							if (attr.getPath().startsWith(GRAPHICS + Attribute.SEPARATOR + PORTS)) {
								// ??? TODO (are ports diplayed? ???
								// evtl nur einzelne Kanten updaten?
								// this.updateDependentComponents();
							} else
								if ((id.equals(LINEMODE) || id.equals(FRAMECOLOR) ||
													id.equals(FILLCOLOR) || id.equals(ROUNDING) || id.equals(SHAPE) || id.equals(GRAPHICS))) {
									createNewShape(coordinateSystem);
									adjustComponentSize();
								}
			
			// update attribute components like labels:
			for (GraffitiViewComponent gvc : attributeComponents.values()) {
				AttributeComponent attrComp = (AttributeComponent) gvc;
				attrComp.setShift(getLocation());
				attrComp.setGraphElementShape(shape);
				attrComp.attributeChanged(attr);
			}
			updateRelatedEdgeComponents();
		}
	}
	
	/**
	 * Draws the shape of the node contained in this component according to the
	 * graphic attributes of the node.
	 * 
	 * @param g
	 *           the graphics context in which to draw.
	 */
	@Override
	protected void drawShape(Graphics g) {
		// super.drawShape(g);
		Graphics2D drawArea = (Graphics2D) g;
		
		drawArea.translate(shape.getXexcess(), shape.getYexcess());
		
		// set method of drawing according to attributes of node
		if (nodeAttr == null)
			nodeAttr = (NodeGraphicAttribute) ((Node) graphElement).getAttribute(GRAPHICS);
		
		// outline
		// Stroke backupStroke = drawArea.getStroke();
		float frameThickness = (float) nodeAttr.getFrameThickness();
		if (frameThickness > 0) {
			if (stroke == null || (Math.abs((lastStrokeWidth - frameThickness)) > 0.0001)) {
				lastStrokeWidth = frameThickness;
				stroke = new BasicStroke(lastStrokeWidth,
									DEFAULT_CAP_R, DEFAULT_JOIN, DEFAULT_MITER,
									nodeAttr.getLineMode().getDashArray(),
									nodeAttr.getLineMode().getDashPhase());
			}
			drawArea.setStroke(stroke);
		}
		
		// draw background image
		// fill the shape
		ColorAttribute color = nodeAttr.getFillcolor();
		Paint fillPaint = color.getColor();
		double epsilon = 0.0001;
		if (rgp != null && Math.abs(nodeAttr.getUseGradient()) > epsilon) {
			fillPaint = rgp;
		}
		Paint framePaint = nodeAttr.getFramecolor().getColor();
		boolean drawFrame = (nodeAttr.getFrameThickness() > epsilon);
		if (shape instanceof ProvidesAdditonalDrawingShapes) {
			Collection<Shape> preShapes = ((ProvidesAdditonalDrawingShapes) shape).getPreBorderShapes();
			if (preShapes != null)
				for (Shape s : preShapes) {
					drawArea.setPaint(fillPaint);
					drawArea.fill(s);
					if (drawFrame) {
						drawArea.setPaint(framePaint);
						drawArea.draw(s);
					}
				}
		}
		drawArea.setPaint(fillPaint);
		try {
			drawArea.fill(shape);
		} catch (Exception eee) {
			// empty
		}
		// draw the outline of the shape according to attributes
		/*
		 * must not be transparent because otherwise would lead to
		 * problems with overlapping fill and frame
		 */

		if (drawFrame) {
			drawArea.setPaint(framePaint);
			drawArea.draw(shape);
		}
		if (shape instanceof PolygonalNodeShape) {
			PolygonalNodeShape pp = (PolygonalNodeShape) shape;
			if (pp.ignorePoints != null && pp.ignorePoints.size() > 0) {
				drawArea.setPaint(framePaint);
				drawArea.fillPolygon(pp.getIgnorePolygon());
			}
		}
		
		if (shape instanceof ProvidesAdditonalDrawingShapes) {
			Collection<Shape> postShapes = ((ProvidesAdditonalDrawingShapes) shape).getPostBorderShapes();
			if (postShapes != null)
				for (Shape s : postShapes) {
					drawArea.setPaint(fillPaint);
					drawArea.fill(s);
					if (drawFrame) {
						drawArea.setPaint(framePaint);
						drawArea.draw(s);
					}
				}
		}
		
		drawArea.translate(-1 - shape.getXexcess(), -1 - shape.getYexcess());
	}
	
	/**
	 * Used when the shape changed in the datastructure.
	 * 
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	@Override
	protected void recreate()
						throws ShapeNotFoundException {
		if (!this.graphElement.getAttributes().getCollection().containsKey(GRAPHICS)) {
			Node n = (Node) graphElement;
			AttributeHelper.setDefaultGraphicsAttribute(n, 100, 100);
		}
		Object obj = graphElement.getAttribute(GRAPHICS);
		
		NodeGraphicAttribute geAttr = (NodeGraphicAttribute) obj;
		
		// get classname of the shape to use and instantiate this
		String shapeClass = geAttr.getShape();
		shapeClass = AttributeHelper.getShapeClassFromShapeName(shapeClass);
		NodeShape newShape = null;
		
		try {
			newShape = (NodeShape) InstanceLoader.createInstance(shapeClass);
		} catch (InstanceCreationException ie) {
			throw new ShapeNotFoundException(ie.toString());
		}
		
		// get graphic attribute and pass it to the shape
		newShape.setCoordinateSystem(coordinateSystem);
		newShape.buildShape(geAttr);
		this.shape = newShape;
		this.adjustComponentSize();
		
		int maxR = getHeight() > getWidth() ? getHeight() : getWidth();
		// maxR = maxR / 2;
		if (nodeAttr == null)
			nodeAttr = (NodeGraphicAttribute) ((Node) graphElement).getAttribute(GRAPHICS);
		
		double epsilon = 0.0001;
		if (nodeAttr.getUseGradient() > epsilon)
			rgp = new RoundGradientPaint(nodeAttr.getDimension().getWidth() * nodeAttr.getUseGradient(), nodeAttr.getDimension().getHeight()
								* nodeAttr.getUseGradient(), nodeAttr.getFillcolor().getColor(), new Point2D.Double(0, maxR), nodeAttr.getFramecolor().getColor());
		else
			if (nodeAttr.getUseGradient() < -1) {
				Point2D p1 = new Point2D.Double(0, nodeAttr.getDimension().getHeight() * (1 + 1 + nodeAttr.getUseGradient()));
				Point2D p2 = new Point2D.Double(0, nodeAttr.getDimension().getHeight()); // *(1+epsilon+nodeAttr.getUseGradient()));
				Color c1 = nodeAttr.getFillcolor().getColor();
				Color c2 = nodeAttr.getFramecolor().getColor();
				rgp = new GradientPaint(p1, c1, p2, c2);
			} else
				if (nodeAttr.getUseGradient() < -epsilon) {
					Point2D p1 = new Point2D.Double(0, nodeAttr.getDimension().getHeight() * (1 + nodeAttr.getUseGradient()));
					Point2D p2 = new Point2D.Double(0, nodeAttr.getDimension().getHeight() * (1 + epsilon + nodeAttr.getUseGradient()));
					Color c1 = nodeAttr.getFillcolor().getColor();
					Color c2 = nodeAttr.getFramecolor().getColor();
					rgp = new GradientPaint(p1, c1, p2, c2);
				} else
					rgp = null;
	}
	
	/**
	 * Called whenever the size of the shape within this component has changed.
	 */
	protected void adjustComponentSize() {
		Rectangle2D bounds = shape.getRealBounds2D();
		
		double offA = -0.5d;
		double offB = 2d;
		double xE = shape.getXexcess();
		double yE = shape.getYexcess();
		setBounds((int) (bounds.getX() + offA - xE), (int) (bounds.getY() + offA - yE),
							(int) (bounds.getWidth() + offB + xE * 2d), (int) (bounds.getHeight() + offB + yE * 2d));
		
	}
	
	/**
	 * Calls <code>updateShape</code> on all dependent (edge) components.
	 * 
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	protected void updateRelatedEdgeComponents() {
		synchronized (dependentComponents) {
			for (Iterator<?> it = dependentComponents.iterator(); it.hasNext();) {
				EdgeComponent ec = null;
				
				try {
					ec = (EdgeComponent) it.next();
					ec.updateShape();
				} catch (ClassCastException cce) {
					ErrorMsg.addErrorMessage(
										"Only EdgeComponents should be registered as dependent components. " +
															"Others should probably be attributeComponents!" + cce);
				}
			}
		}
	}
	
	@Override
	public String getToolTipText() {
		try {
			Attribute a = graphElement.getAttribute("tooltip");
			if (a != null)
				return (String) a.getValue();
		} catch (Exception e) {
			// empty
		}
		return null;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
