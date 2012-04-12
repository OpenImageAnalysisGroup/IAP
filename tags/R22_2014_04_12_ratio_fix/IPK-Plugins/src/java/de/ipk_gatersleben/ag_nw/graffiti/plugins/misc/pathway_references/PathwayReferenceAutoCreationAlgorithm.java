/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.10.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.JLabel;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.actions.FileOpenAction;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

public class PathwayReferenceAutoCreationAlgorithm extends AbstractAlgorithm implements Algorithm {
	
	private static boolean onlyCompounds;
	boolean processLoadedFiles = true;
	boolean considerCluster = true;
	boolean multipleClusterTargets = false;
	boolean processOnlyCompounds = false;
	
	boolean linkViz = false;
	private boolean performRecreationOfView = true;
	
	public String getName() {
		return "Analyze open network files and auto-create links...";
	}
	
	@Override
	public String getCategory() {
		return "menu.window";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active graph editor window found!");
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"From the current node selection, nodes with the same node labels are searched<br>" +
							"within all other currenty opened files.<br>" +
							"If a node with the same label is found, a map link node is created, which points<br>" +
							"to the other network. Right-click such map link nodes to navigate to those networks.<br>" +
							"<br>" +
							"If enabled, only nodes with the same cluster ID are considered.<br>" +
							"If cluster IDs are enabled to be considered, either a single map link node may be<br>" +
							"created (depending on the second option), or multiple map link nodes may be<br>" +
							"created (set cluster ID is selected).<br>" +
							"<br>" +
							"If the last option is enabled, no map link nodes are created, instead<br>" +
							"reference attributes are added to relevant graph elements.<br>" +
							"You may refer to these pathway links from the Node side panel and by<br>" +
							"right-clicking nodes in the graph view. New command buttons will be shown<br>" +
							"under the category Links, new menu items will be included in the context menu,<br>" +
							"respectively." +
							"<br><br>";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							// new BooleanParameter(processLoadedFiles, "Process open files", "<html>" +
				// "If enabled, the currently opened files are processed, otherwise<br>" +
				// "a file open dialog is shown and network files on disk are processed."),
				new BooleanParameter(considerCluster, "Consider Source and Target Cluster ID",
												"If enabled, only nodes with the same cluster ID are considered."),
							new BooleanParameter(multipleClusterTargets, "Set Cluster ID", "<html>" +
												"If enabled, the cluster ID of the map link nodes will be set, additionally<br>" +
												"multiple target nodes are created if multiple target clusters exist."),
							new BooleanParameter(linkViz,
												"<html>" +
																	"Add Reference Attributes<br>" +
																	"(Link-Visualization)",
												"<html>" +
																	"If enabled, no map link nodes are created, <br>" +
																	"instead reference attributes are added to relevant graph elements."),
							new BooleanParameter(onlyCompounds,
												"Consider only Compounds",
												"<html>" +
																	"If enabled, only compound nodes (KEGG attribute or label derived) are considered.") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		// processLoadedFiles = ((BooleanParameter)params[i++]).getBoolean().booleanValue();
		considerCluster = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		multipleClusterTargets = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		linkViz = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		onlyCompounds = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
	}
	
