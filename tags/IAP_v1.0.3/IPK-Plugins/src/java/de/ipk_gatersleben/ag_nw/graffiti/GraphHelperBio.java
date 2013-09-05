/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.xml.rpc.ServiceException;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.HelperClass;
import org.PositionGridGenerator;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute.URLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.MergeNodes;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggPathwayEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.OrganismEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.RelationType;

/**
 * @author Christian Klukas
 */
public class GraphHelperBio implements HelperClass {
	
	public static String getKeggType(GraphElement ge, String resultIfNotAvailable) {
		return (String) AttributeHelper.getAttributeValue(ge, "kegg", "kegg_type", resultIfNotAvailable, "", false);
	}
	
	public static void setKeggType(GraphElement ge, String keggType) {
		AttributeHelper.setAttribute(ge, "kegg", "kegg_type", keggType);
	}
	
	public static String getKeggName(GraphElement ge, String resultIfNotAvailable) {
		return (String) AttributeHelper.getAttributeValue(ge, "kegg", "kegg_name", resultIfNotAvailable, "", false);
	}
	
	public static boolean isMapLink(Edge e) {
		String keggType = getKeggType(e, null);
		if (keggType != null && keggType.equalsIgnoreCase("maplink"))
			return true;
		else
			return false;
	}
	
	public static ArrayList<Edge> getMapLinkEdges(Graph g) {
		ArrayList<Edge> result = new ArrayList<Edge>();
		for (Edge e : g.getEdges()) {
			if (isMapLink(e))
				result.add(e);
		}
		return result;
	}
	
