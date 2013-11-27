/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.Vector2d;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.services.AlgorithmServices;

/**
 * Places all nodes on a circle with a user specified radius.
 * 
 * @author Christian Klukas
 */
public class CircleLayouterWithMinimumCrossingsAlgorithm extends
					AbstractAlgorithm {
	
	/**
	 * The default radius.
	 */
	private double defaultRadius = 150;
	
	private boolean useSelection;
	
	/**
	 * Creates a new CircleLayouterAlgorithm object.
	 */
	public CircleLayouterWithMinimumCrossingsAlgorithm() {
		super();
	}
	
	@Override
	public void attach(Graph graph, Selection selection) {
		super.attach(graph, selection);
	}
	
	/**
	 * Creates a new CircleLayouterAlgorithm object.
	 * 
	 * @param defaultRadius
	 *           a value for the radius
	 */
	public CircleLayouterWithMinimumCrossingsAlgorithm(double defaultRadius) {
		super();
		this.defaultRadius = defaultRadius;
	}
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name of the algorithm
	 */
	public String getName() {
		return "Circle (min. crossings)";
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
		
		// if (defaultRadius < 0) {
		// errors.add("The radius may not be negative.");
		// }
		
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
	public void execute() {
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
	
	/**
	 * Returns the parameter object for the radius and the minimize edge crossings parameter.
	 * 
	 * @return the parameter array
	 */
	@Override
	public Parameter[] getParameters() {
		DoubleParameter radiusParam = new DoubleParameter("Radius",
							"The radius of the circle.");
		
		radiusParam.setDouble(defaultRadius);
		
		BooleanParameter useSelectionParam = new BooleanParameter(useSelection,
							"Work on Selection", "Do the layout for the selected nodes");
		
		return new Parameter[] { radiusParam, useSelectionParam };
	}
	
	/**
	 * Sets the radius parameter and the minimize edge crossings flag to the given values.
	 * 
	 * @param params
	 *           An array with one DoubleParameter and a BooleanParameter.
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		defaultRadius = ((DoubleParameter) params[0]).getDouble().doubleValue();
		useSelection = ((BooleanParameter) params[1]).getBoolean().booleanValue();
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
