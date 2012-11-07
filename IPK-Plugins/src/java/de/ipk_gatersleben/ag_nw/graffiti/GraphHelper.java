/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import javax.swing.SwingUtilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.HelperClass;
import org.PositionGridGenerator;
import org.Release;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.Vector2d;
import org.Vector3d;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.EdgeShapeAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.util.MultipleIterator;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;

/**
 * Graph Helper Class - A Collection of Helper routines for the work with graphs
 * 
 * @author Christian Klukas
 */
public class GraphHelper implements HelperClass {
	
	/**
	 * displays a graph in the mainframe
	 * 
	 * @param the
	 *           graph to be displayed
	 */
	public static void diplayGraph(Graph g) {
		EditorSession es = new EditorSession(g);
		
		GravistoService.getInstance().getMainFrame().showViewChooserDialog(es, false, null);
	}
	
	public static Graph createClusterReferenceGraph(Graph graph,
			HashMap<String, Integer> clusterNodeIDandNumberOfContainingNodes) {
		if (clusterNodeIDandNumberOfContainingNodes == null)
			clusterNodeIDandNumberOfContainingNodes = new LinkedHashMap<String, Integer>();
		Graph clusterReferenceGraph = new AdjListGraph(new ListenerManager());
		clusterReferenceGraph.setDirected(true);
		// HashMap clusterLocations = new HashMap();
		Collection<String> clusters = GraphHelper.getClusters(graph.getNodes());
		clusterReferenceGraph.getListenerManager().transactionStarted(graph);
		HashMap<String, Node> clusterNodes = new LinkedHashMap<String, Node>();
		// create "virtual" cluster graph that will be used for circular layout
		// add cluster central nodes to this non visible graph, these
		// nodes represent the target position of the cluster in the graph to be
		// layouted by
		// the springembedder
		PositionGridGenerator pgg = new PositionGridGenerator(40, 40, 400);
		for (Iterator<String> it = clusters.iterator(); it.hasNext();) {
			Node clusterNode = clusterReferenceGraph.addNode();
			String clusterID = it.next();
			clusterNodeIDandNumberOfContainingNodes.put(clusterID, new Integer(0));
			NodeTools.setClusterID(clusterNode, clusterID);
			AttributeHelper.setDefaultGraphicsAttribute(clusterNode, pgg.getNextPosition());
			AttributeHelper.setLabel(clusterNode, "Cluster " + clusterID);
			clusterNodes.put(clusterID, clusterNode);
		}
		// construct virtual cluster graph
		// the clusters will be connected as often as there are connections
		// between nodes
		// in the different clusters of the graph to be layouted by the
		// springembedder
		// only outgoing edges are used to avoid duplications
		// MODIFICATION:
		// For performance reasons no double edges are created, instead a
		// counter
		// attribute is increased, this increases speed e.g. for the minimum
		// edge crossings
		// algorithm greatly
		HashMap<String, Edge> knownEdges = new LinkedHashMap<String, Edge>();
		for (Iterator<?> it = graph.getNodes().iterator(); it.hasNext();) {
			Node graphNode = (Node) it.next();
			String cluster = NodeTools.getClusterID(graphNode, "");
			if (cluster.equals(""))
				continue;
			String cid = cluster;
			clusterNodeIDandNumberOfContainingNodes.put(cid, new Integer(clusterNodeIDandNumberOfContainingNodes.get(cid)
					.intValue() + 1));
			if (!cluster.equals(""))
				for (Iterator<?> itOutN = graphNode.getOutNeighborsIterator(); itOutN.hasNext();) {
					Node graphNodeNeighbor = (Node) itOutN.next();
					String neighborCluster = NodeTools.getClusterID(graphNodeNeighbor, "");
					if (!neighborCluster.equals("")) {
						// add edge between the two virtual cluster nodes
						// multiple edges for the same nodes should be created
						// to make it possible to reduce the number of edge
						// crossings
						// for the graph to be layouted by the springembedder
						Node clusterNodeA = clusterNodes.get(cluster);
						Node clusterNodeB = clusterNodes.get(neighborCluster);
						if (clusterNodeA != null && clusterNodeB != null) {
							String id = cluster + "->" + neighborCluster;
							String id2 = neighborCluster + "->" + cluster;
							Edge knownEdge = knownEdges.get(id);
							
							// check also for the opposite direction (do not add
							// a second edge in this case)
							Edge knownEdge2 = knownEdges.get(id2);
							if (knownEdge == null)
								knownEdge = knownEdge2;
							// ***
							
							if (knownEdge == null) {
								Edge newEdge = clusterReferenceGraph.addEdge(clusterNodeA, clusterNodeB, true, AttributeHelper
										.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
								knownEdges.put(id, newEdge);
								knownEdge = newEdge;
							}
							Integer edgeDoublingCount = (Integer) AttributeHelper.getAttributeValue(knownEdge, "cluster",
									"edgecount", new Integer(0), null);
							AttributeHelper.setAttribute(knownEdge, "cluster", "edgecount", new Integer(edgeDoublingCount
									.intValue() + 1));
						}
					}
				}
		}
		clusterReferenceGraph.getListenerManager().transactionFinished(graph);
		return clusterReferenceGraph;
	}
	
	public static Graph getClusterSubGraph(Graph mainGraph, String validClusterID) {
		AdjListGraph clusterSubGraph = new AdjListGraph(mainGraph, new ListenerManager());
		ArrayList<Long> validNodeIDs = new ArrayList<Long>();
		for (Iterator<?> it = mainGraph.getNodesIterator(); it.hasNext();) {
			Node n = (Node) it.next();
			String clusterID = NodeTools.getClusterID(n, "");
			if (clusterID.equals(validClusterID))
				validNodeIDs.add(new Long(n.getID()));
		}
		
		ArrayList<org.graffiti.graph.Node> toBeDeleted = new ArrayList<org.graffiti.graph.Node>();
		for (org.graffiti.graph.Node n : clusterSubGraph.getNodes()) {
			if (!validNodeIDs.contains(new Long(n.getID()))) {
				toBeDeleted.add(n);
			}
		}
		for (org.graffiti.graph.Node n : toBeDeleted)
			clusterSubGraph.deleteNode(n);
		
		return clusterSubGraph;
	}
	
	/**
	 * gets the nodes which can be reached by the the specified nodes
	 * 
	 * @param startNode
	 *           node to start with
	 * @return set of reachable nodes
	 */
	public static Set<Node> getConnectedNodes(Node startNode) {
		Stack<Node> stack = new Stack<Node>();
		Set<Node> hashSet = new LinkedHashSet<Node>();
		
		stack.add(startNode);
		hashSet.add(startNode);
		
		while (!stack.isEmpty()) {
			Iterator<?> neighbours = stack.pop().getNeighborsIterator();
			
			while (neighbours.hasNext()) {
				Node neighbour = (Node) neighbours.next();
				
				if (!hashSet.contains(neighbour)) {
					hashSet.add(neighbour);
					stack.push(neighbour);
				}
			}
		}
		
		return hashSet;
	}
	
	@SuppressWarnings("unchecked")
	public static void getConnectedNodes(Node startNode, boolean directed, Set<Node> result) {
		Stack<Node> stack = new Stack<Node>();
		
		stack.add(startNode);
		
		result.add(startNode);
		
		while (!stack.isEmpty()) {
			Iterator<Node> neighbours;
			
			if (!directed)
				neighbours = stack.pop().getNeighborsIterator();
			else {
				Node nnn = stack.pop();
				neighbours = new MultipleIterator(nnn.getOutNeighborsIterator(), nnn.getUndirectedNeighborsIterator());
			}
			
			while (neighbours.hasNext()) {
				Node neighbour = neighbours.next();
				
				if (!result.contains(neighbour)) {
					result.add(neighbour);
					stack.push(neighbour);
				}
			}
		}
	}
	
	/**
	 * builds for all connected components a separate graph and returns them as a
	 * collection of graphs
	 * 
	 * @param graph
	 *           the graph to use
	 * @return a collection with all connected components as separate graphs
	 */
	public static Collection<Graph> getConnectedComponents(Graph graph) {
		if (graph.getNumberOfNodes() <= 0)
			return new ArrayList<Graph>();
		
		ArrayList<Graph> graphList = new ArrayList<Graph>();
		
		List<Node> nodesToProcess = graph.getNodes();
		HashMap<Node, Node> sourceGraphNode2connectedGraphNode = new LinkedHashMap<Node, Node>();
		while (!nodesToProcess.isEmpty()) {
			Node startNode = nodesToProcess.get(0);
			Set<Node> connectedNodes = getConnectedNodes(startNode);
			Graph connectedComponentGraph = new AdjListGraph();
			for (Node n : connectedNodes) {
				Node newNode = connectedComponentGraph.addNodeCopy(n);
				sourceGraphNode2connectedGraphNode.put(n, newNode);
			}
			for (Node n : connectedNodes) {
				for (Edge e : n.getEdges()) {
					if (connectedNodes.contains(e.getSource()) || connectedNodes.contains(e.getTarget()))
						connectedComponentGraph.addEdgeCopy(e, sourceGraphNode2connectedGraphNode.get(e.getSource()),
								sourceGraphNode2connectedGraphNode.get(e.getTarget()));
				}
			}
			graphList.add(connectedComponentGraph);
			nodesToProcess.removeAll(getConnectedNodes(startNode));
		}
		return graphList;
	}
	
	/**
	 * Remove all bends from a graph
	 */
	public static void removeAllBends(Graph g, boolean enableUndo) {
		removeBends(g, g.getEdges(), enableUndo);
	}
	
	public static void removeBends(final Graph graph, final Collection<Edge> edges, boolean enableUndo) {
		if (graph == null || edges == null || edges.size() <= 0)
			return;
		
		boolean hasBends = false;
		for (Edge e : edges) {
			Collection<Vector2d> positions = AttributeHelper.getEdgeBends(e);
			if (positions.size() > 0) {
				hasBends = true;
				break;
			}
		}
		if (!hasBends)
			return;
		
		if (!enableUndo) {
			graph.getListenerManager().transactionStarted(graph);
			try {
				for (Edge edge : edges)
					AttributeHelper.removeEdgeBends(edge);
			} finally {
				graph.getListenerManager().transactionFinished(graph, true);
				GraphHelper.issueCompleteRedrawForGraph(graph);
			}
			return;
		}
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			private final String description = "Remove Edge Bends";
			private final HashMap<Edge, Collection<Vector2d>> edge2oldBendPositions = new LinkedHashMap<Edge, Collection<Vector2d>>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				edge2oldBendPositions.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					for (Edge e : edges) {
						Collection<Vector2d> positions = AttributeHelper.getEdgeBends(e);
						if (positions != null && positions.size() > 0)
							edge2oldBendPositions.put(e, positions);
						AttributeHelper.removeEdgeBends(e);
					}
				} finally {
					graph.getListenerManager().transactionFinished(this, true);
					GraphHelper.issueCompleteRedrawForGraph(graph);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Edge, Collection<Vector2d>> entry : edge2oldBendPositions.entrySet()) {
						AttributeHelper.removeEdgeBends(entry.getKey());
						AttributeHelper.addEdgeBends(entry.getKey(), entry.getValue());
					}
				} finally {
					edge2oldBendPositions.clear();
					graph.getListenerManager().transactionFinished(this, true);
					GraphHelper.issueCompleteRedrawForGraph(graph);
				}
			}
		};
		
		updateCmd.redo();
		
		if (graph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
		
	}
	
	private final static String SHAPE = GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR
			+ GraphicAttributeConstants.SHAPE;
	
	/**
	 * Add new bends to a graph
	 * 
	 * @param bends
	 *           Number of bends to be introduced, either 1 or 2!
	 * @author klukas
	 */
	public static void introduceNewBends(Graph graph, HashSet<Edge> edges, int percent, String shape, int bends,
			boolean massCenterFromSelection, String description, boolean enableUndo) {
		edges = filterSelfEdges(edges);
		removeBends(graph, edges, enableUndo);
		
		Vector2d ctr;
		if (massCenterFromSelection) {
			HashSet<Node> nodes = new LinkedHashSet<Node>();
			for (Edge e : edges) {
				nodes.add(e.getSource());
				nodes.add(e.getTarget());
			}
			ctr = NodeTools.getCenter(nodes);
		} else
			ctr = NodeTools.getCenter(edges.iterator().next().getGraph().getNodes());
		HashMap<LinkedHashMapAttribute, Collection<Vector2d>> bendAttribute2newPoints = new LinkedHashMap<LinkedHashMapAttribute, Collection<Vector2d>>();
		for (Edge edge : edges) {
			try {
				LinkedHashMapAttribute lhma = (LinkedHashMapAttribute) edge.getAttribute(AttributeConstants.BENDS);
				Node src = edge.getSource();
				Node tgt = edge.getTarget();
				Vector2d srcP = AttributeHelper.getPositionVec2d(src);
				Vector2d tgtP = AttributeHelper.getPositionVec2d(tgt);
				// Vector2d middleP = new Vector2d(
				// (srcP.x+tgtP.x)/2d,
				// (srcP.y+tgtP.y)/2d
				// );
				
				EdgeShapeAttribute edgeShape = (EdgeShapeAttribute) (edge.getAttribute(SHAPE));
				
				if ((Math.abs(srcP.x - tgtP.x) < Math.abs(srcP.y - tgtP.y) * (percent / 100d))
						|| (Math.abs(srcP.y - tgtP.y) < Math.abs(srcP.x - tgtP.x) * (percent / 100d))) {
					// AttributeHelper.setLabel(edge, "S");
					edgeShape.setValue("org.graffiti.plugins.views.defaults.StraightLineEdgeShape");
					continue;
				}
				if (!bendAttribute2newPoints.containsKey(lhma))
					bendAttribute2newPoints.put(lhma, new ArrayList<Vector2d>());
				if (bends == 1) {
					Vector2d targetPoint = getEdgePoint(ctr, srcP, tgtP);
					bendAttribute2newPoints.get(lhma).add(targetPoint);
				} else
					if (bends == 2) {
						Vector2d targetPoint1 = getEdgePointAB(srcP, tgtP, true);
						Vector2d targetPoint2 = getEdgePointAB(srcP, tgtP, false);
						
						bendAttribute2newPoints.get(lhma).add(targetPoint1);
						bendAttribute2newPoints.get(lhma).add(targetPoint2);
						
					} else {
						ErrorMsg
								.addErrorMessage("Internal Error: Invalid Bend count parameter (only 1 or 2 bends are possible).");
					}
				edgeShape.setValue(shape);
				
			} catch (AttributeNotFoundException nfe) {
				ErrorMsg.addErrorMessage(nfe);
			}
		}
		applyUndoableBendAddOperation(graph, bendAttribute2newPoints, description, enableUndo);
	}
	
	private static void applyUndoableBendAddOperation(final Graph graph,
			final HashMap<LinkedHashMapAttribute, Collection<Vector2d>> bendAttribute2newPoints, final String description,
			boolean enableUndo) {
		/*
		 * lhma.add(new CoordinateAttribute("bend", targetPoint.x,
		 * targetPoint.y)); lhma.add(new CoordinateAttribute("bend1",
		 * targetPoint1.x, targetPoint1.y)); lhma.add(new
		 * CoordinateAttribute("bend2", targetPoint2.x, targetPoint2.y));
		 */
		if (graph == null || bendAttribute2newPoints == null || bendAttribute2newPoints.size() <= 0)
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			private final HashMap<LinkedHashMapAttribute, Collection<CoordinateAttribute>> edge2newBendAttributes = new LinkedHashMap<LinkedHashMapAttribute, Collection<CoordinateAttribute>>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				edge2newBendAttributes.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<LinkedHashMapAttribute, Collection<Vector2d>> entry : bendAttribute2newPoints.entrySet()) {
						for (Vector2d newpoint : entry.getValue()) {
							int size = entry.getKey().getCollectionNoClone().size() + 1;
							CoordinateAttribute ca = new CoordinateAttribute("bend" + size, newpoint.x, newpoint.y);
							if (!edge2newBendAttributes.containsKey(entry.getKey()))
								edge2newBendAttributes.put(entry.getKey(), new ArrayList<CoordinateAttribute>());
							edge2newBendAttributes.get(entry.getKey()).add(ca);
							entry.getKey().add(ca);
						}
					}
				} finally {
					graph.getListenerManager().transactionFinished(this, true);
					GraphHelper.issueCompleteRedrawForGraph(graph);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<LinkedHashMapAttribute, Collection<CoordinateAttribute>> entry : edge2newBendAttributes
							.entrySet()) {
						Collection<CoordinateAttribute> removeThese = entry.getValue();
						for (CoordinateAttribute ca : removeThese)
							entry.getKey().getCollectionNoClone().remove(ca);
					}
				} finally {
					graph.getListenerManager().transactionFinished(this);
				}
				edge2newBendAttributes.clear();
			}
		};
		
		updateCmd.redo();
		
		if (enableUndo && graph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
	}
	
	private static HashSet<Edge> filterSelfEdges(HashSet<Edge> edges) {
		HashSet<Edge> result = new LinkedHashSet<Edge>();
		for (Edge e : edges) {
			if (e.getSource() != e.getTarget())
				result.add(e);
		}
		return result;
	}
	
	public static void introduceNewBends(Graph graph, HashSet<Edge> workEdges, int minPercent, String edgeShape, int i,
			String description, boolean enableUndo) {
		introduceNewBends(graph, workEdges, minPercent, edgeShape, i, false, description, enableUndo);
	}
	
	private static Vector2d getEdgePoint(Vector2d ctr, Vector2d srcP, Vector2d tgtP) {
		Vector2d posA = new Vector2d(srcP.x, tgtP.y);
		Vector2d posB = new Vector2d(tgtP.x, srcP.y);
		double d1 = Math.sqrt((ctr.x - posA.x) * (ctr.x - posA.x) + (ctr.y - posA.y) * (ctr.y - posA.y));
		double d2 = Math.sqrt((ctr.x - posB.x) * (ctr.x - posB.x) + (ctr.y - posB.y) * (ctr.y - posB.y));
		return d1 > d2 ? posA : posB;
	}
	
	public static void addBends(Edge edge, Collection<Vector2d> bendPoints) {
		try {
			String shape = "org.graffiti.plugins.views.defaults.SmoothLineEdgeShape";
			LinkedHashMapAttribute lhma = (LinkedHashMapAttribute) edge.getAttribute(AttributeConstants.BENDS);
			EdgeShapeAttribute edgeShape = (EdgeShapeAttribute) (edge.getAttribute(SHAPE));
			
			int cnt = 0;
			for (Vector2d targetPoint : bendPoints) {
				cnt++;
				lhma.add(new CoordinateAttribute("bend" + cnt, targetPoint.x, targetPoint.y));
			}
			
			edgeShape.setValue(shape);
			
		} catch (AttributeNotFoundException nfe) {
			// todo
		}
	}
	
	private static Vector2d getEdgePointAB(Vector2d srcP, Vector2d tgtP, boolean returnA) {
		Math.abs(tgtP.x - srcP.x);
		Math.abs(tgtP.y - srcP.y);
		Vector2d pA, pB;
		{
			// divide x line
			pA = new Vector2d(srcP.x + (tgtP.x - srcP.x) / 2d, srcP.y);
			pB = new Vector2d(srcP.x + (tgtP.x - srcP.x) / 2d, tgtP.y);
		}
		if (returnA)
			return pA;
		else
			return pB;
	}
	
	/**
	 * Returns a list of the currently selected nodes. If no nodes are selected,
	 * all nodes are returned.
	 * 
	 * @return Selected or all (if no selection) Nodes, if the given graph is
	 *         null, a empty list is returned.
	 */
	public static List<Node> getSelectedOrAllNodes(Selection selection, Graph graph) {
		List<Node> nodes;
		if (graph == null)
			return new ArrayList<Node>();
		if (selection == null || selection.isEmpty())
			nodes = graph.getNodes();
		else {
			ArrayList<Node> nl = new ArrayList<Node>();
			nl.addAll(selection.getNodes());
			nodes = nl;
		}
		return nodes;
	}
	
	public static Collection<GraphElement> getSelectedOrAllGraphElements(Selection selection, Graph graph) {
		Collection<GraphElement> result;
		if (graph == null)
			return new ArrayList<GraphElement>();
		if (selection != null)
			selection = removeNonValidElementsFromSelection(selection);
		if (selection == null || selection.isEmpty())
			result = graph.getGraphElements();
		else {
			ArrayList<GraphElement> nl = new ArrayList<GraphElement>();
			nl.addAll(selection.getElements());
			result = nl;
		}
		return result;
	}
	
	public static Selection removeNonValidElementsFromSelection(Selection s) {
		if (s == null || s.isEmpty())
			return s;
		ArrayList<GraphElement> del = null;
		for (GraphElement ge : s.getElements()) {
			if (ge.getGraph() == null) {
				if (del == null)
					del = new ArrayList<GraphElement>();
				del.add(ge);
			}
		}
		if (del != null)
			for (GraphElement ge : del)
				s.remove(ge);
		return s;
	}
	
	public static Collection<Edge> getSelectedOrAllEdges() {
		return getSelectedOrAllEdges(MainFrame.getInstance().getActiveEditorSession());
	}
	
	public static Collection<Edge> getSelectedOrAllEdges(EditorSession workSession) {
		Selection sel = null;
		if (workSession.getSelectionModel() != null)
			sel = workSession.getSelectionModel().getActiveSelection();
		Graph graph = workSession.getGraph();
		return getSelectedOrAllEdges(sel, graph);
	}
	
	public static Collection<Edge> getSelectedOrAllEdges(Selection selection, Graph graph) {
		Collection<Edge> result;
		if (graph == null)
			return new ArrayList<Edge>();
		if (selection == null || selection.isEmpty())
			result = graph.getEdges();
		else {
			ArrayList<Edge> nl = new ArrayList<Edge>();
			nl.addAll(selection.getEdges());
			result = nl;
		}
		return result;
	}
	
	/**
	 * @param activeView
	 */
	public synchronized static void issueCompleteRedrawForView(final View activeView, final Graph g) {
		if (SwingUtilities.isEventDispatchThread()) {
			if (activeView == null || g == null)
				return;
			activeView.setGraph(g);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (activeView == null || g == null)
						return;
					activeView.setGraph(g);
				}
			});
		}
	}
	
	/**
	 * Adds a new node to a graph.
	 * 
	 * @return the new node.
	 * @param posx
	 *           Position of the new Node (X)
	 * @param posy
	 *           Position of the new Node (Y)
	 * @param frameThickness_3
	 *           Thinkness of Frame (default was 3)
	 * @param width_25
	 *           Width of the new node (default was 25)
	 * @param height_25
	 *           Height of the new node (default was 25)
	 * @param frameColor_0_0_0_255
	 *           Color of the framw, the default was <code>new Color(0,0,0,255)</code>.
	 * @param fillColor_0_100_250_100
	 *           Fill-Color. Default was <code>new Color(0,100,250,100)</code>.
	 */
	public static Node addNodeToGraph(Graph graph, double posx, double posy, double frameThickness_3, double width_25,
			double height_25, Color frameColor_0_0_0_255, Color fillColor_0_100_250_100) {
		CollectionAttribute col = new HashMapAttribute("");
		AttributeHelper.setNodeGraphicsAttribute(posx, posy, frameThickness_3, width_25, height_25, frameColor_0_0_0_255,
				fillColor_0_100_250_100, col);
		
		Node node = graph.addNode(col);
		return node;
	}
	
	/**
	 * @param graphInstance
	 * @return A List of Integers, which describe the cluster numbers in the list
	 *         of given nodes.
	 */
	public static Collection<String> getClusters(List<?> nodes) {
		Set<String> result = new TreeSet<String>();
		for (Iterator<?> n = nodes.iterator(); n.hasNext();) {
			String cluster = NodeTools.getClusterID((Node) n.next(), "");
			if (!cluster.equals(""))
				result.add(cluster);
		}
		return result;
	}
	
	public static List<Node> getSelectedOrAllNodes() {
		return getSelectedOrAllNodes(MainFrame.getInstance().getActiveEditorSession());
	}
	
	public static List<Node> getSelectedOrAllNodes(EditorSession workSession) {
		Selection sel = null;
		if (workSession.getSelectionModel() != null)
			sel = workSession.getSelectionModel().getActiveSelection();
		Graph graph = workSession.getGraph();
		return getSelectedOrAllNodes(sel, graph);
	}
	
	public static Collection<GraphElement> getSelectedOrAllGraphElements() {
		try {
			return getSelectedOrAllGraphElements(MainFrame.getInstance().getActiveEditorSession());
		} catch (Exception e) {
			return new ArrayList<GraphElement>();
		}
	}
	
	public static Collection<GraphElement> getSelectedOrAllGraphElements(EditorSession workSession) {
		Selection sel = null;
		if (workSession.getSelectionModel() != null)
			sel = workSession.getSelectionModel().getActiveSelection();
		Graph graph = workSession.getGraph();
		return getSelectedOrAllGraphElements(sel, graph);
	}
	
	public static List<Node> getSelectedNodes(EditorSession workSession) {
		Selection selection = null;
		if (workSession.getSelectionModel() != null)
			selection = workSession.getSelectionModel().getActiveSelection();
		Graph graph = workSession.getGraph();
		
		List<Node> nodes;
		if (graph == null)
			return new ArrayList<Node>();
		if (selection == null || selection.isEmpty())
			return new ArrayList<Node>();
		else {
			ArrayList<Node> nl = new ArrayList<Node>();
			nl.addAll(selection.getNodes());
			nodes = nl;
		}
		return nodes;
	}
	
	public static List<NodeHelper> getSelectedOrAllHelperNodes(EditorSession workSession) {
		Selection sel = null;
		if (workSession.getSelectionModel() != null)
			sel = workSession.getSelectionModel().getActiveSelection();
		Graph graph = workSession.getGraph();
		List<Node> nl = getSelectedOrAllNodes(sel, graph);
		ArrayList<NodeHelper> result = new ArrayList<NodeHelper>();
		for (Iterator<Node> it = nl.iterator(); it.hasNext();) {
			Node n = it.next();
			result.add(new NodeHelper(n, !it.hasNext()));
		}
		return result;
	}
	
	public static List<NodeHelper> getHelperNodes(Graph graph) {
		ArrayList<NodeHelper> result = new ArrayList<NodeHelper>();
		for (Iterator<Node> it = graph.getNodesIterator(); it.hasNext();) {
			Node n = it.next();
			result.add(new NodeHelper(n, !it.hasNext()));
		}
		return result;
	}
	
	/**
	 * this same method code is duplicated somewhere... in case of changes, the
	 * method name should be searched for
	 */
	public static void issueCompleteRedrawForAllViews() {
		Set<?> s = MainFrame.getSessions();
		for (Iterator<?> it = s.iterator(); it.hasNext();) {
			Session ses = (Session) it.next();
			List<?> views = ses.getViews();
			for (Iterator<?> itViews = views.iterator(); itViews.hasNext();) {
				View v = (View) itViews.next();
				issueCompleteRedrawForView(v, ses.getGraph());
			}
		}
	}
	
	public synchronized static void issueCompleteRedrawForGraph(Graph g) {
		Set<?> s = MainFrame.getSessions();
		for (Iterator<?> it = s.iterator(); it.hasNext();) {
			Session ses = (Session) it.next();
			List<?> views = ses.getViews();
			if (ses.getGraph() == g)
				for (Iterator<?> itViews = views.iterator(); itViews.hasNext();) {
					View v = (View) itViews.next();
					issueCompleteRedrawForView(v, ses.getGraph());
				}
		}
	}
	
	public static void issueCompleteRedrawForActiveView() {
		View activeView = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getActiveView();
		Graph activeGraph = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getGraph();
		issueCompleteRedrawForView(activeView, activeGraph);
	}
	
	public static void setClusterGraphNodeSizeAndPositionFromReferenceGraph(Graph mainGraph,
			Graph clusterBackgroundGraph, BackgroundTaskStatusProviderSupportingExternalCall statusProvider) {
		if (clusterBackgroundGraph == null)
			return;
		Collection<String> clusters = getClusters(mainGraph.getNodes());
		int cnt = 0;
		int work = clusters.size();
		statusProvider.setCurrentStatusValueFine(0d);
		
		HashMap<String, String> clusterId2PathwayName = new LinkedHashMap<String, String>();
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			for (Node n : mainGraph.getNodes()) {
				String keggId = KeggGmlHelper.getKeggId(n);
				if (keggId != null && keggId.length() > 0 && clusters.contains(keggId)) {
					String lbl = AttributeHelper.getLabel(n, "");
					if (lbl != null && lbl.length() > 0) {
						lbl = StringManipulationTools.stringReplace(lbl, "<html>", "");
						lbl = StringManipulationTools.stringReplace(lbl, "<br>", "");
						lbl = StringManipulationTools.stringReplace(lbl, "TITLE:", "");
						lbl = lbl.trim();
						clusterId2PathwayName.put(keggId, lbl);
					}
				}
			}
		}
		
		for (String clusterID : clusters) {
			if (statusProvider.wantsToStop())
				break;
			statusProvider.setCurrentStatusText2("Process Sub-Graph " + (++cnt) + "/" + work + "...");
			statusProvider.setCurrentStatusValueFine(100d * cnt / work);
			Graph subGraph = getClusterSubGraph(mainGraph, clusterID);
			// Vector2d center = NodeTools.getCenter(subGraph.getNodes());
			Vector2d topLeft = NodeTools.getMinimumXY(subGraph.getNodes(), 1d, 0, 0, true);
			Vector2d bottomRight = NodeTools.getMaximumXY(subGraph.getNodes(), 1d, 0, 0, true, false);
			for (Node cn : clusterBackgroundGraph.getNodes()) {
				if (NodeTools.getClusterID(cn, "").equals(clusterID)) {
					NodeHelper nh = new NodeHelper(cn);
					nh.setLabel(clusterID);
					nh.setLabelFontSize(40, false);
					nh.setAttributeValue("clusterinfo", "subgraphNodeCount", subGraph.getNodes().size());
					nh.setAttributeValue("clusterinfo", "subgraphEdgeCount", subGraph.getEdges().size());
					
					if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
						KeggGmlHelper.setKeggId(nh, clusterID);
						KeggGmlHelper.setKeggType(nh, "map");
						KeggGmlHelper.setIsPartOfGroup(nh, false);
						if (clusterId2PathwayName.containsKey(clusterID))
							nh.setLabel(clusterId2PathwayName.get(clusterID));
					}
					
					Vector2d center = new Vector2d((bottomRight.x + topLeft.x) / 2d, (bottomRight.y + topLeft.y) / 2d);
					nh.setPosition(center.x, center.y);
					nh.setSize(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
					break;
				}
			}
		}
		statusProvider.setCurrentStatusText2(work + " Sub-Graphs processed");
		statusProvider.setCurrentStatusValueFine(100d);
	}
	
	public static void printNodeLayout(Graph graph) {
		System.out.println("Graph: " + graph.getName());
		System.out.println("Nodes/Edges: " + graph.getNodes().size() + "/" + graph.getEdges().size());
		for (Node n : graph.getNodes()) {
			Vector2d position = AttributeHelper.getPositionVec2d(n);
			Vector2d size = AttributeHelper.getSize(n);
			System.out.println("Node " + n.getID() + " [" + AttributeHelper.getLabel(n, "no label") + "]: " + "P["
					+ (int) position.x + ":" + (int) position.y + "] " + "S[" + (int) size.x + ":" + (int) size.y + "]");
		}
	}
	
	public static void exchangePositions(List<Node> nodes, final NodeSortCommand sortCommand,
			final boolean sortConsiderCluster) {
		if (sortCommand == NodeSortCommand.dontSort)
			return;
		SortedSet<NodeHelper> sortedNodes = new TreeSet<NodeHelper>(new Comparator<NodeHelper>() {
			public int compare(NodeHelper o1, NodeHelper o2) {
				if (sortConsiderCluster) {
					String c1 = o1.getClusterID("");
					String c2 = o2.getClusterID("");
					if (!c1.equals(c2))
						return c1.compareToIgnoreCase(c2);
				}
				if (sortCommand == NodeSortCommand.sortLabel)
					return o1.getLabel().compareToIgnoreCase(o2.getLabel());
				if (sortCommand == NodeSortCommand.sortLabelInverse)
					return -o1.getLabel().compareToIgnoreCase(o2.getLabel());
				if (sortCommand == NodeSortCommand.sortRatio) {
					int r = new Double(o1.getAverage()).compareTo(o2.getAverage());
					if (r == 0)
						r = o1.getLabel().compareToIgnoreCase(o2.getLabel());
					return r;
				}
				if (sortCommand == NodeSortCommand.sortRatioInverse) {
					int r = -new Double(o1.getAverage()).compareTo(o2.getAverage());
					if (r == 0)
						r = o2.getLabel().compareToIgnoreCase(o1.getLabel());
					return r;
				}
				return 0;
			}
		});
		ArrayList<Point2D> sourceListOfPositions = new ArrayList<Point2D>();
		for (Node n : nodes) {
			NodeHelper nh = new NodeHelper(n);
			sourceListOfPositions.add(nh.getPosition());
			sortedNodes.add(nh);
		}
		int idx = 0;
		for (NodeHelper nh : sortedNodes) {
			nh.setPosition(sourceListOfPositions.get(idx++));
		}
	}
	
	public static void exchangePositionsNHL(List<NodeHelper> nodes, final NodeSortCommand sortCommand,
			final boolean sortConsiderCluster) {
		if (sortCommand == NodeSortCommand.dontSort)
			return;
		ArrayList<Node> todo = new ArrayList<Node>();
		for (NodeHelper nh : nodes)
			todo.add(nh.getGraphNode());
		exchangePositions(todo, sortCommand, sortConsiderCluster);
	}
	
	public static Collection<NodeHelper> getSortedNodeHelpers(Collection<Node> nodes, final NodeSortCommand sortCommand,
			final boolean sortConsiderCluster) {
		if (sortCommand == NodeSortCommand.dontSort) {
			ArrayList<NodeHelper> result = new ArrayList<NodeHelper>();
			for (Node n : nodes) {
				if (n instanceof NodeHelper)
					result.add((NodeHelper) n);
				else {
					NodeHelper nh = new NodeHelper(n);
					result.add(nh);
				}
			}
			return result;
		}
		SortedSet<NodeHelper> sortedNodes = new TreeSet<NodeHelper>(new Comparator<NodeHelper>() {
			public int compare(NodeHelper o1, NodeHelper o2) {
				if (sortConsiderCluster) {
					String c1 = o1.getClusterID("");
					String c2 = o2.getClusterID("");
					if (!c1.equals(c2))
						return c1.compareToIgnoreCase(c2);
				}
				if (sortCommand == NodeSortCommand.sortLabel)
					return o1.getLabel().compareToIgnoreCase(o2.getLabel());
				if (sortCommand == NodeSortCommand.sortLabelInverse)
					return -o1.getLabel().compareToIgnoreCase(o2.getLabel());
				if (sortCommand == NodeSortCommand.sortRatio)
					return new Double(o1.getAverage()).compareTo(o2.getAverage());
				if (sortCommand == NodeSortCommand.sortRatioInverse)
					return -new Double(o1.getAverage()).compareTo(o2.getAverage());
				return 0;
			}
		});
		for (Node n : nodes) {
			if (n instanceof NodeHelper)
				sortedNodes.add((NodeHelper) n);
			else {
				NodeHelper nh = new NodeHelper(n);
				sortedNodes.add(nh);
			}
		}
		return sortedNodes;
	}
	
	public static Collection<NodeHelper> getSortedNodeHelpersNHL(Collection<NodeHelper> nodes,
			final NodeSortCommand sortCommand, final boolean sortConsiderCluster) {
		if (sortCommand == NodeSortCommand.dontSort) {
			ArrayList<NodeHelper> result = new ArrayList<NodeHelper>();
			for (Node n : nodes) {
				NodeHelper nh = new NodeHelper(n);
				result.add(nh);
			}
			return result;
		}
		SortedSet<NodeHelper> sortedNodes = new TreeSet<NodeHelper>(new Comparator<NodeHelper>() {
			public int compare(NodeHelper o1, NodeHelper o2) {
				if (sortConsiderCluster) {
					String c1 = o1.getClusterID("");
					String c2 = o2.getClusterID("");
					if (!c1.equals(c2))
						return c1.compareToIgnoreCase(c2);
				}
				if (sortCommand == NodeSortCommand.sortLabel)
					return o1.getLabel().compareToIgnoreCase(o2.getLabel());
				if (sortCommand == NodeSortCommand.sortLabelInverse)
					return -o1.getLabel().compareToIgnoreCase(o2.getLabel());
				if (sortCommand == NodeSortCommand.sortRatio)
					return new Double(o1.getAverage()).compareTo(o2.getAverage());
				if (sortCommand == NodeSortCommand.sortRatioInverse)
					return -new Double(o1.getAverage()).compareTo(o2.getAverage());
				return 0;
			}
		});
		for (NodeHelper nh : nodes) {
			sortedNodes.add(nh);
		}
		return sortedNodes;
	}
	
	@SuppressWarnings("unchecked")
	public static void selectGraphElements(Collection<GraphElement> elements) {
		try {
			EditorSession es = findSession((Collection) elements);
			es.getSelectionModel().getActiveSelection().addAll(elements);
			es.getSelectionModel().selectionChanged();
		} catch (Exception e) {
			// empty
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void selectGraphElements(boolean clearBefore, Collection<GraphElement> elements) {
		try {
			EditorSession es = findSession((Collection) elements);
			if (clearBefore)
				es.getSelectionModel().getActiveSelection().clear();
			es.getSelectionModel().getActiveSelection().addAll(elements);
			es.getSelectionModel().selectionChanged();
		} catch (Exception e) {
			// empty
		}
	}
	
	private static EditorSession findSession(Collection<Attributable> elements) {
		if (elements == null || elements.size() == 0)
			return null;
		else {
			Attributable ge = elements.iterator().next();
			for (Session s : MainFrame.getSessions()) {
				if (s instanceof EditorSession) {
					if (ge instanceof GraphElement) {
						if (s.getGraph() == ((GraphElement) ge).getGraph())
							return (EditorSession) s;
					} else {
						if (ge instanceof Graph)
							if (s.getGraph() == ge)
								return (EditorSession) s;
					}
				}
			}
			return null;
		}
	}
	
	private static EditorSession findSession(GraphElement element) {
		if (element == null || element.getGraph() == null)
			return null;
		else {
			GraphElement ge = element;
			for (Session s : MainFrame.getSessions()) {
				if (s instanceof EditorSession) {
					if (s.getGraph() == ge.getGraph())
						return (EditorSession) s;
				}
			}
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void unselectGraphElements(Collection<GraphElement> elements) {
		try {
			EditorSession es = findSession((Collection) elements);
			es.getSelectionModel().getActiveSelection().removeAll(elements);
			es.getSelectionModel().selectionChanged();
		} catch (Exception e) {
			// empty
		}
	}
	
	public static void selectElements(Collection<Attributable> elements) {
		try {
			EditorSession es = findSession(elements);
			es.getSelectionModel().getActiveSelection().addAll(elements);
			es.getSelectionModel().selectionChanged();
		} catch (Exception e) {
			// empty
		}
	}
	
	public static void selectGraphElement(Node n) {
		try {
			EditorSession es = findSession(n);
			es.getSelectionModel().getActiveSelection().add(n);
			es.getSelectionModel().selectionChanged();
		} catch (Exception e) {
			// empty
		}
	}
	
	public static void selectGraphElement(Edge e) {
		try {
			EditorSession es = findSession(e);
			es.getSelectionModel().getActiveSelection().add(e);
			es.getSelectionModel().selectionChanged();
		} catch (Exception err) {
			// empty
		}
	}
	
	public static void clearSelection() {
		try {
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection().clear();
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		} catch (Exception e) {
			// empty
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void selectNodes(Collection<Node> nodes) {
		selectGraphElements((Collection) nodes);
	}
	
	@SuppressWarnings("unchecked")
	public static void selectNodes(boolean clearBefore, Collection<Node> nodes) {
		selectGraphElements(clearBefore, (Collection) nodes);
	}
	
	public static Collection<GraphElement> getSelectedOrAllGraphElements(Graph graph) {
		for (Session s : MainFrame.getSessions()) {
			if ((s instanceof EditorSession) && s.getGraph() == graph) {
				return getSelectedOrAllGraphElements((EditorSession) s);
			}
		}
		return null;
	}
	
	public static void applyUndoableNodePositionUpdate(final HashMap<Node, Vector2d> nodes2newPositions,
			final String description) {
		if (nodes2newPositions == null || nodes2newPositions.size() <= 0)
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			HashMap<Node, Vector2d> oldPositions = new LinkedHashMap<Node, Vector2d>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				oldPositions.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				Graph myGraph = nodes2newPositions.keySet().iterator().next().getGraph();
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Node, Vector2d> entry : nodes2newPositions.entrySet()) {
						Node n = entry.getKey();
						Vector2d oldPos = AttributeHelper.getPositionVec2d(n);
						Vector2d newPos = entry.getValue();
						if (!oldPositions.containsKey(n))
							oldPositions.put(n, oldPos);
						AttributeHelper.setPosition(n, newPos);
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				Graph myGraph = null;
				for (Node n : nodes2newPositions.keySet()) {
					if (n.getGraph() != null) {
						myGraph = n.getGraph();
						break;
					}
				}
				if (myGraph == null) {
					CannotUndoException ce = new CannotUndoException() {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
						
						@Override
						public String getMessage() {
							return "Graph elements have been deleted";
						}
						
					};
					throw ce;
				}
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Node, Vector2d> entry : nodes2newPositions.entrySet()) {
						if (entry.getKey().getGraph() == null)
							continue;
						Vector2d oldPos = oldPositions.get(entry.getKey());
						if (oldPos != null)
							AttributeHelper.setPosition(entry.getKey(), oldPos);
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
				oldPositions.clear();
			}
		};
		
		updateCmd.redo();
		
		Graph myGraph = nodes2newPositions.keySet().iterator().next().getGraph();
		if (MainFrame.getInstance() != null && MainFrame.getInstance().getActiveSession() != null)
			if (myGraph == MainFrame.getInstance().getActiveSession().getGraph()) {
				UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
				undo.beginUpdate();
				undo.postEdit(updateCmd);
				undo.endUpdate();
			}
	}
	
	public static void applyUndoableNodeSizeUpdate(final HashMap<Node, Vector2d> nodes2newNodeSize,
			final String description) {
		if (nodes2newNodeSize == null || nodes2newNodeSize.size() <= 0)
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			HashMap<Node, Vector2d> oldSize = new LinkedHashMap<Node, Vector2d>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				oldSize.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				Graph myGraph = nodes2newNodeSize.keySet().iterator().next().getGraph();
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Node, Vector2d> entry : nodes2newNodeSize.entrySet()) {
						Node n = entry.getKey();
						Vector2d oldS = AttributeHelper.getSize(n);
						Vector2d newS = entry.getValue();
						if (!oldSize.containsKey(n))
							oldSize.put(n, oldS);
						AttributeHelper.setSize(n, newS);
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				Graph myGraph = null;
				for (Node n : nodes2newNodeSize.keySet()) {
					if (n.getGraph() != null) {
						myGraph = n.getGraph();
						break;
					}
				}
				if (myGraph == null) {
					CannotUndoException ce = new CannotUndoException() {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
						
						@Override
						public String getMessage() {
							return "Graph elements have been deleted";
						}
						
					};
					throw ce;
				}
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Node, Vector2d> entry : nodes2newNodeSize.entrySet()) {
						if (entry.getKey().getGraph() == null)
							continue;
						Vector2d oldS = oldSize.get(entry.getKey());
						if (oldS != null)
							AttributeHelper.setSize(entry.getKey(), oldS);
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
				oldSize.clear();
			}
		};
		
		updateCmd.redo();
		
		Graph myGraph = nodes2newNodeSize.keySet().iterator().next().getGraph();
		if (myGraph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
		
	}
	
	public static void applyUndoableNodeAndBendPositionUpdate(final HashMap<Node, Vector2d> nodes2newPositions,
			final HashMap<CoordinateAttribute, Vector2d> bends2newPositions, final String description) {
		if (nodes2newPositions == null || bends2newPositions == null
				|| (nodes2newPositions.size() == 0 && bends2newPositions.size() == 0))
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			HashMap<Node, Vector2d> oldPositions = new LinkedHashMap<Node, Vector2d>();
			HashMap<CoordinateAttribute, Vector2d> oldPositionsOfBends = new LinkedHashMap<CoordinateAttribute, Vector2d>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				oldPositions.clear();
				oldPositionsOfBends.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				Graph myGraph = nodes2newPositions.keySet().iterator().next().getGraph();
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Node, Vector2d> entry : nodes2newPositions.entrySet()) {
						Node n = entry.getKey();
						Vector2d oldPos = AttributeHelper.getPositionVec2d(n);
						Vector2d newPos = entry.getValue();
						if (!oldPositions.containsKey(n))
							oldPositions.put(n, oldPos);
						AttributeHelper.setPosition(n, newPos);
					}
					for (Entry<CoordinateAttribute, Vector2d> entry : bends2newPositions.entrySet()) {
						Graph g = ((Edge) entry.getKey().getAttributable()).getGraph();
						if (g == null)
							continue;
						if (entry.getValue() != null) {
							if (!oldPositionsOfBends.containsKey(entry.getKey()))
								oldPositionsOfBends.put(entry.getKey(), new Vector2d(entry.getKey().getX(), entry.getKey()
										.getY()));
							entry.getKey().setX(entry.getValue().x);
							entry.getKey().setY(entry.getValue().y);
						}
					}
					
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				Graph myGraph = null;
				for (Node n : nodes2newPositions.keySet()) {
					if (n.getGraph() != null) {
						myGraph = n.getGraph();
						break;
					}
				}
				if (myGraph == null)
					for (CoordinateAttribute ca : bends2newPositions.keySet()) {
						Graph g = ((Edge) ca.getAttributable()).getGraph();
						if (g != null) {
							myGraph = g;
							break;
						}
					}
				if (myGraph == null) {
					CannotUndoException ce = new CannotUndoException() {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
						
						@Override
						public String getMessage() {
							return "Graph elements have been deleted";
						}
						
					};
					throw ce;
				}
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Node, Vector2d> entry : nodes2newPositions.entrySet()) {
						if (entry.getKey().getGraph() == null)
							continue;
						Vector2d oldPos = oldPositions.get(entry.getKey());
						if (oldPos != null)
							AttributeHelper.setPosition(entry.getKey(), oldPos);
					}
					for (Entry<CoordinateAttribute, Vector2d> entry : bends2newPositions.entrySet()) {
						Graph g = ((Edge) entry.getKey().getAttributable()).getGraph();
						if (g == null)
							continue;
						Vector2d oldPos = oldPositionsOfBends.get(entry.getKey());
						if (oldPos != null) {
							entry.getKey().setCoordinate(oldPos.x, oldPos.y);
						}
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
				oldPositions.clear();
				oldPositionsOfBends.clear();
			}
		};
		
		updateCmd.redo();
		
		Graph myGraph = null;
		for (Node n : nodes2newPositions.keySet()) {
			if (n.getGraph() != null) {
				myGraph = n.getGraph();
				break;
			}
		}
		if (myGraph == null)
			for (CoordinateAttribute ca : bends2newPositions.keySet()) {
				Graph g = ((Edge) ca.getAttributable()).getGraph();
				if (g != null) {
					myGraph = g;
					break;
				}
			}
		if (MainFrame.getInstance().getActiveSession() != null
				&& myGraph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
	}
	
	public static void applyUndoableNodePositionAndSizeUpdate(final HashMap<Node, Vector2d> nodes2newPositions,
			final HashMap<Node, Vector2d> nodes2newNodeSize, final String description) {
		if (nodes2newPositions == null || nodes2newNodeSize == null
				|| (nodes2newNodeSize.size() == 0 && nodes2newPositions.size() == 0))
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			HashMap<Node, Vector2d> oldPositions = new LinkedHashMap<Node, Vector2d>();
			HashMap<Node, Vector2d> oldNodeSize = new LinkedHashMap<Node, Vector2d>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				oldPositions.clear();
				oldNodeSize.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				Graph myGraph = nodes2newPositions.keySet().iterator().next().getGraph();
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Node, Vector2d> entry : nodes2newPositions.entrySet()) {
						Node n = entry.getKey();
						Vector2d oldPos = AttributeHelper.getPositionVec2d(n);
						Vector2d newPos = entry.getValue();
						if (!oldPositions.containsKey(n))
							oldPositions.put(n, oldPos);
						AttributeHelper.setPosition(n, newPos);
					}
					for (Entry<Node, Vector2d> entry : nodes2newNodeSize.entrySet()) {
						Node n = entry.getKey();
						Vector2d oldSize = AttributeHelper.getSize(n);
						Vector2d newSize = entry.getValue();
						if (!oldNodeSize.containsKey(n))
							oldNodeSize.put(n, oldSize);
						AttributeHelper.setSize(n, newSize);
					}
					
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				Graph myGraph = null;
				for (Node n : nodes2newPositions.keySet()) {
					if (n.getGraph() != null) {
						myGraph = n.getGraph();
						break;
					}
				}
				if (myGraph == null)
					for (Node n : nodes2newNodeSize.keySet()) {
						if (n.getGraph() != null) {
							myGraph = n.getGraph();
							break;
						}
					}
				if (myGraph == null) {
					CannotUndoException ce = new CannotUndoException() {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
						
						@Override
						public String getMessage() {
							return "Graph elements have been deleted";
						}
						
					};
					throw ce;
				}
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<Node, Vector2d> entry : nodes2newPositions.entrySet()) {
						if (entry.getKey().getGraph() == null)
							continue;
						Vector2d oldPos = oldPositions.get(entry.getKey());
						if (oldPos != null)
							AttributeHelper.setPosition(entry.getKey(), oldPos);
					}
					for (Entry<Node, Vector2d> entry : nodes2newPositions.entrySet()) {
						if (entry.getKey().getGraph() == null)
							continue;
						Vector2d oldSize = oldNodeSize.get(entry.getKey());
						if (oldSize != null)
							AttributeHelper.setSize(entry.getKey(), oldSize);
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
				oldPositions.clear();
				oldNodeSize.clear();
			}
		};
		
		updateCmd.redo();
		
		Graph myGraph = null;
		for (Node n : nodes2newPositions.keySet()) {
			if (n.getGraph() != null) {
				myGraph = n.getGraph();
				break;
			}
		}
		if (myGraph == null)
			for (Node n : nodes2newNodeSize.keySet()) {
				if (n.getGraph() != null) {
					myGraph = n.getGraph();
					break;
				}
			}
		if (myGraph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
	}
	
	public static void postUndoableChanges(final Graph myGraph,
			final HashMap<CoordinateAttribute, Vector2d> coordinates2oldPositions,
			final HashMap<CoordinateAttribute, Vector2d> coordinates2newPositions, final String description) {
		if (myGraph == null || coordinates2oldPositions == null || coordinates2newPositions == null
				|| coordinates2newPositions.size() <= 0)
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				coordinates2newPositions.clear();
				coordinates2oldPositions.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<CoordinateAttribute, Vector2d> entry : coordinates2newPositions.entrySet()) {
						if (entry.getValue() != null) {
							entry.getKey().setX(entry.getValue().x);
							entry.getKey().setY(entry.getValue().y);
						}
					}
					
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<CoordinateAttribute, Vector2d> entry : coordinates2oldPositions.entrySet()) {
						Vector2d oldPos = coordinates2oldPositions.get(entry.getKey());
						if (oldPos != null) {
							entry.getKey().setCoordinate(oldPos.x, oldPos.y);
						}
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
		};
		
		if (MainFrame.getInstance().getActiveSession() != null
				&& myGraph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		} else {
			updateCmd.redo();
		}
	}
	
	public static void postUndoableChanges3d(final Graph myGraph,
			final HashMap<CoordinateAttribute, Vector3d> coordinates2oldPositions,
			final HashMap<CoordinateAttribute, Vector3d> coordinates2newPositions, final String description) {
		if (myGraph == null || coordinates2oldPositions == null || coordinates2newPositions == null
				|| coordinates2newPositions.size() <= 0)
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				coordinates2newPositions.clear();
				coordinates2oldPositions.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<CoordinateAttribute, Vector3d> entry : coordinates2newPositions.entrySet()) {
						if (entry.getValue() != null) {
							entry.getKey().setX(entry.getValue().x);
							entry.getKey().setY(entry.getValue().y);
							AttributeHelper.setPositionZ((Node) entry.getKey().getAttributable(), entry.getValue().z);
						}
					}
					
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				myGraph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<CoordinateAttribute, Vector3d> entry : coordinates2oldPositions.entrySet()) {
						Vector3d oldPos = coordinates2oldPositions.get(entry.getKey());
						if (oldPos != null) {
							entry.getKey().setCoordinate(oldPos.x, oldPos.y);
							AttributeHelper.setPositionZ((Node) entry.getKey().getAttributable(), oldPos.z);
						}
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
		};
		
		if (myGraph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
	}
	
	public static void enumerateNodePositions(Graph graphInstance, HashMap<CoordinateAttribute, Vector2d> oldPositions) {
		for (Node n : graphInstance.getNodes()) {
			CoordinateAttribute coA = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
			oldPositions.put(coA, new Vector2d(coA.getCoordinate()));
		}
	}
	
	public static void enumerateNodePositions3d(Graph graphInstance, HashMap<CoordinateAttribute, Vector3d> oldPositions) {
		for (Node n : graphInstance.getNodes()) {
			CoordinateAttribute coA = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
			oldPositions.put(coA, AttributeHelper.getPositionVec3d(n, true));
		}
	}
	
	public static void applyUndoableClusterIdAssignment(final Graph graph,
			final HashMap<GraphElement, String> ge2newClusterID, final String description, boolean enableUndo) {
		if (graph == null || ge2newClusterID == null || ge2newClusterID.size() <= 0)
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			HashMap<GraphElement, String> oldIds = new LinkedHashMap<GraphElement, String>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				oldIds.clear();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<GraphElement, String> entry : ge2newClusterID.entrySet()) {
						GraphElement ge = entry.getKey();
						String oldId = NodeTools.getClusterID(ge, null);
						String newId = entry.getValue();
						if (!oldIds.containsKey(ge))
							oldIds.put(ge, oldId);
						NodeTools.setClusterID(ge, newId);
					}
				} finally {
					graph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					for (Entry<GraphElement, String> entry : ge2newClusterID.entrySet()) {
						if (entry.getKey().getGraph() == null)
							continue;
						String oldId = oldIds.get(entry.getKey());
						NodeTools.setClusterID(entry.getKey(), oldId);
					}
				} finally {
					graph.getListenerManager().transactionFinished(this);
				}
				oldIds.clear();
			}
		};
		
		updateCmd.redo();
		
		if (enableUndo && graph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
	}
	
	public static void postUndoableNodeAndEdgeAdditions(final Graph graph, final HashSet<Node> newNodes,
			final HashSet<Edge> newEdges, final String description) {
		if (graph == null || newNodes == null || newEdges == null || (newNodes.size() + newEdges.size() <= 0))
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			HashMap<Node, Node> oldNode2newlyAddedNode = new LinkedHashMap<Node, Node>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
			}
			
			@Override
			public void redo() throws CannotRedoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					HashSet<Node> nn = new LinkedHashSet<Node>();
					for (Node n : newNodes) {
						Node newNode = graph.addNodeCopy(n);
						oldNode2newlyAddedNode.put(n, newNode);
						nn.add(newNode);
					}
					newNodes.clear();
					newNodes.addAll(nn);
					HashSet<Edge> ne = new LinkedHashSet<Edge>();
					for (Edge e : newEdges) {
						Node source = oldNode2newlyAddedNode.get(e.getSource());
						Node target = oldNode2newlyAddedNode.get(e.getTarget());
						if (source == null)
							source = e.getSource();
						if (target == null)
							target = e.getSource();
						if (source.getGraph() != null && target.getGraph() != null) {
							Edge newEdge = graph.addEdgeCopy(e, source, target);
							ne.add(newEdge);
						}
					}
					newEdges.clear();
					newEdges.addAll(ne);
				} finally {
					graph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					for (Node n : newNodes) {
						if (n.getGraph() != null)
							graph.deleteNode(n);
					}
					for (Edge e : newEdges) {
						if (e.getGraph() != null)
							graph.deleteEdge(e);
					}
				} finally {
					graph.getListenerManager().transactionFinished(this);
				}
			}
		};
		
		if (graph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
	}
	
	public static Set<Node> getLeafNodes(Graph graph) {
		LinkedHashSet<Node> result = new LinkedHashSet<Node>();
		for (Node n : graph.getNodes())
			if (n.getOutDegree() == 0)
				result.add(n);
		return result;
	}
	
	public static Set<Node> getLeafNodes(Collection<Node> sourceNodes) {
		LinkedHashSet<Node> result = new LinkedHashSet<Node>();
		LinkedHashSet<Node> reachableNodes = new LinkedHashSet<Node>();
		for (Node n : sourceNodes)
			getConnectedNodes(n, true, reachableNodes);
		for (Node n : reachableNodes)
			if (n.getOutDegree() == 0)
				result.add(n);
		return result;
	}
	
	public static Collection<GraphElement> getVisibleElements(Collection<GraphElement> elements) {
		ArrayList<GraphElement> result = new ArrayList<GraphElement>();
		for (GraphElement ge : elements)
			if (!AttributeHelper.isHiddenGraphElement(ge))
				result.add(ge);
		return result;
	}
	
	public static Collection<Node> getVisibleNodes(Collection<Node> elements) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node ge : elements)
			if (!AttributeHelper.isHiddenGraphElement(ge))
				result.add(ge);
		return result;
	}
	
	public static void moveGraph(Graph graph, double offX, double offY) {
		CenterLayouterAlgorithm.moveGraph(graph, "graph movement (" + (int) offX + "/" + (int) offY + ")", false, offX,
				offY);
	}
	
	public static void getShortestDistances(HashMap<Node, Integer> result, HashSet<Node> from, boolean directed,
			int currentDistance) {
		HashSet<Node> todo = new HashSet<Node>();
		for (Node n : from) {
			result.put(n, currentDistance);
			Collection<Edge> edges;
			if (directed)
				edges = n.getAllOutEdges();
			else
				edges = n.getEdges();
			
			for (Edge e : edges) {
				if (e.getSource() == e.getTarget())
					continue;
				Node neighbour;
				if (e.getSource() == n)
					neighbour = e.getTarget();
				else
					neighbour = e.getSource();
				if (!result.containsKey(neighbour))
					todo.add(neighbour);
			}
		}
		if (todo.size() > 0)
			getShortestDistances(result, todo, directed, currentDistance + 1);
	}
	
	public static void applyUndoableEdgeReversal(final Graph graph, final Collection<Edge> edges,
			final String description) {
		
		if (graph == null || edges == null || edges.size() <= 0)
			return;
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			ArrayList<Edge> newEdges = new ArrayList<Edge>();
			
			@Override
			public String getPresentationName() {
				return description;
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo " + description;
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo " + description;
			}
			
			@Override
			public void die() {
				super.die();
				newEdges.clear();
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void redo() throws CannotRedoException {
				Graph myGraph = null;
				for (Edge e : edges) {
					if (e.getGraph() != null) {
						myGraph = e.getGraph();
						break;
					}
				}
				if (myGraph == null)
					throw new CannotRedoException() {
						/**
					 * 
					 */
						private static final long serialVersionUID = 1L;
						
						@Override
						public String getMessage() {
							return "In the mean time all graph elements from the working set have been deleted!";
						}
						
					};
				myGraph.getListenerManager().transactionStarted(this);
				int missing = 0;
				try {
					for (Edge e : edges) {
						if (e.getGraph() == null) {
							missing++;
							continue;
						}
						Edge newEdge = myGraph.addEdgeCopy(e, (e).getTarget(), (e).getSource());
						ArrayList<Vector2d> bends = AttributeHelper.getEdgeBends(newEdge);
						if (bends != null && bends.size() >= 2) {
							AttributeHelper.removeEdgeBends(newEdge);
							Collection<Vector2d> newOrder = new ArrayList<Vector2d>();
							for (int i = bends.size() - 1; i >= 0; i--)
								newOrder.add(bends.get(i));
							AttributeHelper.addEdgeBends(newEdge, newOrder);
						}
						newEdges.add(newEdge);
					}
					GraphHelper.unselectGraphElements((Collection) edges);
					myGraph.deleteAll((Collection) edges);
					GraphHelper.selectGraphElements((Collection) newEdges);
					if (missing > 0)
						MainFrame.showMessageDialog("<html>In the meantime " + missing
								+ " edges have been removed from the graph.", "Processing incomplete");
				} finally {
					myGraph.getListenerManager().transactionFinished(this, true);
					GraphHelper.issueCompleteRedrawForGraph(myGraph);
				}
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void undo() throws CannotUndoException {
				Graph myGraph = null;
				for (Edge e : newEdges) {
					if (e.getGraph() != null) {
						myGraph = e.getGraph();
						break;
					}
				}
				if (myGraph == null) {
					CannotUndoException ce = new CannotUndoException() {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
						
						@Override
						public String getMessage() {
							return "In the mean time all graph elements from the working set have been deleted!";
						}
						
					};
					throw ce;
				}
				myGraph.getListenerManager().transactionStarted(this);
				try {
					edges.clear();
					int missing = 0;
					for (Edge e : newEdges) {
						if (e.getGraph() == null) {
							missing++;
							continue;
						}
						Edge newEdge = graph.addEdgeCopy(e, (e).getTarget(), (e).getSource());
						ArrayList<Vector2d> bends = AttributeHelper.getEdgeBends(newEdge);
						if (bends != null && bends.size() >= 2) {
							AttributeHelper.removeEdgeBends(newEdge);
							Collection<Vector2d> newOrder = new ArrayList<Vector2d>();
							for (int i = bends.size() - 1; i >= 0; i--)
								newOrder.add(bends.get(i));
							AttributeHelper.addEdgeBends(newEdge, newOrder);
						}
						edges.add(newEdge);
					}
					GraphHelper.unselectGraphElements((Collection) newEdges);
					myGraph.deleteAll((Collection) newEdges);
					newEdges.clear();
					GraphHelper.selectGraphElements((Collection) edges);
					if (missing > 0)
						MainFrame.showMessageDialog("<html>In the meantime " + missing
								+ " edges have been removed from the graph.", "Processing incomplete");
				} finally {
					myGraph.getListenerManager().transactionFinished(this, true);
					GraphHelper.issueCompleteRedrawForGraph(myGraph);
				}
			}
		};
		
		Graph myGraph = null;
		for (Edge e : edges) {
			if (e.getGraph() != null) {
				myGraph = e.getGraph();
				break;
			}
		}
		
		updateCmd.redo();
		
		if (myGraph == MainFrame.getInstance().getActiveSession().getGraph()) {
			UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
			undo.beginUpdate();
			undo.postEdit(updateCmd);
			undo.endUpdate();
		}
	}
	
	public static int countOverlapps(Graph g, Vector2d pos, Vector2d size) {
		int res = 0;
		size = new Vector2d(size);
		size.x += 10;
		size.y += 10;
		Rectangle2D otherR = new Rectangle2D.Double(pos.x - size.x / 2, pos.y - size.y / 2, size.x, size.y);
		for (Node n : g.getNodes()) {
			Rectangle2D.Double r = AttributeHelper.getNodeRectangle(n);
			if (r.intersects(otherR))
				res++;
		}
		return res;
	}
	
	/**
	 * @param n
	 *           Node
	 * @return Null, if clustering coefficient is undefined (division by zero)
	 */
	public static Double getClusteringCoefficientUndirected(Node n) {
		Collection<Node> neiL = n.getNeighbors();
		int neighbors = neiL.size();
		if (neighbors <= 1)
			return null;
		else {
			int edgesConnectingNeighbors = 0;
			for (Node nei1 : neiL) {
				Set<Node> nei1L = nei1.getNeighbors();
				for (Node nei2 : neiL) {
					if (nei2 == nei1)
						continue;
					if (nei1L.contains(nei2))
						edgesConnectingNeighbors++;
				}
			}
			edgesConnectingNeighbors = edgesConnectingNeighbors / 2;
			double result = 2d * edgesConnectingNeighbors / neighbors / (neighbors - 1);
			return new Double(result);
		}
	}
	
	/**
	 * @param n
	 *           Node
	 * @return Null, if clustering coefficient is undefined (division by zero)
	 */
	public static Double getClusteringCoefficientDirected(Node n) {
		Collection<Node> neiL = n.getNeighbors();
		int neighbors = neiL.size();
		if (neighbors <= 1)
			return null;
		else {
			int edgesConnectingNeighbors = 0;
			for (Node nei1 : neiL) {
				Set<Node> nei1L = nei1.getOutNeighbors();
				for (Node nei2 : neiL) {
					if (nei2 == nei1)
						continue;
					if (nei1L.contains(nei2))
						edgesConnectingNeighbors++;
				}
			}
			double result = (double) edgesConnectingNeighbors / (double) neighbors / (neighbors - 1);
			return new Double(result);
		}
	}
}
