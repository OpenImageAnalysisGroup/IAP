/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.03.2007 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.AttributeHelper;
import org.Vector2df;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;

public class ClusterBackgroundDraw {
	
	public HashMap<String, Color> cluster2color;
	public HashMap<Node, Vector2df> node2position;
	public HashMap<Node, String> node2cluster;
	
	public float minX = Float.MAX_VALUE;
	public float minY = Float.MAX_VALUE;
	public float maxX = Float.MIN_VALUE;
	public float maxY = Float.MIN_VALUE;
	
	public void init(Graph graph) {
		cluster2color = new HashMap<String, Color>();
		
		Set<String> clusters = new TreeSet<String>();
		for (GraphElement ge : graph.getGraphElements()) {
			if (AttributeHelper.isHiddenGraphElement(ge))
				continue;
			String clusterId = NodeTools.getClusterID(ge, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
		}
		
		ClusterColorAttribute cca = (ClusterColorAttribute) AttributeHelper.getAttributeValue(
							graph,
							ClusterColorAttribute.attributeFolder,
							ClusterColorAttribute.attributeName,
							ClusterColorAttribute.getDefaultValue(clusters.size()),
							new ClusterColorAttribute("resulttype"), true);
		
		cca.ensureMinimumColorSelection(clusters.size());
		ArrayList<String> clusterArray = new ArrayList<String>(clusters);
		for (int idx = 0; idx < clusterArray.size(); idx++) {
			String cluster = clusterArray.get(idx);
			cluster2color.put(cluster, cca.getClusterColor(idx));
		}
		node2position = new HashMap<Node, Vector2df>();
		node2cluster = new HashMap<Node, String>();
		for (Node n : graph.getNodes()) {
			if (AttributeHelper.isHiddenGraphElement(n))
				continue;
			node2cluster.put(n, NodeTools.getClusterID(n, ""));
			node2position.put(n, AttributeHelper.getPositionVec2df(n));
		}
		
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		
		for (Vector2df p : node2position.values()) {
			if (p.x < minX)
				minX = p.x;
			if (p.y < minY)
				minY = p.y;
			if (p.x > maxX)
				maxX = p.x;
			if (p.y > maxY)
				maxY = p.y;
		}
		
		int outer = 100;
		minX += -outer;
		minY += -outer;
		maxX += outer;
		maxY += outer;
	}
	
}
