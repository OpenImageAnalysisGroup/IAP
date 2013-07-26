/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.petri_ina_pnt;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.AbstractOutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * @author klukas
 *         7.12.2006
 */
public class PNTSerializer
					extends AbstractOutputSerializer {
	/*
	 * @see org.graffiti.plugin.io.Serializer#getExtensions()
	 */
	public String[] getExtensions() {
		return new String[] { ".pnt" };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "INA PNT" };
	}
	
	/*
	 * @see org.graffiti.plugin.io.OutputSerializer#write(java.io.OutputStream,
	 * org.graffiti.graph.Graph)
	 */
	public synchronized void write(OutputStream out, Graph g) {
		PrintStream stream = new PrintStream(out);
		
		stream.print("P   M    PRE,POST  NETZ 1:" + encodeName(g.getName()) + "\r\n");
		int i = 0;
		HashMap<Node, Integer> node2placeNo = new HashMap<Node, Integer>();
		HashMap<Node, ArrayList<Integer>> node2transNo = new HashMap<Node, ArrayList<Integer>>();
		int nextTransIdx = 1;
		for (Node n : g.getNodes()) {
			if (isTrans(n)) {
				ArrayList<Integer> idxList = new ArrayList<Integer>();
				idxList.add(nextTransIdx);
				if (definesReversibleTransitionWithoutReferingToModifiers(n)) {
					nextTransIdx++;
					idxList.add(nextTransIdx);
				}
				node2transNo.put(n, idxList);
				nextTransIdx++;
			}
		}
		for (Node n : g.getNodes()) {
			if (isPlace(n)) {
				writePlaceConnectionInfo(i, n, g, stream, node2transNo);
				node2placeNo.put(n, i);
				i++;
			}
		}
		stream.print("@\r\n");
		stream.print("place nr.             name capacity time\r\n");
		for (Node n : g.getNodes()) {
			if (isPlace(n)) {
				int idx = node2placeNo.get(n);
				writePlaceInfo(idx, n, g, stream);
			}
		}
		stream.print("@\r\n");
		stream.print("trans nr.             name priority time\r\n");
		for (Node n : g.getNodes()) {
			if (isTrans(n)) {
				ArrayList<Integer> idxList = node2transNo.get(n);
				int ii = 0;
				for (int idx : idxList) {
					if (idxList.size() == 2 && ii == 0)
						writeTransInfo(idx, n, g, stream, "f_");
					else {
						if (idxList.size() == 2 && ii == 1)
							writeTransInfo(idx, n, g, stream, "b_");
						else
							writeTransInfo(idx, n, g, stream, "");
					}
					ii++;
				}
			}
		}
		stream.println("@\r\n");
		
		// finished
		stream.close();
		
		reversibeErrorCheck(g);
	}
	
	private boolean definesReversibleTransitionWithoutReferingToModifiers(Node n) {
		boolean foundReversible = false;
		for (Edge e : n.getEdges()) {
			boolean isReversible = AttributeHelper.isSBMLreversibleReaction(e);
			String role = AttributeHelper.getSBMLrole(e);
			if (role != null && role.equals("modifier")) {
				continue;
			}
			if (isReversible)
				foundReversible = true;
		}
		return foundReversible;
	}
	
	private void reversibeErrorCheck(Graph g) {
		Collection<Node> invalidNodes = new ArrayList<Node>();
		for (Node n : g.getNodes()) {
			if (isTrans(n)) {
				boolean foundReversible = false;
				boolean foundIrReversible = false;
				for (Edge e : n.getEdges()) {
					boolean isReversible = AttributeHelper.isSBMLreversibleReaction(e);
					String role = AttributeHelper.getSBMLrole(e);
					if (role != null && role.equals("modifier")) {
						continue;
					}
					if (isReversible)
						foundReversible = true;
					else
						foundIrReversible = true;
				}
				if (foundReversible && foundIrReversible) {
					invalidNodes.add(n);
				}
			}
		}
		if (invalidNodes.size() > 0) {
			MainFrame.showMessageDialog(
								"<html>" +
													"A number of invalid transitions have been found.<br>" +
													"Edges, connected to these transitions do not have a<br>" +
													"uniform setting for the specification of a reversible<br>" +
													"or irreversible reaction.<br>" +
													"<b>The written PNT file might therefore be not error-free.</b><br>" +
													"The invalid nodes have been selected.",
								"Invalid Transitions (reversible/irreversible)");
			GraphHelper.selectNodes(invalidNodes);
		}
	}
	
	private String encodeName(String name) {
		String res = "";
		if (name != null)
			res = name;
		res = StringManipulationTools.removeHTMLtags(res);
		res = StringManipulationTools.stringReplace(res, " ", "_");
		while (res.length() < 16)
			res = res + " ";
		res = res.substring(0, 16);
		return res;
	}
	
	private void writePlaceConnectionInfo(int i, Node n, Graph g, PrintStream stream, HashMap<Node, ArrayList<Integer>> node2transNo) {
		String a = getPreOrPostTransitionInfo(true, n, node2transNo);
		String b = getPreOrPostTransitionInfo(false, n, node2transNo);
		if (a.length() <= 0)
			a = "";
		if (b.length() > 0)
			b = ", " + b;
		stream.print(getSpace(i + "", " ", 3) + " " + getSpace(getMarkierung(n) + "", " ", 1) + "      " + a + b + "\r\n");
	}
	
	private int getMarkierung(Node n) {
		String lbl = AttributeHelper.getLabel(n, "");
		if (lbl.indexOf(":") > 0) {
			String pre = lbl.substring(0, lbl.indexOf(":")).trim();
			try {
				return Integer.parseInt(pre);
			} catch (NumberFormatException nfe) {
				return 0;
			}
		} else
			return 0;
	}
	
	private String getPreOrPostTransitionInfo(boolean pre, Node nodePlace, HashMap<Node, ArrayList<Integer>> node2transNo) {
		TreeSet<String> tt = new TreeSet<String>();
		for (Edge e : nodePlace.getEdges()) {
			if (e.getSource() == e.getTarget())
				continue;
			Node otherNode = e.getSource() == nodePlace ? e.getTarget() : e.getSource();
			String edgeLabel = AttributeHelper.getLabel(e, "");
			int freq = 1;
			try {
				if (edgeLabel != null && edgeLabel.length() > 0)
					freq = Integer.parseInt(edgeLabel);
			} catch (NumberFormatException nfe) {
				try {
					double t = Double.parseDouble(edgeLabel);
					freq = (int) t;
					ErrorMsg.addErrorMessage("Warning: non-Integer value " + edgeLabel + " has been interpreted as value " + freq + ", for PNT export!");
				} catch (NumberFormatException nfe2) {
					ErrorMsg.addErrorMessage(nfe);
				}
			}
			
			boolean isModifier = false;
			boolean isSubstrateEdge = false;
			String role = AttributeHelper.getSBMLrole(e);
			if (role != null && role.equals("modifier")) {
				isModifier = true;
			}
			if (role != null && role.equals("reactant")) {
				isSubstrateEdge = true;
			}
			
			boolean reversible = AttributeHelper.isSBMLreversibleReaction(e);
			
			if (isModifier || reversible || (!reversible && ((pre && e.getTarget() == nodePlace) || (!pre && e.getSource() == nodePlace)))) {
				ArrayList<Integer> tidxlist = node2transNo.get(otherNode);
				int transIdx = tidxlist.get(0);
				if (tidxlist.size() > 2)
					ErrorMsg.addErrorMessage("More than 2 possible transitions for a given node!");
				if (tidxlist.size() < 1)
					ErrorMsg.addErrorMessage("No possible transitions for a given node!");
				if (tidxlist.size() == 2) {
					if (isSubstrateEdge) {
						if (pre)
							transIdx = tidxlist.get(1);
						else
							transIdx = tidxlist.get(0);
					} else {
						if (pre)
							transIdx = tidxlist.get(0);
						else
							transIdx = tidxlist.get(1);
						
					}
				}
				
				String t;
				if (freq != 1) {
					t = transIdx + ": " + freq;
				} else
					t = transIdx + "";
				tt.add(t);
			}
		}
		StringBuilder res = new StringBuilder();
		for (String t : tt) {
			res.append(t);
			res.append(" ");
		}
		return res.toString().trim();
	}
	
	private void writeTransInfo(int idx, Node n, Graph g, PrintStream stream, String preLbl) {
		NodeHelper nh = new NodeHelper(n);
		stream.print(getSpace(idx + "", " ", 8) + ": " + encodeName(preLbl + nh.getLabel()) + "        0    0\r\n");
	}
	
	private void writePlaceInfo(int idx, Node n, Graph g, PrintStream stream) {
		NodeHelper nh = new NodeHelper(n);
		stream.print(getSpace(idx + "", " ", 8) + ": " + encodeName(nh.getLabel()) + "       oo    0\r\n");
	}
	
	private String getSpace(String value, String spaceString, int len) {
		String res = value;
		while (res.length() < len)
			res = spaceString + res;
		return res;
	}
	
	private boolean isTrans(Node n) {
		NodeHelper nh = new NodeHelper(n);
		String shape = nh.getShape();
		return shape != null && shape.indexOf("Rectangle") >= 0 && nh.getDegree() > 0;
	}
	
	private boolean isPlace(Node n) {
		NodeHelper nh = new NodeHelper(n);
		String shape = nh.getShape();
		return shape != null && (shape.indexOf("Ellipse") >= 0 || shape.indexOf("Circle") >= 0) && nh.getDegree() > 0;
	}
}
