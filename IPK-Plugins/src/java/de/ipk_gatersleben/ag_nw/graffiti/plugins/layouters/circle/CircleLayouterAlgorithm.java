/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.services.AlgorithmServices;

/**
 * Places all nodes on a circle with a user specified radius.
 * 
 * @author Dirk Koschützki, Christian Klukas, Matthias Klapperstueck
 */
public class CircleLayouterAlgorithm extends AbstractAlgorithm {
	
	private double defaultRadius = 150;
	private boolean minimzeCrossings;
	private boolean useSelection;
	private boolean equalize = true;
	private boolean averageCenterLength = true;
	/**
	 * Creates a new CircleLayouterAlgorithm object.
	 */
	public CircleLayouterAlgorithm() {
		super();
	}
	
	/**
	 * Creates a new CircleLayouterAlgorithm object.
	 * 
	 * @param defaultRadius
	 *           a value for the radius
	 */
	public CircleLayouterAlgorithm(double defaultRadius) {
		super();
		this.defaultRadius = defaultRadius;
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
		return "Circle";
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
	
	public void setRadius(double radius) {
		this.defaultRadius = radius;
	}
	
	/**
	 * Performs the layout.
	 */
	public void execute() {
		if (!minimzeCrossings)
			withoutMinimizingCrossings();
		else
			withMinimizingCrossings();
		
	}
	
	private void withoutMinimizingCrossings() {
		
		Collection<Node> workNodes = new ArrayList<Node>();
		if (selection.getNodes().size() > 0)
			workNodes.addAll(selection.getNodes());
		else
			workNodes.addAll(graph.getNodes());
		
		layoutOnCircles(workNodes, defaultRadius, getName());
	}
	
	public void withMinimizingCrossings() {
		// EditorSession session = GravistoService.getInstance().getMainFrame()
		// .getActiveEditorSession();
		
		Collection<Node> workNodes = new ArrayList<Node>();
		if (useSelection)
			workNodes.addAll(selection.getNodes());
		else
			workNodes.addAll(graph.getNodes());
		
		final Vector2d ctr = NodeTools.getCenter(workNodes);
		
		int numberOfNodes = workNodes.size();
		final double singleStep = 2 * Math.PI / numberOfNodes;
		
		final Graph workGraph = graph;
		final ArrayList<Node> sortedNodes = new ArrayList<Node>();
		sortedNodes.addAll(workNodes);
		
		AlgorithmServices.doCircularEdgeCrossingsMinimization(this, sortedNodes,
							new Runnable() {
								public void run() {
									if (sortedNodes == null || workGraph == null
														|| workGraph.getListenerManager() == null)
										return;
									HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
									int iMinEnergy = 0;
									double minEnergy = Double.MAX_VALUE;
									for (int testI = 0; testI < sortedNodes.size(); testI++) {
										double energy = 0;
										int i = 0;
										for (Node n : sortedNodes) {
											double newX = Math.sin(singleStep * (testI + i)) * defaultRadius + ctr.x;
											double newY = Math.cos(singleStep * (testI + i)) * defaultRadius + ctr.y;
											energy += CircleLayouterAlgorithm.energyOfNode(n, newX, newY);
											i++;
										}
										if (energy < minEnergy) {
											minEnergy = energy;
											iMinEnergy = testI;
										}
									}
									int i = iMinEnergy;
									for (Node n : sortedNodes) {
										double newX = Math.sin(singleStep * i) * defaultRadius
															+ ctr.x;
										double newY = Math.cos(singleStep * i) * defaultRadius
															+ ctr.y;
										
										nodes2newPositions.put(n, new Vector2d(newX, newY));
										i = i + 1;
									}
									GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());
								}
							});
	}
	
