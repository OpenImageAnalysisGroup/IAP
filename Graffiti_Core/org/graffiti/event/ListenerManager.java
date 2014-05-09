// ==============================================================================
//
// ListenerManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ListenerManager.java,v 1.1 2011-01-31 09:05:00 klukas Exp $

package org.graffiti.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.util.MultipleIterator;

/**
 * Class that is responsible to keep track of all the Listeners that are
 * registered. It therefore provides methods to add and remove certain types
 * of Listeners. It also contains methods representing all available events.
 * When one of these methods is called, the <code>ListenerManager</code> delegates the call to all Listeners registered in the appropriate listener
 * set. When a listener is registered as strict, it does not get any messages
 * during the time a transaction is active (i.e. between <code>transactionStarted</code> and <code>transactionFinished</code>). Non
 * strict listeners receive events independent of transactions. It is not
 * possible to add a Listener both as strict and non strict. At the end of a
 * transaction, a set is passed within a <code>TransactionEvent</code> that
 * contains all objects that (might) have been changed. This set is passed to
 * both, strict and non strict listeners.
 * 
 * @version $Revision: 1.1 $
 */
public class ListenerManager {
	// ~ Instance fields ========================================================
	
	/**
	 * Holds the list of registered NodeListeners that receive events even if a
	 * transaction is active.
	 */
	private Set<AttributeListener> alltimeAttributeListenerList;
	private Set<EdgeListener> alltimeEdgeListenerList;
	private Set<GraphListener> alltimeGraphListenerList;
	private Set<NodeListener> alltimeNodeListenerList;
	
	/**
	 * Holds the list of registered AttributeListeners that do not receive any
	 * events whenever a transaction is active.
	 */
	private Set<AttributeListener> delayedAttributeListenerList;
	private Set<EdgeListener> delayedEdgeListenerList;
	private Set<GraphListener> delayedGraphListenerList;
	private Set<NodeListener> delayedNodeListenerList;
	
	/** Logs the objects that get changed during a transaction. */
	private TransactionHashMap changedObjects;
	
