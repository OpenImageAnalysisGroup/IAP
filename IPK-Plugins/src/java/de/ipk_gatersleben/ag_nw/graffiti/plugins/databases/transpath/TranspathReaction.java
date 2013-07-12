/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

import java.util.HashMap;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

public class TranspathReaction extends TranspathEntity {
	
	public String ID, DESCRIPTION, TYPE, REVERSIBLE, QUALITY, EFFECT, EVIDENCE, PATHWAYSTEP, SEMANTIC,
						COMPOSITION, DECOMPOSITION, REACTANT, PRODUCES, ENZYME, INHIBITOR, COMMENT, REFERENCE, PATHWAY;
	
	// public String ID, NEGATIVE, CYTOMERORGAN, CYTOMERCELL, CYTOMERSTAGE, CYTOMERSPECIES, LOCATIONACCNOS, METHOD, MATERIAL, CELLLINE, SPECIES;
	
	public String getKey() {
		return ID;
	}
	
	@Override
	public String toString() {
		return ID + ", desc: " + DESCRIPTION + ", type: " + TYPE + ", reversible: " + REVERSIBLE;
	}
	
	public void addElementsToGraph(Graph g, String clusterID, HashMap<String, Node> graphElementId2graphNode) {
		getGraphNode(g, clusterID, graphElementId2graphNode);
		
	}
	
	private Node getGraphNode(Graph g, String clusterID, HashMap<String, Node> graphElementId2graphNode) {
		if (graphElementId2graphNode.containsKey(ID))
			return graphElementId2graphNode.get(ID);
		Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(Math.random() * 500, Math.random() * 500));
		graphElementId2graphNode.put(ID, n);
		NodeHelper nh = new NodeHelper(n);
		nh.setLabel(TYPE);
		nh.setClusterID(clusterID);
		nh.setSize(10, 10);
		nh.setTooltip("Description: " + DESCRIPTION + ", Reversible: " + REVERSIBLE + ", Quality: " + QUALITY + ", Comment: " + COMMENT);
		nh.setAttributeValue("TRANSPATH", "tpID", ID);
		nh.setAttributeValue("TRANSPATH", "tpDESCRIPTION", DESCRIPTION);
		nh.setAttributeValue("TRANSPATH", "tpTYPE", TYPE);
		nh.setAttributeValue("TRANSPATH", "tpREVERSIBLE", REVERSIBLE);
		nh.setAttributeValue("TRANSPATH", "tpQUALITY", QUALITY);
		nh.setAttributeValue("TRANSPATH", "tpEFFECT", EFFECT);
		nh.setAttributeValue("TRANSPATH", "tpEVIDENCE", EVIDENCE);
		nh.setAttributeValue("TRANSPATH", "tpSEMANTIC", SEMANTIC);
		nh.setAttributeValue("TRANSPATH", "tpCOMPOSITION", COMPOSITION);
		nh.setAttributeValue("TRANSPATH", "tpDECOMPOSITION", DECOMPOSITION);
		nh.setAttributeValue("TRANSPATH", "tpREACTANT", REACTANT);
		nh.setAttributeValue("TRANSPATH", "tpPRODUCES", PRODUCES);
		nh.setAttributeValue("TRANSPATH", "tpENZYME", ENZYME);
		nh.setAttributeValue("TRANSPATH", "tpINHIBITOR", INHIBITOR);
		nh.setAttributeValue("TRANSPATH", "tpCOMMENT", COMMENT);
		nh.setAttributeValue("TRANSPATH", "tpREFERENCE", REFERENCE);
		/*
		 * public String ID, NAME, ALIASES, TYPE, FIRST, LAST, CLASS,
		 * SUPERFAMILY, SUBFAMILY, RKOUT, RKIN, CATALYZES, INHIBITS, COMMENT,
		 * REFERENCE, GENBANKID, ENSEMBLID, ENTREZGENEID, INTERPRODESCRIPTION,
		 * INTERPROID, OMIMID, PDBID, PFAMID, PIRID, PROSITEID,
		 * REFSEQID, REFSEQPID, UNIGENEID, UNIPROTACCESSION, GOTYPE, GODESCRIPTION,
		 * GOACCESSION, OTHERACCESION, OTHERGENOMICACCESION;
		 */
		return n;
	}
	
	public String getXMLstartEndEntity() {
		return "Reaction";
	}
	
	/*
	 * TRANSPATHReaction:XN000000000 XN000000000 EGF + 2ErbB1 <=> EGF:(ErbB1)2 pathway step True 2
	 * binding XN000026374 XN000000071 MO000000071 MO000004374 MO000016681 MO000038630
	 * PA000015142 PA000015147 PA000015158 PA000014438 PA000014219 PA000014221 TFPA23825 CH000000622 CH000000724
	 */
}