	public void layoutOnCircles(Collection<Node> workNodes, double defaultRadius, String operationname) {
		

		workNodes = GraphHelper.getVisibleNodes(workNodes);

		int numberOfNodes = workNodes.size();
		if(numberOfNodes < 2)
			return;
		
		double singleStep = 2 * Math.PI / numberOfNodes;

		Vector2d ctr = NodeTools.getCenter(workNodes);
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		
		
		double avgDist = 0;
		int i = 0;
		if(averageCenterLength){
			for (Node n : workNodes) {
				avgDist += getDistance(ctr.x, ctr.y, n);
				i++;
			}
			avgDist /= i;
		}
		else
			avgDist = defaultRadius;
		
		if( equalize ){
			i = 0;
			Collection<Node> orderedNodes = createCircleOrder(workNodes);
			Node startNode = orderedNodes.iterator().next();
			double startangle = getAngle(ctr, startNode);
			for(Node n : orderedNodes){
//				double x = AttributeHelper.getPositionX(n); //debugging
//				double y = AttributeHelper.getPositionY(n); //debugging
				double newX = Math.cos(startangle + singleStep * i) * avgDist + ctr.x;
				double newY = Math.sin(startangle + singleStep * i) * avgDist + ctr.y;
				nodes2newPositions.put(n, new Vector2d(newX, newY));
				i++;
			}
		} else {
			for (Node n : workNodes) {
				double x = AttributeHelper.getPositionX(n);
				double y = AttributeHelper.getPositionY(n);
				double dist = getDistance(ctr.x, ctr.y, n);
				double factor = avgDist / dist;
				Vector2d direction = new Vector2d(x-ctr.x , y- ctr.y);
				double newX = ctr.x + factor*direction.x;
				double newY = ctr.y + factor*direction.y;
				nodes2newPositions.put(n, new Vector2d(newX, newY));
				i++;
			}
		}
		/* correct node positions, if graph is in negative view space */
		double minx = 0;
		double miny = 0;
		for(Node n : nodes2newPositions.keySet()){
			Vector2d curVector = nodes2newPositions.get(n);
			if(curVector.x - AttributeHelper.getWidth(n) < minx)
				minx = curVector.x - AttributeHelper.getWidth(n);
			if(curVector.y - AttributeHelper.getWidth(n)< miny)
				miny = curVector.y - AttributeHelper.getWidth(n);
			
		}
		if(minx < 0 || miny < 0){
			/* abs. values of minx and miny */
			minx = Math.abs(minx);
			miny = Math.abs(miny);
			for(Node n : nodes2newPositions.keySet()){
				Vector2d curVector = nodes2newPositions.get(n);
				curVector.x += minx;
				curVector.y += miny;
				
			}
		}
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions,
							getName());
	}

	public static Collection<Node> createCircleOrder(Collection<Node> nodes){
		Vector2d ctr = NodeTools.getCenter(nodes);
		
		ArrayList<Node> orderedNodes = new ArrayList<Node>();
		ArrayList<AngleNode> angleNodes = new ArrayList<AngleNode>();
		
//		Vector2d baseVector = new Vector2d(100,0);
		for (Node n : nodes) {
			String label = AttributeHelper.getLabel(n, null);
			double angle = getAngle(ctr, n);
			if(angleNodes.size() == 0)
				angleNodes.add(new AngleNode(angle, n));
			else {
				int c = 0;
				for(; c < angleNodes.size(); c++){
					if(angleNodes.get(c).angle >= angle)
						break;
				}
				if(c < angleNodes.size())
					angleNodes.add(c, new AngleNode(angle, n));
				else
					angleNodes.add(new AngleNode(angle, n));
			}
		}
		for(AngleNode an : angleNodes)
			orderedNodes.add(an.node);
		return orderedNodes;
	}
	
	static double getAngle(Vector2d ctr, Node n){
		double x = AttributeHelper.getPositionX(n);
		double y = AttributeHelper.getPositionY(n);
		double lengthNodeToCenter = getDistance(ctr.x, ctr.y, n);
		Vector2d direction = new Vector2d(x-ctr.x , y- ctr.y);
		double angle;
		if(direction.y <0 )
			angle = -Math.acos(direction.x / lengthNodeToCenter);
		else
			angle = Math.acos(direction.x / lengthNodeToCenter);
		return angle;
	}
	static double getDotProduct(Vector2d a, Vector2d b){
		double val = 0;
		val = a.x*b.x + a.y* b.y;
		return val;
	}

	
	public static double energyOfNode(Node node, double newX, double newY) {
		double distanceToOtherNodes = 0;
		for (Node n : node.getNeighbors())
			distanceToOtherNodes += getDistance(newX, newY, n);
		return distanceToOtherNodes;
	}
	
	public static double getDistance(double x, double y, Node b) {
		Vector2d posA = new Vector2d(x, y);
		Vector2d posB = AttributeHelper.getPositionVec2d(b);
		return Math.sqrt((posA.x - posB.x) * (posA.x - posB.x) + (posA.y - posB.y) * (posA.y - posB.y));
	}
	
	/**
	 * Returns the parameter object for the radius.
	 * 
	 * @return the parameter array
	 */
	@Override
	public Parameter[] getParameters() {
		// if (defaultRadius == -1) {
		DoubleParameter radiusParam = new DoubleParameter("Radius",
							"The radius of the circle.");
		
		radiusParam.setDouble(defaultRadius);
		
		BooleanParameter useSelectionParam = new BooleanParameter(useSelection,
							"Work on Selection", "Do the layout for the selected nodes");
		
		BooleanParameter equalizeParam = new BooleanParameter(true, "Equalize", "Equalize distance between nodes on the circle");
		BooleanParameter avgDistBoolean = new BooleanParameter(false, "Average Radius", "This parameter overrules the 'Radius' parameter. It calculates the center of all nodes, \n" +
				"selected for layout and calculates then the average distance to every node,\n" +
				"which is then taken as final radius for the circle");

		// BooleanParameter useSelectionParam = new BooleanParameter(useSelection,
		// "Work on Selection", "Do the layout for the selected nodes");
		//
		return new Parameter[] { radiusParam, useSelectionParam,
							new BooleanParameter(minimzeCrossings, "Minimize Crossings", "If checked, the edge crossings will be minimzed"),
							equalizeParam,
							avgDistBoolean};
	}
	
	/**
	 * Sets the radius parameter to the given value.
	 * 
	 * @param params
	 *           An array with exact one DoubleParameter.
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		defaultRadius = ((DoubleParameter) params[0]).getDouble().doubleValue();
		useSelection = ((BooleanParameter) params[1]).getBoolean();
		minimzeCrossings = ((BooleanParameter) params[2]).getBoolean();
		equalize = ((BooleanParameter) params[3]).getBoolean().booleanValue();
		averageCenterLength = ((BooleanParameter) params[4]).getBoolean().booleanValue();
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
	
	private double patternNodeDistance = 50;
	
	public double getPatternNodeDistance() {
		return patternNodeDistance;
	}
	
	public void setPatternNodeDistance(double patternNodeDistance) {
		this.patternNodeDistance = patternNodeDistance;
	}
	
	static class AngleNode{
		public double angle;
		public Node node;
		
		public AngleNode(double angle, Node node) {
			this.angle = angle;
			this.node = node;
		}
	}
}
