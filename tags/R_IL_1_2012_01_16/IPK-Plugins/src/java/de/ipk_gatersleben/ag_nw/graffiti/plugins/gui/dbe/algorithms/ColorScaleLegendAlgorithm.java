/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import java.util.ArrayList;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.HeatMapOptions;

/**
 * @author Christian Klukas
 *         (c) 2008 IPK Gatersleben, Group Network Analysis
 */
public class ColorScaleLegendAlgorithm extends AbstractAlgorithm {
	
	int nElements = 11;
	int legendWidth = 10;
	int legendHeight = 100;
	
	public String getName() {
		return "Create Heatmap Diagram Legend";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (!AttributeHelper.hasAttribute(graph, "hm_gamma")) {
			throw new PreconditionException("Graph contains no diagrams with heatmap visualization style!");
		};
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"This command creates a color-scale legend for the<br>" +
							"heatmap (color-coding) diagrams of the current graph.<br>" +
							"The heatmap color-selection and boundary specification<br>" +
							"is available from the network tab, but only in case<br>" +
							"the heatmap diagram style has been activated before<br>" +
							"from the node or edge tab.<br>" +
							"You may specify here, how many different colors should<br>" +
							"be processed, and how large the legend should be.";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new IntegerParameter(nElements, "Number of Color Shades", "Specify the number of color-shades (differently colored nodes)"), // number of
				// elements
				new IntegerParameter(legendWidth, "Color-Scale Width", "Width of the colored network elements"), // width
				new IntegerParameter(legendHeight, "Color-Scale Height", "Overall height of the color-scale") // height
		};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		nElements = ((IntegerParameter) params[i++]).getInteger().intValue();
		legendWidth = ((IntegerParameter) params[i++]).getInteger().intValue();
		legendHeight = ((IntegerParameter) params[i++]).getInteger().intValue();
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	public void execute() {
		HeatMapOptions hmo = new HeatMapOptions(graph);
		nElements = Math.abs(nElements);
		legendWidth = Math.abs(legendWidth);
		legendHeight = Math.abs(legendHeight);
		
		double nodeHeight = (double) legendHeight / (double) nElements;
		double nodeWidth = legendWidth;
		
		Vector2d maxxy = NodeTools.getMaximumXY(graph.getNodes(), 1, 0, 0, true);
		double offX = maxxy.x + 30d;
		double offY = 50d;
		ArrayList<Node> newNodes = new ArrayList<Node>();
		graph.getListenerManager().transactionStarted(this);
		NodeHelper middleNode = null;
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < nElements; i++) {
			double currentValue = (double) i / (double) (nElements - 1) * (hmo.heatMapUpperBound - hmo.heatMapLowerBound) + hmo.heatMapLowerBound;
			double x = offX;
			double y = offY + i * nodeHeight;
			Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(x, y));
			
			newNodes.add(n);
			
			NodeHelper nh = new NodeHelper(n);
			
			if (Math.abs(currentValue - hmo.heatMapMiddleBound) < minDist) {
				minDist = Math.abs(currentValue - hmo.heatMapMiddleBound);
				middleNode = nh;
			}
			
			nh.setBorderWidth(0);
			nh.setRounding(0);
			nh.setFillColor(hmo.getHeatmapColor(currentValue));
			nh.setLabel(" " + currentValue);
			nh.setLabelAlignment(-1, AlignmentSetting.RIGHT);
			nh.setLabelFontSize(0, false);
			nh.setSize(nodeWidth, nodeHeight);
			if (i == 0 || i + 1 == nElements) {
				nh.setLabelFontSize(10, false);
			}
		}
		if (middleNode != null) {
			middleNode.setLabelFontSize(10, false);
		}
		graph.getListenerManager().transactionFinished(this);
		
		GraphHelper.selectNodes(newNodes);
		MainFrame.showMessage("Heatmap color-scale legend has been created (" + newNodes.size() + " nodes added and selected)", MessageType.INFO);
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
