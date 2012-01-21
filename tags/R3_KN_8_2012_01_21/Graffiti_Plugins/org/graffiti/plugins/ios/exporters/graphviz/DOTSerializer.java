// ==============================================================================
//
// DOTSerializer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DOTSerializer.java,v 1.1 2011-01-31 09:03:32 klukas Exp $

package org.graffiti.plugins.ios.exporters.graphviz;

import java.awt.Dimension;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.plugin.io.AbstractOutputSerializer;

/**
 * A very very simple writer for a graph in the dot format for <a
 * href="http://www.research.att.com/sw/tools/graphviz/">Graphviz</a>. The
 * only exported attribute are labels.
 * 
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:03:32 $
 */
public class DOTSerializer
					extends AbstractOutputSerializer {
	// ~ Static fields/initializers =============================================
	
	/** String representing one indentation level. */
	private static final String TAB = "    ";
	
	private static boolean topBottom = false;
	
	// ~ Instance fields ========================================================
	
	/** Internal map saving the ids assigned to the nodes. */
	private Map<Node, String> nodes;
	
	/** Internal id the next node will be given. */
	private int nodeCount;
	
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.plugin.io.Serializer#getExtensions()
	 */
	public String[] getExtensions() {
		return new String[] { ".dot" };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "DOT" };
	}
	
	/*
	 * @see org.graffiti.plugin.io.OutputSerializer#write(java.io.OutputStream,
	 * org.graffiti.graph.Graph)
	 */
	public synchronized void write(OutputStream out, Graph g) {
		PrintStream stream = new PrintStream(out);
		
		nodes = new HashMap<Node, String>();
		nodeCount = 1;
		
		HashSet<Node> subgraphNodes = new HashSet<Node>();
		
		// open graph
		if (g.isDirected())
			stream.print("di");
		
		stream.println("graph myGraph {");
		
		Dimension d = getMaximumXY(g.getNodes(), 1d, 0, 0, true);
		
		printGraphFormat(stream, g, d);
		
		HashMap<String, ArrayList<Node>> clusterId2Nodes = new HashMap<String, ArrayList<Node>>();
		int i = 0;
		for (Node n : g.getNodes()) {
			String cluster = ((String) AttributeHelper.getAttributeValue(n, "cluster",
								"cluster", null, null));
			if (cluster != null && cluster.length() > 0) {
				if (!clusterId2Nodes.containsKey(cluster))
					clusterId2Nodes.put(cluster, new ArrayList<Node>());
				clusterId2Nodes.get(cluster).add(n);
				subgraphNodes.add(n);
			}
			i++;
		}
		for (String cluster : clusterId2Nodes.keySet()) {
			writeSubGraph(stream, cluster, clusterId2Nodes.get(cluster), d);
		}
		
		// print nodes
		for (Node n : g.getNodes()) {
			if (!subgraphNodes.contains(n)) {
				String nodeRef = nodeCount + "";
				writeNode("", nodeRef, stream, n, d);
				nodeCount++;
			}
		}
		
		// print edges
		for (Edge e : g.getEdges())
			writeEdge(stream, e);
		
		// close graph
		stream.println("}");
		
		// finished
		stream.close();
	}
	
	private void writeSubGraph(PrintStream stream, String cluster, ArrayList<Node> clusterNodes, Dimension d) {
		stream.println(TAB + "subgraph " + cluster + " {");
		stream.println(TAB + TAB + "label \"" + cluster.trim() + "\";");
		for (Node node : clusterNodes) {
			String nodeRef = nodeCount + "";
			nodes.put(node, nodeRef);
			writeNode(TAB, nodeRef, stream, node, d);
			nodeCount++;
		}
		stream.println(TAB + "}");
	}
	
	private void printGraphFormat(PrintStream out, Graph g, Dimension d) {
		out.println("node [label=\"\\N\", shape=box, fontsize=" + getFontSize(g) + "];");
		int w = d.width;
		int h = d.height;
		if (topBottom)
			out.println("graph [overlap=\"false\",splines=\"polyline\",rankdir=\"TB\",bb=\"0,0," + w + "," + h + "\"]");
		else
			out.println("graph [overlap=\"false\",splines=\"polyline\",rankdir=\"LR\",bb=\"0,0," + w + "," + h + "\"]");
		topBottom = false;
	}
	
	private int getFontSize(Graph g) {
		if (g == null || g.getNumberOfNodes() <= 0)
			return 10;
		int result = -1;
		for (Node n : g.getNodes()) {
			LabelAttribute la = AttributeHelper.getLabel(-1, n);
			if (la != null) {
				if (la.getFontSize() > result)
					result = la.getFontSize();
			}
		}
		if (result == -1)
			return 10;
		return result;
	}
	
	/**
	 * Write one edge in dot format.
	 * 
	 * @param stream
	 *           The stream to which the edge is written
	 * @param edge
	 *           The edge that is written
	 */
	private void writeEdge(PrintStream stream, Edge edge) {
		stream.print(TAB + nodes.get(edge.getSource()));
		
		if (edge.isDirected())
			stream.print(" -> ");
		else
			stream.print(" -- ");
		
		stream.print(nodes.get(edge.getTarget()));
		stream.println(";");
	}
	
	/**
	 * Write one node in dot format.
	 * 
	 * @param stream
	 *           The stream to which the node is written
	 * @param node
	 *           The node that is written
	 * @param d
	 * @param writtenNodeIds
	 */
	private void writeNode(String pre, String nodeRef, PrintStream stream, Node node, Dimension d) {
		stream.print(pre + TAB + nodeCount);
		try {
			String label = AttributeHelper.getLabel(node, null);
			if ((label != null) && !label.equals(""))
				stream.print(" [ label = \"" + label.trim() + "\" " + getNodeFormat(node, d) + "]");
		} catch (AttributeNotFoundException e) {
			// no label has been found: ignore
		}
		nodes.put(node, nodeRef);
		stream.println(";");
	}
	
	private String getNodeFormat(Node node, Dimension d) {
		double width = AttributeHelper.getWidth(node);
		double height = AttributeHelper.getHeight(node);
		int x = (int) AttributeHelper.getPositionX(node);
		int y = (int) AttributeHelper.getPositionY(node);
		boolean includeFormat = true;
		if (includeFormat) {
			return ", height=\"" + myFormat(height / 72f) + "\", " +
								"width=\"" + myFormat(width / 72f) + "\", pos=\"" + x + "," + (d.height - y) + "\"";
		} else
			return "";
	}
	
	private static DecimalFormat fff = ErrorMsg.getDecimalFormat("#.##");
	
	private String myFormat(double v) {
		return fff.format(v);
	}
	
	public static Dimension getMaximumXY(Collection<?> nodeList, double factorXY,
						int minx, int miny, boolean includeSizeInformation) {
		
		int maxx = 0, maxy = 0;
		
		Iterator<?> nodeIterator = nodeList.iterator();
		while (nodeIterator.hasNext()) {
			Object nodeOrPatternStruct = nodeIterator.next();
			int x, y;
			int sx = 0, sy = 0;
			Node currentNode = (Node) nodeOrPatternStruct;
			x = (int) AttributeHelper.getPositionX(currentNode);
			y = (int) AttributeHelper.getPositionY(currentNode);
			if (includeSizeInformation) {
				Vector2d sz = AttributeHelper.getSize(currentNode);
				sx = (int) sz.x;
				sy = (int) sz.y;
				x += sx;
				y += sy;
			}
			if (x > maxx)
				maxx = x;
			if (y > maxy)
				maxy = y;
		}
		return new Dimension((int) (maxx * factorXY - minx), (int) (maxy
							* factorXY - miny));
	}
	
	public static void setNextOrientationTopBottom(boolean topBottom) {
		DOTSerializer.topBottom = topBottom;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
