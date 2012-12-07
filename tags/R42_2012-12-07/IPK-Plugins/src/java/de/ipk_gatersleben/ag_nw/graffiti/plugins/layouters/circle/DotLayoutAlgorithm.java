/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemInfo;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;
import org.graffiti.plugins.ios.exporters.graphviz.DOTSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.apply_from_graph.ApplyGraphLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;

/**
 * @author Christian Klukas
 */
public class DotLayoutAlgorithm extends AbstractAlgorithm {
	
	private String layoutCommand;
	private boolean layoutEdges = false;
	private int increaseSize = 0;
	private String dotOrientation = "Left-Right";
	private boolean sortChildren = false;
	private boolean considerSameNodes = false;
	
	private String installPath = getInstallPath();
	private boolean movetopleft;
	
	/**
	 * Creates a new CircleLayouterAlgorithm object.
	 */
	public DotLayoutAlgorithm() {
		super();
	}
	
	public static boolean isInstalled() {
		String path = getInstallPath();
		return path != null && !path.equals("") && new File(path).exists();
	}
	
	public static String getInstallPath() {
		try {
			if (!SystemInfo.isLinux() && !SystemInfo.isMac()) {
				// assume windows
				// get program path
				String[] ps = { System.getenv("ProgramFiles"), System.getenv("ProgramFiles") + " (x86)" };
				for (String path : ps) {
					if (path != null && path.length() > 0) {
						File f = new File(path);
						if (f.exists()) {
							for (String app : f.list()) {
								if (app.startsWith("Graphviz")) {
									String found = path + "\\" + app + "\\bin";
									if (new File(found).exists()) {
										return found + "\\";
									}
								}
							}
						}
					}
				}
			}
			String[] paths = { "/usr/local/bin/", "/usr/bin/" };
			for (String p : paths) {
				if (new File(p + "dot").exists()) {
					return p;
				}
			}
		} catch (Exception e) {
			// empty
		}
		return "";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new ObjectListParameter(getDOT(), "<html>" +
												"Command",
												"<html>DOT needs to be installed<br>" +
																	"and needs to be available<br>" +
																	"in the search path. See<br>" +
																	"www.graphviz.org for details!", getLayouts()),
							new IntegerParameter(increaseSize, "Increase Node Size", "If set, the node sizes will be increased (or decreased) for the layout"),
							new BooleanParameter(layoutEdges, "Layout Edges", "If enabled, the edge routing is considered"),
							new ObjectListParameter(dotOrientation, "Orientation (DOT)", "", new Object[] { "Left-Right", "Top-Down" }),
							new BooleanParameter(sortChildren, "Sort Nodes", "If enabled, the ordering of nodes is determined by the ordering of their labels"),
							new BooleanParameter(considerSameNodes, "Unify Subgraphs", "If enabled, subgraph nodes with the same labels are concurrently processed"),
							new StringParameter(installPath, "<html>" +
												"Program path<br>" +
												"(optional)", "The path, where the layout programs are installed"),
								new BooleanParameter(true, "Move graph to top-left",
													"<html>If set, the graph will be moved to top-left,<br>after layouting has been completed") };
	}
	
	private Collection<String> getLayouts() {
		ArrayList<String> res = new ArrayList<String>();
		res.add(getDOT());
		res.add(getNeato());
		res.add(getTWOPI());
		res.add(getCIRCO());
		res.add(getFDP());
		return res;
	}
	
	private String getDOT() {
		return "dot";
	}
	
	private String getNeato() {
		return "neato";
	}
	
	private String getTWOPI() {
		return "twopi";
	}
	
	private String getCIRCO() {
		return "circo";
	}
	
	private String getFDP() {
		return "fdp";
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		layoutCommand = (String) ((ObjectListParameter) params[i++]).getValue();
		increaseSize = (Integer) ((IntegerParameter) params[i++]).getValue();
		layoutEdges = (Boolean) ((BooleanParameter) params[i++]).getValue();
		dotOrientation = (String) ((ObjectListParameter) params[i++]).getValue();
		sortChildren = (Boolean) ((BooleanParameter) params[i++]).getValue();
		considerSameNodes = (Boolean) ((BooleanParameter) params[i++]).getValue();
		installPath = ((StringParameter) params[i++]).getString();
		movetopleft = ((BooleanParameter) params[i++]).getBoolean();
		super.setParameters(params);
	}
	
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
		
		if (graph.getNumberOfNodes() <= 0) {
			throw new PreconditionException("The graph is empty. Cannot run layouter.");
		}
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"This layout command requires the following Graphviz " +
							"command-line layout commands to be available in the " +
							"search path:<br>" +
							"<ul>" +
							"<li>dot - layout directed acyclic graphs" +
							"<li>neato - spring model Kamada and Kawai" +
							"<li>twopi - radial layout" +
							"<li>circo - circular layout" +
							"<li>fdp - spring model Fruchterman and Reingold" +
							"</ul>" +
							"<small>See www.graphviz.org for further details.<br>" +
							"Try 'dot -V' at the command line, to see if this layouter is " +
							"correctly installed and included in the search path.<br><br>" +
							"Remark: Edge Layout may not work correctly for undirected graphs. " +
							"Use the network tab and temporarily change the 'directed edges' setting before layouting " +
							"the network." +
							"</small>";
	}
	
	@Override
	public void reset() {
		super.reset();
	}
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name of the algorithm
	 */
	public String getName() {
		return "External Graphviz Layout";
	}
	
	public void execute() {
		try {
			String fileName = ReleaseInfo.getAppFolderWithFinalSep() + "temp.dot";
			String fileName2 = ReleaseInfo.getAppFolderWithFinalSep() + "temp_layouted.dot";
			if (new File(fileName).exists())
				new File(fileName).delete();
			if (new File(fileName2).exists())
				new File(fileName2).delete();
			
			if (considerSameNodes) {
				createUnifiedGraph();
			}
			
			HashMap<Node, String> node2oldLabel = new HashMap<Node, String>();
			try {
				int lblIdx = 1;
				for (Node n : getSelectedOrAllNodes(true, sortChildren)) {
					String lbl = AttributeHelper.getLabel(n, null);
					node2oldLabel.put(n, lbl);
					AttributeHelper.setLabel(n, "node" + lblIdx);
					if (increaseSize != 0) {
						Vector2d size = AttributeHelper.getSize(n);
						size.x += increaseSize;
						size.y += increaseSize;
						AttributeHelper.setSize(n, size);
					}
					lblIdx++;
				}
				
				DOTSerializer.setNextOrientationTopBottom(dotOrientation.contains("Top"));
				
				Vector2d center = null;
				if (!movetopleft)
					center = NodeTools.getCenter(getSelectedOrAllNodes(true));
				if (getSelectedOrAllNodes(true).size() <= graph.getNumberOfNodes()) {
					Graph gg = new AdjListGraph();
					HashMap<Node, Node> srcNode2tgtNode = new HashMap<Node, Node>();
					for (Node n : getSelectedOrAllNodes(true, sortChildren)) {
						Node newNode = gg.addNodeCopy(n);
						srcNode2tgtNode.put(n, newNode);
					}
					for (Edge e : graph.getEdges()) {
						if (srcNode2tgtNode.containsKey(e.getSource()) && srcNode2tgtNode.containsKey(e.getTarget())) {
							gg.addEdgeCopy(e, srcNode2tgtNode.get(e.getSource()), srcNode2tgtNode.get(e.getTarget()));
						}
					}
					saveGraphAsDot(gg, fileName);
				} else
					saveGraphAsDot(graph, fileName);
				if (new File(fileName).exists()) {
					String llcc = layoutCommand;
					if (layoutCommand.indexOf(" ") > 0)
						llcc = layoutCommand.substring(0, layoutCommand.indexOf(" "));
					String pp = llcc + " -Tdot '" + fileName + "' -o '" + fileName2 + "'";
					Process p = Runtime.getRuntime().exec(new String[] {
										installPath + llcc,
										" -Tdot",
										fileName,
										"-o",
										fileName2
					});
					// Process p = Runtime.getRuntime().exec(pp);
					p.waitFor();
					System.out.println("Exec: " + pp);
					System.out.println("Result: " + p.exitValue());
					if (new File(fileName2).exists()) {
						Graph layoutedGraph = MainFrame.getInstance().getGraph(new File(fileName2));
						if (!movetopleft)
							NodeTools.setCenter(layoutedGraph.getNodes(), center);
						ApplyGraphLayout.applyLayoutFromGraphToGraph(selection, graph, layoutedGraph, getName(), layoutEdges);
					} else {
						MainFrame.getInstance().showMessageDialog("<html>" +
											"External call to DOT layout did not produce output (file " + fileName + ")" +
											"<br><br>Eventually the DOT program is not available in the search path or is not installed at all.<br>" +
											"<br>Tried to execute:<br>" +
											"> " + pp + "<br>" +
											"Return code: " + p.exitValue());
					}
				} else {
					ErrorMsg.addErrorMessage("Could not create or save DOT file: " + fileName);
				}
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
				String command = layoutCommand;
				if (layoutCommand.indexOf(" ") >= 0)
					command = layoutCommand.substring(0, layoutCommand.indexOf(" "));
				MainFrame.showMessageDialog(
									"<html>" +
														"A I/O Error occurred. A possible source of the problem might be that the external layout program<br>" +
														"could not be found. Please check if you are able to start the layout program &quot;" + command + "&quot;<br>" +
														"from the command line of your operating system. This command line layout program needs to be<br>" +
														"installed and needs to be included in the application search path.<br>" +
														"Consult the www.graphviz.org website for information on how to download and install the layout<br>" +
														"programs and consult web information ressources and operating system documentation for information<br>" +
														"on how to include the installation path of the layouters in the search path of your operating system.<br>" +
														"Further technical details of this error are available from the error-log (Help/Error Messages),<br>" +
														"eventually other problem sources need to be considered.",
									"Error");
			} finally {
				for (Node n : graph.getNodes()) {
					if (!node2oldLabel.containsKey(n))
						continue;
					String oldlbl = node2oldLabel.get(n);
					AttributeHelper.setLabel(n, oldlbl);
					if (increaseSize != 0) {
						Vector2d size = AttributeHelper.getSize(n);
						size.x -= increaseSize;
						size.y -= increaseSize;
						AttributeHelper.setSize(n, size);
					}
					
				}
				if (considerSameNodes) {
					moveGraphsToUnifiedGraph();
				}
				if (movetopleft)
					CenterLayouterAlgorithm.moveGraph(graph, getName(), true, 50, 50);
				
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private void saveGraphAsDot(Graph g, String fileName) throws Exception {
		DOTSerializer ser = new DOTSerializer();
		ser.write(g, fileName);
	}
	
	double maxWidth, maxHeight;
	
	@SuppressWarnings("unchecked")
	private void moveGraphsToUnifiedGraph() {
		HashMap<String, Node> lbl2uniNode = new HashMap<String, Node>();
		Set<String> clusters = new TreeSet<String>();
		for (Node n : getSelectedOrAllNodes(true)) {
			String clusterId = NodeTools.getClusterID(n, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
			String uni = (String) AttributeHelper.getAttributeValue(n, "temp", "unigraph", "", "");
			if (uni.equals("true")) {
				lbl2uniNode.put(AttributeHelper.getLabel(n, ""), n);
			}
		}
		HashMap<String, Integer> clusterId2idx = new HashMap<String, Integer>();
		int idx = 0;
		for (String cluster : clusters) {
			clusterId2idx.put(cluster, idx);
			idx++;
		}
		for (Node n : graph.getNodes()) {
			String clusterId = NodeTools.getClusterID(n, "");
			if (!clusterId.equals("")) {
				String lbl = upLabel(n);
				Node uniNode = lbl2uniNode.get(lbl);
				if (uniNode != null) {
					AttributeHelper.setHidden((Collection) n.getEdges(), true);
					Vector2d posUniNode = AttributeHelper.getPositionVec2d(uniNode);
					Vector2d sizeUniNode = AttributeHelper.getSize(uniNode);
					idx = clusterId2idx.get(clusterId);
					double targetX = posUniNode.x - sizeUniNode.x / 2 + maxWidth * idx + maxWidth / 2;
					double targetY = posUniNode.y - 5;
					AttributeHelper.setPosition(n, targetX, targetY);
					if (uniNode.getOutDegree() > 0)
						AttributeHelper.setLabelAlignment(-1, uniNode, AlignmentSetting.LEFT);
					else
						AttributeHelper.setLabelAlignment(-1, uniNode, AlignmentSetting.RIGHT);
					AttributeHelper.getLabel(-1, n).setFontSize(10);
					AttributeHelper.setLabelAlignment(-1, n, AlignmentSetting.BELOW);
				}
			}
		}
		for (Node n : lbl2uniNode.values()) {
			AttributeHelper.setLabel(n, processUpLabelRevert(n));
		}
		for (Node n : graph.getNodes()) {
			String ll = extractNumericDataFromStoredParenthesisData(n);
			if (ll.length() > 0)
				AttributeHelper.setLabel(n, ll);
		}
	}
	
	private String extractNumericDataFromStoredParenthesisData(Node n) {
		String storedLabel = (String) AttributeHelper.getAttributeValue(n, "", "oldlabel", "", "");
		if (storedLabel.indexOf("(") >= 0) {
			storedLabel = storedLabel.substring(storedLabel.indexOf("(") + "(".length());
			if (storedLabel.indexOf(")") >= 0) {
				storedLabel = storedLabel.substring(0, storedLabel.indexOf(")"));
				storedLabel = StringManipulationTools.stringReplace(storedLabel, " ", "");
				storedLabel = StringManipulationTools.stringReplace(storedLabel, ",", ";");
				return storedLabel.trim();
			} else
				return "";
		} else
			return "";
	}
	
	private void createUnifiedGraph() {
		maxWidth = 0;
		maxHeight = 0;
		
		Set<String> clusters = new TreeSet<String>();
		HashMap<String, ArrayList<Node>> cluster2nodes = new HashMap<String, ArrayList<Node>>();
		for (Node ge : getSelectedOrAllNodes(true)) {
			String clusterId = NodeTools.getClusterID(ge, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
			if (!cluster2nodes.containsKey(clusterId))
				cluster2nodes.put(clusterId, new ArrayList<Node>());
			cluster2nodes.get(clusterId).add(ge);
			Vector2d size = AttributeHelper.getSize(ge);
			if (size.x > maxWidth)
				maxWidth = size.x;
			if (size.y > maxHeight)
				maxHeight = size.y;
		}
		
		HashSet<String> edgeList = new HashSet<String>();
		for (Edge e : graph.getEdges()) {
			if (!AttributeHelper.isHiddenGraphElement(e.getSource()) && !AttributeHelper.isHiddenGraphElement(e.getTarget())) {
				String lblA = upLabel(e.getSource());
				String lblB = upLabel(e.getTarget());
				if (lblA.length() > 0 && lblB.length() > 0) {
					edgeList.add(lblA + ">" + lblB);
				}
			}
		}
		
		maxWidth += 20;
		maxHeight += 20;
		
		double targetWidth = clusters.size() * maxWidth;
		double targetHeight = maxHeight;
		
		HashMap<String, Node> newNodes = new HashMap<String, Node>();
		for (String edge : edgeList) {
			String lA = edge.substring(0, edge.indexOf(">"));
			String lB = edge.substring(edge.indexOf(">") + ">".length());
			boolean newNode = false;
			if (!newNodes.containsKey(lA)) {
				Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(50, 50));
				AttributeHelper.setLabel(n, lA);
				AttributeHelper.setSize(n, targetWidth, targetHeight);
				AttributeHelper.setAttribute(n, "temp", "unigraph", "true");
				AttributeHelper.setBorderWidth(n, 1);
				AttributeHelper.setRoundedEdges(n, 25);
				newNodes.put(lA, n);
				newNode = true;
			}
			Node nA = newNodes.get(lA);
			if (!newNodes.containsKey(lB)) {
				Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(50, 50));
				AttributeHelper.setLabel(n, lB);
				AttributeHelper.setSize(n, targetWidth, targetHeight);
				AttributeHelper.setAttribute(n, "temp", "unigraph", "true");
				AttributeHelper.setBorderWidth(n, 1);
				AttributeHelper.setRoundedEdges(n, 25);
				newNodes.put(lB, n);
				newNode = true;
			}
			Node nB = newNodes.get(lB);
			
			if (newNode || !nA.getOutNeighbors().contains(nB)) {
				graph.addEdge(nA, nB, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
			}
		}
	}
	
	private String upLabel(Node source) {
		String label = AttributeHelper.getLabel(source, "");
		if (source.getInDegree() == 1) {
			return upLabel(source.getInNeighbors().iterator().next()) + "/" + label;
		} else
			return "/" + label;
	}
	
	private String processUpLabelRevert(Node n) {
		String label = AttributeHelper.getLabel(n, "");
		if (label.indexOf("/") >= 0)
			return label.substring(label.lastIndexOf("/") + "/".length());
		else
			return label;
	}
	
	private Collection<Node> getSelectedOrAllNodes(boolean filterNonVisible) {
		if (!filterNonVisible)
			return getSelectedOrAllNodes();
		else {
			Collection<Node> result = new ArrayList<Node>();
			for (Node n : getSelectedOrAllNodes())
				if (!AttributeHelper.isHiddenGraphElement(n))
					result.add(n);
			return result;
		}
	}
	
	private Collection<Node> getSelectedOrAllNodes(boolean filterNonVisible, boolean sortByLabel) {
		if (sortByLabel) {
			ArrayList<Node> result = new ArrayList<Node>(getSelectedOrAllNodes(filterNonVisible));
			Collections.sort(result, new Comparator<Node>() {
				public int compare(Node o1, Node o2) {
					try {
						Double v1 = (Double) AttributeHelper.getAttributeValue(o1, "properties", "sample_ratio_avg", Double.MAX_VALUE, 0d);
						Double v2 = (Double) AttributeHelper.getAttributeValue(o2, "properties", "sample_ratio_avg", Double.MAX_VALUE, 0d);
						int r = v1.compareTo(v2);
						if (r == 0) {
							String label1 = AttributeHelper.getLabel(o1, "");
							String label2 = AttributeHelper.getLabel(o2, "");
							return label1.compareTo(label2);
						} else
							return r;
					} catch (Exception e) {
						try {
							String label1 = AttributeHelper.getLabel(o1, "");
							String label2 = AttributeHelper.getLabel(o2, "");
							return new Double(label1).compareTo(new Double(label2));
						} catch (Exception err) {
							String label1 = AttributeHelper.getLabel(o1, "");
							String label2 = AttributeHelper.getLabel(o2, "");
							return label1.compareTo(label2);
						}
					}
				}
				
			});
			
			return result;
		} else {
			return getSelectedOrAllNodes(filterNonVisible);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
}
