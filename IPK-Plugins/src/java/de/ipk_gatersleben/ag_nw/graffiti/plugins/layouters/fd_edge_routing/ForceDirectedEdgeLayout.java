/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 15.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.fd_edge_routing;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.myOp;

public class ForceDirectedEdgeLayout extends AbstractAlgorithm {
	
	double paramMinDistancePercent, paramForce, paramSegementLength, paramLayoutLength;
	int paramMinimumBendCount = 0;
	
	public String getName() {
		return "Edge-Routing (force-directed)";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active graph");
		if (graph.getNumberOfEdges() <= 0)
			throw new PreconditionException("Graph contains no edges!");
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new DoubleParameter(40, "Segement Length", "Length of segments between edge bends"),
							new DoubleParameter(20, "Target Length (Layout)", "Target length for force directed layout"),
							new DoubleParameter(5000, "Repulsion Force (Layout)", "Repulsive force applied to edge bend points for layout"),
							new DoubleParameter(0.01, "Minimum Distance (Percent)",
												"Minimum distance (in percent of edge length) for bend distance to direct line betweeen nodes"),
							new IntegerParameter(0, 0, Integer.MAX_VALUE, "Minimum Bend Count",
												"If specified (>0), only edges with the specified possible bend count (dependent on edge length) will be processed") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		paramSegementLength = ((DoubleParameter) params[i++]).getDouble();
		paramLayoutLength = ((DoubleParameter) params[i++]).getDouble();
		paramForce = ((DoubleParameter) params[i++]).getDouble();
		paramMinDistancePercent = ((DoubleParameter) params[i++]).getDouble();
		paramMinimumBendCount = ((IntegerParameter) params[i++]).getInteger();
	}
	
