/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

import java.util.HashMap;

import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

public class TranspathPathway extends TranspathEntity {
	
	public String ID, NAME, ALIASES, TYPE, SUPERFAMILY, SUBFAMILY, REACTION, MOLECULE, COMMENT, REFERENCE;
	
	public String getKey() {
		return ID;
	}
	
	@Override
	public String toString() {
		return SUPERFAMILY + "/" + SUBFAMILY + ": " + ID + " - " + ALIASES;
	}
	
	@Override
	public void processXMLentityValue(String environment, String value) {
		super.processXMLentityValue(environment, value);
	}
	
	public void addElementsToGraph(Graph g, String clusterID, HashMap<String, Node> graphElementId2graphNode) {
		System.out.println("Create graph from " + toString());
		System.out.println("Reactions: " + REACTION);
		System.out.println("Molecules: " + MOLECULE);
		if (MOLECULE.length() > 0) {
			String[] molecules = MOLECULE.split(" ");
			Node lastMoleculeNode = null;
			for (String m : molecules) {
				if (m.trim().length() <= 0)
					continue;
				TranspathMolecule tpm = TranspathService.getMolecule(m);
				if (tpm == null)
					ErrorMsg.addErrorMessage("Unknown Molecule ID: " + m);
				else {
					Node moleculeNode = tpm.getGraphNode(g, clusterID, graphElementId2graphNode);
					if (lastMoleculeNode != null) {
						TranspathMolecule.connectMoleculeNodes(g, lastMoleculeNode, moleculeNode);
					}
					lastMoleculeNode = moleculeNode;
				}
			}
		}
		if (REACTION.length() > 0) {
			String[] reactions = REACTION.split(" ");
			for (String r : reactions) {
				if (r.trim().length() <= 0)
					continue;
				TranspathReaction tpr = TranspathService.getReaction(r);
				if (tpr == null) {
					System.out.println("Unknown Reaction ID (might be pathway chain): " + r);
					TranspathPathway tpChain = TranspathService.getPathway(r);
					if (tpChain == null) {
						System.out.println("Unknown Reaction ID (is no pathway chain): " + r);
					} else {
						System.out.println("Pathway Chain: " + tpChain.toString());
						tpChain.addElementsToGraph(g, clusterID + "\\" + tpChain.ID, graphElementId2graphNode);
					}
				} else {
					System.out.println("Reaction " + r + ": " + tpr.toString());
					tpr.addElementsToGraph(g, clusterID, graphElementId2graphNode);
				}
			}
		}
	}
	
	public String getXMLstartEndEntity() {
		return "Pathway";
	}
}
