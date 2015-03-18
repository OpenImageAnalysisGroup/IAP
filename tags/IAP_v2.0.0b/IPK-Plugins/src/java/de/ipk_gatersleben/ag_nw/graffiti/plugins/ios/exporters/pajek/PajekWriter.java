/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.pajek;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.AbstractOutputSerializer;

public class PajekWriter
					extends AbstractOutputSerializer {
	
	public void write(OutputStream out, Graph g) throws IOException {
		PrintStream stream = new PrintStream(out);
		printGraph(stream, g);
		stream.close();
	}
	
	private void printGraph(PrintStream stream, Graph g) {
		String WINDOWS_LINE_SEP = "\r\n"; // Pajek is a Windows Program ...
		stream.print("*Vertices " + g.getNumberOfNodes() + WINDOWS_LINE_SEP);
		
		HashMap<Node, Long> node2pajekId = new HashMap<Node, Long>();
		long pajekID = 1;
		for (Node n : g.getNodes()) {
			node2pajekId.put(n, new Long(pajekID++));
		}
		boolean doScale = true;
		for (Node n : g.getNodes()) {
			Vector2d p = AttributeHelper.getPositionVec2d(n);
			if (p.x > 100 || p.y > 100 || p.x < 0 || p.y < 0) {
				doScale = false;
				break;
			}
		}
		for (Node n : g.getNodes()) {
			Vector2d p = AttributeHelper.getPositionVec2d(n);
			if (doScale) {
				p.x /= 100;
				p.y /= 100;
			}
			String x = StringManipulationTools.formatNumber(p.x, "0.0000");
			String y = StringManipulationTools.formatNumber(p.y, "0.0000");
			double pz = AttributeHelper.getPositionZ(n, Double.NaN, false);
			String z;
			if (!Double.isNaN(pz)) {
				if (doScale)
					pz /= 100;
				z = " " + StringManipulationTools.formatNumber(pz, "0.0000");
			} else
				z = "";
			stream.print(node2pajekId.get(n) + " \"" + AttributeHelper.getLabel(n, "") + "\" " + x + " " + y + "" + z + WINDOWS_LINE_SEP);
		}
		
		boolean printArcs = true;
		boolean printEdges = true;
		
		for (Edge e : g.getEdges()) {
			if (e.isDirected()) {
				if (printArcs) {
					stream.println("*Arcs");
					printArcs = false;
				}
				stream.print(node2pajekId.get(e.getSource()) + " " + node2pajekId.get(e.getTarget()) + getEdgeWeight(e) + WINDOWS_LINE_SEP);
			}
		}
		
		for (Edge e : g.getEdges()) {
			if (!e.isDirected()) {
				if (printEdges) {
					stream.print("*Edges" + WINDOWS_LINE_SEP);
					printEdges = false;
				}
				stream.print(node2pajekId.get(e.getSource()) + " " + node2pajekId.get(e.getTarget()) + getEdgeWeight(e) + WINDOWS_LINE_SEP);
			}
		}
	}
	
	private String getEdgeWeight(Edge e) {
		Double weight = (Double) AttributeHelper.getAttributeValue(e, "pajek", "weight", null, 0d, false);
		if (weight != null && !Double.isNaN(weight))
			return " " + StringManipulationTools.formatNumber(weight, "0.0000000");
		else
			return "";
	}
	
	public String[] getExtensions() {
		return new String[] { ".net" };
	}
	
	public String[] getFileTypeDescriptions() {
		return new String[] { "Pajek .NET" };
	}
	
}
