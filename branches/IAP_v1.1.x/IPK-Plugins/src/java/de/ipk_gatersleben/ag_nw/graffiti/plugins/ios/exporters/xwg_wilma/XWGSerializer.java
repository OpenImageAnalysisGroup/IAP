/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.xwg_wilma;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.AbstractOutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

public class XWGSerializer
					extends AbstractOutputSerializer {
	
	public String[] getExtensions() {
		return new String[] { ".xwg" };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "XWG Wilmascope" };
	}
	
	public void write(OutputStream out, Graph g) {
		PrintStream stream;
		try {
			stream = new PrintStream(out, false, "iso-8859-1");
			stream.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
			stream.println("<!DOCTYPE WilmaGraph SYSTEM \"WilmaGraph.dtd\">");
			stream.println("<WilmaGraph>");
			stream.println("<Cluster>");
			HashMap<String, List<Node>> cluster2node = new HashMap<String, List<Node>>();
			g.numberGraphElements();
			for (Node n : g.getNodes()) {
				String cluster = NodeTools.getClusterID(n, "");
				if (cluster2node.containsKey(cluster)) {
					List<Node> nl = cluster2node.get(cluster);
					nl.add(n);
				} else {
					List<Node> nl = new ArrayList<Node>();
					nl.add(n);
					cluster2node.put(cluster, nl);
				}
			}
			
			for (String cluster : cluster2node.keySet()) {
				// stream.println(getTab(1)+"<Cluster>");
				if (cluster.length() > 0)
					stream.println(getTab(2) + "<Property Key=\"Name\" Value=\"" + cluster + "\"/>");
				for (Node n : cluster2node.get(cluster)) {
					stream.println(getTab(2) + "<Node ID=\"" + getNodeID(n) + "\">");
					stream.println(getTab(3) + "<ViewType Name=\"" + getViewType(n) + "\">");
					stream.println(getTab(4) + "<Property Key=\"Label\" Value=\"" + getLabel(n) + "\"/>");
					stream.println(getTab(4) + "<Property Key=\"Radius\" Value=\"" + getRadius(n) + "\"/>");
					stream.println(getTab(4) + "<Property Key=\"Colour\" Value=\"" + getColor(n) + "\"/>");
					stream.println(getTab(3) + "</ViewType>");
					stream.println(getTab(3) + "<Property Key=\"Position\" Value=\"" + getPosition(n) + "\"/>");
					stream.println(getTab(2) + "</Node>");
				}
				// stream.println(getTab(1)+"</Cluster>");
			}
			for (Edge e : g.getEdges()) {
				stream.println(getTab(2) + "<Edge EndID=\"" + getNodeID(e.getSource()) + "\" StartID=\"" + getNodeID(e.getTarget()) + "\">");
				stream.println(getTab(3) + "<ViewType Name=\"" + getViewType(e) + "\">");
				stream.println(getTab(4) + "<Property Key=\"Label\" Value=\"" + getLabel(e) + "\"/>");
				stream.println(getTab(4) + "<Property Key=\"Radius\" Value=\"" + getRadius(e) + "\"/>");
				stream.println(getTab(4) + "<Property Key=\"Colour\" Value=\"" + getColor(e) + "\"/>");
				stream.println(getTab(3) + "</ViewType>");
				stream.println(getTab(2) + "</Edge>");
			}
			stream.println("</Cluster>");
			stream.println("</WilmaGraph>");
			stream.close();
		} catch (UnsupportedEncodingException e1) {
			ErrorMsg.addErrorMessage(e1);
		}
	}
	
	private String getLabel(GraphElement ge) {
		String label = AttributeHelper.getLabel(ge, "");
		label = StringManipulationTools.stringReplace(label, "<html>", "");
		label = StringManipulationTools.stringReplace(label, "<br>", "");
		label = StringManipulationTools.stringReplace(label, "<", "");
		label = StringManipulationTools.stringReplace(label, ">", "");
		return label;
	}
	
	private String getRadius(Edge e) {
		return "0.2";
	}
	
	private String getRadius(Node n) {
		AttributeHelper.getSize(n);
		return "2.0"; // +(sz.x>10 ? sz.x/20d : 20/20d);
	}
	
	private String getViewType(Edge e) {
		return "Arrow";
	}
	
	private String getPosition(Node n) {
		Point2D p = AttributeHelper.getPosition(n);
		String x, y, z;
		x = StringManipulationTools.formatNumber(p.getX() / 10d, "#.#");
		y = StringManipulationTools.formatNumber(p.getY() / 10d, "#.#");
		x = StringManipulationTools.stringReplace(x, ",", ".");
		y = StringManipulationTools.stringReplace(y, ",", ".");
		double zz = AttributeHelper.getPositionZ(n, false);
		z = StringManipulationTools.formatNumber(zz / 10d, "#.#");
		z = StringManipulationTools.stringReplace(z, ",", ".");
		return x + " " + y + " " + z;
	}
	
	private String getColor(GraphElement ge) {
		Color c = AttributeHelper.getFillColor(ge);
		String r, g, b;
		r = StringManipulationTools.formatNumber(c.getRed() / 255d, "#.#");
		g = StringManipulationTools.formatNumber(c.getGreen() / 255d, "#.#");
		b = StringManipulationTools.formatNumber(c.getBlue() / 255d, "#.#");
		return r + " " + g + " " + b;
	}
	
	private String getViewType(Node n) {
		NodeTools.getNodeComponentType(n);
		NodeHelper nh = new NodeHelper(n, false);
		if (nh.getAttributeValue("kegg", "kegg_type", "", "").equals("compound"))
			return "Oriented Circle Node";
		else
			return "Oriented Box Node";
	}
	
	private String getNodeID(Node n) {
		return "N" + n.getID();
		// return n.getGraph().getName()+"_"+n.getID();
	}
	
	private String getTab(int d) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < d; i++)
			sb.append("\t");
		return sb.toString();
	}
}
