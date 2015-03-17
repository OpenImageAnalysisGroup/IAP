/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.text_list;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.AbstractInputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe.StringScanner;

/**
 * 
 edges in list form
 * S T W (header) a b 0.3 c b 0.7 % comment a c 1.0
 * or simple text format (see
 * http://www.graphdrawing.de/contest2009/gdcategories2009.html):
 * # Nodes Id1,40,40,20,20 Id2,40,40,100,100 Id3,40,40 # Edges
 * Id1,Id2,40,20,100,20,100,80 Id3,Id2
 * 
 * @author Christian Klukas
 */
public class TextListReader extends AbstractInputSerializer {
	
	private String fileNameExt = ".txt";
	
	/**
	 *
	 */
	public TextListReader() {
		super();
	}
	
	@Override
	public void read(String filename, Graph g) throws IOException {
		super.read(filename, g);
		if (g != null) {
			g.setName(filename);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.graffiti.plugin.io.AbstractInputSerializer#read(java.io.InputStream,
	 * org.graffiti.graph.Graph)
	 */
	@Override
	public void read(InputStream in, Graph g) throws IOException {
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader bi = new BufferedReader(isr);
		read(g, bi);
		isr.close();
	}
	
	boolean readNodes = false;
	boolean readEdges = false;
	
	private void read(Graph g, BufferedReader bi) throws IOException,
						FileNotFoundException {
		HashMap<String, Node> nodes = new HashMap<String, Node>();
		
		PositionGridGenerator positionGen = new PositionGridGenerator(30, 30,
							500);
		
		String currLine;
		int line = 0;
		
		while ((currLine = bi.readLine()) != null) {
			line++;
			if (currLine.length() > 0 && !currLine.equals(" ")) {
				if (currLine.contains(",") || currLine.startsWith("#")) {
					{
						processGraphDrawingFormat(g, nodes, positionGen,
											currLine);
					}
				} else {
					if (currLine.startsWith("%"))
						continue;
					processEdgeListFormat(g, nodes, positionGen, currLine);
				}
			}
			if (line % 1000 == 0) {
				MainFrame.showMessage("Read line " + line + " - "
									+ g.getNumberOfNodes() + " nodes, "
									+ g.getNumberOfEdges() + " edges created",
									MessageType.PERMANENT_INFO);
			}
		}
		bi.close();
		g.numberGraphElements();
		MainFrame.showMessage("Finished reading, created "
							+ g.getNumberOfNodes() + " nodes and " + g.getNumberOfEdges()
							+ " edges!", MessageType.INFO);
	}
	
	private void processGraphDrawingFormat(Graph g,
						HashMap<String, Node> nodes, PositionGridGenerator positionGen,
						String currLine) {
		if (currLine.startsWith("#")) {
			if (currLine.equalsIgnoreCase("# Nodes")) {
				readNodes = true;
				readEdges = false;
			}
			if (currLine.equalsIgnoreCase("# Edges")) {
				readNodes = false;
				readEdges = true;
			}
		} else {
			currLine = StringManipulationTools.stringReplace(currLine, " ,", ",");
			currLine = StringManipulationTools.stringReplace(currLine, " ,", ",");
			currLine = StringManipulationTools.stringReplace(currLine, ", ", ",");
			currLine = StringManipulationTools.stringReplace(currLine, ", ", ",");
			currLine = StringManipulationTools.stringReplace(currLine, " ", "_");
			currLine = StringManipulationTools.stringReplace(currLine, ",", " ");
			StringScanner s = new StringScanner(currLine, " ", "", "");
			if (readNodes) {
				String label = s.nextNotQuotedString();
				Vector2d size = null;
				if (s.stillInputAvailable()) {
					double width = s.nextDouble();
					if (s.stillInputAvailable()) {
						double height = s.nextDouble();
						size = new Vector2d(width, height);
					}
				}
				if (size == null)
					size = new Vector2d(20, 20);
				Vector2d position = null;
				if (s.stillInputAvailable()) {
					double px = s.nextDouble();
					if (s.stillInputAvailable()) {
						double py = s.nextDouble();
						position = new Vector2d(px, py);
					}
				}
				if (position == null)
					position = positionGen.getNextPositionVec2d();
				Node newNode = addNode(g, nodes, position, label);
				AttributeHelper.setSize(newNode, size);
			}
			if (readEdges) {
				String src = s.nextNotQuotedString();
				String tgt = s.nextNotQuotedString();
				Collection<Vector2d> points = new ArrayList<Vector2d>();
				while (s.stillInputAvailable()) {
					int bendPosX = s.nextInt();
					int bendPosY = s.nextInt();
					points.add(new Vector2d(bendPosX, bendPosY));
				}
				Node a = nodes.get(src);
				Node b = nodes.get(tgt);
				if (a != null && b != null) {
					Edge newEdge = g.addEdge(a, b, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.black, Color.black, true));
					if (points.size() > 0)
						AttributeHelper.addEdgeBends(newEdge, points);
				} else {
					ErrorMsg.addErrorMessage("Unknown edge source or target label (" + src + "/" + tgt + ")");
				}
			}
		}
	}
	
	private void processEdgeListFormat(Graph g, HashMap<String, Node> nodes,
						PositionGridGenerator positionGen, String currLine) {
		currLine = currLine.replaceAll("\t", " ");
		StringScanner s = new StringScanner(currLine, "", "", "");
		
		String src = s.nextNotQuotedString();
		String tgt = s.nextNotQuotedString();
		double weight = s.nextDouble();
		if (src != null && src.trim().length() > 0 && tgt != null
							&& tgt.trim().length() > 0) {
			src = src.trim();
			tgt = tgt.trim();
			Node a = nodes.get(src);
			Node b = nodes.get(tgt);
			if (a == null)
				a = addNode(g, nodes, positionGen.getNextPositionVec2d(), src);
			if (b == null)
				b = addNode(g, nodes, positionGen.getNextPositionVec2d(), tgt);
			mode2dirEdge(g, a, b, weight);
		}
	}
	
	private Node addNode(Graph g, HashMap<String, Node> nodes,
						Vector2d position, String src) {
		Node n;
		n = g.addNode();
		nodes.put(src, n);
		AttributeHelper.setLabel(n, src);
		AttributeHelper.setDefaultGraphicsAttribute(n, position.x, position.y);
		return n;
	}
	
	private void mode2dirEdge(Graph g, Node a, Node b, double weight) {
		Edge newEdge = g.addEdge(a, b, true);
		AttributeHelper.setAttribute(newEdge, "pajek", "dir", 1d);
		if (!Double.isNaN(weight))
			AttributeHelper.setAttribute(newEdge, "pajek", "weight", weight);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getExtensions()
	 */
	public String[] getExtensions() {
		return new String[] { fileNameExt };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "Simple Graph Format (src tgt [weight]/GD format)" };
	}
	
	public void read(Reader reader, Graph g) throws Exception {
		BufferedReader bi = new BufferedReader(reader);
		read(g, bi);
		reader.close();
	}
}
