// ==============================================================================
//
// NodeParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeParameter.java,v 1.1 2011-01-31 09:05:03 klukas Exp $

package org.graffiti.plugin.parameter;

import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

/**
 * This class contains a single <code>Node</code>.
 * 
 * @version $Revision: 1.1 $
 */
public class NodeParameter
					extends AbstractSingleParameter {
	// ~ Instance fields ========================================================
	
	/** The value of this parameter. */
	private Node value = null;
	
	private Graph graph = null;
	
	// ~ Constructors ===========================================================
	
	// /**
	// * Constructs a new node parameter.
	// *
	// * @param name the name of the parameter.
	// * @param description the description of the parameter.
	// */
	// public NodeParameter(String name, String description)
	// {
	// super(name, description);
	// }
	
	/**
	 * Constructs a new node parameter.
	 * 
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public NodeParameter(Graph graph, Node initalNode, String name, String description) {
		super(name, description);
		if (initalNode == null)
			initalNode = graph.getNodesIterator().next();
		value = initalNode;
		this.graph = graph;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>Node</code> contained in this <code>NodeParameter</code>.
	 * 
	 * @return the <code>Node</code> contained in this <code>NodeParameter</code>.
	 */
	public Node getNode() {
		return value;
	}
	
	public Node[] getPossibleNodes() {
		return graph.getNodes().toArray(new Node[] {});
	}
	
	/**
	 * Sets the value of the <code>AttributeParameter</code>.
	 * 
	 * @param val
	 *           the new value of the <code>AttributeParameter</code>.
	 */
	@Override
	public void setValue(Object val) {
		value = (Node) val;
	}
	
	/**
	 * Returns the value of this parameter.
	 * 
	 * @return the value of this parameter.
	 */
	@Override
	public Object getValue() {
		return value;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
