/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors.ChartColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.HeatMapOptions;

/**
 * @author Christian Klukas
 *         (c) 2007 IPK Gatersleben, Group Network Analysis
 */
public class AddDiagramLegendAlgorithm extends AbstractAlgorithm {
	
	int nElements = 11;
	int legendWidth = 10;
	int legendHeight = 100;
	private static LegendType type = LegendType.OLDSTYLE;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Create Legend";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (!ChartColorAttribute.hasAttribute(graph) && !AttributeHelper.hasAttribute(graph, "hm_gamma"))
			throw new PreconditionException("Graph contains neither diagrams nor heatmap visualizations!");
		// if (!AttributeHelper.hasAttribute(graph, "hm_gamma")) {
		// throw new PreconditionException("Graph contains no diagrams with heatmap visualization style!");
		// };
	}
	
	@Override
	public String getDescription() {
		if (AttributeHelper.hasAttribute(graph, "hm_gamma"))
			return "<html>" +
								"This command creates a color-scale legend for the<br>" +
								"heatmap (color-coding) diagrams of the current graph.<br>" +
								"The heatmap color-selection and boundary specification<br>" +
								"is available from the network tab, but only in case<br>" +
								"the heatmap diagram style has been activated before<br>" +
								"from the node or edge tab.<br>" +
								"You may specify here, how many different colors should<br>" +
								"be processed, and how large the legend should be.";
		else
			return null;
	}
	
	@Override
	public Parameter[] getParameters() {
		
		// nutzer fragen nach style (mit nummer und allem, mit allem, nur die wichtigen)
		
		if (AttributeHelper.hasAttribute(graph, "hm_gamma"))
			return new Parameter[] {
					new IntegerParameter(nElements, "Number of Color Shades", "Specify the number of color-shades (differently colored nodes)"), // number of
					// elements
					new IntegerParameter(legendWidth, "Color-Scale Width", "Width of the colored network elements"), // width
					new IntegerParameter(legendHeight, "Color-Scale Height", "Overall height of the color-scale") // height
			};
		else
			return new Parameter[] { new ObjectListParameter(LegendType.COMPLETE, "Type", "", LegendType.values()) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		if (AttributeHelper.hasAttribute(graph, "hm_gamma")) {
			int i = 0;
			nElements = ((IntegerParameter) params[i++]).getInteger().intValue();
			legendWidth = ((IntegerParameter) params[i++]).getInteger().intValue();
			legendHeight = ((IntegerParameter) params[i++]).getInteger().intValue();
		} else
			type = (LegendType) params[0].getValue();
	}
	
	public void execute() {
		if (ChartColorAttribute.hasAttribute(graph))
			createSeriesColorLegend();
		if (AttributeHelper.hasAttribute(graph, "hm_gamma"))
			createHeatMapLegend();
	}
	
	private void createSeriesColorLegend() {
		int startX = 50;
		int startY = 50;
		int step = 20;
		int w = 15;
		int h = 15;
		Vector2d maxxy = NodeTools.getMaximumXY(graph.getNodes(), 1, 0, 0, true);
		startY = (int) maxxy.y + 40;
		graph.getListenerManager().transactionStarted(this);
		try {
			HashMap<String, ConditionInterface> condname2cond = new HashMap<String, ConditionInterface>();
			for (Node nd : graph.getNodes()) {
				for (SubstanceInterface s : new NodeHelper(nd).getDataMappings())
					for (ConditionInterface c : s)
						condname2cond.put(c.getConditionName(), c);
			}
			ChartColorAttribute cca = ChartColorAttribute.getAttribute(graph);
			int barCnt = cca.getDefinedBarCount();
			for (int i = 0; i < barCnt; i++) {
				String id = cca.getIdList(barCnt).get(i);
				Color fillc = cca.getSeriesColors(cca.getIdList(barCnt)).get(i);
				Color framec = cca.getSeriesOutlineColors(cca.getIdList(barCnt)).get(i);
				Node n = GraphHelper.addNodeToGraph(graph, startX, startY + step * i, 1, w, h, framec, fillc);
				String lbl = null;
				if (condname2cond.containsKey(id)) {
					Condition cond = ((Condition) condname2cond.get(id));
					switch (type) {
						case COMPLETE:
							lbl = cond.getExperimentName() + ", " + cond.getRowId() + ": " + getValue(cond.getSpecies()) + getValue(cond.getGenotype())
									+ getValue(cond.getTreatment()) + getValue(cond.getGrowthconditions()) + getValue(cond.getVariety());
							break;
						case COMPLETE_WITHOUT_EXPERIMENT:
							lbl = cond.getRowId() + ": " + getValue(cond.getSpecies()) + getValue(cond.getGenotype())
									+ getValue(cond.getTreatment()) + getValue(cond.getGrowthconditions()) + getValue(cond.getVariety());
							break;
						case COMPLETE_WITHOUT_EXPERIMENT_AND_CONDITIONNUMBER:
							lbl = getValue(cond.getSpecies()) + getValue(cond.getGenotype())
									+ getValue(cond.getTreatment()) + getValue(cond.getGrowthconditions()) + getValue(cond.getVariety());
							break;
						default: // case OLDSTYLE:
							lbl = id;
							break;
					}
				} else
					lbl = id;
				
				if (lbl.endsWith(" / "))
					lbl = lbl.substring(0, lbl.length() - " / ".length());
				AttributeHelper.setLabel(n, lbl);
				AttributeHelper.setShapeEllipse(n);
				// AttributeHelper.setRoundedEdges(n, 2);
				AttributeHelper.setLabelAlignment(-1, n, AlignmentSetting.RIGHT);
			}
			if (barCnt > 0)
				MainFrame.showMessageDialog("Legend with " + barCnt + " series items has been created and is placed at the lower left.", "Information");
			else
				MainFrame.showMessageDialog("Legend could not be created as no series data is visible or mapped to the network!", "Information");
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	public String getValue(String s) {
		if (s == null || s.length() <= 0)
			return "";
		else
			return s + " / ";
	}
	
	public void createHeatMapLegend() {
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
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
}
