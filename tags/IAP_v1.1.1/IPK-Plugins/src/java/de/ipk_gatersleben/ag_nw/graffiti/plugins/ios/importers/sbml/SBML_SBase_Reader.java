/**
 * Reads in Attributes from the class SBase. All other components of the
 * model inherit from this class
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.xml.XMLNamespaces;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_SBase_Reader {
	
	/**
	 * To make the code shorter
	 */
	public static final String ATT = AttributeHelper.attributeSeparator;
	
	/**
	 * Stores the namespaces which already has been added to the graph tab
	 */
	public static HashMap<String, String> namespaceCollector;
	
	/**
	 * This method processes an string with xhtml content
	 * 
	 * @param xhtml
	 * @return
	 */
	public static String removeTagFromString(String xhtml) {
		String content = xhtml.replace("\n", "").replace("\t", "").trim();
		// Replace anything between script or style tags
		// A regular expression to match anything in between <>
		// Reads as: Match a "<"
		// Match one or more characters that are not ">"
		// Match "<";
		String tagregex = "<[^>]*>";
		Pattern p2 = Pattern.compile(tagregex);
		Matcher m2 = p2.matcher(content);
		// Replace any matches with nothing
		content = m2.replaceAll("");
		return content.trim();
	}
	
	/**
	 * Method transforms an presented headline into an intern headline
	 * 
	 * @param presentedHeadline
	 *        is visible for the user
	 * @return
	 */
	public String getNiceHeadline(String presentedHeadline) {
		return presentedHeadline.replaceAll(" ", "_").toLowerCase();
	}
	
	public void addNotes(XMLNode notesObject, String notesString, Edge e,
			String path, String notesName) {
		// addNamespaces(notesObject, g, "sbml", "sbml_namespaces");
		if ((null != notesString)
				&& !(SBML_Constants.EMPTY.equals(notesString))) {
			// String notes = removeTagFromString(notesString);
			// AttributeHelper.setAttribute(g, path, path+"_"+notesName, notes);
			// AttributeHelper.setAttribute(e, path, notesName, notes);
			AttributeHelper.setAttribute(e, path, notesName, notesObject);
		}
	}
	
	/**
	 * This method adds notes to the current object in the graph tab
	 * 
	 * @param notesObject
	 *        contains the notes in XML Node form
	 * @param notesString
	 *        contains the notes as a String
	 * @param g
	 *        is the data structure for reading in the information
	 * @param path
	 *        intern representation where to add the notes
	 * @param notesName
	 *        contains the presented headline and the presented attribute
	 *        name
	 */
	public void addNotes(XMLNode notesObject, String notesString, Graph g,
			String path, String notesName) {
		addNamespaces(notesObject, g, "sbml", "sbml_namespaces");
		// if ((null != notesString)
		// && !(SBML_Constants.EMPTY.equals(notesString))) {
		// String notes = removeTagFromString(notesString);
		// AttributeHelper.setAttribute(g, path, path+"_"+notesName, notes);
		AttributeHelper.setAttribute(g, path, notesName, notesObject);
		// }
	}
	
	/**
	 * This method adds notes to the current object in the node tab
	 * 
	 * @param notesObject
	 *        contains the notes in XML Node form
	 * @param notesString
	 *        contains the notes as a String
	 * @param node
	 *        is the data structure for reading in the information
	 * @param path
	 *        intern representation where to add the notes
	 * @param notesName
	 *        contains the presented headline and the presented attribute
	 *        name
	 */
	public void addNotes(XMLNode notesObject, String notesString, Node node,
			String path, String notesName) {
		addNamespaces(notesObject, node, path, notesName + " Namespace");
		if ((null != notesString)
				&& !(SBML_Constants.EMPTY.equals(notesString))) {
			// String notes = removeTagFromString(notesString);
			// AttributeHelper.setAttribute(node, path, notesName, notes);
			AttributeHelper.setAttribute(node, path, notesName, notesObject);
		}
	}
	
	/**
	 * Method helps to set Attributes
	 * Creates nodes with 20, 20 height/width
	 * @param node
	 *        the current node
	 * @param color
	 *        the aimed color
	 * @param label
	 *        label of the node
	 * @param point
	 *        coordinates of the node
	 * @param size_multiplier
	 *        determines the size of the node
	 */
	public void setAttributes(Node node, Color color, String label,
			Point2D point, int size_multiplier) {
		AttributeHelper.setDefaultGraphicsAttribute(node, point);
		AttributeHelper.setLabel(node, label);
		AttributeHelper.setSize(node, 20d/*label.length() * size_multiplier*/, 20d);
		AttributeHelper.setFillColor(node, color);
	}
	
	/**
	 * Adds the namespaces of an XMLNode to the graph tab
	 * 
	 * @param node
	 *        contains the namespaces
	 * @param g
	 *        the graph that should be imported
	 */
	public static void addNamespaces(XMLNode node, Graph g, String path,
			String attribute) {
		if (node != null) {
			if (!(node.isNamespacesEmpty())) {
				XMLNamespaces ns = node.getNamespaces();
				String namespacesList = "";
				String namespaceAttribute = (String) AttributeHelper
						.getAttributeValue(g, path, attribute, null, null);
				if (namespaceAttribute != null) {
					namespacesList = namespaceAttribute;
				}
				if (null != ns) {
					for (int i = 0; i < ns.getLength(); i++) {
						if (!(namespaceCollector.containsKey(ns.getPrefix(i)))) {
							namespacesList += ns.getPrefix(i) + "=\""
									+ ns.getURI(i) + "\"";
							namespaceCollector.put(ns.getPrefix(i),
									"=\"" + ns.getURI(i) + "\"");
						}
					}
				}
				AttributeHelper
						.setAttribute(g, path, attribute, namespacesList);
			}
		}
	}
	
	/**
	 * Adds the namespaces of an XMLNode to the graph tab
	 * 
	 * @param node
	 *        contains the namespaces
	 * @param g
	 *        the graph that should be imported
	 */
	private void addNamespaces(XMLNode node, Node n, String path,
			String attribute) {
		if (node != null) {
			if (!(node.isNamespacesEmpty())) {
				XMLNamespaces ns = node.getNamespaces();
				String namespacesList = "";
				String namespaceAttribute = (String) AttributeHelper
						.getAttributeValue(n, path, attribute, null, null);
				if (namespaceAttribute != null) {
					namespacesList = namespaceAttribute;
				}
				if (null != ns) {
					for (int i = 0; i < ns.getLength(); i++) {
						namespacesList += ns.getPrefix(i) + "=\""
								+ ns.getURI(i) + "\"";
					}
				}
				AttributeHelper
						.setAttribute(n, path, attribute, namespacesList);
			}
		}
	}
	
	/**
	 * Adds a map of namespaces (namespace, URL) to the graph tab.
	 * 
	 * @param namespaces
	 *        contains the namespaces and the URLs
	 * @param g
	 *        the graph that should be imported
	 */
	public void addNamespaces(Map<String, String> namespaces, Graph g,
			String path, String attribute) {
		if (namespaces != null) {
			String namespacesList = "";
			String namespaceAttribute = (String) AttributeHelper
					.getAttributeValue(g, path, attribute, null, null);
			if (namespaceAttribute != null) {
				namespacesList = namespaceAttribute;
			}
			Set<Entry<String, String>> entries = namespaces.entrySet();
			for (Iterator<Entry<String, String>> iterator = entries.iterator(); iterator
					.hasNext();) {
				Entry<String, String> entry = (Entry<String, String>) iterator
						.next();
				if (!(namespaceCollector.containsKey(entry.getKey()))
						&& entry.getKey().contains(":")) {
					namespacesList += (entry.getKey() + "=\""
							+ entry.getValue() + "\" ");
					namespaceCollector.put(entry.getKey(),
							"=\"" + entry.getValue() + "\" ");
				}
			}
			if (!namespacesList.isEmpty()) {
				AttributeHelper
						.setAttribute(g, path, attribute, namespacesList);
			}
		}
		/*
		 * else { AttributeHelper.setAttribute(g, path, attribute, ""); }
		 */
	}
}