/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import org.graffiti.graph.Node;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class NodePair {
	public Node a, b;
	
	public NodePair(Node a, Node b) {
		this.a = a;
		this.b = b;
	}
}
