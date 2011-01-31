// ==============================================================================
//
// EdgeComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeComponent.java,v 1.1 2011-01-31 09:03:28 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.EdgeComponentInterface;
import org.graffiti.plugin.view.EdgeShape;
import org.graffiti.plugin.view.GraffitiViewComponent;
import org.graffiti.plugin.view.NodeComponentInterface;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

/**
 * This component represents a <code>org.graffiti.graph.Edge</code>.
 * 
 * @version $Revision: 1.1 $
 */
public class EdgeComponent extends AbstractGraphElementComponent implements
					EdgeComponentInterface {
	// ~ Instance fields
	// ========================================================
	private static final long serialVersionUID = 3256442525387600951L;
	
	/** The component of the source node of this edge. */
	private NodeComponentInterface sourceComp;
	
	/** The component of the target node of this edge. */
	private NodeComponentInterface targetComp;
	
	private EdgeGraphicAttribute edgeAttr = null;
	
	private Stroke stroke = null;
	
	private GradientPaint gp = null;
	
	// ~ Constructors
	// ===========================================================
	
	/**
	 * Constructor for EdgeComponent.
	 * 
	 * @param ge
	 *           the edge to which this component belongs.
	 * @param source
	 *           the <code>NodeComponent</code> of the source node of this
	 *           edge.
	 * @param target
	 *           the <code>NodeComponent</code> of the target node of this
	 *           edge.
	 */
	public EdgeComponent(GraphElement ge, NodeComponent source,
						NodeComponent target) {
		super(ge);
		this.sourceComp = source;
		this.targetComp = target;
	}
	
	// ~ Methods
	// ================================================================
	
	/**
	 * Sets the source component.
	 * 
	 * @param snc
	 *           the source component to be set.
	 */
	public void setSourceComponent(NodeComponentInterface snc) {
		// System.out.println("setting source comp to " + snc);
		this.sourceComp = snc;
		nodeComponentChanged();
	}
	
	/**
	 * Sets the source component.
	 * 
	 * @param tnc
	 *           the source component to be set.
	 */
	public void setTargetComponent(NodeComponentInterface tnc) {
		this.targetComp = tnc;
		nodeComponentChanged();
	}
	
	/**
	 * Sets a standard shape for this edge. It uses a <code>StraightLineEdgeShape</code>.
	 * 
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	@Override
	public void createStandardShape() {
		EdgeShape newShape = new StraightLineEdgeShape();
		if (edgeAttr == null)
			edgeAttr = (EdgeGraphicAttribute) ((Edge) graphElement)
								.getAttribute(GRAPHICS);
		
		try {
			newShape.buildShape(edgeAttr,
								(NodeShape) this.sourceComp.getShape(),
								(NodeShape) this.targetComp.getShape());
		} catch (ShapeNotFoundException e) {
			throw new RuntimeException("this should never happen since the "
								+ "standard edge shape should always " + "exist." + e);
		}
		
		this.shape = newShape;
		edgeAttr.setShape("org.graffiti.plugins.views.defaults.StraightLineEdgeShape");
		
		this.adjustComponentSize();
	}
	
	/**
	 * Draws the shape associated with this component onto the graphics context
	 * of this component. This method uses the graphics attributes defined for
	 * line color, thickness etc.
	 * 
	 * @param g
	 *           the <code>Graphics</code> context to draw on.
	 */
	@Override
	public void drawShape(Graphics g) {
		// super.drawShape(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		if (edgeAttr == null)
			edgeAttr = (EdgeGraphicAttribute) ((Edge) graphElement)
								.getAttribute(GRAPHICS);
		
		if (edgeAttr.getFrameThickness() < 0) {
			try {
				recreate();
			} catch (ShapeNotFoundException e) {
				ErrorMsg.addErrorMessage(e);
			}
			return;
		}
		
		LineEdgeShape edgeShape = (LineEdgeShape) this.shape;
		
		// outline (includes linewidth, linemode)
		if (stroke == null) {
			if (edgeShape.getFrameThickness() <= 1)
				stroke = new BasicStroke((float) edgeShape.getFrameThickness(),
									DEFAULT_CAP_R, DEFAULT_JOIN, DEFAULT_MITER, edgeAttr
														.getLineMode().getDashArray(), edgeAttr
														.getLineMode().getDashPhase());
			else
				stroke = new BasicStroke((float) edgeShape.getFrameThickness(),
									DEFAULT_CAP_B, DEFAULT_JOIN, DEFAULT_MITER, edgeAttr
														.getLineMode().getDashArray(), edgeAttr
														.getLineMode().getDashPhase());
		}
		
		Shape hArrow = edgeShape.getHeadArrow();
		
		ColorAttribute fillColor = edgeAttr.getFillcolor();
		Color fillColorOpaque = fillColor.getColor();
		
		ColorAttribute frameColor = edgeAttr.getFramecolor();
		Color frameColorOpaque = frameColor.getColor(); // new
		// Color(frameColor.getRed(),
		// frameColor.getGreen(),
		// frameColor.getBlue());
		
		g2d.setStroke(stroke);
		
		// draw the outline of the shape according to attributes
		// must not be transparent because otherwise would lead to
		// problems with overlapping fill and frame
		
		g2d.setPaint(frameColorOpaque);
		if (edgeAttr.getUseGradient() > 0)
			g2d.setPaint(gp);
		g2d.draw(shape);
		
		if (hArrow != null) {
			if (((LineEdgeShape) shape).hollowTargetArrowShape) {
				g2d.setPaint(Color.WHITE);
				g2d.fill(hArrow);
				g2d.setPaint(frameColorOpaque);
				g2d.draw(hArrow);
			} else {
				g2d.setPaint(frameColorOpaque);
				g2d.fill(hArrow);
			}
		}
		
		Shape tArrow = edgeShape.getTailArrow();
		if (tArrow != null) {
			if (((LineEdgeShape) shape).hollowSourceArrowShape) {
				g2d.setPaint(Color.WHITE);
				g2d.fill(tArrow);
				g2d.setPaint(fillColorOpaque);
				g2d.draw(tArrow);
			} else {
				g2d.setPaint(fillColorOpaque);
				g2d.fill(tArrow);
			}
		}
	}
	
	/**
	 * Called when a graphic attribute of the edge represented by this component
	 * has changed.
	 * 
	 * @param attr
	 *           the graphic attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	@Override
	public void graphicAttributeChanged(Attribute attr)
						throws ShapeNotFoundException {
		/*
		 * if the type of the shape or the size changed then we have to rebuild
		 * the shape
		 */
		String id = attr.getId();
		
		if (id.equals(LINEMODE) || id.equals(FRAMETHICKNESS)
							|| id.equals(FRAMECOLOR)) {
			repaint();
		} else
			if (id.equals(DOCKING)) {
				((EdgeShape) this.shape).buildShape((EdgeGraphicAttribute) attr,
									(NodeShape) sourceComp.getShape(), (NodeShape) targetComp
														.getShape());
			} else {
				createNewShape(coordinateSystem);
			}
		
	}
	
	/**
	 * Calls buildShape if no NodeShapes have changed.
	 */
	public void updateShape() {
		nodeComponentChanged();
	}
	
	/**
	 * Used when the shape changed in the datastructure. Makes the painter
	 * create a new shape.
	 * 
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	@Override
	protected void recreate() throws ShapeNotFoundException {
		stroke = null;
		EdgeGraphicAttribute geAttr;
		if (!this.graphElement.getAttributes().getCollection().containsKey(
							GRAPHICS)) {
			graphElement.addAttribute(AttributeHelper
								.getNewEdgeGraphicsAttribute(Color.BLACK, Color.BLACK,
													((Edge) graphElement).isDirected()), "");
		}
		geAttr = (EdgeGraphicAttribute) this.graphElement
							.getAttribute(GRAPHICS);
		String shapeClass = geAttr.getShape();
		EdgeShape newShape = null;
		
		try {
			newShape = (EdgeShape) InstanceLoader.createInstance(shapeClass);
		} catch (InstanceCreationException ie) {
			throw new ShapeNotFoundException(ie.toString());
		}
		
		// get graphic attribute and pass it to the shape
		newShape.buildShape(geAttr,
							(sourceComp != null ? (NodeShape) this.sourceComp.getShape()
												: null),
							(targetComp != null ? (NodeShape) this.targetComp.getShape()
												: null));
		this.shape = newShape;
		this.adjustComponentSize();
		
		for (Iterator<?> it = this.attributeComponents.values().iterator(); it
							.hasNext();) {
			AttributeComponent attrComp = (AttributeComponent) it.next();
			attrComp.setShift(this.getLocation());
			attrComp.setGraphElementShape(this.shape);
			attrComp.createNewShape(coordinateSystem);
		}
		
		if (geAttr.getUseGradient() > 0) {
			Edge e = (Edge) geAttr.getAttributable();
			Vector2d a = AttributeHelper.getPositionVec2d(e.getSource());
			Vector2d b = AttributeHelper.getPositionVec2d(e.getTarget());
			Vector2d ao = new Vector2d(a);
			Vector2d bo = new Vector2d(b);
			double minX = a.x < b.x ? a.x : b.x;
			double minY = a.y < b.y ? a.y : b.y;
			double maxX = a.x > b.x ? a.x : b.x;
			double maxY = a.y > b.y ? a.y : b.y;
			
			a.x = 0;
			a.y = 0;
			b.x = maxX - minX;
			b.y = maxY - minY;
			
			if (ao.x > bo.x) {
				double t = a.x;
				a.x = b.x;
				b.x = t;
			}
			if (ao.y > bo.y) {
				double t = a.y;
				a.y = b.y;
				b.y = t;
			}
			
			gp = new GradientPaint(a.getPoint2D(), geAttr.getFillcolor()
								.getColor(), b.getPoint2D(), geAttr.getFramecolor()
								.getColor(), false);
		} else
			gp = null;
	}
	
	/**
	 * Called when source or target node shape have not changed.
	 * 
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	private void nodeComponentChanged() {
		try {
			recreate();
		} catch (ShapeNotFoundException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugins.views.defaults.AbstractGraphElementComponent#adjustComponentSize()
	 */
	protected void adjustComponentSize() {
		
		Rectangle2D bounds = shape.getRealBounds2D();
		setBounds((int) Math.floor(bounds.getX()) - 1,
							(int) Math.floor(bounds.getY()) - 1,
							(int) (Math.ceil(bounds.getWidth())) + 2,
							(int) (Math.ceil(bounds.getHeight())) + 2);
		for (GraffitiViewComponent ac : attributeComponents.values()) {
			if (ac instanceof AttributeComponent) {
				AttributeComponent acc = (AttributeComponent) ac;
				acc.adjustComponentSize();
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
		try {
			String lblA = AttributeHelper.getLabel(((Edge) graphElement)
								.getSource(), null);
			String lblB = AttributeHelper.getLabel(((Edge) graphElement)
								.getTarget(), null);
			Edge e = (Edge) graphElement;
			if (lblA == null || lblB == null)
				return (e.isDirected() ? "<html>" + doubleClickHelp(e)
									+ "? --&gt; ?" : "<html>" + doubleClickHelp(e)
									+ "? &lt;--&gt; ?");
			if (e.isDirected())
				return "<html>" + doubleClickHelp(e) + "" + lblA + " --&gt; "
									+ lblB;
			else
				return "<html>" + doubleClickHelp(e) + "" + lblA
									+ " &lt;--&gt; " + lblB;
		} catch (Exception e) {
			// empty
		}
		return null;
	}
	
	private String doubleClickHelp(Edge e) {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "";
		else
			return "Double-click to edit relations / reactions | ";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
