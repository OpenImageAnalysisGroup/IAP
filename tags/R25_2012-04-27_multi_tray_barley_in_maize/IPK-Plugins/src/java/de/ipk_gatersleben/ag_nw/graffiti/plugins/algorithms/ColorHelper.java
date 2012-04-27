/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms;

import java.awt.Color;
import java.util.HashMap;

import org.HelperClass;
import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors.ChartColorAttribute;

public class ColorHelper implements HelperClass {
	
	ChartColorAttribute cca = null;
	
	HashMap<String, Color> seriesInner2color = new HashMap<String, Color>();
	HashMap<String, Color> seriesOuter2color = new HashMap<String, Color>();
	
	public ColorHelper(Graph g) {
		cca = ChartColorAttribute.getAttribute(g);
	}
	
	public Color getColor1ForRowKey(String serie) {
		if (serie == null || serie.length() <= 0)
			return Color.DARK_GRAY;
		if (seriesInner2color.containsKey(serie))
			return seriesInner2color.get(serie);
		Color c = cca.getSeriesColor(serie, Color.DARK_GRAY);
		seriesInner2color.put(serie, c);
		return c;
	}
	
	public Color getColor2ForRowKey(String serie) {
		if (serie == null || serie.length() <= 0)
			return Color.BLACK;
		if (seriesOuter2color.containsKey(serie))
			return seriesOuter2color.get(serie);
		Color c = cca.getSeriesOutlineColor(serie, Color.BLACK);
		seriesOuter2color.put(serie, c);
		return c;
	}
	
	public void setColor1For(String col, Color color1) {
		cca.setSeriesColor(col, color1);
	}
}
