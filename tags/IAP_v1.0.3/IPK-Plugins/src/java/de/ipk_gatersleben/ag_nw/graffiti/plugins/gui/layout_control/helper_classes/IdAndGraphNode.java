/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.10.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.QuadNumber;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class IdAndGraphNode {
	public String id;
	public org.graffiti.graph.Node graphNode;
	public QuadNumber quadNumber;
	
	public IdAndGraphNode(String id2, org.graffiti.graph.Node graphNode) {
		if (id2 != null)
			id2 = id2.trim();
		this.id = id2;
		this.id = StringManipulationTools.stringReplace(this.id, "<html>", "");
		this.id = StringManipulationTools.stringReplace(this.id, "<br>", "");
		this.id = StringManipulationTools.stringReplace(this.id, "<br/>", "");
		this.graphNode = graphNode;
		this.quadNumber = new QuadNumber(id);
		if (!quadNumber.isValidQuadNumber())
			quadNumber = null;
	}
	
}
