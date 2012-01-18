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
 * @author Dirk Kosch�tzki, Christian Klukas
 */
public class CircleLayouterAlgorithm extends AbstractAlgorithm {
	
	private double defaultRadius = 150;
	private boolean minimzeCrossings;
	private boolean useSelection;
	
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
	
	public static void layoutOnCircles(Collection<Node> workNodes, double defaultRadius, String operationname) {
		
		workNodes = GraphHelper.getVisibleNodes(workNodes);
		
		int numberOfNodes = workNodes.size();
		double singleStep = 2 * Math.PI / numberOfNodes;
		
		Vector2d ctr = NodeTools.getCenter(workNodes);
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		
		int i = 0;
		for (Node n : workNodes) {
			double newX = Math.sin(Math.PI + singleStep * i) * defaultRadius + ctr.x;
			double newY = Math.cos(Math.PI + singleStep * i) * defaultRadius + ctr.y;
			nodes2newPositions.put(n, new Vector2d(newX, newY));
			i++;
		}
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, operationname);
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
		
		// BooleanParameter useSelectionParam = new BooleanParameter(useSelection,
		// "Work on Selection", "Do the layout for the selected nodes");
		//
		return new Parameter[] { radiusParam, useSelectionParam,
							new BooleanParameter(minimzeCrossings, "Minimize Crossings", "If checked, the edge crossings will be minimzed") };
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
}
