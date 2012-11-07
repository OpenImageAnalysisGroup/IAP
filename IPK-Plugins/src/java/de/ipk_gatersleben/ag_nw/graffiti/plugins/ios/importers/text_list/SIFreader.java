/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.text_list;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import org.AttributeHelper;
import org.PositionGridGenerator;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.AbstractInputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe.StringScanner;

/**
 * 
 edges in list form
 * S I T (Source Interaction Type Target)
 * =====
 * n1 i1 n2
 * n2 i1 n3
 * n3 i2 n1
 * 
 * @author Christian Klukas
 */
public class SIFreader
					extends AbstractInputSerializer {
	
	private final String fileNameExt = ".sif";
	
	/**
	 *
	 */
	public SIFreader() {
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
	 * @see org.graffiti.plugin.io.AbstractInputSerializer#read(java.io.InputStream, org.graffiti.graph.Graph)
	 */
	@Override
	public void read(InputStream in, Graph g)
						throws IOException {
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader bi = new BufferedReader(isr);
		read(g, bi);
		isr.close();
	}
	
	private void read(Graph g, BufferedReader bi) throws IOException, FileNotFoundException {
		HashMap<String, Node> nodes = new HashMap<String, Node>();
		
		PositionGridGenerator positionGen = new PositionGridGenerator(30, 30, 500);
		
		String currLine;
		int line = 0;
		while ((currLine = bi.readLine()) != null) {
			line++;
			if (currLine.startsWith("%"))
				continue;
			if (currLine.length() > 0 && !currLine.equals(" ")) {
				currLine = currLine.replaceAll("\t", " ");
				StringScanner s = new StringScanner(currLine, "", "", "");
				
				String src = s.nextNotQuotedString();
				String type = s.nextNotQuotedString();
				String tgt = s.nextNotQuotedString();
				s.nextDouble();
				if (src != null && src.trim().length() > 0) {
					src = src.trim();
					Node a = nodes.get(src);
					if (a == null)
						a = addNode(g, nodes, positionGen, src);
					
					if (tgt != null && tgt.trim().length() > 0) {
						tgt = tgt.trim();
						type = type.trim();
						Node b = nodes.get(tgt);
						if (b == null)
							b = addNode(g, nodes, positionGen, tgt);
						mode2dirEdge(g, a, b, type);
					}
				}
			}
			if (line % 1000 == 0) {
				MainFrame.showMessage("Read line " + line + " - " + g.getNumberOfNodes() + " nodes, " + g.getNumberOfEdges() + " edges created",
									MessageType.PERMANENT_INFO);
			}
		}
		bi.close();
		g.numberGraphElements();
		MainFrame.showMessage("Finished reading, created " + g.getNumberOfNodes() + " nodes and " + g.getNumberOfEdges() + " edges!", MessageType.INFO);
	}
	
	private Node addNode(Graph g, HashMap<String, Node> nodes, PositionGridGenerator positionGen, String src) {
		Node n;
		n = g.addNode();
		nodes.put(src, n);
		AttributeHelper.setLabel(n, src);
		AttributeHelper.setDefaultGraphicsAttribute(n, positionGen.getNextPosition());
		return n;
	}
	
	private void mode2dirEdge(Graph g, Node a, Node b, String type) {
		Edge newEdge = g.addEdge(a, b, true);
		if (type != null && type.length() > 0) {
			NodeTools.setClusterID(newEdge, type);
			AttributeHelper.setLabel(newEdge, type);
		}
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
		return new String[] { "SIF (src interac. tgt)" };
	}
	
	public void read(Reader reader, Graph g) throws Exception {
		BufferedReader bi = new BufferedReader(reader);
		read(g, bi);
		reader.close();
	}
}
