// ==============================================================================
//
// AbstractGraphElement.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractGraphElement.java,v 1.1 2011-01-31 09:04:45 klukas Exp $

package org.graffiti.graph;

import org.graffiti.attributes.AbstractAttributable;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.event.ListenerManager;

/**
 * GraphElements are Attributables which know the graph they belong to. This
 * class provides the functionality for accessing the graph.
 * 
 * @see AbstractNode
 * @see AbstractEdge
 * @see Node
 * @see Edge
 */
public abstract class AbstractGraphElement
					extends AbstractAttributable
					implements GraphElement {
	// ~ Static fields/initializers =============================================
	
	/** The logger for the current class. */
	// static protected final Logger logger = Logger.getLogger(AbstractGraphElement.class.getName());
	
	// ~ Instance fields ========================================================
	
	/** The graph the current <code>AbstractGraphElement</code> belongs to. */
	protected Graph graph;
	
	private long id;
	
	/**
	 * Used for sorting the graphical output in Z-order. (e.g. nodes before edges)
	 */
	private int viewID;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>AbstrctGraphElement</code>.
	 */
	public AbstractGraphElement() {
	}
	
	/**
	 * Constructs a new <code>AbstrctGraphElement</code>.
	 * 
	 * @param coll
	 *           the <code>CollectionAttribute</code> of the new <code>AbstractGraphElement</code> instance.
	 */
	public AbstractGraphElement(CollectionAttribute coll) {
		super(coll);
	}
	
	/**
	 * Constructs a new <code>AbstrctGraphElement</code>. Sets the graph of the
	 * current <code>AbstrctGraphElement</code>.
	 * 
	 * @param graph
	 *           the graph the <code>AbstrctGraphElement</code> belongs to.
	 * @param coll
	 *           the <code>CollectionAttribute</code> of the new <code>AbstractGraphElement</code> instance.
	 */
	public AbstractGraphElement(Graph graph, CollectionAttribute coll) {
		super(coll);
		assert graph != null;
		this.graph = graph;
		setID(IdGenereator.getNextID());
	}
	
	/**
	 * Constructs a new <code>AbstrctGraphElement</code>. Sets the graph of the
	 * current <code>AbstrctGraphElement</code>.
	 * 
	 * @param graph
	 *           the graph the <code>AbstrctGraphElement</code> belongs to.
	 */
	public AbstractGraphElement(Graph graph) {
		assert graph != null;
		this.graph = graph;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the Graph the <code>AbstractGraphElement</code> belongs to.
	 * 
	 * @return the Graph the GraphElement belongs to.
	 */
	public Graph getGraph() {
		return graph;
	}
	
	/**
	 * Returns the ListenerManager of the <code>GraphElement</code>.
	 * 
	 * @return the ListenerManager of the <code>GraphElement</code>.
	 */
	public ListenerManager getListenerManager() {
		// assert graph != null;
		if (graph == null)
			return null;
		else
			return getGraph().getListenerManager();
	}
	
	public void setViewID(int viewID) {
		this.viewID = viewID;
	}
	
	public int getViewID() {
		return viewID;
	}
	
	public int compareTo(AbstractGraphElement o) {
		if (getViewID() > ((GraphElement) o).getViewID())
			return -1;
		if (getViewID() < ((GraphElement) o).getViewID())
			return 1;
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.GraphElement#getID()
	 */
	public long getID() {
		return id;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.GraphElement#setID(int)
	 */
	public void setID(long id) {
		this.id = id;
		getGraph().checkMaxGraphElementId(id);
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
