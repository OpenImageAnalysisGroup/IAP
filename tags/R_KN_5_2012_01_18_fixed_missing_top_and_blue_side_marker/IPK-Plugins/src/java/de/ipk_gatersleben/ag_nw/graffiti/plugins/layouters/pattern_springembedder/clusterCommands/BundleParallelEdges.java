/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

public class BundleParallelEdges extends AbstractAlgorithm {
	
	private Vector2d sourcePos;
	private Vector2d targetPos;
	private Vector2d onethird;
	private Vector2d twothird;
	private Vector2d middle;
	
	private static double scalingFactor = 2f;
	private static Shapes shape = Shapes.SMOOTH;
	private static String source = "Cluster ID";
	
	public String getName() {
		return "Bundle Parallel Edges...";
	}
	
	@Override
	public String getCategory() {
		return "Edges";
	}
	
	@Override
	public String getDescription() {
		return "<html>Will introduce bends to the selected egdes<br>" +
							"in order to bundle them. This will highlight the<br>" +
							"general edge direction, without loosing single edges";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new DoubleParameter(scalingFactor, "Edge distance", "Indicates the distance between edges when bundled"),
							new ObjectListParameter(shape, "Edge Shape", "", Shapes.values()),
							new ObjectListParameter(source, "Source of Cluster Information",
												"Determines, which attribute will be used for the node clustering information", new String[] { "Cluster ID",
																	"Source Attribut" }) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		scalingFactor = (Double) params[0].getValue();
		shape = (Shapes) params[1].getValue();
		source = (String) params[2].getValue();
	}
	
	@Override
	public void check() throws PreconditionException {
		if (this.graph == null)
			throw new PreconditionException("No graph available!");
		if (this.graph.getEdges().size() < 2)
			throw new PreconditionException("Graph contains not more than one edge!");
	}
	