	@SuppressWarnings("unchecked")
	public void execute() {
		HashMap<Edge, ArrayList<Node>> oldEdge2newNodes = new HashMap<Edge, ArrayList<Node>>();
		ArrayList<Node> borderNodes = new ArrayList<Node>();
		boolean selectLines = selection.getEdges().size() > 0;
		try {
			graph.getListenerManager().transactionStarted(this);
			
			ArrayList<Node> allNodes = new ArrayList<Node>(graph.getNodes());
			for (Node n : allNodes) {
				Vector2d pos = AttributeHelper.getPositionVec2d(n);
				Vector2d size = AttributeHelper.getSize(n);
				Node tl = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x - size.x / 2, pos.y - size.y / 2));
				Node tr = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x + size.x / 2, pos.y - size.y / 2));
				Node bl = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x - size.x / 2, pos.y + size.y / 2));
				Node br = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x + size.x / 2, pos.y + size.y / 2));
				Node l = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x - size.x / 2, pos.y));
				Node r = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x + size.x / 2, pos.y));
				Node t = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x, pos.y - size.y / 2));
				Node b = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x, pos.y + size.y / 2));
				borderNodes.add(tl);
				borderNodes.add(tr);
				borderNodes.add(bl);
				borderNodes.add(br);
				borderNodes.add(l);
				borderNodes.add(r);
				borderNodes.add(t);
				borderNodes.add(b);
			}
			
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				if (!(ge instanceof Edge))
					continue;
				Edge e = (Edge) ge;
				double length = getLength(e);
				int bends = (int) (length / paramSegementLength) - 2;
				if (bends < 1 && paramMinimumBendCount < 1)
					bends = 1;
				if (bends < paramMinimumBendCount)
					bends = 0;
				if (e.getSource() == e.getTarget())
					bends = 2;
				
				// System.out.println("D="+length+", B="+bends);
				AttributeHelper.removeEdgeBends(e);
				if (bends > 0) {
					for (Node n : createEdgeBendNodes(e, bends)) {
						if (!oldEdge2newNodes.containsKey(e))
							oldEdge2newNodes.put(e, new ArrayList<Node>());
						oldEdge2newNodes.get(e).add(n);
					}
					graph.deleteEdge(e);
				}
			}
			
			ArrayList<Node> bendNodes = new ArrayList<Node>();
			for (ArrayList<Node> nodeList : oldEdge2newNodes.values())
				bendNodes.addAll(nodeList);
			
			Selection selection = new Selection("bend layout", bendNodes);
			try {
				ThreadSafeOptions tso = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
				tso.temp_alpha = 0.96;
				tso.setDval(myOp.DvalIndexSliderZeroLength, paramLayoutLength);
				tso.setDval(myOp.DvalIndexSliderHorForce, paramForce);
				tso.setDval(myOp.DvalIndexSliderVertForce, paramForce);
				tso.setDval(myOp.DvalIndexSliderStiffness, 15);
				MyNonInteractiveSpringEmb se = new MyNonInteractiveSpringEmb(graph, selection, tso);
				se.run();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} finally {
			for (Node n : borderNodes)
				AttributeHelper.setSize(n, 1, 1);
			
			ArrayList<Edge> newEdges = new ArrayList<Edge>();
			for (Edge e : oldEdge2newNodes.keySet()) {
				Edge newEdge = graph.addEdgeCopy(e, e.getSource(), e.getTarget());
				newEdges.add(newEdge);
				Collection<Vector2d> points = new ArrayList<Vector2d>();
				Rectangle2D.Double rs = AttributeHelper.getNodeRectangle(newEdge.getSource());
				Rectangle2D.Double rt = AttributeHelper.getNodeRectangle(newEdge.getTarget());
				Vector2d start = AttributeHelper.getPositionVec2d(newEdge.getSource());
				Vector2d end = AttributeHelper.getPositionVec2d(newEdge.getTarget());
				Line2D.Double line = new Line2D.Double(start.x, start.y, end.x, end.y);
				double lineLength = start.distance(end);
				for (Node n : oldEdge2newNodes.get(e)) {
					Vector2d p = AttributeHelper.getPositionVec2d(n);
					if (newEdge.getSource() == newEdge.getTarget()) {
						points.add(p);
					} else {
						if (!rs.contains(p.x, p.y) && !rt.contains(p.x, p.y)) {
							double dist = line.ptLineDist(p.x, p.y);
							if (dist > lineLength * paramMinDistancePercent)
								points.add(p);
						}
					}
					graph.deleteNode(n);
				}
				if (points.size() > 0) {
					// AttributeHelper.setEdgeBendStyle(newEdge, "Poly");
					AttributeHelper.setEdgeBendStyle(newEdge, "Smooth");
				} else
					AttributeHelper.setEdgeBendStyle(newEdge, "");
				AttributeHelper.addEdgeBends(newEdge, points);
				if (selectLines) {
					GraphHelper.selectElements((Collection) newEdges);
				}
			}
			for (Node n : borderNodes)
				graph.deleteNode(n);
			graph.getListenerManager().transactionFinished(this, true);
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
	}
	
	private Collection<Node> createEdgeBendNodes(Edge e, int bends) {
		ArrayList<Node> result = new ArrayList<Node>();
		if (e.getGraph() == null)
			return result;
		int off = 5;
		Vector2d p1 = AttributeHelper.getPositionVec2d(e.getSource());
		Vector2d p2 = AttributeHelper.getPositionVec2d(e.getTarget());
		Vector2d p = new Vector2d(p1, p2);
		Node lastNode = e.getSource();
		for (int i = 0; i < bends; i++) {
			p.x += off;
			off += 5;
			Node n = e.getGraph().addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(p.x, p.y));
			AttributeHelper.setSize(n, 4, 4);
			e.getGraph().addEdge(lastNode, n, false);
			lastNode = n;
			result.add(n);
		}
		e.getGraph().addEdge(lastNode, e.getTarget(), false);
		return result;
	}
	
	private double getLength(Edge e) {
		Vector2d p1, p2;
		p1 = AttributeHelper.getPositionVec2d(e.getSource());
		p2 = AttributeHelper.getPositionVec2d(e.getTarget());
		double d = p1.distance(p2);
		d -= AttributeHelper.getSize(e.getSource()).maxXY() / 2;
		d -= AttributeHelper.getSize(e.getTarget()).maxXY() / 2;
		if (d < 0)
			d = 0;
		return d;
	}
}
