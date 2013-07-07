/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.resize_and_grid_layout;

import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;

/**
 * Resize+Gridlayout: Resizes all nodes and places them on a grid.
 * 
 * @author Christian Klukas
 */

public class GridLayoutAlgorithm
					extends AbstractAlgorithm {
	private double xDistance = 130;
	private double yDistance = 130;
	
	private double targetSizeX = 120;
	private double targetSizeY = 120;
	
	private boolean moveToTop = true;
	
	private boolean setWidth = false;
	private int maxX = 1;
	
	private double widthHeightRatio = 1;
	private boolean resize = true;
	
	public String getName() {
		return "Grid Layout";
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null)
			errors.add("The graph instance may not be null.");
		
		if (!errors.isEmpty())
			throw errors;
		
		if (graph.getNumberOfNodes() <= 0)
			errors.add("The graph is empty. Cannot run layouter.");
		
		if (setWidth && maxX < 1)
			maxX = 1;
		
		if (widthHeightRatio <= 0) {
			errors.add("The widht/height ratio may not be negative.");
		}
		
		if (!errors.isEmpty())
			throw errors;
	}
	
	public void execute() {
		if (resize)
			executeWithResize();
		else
			executeWithoutResize();
	}
	
	public void executeWithResize() {
		Collection<Node> nodes = GraphHelper.getSelectedOrAllNodes(selection, graph);
		nodes = GraphHelper.getVisibleNodes(nodes);
		if (nodes.size() < 1)
			return;
		int numberOfNodes = nodes.size();
		int nodesOnLine = (int) (Math.sqrt(numberOfNodes));
		if (setWidth)
			nodesOnLine = maxX;
		
		int column = 0;
		int row = 0;
		
		if (nodesOnLine <= 0) {
			nodesOnLine = 1;
		}
		int rows = nodes.size() / nodesOnLine;
		if (rows < 1)
			rows = 1;
		
		Vector2d center = NodeTools.getCenter(nodes);
		double xStart = center.x - xDistance * (nodesOnLine / 2d) + xDistance / 2d;
		double yStart = center.y - yDistance * (rows / 2d) + yDistance / 2d;
		
		MainFrame.showMessage("Grid-Layout: columns: " + nodesOnLine + ", rows: " + rows + ", inital avergage node position (x/y): " + center.x + " / "
							+ center.y, MessageType.INFO);
		HashMap<Node, Vector2d> nodes2newPosition = new HashMap<Node, Vector2d>();
		HashMap<Node, Vector2d> nodes2newSize = new HashMap<Node, Vector2d>();
		for (Node n : nodes) {
			/* Compute the new node coordinates */
			double newX = xStart + xDistance * column;
			double newY = yStart + yDistance * row;
			
			nodes2newPosition.put(n, new Vector2d(newX, newY));
			nodes2newSize.put(n, new Vector2d(targetSizeX, targetSizeY));
			
			column++;
			if (column >= nodesOnLine) {
				row++;
				column = 0;
			}
		}
		GraphHelper.applyUndoableNodePositionAndSizeUpdate(nodes2newPosition, nodes2newSize, getName());
		Selection sel = new Selection();
		sel.addAll(nodes);
		if (moveToTop)
			GravistoService.getInstance().runAlgorithm(
								new CenterLayouterAlgorithm(), graph, sel, getActionEvent());
	}
	
	/**
	 * Computes node coordinates and sets these coordinates
	 */
	public void executeWithoutResize() {
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
	
	@Override
	public Parameter[] getParameters() {
		
		DoubleParameter widthHeightRatioParam =
							new DoubleParameter("width/heigt ratio",
												"The ratio between the width and the height of the layout.");
		
		widthHeightRatioParam.setDouble(widthHeightRatio);
		
		DoubleParameter xDistanceParam =
							new DoubleParameter("Horizonzal space",
												"The distance between nodes in horizontal direction.");
		
		DoubleParameter yDistanceParam =
							new DoubleParameter("Vertical space",
												"The distance between nodes in vertical direction.");
		
		DoubleParameter widthParam =
							new DoubleParameter("Node width",
												"The new width of the selected (or all) nodes.");
		
		DoubleParameter heightParam =
							new DoubleParameter("Node height",
												"The new height.");
		
		BooleanParameter limitXparameter =
							new BooleanParameter(setWidth, "Non-rectangular grid", "If selected, the Width parameter will be used to create a non-rectangular grid");
		
		IntegerParameter widthParameter =
							new IntegerParameter(maxX, "Grid-Width", "The height of the grid depends on the number of nodes and the grid width");
		
		BooleanParameter moveToTopParameter =
							new BooleanParameter(moveToTop, "Finish: Move to Upper-Left", "Move all network elements to the upper left");
		
		xDistanceParam.setDouble(xDistance - targetSizeX);
		yDistanceParam.setDouble(yDistance - targetSizeY);
		
		widthParam.setDouble(targetSizeX);
		heightParam.setDouble(targetSizeY);
		
		return new Parameter[] {
							new BooleanParameter(resize, "Resize Nodes", "If checked, the nodes will be resized when layouting"),
							xDistanceParam,
							yDistanceParam,
							widthParam,
							heightParam,
							limitXparameter,
							widthParameter,
							moveToTopParameter,
							widthHeightRatioParam, };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		int i = 0;
		resize = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		double p_xDistance = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		double p_yDistance = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		targetSizeX = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		targetSizeY = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		xDistance = p_xDistance + targetSizeX;
		yDistance = p_yDistance + targetSizeY;
		setWidth = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		maxX = ((IntegerParameter) params[i++]).getInteger().intValue();
		moveToTop = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		widthHeightRatio = ((DoubleParameter) params[i++]).getDouble().doubleValue();
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
}
