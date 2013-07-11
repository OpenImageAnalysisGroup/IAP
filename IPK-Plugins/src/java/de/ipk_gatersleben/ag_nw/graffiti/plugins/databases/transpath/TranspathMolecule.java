/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

import java.awt.Color;
import java.util.HashMap;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

public class TranspathMolecule extends TranspathEntity {
	
	public String ID, NAME, ALIASES, TYPE, FIRST, LAST, CLASS, SUPERFAMILY, SUBFAMILY, RKOUT, RKIN, CATALYZES, INHIBITS, COMMENT,
						REFERENCE, GENBANKID, ENSEMBLID, ENTREZGENEID, INTERPRODESCRIPTION, INTERPROID, OMIMID, PDBID, PFAMID, PIRID, PROSITEID,
						REFSEQID, REFSEQPID, UNIGENEID, UNIPROTACCESSION, GOTYPE, GODESCRIPTION, GOACCESSION, OTHERACCESION, OTHERGENOMICACCESION;
	
	// public String ID, CELLLINE, COMPARTMENT, SPECIES, METHOD, CYTOMERORGAN, CYTOMERSTAGE, EXPRESSIONLEVEL;
	
	public String getKey() {
		return ID;
	}
	
	public Node getGraphNode(Graph g, String clusterID, HashMap<String, Node> graphElementId2graphNode) {
		if (graphElementId2graphNode.containsKey(ID))
			return graphElementId2graphNode.get(ID);
		Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(Math.random() * 500, Math.random() * 500));
		graphElementId2graphNode.put(ID, n);
		NodeHelper nh = new NodeHelper(n);
		nh.setLabel(NAME);
		nh.setClusterID(clusterID);
		nh.setSize(20, 20);
		nh.setTooltip("Type: " + TYPE + ", Super/Sub-Family: " + SUPERFAMILY + "/" + SUBFAMILY + ", Comment: " + COMMENT);
		nh.setAttributeValue("TRANSPATH", "tpID", ID);
		nh.setAttributeValue("TRANSPATH", "tpNAME", NAME);
		nh.setAttributeValue("TRANSPATH", "tpALIASES", ALIASES);
		nh.setAttributeValue("TRANSPATH", "tpTYPE", TYPE);
		nh.setAttributeValue("TRANSPATH", "tpFIRST", FIRST);
		nh.setAttributeValue("TRANSPATH", "tpLAST", LAST);
		nh.setAttributeValue("TRANSPATH", "tpCLASS", CLASS);
		nh.setAttributeValue("TRANSPATH", "tpSUPERFAMILY", SUPERFAMILY);
		nh.setAttributeValue("TRANSPATH", "tpSUBFAMILY", SUBFAMILY);
		nh.setAttributeValue("TRANSPATH", "tpRKOUT", RKOUT);
		nh.setAttributeValue("TRANSPATH", "tpRKIN", RKIN);
		nh.setAttributeValue("TRANSPATH", "tpCATALYZES", CATALYZES);
		nh.setAttributeValue("TRANSPATH", "tpINHIBITS", INHIBITS);
		nh.setAttributeValue("TRANSPATH", "tpCOMMENT", COMMENT);
		nh.setAttributeValue("TRANSPATH", "tpREFERENCE", REFERENCE);
		nh.setAttributeValue("TRANSPATH", "tpGOTYPE", GOTYPE);
		nh.setAttributeValue("TRANSPATH", "tpGODESCRIPTION", GODESCRIPTION);
		nh.setAttributeValue("TRANSPATH", "tpGOACCESSION", GOACCESSION);
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
	
	public static void connectMoleculeNodes(Graph g, Node moleculeNode1, Node moleculeNode2) {
		g.addEdge(moleculeNode1, moleculeNode2, true,
							AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
	}
	
	public String getXMLstartEndEntity() {
		return "Molecule";
	}
	
	/*
	 * TRANSPATHMolecule:MO000000001 MO000000001 PKAc cAMP-dependent protein kinase cAPK PKA catalytic subunit protein kinase
	 * A protein kinase A catalytic subunit orthofamily enzymes; transferases EC 2; transferases EC 2.7;
	 * phosphotransferases EC 2.7.1; protein serine/threonine kinases; AGC kinases MO000019596 MO000020657 MO000021790
	 * MO000021807 MO000033328 MO000033604 MO000034581 MO000035255 MO000035257 XN000000509 XN000000728 XN000000968
	 * XN000001405 XN000001552 XN000001715 XN000003642 XN000004354 XN000005173 XN000005174 XN000005176 XN000005182 XN000005616
	 * XN000006622 XN000015246 XN000015281 XN000015486 XN000015628 XN000016709 XN000016728 XN000017434 XN000017692 XN000017695
	 * XN000017964 XN000021885 XN000022739 XN000026992 XN000026996 XN000026999 XN000027342 XN000033827 XN000000516 XN000000539
	 * XN000001626 XN000015487 XN000000309 XN000000386 XN000000390 XN000023917 XN000024089 XN000027341 XN000027958 XN000033826
	 * AN000033346 AN000036244 PA000016059
	 */

}
