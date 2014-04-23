/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.07.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.awt.Color;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.graffiti.graph.GraphElement;

public class AttributePathNameSearchType {
	private String attributePath;
	private String attributeName;
	private SearchType searchType;
	private String niceID;
	private boolean inNode = false;
	private boolean inEdge = false;
	
	public AttributePathNameSearchType(String attributePath, String attributeName, SearchType searchType, String niceID) {
		this.attributePath = attributePath;
		this.attributeName = attributeName;
		this.searchType = searchType;
		this.niceID = niceID;
	}
	
	public String getAttributePath() {
		return attributePath;
	}
	
	public String getAttributeName() {
		return attributeName;
	}
	
	public SearchType getSearchType() {
		return searchType;
	}
	
	@Override
	public String toString() {
		String strippedNiceID = niceID;
		int ps = strippedNiceID.indexOf("<");
		while (ps >= 0) {
			int es = strippedNiceID.indexOf(">", ps) + 1;
			if (es > 0) {
				strippedNiceID = strippedNiceID.substring(0, ps) + strippedNiceID.substring(es);
				ps = strippedNiceID.indexOf("<");
			} else
				es = -1;
		}
		strippedNiceID = StringManipulationTools.stringReplace(strippedNiceID, "&nbsp;", " ");
		
		strippedNiceID = StringManipulationTools.stringReplace(strippedNiceID, "(selected elements)", " ");
		strippedNiceID = StringManipulationTools.stringReplace(strippedNiceID, "(not auto-updated)", " ");
		String res = "<html>" + strippedNiceID + getNiceSearchType(); // + " ("+attributePath+")";
		return res;
	}
	
	private String getNiceSearchType() {
		String st = "";
		if (searchType == SearchType.searchBoolean)
			st = "boolean";
		if (searchType == SearchType.searchDouble)
			st = "number";
		if (searchType == SearchType.searchInteger)
			st = "integer";
		if (searchType == SearchType.searchString)
			st = "text";
		
		return "<small><font color=\"gray\"> - " + st + "</font></small>";
	}
	
	public String getNiceID() {
		return niceID;
	}
	
	public boolean isInEdge() {
		return inEdge;
	}
	
	public boolean isInNode() {
		return inNode;
	}
	
	public void setInEdge(boolean inEdge) {
		this.inEdge = inEdge;
	}
	
	public void setInNode(boolean inNode) {
		this.inNode = inNode;
	}
	
	// public double getMinimumEdgeAttributeValueConnectingGivenNodes(boolean directed, Node srcNode, Node tgtNode, double returnIfNotAvail) {
	// Collection<Edge> checkTheseEdges;
	// if (directed)
	// checkTheseEdges = srcNode.getAllOutEdges();
	// else
	// checkTheseEdges = srcNode.getEdges();
	// double minVal = Double.MAX_VALUE;
	// for (Edge e : checkTheseEdges) {
	// if (directed && e.getTarget()!=tgtNode)
	// continue;
	// if (!directed && ! ( (e.getSource()==srcNode && e.getTarget()==tgtNode) || (e.getSource()==tgtNode && e.getTarget()==srcNode) ))
	// continue;
	// double val = (Double)AttributeHelper.getAttributeValue(e, attributePath, attributeName, Double.MAX_VALUE, Double.MAX_VALUE);
	// if (val<minVal)
	// minVal = val;
	// }
	// if (!(minVal<Double.MAX_VALUE))
	// return returnIfNotAvail;
	// else
	// return minVal;
	// }
	
	public double getAttributeValue(GraphElement ge, double returnIfNotAvail) {
		return (Double) AttributeHelper.getAttributeValue(ge, attributePath, attributeName, returnIfNotAvail, returnIfNotAvail, false);
	}
	
	public void setAttributeValue(GraphElement ge, double value) {
		if (searchType == SearchType.searchDouble)
			AttributeHelper.setAttribute(ge, attributePath, attributeName, value);
		if (searchType == SearchType.searchInteger)
			AttributeHelper.setAttribute(ge, attributePath, attributeName, (int) value);
	}
	
	public void setAttributeValue(GraphElement ge, Color color) {
		AttributeHelper.setAttribute(ge, attributePath, attributeName, color);
	}
	
	// public boolean getIsEdgeWithMinimumAttributeValueConnectingSrcTgtNodes(boolean directed, Edge edge, double returnIfNotAvail) {
	// Node srcNode = edge.getSource();
	// Node tgtNode = edge.getTarget();
	//
	// Collection<Edge> checkTheseEdges;
	// if (directed)
	// checkTheseEdges = srcNode.getAllOutEdges();
	// else
	// checkTheseEdges = srcNode.getEdges();
	// double minVal = getAttributeValue(edge, returnIfNotAvail);
	// boolean givenEdgeHasMinimumValue = true;
	//
	// for (Edge e : checkTheseEdges) {
	// if (directed && e.getTarget()!=tgtNode)
	// continue;
	// if (!directed && ! ( (e.getSource()==srcNode && e.getTarget()==tgtNode) || (e.getSource()==tgtNode && e.getTarget()==srcNode) ))
	// continue;
	// double val = (Double)AttributeHelper.getAttributeValue(e, attributePath, attributeName, Double.MAX_VALUE, Double.MAX_VALUE);
	// if (val<minVal) {
	// minVal = val;
	// if (e!=edge) {
	// givenEdgeHasMinimumValue = false;
	// break;
	// }
	// }
	// }
	// return givenEdgeHasMinimumValue;
	// }
	
}