	public synchronized void execute() {
		EditorSession thisSession = null;
		for (EditorSession s : MainFrame.getEditorSessions()) {
			if (s.getGraph() == graph) {
				thisSession = s;
				break;
			}
		}
		if (performErrorCheck(thisSession))
			return;
		
		TreeMap<SessionLinkInfo, HashSet<Node>> linkSessions = new TreeMap<SessionLinkInfo, HashSet<Node>>();
		HashMap<SessionLinkInfo, HashSet<String>> linkSessionsAndClusters = new HashMap<SessionLinkInfo, HashSet<String>>();
		HashSet<SessionLinkInfo> invalidUnsavedSessions = new HashSet<SessionLinkInfo>();
		
		if (processLoadedFiles) {
			ArrayList<SessionLinkInfo> sessions = new ArrayList<SessionLinkInfo>();
			for (EditorSession s : MainFrame.getEditorSessions()) {
				sessions.add(new SessionLinkInfo(s));
			}
			processLoadedFiles(graph, considerCluster, linkSessions, linkSessionsAndClusters,
								invalidUnsavedSessions, sessions, getSelectedOrAllNodes());
		} else {
			MainFrame.showMessageDialog("<html>" +
								"This mode of operation is not yet implemented.<br>" +
								"Please process open files instead.", "Internal Error");
			final Collection<File> fileList = FileOpenAction.getGraphFilesFromUser();
			if (fileList == null) {
				return;
			} else {
				for (@SuppressWarnings("unused")
				File file : fileList) {
					try {
						// Graph g = MainFrame.getInstance().getGraph(file);
						
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			}
		}
		
		String workSessionFilePath = thisSession.getWorkSessionFilePath();
		
		createLinks(linkViz, performRecreationOfView, graph, multipleClusterTargets, considerCluster,
							linkSessions, linkSessionsAndClusters,
							invalidUnsavedSessions, workSessionFilePath);
	}
	
	// public void connectSessionsToSessions(Collection<EditorSession> sessions, String workSessionFilePath) {
	//
	// TreeMap<SessionLinkInfo, HashSet<Node>> linkSessions = new TreeMap<SessionLinkInfo, HashSet<Node>>();
	// HashMap<SessionLinkInfo, HashSet<String>> linkSessionsAndClusters = new HashMap<SessionLinkInfo, HashSet<String>>();
	// HashSet<SessionLinkInfo> invalidUnsavedSessions = new HashSet<SessionLinkInfo>();
	//
	// Collection<SessionLinkInfo> workSessions = new ArrayList<SessionLinkInfo>();
	// for (EditorSession s : sessions)
	// workSessions.add(new SessionLinkInfo(s));
	//
	// processLoadedFiles(linkSessions, linkSessionsAndClusters, invalidUnsavedSessions, workSessions);
	//
	// for (Session s: ....)
	// createLinks(true, false, linkSessions, linkSessionsAndClusters, invalidUnsavedSessions, workSessionFilePath);
	// }
	
	public void connectGraphToGraphs(Collection<Graph> graphs, String workSessionFilePath, boolean linkviz) {
		
		HashSet<SessionLinkInfo> invalidUnsavedSessions = new HashSet<SessionLinkInfo>();
		
		Collection<SessionLinkInfo> workSessions = new ArrayList<SessionLinkInfo>();
		for (Graph g : graphs) {
			try {
				workSessions.add(new SessionLinkInfo(g));
			} catch (URISyntaxException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		for (Graph g : graphs) {
			TreeMap<SessionLinkInfo, HashSet<Node>> linkSessions = new TreeMap<SessionLinkInfo, HashSet<Node>>();
			HashMap<SessionLinkInfo, HashSet<String>> linkSessionsAndClusters = new HashMap<SessionLinkInfo, HashSet<String>>();
			processLoadedFiles(g, false, linkSessions, linkSessionsAndClusters, invalidUnsavedSessions, workSessions, g.getNodes());
			createLinks(linkviz, false, g, false, false, linkSessions, linkSessionsAndClusters, invalidUnsavedSessions, workSessionFilePath);
		}
	}
	
	private static void createLinks(
						boolean linkViz, boolean performRecreationOfView,
						Graph graph, boolean multipleClusterTargets, boolean considerClusters,
						TreeMap<SessionLinkInfo, HashSet<Node>> linkSessions,
						HashMap<SessionLinkInfo, HashSet<String>> linkSessionsAndClusters,
						HashSet<SessionLinkInfo> invalidUnsavedSessions, String workSessionFilePath) {
		if (linkViz) {
			// create overall list of links
			// process nodes with at least on link
			// add link info
			HashSet<Node> nodes = new HashSet<Node>();
			for (HashSet<Node> hn : linkSessions.values())
				nodes.addAll(hn);
			AttributeHelper.removePathwayReferences(graph, false);
			for (Node n : nodes)
				AttributeHelper.removePathwayReferences(n, false);
			for (Node n : graph.getNodes())
				AttributeHelper.deleteAttribute(n, "", "pathway_link_visualization");
			int idx = 0;
			HashMap<Node, Integer> node2currentPathwayReferenceIndex = new HashMap<Node, Integer>();
			for (SessionLinkInfo sli : linkSessions.keySet()) {
				idx++;
				String targetLink = getTargetLink(workSessionFilePath, sli);
				AttributeHelper.setPathwayReference(graph, idx, targetLink);
				HashSet<Node> affectedNodes = linkSessions.get(sli);
				for (Node n : affectedNodes) {
					if (!node2currentPathwayReferenceIndex.containsKey(n)) {
						node2currentPathwayReferenceIndex.put(n, new Integer(0));
						AttributeHelper.setAttribute(n, "", "pathway_link_visualization", "mode1");
					}
					int currentIndex = node2currentPathwayReferenceIndex.get(n) + 1;
					node2currentPathwayReferenceIndex.put(n, currentIndex);
					AttributeHelper.setPathwayReference(n, currentIndex, targetLink);
				}
			}
			if (performRecreationOfView)
				GraphHelper.issueCompleteRedrawForGraph(graph);
		} else {
			// ask for parameters:
			// Process File Group Information (y/n)
			// Get File Group Information from FileName (group.pathwayName)
			// Get File Group Information from KEGG (for KEGG Pathways)
			// Indicate Group Information:
			// ignore / add in parenthesis / don't include in label (remove)
			
			createMapLinkNodes(
								graph, multipleClusterTargets, considerClusters,
								workSessionFilePath, linkSessions,
								linkSessionsAndClusters);
		}
		if (invalidUnsavedSessions.size() > 0) {
			showUnsavedGraphsWarningMessage(invalidUnsavedSessions);
		}
	}
	
	private boolean performErrorCheck(EditorSession thisSession) {
		if (thisSession == null) {
			MainFrame.showMessageDialog("Internal error: Work-Graph-Session could not be found!", "Error");
			return true;
		}
		if (processLoadedFiles && MainFrame.getSessions().size() < 2) {
			MainFrame.showMessageDialog("This command mode requires at least two opened files for cross-link creation!", "Error");
			return true;
		}
		if (thisSession.getGraph().isModified()) {
			MainFrame.showMessageDialog(
								"<html>" +
													"The current graph needs to be saved to disk before<br>" +
													"network links may be established.",
								"Information");
			return true;
		}
		return false;
	}
	
	private static void showUnsavedGraphsWarningMessage(
						HashSet<SessionLinkInfo> invalidUnsavedSessions) {
		StringBuilder sessions = new StringBuilder();
		for (SessionLinkInfo s : invalidUnsavedSessions) {
			sessions.append("<li>Graph " + s.getGraph().getName(false));
		}
		MainFrame.showMessageDialog(
							"<html>" +
												"Links to the following network files could not be created as they are<br>" +
												"not saved to disk:<ul>" + sessions.toString() + "</ul>",
							"Invalid link targets found");
	}
	
	private static void createMapLinkNodes(
						Graph graph,
						boolean multipleClusterTargets,
						boolean considerCluster,
						String workSessionFilePath,
						TreeMap<SessionLinkInfo, HashSet<Node>> linkSessions,
						HashMap<SessionLinkInfo, HashSet<String>> linkSessionsAndClusters) {
		int linkNodeWidth = 70;
		int offX = linkNodeWidth + 20;
		int offY = 10;
		int i = 0;
		Vector2d topLeft = NodeTools.getMinimumXY(graph.getNodes(), 1d, 0, 0, true);
		Vector2d bottomRight = NodeTools.getMaximumXY(graph.getNodes(), 1d, 0, 0, true);
		int startX = (int) (bottomRight.x + offX);
		int startY = (int) (topLeft.y);
		double targetY = startY;
		String specialAllClusterID = "§§§§§§§§";
		try {
			graph.getListenerManager().transactionStarted(graph);
			for (SessionLinkInfo ls : linkSessions.keySet()) {
				HashSet<String> clusters = linkSessionsAndClusters.get(ls);
				if (!multipleClusterTargets && considerCluster) {
					clusters.clear();
					clusters.add(specialAllClusterID);
				}
				for (String validCluster : clusters) {
					try {
						SessionLinkInfo es = ls;
						String targetLink = getTargetLink(workSessionFilePath, es);
						double targetX = startX + offX;
						targetY = addMapLinkNodeInternal(graph, considerCluster, multipleClusterTargets,
											linkSessions, linkNodeWidth, offY, targetY, ls, validCluster, targetLink, targetX).targetY;
						i++;
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			}
		} finally {
			graph.getListenerManager().transactionFinished(graph);
		}
	}
	
	private static TargetYandNewNodeResult addMapLinkNodeInternal(
						Graph graph,
						boolean considerCluster, boolean multipleClusterTargets,
						TreeMap<SessionLinkInfo, HashSet<Node>> linkSessions, int linkNodeWidth, int offY, double targetY, SessionLinkInfo ls, String validCluster,
						String targetLink, double targetX) {
		Node linkNode = graph.addNode(
							AttributeHelper.getDefaultGraphicsAttributeForNode(targetX, targetY));
		int linkNodeHeight =
							pretifyLinkNode(ls.getFileName(), linkNode, linkNodeWidth,
												GroupLinkProcessingMode.ADD_PARENTHESIS);
		targetY += offY;
		targetY += linkNodeHeight;
		AttributeHelper.setPathwayReference(linkNode, targetLink);
		if (considerCluster && multipleClusterTargets)
			new NodeHelper(linkNode).setClusterID(validCluster);
		for (Node n : linkSessions.get(ls)) {
			if (considerCluster && multipleClusterTargets) {
				String cc = new NodeHelper(n).getClusterID("");
				if (!cc.equals(validCluster))
					continue;
			}
			Edge edge = graph.addEdge(n, linkNode, false,
								AttributeHelper.getDefaultGraphicsAttributeForEdge(
													Color.GRAY, Color.GRAY, false));
			AttributeHelper.setDashInfo(edge, 5, 5);
		}
		return new TargetYandNewNodeResult(targetY, linkNode);
	}
	
	private static String getTargetLink(String workSessionFilePath, SessionLinkInfo es) {
		return es.graph.getName(false);
	}
	
	private static void processLoadedFiles(
						Graph g, boolean considerCluster, TreeMap<SessionLinkInfo, HashSet<Node>> linkSessions,
						HashMap<SessionLinkInfo, HashSet<String>> linkSessionsAndClusters,
						HashSet<SessionLinkInfo> invalidUnsavedSessions,
						Collection<SessionLinkInfo> allSessions, Collection<Node> workingset) {
		// System.out.println("Graph "+graph.getName(true)+" ("+graph.getNumberOfNodes()+" nodes)");
		for (SessionLinkInfo infoAboutOtherSession : allSessions) {
			
			if (infoAboutOtherSession.getGraph() == g)
				continue;
			
			for (Node n : workingset) {
				String lbl = AttributeHelper.getLabel(n, null);
				if (lbl == null || lbl.length() == 0)
					continue;
				String tn = KeggGmlHelper.getKeggType(n);
				if (tn != null && tn.equals("map"))
					continue;
				if (onlyCompounds) {
					if (tn == null || !tn.equals("compound"))
						continue;
					else {
						if (CompoundService.getInformation(lbl) == null)
							continue;
					}
				}
				String cluster = "";
				if (considerCluster) {
					cluster = new NodeHelper(n).getClusterID("");
					lbl = cluster + "§" + lbl;
				}
				boolean link = false;
				for (Node otherNode : infoAboutOtherSession.getGraph().getNodes()) {
					String otherLbl = AttributeHelper.getLabel(otherNode, null);
					if (otherLbl == null || otherLbl.length() == 0)
						continue;
					String to = KeggGmlHelper.getKeggType(otherNode);
					if (to != null && to.equals("map"))
						continue;
					if (onlyCompounds) {
						if (to == null || !to.equals("compound"))
							continue;
						else {
							if (CompoundService.getInformation(otherLbl) == null)
								continue;
						}
					}
					if (considerCluster) {
						String otherCluster = new NodeHelper(otherNode).getClusterID("");
						otherLbl = otherCluster + "§" + otherLbl;
					}
					if (lbl.equals(otherLbl)) {
						link = true;
						break;
					}
				}
				if (link) {
					SessionLinkInfo sli = infoAboutOtherSession;
					if (!linkSessions.containsKey(sli))
						linkSessions.put(sli, new HashSet<Node>());
					linkSessions.get(sli).add(n);
					
					if (!linkSessionsAndClusters.containsKey(sli))
						linkSessionsAndClusters.put(sli, new HashSet<String>());
					linkSessionsAndClusters.get(sli).add(cluster);
				}
			}
		}
	}
	
	private static int pretifyLinkNode(
						String title, Node linkNode, int linkNodeWidth,
						GroupLinkProcessingMode glpm) {
		if (title.indexOf(".") > 0)
			title = title.substring(0, title.lastIndexOf("."));
		
		if (glpm == GroupLinkProcessingMode.ADD_PARENTHESIS) {
			if (title.indexOf(".") > 0) {
				String group = title.substring(0, title.indexOf("."));
				title = title.substring(title.indexOf(".") + ".".length());
				title = "<html>" + title + "<br>(" + group + ")";
			}
		}
		if (glpm == GroupLinkProcessingMode.REMOVE_GROUP_INFO) {
			if (title.indexOf(".") > 0) {
				title = title.substring(title.lastIndexOf(".") + ".".length());
			}
		}
		
		title = title.replaceAll("%20", " ");
		AttributeHelper.setLabel(linkNode, title);
		AttributeHelper.setRoundedEdges(linkNode, 25);
		AttributeHelper.setBorderWidth(linkNode, 1);
		JLabel jll = new JLabel(title);
		int w = jll.getPreferredSize().width + 20;
		w = w - w % 20 + 20;
		int linkNodeHeight = jll.getPreferredSize().height + 10;
		AttributeHelper.setPosition(linkNode,
							AttributeHelper.getPositionX(linkNode) + (w - linkNodeWidth) / 2, AttributeHelper.getPositionY(linkNode));
		AttributeHelper.setSize(linkNode, w, linkNodeHeight);
		
		// AttributeHelper.setDashInfo(linkNode, 5, 5);
		// AttributeHelper.setFillColor(linkNode, new Color(225,225,255));
		// AttributeHelper.setLabelColor(linkNode, Color.DARK_GRAY);
		
		return linkNodeHeight;
	}
	
	public static Node addMapLinkNode(String fileName, Graph graph,
						Node initNode, ActionEvent ae,
						boolean searchExistingMapLinkNode,
						boolean searchAndLinkSimilarNodes,
						boolean searchAndLinkSameTarget) {
		HashSet<Node> sourceNodes = new HashSet<Node>();
		sourceNodes.add(initNode);
		String lbl = AttributeHelper.getLabel(initNode, "");
		if (searchAndLinkSimilarNodes && lbl.length() > 0) {
			for (Node n : graph.getNodes()) {
				String nlbl = AttributeHelper.getLabel(n, "");
				if (nlbl.length() <= 0)
					continue;
				if (!nlbl.equals(lbl))
					continue;
				Collection<String> refs = AttributeHelper.getPathwayReferences(n, true);
				for (String r : refs)
					if (r.equals("filepath|" + fileName))
						sourceNodes.add(n);
			}
		}
		if (searchAndLinkSameTarget) {
			for (Node n : graph.getNodes()) {
				Collection<String> refs = AttributeHelper.getPathwayReferences(n, true);
				for (String r : refs)
					if (r.equals("filepath|" + fileName))
						sourceNodes.add(n);
			}
		}
		Vector2d pos = AttributeHelper.getPositionVec2d(initNode);
		double targetX = pos.x + 100;
		double targetY = pos.y;
		Node linkNode = null;
		if (searchExistingMapLinkNode) {
			for (Node n : graph.getNodes()) {
				String ref = AttributeHelper.getPathwayReference(n);
				if (ref == null)
					continue;
				if (ref.equals(fileName)) {
					linkNode = n;
					break;
				}
			}
		}
		if (linkNode == null) {
			linkNode = graph.addNode(
								AttributeHelper.getDefaultGraphicsAttributeForNode(targetX, targetY));
			int linkNodeWidth = 70;
			pretifyLinkNode(fileName, linkNode, linkNodeWidth, GroupLinkProcessingMode.ADD_PARENTHESIS);
			
			AttributeHelper.setPathwayReference(linkNode, fileName);
		}
		for (Node n : sourceNodes) {
			if (n.getNeighbors().contains(linkNode))
				continue;
			Edge edge = graph.addEdge(n, linkNode, false,
								AttributeHelper.getDefaultGraphicsAttributeForEdge(
													Color.GRAY, Color.GRAY, false));
			AttributeHelper.setDashInfo(edge, 5, 5);
		}
		return linkNode;
	}
	
	public void setPerformRecreationOfView(boolean performRecreationOfView) {
		this.performRecreationOfView = performRecreationOfView;
	}
}
