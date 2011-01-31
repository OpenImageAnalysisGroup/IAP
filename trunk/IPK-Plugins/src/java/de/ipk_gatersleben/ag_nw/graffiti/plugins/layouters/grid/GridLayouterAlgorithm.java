/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid;

import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

/**
 * Gridlayouter algorithm: Performs a layout where every node is placed on a
 * position of a grid.
 * 
 * @author Falk Schreiber, Christian Klukas
 */

/* TODO: Layout of non-horizontal/vertical grids */
public class GridLayouterAlgorithm
					extends AbstractAlgorithm {
	/*************************************************************/
	/* Member variables */
	/*************************************************************/
	
	/**
	 * Horizontal distance between nodes
	 */
	private double xDistance = 10;
	
	/**
	 * Vertical distance between nodes
	 */
	private double yDistance = 10;
	
	/**
	 * width/height ratio
	 */
	private double widthHeightRatio = 1;
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name
	 */
	public String getName() {
		return "Grid (no node resize)";
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	/**
	 * Checks parameters
	 * 
	 * @throws PreconditionException
	 *            if xDistance, yDistance or
	 *            widthHeightRatio is smaller than 0 or the graph does not
	 *            exists
	 */
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
		}
		
		if ((xDistance < 0) | (yDistance < 0)) {
			errors.add("The distance (x or y) may not be negative.");
		}
		
		if (widthHeightRatio <= 0) {
			errors.add("The widht/height ratio may not be negative.");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
		
		if (graph.getNumberOfNodes() <= 0) {
			throw new PreconditionException("The graph is empty. Cannot run layouter.");
		}
		
	}
	
	/**
	 * Computes node coordinates and sets these coordinates
	 */
	public void execute() {
		Collection<Node> workNodes = getSelectedOrAllNodes();
		
		try {
			graph.getListenerManager().transactionStarted(this);
			layoutOnGrid(workNodes, widthHeightRatio, xDistance, yDistance);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	public static void layoutOnGrid(Collection<Node> workNodes, double widthHeightRatio, double xDistance, double yDistance) {
		workNodes = GraphHelper.getVisibleNodes(workNodes);
		int numberOfNodes = workNodes.size();
		if (numberOfNodes <= 0)
			return;
		int nodesOnLine = (int) (Math.sqrt(--numberOfNodes));
		int nodeLines = (int) (Math.sqrt(numberOfNodes));
		
		/*
		 * Computes the number of nodes on each grid line under
		 * consideration of the given width/heigth ratio
		 */
		nodesOnLine *= widthHeightRatio;
		if (nodesOnLine == 0) {
			nodesOnLine = 1;
		}
		int i = 0;
		int j = 0;
		
		Vector2d ctr = NodeTools.getCenter(workNodes);
		double xStart = ctr.x - (nodesOnLine * xDistance / 2d);
		double yStart = ctr.y - (nodeLines * yDistance / 2d);
		
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		
		HashMap<Integer, Double> maxWidthInColumn = new HashMap<Integer, Double>();
		HashMap<Integer, Double> maxHeightInRow = new HashMap<Integer, Double>();
		
		for (Node n : workNodes) {
			Vector2d sz = AttributeHelper.getSize(n);
			if (!maxWidthInColumn.containsKey(i))
				maxWidthInColumn.put(i, sz.x);
			if (!maxHeightInRow.containsKey(j))
				maxHeightInRow.put(j, sz.y);
			
			if (maxWidthInColumn.get(i) < sz.x)
				maxWidthInColumn.put(i, sz.x);
			
			if (maxHeightInRow.get(j) < sz.y)
				maxHeightInRow.put(j, sz.y);
			
			i++;
			if (i > nodesOnLine) {
				j++;
				i = 0;
			}
		}
		
		i = 0;
		j = 0;
		
		for (Node n : workNodes) {
			/* Compute the new node coordinates */
			double wid = 0;
			for (int k = 0; k < i; k++)
				wid += maxWidthInColumn.get(k);
			double hei = 0;
			for (int k = 0; k < j; k++)
				hei += maxHeightInRow.get(k);
			
			wid += maxWidthInColumn.get(i) / 2;
			hei += maxHeightInRow.get(j) / 2;
			
			double newX = xStart + xDistance * i + wid;
			double newY = yStart + yDistance * j + hei;
			
			nodes2newPositions.put(n, new Vector2d(newX, newY));
			
			i++;
			if (i > nodesOnLine) {
				j++;
				i = 0;
			}
		}
		
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, "Grid Layout");
	}
	
	/**
	 * Parameters
	 * 
	 * @return parameters
	 */
	@Override
	public Parameter[] getParameters() {
		DoubleParameter xDistanceParam =
							new DoubleParameter("x distance",
												"The distance between nodes in horizontal direction.");
		
		DoubleParameter yDistanceParam =
							new DoubleParameter("y distance",
												"The distance between nodes in vertical direction.");
		
		DoubleParameter widthHeightRatioParam =
							new DoubleParameter("width/heigt ratio",
												"The ratio between the width and the height of the layout.");
		
		xDistanceParam.setDouble(xDistance);
		yDistanceParam.setDouble(yDistance);
		
		widthHeightRatioParam.setDouble(widthHeightRatio);
		
		return new Parameter[] {
							xDistanceParam, yDistanceParam, // xStartParam, yStartParam,
				widthHeightRatioParam };
	}
	
	/**
	 * Sets parameters
	 * 
	 * @param params
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		int i = 0;
		xDistance = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		yDistance = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		widthHeightRatio = ((DoubleParameter) params[i++]).getDouble().doubleValue();
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
}
