/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.expand_no_overlapp;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * Epands the layout to remove any overlapping of nodes.
 * 
 * @author Christian Klukas
 */
public class NoOverlappLayoutAlgorithm extends AbstractAlgorithm {
	
	private double space = 10;
	private boolean doNotAskForParameters = false;
	private boolean layoutFirstX = false;
	private boolean layoutFirstY = false;
	private boolean layoutXY = true;
	
	// private boolean considerViewComponents = true;
	
	public NoOverlappLayoutAlgorithm() {
		super();
		doNotAskForParameters = false;
	}
	
	public NoOverlappLayoutAlgorithm(int space, boolean doFirstX, boolean doFirstY, boolean doXY) {
		doNotAskForParameters = true;
		this.space = space;
		this.layoutFirstX = doFirstX;
		this.layoutFirstY = doFirstY;
		this.layoutXY = doXY;
	}
	
	@Override
	public void reset() {
		super.reset();
		space = 10d;
		layoutFirstX = true;
		layoutFirstY = true;
		// considerViewComponents = true;
	}
	
	public String getName() {
		return "Remove Node Overlaps (simple)";
	}
	
	@Override
	public String getDescription() {
		return "<html>Simple algorithm to remove node overlap, which also works for nodes with the same position.";
	}
	
	/**
	 * Checks, if a graph was given and that the radius is positive.
	 * 
	 * @throws PreconditionException
	 *            if no graph was given during algorithm
	 *            invocation or the radius is negative
	 */
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
	
	/**
	 * Performs the layout.
	 */
	@SuppressWarnings("unchecked")
	public void execute() {
		Collection<Node> workNodesC = getSelectedOrAllNodes();
		
		Node[] workNodes = workNodesC.toArray(new Node[] {});
		
		HashMap<Node, Vector2d> nodePositionOld = new HashMap<Node, Vector2d>();
		final HashMap<Node, Vector2d> nodePosition = new HashMap<Node, Vector2d>();
		HashMap<Node, Vector2d> nodeSize = new HashMap<Node, Vector2d>();
		
		// retrieve position and size of nodes
		for (Node n : workNodes) {
			nodePositionOld.put(n, AttributeHelper.getPositionVec2d(n));
			nodePosition.put(n, AttributeHelper.getPositionVec2d(n));
			nodeSize.put(n, AttributeHelper.getSize(n));
		}
		// sort nodes depending on the distance to the position 0:0
		java.util.Arrays.sort(workNodes, new Comparator() {
			public int compare(Object o1, Object o2) {
				Vector2d p1 = nodePosition.get(o1);
				Vector2d p2 = nodePosition.get(o2);
				return (p1.y * p1.y < p2.y * p2.y) || (p1.x * p1.x < p2.x * p2.x) ? -1 : 1;
			}
		});
		// calculate new positions
		
		if (!layoutFirstX && !layoutFirstY)
			doLayout(true, true, workNodes, nodePositionOld, nodePosition, nodeSize);
		else {
			doLayout(layoutFirstX, layoutFirstY, workNodes, nodePositionOld, nodePosition, nodeSize);
			doLayout(!layoutFirstX, !layoutFirstY, workNodes, nodePositionOld, nodePosition, nodeSize);
		}
		
		GraphHelper.applyUndoableNodePositionUpdate(nodePosition, getName());
	}
	
	private void doLayout(boolean doX, boolean doY,
						Node[] workNodes, HashMap<Node, Vector2d> nodePositionOld,
						HashMap<Node, Vector2d> nodePosition,
						HashMap<Node, Vector2d> nodeSize) {
		for (Node n1 : workNodes) {
			Vector2d p1 = nodePosition.get(n1);
			Vector2d s1 = nodeSize.get(n1);
			for (Node n2 : workNodes) {
				if (n2 == n1)
					continue;
				Vector2d p2 = nodePosition.get(n2);
				Vector2d s2 = nodeSize.get(n2);
				if (horOverlapp(p1, p2, s1, s2, space) && vertOverlapp(p1, p2, s1, s2, space)) {
					if (p2.y >= p1.y || p2.x >= p1.x) {
						checkOverlapp(p1, s1, n2, p2, s2,
											n1, workNodes, nodePositionOld, nodePosition,
											doX, doY);
					}
				}
			}
		}
	}
	