	/**
	 * Indicates whether or, to be more exact, how many <code>transactionStarted</code> events have previously been encountered
	 * and strict Listeners not notified of events any longer if that number
	 * is greater than zero.
	 */
	private int transactionsActive = 0;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Default contructor. Initializes all listener sets as empty hash sets.
	 */
	public ListenerManager() {
		delayedNodeListenerList = new HashSet<NodeListener>();
		delayedEdgeListenerList = new HashSet<EdgeListener>();
		delayedAttributeListenerList = new HashSet<AttributeListener>();
		delayedGraphListenerList = new HashSet<GraphListener>();
		alltimeNodeListenerList = new HashSet<NodeListener>();
		alltimeEdgeListenerList = new HashSet<EdgeListener>();
		alltimeAttributeListenerList = new HashSet<AttributeListener>();
		alltimeGraphListenerList = new HashSet<GraphListener>();
		
		changedObjects = new TransactionHashMap();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Registers AttributeListener l by adding it to the list of nonstrict and
	 * strict transaction AttributeListeners.
	 * 
	 * @param l
	 *           the AttributeListener that is registered.
	 * @throws ListenerRegistrationException
	 *            DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addAllTimeAttributeListener(AttributeListener l)
						throws ListenerRegistrationException {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (!delayedAttributeListenerList.contains(l)) {
			if (!alltimeAttributeListenerList.contains(l)) {
				alltimeAttributeListenerList.add(l);
			}
		} else
			throw new ListenerRegistrationException(
								"Listener already registered as strict");
	}
	
	/**
	 * Registers EdgeListener l by adding it to the list of nonstrict
	 * transaction EdgeListeners.
	 * 
	 * @param l
	 *           the EdgeListener that is registered.
	 * @throws ListenerRegistrationException
	 *            DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addAllTimeEdgeListener(EdgeListener l)
						throws ListenerRegistrationException {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (!delayedEdgeListenerList.contains(l)) {
			if (!alltimeEdgeListenerList.contains(l)) {
				alltimeEdgeListenerList.add(l);
			}
		} else
			throw new ListenerRegistrationException(
								"Listener already registered as strict");
	}
	
	/**
	 * Registers GraphListener l by adding it to the list of GraphListeners.
	 * 
	 * @param l
	 *           the GraphListener that is registered.
	 * @throws ListenerRegistrationException
	 *            DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addAllTimeGraphListener(GraphListener l)
						throws ListenerRegistrationException {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (!delayedGraphListenerList.contains(l)) {
			if (!alltimeGraphListenerList.contains(l)) {
				alltimeGraphListenerList.add(l);
			}
		} else
			throw new ListenerRegistrationException(
								"Listener already registered as strict");
	}
	
	/**
	 * Registers <code>NodeListener</code> l by adding it to the list of
	 * nonstrict transaction NodeListeners.
	 * 
	 * @param l
	 *           the NodeListener that is registered.
	 * @throws ListenerRegistrationException
	 *            DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addAllTimeNodeListener(NodeListener l)
						throws ListenerRegistrationException {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (!delayedNodeListenerList.contains(l)) {
			if (!alltimeNodeListenerList.contains(l)) {
				alltimeNodeListenerList.add(l);
			}
		} else
			throw new ListenerRegistrationException(
								"Listener already registered as strict");
	}
	
	/**
	 * Registers AttributeListener l by adding it to the list of strict
	 * transaction AttributeListeners.
	 * 
	 * @param l
	 *           the AttributeListener that is registered.
	 * @throws ListenerRegistrationException
	 *            DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addDelayedAttributeListener(AttributeListener l)
						throws ListenerRegistrationException {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (!alltimeAttributeListenerList.contains(l)) {
			if (!delayedAttributeListenerList.contains(l)) {
				delayedAttributeListenerList.add(l);
			}
		} else
			throw new ListenerRegistrationException(
								"Listener already registered as non strict");
	}
	
	/**
	 * Registers EdgeListener l by adding it to the list of strict transaction
	 * EdgeListeners.
	 * 
	 * @param l
	 *           the EdgeListener that is registered.
	 * @throws ListenerRegistrationException
	 *            DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addDelayedEdgeListener(EdgeListener l)
						throws ListenerRegistrationException {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (!alltimeEdgeListenerList.contains(l)) {
			if (!delayedEdgeListenerList.contains(l)) {
				delayedEdgeListenerList.add(l);
			}
		} else
			throw new ListenerRegistrationException(
								"Listener already registered as non strict");
	}
	
	/**
	 * Registers GraphListener l by adding it to the list of strict
	 * transaction GraphListeners.
	 * 
	 * @param l
	 *           the GraphListener that is registered.
	 * @throws ListenerRegistrationException
	 *            DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addDelayedGraphListener(GraphListener l)
						throws ListenerRegistrationException {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (!alltimeGraphListenerList.contains(l)) {
			if (!delayedGraphListenerList.contains(l)) {
				delayedGraphListenerList.add(l);
			}
		} else
			throw new ListenerRegistrationException(
								"Listener already registered as non strict");
	}
	
	/**
	 * Registers <code>NodeListener</code> l by adding it to the list of strict
	 * transaction NodeListeners.
	 * 
	 * @param l
	 *           the NodeListener that is registered.
	 * @throws ListenerRegistrationException
	 *            DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void addDelayedNodeListener(NodeListener l)
						throws ListenerRegistrationException {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (!alltimeNodeListenerList.contains(l)) {
			if (!delayedNodeListenerList.contains(l)) {
				delayedNodeListenerList.add(l);
			}
		} else
			throw new ListenerRegistrationException(
								"Listener already registered as non strict");
	}
	
	/**
	 * Called after an attribute has been added. Calls the same method in all <code>AttributeListeners</code> in the <code>strictAttributeListenerList</code>
	 * 
	 * @param event
	 *           the AttributeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postAttributeAdded(AttributeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Collection<AttributeListener> list = new ArrayList<AttributeListener>(delayedAttributeListenerList);
			for (AttributeListener al : list) {
				al.postAttributeAdded(event);
			}
		} else {
			changedObjects.put(event.getAttributeable(), event);
		}
		for (AttributeListener al : alltimeAttributeListenerList) {
			al.postAttributeAdded(event);
		}
	}
	
	/**
	 * Called after an attribute has been changed. Calls the same method in all <code>AttributeListeners</code> in the <code>strictAttributeListenerList</code>
	 * 
	 * @param event
	 *           the AttributeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postAttributeChanged(AttributeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			for (AttributeListener al : delayedAttributeListenerList) {
				al.postAttributeChanged(event);
			}
		} else {
			changedObjects.put(event.getAttributeable(), event);
		}
		for (AttributeListener al : alltimeAttributeListenerList) {
			al.postAttributeChanged(event);
		}
	}
	
	/**
	 * Called after an attribute has been removed. Calls the same method in all <code>AttributeListeners</code> in the <code>strictAttributeListenerList</code>
	 * 
	 * @param event
	 *           the AttributeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postAttributeRemoved(AttributeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			for (AttributeListener al : delayedAttributeListenerList) {
				al.postAttributeRemoved(event);
			}
		} else {
			changedObjects.put(event.getAttributeable(), event);
		}
		for (AttributeListener al : alltimeAttributeListenerList) {
			al.postAttributeRemoved(event);
		}
	}
	
	/**
	 * Called after the edge was set directed or undirected. Calls the same
	 * method in all <code>EdgeListeners</code> in the <code>strictEdgeListenerList</code>
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postDirectedChanged(EdgeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<EdgeListener> it = delayedEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postDirectedChanged(event);
			}
			
			it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postDirectedChanged(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			Iterator<EdgeListener> it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postDirectedChanged(event);
			}
		}
	}
	
	/**
	 * Called after an edge has been added to the graph. Calls the same method
	 * in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postEdgeAdded(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postEdgeAdded(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postEdgeAdded(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postEdgeAdded(event);
			}
		}
	}
	
	/**
	 * Called after an edge has been removed from the graph. Calls the same
	 * method in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postEdgeRemoved(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postEdgeRemoved(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postEdgeRemoved(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			changedObjects.put(event.getAttributeable(), event.getAttributeable());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postEdgeRemoved(event);
			}
		}
	}
	
	/**
	 * Called after the edge has been reversed. Calls the same method in all <code>EdgeListeners</code> in the <code>strictEdgeListenerList</code>
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postEdgeReversed(EdgeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<EdgeListener> it = delayedEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postEdgeReversed(event);
			}
			
			it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postEdgeReversed(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			Iterator<EdgeListener> it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postEdgeReversed(event);
			}
		}
	}
	
	/**
	 * Called after method <code>clear()</code> has been called on a graph.
	 * Calls the same method in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postGraphCleared(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postGraphCleared(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postGraphCleared(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postGraphCleared(event);
			}
		}
	}
	
	// /**
	// * Called just after an incoming edge has been added to the node. (For
	// * undirected edges postUndirectedEdgeAdded is called instead.) Calls the
	// * same method in all NodeListeners in the strictNodeListenerList
	// *
	// * @param event the NodeEvent detailing the changes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void postInEdgeAdded(NodeEvent event) {
	// if (event == null)
	// throw new IllegalArgumentException("The argument " + "may not be null");
	//
	// if (transactionsActive == 0) {
	// Iterator it = delayedNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postInEdgeAdded(event);
	// }
	//
	// it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postInEdgeAdded(event);
	// }
	// } else {
	// // log objects that are (probably) affected
	// changedObjects.put(event.getAttributeable(), event.getSource());
	//
	// if (event.getEdge() != null)
	// changedObjects.put(event.getAttributeable(), event.getEdge());
	//
	// Iterator it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postInEdgeAdded(event);
	// }
	// }
	// }
	//
	// /**
	// * Called after an incoming edge has been removed from the node. (For
	// * undirected edges postUndirectedEdgeRemoved is called.) Calls the same
	// * method in all NodeListeners in the strictNodeListenerList
	// *
	// * @param event the NodeEvent detailing the changes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void postInEdgeRemoved(NodeEvent event) {
	// if (event == null)
	// throw new IllegalArgumentException("The argument " + "may not be null");
	//
	// if (transactionsActive == 0) {
	// Iterator it = delayedNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postInEdgeRemoved(event);
	// }
	//
	// it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postInEdgeRemoved(event);
	// }
	// } else {
	// // log objects that are (probably) affected
	// changedObjects.put(event.getAttributeable(), event.getSource());
	//
	// if (event.getEdge() != null)
	// changedObjects.put(event.getAttributeable(), event.getEdge());
	//
	// Iterator it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postInEdgeRemoved(event);
	// }
	// }
	// }
	
	/**
	 * Called after an edge has been added to the graph. Calls the same method
	 * in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postNodeAdded(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postNodeAdded(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postNodeAdded(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postNodeAdded(event);
			}
		}
	}
	
	/**
	 * Called after a node has been removed from the graph. Calls the same
	 * method in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postNodeRemoved(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postNodeRemoved(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postNodeRemoved(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).postNodeRemoved(event);
			}
		}
	}
	
	// /**
	// * Called after an outgoing edge has been added to the node. (For
	// * undirected edges postUndirectedEdgeAdded is called instead.) Calls the
	// * same method in all NodeListeners in the strictNodeListenerList
	// *
	// * @param event the NodeEvent detailing the changes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void postOutEdgeAdded(NodeEvent event) {
	// if (event == null)
	// throw new IllegalArgumentException("The argument " + "may not be null");
	//
	// if (transactionsActive == 0) {
	// Iterator it = delayedNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postOutEdgeAdded(event);
	// }
	//
	// it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postOutEdgeAdded(event);
	// }
	// } else {
	// // log objects that are (probably) affected
	// changedObjects.put(event.getAttributeable(), event.getSource());
	//
	// if (event.getEdge() != null)
	// changedObjects.put(event.getAttributeable(), event.getEdge());
	//
	// Iterator it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postOutEdgeAdded(event);
	// }
	// }
	// }
	//
	// /**
	// * Called after an outgoing edge has been removed from the node. (For
	// * undirected edges postUndirectedEdgeRemoved is called.) Calls the same
	// * method in all NodeListeners in the strictNodeListenerList
	// *
	// * @param event the NodeEvent detailing the changes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void postOutEdgeRemoved(NodeEvent event) {
	// if (event == null)
	// throw new IllegalArgumentException("The argument " + "may not be null");
	//
	// if (transactionsActive == 0) {
	// Iterator it = delayedNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postOutEdgeRemoved(event);
	// }
	//
	// it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postOutEdgeRemoved(event);
	// }
	// } else {
	// // log objects that are (probably) affected
	// changedObjects.put(event.getAttributeable(), event.getSource());
	//
	// if (event.getEdge() != null)
	// changedObjects.put(event.getAttributeable(), event.getEdge());
	//
	// Iterator it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).postOutEdgeRemoved(event);
	// }
	// }
	// }
	
	/**
	 * Called after the source node of an edge has changed. Calls the same
	 * method in all <code>EdgeListeners</code> in the <code>strictEdgeListenerList</code>
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postSourceNodeChanged(EdgeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<EdgeListener> it = delayedEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postSourceNodeChanged(event);
			}
			
			it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postSourceNodeChanged(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			Iterator<EdgeListener> it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postSourceNodeChanged(event);
			}
		}
	}
	
	/**
	 * Called after the target node of an edge has changed. Calls the same
	 * method in all <code>EdgeListeners</code> in the <code>strictEdgeListenerList</code>
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postTargetNodeChanged(EdgeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<EdgeListener> it = delayedEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postTargetNodeChanged(event);
			}
			
			it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postTargetNodeChanged(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			Iterator<EdgeListener> it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postTargetNodeChanged(event);
			}
		}
	}
	
	/**
	 * Called after an (undirected) edge has been added to the node. (For
	 * directed edges pre- In/Out- EdgeAdded is called.) Calls the same method
	 * in all NodeListeners in the strictNodeListenerList
	 * 
	 * @param event
	 *           the NodeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postUndirectedEdgeAdded(NodeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<NodeListener> it = delayedNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).postUndirectedEdgeAdded(event);
			}
			
			it = alltimeNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).postUndirectedEdgeAdded(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			Iterator<NodeListener> it = alltimeNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).postUndirectedEdgeAdded(event);
			}
		}
	}
	
	/**
	 * Called after an (undirected) edge has been removed from the node. (For
	 * directed edges pre- In/Out- EdgeRemoved is called.) Calls the same
	 * method in all NodeListeners in the strictNodeListenerList
	 * 
	 * @param event
	 *           the NodeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void postUndirectedEdgeRemoved(NodeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<NodeListener> it = delayedNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).postUndirectedEdgeRemoved(event);
			}
			
			it = alltimeNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).postUndirectedEdgeRemoved(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			Iterator<NodeListener> it = alltimeNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).postUndirectedEdgeRemoved(event);
			}
		}
	}
	
	/**
	 * Called just before an attribute is added. Calls the same method in all <code>AttributeListeners</code> in the <code>strictAttributeListenerList</code>
	 * 
	 * @param event
	 *           the AttributeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preAttributeAdded(AttributeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			LinkedList<AttributeListener> ll = new LinkedList<AttributeListener>();
			ll.addAll(delayedAttributeListenerList);
			for (AttributeListener l : ll) {
				l.preAttributeAdded(event);
			}
			
			ll.clear();
			ll.addAll(alltimeAttributeListenerList);
			
			for (AttributeListener l : ll) {
				l.preAttributeAdded(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getAttribute());
			
			Iterator<AttributeListener> it = alltimeAttributeListenerList.iterator();
			
			while (it.hasNext()) {
				((AttributeListener) it.next()).preAttributeAdded(event);
			}
		}
	}
	
	/**
	 * Called before a change of an attribute takes place. Calls the same
	 * method in all <code>AttributeListeners</code> in the <code>strictAttributeListenerList</code>
	 * 
	 * @param event
	 *           the AttributeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preAttributeChanged(AttributeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<AttributeListener> it = delayedAttributeListenerList.iterator();
			
			while (it.hasNext()) {
				((AttributeListener) it.next()).preAttributeChanged(event);
			}
			
			it = alltimeAttributeListenerList.iterator();
			
			while (it.hasNext()) {
				((AttributeListener) it.next()).preAttributeChanged(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event);
			
			Iterator<AttributeListener> it = alltimeAttributeListenerList.iterator();
			
			while (it.hasNext()) {
				((AttributeListener) it.next()).preAttributeChanged(event);
			}
		}
	}
	
	/**
	 * Called just before an attribute is removed. Calls the same method in all <code>AttributeListeners</code> in the <code>strictAttributeListenerList</code>
	 * 
	 * @param event
	 *           the AttributeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preAttributeRemoved(AttributeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<AttributeListener> it = delayedAttributeListenerList.iterator();
			
			while (it.hasNext()) {
				((AttributeListener) it.next()).preAttributeRemoved(event);
			}
			
			it = alltimeAttributeListenerList.iterator();
			
			while (it.hasNext()) {
				((AttributeListener) it.next()).preAttributeRemoved(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getAttribute());
			
			Iterator<AttributeListener> it = alltimeAttributeListenerList.iterator();
			
			while (it.hasNext()) {
				((AttributeListener) it.next()).preAttributeRemoved(event);
			}
		}
	}
	
	/**
	 * Called before the edge is set directed or undirected. Calls the same
	 * method in all <code>EdgeListeners</code> in the <code>strictEdgeListenerList</code>
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preDirectedChanged(EdgeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<EdgeListener> it = delayedEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preDirectedChanged(event);
			}
			
			it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preDirectedChanged(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			Iterator<EdgeListener> it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preDirectedChanged(event);
			}
		}
	}
	
	/**
	 * Called just before an edge is added to the graph. Calls the same method
	 * in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preEdgeAdded(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preEdgeAdded(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preEdgeAdded(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preEdgeAdded(event);
			}
		}
	}
	
	/**
	 * Called just before an edge is removed from the graph. Calls the same
	 * method in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preEdgeRemoved(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preEdgeRemoved(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preEdgeRemoved(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preEdgeRemoved(event);
			}
		}
	}
	
	/**
	 * Called before the edge is going to be reversed. Calls the same method in
	 * all <code>EdgeListeners</code> in the <code>strictEdgeListenerList</code>
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preEdgeReversed(EdgeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<EdgeListener> it = delayedEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preEdgeReversed(event);
			}
			
			it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preEdgeReversed(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			Iterator<EdgeListener> it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).postEdgeReversed(event);
			}
		}
	}
	
	/**
	 * Called before method <code>clear()</code> is called on a graph. Calls
	 * the same method in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preGraphCleared(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preGraphCleared(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preGraphCleared(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preGraphCleared(event);
			}
		}
	}
	
	// /**
	// * Called just before an incoming edge is added to the node. (For
	// * undirected edges preUndirectedEdgeAdded is called instead.) Calls the
	// * same method in all NodeListeners in the strictNodeListenerList
	// *
	// * @param event the NodeEvent detailing the changes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void preInEdgeAdded(NodeEvent event) {
	// if (event == null)
	// throw new IllegalArgumentException("The argument " + "may not be null");
	//
	// if (transactionsActive == 0) {
	// Iterator it = delayedNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preInEdgeAdded(event);
	// }
	//
	// it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preInEdgeAdded(event);
	// }
	// } else {
	// // log objects that are (probably) affected
	// changedObjects.put(event.getAttributeable(), event.getSource());
	//
	// if (event.getEdge() != null)
	// changedObjects.put(event.getAttributeable(), event.getEdge());
	//
	// Iterator it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preInEdgeAdded(event);
	// }
	// }
	// }
	//
	// /**
	// * Called just before an incoming edge is removed from the node. (For
	// * undirected edges preUndirectedEdgeRemoved is called.) Calls the same
	// * method in all NodeListeners in the strictNodeListenerList
	// *
	// * @param event the NodeEvent detailing the changes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void preInEdgeRemoved(NodeEvent event) {
	// if (event == null)
	// throw new IllegalArgumentException("The argument " + "may not be null");
	//
	// if (transactionsActive == 0) {
	// Iterator it = delayedNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preInEdgeRemoved(event);
	// }
	//
	// it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preInEdgeRemoved(event);
	// }
	// } else {
	// // log objects that are (probably) affected
	// changedObjects.put(event.getAttributeable(), event.getSource());
	//
	// if (event.getEdge() != null)
	// changedObjects.put(event.getAttributeable(), event.getEdge());
	//
	// Iterator it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preInEdgeRemoved(event);
	// }
	// }
	// }
	
	/**
	 * Called just before a node is added to the graph. Calls the same method
	 * in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preNodeAdded(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preNodeAdded(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preNodeAdded(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preNodeAdded(event);
			}
		}
	}
	
	/**
	 * Called just before a node is removed from the graph. Calls the same
	 * method in all <code>GraphListeners</code> in the <code>strictGraphListenerList</code>
	 * 
	 * @param event
	 *           the GraphEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preNodeRemoved(GraphEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<GraphListener> it = delayedGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preNodeRemoved(event);
			}
			
			it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preNodeRemoved(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			if (event.getNode() != null)
				changedObjects.put(event.getAttributeable(), event.getNode());
			
			if (event.getSecondNode() != null)
				changedObjects.put(event.getAttributeable(), event.getSecondNode());
			
			Iterator<GraphListener> it = alltimeGraphListenerList.iterator();
			
			while (it.hasNext()) {
				((GraphListener) it.next()).preNodeRemoved(event);
			}
		}
	}
	
	// /**
	// * Called just before an outgoing edge is added to the node. (For
	// * undirected edges preUndirectedEdgeAdded is called instead.) Calls the
	// * same method in all NodeListeners in the nodeListenerList
	// *
	// * @param event the NodeEvent detailing the changes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void preOutEdgeAdded(NodeEvent event) {
	// if (event == null)
	// throw new IllegalArgumentException("The argument " + "may not be null");
	//
	// if (transactionsActive == 0) {
	// Iterator it = delayedNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preOutEdgeAdded(event);
	// }
	//
	// it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preOutEdgeAdded(event);
	// }
	// } else {
	// // log objects that are (probably) affected
	// changedObjects.put(event.getAttributeable(), event.getSource());
	//
	// if (event.getEdge() != null)
	// changedObjects.put(event.getAttributeable(), event.getEdge());
	//
	// Iterator it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preOutEdgeAdded(event);
	// }
	// }
	// }
	//
	// /**
	// * Called just before an outgoing edge is removed from the node. (For
	// * undirected edges preUndirectedEdgeRemoved is called.) Calls the same
	// * method in all NodeListeners in the strictNodeListenerList
	// *
	// * @param event the NodeEvent detailing the changes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void preOutEdgeRemoved(NodeEvent event) {
	// if (event == null)
	// throw new IllegalArgumentException("The argument " + "may not be null");
	//
	// if (transactionsActive == 0) {
	// Iterator it = delayedNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preOutEdgeRemoved(event);
	// }
	//
	// it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preOutEdgeRemoved(event);
	// }
	// } else {
	// // log objects that are (probably) affected
	// changedObjects.put(event.getAttributeable(), event.getSource());
	//
	// if (event.getEdge() != null)
	// changedObjects.put(event.getAttributeable(), event.getEdge());
	//
	// Iterator it = alltimeNodeListenerList.iterator();
	//
	// while (it.hasNext()) {
	// ((NodeListener) it.next()).preOutEdgeRemoved(event);
	// }
	// }
	// }
	
	/**
	 * Called before a change of the source node of an edge takes place. Calls
	 * the same method in all <code>EdgeListeners</code> in the <code>strictEdgeListenerList</code>
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preSourceNodeChanged(EdgeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<EdgeListener> it = delayedEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preSourceNodeChanged(event);
			}
			
			it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preSourceNodeChanged(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			Iterator<EdgeListener> it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preSourceNodeChanged(event);
			}
		}
	}
	
	/**
	 * Called before a change of the target node of an edge takes place. Calls
	 * the same method in all <code>EdgeListeners</code> in the <code>strictEdgeListenerList</code>
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preTargetNodeChanged(EdgeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<EdgeListener> it = delayedEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preTargetNodeChanged(event);
			}
			
			it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preTargetNodeChanged(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			Iterator<EdgeListener> it = alltimeEdgeListenerList.iterator();
			
			while (it.hasNext()) {
				((EdgeListener) it.next()).preTargetNodeChanged(event);
			}
		}
	}
	
	/**
	 * Called just before an (undirected) edge is added to the node. (For
	 * directed edges pre- In/Out- EdgeAdded is called.) Calls the same method
	 * in all NodeListeners in the strictNodeListenerList
	 * 
	 * @param event
	 *           the NodeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preUndirectedEdgeAdded(NodeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<NodeListener> it = delayedNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).preUndirectedEdgeAdded(event);
			}
			
			it = alltimeNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).preUndirectedEdgeAdded(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			Iterator<NodeListener> it = alltimeNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).preUndirectedEdgeAdded(event);
			}
		}
	}
	
	/**
	 * Called just before an (undirected) edge is removed from the node. (For
	 * directed edges pre- In/Out- EdgeRemoved is called.) Calls the same
	 * method in all NodeListeners in the strictNodeListenerList
	 * 
	 * @param event
	 *           the NodeEvent detailing the changes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void preUndirectedEdgeRemoved(NodeEvent event) {
		if (event == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		
		if (transactionsActive == 0) {
			Iterator<NodeListener> it = delayedNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).preUndirectedEdgeRemoved(event);
			}
			
			it = alltimeNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).preUndirectedEdgeRemoved(event);
			}
		} else {
			// log objects that are (probably) affected
			changedObjects.put(event.getAttributeable(), event.getSource());
			
			if (event.getEdge() != null)
				changedObjects.put(event.getAttributeable(), event.getEdge());
			
			Iterator<NodeListener> it = alltimeNodeListenerList.iterator();
			
			while (it.hasNext()) {
				((NodeListener) it.next()).preUndirectedEdgeRemoved(event);
			}
		}
	}
	
	/**
	 * Unregisters AttributeListener l by removing it from the list of
	 * AttributeListeners.
	 * 
	 * @param l
	 *           the AttributeListener that is unregistered.
	 * @exception ListenerNotFoundException
	 *               if the listener to delete cannot be
	 *               found in the listener list.
	 */
	public void removeAttributeListener(AttributeListener l)
						throws ListenerNotFoundException {
		if (!delayedAttributeListenerList.remove(l)
							&& !alltimeAttributeListenerList.remove(l)) {
			throw new ListenerNotFoundException("The attr. listener you want "
								+ "to remove cannot be found.");
		}
	}
	
	/**
	 * Unregisters EdgeListener l by removing it from the list of
	 * EdgeListeners.
	 * 
	 * @param l
	 *           the EdgeListener that is unregistered.
	 * @exception ListenerNotFoundException
	 *               if the listener to delete cannot be
	 *               found in the listener list.
	 */
	public void removeEdgeListener(EdgeListener l)
						throws ListenerNotFoundException {
		if (!delayedEdgeListenerList.remove(l)
							&& !alltimeEdgeListenerList.remove(l)) {
			throw new ListenerNotFoundException("The edge listener you want "
								+ "to remove cannot be found.");
		}
	}
	
	/**
	 * Unregisters GraphListener l by removing it from the list of nonstrict
	 * and strict transaction GraphListeners.
	 * 
	 * @param l
	 *           the GraphListener that is unregistered.
	 * @exception ListenerNotFoundException
	 *               if the listener to delete cannot be
	 *               found in the listener list.
	 */
	public void removeGraphListener(GraphListener l)
						throws ListenerNotFoundException {
		if (!delayedGraphListenerList.remove(l)
							&& !alltimeGraphListenerList.remove(l)) {
			throw new ListenerNotFoundException("The graph listener you want "
								+ "to remove cannot be found.");
		}
	}
	
	/**
	 * Unregisters <code>NodeListener</code> l by removing it from the list of
	 * NodeListeners
	 * 
	 * @param l
	 *           the NodeListener that is unregistered.
	 * @exception ListenerNotFoundException
	 *               if the listener to delete cannot be
	 *               found in the listener list.
	 */
	public void removeNodeListener(NodeListener l)
						throws ListenerNotFoundException {
		if (!delayedNodeListenerList.remove(l)
							&& !alltimeNodeListenerList.remove(l)) {
			throw new ListenerNotFoundException("The node listener you want "
								+ "to remove cannot be found.");
		}
	}
	
	private static ArrayList<Object> runningTransactions = new ArrayList<Object>();
	
	private static void postDebugTransactionStarted(Object source) {
		synchronized (runningTransactions) {
			runningTransactions.add(source);
		}
	}
	
	private static void postDebugTransactionFinished(Object source) {
		synchronized (runningTransactions) {
			runningTransactions.remove(source);
		}
	}
	
	public void finishOpenTransactions() {
		synchronized (runningTransactions) {
			while (transactionsActive > 0)
				transactionFinished(runningTransactions.get(0));
		}
	}
	
	/**
	 * Called when a transaction has finished. Changes the event it gets by
	 * reusing the <code>source</code> but adding the <code>Set</code> of
	 * (probably) changed objects.
	 * 
	 * @param source
	 *           the object, which initiated the end of the transaction.
	 */
	public void transactionFinished(Object source) {
		transactionFinished(source, false);
	}
	
	public void transactionFinished(Object source, boolean forgetChanges) {
		transactionFinished(source, forgetChanges, null);
	}
	
	public void transactionFinished(Object source, boolean forgetChanges, BackgroundTaskStatusProviderSupportingExternalCall status) {
		postDebugTransactionFinished(source);
		this.transactionsActive--;
		assert this.transactionsActive >= 0;
		
		if (forgetChanges)
			this.changedObjects = new TransactionHashMap();
		
		if (transactionsActive > 0)
			return;
		TransactionEvent event = new TransactionEvent(source, changedObjects);
		
		Iterator<?> mIter = new MultipleIterator(new Iterator[] {
							delayedNodeListenerList.iterator(),
							delayedEdgeListenerList.iterator(),
							delayedAttributeListenerList.iterator(),
							delayedGraphListenerList.iterator(),
							alltimeNodeListenerList.iterator(),
							alltimeEdgeListenerList.iterator(),
							alltimeAttributeListenerList.iterator(),
							alltimeGraphListenerList.iterator() });
		if (status != null)
			status.setCurrentStatusValue(-1);
		if (status != null)
			status.setCurrentStatusText1("Processing graph changes (" + changedObjects.size() + ")...");
		while (mIter.hasNext()) {
			TransactionListener l = (TransactionListener) mIter.next();
			if (status != null)
				status.setCurrentStatusText2("Inform listener " + l.getClass().getSimpleName());
			l.transactionFinished(event, status);
		}
		if (status != null)
			status.setCurrentStatusValue(100);
		
		// only clear list when no transactions are active
		if (transactionsActive == 0)
			this.changedObjects = new TransactionHashMap();
	}
	
	/**
	 * Called when a transaction has started.
	 * 
	 * @param source
	 *           the object, which initiated the transaction.
	 */
	public void transactionStarted(Object source) {
		postDebugTransactionStarted(source);
		TransactionEvent event = new TransactionEvent(source);
		this.transactionsActive++;
		
		Iterator<?> mIter = new MultipleIterator(new Iterator[] {
							delayedNodeListenerList.iterator(),
							delayedEdgeListenerList.iterator(),
							delayedAttributeListenerList.iterator(),
							delayedGraphListenerList.iterator(),
							alltimeNodeListenerList.iterator(),
							alltimeEdgeListenerList.iterator(),
							alltimeAttributeListenerList.iterator(),
							alltimeGraphListenerList.iterator() });
		
		while (mIter.hasNext()) {
			((TransactionListener) mIter.next()).transactionStarted(event);
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
