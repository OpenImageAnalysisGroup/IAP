/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.pajek;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.Vector3d;
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
 edges in list form (not supported):
 * vertices <# of vertices>
 * 1 "x"
 * 2 "y"
 * 3 "z"
 * arcslist
 * 1 2 3
 * 2 3
 * non-list form for edges:
 * vertices <# of vertices>
 * 1 "a" x y z
 * 2 "b" x y
 * 3 "c"
 * edges
 * 1 2 0.3
 * 1 3 0.7
 * 2 3 1.0
 * arcs
 * 1 2 0.3
 * 1 3 0.7
 * 2 3 1.0
 * 
 * @author Christian Klukas
 */
public class PajekReader
					extends AbstractInputSerializer {
	
	private String fileNameExt = ".net";
	
	private String specialStart = "*";
	private String nodeStart = "*Vertices".toUpperCase();
	private String edgeStart = "*Arcs".toUpperCase();
	private String edgeUnDirStart = "*Edges".toUpperCase();
	private String filename;
	
	/**
	 *
	 */
	public PajekReader() {
		super();
	}
	
	@Override
	public void read(String filename, Graph g) throws IOException {
		this.filename = filename;
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
		HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
		/**
		 * mode = 0 => scan for known start lines
		 * mode = 1 => "*Vertices" found, now we read nodes
		 * mode = 2 => "*Arcs" found, now we read directed edges
		 * mode = 3 => "*Edges" found, now we read undirected edges
		 */
		int mode = 0;
		
		PositionGridGenerator positionGen = new PositionGridGenerator(30, 30, 500);
		boolean performPositionScaling = true;
		String currLine;
		while ((currLine = bi.readLine()) != null) {
			if (currLine.startsWith("%"))
				continue;
			if (currLine.length() > 0 && !currLine.equals(" ")) {
				if (currLine.startsWith(specialStart)) {
					if (currLine.toUpperCase().startsWith("*NETWORK"))
						g.setName(currLine.substring("*NETWORK".length()).trim());
					else
						if (currLine.toUpperCase().startsWith(nodeStart))
							mode = 1;
						else
							if (currLine.toUpperCase().startsWith(edgeStart))
								mode = 2;
							else
								if (currLine.toUpperCase().startsWith(edgeUnDirStart))
									mode = 3;
								else
									mode = 0;
				} else {
					StringScanner s = new StringScanner(currLine, " ", " ", " ");
					switch (mode) {
						case 1:
							if (!mode1node(g, nodes, positionGen, s))
								performPositionScaling = false;
							break;
						case 2:
							mode2dirEdge(g, nodes, s);
							break;
						case 3:
							mode3undirEdge(g, nodes, s);
							break;
						default:
							; // ignore
					}
				}
			}
		}
		bi.close();
		File clusterFile = null;
		if (filename == null) {
			ErrorMsg.addErrorMessage("Internal Error: Cluster Information (if available), could not be read. Check File-Load-Call.");
		} else {
			String clusterFilename = filename.substring(0, filename.length() - fileNameExt.length()) + ".clu";
			clusterFile = new File(clusterFilename);
			if (!clusterFile.canRead()) {
				clusterFilename = filename.substring(0, filename.length() - fileNameExt.length()) + ".CLU";
				clusterFile = new File(clusterFilename);
			}
			if (clusterFile.canRead()) {
				readClusterInfo(clusterFile, nodes);
			} else {
				MainFrame.showMessage("No Cluster Information (.CLU) found.", MessageType.INFO, 1000);
			}
		}
		g.numberGraphElements();
		if (performPositionScaling) {
			for (Node n : g.getNodes()) {
				Vector3d p = AttributeHelper.getPositionVec3d(n, Double.NaN, false);
				AttributeHelper.setPosition(n, p.x * 100, p.y * 100);
				if (!Double.isNaN(p.z)) {
					AttributeHelper.setPositionZ(n, p.z * 100);
				}
			}
		}
	}
	
	private void mode2dirEdge(Graph g, HashMap<Integer, Node> nodes, StringScanner s) {
		Node node1 = nodes.get(new Integer(s.nextInt()));
		Node node2 = nodes.get(new Integer(s.nextInt()));
		Double weight = new Double(s.nextDouble());
		if (weight != null && weight.isNaN())
			weight = null;
		Edge newEdge = g.addEdge(node1, node2, true);
		AttributeHelper.setAttribute(newEdge, "pajek", "dir", 1d);
		if (weight != null)
			AttributeHelper.setAttribute(newEdge, "pajek", "weight", weight);
	}
	
	private void mode3undirEdge(Graph g, HashMap<Integer, Node> nodes, StringScanner s) {
		Node node1 = nodes.get(new Integer(s.nextInt()));
		Node node2 = nodes.get(new Integer(s.nextInt()));
		Double weight = new Double(s.nextDouble());
		if (weight != null && weight.isNaN())
			weight = null;
		Edge newEdge = g.addEdge(node1, node2, false);
		AttributeHelper.setAttribute(newEdge, "pajek", "dir", 0d);
		if (weight != null)
			AttributeHelper.setAttribute(newEdge, "pajek", "weight", weight);
	}
	
	/**
	 * @return true, in case x, y and z position is less or equal to 1 and greater or equal to 0
	 */
	private boolean mode1node(Graph g, HashMap<Integer, Node> nodes, PositionGridGenerator positionGen, StringScanner s) {
		boolean requestScale = true;
		Node newNode = g.addNode();
		Integer id = new Integer(s.nextInt());
		nodes.put(id, newNode);
		AttributeHelper.setAttribute(newNode, "", "pajek_id", id);
		if (s.stillInputAvailable() && s.contains("\""))
			AttributeHelper.setLabel(newNode, s.nextString("\""));
		else
			if (s.stillInputAvailable())
				AttributeHelper.setLabel(newNode, s.nextNotQuotedString());
		double x = s.nextDouble();
		double y = s.nextDouble();
		double z = s.nextDouble();
		double faktor = 1;
		if (!Double.isNaN(x) && !Double.isNaN(y)) {
			if (x < 0 || x > 1 || y < 0 || y > 1)
				requestScale = false;
			AttributeHelper.setDefaultGraphicsAttribute(newNode, x * faktor, y * faktor);
			if (!Double.isNaN(z))
				AttributeHelper.setPositionZ(newNode, z * faktor);
		} else {
			AttributeHelper.setDefaultGraphicsAttribute(newNode, positionGen.getNextPosition());
			requestScale = false;
		}
		return requestScale;
	}
	
	/**
	 * @param in
	 * @param bi
	 * @param clusterFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readClusterInfo(File clusterFile, HashMap<Integer, Node> nodes) throws FileNotFoundException, IOException {
		MainFrame.showMessage("Cluster Information (.CLU) found.", MessageType.INFO, 1000);
		FileInputStream fisCl = new FileInputStream(clusterFile);
		InputStreamReader isrCl = new InputStreamReader(fisCl);
		BufferedReader biCl = new BufferedReader(isrCl);
		/**
		 * Mode = 0 ==> no start tag found
		 * Mode = 1 ==> "*Verticies" found => read cluster info
		 */
		int mode = 0;
		int curNodeId = 0;
		String currLine = biCl.readLine();
		while (currLine != null) {
			if (currLine.toUpperCase().startsWith(specialStart)) {
				if (currLine.toUpperCase().startsWith(nodeStart))
					mode = 1;
				else
					mode = 0;
			} else {
				if (mode == 1 && !currLine.equals("") && !currLine.equals(" ")) {
					curNodeId += 1;
					try {
						String cluster = currLine.trim();
						Node n = (Node) nodes.get(new Integer(curNodeId));
						NodeTools.setClusterID(n, cluster);
					} catch (NumberFormatException nfe) {
						ErrorMsg.addErrorMessage(nfe.getLocalizedMessage());
					}
				}
			}
			currLine = biCl.readLine();
		}
		biCl.close();
		isrCl.close();
		fisCl.close();
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
		return new String[] { "Pajek" };
	}
	
	public void read(Reader reader, Graph g) throws Exception {
		BufferedReader bi = new BufferedReader(reader);
		read(g, bi);
		reader.close();
	}
}