	private void checkOverlapp(Vector2d p1, Vector2d s1, Node n2, Vector2d p2, Vector2d s2, Node n1,
						Node[] workNodes, HashMap<Node, Vector2d> nodePositionOld, HashMap<Node, Vector2d> nodePosition,
						boolean layoutX, boolean layoutY) {
		double vy = getVertOverlapp(p1, p2, s1, s2, space);
		double vx = getHorOverlapp(p1, p2, s1, s2, space);
		// System.out.println("Y Overl.:"+getInfo(n1,p1,s1)+" and "+getInfo(n2,p2,s2)+": vy="+vy);
		for (Node n3 : workNodes) {
			if (n3 == n1)
				continue;
			Vector2d p3 = nodePosition.get(n3);
			if (p2.x >= p1.x && p3.x >= p1.x && layoutX)
				p3.x = p3.x + vx;
			if (p2.y >= p1.y && p3.y >= p1.y && layoutY)
				p3.y = p3.y + vy;
		}
	}
	
	private double getHorOverlapp(Vector2d p1, Vector2d p2, Vector2d s1, Vector2d s2, double space) {
		double minDist = (s1.x / 2d + s2.x / 2d + space);
		double dist = Math.abs(p1.x - p2.x);
		return dist < minDist ? minDist - dist : 0;
	}
	
	private double getVertOverlapp(Vector2d p1, Vector2d p2, Vector2d s1, Vector2d s2, double space) {
		double minDist = (s1.y / 2d + s2.y / 2d + space);
		double dist = Math.abs(p1.y - p2.y);
		return dist < minDist ? minDist - dist : 0;
	}
	
	private boolean horOverlapp(Vector2d p1, Vector2d p2, Vector2d s1, Vector2d s2, double space) {
		return getHorOverlapp(p1, p2, s1, s2, space) > 0;
	}
	
	private boolean vertOverlapp(Vector2d p1, Vector2d p2, Vector2d s1, Vector2d s2, double space) {
		return getVertOverlapp(p1, p2, s1, s2, space) > 0;
	}
	
	/**
	 * Returns the parameter object for the radius.
	 * 
	 * @return the parameter array
	 */
	@Override
	public Parameter[] getParameters() {
		if (doNotAskForParameters)
			return null;
		DoubleParameter spaceParam = new DoubleParameter(space, "Gap between Nodes", "Specify the minimum space between all nodes");
		
		BooleanParameter firstXparam = new BooleanParameter(layoutFirstX || layoutXY,
							"First expand horizontally", "If this is selected, the horizontal overlapping will be removed, first");
		BooleanParameter firstYparam = new BooleanParameter(layoutFirstY || layoutXY,
							"First expand vertically", "If this is selected, the vertical overlapping will be removed, first");
		// BooleanParameter considerView = new BooleanParameter(considerViewComponents, "Consider View Components",
		// "If enabled, graphical annotations, like the node labels will be considered and processed - Enable");
		
		return new Parameter[] { spaceParam, firstXparam, firstYparam, /* considerView /*, debugParam */};
	}
	
	/**
	 * Sets the radius parameter to the given value.
	 * 
	 * @param params
	 *           An array with exact one DoubleParameter.
	 */
	@Override
	public void setParameters(Parameter[] params) {
		if (doNotAskForParameters)
			return;
		this.parameters = params;
		int i = 0;
		space = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		boolean b1x = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		boolean b2y = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		if (b1x && b2y) {
			layoutFirstX = false;
			layoutFirstY = false;
			layoutXY = true;
		} else {
			layoutFirstX = b1x;
			layoutFirstY = b2y;
			layoutXY = false;
		}
		// considerViewComponents = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
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
