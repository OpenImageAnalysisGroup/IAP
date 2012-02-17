// ==============================================================================
//
// GraphElementsDeletionEdit.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphElementsDeletionEdit.java,v 1.1 2011-01-31 09:05:05 klukas Exp $

package org.graffiti.undo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ErrorMsg;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

/**
 * <code>GraphElementsDeletionEdit</code> makes deletion of graph elements
 * undoable.
 * 
 * @author $Author $
 * @version $Revision: 1.1 $
 */
@SuppressWarnings("unchecked")
public class GraphElementsDeletionEdit
					extends GraphElementsEdit {
	// ~ Static fields/initializers =============================================
	
	private static final long serialVersionUID = 1L;
	
	/** The logger for the current class. */
	private static final Logger logger = Logger.getLogger(GraphElementsDeletionEdit.class.getName());
	
	// ~ Instance fields ========================================================
	
	/**
	 * this flag assures that execute method will be invoked before calling
	 * undo or redo methods
	 */
	protected boolean executed = false;
	
	/** set of graph elements that were selected for deletion. */
	private LinkedHashSet<GraphElement> graphElements;
	
	/**
	 * temporary graph element set. It is necessary for update of graph
	 * elements contained in the selection set.
	 */
	private Set<GraphElement> tempGraphElements;
	
	// ~ Constructors ===========================================================
	
	/**
	 * @see GraffitiAbstractUndoableEdit#GraffitiAbstractUndoableEdit(Map)
	 */
	public GraphElementsDeletionEdit(Collection<GraphElement> graphElemList, Graph graph,
						Map<GraphElement, GraphElement> geMap) {
		super(graph, geMap);
		this.graphElements = new LinkedHashSet<GraphElement>(graphElemList);
		tempGraphElements = new HashSet<GraphElement>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Used to display the name for this edit.
	 * 
	 * @return the name of this edit.
	 * @see javax.swing.undo.UndoableEdit
	 */
	@Override
	public String getPresentationName() {
		String name = "";
		
		if (graphElements.size() == 1) {
			if (graphElements.iterator().next() instanceof Node) {
				name = sBundle.getString("undo.deleteNode");
			} else
				if (graphElements.iterator().next() instanceof Edge) {
					name = sBundle.getString("undo.deleteEdge");
				}
		} else
			if (graphElements.size() > 1) {
				name = sBundle.getString("undo.deleteGraphElements");
			}
		
		return name;
	}
	
	/**
	 * Executes the deletion of selected grpah elements
	 */
	@Override
	public void execute() {
		executed = true;
		graph.getListenerManager().transactionStarted(this);
		/* saves adjacent edges of nodes in the common graph elements set. */
		Set<Edge> adjacentEdges = new HashSet<Edge>();
		
		for (Iterator iter = graphElements.iterator(); iter.hasNext();) {
			GraphElement ge = (GraphElement) iter.next();
			
			if (ge instanceof Node) {
				Collection<Edge> edgeList = ((Node) ge).getEdges();
				adjacentEdges.addAll(edgeList);
			}
		}
		
		graphElements.addAll(adjacentEdges);
		
		/* deletes all nodes contained in the selection set then. */
		for (Iterator iter = graphElements.iterator(); iter.hasNext();) {
			GraphElement ge = (GraphElement) iter.next();
			
			if (ge instanceof Node) {
				try {
					graph.deleteNode((Node) ge);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		
		/* deletes all edges contained in the selection set firstly. */
		for (Iterator iter = graphElements.iterator(); iter.hasNext();) {
			GraphElement ge = (GraphElement) iter.next();
			
			if (ge instanceof Edge) {
				if (ge.getGraph() != null)
					graph.deleteEdge((Edge) ge);
			}
		}
		
		graph.getListenerManager().transactionFinished(this);
	}
	
	/**
	 * Deletes the GraphElements stored in this edit.
	 */
	@Override
	public void redo() {
		super.redo();
		
		if (!executed) {
			logger.info("The execute method hasn't been invocated");
			ErrorMsg.addErrorMessage("The execute method hasn't been invocated");
			return;
		}
		
		graph.getListenerManager().transactionStarted(this);
		for (Iterator iter = graphElements.iterator(); iter.hasNext();) {
			GraphElement ge = (GraphElement) iter.next();
			
			if (ge instanceof Edge) {
				Edge newEdge = (Edge) ge; // (Edge) getNewGraphElement(ge);
				newEdge.setGraph(graph);
				tempGraphElements.add(newEdge);
				graph.deleteEdge(newEdge);
			}
		}
		
		for (Iterator iter = graphElements.iterator(); iter.hasNext();) {
			GraphElement ge = (GraphElement) iter.next();
			
			if (ge instanceof Node) {
				Node newNode = (Node) ge; // (Node) getNewGraphElement(ge);
				newNode.setGraph(graph);
				tempGraphElements.add(newNode);
				graph.deleteNode(newNode);
			}
		}
		
		/*
		 * updates all graph elements referencies in the set containing graph
		 * elements.
		 */
		graphElements.clear();
		graphElements.addAll(tempGraphElements);
		tempGraphElements.clear();
		graph.getListenerManager().transactionFinished(this);
	}
	
	/**
	 * Adds the deleted GraphElements stored in this edit.
	 */
	@Override
	public void undo() {
		super.undo();
		
		if (!executed) {
			logger.info("The execute method hasn't been invocated");
			ErrorMsg.addErrorMessage("The execute method hasn't been invocated");
			return;
		}
		graph.getListenerManager().transactionStarted(this);
		for (Iterator iter = graphElements.iterator(); iter.hasNext();) {
			GraphElement ge = (GraphElement) iter.next();
			
			if (ge instanceof Node) {
				Node newNode = graph.addNodeCopy((Node) ge);
				newNode.setGraph(graph);
				geMap.put(ge, newNode);
			}
		}
		
		for (Iterator iter = graphElements.iterator(); iter.hasNext();) {
			GraphElement ge = (GraphElement) iter.next();
			
			if (ge instanceof Edge) {
				// logger.info("undo the edge deleting");
				
				Edge oldEdge = (Edge) ge;
				GraphElement newSource = getNewGraphElement(oldEdge.getSource());
				GraphElement newTarget = getNewGraphElement(oldEdge.getTarget());
				Edge newEdge = graph.addEdgeCopy(oldEdge, (Node) newSource, (Node) newTarget);
				newEdge.setGraph(graph);
				geMap.put(oldEdge, newEdge);
			}
		}
		graph.getListenerManager().transactionFinished(this);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
