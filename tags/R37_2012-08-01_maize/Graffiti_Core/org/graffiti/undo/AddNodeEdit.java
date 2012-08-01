// ==============================================================================
//
// AddNodeEdit.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AddNodeEdit.java,v 1.1 2011-01-31 09:05:04 klukas Exp $

package org.graffiti.undo;

import java.util.Map;

import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

/**
 * Class <code>AddNodeEdit</code> makes the add node action undoable.
 * 
 * @author Walter Wirch
 * @version $Revision: 1.1 $
 */
public class AddNodeEdit
					extends GraphElementsEdit {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** added node */
	private Node node;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for AddNodeEdit.
	 * 
	 * @param node
	 * @param graph
	 * @param geMap
	 */
	public AddNodeEdit(Node node, Graph graph, Map<GraphElement, GraphElement> geMap) {
		super(graph, geMap);
		this.node = node;
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
		return sBundle.getString("undo.addNode");
	}
	
	/*
	 * @see org.graffiti.undo.GraffitiAbstractUndoableEdit#execute()
	 */
	@Override
	public void execute() {
	}
	
	/**
	 * Adds the same node that was added through the method that created this
	 * edit.
	 */
	@Override
	public void redo() {
		super.redo();
		
		Node newNode = graph.addNodeCopy(node);
		geMap.put(node, newNode);
	}
	
	/**
	 * Deletes the node that is stored in this edit.
	 */
	@Override
	public void undo() {
		super.undo();
		
		node = (Node) getNewGraphElement(node);
		graph.deleteNode(node);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
