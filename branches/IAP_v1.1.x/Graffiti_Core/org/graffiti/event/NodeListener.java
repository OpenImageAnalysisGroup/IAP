// ==============================================================================
//
// NodeListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeListener.java,v 1.1 2011-01-31 09:05:00 klukas Exp $

package org.graffiti.event;

/**
 * Interface that contains methods which are called when a node is changed.
 * 
 * @version $Revision: 1.1 $
 */
public interface NodeListener
					extends TransactionListener {
	// ~ Methods ================================================================
	
	// /**
	// * Called just after an incoming edge has been added to the node. (For
	// * undirected edges postUndirectedEdgeAdded is called instead.)
	// *
	// * @param e The NodeEvent detailing the changes.
	// */
	// public void postInEdgeAdded(NodeEvent e);
	//
	// /**
	// * Called after an incoming edge has been removed from the node. (For
	// * undirected edges postUndirectedEdgeRemoved is called.)
	// *
	// * @param e The NodeEvent detailing the changes.
	// */
	// public void postInEdgeRemoved(NodeEvent e);
	//
	// /**
	// * Called after an outgoing edge has been added to the node. (For
	// * undirected edges postUndirectedEdgeAdded is called instead.)
	// *
	// * @param e The NodeEvent detailing the changes.
	// */
	// public void postOutEdgeAdded(NodeEvent e);
	//
	// /**
	// * Called after an outgoing edge has been removed from the node. (For
	// * undirected edges postUndirectedEdgeRemoved is called.)
	// *
	// * @param e The NodeEvent detailing the changes.
	// */
	// public void postOutEdgeRemoved(NodeEvent e);
	
	/**
	 * Called after an (undirected) edge has been added to the node. (For
	 * directed edges pre- In/Out- EdgeAdded is called.)
	 * 
	 * @param e
	 *           The NodeEvent detailing the changes.
	 */
	public void postUndirectedEdgeAdded(NodeEvent e);
	
	/**
	 * Called after an (undirected) edge has been removed from the node. (For
	 * directed edges pre- In/Out- EdgeRemoved is called.)
	 * 
	 * @param e
	 *           The NodeEvent detailing the changes.
	 */
	public void postUndirectedEdgeRemoved(NodeEvent e);
	
	// /**
	// * Called just before an incoming edge is added to the node. (For
	// * undirected edges preUndirectedEdgeAdded is called instead.)
	// *
	// * @param e The NodeEvent detailing the changes.
	// */
	// public void preInEdgeAdded(NodeEvent e);
	//
	// /**
	// * Called just before an incoming edge is removed from the node. (For
	// * undirected edges preUndirectedEdgeRemoved is called.)
	// *
	// * @param e The NodeEvent detailing the changes.
	// */
	// public void preInEdgeRemoved(NodeEvent e);
	//
	// /**
	// * Called just before an outgoing edge is added to the node. (For
	// * undirected edges preUndirectedEdgeAdded is called instead.)
	// *
	// * @param e The NodeEvent detailing the changes.
	// */
	// public void preOutEdgeAdded(NodeEvent e);
	//
	// /**
	// * Called just before an outgoing edge is removed from the node. (For
	// * undirected edges preUndirectedEdgeRemoved is called.)
	// *
	// * @param e The NodeEvent detailing the changes.
	// */
	// public void preOutEdgeRemoved(NodeEvent e);
	
	/**
	 * Called just before an (undirected) edge is added to the node. (For
	 * directed edges pre- In/Out- EdgeAdded is called.)
	 * 
	 * @param e
	 *           The NodeEvent detailing the changes.
	 */
	public void preUndirectedEdgeAdded(NodeEvent e);
	
	/**
	 * Called just before an (undirected) edge is removed from the node. (For
	 * directed edges pre- In/Out- EdgeRemoved is called.)
	 * 
	 * @param e
	 *           The NodeEvent detailing the changes.
	 */
	public void preUndirectedEdgeRemoved(NodeEvent e);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