	public static ArrayList<Node> getEnzymeNodes(Graph g) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node n : g.getNodes()) {
			String keggType = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", null, "", false);
			if (keggType != null && keggType.equalsIgnoreCase("enzyme"))
				result.add(n);
		}
		return result;
	}
	
	public static ArrayList<Node> getMapLinkNodes(Graph g) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node n : g.getNodes()) {
			String keggType = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", null, "", false);
			if (keggType != null && keggType.equalsIgnoreCase("map"))
				result.add(n);
		}
		return result;
	}
	
	public static Edge addEdgeIfNotExistant(
			Graph graph,
			Node nodeA, Node nodeB,
			boolean directed,
			CollectionAttribute graphicsAttributeForEdge) {
		// if (directed)
		// if (nodeA.getOutNeighbors().contains(nodeB)) {
		// for (Edge e : nodeA.getAllOutEdges()) {
		// if (e.getTarget()==nodeB)
		// return e;
		// }
		// }
		// if (!directed)
		// if (nodeA.getUndirectedNeighbors().contains(nodeB)) {
		// for (Edge e : nodeA.getUndirectedEdges()) {
		// if (e.getTarget()==nodeB || e.getSource()==nodeB)
		// return e;
		// }
		// }
		if (directed) {
			for (Edge e : nodeA.getEdges()) {
				if (e.getSource() == nodeA && e.getTarget() == nodeB && e.isDirected())
					return e;
			}
		} else {
			for (Edge e : nodeA.getUndirectedEdges()) {
				if (e.getSource() == nodeA && e.getTarget() == nodeB) {
					return e;
				}
				if (e.getTarget() == nodeA && e.getSource() == nodeB) {
					return e;
				}
			}
		}
		return graph.addEdge(nodeA, nodeB, directed, graphicsAttributeForEdge);
	}
	
	public static Edge addEdgeCopyIfNotExistant(Edge refEdge, Node a, Node b) {
		Graph g = refEdge.getGraph();
		return g.addEdgeCopy(refEdge, a, b);
	}
	
	public static Node addMapNode(Graph superGraph, KeggPathwayEntry kpe) {
		Node newNode = superGraph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(100, 100));
		String pathway = kpe.getPathwayName();
		if (pathway != null && pathway.indexOf(" - ") > 0) {
			pathway = pathway.substring(0, pathway.indexOf(" - "));
		}
		AttributeHelper.setLabel(newNode, pathway);
		// AttributeHelper.setFillColor(newNode, Color.WHITE);
		if (pathway.startsWith("TITLE:") || pathway.startsWith("<html>TITLE:"))
			AttributeHelper.setBorderWidth(newNode, 3d);
		
		AttributeHelper.setAttribute(newNode, "kegg", "kegg_type", "map");
		URLAttribute ua = new URLAttribute("kegg_map_link", "path:" + kpe.getMapName());
		URLAttribute ua2 = new URLAttribute("kegg_link", kpe.getWebURL().toString());
		newNode.addAttribute(ua, "kegg");
		newNode.addAttribute(ua2, "kegg");
		
		return newNode;
	}
	
	/**
	 * @param kegg
	 * @param org
	 * @param returnSuperPathway
	 *           If set to true, only one pathway, which represents all KEGG Pathways is returned,
	 *           otherwise, all pathway graphs are returned.
	 * @param statusProvider
	 * @return Kegg pathway(s)
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static Collection<Graph> getKeggPathways(KeggHelper kegg, OrganismEntry org,
			boolean returnSuperPathway,
			boolean returnFullSuperPathway,
			boolean loadFullGraphsForSuperPathwayOverview_falls_means_use_Soap,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider) throws Exception {
		List<Graph> result = new ArrayList<Graph>();
		Collection<KeggPathwayEntry> pathways = kegg.getXMLpathways(org, false, statusProvider);
		if (loadFullGraphsForSuperPathwayOverview_falls_means_use_Soap)
			processFullMapLoading(returnSuperPathway, returnFullSuperPathway, result, pathways, statusProvider);
		else
			processSOAPoverviewLoading(result, pathways, statusProvider, org);
		return result;
	}
	
	private static void processSOAPoverviewLoading(List<Graph> result,
			Collection<KeggPathwayEntry> pathways,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider,
			OrganismEntry org) {
		Graph superGraph = new AdjListGraph();
		HashMap<String, Node> mapNumber2superGraphNode = new HashMap<String, Node>();
		PositionGridGenerator pgg = new PositionGridGenerator(200, 50, 1000);
		Queue<String> checkTheseMaps = new LinkedList<String>();
		statusProvider.setCurrentStatusText2("Create Map Nodes...");
		statusProvider.setCurrentStatusValueFine(0);
		for (KeggPathwayEntry kpe : pathways) {
			checkTheseMaps.add(kpe.getMapName());
			Node mapNode = GraphHelperBio.addMapNode(superGraph, kpe);
			pretifyMapNode(pgg, mapNumber2superGraphNode, kpe, mapNode);
		}
		statusProvider.setCurrentStatusText1("Using SOAP Interface to");
		statusProvider.setCurrentStatusText2("connect Map Nodes...");
		statusProvider.setCurrentStatusValueFine(0);
		int workLoad = checkTheseMaps.size();
		int i = 0;
		while (checkTheseMaps.size() > 0) {
			if (statusProvider.wantsToStop())
				break;
			String checkThisMap = checkTheseMaps.poll();
			Node sourceNode = mapNumber2superGraphNode.get(checkThisMap);
			if (sourceNode == null) {
				ErrorMsg.addErrorMessage("Unknown source map, no node for map ID " + checkThisMap);
				continue;
			}
			KeggHelper kegg = new KeggHelper();
			try {
				Collection<String> links = kegg.getLinkedPathwayIDs(checkThisMap);
				for (String l : links) {
					Node targetNode = mapNumber2superGraphNode.get(l);
					if (targetNode == null) {
						org.getShortName();
						String mapNumber = l.replaceFirst("path:", "");
						KeggPathwayEntry kpe =
								new KeggPathwayEntry(
										l,
										false,
										mapNumber,
										KeggHelper.getGroupFromMapNumber(mapNumber, "")
								// KeggHelper.getGroupFromMapName("--")
								);
						checkTheseMaps.add(kpe.getMapName());
						Node mapNode = GraphHelperBio.addMapNode(superGraph, kpe);
						pretifyMapNode(pgg, mapNumber2superGraphNode, kpe, mapNode);
						targetNode = mapNode;
					}
					if (targetNode == null)
						ErrorMsg.addErrorMessage("Could not connect map node for map id " + checkThisMap + " to unknown map with ID " + l);
					else {
						if (!sourceNode.getOutNeighbors().contains(targetNode)) {
							Edge ne = superGraph.addEdge(sourceNode, targetNode, false, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK,
									false));
							AttributeHelper.setDashInfo(ne, 5, 5);
							// AttributeHelper.setBorderWidth(ne, 5);
							// AttributeHelper.setArrowSize(ne, 6);
							String id1 = KeggGmlHelper.getKeggId(sourceNode);
							String id2 = KeggGmlHelper.getKeggId(targetNode);
							KeggGmlHelper.setRelationSourceTargetInformation(ne, 0, id1, id2);
							KeggGmlHelper.setRelationSubtypeName(ne, 0, "");
							KeggGmlHelper.setRelationTypeInformation(ne, 0, RelationType.maplink);
						}
					}
				}
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
				ErrorMsg.addErrorMessage("Could not retrieve linked maps for map ID " + checkThisMap);
			} catch (ServiceException e) {
				ErrorMsg.addErrorMessage(e);
				ErrorMsg.addErrorMessage("Could not retrieve linked maps for map ID " + checkThisMap);
			}
			statusProvider.setCurrentStatusValueFine(100d * (i++) / workLoad);
		}
		result.clear();
		result.add(superGraph);
		if (!statusProvider.wantsToStop())
			statusProvider.setCurrentStatusText1("Processing completed");
		else
			statusProvider.setCurrentStatusText1("Processing aborted");
		statusProvider.setCurrentStatusText2("Map Overview: " + superGraph.getNodes().size() + " Nodes, " + superGraph.getEdges().size() + " Edges");
		statusProvider.setCurrentStatusValueFine(100d);
	}
	
	private static void pretifyMapNode(PositionGridGenerator pgg, HashMap<String, Node> mapNumber2superGraphNode, KeggPathwayEntry kpe, Node mapNode) {
		mapNumber2superGraphNode.put(kpe.getMapName(), mapNode);
		String label = AttributeHelper.getLabel(mapNode, "");
		JLabel testLabel = new JLabel(label);
		Dimension d = testLabel.getPreferredSize();
		AttributeHelper.setSize(mapNode, 10d + d.getWidth(), 10d + d.getHeight());
		AttributeHelper.setPosition(mapNode, pgg.getNextPosition());
		NodeHelper nh = new NodeHelper(mapNode);
		nh.setClusterID("path:" + kpe.getMapName());
		KeggGmlHelper.setKeggId(mapNode, "path:" + kpe.getMapName());
	}
	
	private static void processFullMapLoading(boolean returnSuperPathway, boolean returnFullSuperPathway, List<Graph> result,
			Collection<KeggPathwayEntry> pathways,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider) {
		Graph superGraph = new AdjListGraph();
		HashMap<String, Node> mapNumber2superGraphNode = new HashMap<String, Node>();
		statusProvider.setCurrentStatusText1("Process Pathways...");
		statusProvider.setCurrentStatusText2("");
		statusProvider.setCurrentStatusValueFine(0d);
		superGraph.setName("KEGG Pathway Overview");
		int i = 0;
		int iOK = 0;
		int workLoad = pathways.size();
		double maxX = 0;
		double maxY = 0;
		double thisRowMaxY = 0;
		for (KeggPathwayEntry kpe : pathways) {
			if (statusProvider.wantsToStop())
				break;
			Graph keggPathwayGraph = new AdjListGraph();
			statusProvider.setCurrentStatusText2("Load map " + kpe.getMapName() + " (" + kpe.getPathwayName() + ")");
			KeggService.loadKeggPathwayIntoEditor(kpe, keggPathwayGraph, KeggService.getDefaultEnzymeColor(), i == pathways.size() - 1, false);
			double thisMaxX = 0;
			Vector2d maxXYxy = NodeTools.getMaximumXY(keggPathwayGraph.getNodes(), 1d, 0, 0, true);
			thisMaxX = maxXYxy.x;
			if (maxXYxy.y > thisRowMaxY) {
				thisRowMaxY = maxXYxy.y;
			}
			int columns = 10;
			maxX += thisMaxX + 50;
			if (iOK % columns == 0 && iOK > 0) {
				maxY += thisRowMaxY;
				maxY += 50;
				thisRowMaxY = 0;
				maxX = 0;
			}
			for (Node n : keggPathwayGraph.getNodes()) {
				Vector2d np = AttributeHelper.getPositionVec2d(n);
				np.x = np.x + maxX;
				np.y = np.y + maxY;
				AttributeHelper.setPosition(n, np);
			}
			if (keggPathwayGraph != null && keggPathwayGraph.getNumberOfNodes() > 0)
				iOK += 1;
			if (returnFullSuperPathway) {
				// superGraph.addGraph(keggPathwayGraph);
				KeggService.mergeKeggGraphs(superGraph, keggPathwayGraph, null, true, false);
			}
			
			if (returnSuperPathway) {
				if (!mapNumber2superGraphNode.containsKey(kpe.getMapName())) {
					Node mapNode = GraphHelperBio.addMapNode(superGraph, kpe);
					Vector2d minXY = NodeTools.getMinimumXY(keggPathwayGraph.getNodes(), 1d, 0, 0, true);
					Vector2d maxXY = NodeTools.getMaximumXY(keggPathwayGraph.getNodes(), 1d, 0, 0, true);
					double width = maxXY.x - minXY.x;
					double height = maxXY.y - minXY.y;
					AttributeHelper.setSize(mapNode, width, height);
					mapNumber2superGraphNode.put(kpe.getMapName(), mapNode);
				}
				Node thisPathwayNode = mapNumber2superGraphNode.get(kpe.getMapName());
				Collection<Node> mapNodes = GraphHelperBio.getMapLinkNodes(keggPathwayGraph);
				for (Node referencedMapNode : mapNodes) {
					String referencedMap = (String) AttributeHelper.getAttributeValue(referencedMapNode, "kegg", "kegg_map_link", null, "", false);
					if (referencedMap != null && referencedMap.length() > 0) {
						referencedMap = StringManipulationTools.stringReplace(referencedMap, "path:", "");
						if (mapNumber2superGraphNode.containsKey(referencedMap)) {
							Node superPathwayMapNode = mapNumber2superGraphNode.get(referencedMap);
							if (!thisPathwayNode.getNeighbors().contains(superPathwayMapNode) && (!kpe.getMapName().equalsIgnoreCase(referencedMap))) {
								Edge edge = superGraph.addEdge(thisPathwayNode, superPathwayMapNode, false, AttributeHelper.getDefaultGraphicsAttributeForEdge(
										Color.BLACK, Color.BLACK, false));
								AttributeHelper.setDashInfo(edge, 5f, 5f);
								// System.out.println("Connect Map Nodes: "+kpe.getMapName()+" <==> "+referencedMap);
							}
						}
					}
				}
			}
			if (!returnSuperPathway && !returnFullSuperPathway)
				result.add(keggPathwayGraph);
			statusProvider.setCurrentStatusValueFine(100d * (i++) / workLoad);
		}
		if (returnFullSuperPathway) {
			// Collection<Node> mapNodes = GraphHelperBio.getMapLinkNodes(superGraph);
			// superGraph.deleteAll((Collection)mapNodes);
			statusProvider.setCurrentStatusText1("Super Pathway has been created");
			// statusProvider.setCurrentStatusText1("Separate layout, please wait...");
			// statusProvider.setCurrentStatusValue(-1);
			// if (!statusProvider.wantsToStop()) {
			// Algorithm layout = new NoOverlappOfClustersAlgorithm();
			// layout.attach(superGraph, new Selection("empty"));
			// try {
			// layout.check();
			// layout.execute();
			// } catch (PreconditionException e) {
			// ErrorMsg.addErrorMessage(e);
			// }
			// }
			statusProvider.setCurrentStatusValue(100);
		}
		if (returnSuperPathway || returnFullSuperPathway) {
			result.clear();
			superGraph.numberGraphElements();
			result.add(superGraph);
		}
		if (!statusProvider.wantsToStop())
			statusProvider.setCurrentStatusText1("Processing completed");
		else
			statusProvider.setCurrentStatusText1("Processing aborted");
		statusProvider.setCurrentStatusText2("Map Overview: " + superGraph.getNodes().size() + " Nodes, " + superGraph.getEdges().size() + " Edges");
		statusProvider.setCurrentStatusValueFine(100d);
	}
	
	public static void mergeNodesWithSameLabel(
			List<Node> nodes,
			final boolean selectOnlyTrueOrMergeIsFalse,
			boolean extendSelection,
			boolean considerCluster) {
		mergeNodesWithSameLabel(nodes, selectOnlyTrueOrMergeIsFalse, extendSelection, considerCluster, false, true);
	}
	
	@SuppressWarnings("unchecked")
	public static void mergeNodesWithSameLabel(
			List<Node> nodes,
			final boolean selectOnlyTrueOrMergeIsFalse,
			boolean extendSelection,
			boolean considerCluster,
			boolean considerPositionPlusMinus10,
			boolean retainClusterIDs) {
		
		if (selectOnlyTrueOrMergeIsFalse && extendSelection) {
			selectNodesWithSameLabelBasedOnCurrentSelection(nodes);
			return;
		}
		HashMap<String, ArrayList<Node>> label2nodeList = new HashMap<String, ArrayList<Node>>();
		Graph g2 = null;
		int firstn = nodes.size();
		for (Node n : nodes) {
			if (g2 == null)
				g2 = n.getGraph();
			String name = AttributeHelper.getLabel(n, null);
			if (name == null || name.length() <= 0)
				continue;
			if (considerCluster) {
				String cluster = NodeTools.getClusterID(n, "");
				name = cluster + "§" + name;
			}
			if (considerPositionPlusMinus10) {
				Vector2d pos = AttributeHelper.getPositionVec2d(n);
				name = ((int) Math.round(pos.x / 10d)) + "/" + ((int) Math.round(pos.y / 10d)) + "§" + name;
			}
			if (!label2nodeList.containsKey(name))
				label2nodeList.put(name, new ArrayList<Node>());
			ArrayList<Node> nl = label2nodeList.get(name);
			nl.add(n);
		}
		final Graph g = g2;
		if (g != null) {
			try {
				if (!selectOnlyTrueOrMergeIsFalse)
					g.getListenerManager().transactionStarted(g);
				int workSize = label2nodeList.values().size();
				int workI = 0;
				int numberN = 0;
				int lastn = nodes.size();
				HashSet<Node> selNodes = new HashSet<Node>();
				for (ArrayList<Node> toBeCondensed : label2nodeList.values()) {
					MainFrame.showMessage("Condense Nodes (" + (int) (workI * 100d / workSize) + "%)", MessageType.PERMANENT_INFO);
					if (toBeCondensed.size() > 1) {
						if (selectOnlyTrueOrMergeIsFalse) {
							numberN++;
							selNodes.addAll(toBeCondensed);
						} else {
							Node newNode = MergeNodes.mergeNode(g, toBeCondensed, NodeTools.getCenter(toBeCondensed), retainClusterIDs);
							Set<Object> srcFileNamesSet = AttributeHelper.getAttributeValueSet((Collection) toBeCondensed, "src", "fileName", "-", "", false);
							String srcFileNames = AttributeHelper.getStringList(srcFileNamesSet.toArray(new String[] {}), ";");
							if (srcFileNames != null && !srcFileNames.equals("-"))
								AttributeHelper.setAttribute(newNode, "src", "fileName", srcFileNames);
							g.deleteAll((Collection) toBeCondensed);
							lastn = lastn - toBeCondensed.size() + 1;
						}
					}
					workI++;
				}
				if (selectOnlyTrueOrMergeIsFalse) {
					Selection sel = new Selection("");
					for (Node n : selNodes) {
						sel.add(n);
					}
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
					
					MainFrame.showMessage("Number of multiple occurring nodes: " + numberN, MessageType.INFO);
				} else
					MainFrame.showMessage("Condensed Nodes: " + firstn + " --> " + lastn, MessageType.INFO);
			} finally {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!selectOnlyTrueOrMergeIsFalse)
							g.getListenerManager().transactionFinished(g);
					}
				});
			}
		}
	}
	
	private static void selectNodesWithSameLabelBasedOnCurrentSelection(List<Node> selectedNodes) {
		Selection sel = new Selection("multipleOccuringNodes");
		HashSet<String> labelsToTest = new HashSet<String>();
		Graph g = null;
		for (Node n : selectedNodes) {
			if (g == null && n.getGraph() != null)
				g = n.getGraph();
			String label = AttributeHelper.getLabel(n, null);
			if (label != null) {
				labelsToTest.add(label);
			}
		}
		HashMap<String, Integer> labelsToTestAndFrequency = new HashMap<String, Integer>();
		HashMap<Node, String> node2label = new HashMap<Node, String>();
		for (Node n : g.getNodes()) {
			String label = AttributeHelper.getLabel(n, null);
			if (label != null) {
				if (labelsToTest.contains(label)) {
					node2label.put(n, label);
					Integer frequency = labelsToTestAndFrequency.get(label);
					if (frequency == null)
						labelsToTestAndFrequency.put(label, 1);
					else
						labelsToTestAndFrequency.put(label, frequency + 1);
				}
			}
		}
		for (Node n : g.getNodes()) {
			String lbl = node2label.get(n);
			if (lbl != null) {
				Integer frequency = labelsToTestAndFrequency.get(lbl);
				if (frequency != null && frequency > 1)
					sel.add(n);
			}
		}
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
	}
	
	public static void connectNodesWithSameLabel(List<Node> nodes) {
		HashMap<String, ArrayList<Node>> keggID2nodeList = new HashMap<String, ArrayList<Node>>();
		Graph g = null;
		for (Node n : nodes) {
			if (g == null)
				g = n.getGraph();
			String name = AttributeHelper.getLabel(n, null);
			if (name == null || name.length() <= 0)
				continue;
			if (!keggID2nodeList.containsKey(name))
				keggID2nodeList.put(name, new ArrayList<Node>());
			ArrayList<Node> nl = keggID2nodeList.get(name);
			nl.add(n);
		}
		if (g != null) {
			try {
				g.getListenerManager().transactionStarted(g);
				int workSize = keggID2nodeList.values().size();
				int workI = 0;
				int addedges = 0;
				ArrayList<Edge> newEdges = new ArrayList<Edge>();
				for (ArrayList<Node> toBeCondensed : keggID2nodeList.values()) {
					MainFrame.showMessage("Connect Nodes (" + (int) (workI * 100d / workSize) + "%)", MessageType.PERMANENT_INFO);
					if (toBeCondensed.size() > 1) {
						for (Node a : toBeCondensed) {
							for (Node b : toBeCondensed) {
								if (a != b) {
									if (!a.getNeighbors().contains(b)) {
										Edge e = addEdgeIfNotExistant(g, a, b, false, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, false));
										if (e != null) {
											newEdges.add(e);
											AttributeHelper.setDashInfo(e, 5, 5);
											setKeggType(e, "same_id_connection");
											addedges++;
										}
									}
								}
							}
						}
					}
					workI++;
				}
				Selection sel = new Selection("");
				for (Edge e : newEdges) {
					sel.add(e);
				}
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
				MainFrame.showMessage("Connected Nodes: " + addedges + " new edges", MessageType.INFO);
			} finally {
				g.getListenerManager().transactionFinished(g);
			}
		}
	}
}
