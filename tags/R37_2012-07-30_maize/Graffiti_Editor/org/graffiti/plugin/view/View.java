// ==============================================================================
//
// View.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: View.java,v 1.1 2011-01-31 09:04:25 klukas Exp $

package org.graffiti.plugin.view;

import java.awt.AWTEvent;
import java.awt.dnd.Autoscroll;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import org.graffiti.event.AttributeListener;
import org.graffiti.event.EdgeListener;
import org.graffiti.event.GraphListener;
import org.graffiti.event.NodeListener;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.plugin.inspector.InspectorTab;

/**
 * Represents a view of a plugin.
 * 
 * @version $Revision: 1.1 $
 */
public interface View
					extends GraphListener, NodeListener, EdgeListener, AttributeListener,
					Autoscroll, ZoomListener, Zoomable // AttributeConsumer
{
	// ~ Static fields/initializers =============================================
	
	/** Standard zoom value. */
	public static final AffineTransform NO_ZOOM = new AffineTransform();
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the AttributeComponentManager used by this view.
	 */
	public void setAttributeComponentManager(AttributeComponentManager acm);
	
	/**
	 * Returns the map mapping <code>GraphElement</code>s with <code>GraphElementComponent</code>s.
	 * 
	 * @return DOCUMENT ME!
	 */
	public Map<?, ?> getComponentElementMap();
	
	/**
	 * Returns the main <code>GraphElementComponent</code> associated with the
	 * given <code>GraphElement</code>.
	 * 
	 * @param ge
	 *           <code>GraphElement</code> for which the component is wanted.
	 * @return the <code>GraphElementComponent</code> used to display the given <code>GraphELement</code>.
	 */
	public GraphElementComponent getComponentForElement(GraphElement ge);
	
	public Set<AttributeComponent> getAttributeComponentsForElement(GraphElement ge);
	
	/**
	 * Sets the graph of the view to the specified value.
	 * 
	 * @param graph
	 *           the new value of the graph.
	 */
	public void setGraph(Graph graph);
	
	public Graph getGraph();
	
	/**
	 * Returns the main component of the view.
	 * 
	 * @return the main component of the view.
	 */
	public JComponent getViewComponent();
	
	/**
	 * Returns the viewName.
	 * 
	 * @return String
	 */
	public String getViewName();
	
	public boolean putInScrollPane();
	
	// /**
	// * Returns the values for horizontal and vertical zoom encapsulated in a
	// * Point2D object. A value of 1.0 means no zoom is applied.
	// *
	// * @return Point2D see method description
	// */
	// public Point2D getZoom();
	
	/**
	 * Adds a message listener to the view. If the view have been started
	 * without editor instance, this method may be empty.
	 * 
	 * @param ml
	 *           a message listener
	 */
	public void addMessageListener(MessageListener ml);
	
	/**
	 * Closes the current view.
	 */
	public void close();
	
	/**
	 * Instructs the view to do completely refresh its contents.
	 */
	public void completeRedraw();
	
	/**
	 * Removes a message listener from the view.If the view have been started
	 * without editor instance, this method may be empty.
	 * 
	 * @param ml
	 *           a message listener
	 */
	public void removeMessageListener(MessageListener ml);
	
	/**
	 * Repaints the given graph element
	 * 
	 * @param ge
	 *           the <code>GraphElement</code> to repaint.
	 */
	public void repaint(GraphElement ge);
	
	/**
	 * @return Null, or a Double Object specifying the height (or a TableLayout-constant) of empty space or a JComponent, placed
	 *         above the graph view and sized according to its preferred height, the full width of the window
	 *         is used.
	 */
	public Object getViewToolbarComponentTop();
	
	/**
	 * @return Null, or a Double Object specifying the height (or a TableLayout-constant) of empty space or a JComponent, placed
	 *         below the graph view and sized according to its preferred height, the full width of the window
	 *         is used.
	 */
	public Object getViewToolbarComponentBottom();
	
	/**
	 * @return Null, or a Double Object specifying the width (or a TableLayout-constant) of empty space or a JComponent, placed
	 *         left of the graph view and sized according to its preferred height, the height of graph view
	 *         next to the component is used.
	 */
	public Object getViewToolbarComponentLeft();
	
	/**
	 * @return Null, or a Double Object specifying the width (or a TableLayout-constant) of empty space or a JComponent, placed
	 *         to the right of the graph view and sized according to its preferred height, the height of graph view
	 *         next to the component is used.
	 */
	public Object getViewToolbarComponentRight();
	
	/**
	 * @return Either null or JComponent, placed
	 *         behind the graph view. The size corresponds to the window size. Certain components may draw over
	 *         the graph view, resulting in redraw bugs. But a simple JComponent with hand-made paint-code should
	 *         work fine.
	 */
	public JComponent getViewToolbarComponentBackground();
	
	public void closing(AWTEvent e);
	
	public boolean worksWithTab(InspectorTab tab);
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
