/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.resize_and_grid_layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.FeatureSet;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Christian Klukas
 */

public class ArrayLayout extends AbstractAlgorithm {
	private double xDistance = 130;
	private double yDistance = 130;
	
	private double targetSizeX = 120;
	private double targetSizeY = 120;
	
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			return "Array XY";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public String getDescription() {
		return "<html>Places nodes according to the X/Y position info, contained in KEGG Expression dataset files.";
	}
	
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null)
			errors.add("The graph instance may not be null.");
		
		if (!errors.isEmpty())
			throw errors;
		
		if (graph.getNumberOfNodes() <= 0)
			errors.add("The graph is empty. Cannot run layouter.");
		else {
			int validNodesFound = 0;
			for (Node n : graph.getNodes()) {
				NodeHelper nh = new NodeHelper(n);
				Collection<String> info = new ArrayList<String>();
				for (SubstanceInterface m : nh.getMappings()) {
					if (m.getInfo() != null && m.getInfo().length() > 0)
						info.add(m.getInfo());
				}
				if (info != null && info.size() > 0) {
					validNodesFound++;
					break;
				}
			}
			if (validNodesFound <= 0)
				errors.add("No graph node with mapping data and valid info-annotation found.<br>"
									+ "The info-annotation is created automatically from the x/y values specified<br>"
									+ "in KEGG expression data.<br>"
									+ "The resulting layout shows simple or complex datasets in a grid-layout, <br>"
									+ "where each node is placed  at the position, specified for the particular<br>"
									+ "datapoint in the data file.");
		}
		
		if (!errors.isEmpty())
			throw errors;
	}
	
	/**
	 * Computes node coordinates and sets these coordinates
	 */
	public void execute() {
		Collection<Node> nodes = getSelectedOrAllNodes();
		ArrayList<Node> validNodes = new ArrayList<Node>();
		HashMap<Node, Vector2d> node2gridPos = new HashMap<Node, Vector2d>();
		for (Node n : nodes) {
			NodeHelper nh = new NodeHelper(n);
			Collection<String> info = new ArrayList<String>();
			for (SubstanceInterface m : nh.getMappings()) {
				if (m.getInfo() != null && m.getInfo().length() > 0) {
					info.add(m.getInfo());
				}
			}
			if (info != null && info.size() > 0) {
				String info1 = info.iterator().next();
				info1 = StringManipulationTools.htmlToUnicode(info1);
				if (info1.indexOf(":") > 0) {
					String a = info1.substring(0, info1.indexOf(":"));
					String b = info1.substring(info1.indexOf(":") + 1);
					try {
						double ai = Double.parseDouble(a.trim());
						double bi = Double.parseDouble(b.trim());
						validNodes.add(n);
						node2gridPos.put(n, new Vector2d(ai, bi));
					} catch (Exception e) {
						// empty
					}
				}
			}
		}
		
		Vector2d min = NodeTools.getMinimumXY(validNodes, 1, 0, 0, false);
		double xStart = min.x;
		double yStart = min.y;
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		HashMap<Node, Vector2d> nodes2newSize = new HashMap<Node, Vector2d>();
		for (Node n : validNodes) {
			double xi = node2gridPos.get(n).x;
			double yi = node2gridPos.get(n).y;
			
			/* Compute the new node coordinates */
			double newX = xStart + xDistance * xi;
			double newY = yStart + yDistance * yi;
			
			/* Set the new node coordinates */
			nodes2newPositions.put(n, new Vector2d(newX, newY));
			nodes2newSize.put(n, new Vector2d(targetSizeX, targetSizeY));
		}
		GraphHelper.applyUndoableNodePositionAndSizeUpdate(nodes2newPositions, nodes2newSize, "Array Layout");
		//Selection sel = new Selection(validNodes);
		MainFrame.showMessage("Info annotation found and processed from " + validNodes.size()
							+ " nodes (out of working set of " + nodes.size() + " nodes)", MessageType.INFO);
	}
	
	/**
	 * Parameters
	 * 
	 * @return parameters
	 */
	@Override
	public Parameter[] getParameters() {
		DoubleParameter xDistanceParam = new DoubleParameter("Horizonzal space",
							"The distance between nodes in horizontal direction.");
		
		DoubleParameter yDistanceParam = new DoubleParameter("Vertical space",
							"The distance between nodes in vertical direction.");
		
		DoubleParameter widthParam = new DoubleParameter("Node width", "The new width of the selected (or all) nodes.");
		
		DoubleParameter heightParam = new DoubleParameter("Node height", "The new height.");
		
		xDistanceParam.setDouble(xDistance - targetSizeX);
		yDistanceParam.setDouble(yDistance - targetSizeY);
		
		widthParam.setDouble(targetSizeX);
		heightParam.setDouble(targetSizeY);
		
		return new Parameter[] { xDistanceParam, yDistanceParam, widthParam, heightParam };
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
		double p_xDistance = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		double p_yDistance = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		targetSizeX = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		targetSizeY = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		xDistance = p_xDistance + targetSizeX;
		yDistance = p_yDistance + targetSizeY;
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
}
