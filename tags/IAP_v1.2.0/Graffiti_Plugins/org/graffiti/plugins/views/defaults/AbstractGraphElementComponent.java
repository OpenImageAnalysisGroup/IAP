// ==============================================================================
//
// AbstractGraphElementComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractGraphElementComponent.java,v 1.1 2011-01-31 09:03:27 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.graffiti.attributes.Attribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.GraffitiViewComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphElementShape;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.Zoomable;

/**
 * Class that shares common members for all GraphElementComponents.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AbstractGraphElementComponent
					extends GraphElementComponent
					implements GraffitiViewComponent, GraphicAttributeConstants {
	// ~ Instance fields ========================================================
	private static final long serialVersionUID = 1L;
	
	/** The <code>GraphElement</code> that is represented by this component. */
	protected GraphElement graphElement;
	
	/** The <code>shape</code> that is drawn onto that component. */
	protected GraphElementShape shape;
	
	/**
	 * A list of components whose position is dependent on the position of this
	 * shape. This is only meant for edges that depend on the position (and
	 * other graphics attributes) of nodes.
	 */
	protected List<GraphElementComponent> dependentComponents;
	
	/**
	 * A mapping between attribute classnames and attributeComponent classnames
	 * that this <code>GraphElement</code> has. These attributes are therefore
	 * attribute and their position is dependent on the position (and size) of
	 * this GraphElement. (this applies mainly to nodes)
	 */
	protected Map<Attribute, GraffitiViewComponent> attributeComponents;
	
	protected CoordinateSystem coordinateSystem;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for GraphElementComponent.
	 * 
	 * @param ge
	 *           DOCUMENT ME!
	 */
	protected AbstractGraphElementComponent(GraphElement ge) {
		super();
		this.graphElement = ge;
		attributeComponents = new LinkedHashMap<Attribute, GraffitiViewComponent>();
		dependentComponents = new ArrayList<GraphElementComponent>();
		this.setOpaque(false);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns GraphElementShape object
	 * 
	 * @return DOCUMENT ME!
	 */
	public GraphElementShape getShape() {
		return this.shape;
	}
	
	/**
	 * Adds an <code>Attribute</code> and its <code>GraffitiViewComponent</code> to the list of registered attributes
	 * that can be displayed. This attribute is then treated as dependent on
	 * the position, size etc. of this <code>GraphElement</code>.
	 * 
	 * @param attr
	 *           the attribute that is registered as being able to be
	 *           displayed.
	 * @param ac
	 *           the component that will be used to display the attribute.
	 */
	public synchronized void addAttributeComponent(Attribute attr, GraffitiViewComponent ac) {
		attributeComponents.put(attr, ac);
	}
	
	/**
	 * Adds a <code>GraphElementComponent</code> to the list of dependent <code>GraphElementComponent</code>s. These will nearly always be
	 * <code>EdgeComponent</code>s that are dependent on their source or
	 * target nodes.
	 * 
	 * @param comp
	 *           the <code>GraphElementComponent</code> that is added to the
	 *           list of dependent components.
	 */
	public void addDependentComponent(GraphElementComponent comp) {
		this.dependentComponents.add(comp);
	}
	
	/**
	 * Called when an attribute of the GraphElement represented by this
	 * component has changed.
	 * 
	 * @param attr
	 *           the attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	public void attributeChanged(Attribute attr)
						throws ShapeNotFoundException {
		
		if (attr.getPath().startsWith(Attribute.SEPARATOR + GraphicAttributeConstants.GRAPHICS)) {
			if (!attr.getId().equals("cluster"))
				graphicAttributeChanged(attr);
		} else
			nonGraphicAttributeChanged(attr);
		
		// if (!attr.getId().equals("cluster"))
		// graphicAttributeChanged(attr);
		// if(!attr.getPath().startsWith(Attribute.SEPARATOR + GraphicAttributeConstants.GRAPHICS))
		// nonGraphicAttributeChanged(attr);
		
		/*
		 * if(attr==null || "".equals(attr.getPath()))
		 * {
		 * graphicAttributeChanged(attr);
		 * nonGraphicAttributeChanged(attr);
		 * } else {
		 * if(attr.getPath().startsWith(Attribute.SEPARATOR +
		 * GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR) ||
		 * attr.getPath().equals(GraphicAttributeConstants.GRAPHICS) ||
		 * attr.getPath().startsWith(".srcLabel") ||
		 * attr.getPath().startsWith(".tgtLabel") ||
		 * attr.getPath().startsWith(".mapping"))
		 * {
		 * // System.out.println("GA: "+attr.getPath());
		 * graphicAttributeChanged(attr);
		 * }
		 * else
		 * {
		 * // System.out.println("NGA: "+attr.getPath());
		 * nonGraphicAttributeChanged(attr);
		 * }
		 * }
		 */
	}
	
	/**
	 * Removes a <code>GraphElementComponent</code> from the list of dependent <code>GraphElementComponent</code>s.
	 */
	public void clearDependentComponentList() {
		this.dependentComponents = new ArrayList<GraphElementComponent>();
	}
	
	/**
	 * Called to initialize the shape of the NodeComponent correctly. Also
	 * calls <code>repaint()</code>.
	 * 
	 * @exception ShapeNotFoundException
	 *               thrown when the shape class couldn't be
	 *               resolved.
	 */
	public void createNewShape(CoordinateSystem coordSys)
						throws ShapeNotFoundException {
		this.coordinateSystem = coordSys;
		recreate();
	}
	
	/**
	 * Called to initialize and draw a standard shape, if the specified
	 * shape class could not be found.
	 */
	public abstract void createStandardShape();
	
	/**
	 * Returns the attributeComponents of given attribute.
	 * 
	 * @param attr
	 * @return Map
	 */
	public synchronized AttributeComponent getAttributeComponent(Attribute attr) {
		return (AttributeComponent) attributeComponents.get(attr);
	}
	
	/**
	 * Returns the attributeComponents of given attribute.
	 * 
	 * @return Map
	 */
	public synchronized Iterator<GraffitiViewComponent> getAttributeComponentIterator() {
		return attributeComponents.values().iterator();
	}
	
	public synchronized Collection<GraffitiViewComponent> getAttributeComponents() {
		return attributeComponents.values();
	}
	
	/**
	 * Returns the graphElement.
	 * 
	 * @return GraphElement
	 */
	public GraphElement getGraphElement() {
		return graphElement;
	}
	
	/**
	 * Removes all entries in the attributeComponent list.
	 */
	public synchronized void clearAttributeComponentList() {
		attributeComponents = new HashMap<Attribute, GraffitiViewComponent>();
	}
	
	/**
	 * Returns whether the given coordinates lie within this component and
	 * within its encapsulated shape. The coordinates are assumed to be
	 * relative to the coordinate system of this component.
	 * 
	 * @see java.awt.Component#contains(int, int)
	 */
	@Override
	public boolean contains(int x, int y) {
		return false;
		/*
		 * AffineTransform zoom = getZoom();
		 * Point2D p = null;
		 * try
		 * {
		 * p = zoom.inverseTransform(new Point2D.Double(x + getX(), y +
		 * getY()), null);
		 * }
		 * catch(NoninvertibleTransformException e)
		 * {
		 * }
		 * x = (int) (p.getX() - getX());
		 * y = (int) (p.getY() - getY());
		 * return (super.contains(x, y) && this.shape.contains(x, y));
		 */
	}
	
	/**
	 * Called when a graphic attribute of the GraphElement represented by this
	 * component has changed.
	 * 
	 * @param attr
	 *           the graphic attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	public synchronized void graphicAttributeChanged(Attribute attr)
						throws ShapeNotFoundException {
		/*
		 * if the type of the shape or the size changed then we have to
		 * rebuild the shape
		 */
		if (attr == null || attr.getId().equals(SHAPE) || (attr.getId().equals(GRAPHICS))) {
			for (Iterator<GraffitiViewComponent> it = attributeComponents.values().iterator(); it.hasNext();) {
				((AttributeComponent) it.next()).recreate();
			}
			
			createNewShape(CoordinateSystem.XY);
		} else { // if another graphic attribute changed only repaint is needed
		
			for (Iterator<GraffitiViewComponent> it = attributeComponents.values().iterator(); it.hasNext();) {
				((JComponent) it.next()).repaint();
			}
			
			repaint();
		}
	}
	
	/**
	 * Called when a non-graphic attribute of the GraphElement represented by
	 * this component has changed.
	 * 
	 * @param attr
	 *           the attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	public synchronized void nonGraphicAttributeChanged(Attribute attr)
						throws ShapeNotFoundException {
		Attribute runAttr = attr;
		
		while (!(attr == null) && !runAttr.getPath().equals("")) {
			if (attributeComponents.containsKey(runAttr)) {
				(attributeComponents.get(runAttr)).attributeChanged(attr);
				break;
			}
			
			// "else":
			runAttr = runAttr.getParent();
		}
	}
	
	/**
	 * Paints the graph element contained in this component.
	 * 
	 * @param g
	 *           the graphics context in which to paint.
	 * @see javax.swing.JComponent#paintComponent(Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawShape(g);
	}
	
	/**
	 * Removes a <code>GraffitiViewComponent</code> of an <code>Attribute</code> from collection of attribute components.
	 * 
	 * @param attr
	 *           the attribute that has to be removed
	 */
	public synchronized void removeAttributeComponent(Attribute attr) {
		attributeComponents.remove(attr);
	}
	
	/**
	 * Removes a <code>GraphElementComponent</code> from the list of dependent <code>GraphElementComponent</code>s.
	 * 
	 * @param comp
	 *           the <code>GraphElementComponent</code> that is removed from
	 *           the list of dependent components.
	 */
	public void removeDependentComponent(GraphElementComponent comp) {
		this.dependentComponents.remove(comp);
	}
	
	/**
	 * Retrieve the zoom value from the view this component is displayed in.
	 * 
	 * @return DOCUMENT ME!
	 */
	protected AffineTransform getZoom() {
		Container parent = getParent();
		
		if (parent instanceof Zoomable) {
			AffineTransform zoom = ((Zoomable) parent).getZoom();
			
			return zoom;
		} else
			return View.NO_ZOOM;
	}
	
	/**
	 * Draws the shape of the graph element contained in this component
	 * according to its graphic attributes.
	 * 
	 * @param g
	 *           the graphics context in which to draw.
	 */
	protected abstract void drawShape(Graphics g);
	
	/**
	 * Used when the shape changed in the datastructure. Makes the painter
	 * create a new shape.
	 */
	protected abstract void recreate()
						throws ShapeNotFoundException;
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
