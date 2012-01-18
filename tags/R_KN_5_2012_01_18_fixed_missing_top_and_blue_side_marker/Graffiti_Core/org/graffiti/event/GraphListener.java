// ==============================================================================
//
// GraphListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphListener.java,v 1.1 2011-01-31 09:05:01 klukas Exp $

package org.graffiti.event;

/**
 * Interface that contains methods which are called when a graph is changed.
 * 
 * @version $Revision: 1.1 $
 */
public interface GraphListener
					extends TransactionListener {
	// ~ Methods ================================================================
	
	/**
	 * Called after an edge has been added to the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void postEdgeAdded(GraphEvent e);
	
	/**
	 * Called after an edge has been removed from the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void postEdgeRemoved(GraphEvent e);
	
	/**
	 * Called after method <code>clear()</code> has been called on a graph. No
	 * other events (like remove events) are generated.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void postGraphCleared(GraphEvent e);
	
	/**
	 * Called after an edge has been added to the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void postNodeAdded(GraphEvent e);
	
	/**
	 * Called after a node has been removed from the graph. All edges incident
	 * to this node have already been removed (preEdgeRemoved and
	 * postEdgeRemoved have been called).
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void postNodeRemoved(GraphEvent e);
	
	/**
	 * Called just before an edge is added to the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void preEdgeAdded(GraphEvent e);
	
	/**
	 * Called just before an edge is removed from the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void preEdgeRemoved(GraphEvent e);
	
	/**
	 * Called before method <code>clear()</code> is called on a graph. No other
	 * events (like remove events) are generated.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void preGraphCleared(GraphEvent e);
	
	/**
	 * Called just before a node is added to the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void preNodeAdded(GraphEvent e);
	
	/**
	 * Called just before a node is removed from the graph. This method is
	 * called before the incident edges are deleted.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	public void preNodeRemoved(GraphEvent e);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
