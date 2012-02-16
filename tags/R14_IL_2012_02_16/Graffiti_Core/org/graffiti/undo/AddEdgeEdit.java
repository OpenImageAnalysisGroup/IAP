// ==============================================================================
//
// AddEdgeEdit.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AddEdgeEdit.java,v 1.1 2011-01-31 09:05:04 klukas Exp $

package org.graffiti.undo;

import java.util.Map;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

/**
 * Class <code>AddNodeEdit</code> makes the add edge action undoable.
 * 
 * @author Walter Wirch
 * @version $Revision: 1.1 $
 */
public class AddEdgeEdit extends GraphElementsEdit {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** added edge */
	private Edge edge;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for AddEdgeEdit.
	 * 
	 * @param edge
	 * @param graph
	 * @param geMap
	 */
	public AddEdgeEdit(Edge edge, Graph graph,
						Map<GraphElement, GraphElement> geMap) {
		super(graph, geMap);
		this.edge = edge;
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
		return sBundle.getString("undo.addEdge");
	}
	
	/*
	 * @see org.graffiti.undo.GraffitiAbstractUndoableEdit#execute()
	 */
	@Override
	public void execute() {
	}
	
	/**
	 * Adds the same edge that was added through the method that created this
	 * edit.
	 */
	@Override
	public void redo() {
		super.redo();
		
		Node source = (Node) getNewGraphElement(edge.getSource());
		Node target = (Node) getNewGraphElement(edge.getTarget());
		Edge newEdge = graph.addEdgeCopy(edge, source, target);
		assert newEdge.getGraph() != null;
		geMap.put(edge, newEdge);
	}
	
	/**
	 * Deletes the edge whose addition is stored in this edit.
	 */
	@Override
	public void undo() {
		super.undo();
		
		edge = (Edge) getNewGraphElement(edge);
		graph.deleteEdge(edge);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
