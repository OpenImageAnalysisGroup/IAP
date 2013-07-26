/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import org.graffiti.graph.GraphElement;

public class ValueAndGraphElement {
	
	private Comparable<?> value;
	private GraphElement ge;
	
	public ValueAndGraphElement(Comparable<?> value, GraphElement ge) {
		this.value = value;
		this.ge = ge;
	}
	
	public GraphElement getGraphElement() {
		return ge;
	}
	
	@SuppressWarnings("unchecked")
	public Comparable getValue() {
		return value;
	}
}
