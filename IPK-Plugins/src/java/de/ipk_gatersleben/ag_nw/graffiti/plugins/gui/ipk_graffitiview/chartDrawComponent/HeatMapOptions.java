package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import java.awt.Color;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graphics.GraphicAttributeConstants;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.RecolorEdgesAlgorithm;

public class HeatMapOptions {
	public double heatMapLowerBound;
	public double heatMapMiddleBound;
	public double heatMapUpperBound;
	private Color heatMapLowerColor;
	private Color heatMapMiddleColor;
	private Color heatMapUpperColor;
	private double heatMapGamma;
	
	public HeatMapOptions(double heatMapLowerBound, double heatMapMiddleBound, double heatMapUpperBound,
						Color heatMapLowerColor, Color heatMapMiddleColor, Color heatMapUpperColor,
						double heatMapGamma) {
		this.heatMapLowerBound = heatMapLowerBound;
		this.heatMapMiddleBound = heatMapMiddleBound;
		this.heatMapUpperBound = heatMapUpperBound;
		this.heatMapLowerColor = heatMapLowerColor;
		this.heatMapMiddleColor = heatMapMiddleColor;
		this.heatMapUpperColor = heatMapUpperColor;
		this.heatMapGamma = heatMapGamma;
	}
	
	public HeatMapOptions(Graph graph) {
		heatMapLowerColor = NodeTools.getColorAttributeValue(graph, GraphicAttributeConstants.HEATMAP_LOWER_COL, Color.BLUE);
		heatMapUpperColor = NodeTools.getColorAttributeValue(graph, GraphicAttributeConstants.HEATMAP_UPPER_COL, Color.RED);
		heatMapMiddleColor = NodeTools.getColorAttributeValue(graph, GraphicAttributeConstants.HEATMAP_MIDDLE_COL, Color.WHITE);
		
		heatMapLowerBound = AttributeHelper.getHeatMapLowerBound(graph);
		heatMapMiddleBound = AttributeHelper.getHeatMapMiddleBound(graph);
		heatMapUpperBound = AttributeHelper.getHeatMapUpperBound(graph);
		heatMapGamma = AttributeHelper.getHeatMapGamma(graph);
	}
	
	public Color getHeatmapColor(double doubleValue) {
		return RecolorEdgesAlgorithm.get3Color(heatMapLowerBound, heatMapUpperBound, heatMapMiddleBound,
							doubleValue, heatMapGamma, heatMapLowerColor, heatMapMiddleColor, heatMapUpperColor);
	}
}
