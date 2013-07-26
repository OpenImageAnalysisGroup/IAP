// ==============================================================================
//
// AdjListNode.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AdjListNode.java,v 1.1 2011-01-31 09:04:45 klukas Exp $

package org.graffiti.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.event.ListenerManager;
import org.graffiti.event.NodeEvent;
import org.graffiti.util.MultipleIterator;

/**
 * Implements a graph node with adjacency list representation.
 * 
 * @version $Revision: 1.1 $
 * @see AdjListGraph
 * @see AdjListEdge
 */
public class AdjListNode
					extends AbstractNode
					implements Node, GraphElement {
	// ~ Static fields/initializers =============================================
	
	/** The logger for the AdjListNode class. */
	// private static final Logger logger = Logger.getLogger(AdjListNode.class.getName());
	
	// ~ Instance fields ========================================================
	
	/**
	 * Contains all the directed ingoing edges of the current <code>Node</code>.
	 */
	private Set<Edge> directedInEdges;
	
	/**
	 * Contains all the directed outgoing edges of the current <code>Node</code>.
	 */
	private Set<Edge> directedOutEdges;
	
	/**
	 * Contains all the undirected edges connected to the current <code>Node</code>.
	 */
	private Set<Edge> undirectedEdges;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>AdjListNode</code>.
	 * 
	 * @param graph
	 *           the <code>Graph</code> the <code>Node</code> belongs to.
	 */
	protected AdjListNode(Graph graph) {
		super(graph);
		// logger.fine("Creating new instance of AdjListNode");
		directedInEdges = new LinkedHashSet<Edge>(); // new HashSet<Edge>();
		undirectedEdges = new LinkedHashSet<Edge>(); // new HashSet<Edge>();
		directedOutEdges = new LinkedHashSet<Edge>(); // new HashSet<Edge>();
	}
	
	/**
	 * Constructs a new <code>AdjListNode</code>.
	 * 
	 * @param graph
	 *           the <code>Graph</code> the <code>Node</code> belongs to.
	 * @param coll
	 *           the <code>CollectionAttribute</code> of the newly created <code>AdjListNode</code>.
	 */
	protected AdjListNode(Graph graph, CollectionAttribute coll) {
		super(graph, coll);
		// logger.fine("Creating new instance of AdjListNode");
		directedInEdges = new LinkedHashSet<Edge>(); // new HashSet<Edge>();
		undirectedEdges = new LinkedHashSet<Edge>(); // new HashSet<Edge>();
		directedOutEdges = new LinkedHashSet<Edge>(); // new HashSet<Edge>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns an iterator containing the directed ingoing edges of the <code>Node</code>.
	 * 
	 * @return an iterator containing the directed ingoing edges of the <code>Node</code>.
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Edge> getDirectedInEdgesIterator() {
		return new MultipleIterator(directedInEdges.iterator());
	}
	
	/**
	 * Returns an iterator containing the outgoing directed edges of the <code>Node</code>.
	 * 
	 * @return an iterator containing the outgoing directed edges of the <code>Node</code>.
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Edge> getDirectedOutEdgesIterator() {
		return new MultipleIterator(directedOutEdges.iterator());
	}
	
	/**
	 * Returns an iterator containing all the ingoing and outgoing directed and
	 * undirected edges of the current <code>Node</code>. Ingoing and outgoing
	 * edges will not be separated and there will be no ordering on the
	 * collection.
	 * 
	 * @return an iterator containing all ingoing and outgoing directed and
	 *         undirected edges of the current <code>Node</code>.
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Edge> getEdgesIterator() {
		return (Iterator) new MultipleIterator(directedInEdges.iterator(),
							undirectedEdges.iterator(), directedOutEdges.iterator());
	}
	
	/**
	 * Returns the in-degree of the current <code>Node</code>. The in-degree is
	 * defined as the number of ingoing, directed edges plus the number of
	 * undirected edges.
	 * 
	 * @return the in-degree of the current <code>Node</code>.
	 */
	@Override
	public int getInDegree() {
		return directedInEdges.size() + undirectedEdges.size();
	}
	
	/**
	 * Returns the out-degree of the current <code>Node</code>. The out-degree
	 * is defined as the number of outgoing, directed edges plus the number of
	 * undirected edges.
	 * 
	 * @return the out-degree of the current <code>Node</code>.
	 */
	@Override
	public int getOutDegree() {
		return directedOutEdges.size() + undirectedEdges.size();
	}
	
	/**
	 * Returns an iterator containing the undirected ingoing and outgoing edges
	 * of the <code>Node</code>.
	 * 
	 * @return a iterator containing the undirected ingoing and outgoing edges
	 *         of the <code>Node</code>.
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Edge> getUndirectedEdgesIterator() {
		return new MultipleIterator(undirectedEdges.iterator());
	}
	
	@Override
	public Collection<Edge> getEdges() {
		Set<Edge> c = new LinkedHashSet<Edge>();
		c.addAll(directedInEdges);
		c.addAll(directedOutEdges);
		c.addAll(undirectedEdges);
		return c;
	}
	
	/**
	 * Sets the <code>graph</code> member variable to <code>null</code>. <b>Be
	 * Careful:</b> This function should only be called when the node gets
	 * deleted.
	 */
	void setGraphToNull() {
		this.graph = null;
	}
	
	/**
	 * Adds a new ingoing <code>Edge</code> to the corresponding <code>Edge</code> list. Informs the ListenerManageer about the change.
	 * 
	 * @param edge
	 *           the <code>Edge</code> to be added.
	 */
	void addInEdge(AdjListEdge edge) {
		assert edge != null;
		
		ListenerManager listMan = getListenerManager();
		
		if (edge.isDirected()) {
			// logger.fine("adding an ingoing edge to this node");
			// if (listMan!=null)
			// listMan.preInEdgeAdded(new NodeEvent(this, edge));
			directedInEdges.add(edge);
			// if (listMan!=null)
			// listMan.postInEdgeAdded(new NodeEvent(this, edge));
		} else {
			// logger.fine("adding an undirected edge to this node");
			if (listMan != null)
				listMan.preUndirectedEdgeAdded(new NodeEvent(this, edge));
			undirectedEdges.add(edge);
			if (listMan != null)
				listMan.postUndirectedEdgeAdded(new NodeEvent(this, edge));
		}
		
		// logger.fine("exiting doAddEdge()");
	}
	
	/**
	 * Adds a new outgoing <code>Edge</code> to the corresponding <code>Edge</code> list. Informs the ListenerManageer about the change.
	 * 
	 * @param edge
	 *           the <code>Edge</code> to be added.
	 */
	void addOutEdge(AdjListEdge edge) {
		assert edge != null;
		
		ListenerManager listMan = getListenerManager();
		
		if (edge.isDirected()) {
			// logger.info("adding an outgoing edge to this node");
			// if (listMan!=null)
			// listMan.preOutEdgeAdded(new NodeEvent(this, edge));
			this.directedOutEdges.add(edge);
			// if (listMan!=null)
			// listMan.postOutEdgeAdded(new NodeEvent(this, edge));
		} else {
			// logger.info("adding an undirected edge to this node");
			if (listMan != null)
				listMan.preUndirectedEdgeAdded(new NodeEvent(this, edge));
			this.undirectedEdges.add(edge);
			if (listMan != null)
				listMan.postUndirectedEdgeAdded(new NodeEvent(this, edge));
		}
		
		// logger.fine("exiting addEdge()");
	}
	
	/**
	 * Removes an ingoing <code>Edge</code> from the corresponding <code>Edge</code> list. Informs the ListenerManager about the change.
	 * 
	 * @param edge
	 *           the <code>Edge</code> to remove.
	 * @exception GraphElementNotFoundException
	 *               if the <code>Edge</code> cannot
	 *               be found in any of the <code>Edge</code> lists.
	 */
	void removeInEdge(Edge edge)
						throws GraphElementNotFoundException {
		assert edge != null;
		
		ListenerManager listMan = getListenerManager();
		
		if (edge.isDirected()) {
			// logger.fine("removing an inEdge");
			if (directedInEdges.contains(edge)) {
				// if (listMan!=null)
				// listMan.preInEdgeRemoved(new NodeEvent(this, edge));
				directedInEdges.remove(edge);
				
				// if (listMan!=null)
				// listMan.postInEdgeRemoved(new NodeEvent(this, edge));
			} else {
				// logger.severe("Throwing GraphElementNotFoundException, " +
				// "because the edge was not found in the " +
				// "(apropriate) list of the node");
				throw new GraphElementNotFoundException(
									"The edge was not found in the (apropriate) list in " +
														"the node");
			}
		} else {
			// logger.fine("removing an undirected edge");
			
			if (undirectedEdges.contains(edge)) {
				if (listMan != null)
					listMan.preUndirectedEdgeRemoved(new NodeEvent(this, edge));
				this.undirectedEdges.remove(edge);
				if (listMan != null)
					listMan.postUndirectedEdgeRemoved(new NodeEvent(this, edge));
			} else {
				// logger.severe("Throwing GraphElementNotFoundException, " +
				// "because the edge was not found in the " +
				// "(apropriate) list of the node");
				throw new GraphElementNotFoundException(
									"The edge was not found in the (apropriate) list in " +
														"the node");
			}
		}
		
		// logger.fine("exiting removeEdge()");
	}
	
	/**
	 * Removes an outgoing <code>Edge</code> from the corresponding <code>Edge</code> list. Informs the ListenerManager about the change.
	 * 
	 * @param edge
	 *           the <code>Edge</code> to remove.
	 * @exception GraphElementNotFoundException
	 *               if the <code>Edge</code> cannot
	 *               be found in any of the <code>Edge</code> lists.
	 */
	void removeOutEdge(Edge edge)
						throws GraphElementNotFoundException {
		assert edge != null;
		
		ListenerManager listMan = getListenerManager();
		
		if (edge.isDirected()) {
			// logger.fine("removing a directed outEdge");
			
			if (directedOutEdges.contains(edge)) {
				// if (listMan!=null)
				// listMan.preOutEdgeRemoved(new NodeEvent(this, edge));
				this.directedOutEdges.remove(edge);
				// if (listMan!=null)
				// listMan.postOutEdgeRemoved(new NodeEvent(this, edge));
			} else {
				// logger.severe("Throwing GraphElementNotFoundException, " +
				// "because the edge was not found in the " +
				// "(apropriate) list of the node");
				
				// throw new GraphElementNotFoundException(
				// "The edge was not found in the (apropriate) list in " +
				// "the node");
			}
		} else {
			// logger.fine("removing an undirected outEdge");
			
			if (undirectedEdges.contains(edge)) {
				if (listMan != null)
					listMan.preUndirectedEdgeRemoved(new NodeEvent(this, edge));
				this.undirectedEdges.remove(edge);
				if (listMan != null)
					listMan.postUndirectedEdgeRemoved(new NodeEvent(this, edge));
			} else {
				// logger.severe("Throwing GraphElementNotFoundException, " +
				// "because the edge was not found in the " +
				// "(apropriate) list of the node");
				
				// throw new GraphElementNotFoundException(
				// "The edge was not found in the (apropriate) list in " +
				// "the node");
			}
		}
		
		// logger.fine("exiting removeEdge()");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Node#setGraph(org.graffiti.graph.AbstractGraph)
	 */
	public void setGraph(Graph graph) {
		assert graph != null;
		this.graph = graph;
	}
	
	@Override
	public String toString() {
		return "Node ID=" + getID();
	}
	
	public int getDegree() {
		return getEdges().size();
	}
	
	public int compareTo(GraphElement arg0) {
		return new Integer(getViewID()).compareTo(arg0.getViewID());
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
