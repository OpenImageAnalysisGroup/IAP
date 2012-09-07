/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GoProcessing {
	
	private Document doc;
	private HashMap<String, Node> goterm2xmlnode = new HashMap<String, Node>();
	
	private boolean isInitOK = false;
	
	public GoProcessing(File oboxml) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(oboxml);
			initNodeMap();
			isInitOK = true;
		} catch (ParserConfigurationException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (SAXException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (JaxenException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void initNodeMap() throws JaxenException {
		XPath xpath = new DOMXPath("/obo/term");
		List terms = xpath.selectNodes(doc.getFirstChild());
		for (Iterator it = terms.iterator(); it.hasNext();) {
			Node tn = (Node) it.next();
			String goTerm = getIDfromNode(tn);
			goterm2xmlnode.put(goTerm, tn);
			Collection<String> alt_ids = getAltIDsFromNode(tn);
			for (String altid : alt_ids)
				goterm2xmlnode.put(altid, tn);
		}
	}
	
	private Collection<String> getAltIDsFromNode(Node term) {
		ArrayList<String> result = new ArrayList<String>();
		NodeList nl = term.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node tn = nl.item(i);
			if (tn.getNodeName().equalsIgnoreCase("alt_id"))
				result.add(tn.getFirstChild().getNodeValue());
		}
		return result;
	}
	
	public boolean isValid() {
		return isInitOK;
	}
	
	private String getNameFromNode(Node term) {
		NodeList nl = term.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node tn = nl.item(i);
			if (tn.getNodeName().equalsIgnoreCase("name"))
				return tn.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	private String getIsObseleteFromNode(Node term) {
		NodeList nl = term.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node tn = nl.item(i);
			if (tn.getNodeName().equalsIgnoreCase("is_obsolete"))
				return tn.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	private String getIDfromNode(Node term) {
		NodeList nl = term.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node tn = nl.item(i);
			if (tn.getNodeName().equalsIgnoreCase("id"))
				return tn.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	private Collection<String> getDirectParents(Node term) {
		ArrayList<String> result = new ArrayList<String>();
		NodeList nl = term.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node tn = nl.item(i);
			if (tn.getNodeName().equalsIgnoreCase("is_a"))
				result.add(tn.getFirstChild().getNodeValue());
		}
		return result;
	}
	
	// private Collection<String> getDirectParentsPartOf(Node term) {
	// ArrayList<String> result = new ArrayList<String>();
	// NodeList nl = term.getChildNodes();
	// for (int i = 0; i<nl.getLength(); i++) {
	// Node tn = nl.item(i);
	// if (tn.getNodeName().equalsIgnoreCase("relationship")) {
	// result.add(tn.getFirstChild().getNodeValue());
	// NodeList nlR = term.getChildNodes();
	// for (int iPO = 0; iPO<nlR.getLength(); iPO++) {
	// Node tPOn = nlR.item(iPO);
	// if (tPOn.getNodeName().equalsIgnoreCase("to")) {
	// result.add(tPOn.getFirstChild().getNodeValue());
	// }
	// }
	// }
	// }
	// return result;
	// }
	
	public GOinformation getGOinformation(String goTerm) {
		goTerm = goTerm.trim();
		if (goTerm.equalsIgnoreCase("GO:0000000")) {
			return new GOinformation("ROOT NODE", "ROOT NAMESPACE",
								"ARTIFICIAL ROOT NODE", new ArrayList<String>(), new ArrayList<String>(), false);
		}
		Node t = goterm2xmlnode.get(goTerm);
		if (t == null) {
			ErrorMsg.addErrorMessage("Unknown GO-Term: " + goTerm);
			return null;
		}
		String name = getNameFromNode(t);
		String namespace = getNameSpaceFromNode(t);
		String defStr = getDefStrFromNode(t);
		Collection<String> parents = getDirectParents(t);
		Collection<String> partof = getDirectParents(t);
		if (parents.size() + partof.size() <= 0)
			parents.add("GO:0000000");
		
		String obs = getIsObseleteFromNode(t);
		boolean isObsolete = obs != null && obs.trim().equals("1");
		
		return new GOinformation(name, namespace, defStr, parents, partof, isObsolete);
	}
	
	public static String getCorrectGoTermFormat(String goTerm) {
		if (goTerm == null || !goTerm.startsWith("GO:"))
			return goTerm;
		while (goTerm.length() < "GO:0000000".length())
			goTerm = StringManipulationTools.stringReplace(goTerm, "GO:", "GO:0");
		return goTerm;
	}
	
	private String getDefStrFromNode(Node t) {
		NodeList nl = t.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node tn = nl.item(i);
			if (tn.getNodeName().equalsIgnoreCase("def"))
				return getDefStrFromDefNode(tn);
		}
		return null;
	}
	
	private String getDefStrFromDefNode(Node defNode) {
		NodeList nl = defNode.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node tn = nl.item(i);
			if (tn.getNodeName().equalsIgnoreCase("defstr"))
				return tn.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	private String getNameSpaceFromNode(Node t) {
		NodeList nl = t.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node tn = nl.item(i);
			if (tn.getNodeName().equalsIgnoreCase("namespace"))
				return tn.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	public Collection<String> getAllGoTerms() {
		return goterm2xmlnode.keySet();
	}
}
