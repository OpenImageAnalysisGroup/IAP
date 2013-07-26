// ==============================================================================
//
// EdgeParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeParameter.java,v 1.1 2011-01-31 09:05:02 klukas Exp $

package org.graffiti.plugin.parameter;

import org.graffiti.graph.Edge;

/**
 * This class contains a single <code>Edge</code>.
 * 
 * @version $Revision: 1.1 $
 */
public class EdgeParameter
					extends AbstractSingleParameter {
	// ~ Instance fields ========================================================
	
	/** The value of this parameter. */
	private Edge value = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new edge parameter.
	 * 
	 * @param name
	 *           the name of this parameter.
	 * @param description
	 *           the description of this parameter.
	 */
	public EdgeParameter(String name, String description) {
		super(name, description);
	}
	
	/**
	 * Constructs a new edge parameter.
	 * 
	 * @param edge
	 *           the edge saved in the parameter
	 * @param name
	 *           the name of this parameter.
	 * @param description
	 *           the description of this parameter.
	 */
	public EdgeParameter(Edge edge, String name, String description) {
		super(name, description);
		value = edge;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>Edge</code> contained in this <code>EdgeParameter</code>.
	 * 
	 * @return the <code>Edge</code> contained in this <code>EdgeParameter</code>.
	 */
	public Edge getEdge() {
		return value;
	}
	
	/**
	 * Sets the value of the <code>AttributeParameter</code>.
	 * 
	 * @param value
	 *           the new value of the <code>AttributeParameter</code>.
	 */
	@Override
	public void setValue(Object value) {
		// 
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
