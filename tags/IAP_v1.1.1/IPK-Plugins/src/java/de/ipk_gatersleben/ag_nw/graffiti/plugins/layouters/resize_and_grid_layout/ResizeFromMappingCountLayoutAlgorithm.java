/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.resize_and_grid_layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

/**
 * @author Christian Klukas
 */

public class ResizeFromMappingCountLayoutAlgorithm extends AbstractAlgorithm {
	private double targetSizeX = 120;
	
	private double targetSizeY = 120;
	
	public String getName() {
		return null; // "Resize Nodes dep. on attribute values";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
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
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("The graph instance may not be null.");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	/**
	 * Computes node coordinates and sets these coordinates
	 */
	public void execute() {
		List<Node> nodes = GraphHelper.getSelectedOrAllNodes(selection, graph);
		
		HashMap<Node, Vector2d> nodes2newNodeSize = new HashMap<Node, Vector2d>();
		
		for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
			Node n = it.next();
			
			ExperimentInterface mappedDataList = Experiment2GraphHelper.
								getMappedDataListFromGraphElement(n);
			
			int mapCount = 0;
			if (mappedDataList != null)
				mapCount = mappedDataList.size();
			if (mapCount >= 1)
				nodes2newNodeSize.put(n, new Vector2d(targetSizeX, targetSizeY * mapCount));
		}
		GraphHelper.applyUndoableNodeSizeUpdate(nodes2newNodeSize, getName());
		Selection sel = new Selection(nodes);
	}
	
	/**
	 * Parameters
	 * 
	 * @return parameters
	 */
	@Override
	public Parameter[] getParameters() {
		DoubleParameter widthParam = new DoubleParameter("Node width",
							"The new width of the selected (or all) nodes.");
		
		DoubleParameter heightParam = new DoubleParameter("Node height",
							"The new height.");
		
		widthParam.setDouble(targetSizeX);
		heightParam.setDouble(targetSizeY);
		
		return new Parameter[] { widthParam, heightParam };
	}
	
	/**
	 * Sets parameters
	 * 
	 * @param params
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		targetSizeX = ((DoubleParameter) params[0]).getDouble().doubleValue();
		targetSizeY = ((DoubleParameter) params[1]).getDouble().doubleValue();
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
}