	public void execute() {
		this.graph.getListenerManager().transactionStarted(this);
		try {
			
			Collection<Edge> selectedOrAllEdges = null;
			if (selection == null || selection.getEdges().size() <= 1)
				selectedOrAllEdges = GraphHelper.getSelectedOrAllEdges();
			else
				selectedOrAllEdges = selection.getEdges();
			
			HashMap<String, HashSet<Edge>> edges = new HashMap<String, HashSet<Edge>>();
			
			HashMap<String, HashSet<Node>> nodescluster = new HashMap<String, HashSet<Node>>();
			
			for (Edge ed : selectedOrAllEdges) {
				Node ndt = ed.getTarget();
				String clusterid1 = source.equals("Cluster ID") ? NodeTools.getClusterID(ndt, null) : (String) AttributeHelper.getAttributeValue(ndt, "src",
									"fileName", null, "");
				if (clusterid1 != null) {
					if (!nodescluster.containsKey(clusterid1))
						nodescluster.put(clusterid1, new HashSet<Node>());
					nodescluster.get(clusterid1).add(ndt);
				}
				ndt = ed.getSource();
				String clusterid2 = source.equals("Cluster ID") ? NodeTools.getClusterID(ndt, null) : (String) AttributeHelper.getAttributeValue(ndt, "src",
									"fileName", null, "");
				if (clusterid2 != null) {
					if (!nodescluster.containsKey(clusterid2))
						nodescluster.put(clusterid2, new HashSet<Node>());
					nodescluster.get(clusterid2).add(ndt);
				}
				String edgecluster = clusterid1 + "/" + clusterid2;
				if (!edges.containsKey(edgecluster))
					edges.put(edgecluster, new HashSet<Edge>());
				edges.get(edgecluster).add(ed);
			}
			
			ArrayList<String> clusters = new ArrayList<String>(nodescluster.keySet());
			
			if (nodescluster.keySet().size() < 2)
				return;
			
			// pairwise bundling edges
			for (int c1 = 0; c1 < clusters.size() - 1; c1++)
				for (int c2 = c1 + 1; c2 < clusters.size(); c2++) {
					
					// if the edge clusterstring was created the other way around...
					HashSet<Edge> edges1 = edges.get(clusters.get(c1) + "/" + clusters.get(c2));
					HashSet<Edge> edges2 = edges.get(clusters.get(c2) + "/" + clusters.get(c1));
					
					// change only edges between the actual clusters
					ArrayList<Edge> clusteredges = new ArrayList<Edge>();
					if (edges1 != null)
						clusteredges.addAll(edges1);
					if (edges2 != null)
						clusteredges.addAll(edges2);
					
					if (clusteredges == null || clusteredges.size() < 1)
						continue;
					
					sourcePos = NodeTools.getCenter(nodescluster.get(clusters.get(c1)));
					targetPos = NodeTools.getCenter(nodescluster.get(clusters.get(c2)));
					
					// center node position difference vector
					Vector2d diffVector = new Vector2d(targetPos.x - sourcePos.x, targetPos.y - sourcePos.y);
					
					// orthogonal (scaled) vector
					Vector2d orthVector = diffVector.getOrthogonal().scale(scalingFactor / diffVector.distance(0, 0));
					
					// 1/4 and 3/4 point between nodes centers
					onethird = new Vector2d(sourcePos.x + diffVector.x / 8f, sourcePos.y + diffVector.y / 8f);
					middle = new Vector2d(sourcePos.x + diffVector.x / 2f, sourcePos.y + diffVector.y / 2f);
					twothird = new Vector2d(sourcePos.x + 7 * diffVector.x / 8f, sourcePos.y + 7 * diffVector.y / 8f);
					
					// move the 1/4 and 3/4 points in direction of the middle point, until they leave the area of the cluster nodes
					double maxDistanceC1 = getMaxDistanceFromNodesToCenter(nodescluster.get(clusters.get(c1)), sourcePos);
					double maxDistanceC2 = getMaxDistanceFromNodesToCenter(nodescluster.get(clusters.get(c2)), targetPos);
					
					while (onethird.distance(sourcePos) < maxDistanceC1) {
						if (middle.distance(sourcePos) < onethird.distance(sourcePos)) {
							onethird = null; // has moved beyond middle point, so we ignore this bendpoint
							break;
						}
						onethird = new Vector2d(onethird.x + (diffVector.x / 20), onethird.y + (diffVector.y / 20));
					}
					while (twothird.distance(targetPos) < maxDistanceC2) {
						if (middle.distance(targetPos) < twothird.distance(targetPos)) {
							twothird = null; // has moved beyond middle point, so we ignore this bendpoint
							break;
						}
						twothird = new Vector2d(twothird.x - (diffVector.x / 20), twothird.y - (diffVector.y / 20));
					}
					
					int oddEqualizer = 0;
					boolean odd = clusteredges.size() % 2 == 1;
					if (odd) {
						oddEqualizer = 1;
						
						Edge ed = clusteredges.get(0);
						AttributeHelper.removeEdgeBends(ed);
						
						Vector2d p1 = getCorrectlySortedBendpoint(ed.getSource());
						Vector2d p2 = getCorrectlySortedBendpoint(ed.getTarget());
						
						if (p1 != null)
							adjustEdge(ed, p1);
						adjustEdge(ed, middle);
						if (p2 != null)
							adjustEdge(ed, p2);
					}
					
					for (int i = 0; i < (clusteredges.size() - oddEqualizer); i += 2) {
						
						Edge edgeOne = clusteredges.get(oddEqualizer + i);
						Edge edgeTwo = clusteredges.get(oddEqualizer + i + 1);
						AttributeHelper.removeEdgeBends(edgeOne);
						AttributeHelper.removeEdgeBends(edgeTwo);
						
						Vector2d cluster1edge1 = getCorrectlySortedBendpoint(edgeOne.getSource());
						Vector2d cluster1edge2 = getCorrectlySortedBendpoint(edgeTwo.getSource());
						Vector2d cluster2edge1 = getCorrectlySortedBendpoint(edgeOne.getTarget());
						Vector2d cluster2edge2 = getCorrectlySortedBendpoint(edgeTwo.getTarget());
						
						int mult = (i + 1) + ((odd) ? 1 : 0);
						
						if (cluster1edge1 != null)
							adjustEdge(edgeOne, new Vector2d(cluster1edge1.x + orthVector.x * mult, cluster1edge1.y + orthVector.y * mult));
						if (cluster1edge2 != null)
							adjustEdge(edgeTwo, new Vector2d(cluster1edge2.x - orthVector.x * mult, cluster1edge2.y - orthVector.y * mult));
						
						adjustEdge(edgeOne, new Vector2d(middle.x + orthVector.x * mult, middle.y + orthVector.y * mult));
						adjustEdge(edgeTwo, new Vector2d(middle.x - orthVector.x * mult, middle.y - orthVector.y * mult));
						
						if (cluster2edge1 != null)
							adjustEdge(edgeOne, new Vector2d(cluster2edge1.x + orthVector.x * mult, cluster2edge1.y + orthVector.y * mult));
						if (cluster2edge2 != null)
							adjustEdge(edgeTwo, new Vector2d(cluster2edge2.x - orthVector.x * mult, cluster2edge2.y - orthVector.y * mult));
						
					}
				}
			
			// TODO: sort the edge-bendpoints in such a way, that the edge crossings between cluster-pairs will be minimized
			
		} finally {
			this.graph.getListenerManager().transactionFinished(this);
		}
	}
	
	private double getMaxDistanceFromNodesToCenter(HashSet<Node> nodes, Vector2d center) {
		double distance = Double.NEGATIVE_INFINITY;
		for (Node nd : nodes) {
			double dtemp = AttributeHelper.getPositionVec2d(nd).distance(center);
			if (dtemp > distance)
				distance = dtemp;
		}
		return distance;
	}
	
	/**
	 * Will sort the bend points by determining the direction of the edge, because the bends are sorted from source to target
	 * 
	 * @param nd
	 * @return
	 */
	private Vector2d getCorrectlySortedBendpoint(Node nd) {
		return sourcePos.distance(AttributeHelper.getPositionVec2d(nd)) < targetPos.distance(AttributeHelper.getPositionVec2d(nd)) ?
							onethird : twothird;
	}
	
	private void adjustEdge(Edge edge, Vector2d pos) {
		AttributeHelper.addEdgeBend(edge, pos);
		AttributeHelper.setEdgeBendStyle(edge, shape.shape);
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}
	
	public enum Shapes {
		SMOOTH("org.graffiti.plugins.views.defaults.SmoothLineEdgeShape", "Smooth Line"), LINE("org.graffiti.plugins.views.defaults.PolyLineEdgeShape",
							"Segmented Line");
		
		private final String shape;
		private final String name;
		
		Shapes(String shape, String name) {
			this.shape = shape;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
	}
}