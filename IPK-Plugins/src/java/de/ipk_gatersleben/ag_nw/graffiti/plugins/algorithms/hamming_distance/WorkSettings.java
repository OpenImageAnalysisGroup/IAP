/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 15.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.hamming_distance;

public class WorkSettings {
	
	public boolean consNodes;
	public int nodesDistance;
	public boolean consEdges;
	public boolean consEdgeLabels;
	public int edgesDistance;
	public int validGraphIndex;
	
	public WorkSettings(boolean consNodes, int nodesDistance, boolean consEdges, boolean consEdgeLabels, int edgesDistance, int validGraphIndex) {
		this.consNodes = consNodes;
		this.nodesDistance = nodesDistance;
		this.consEdges = consEdges;
		this.consEdgeLabels = consEdgeLabels;
		this.edgesDistance = edgesDistance;
		this.validGraphIndex = validGraphIndex;
	}
	
}
