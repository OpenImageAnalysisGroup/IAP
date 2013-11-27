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
import java.util.Collection;

import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class RecolorEdgesAlgorithm extends AbstractAlgorithm {
	
	private Color minimumColor = Color.RED;
	private Color betweenColor = Color.WHITE;
	private Color maximumColor = Color.BLUE;
	private double gamma = 1d;
	
	private AttributePathNameSearchType attributeA, attributeB;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Set Color dep. on Attribute Value...";
	}
	
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	public void execute() {
		ArrayList<AttributePathNameSearchType> possibleNumericAttributes = new ArrayList<AttributePathNameSearchType>();
		SearchAndSelecAlgorithm.enumerateAllAttributes(possibleNumericAttributes, graph, SearchType.getSetOfNumericSearchTypes());
		
		ArrayList<AttributePathNameSearchType> possibleColorAttributes = new ArrayList<AttributePathNameSearchType>();
		SearchAndSelecAlgorithm.enumerateAllAttributes(possibleColorAttributes, graph, SearchType.getSetOfColorSearchTypes());
		
		Collection<GraphElement> graphElements = getSelectedOrAllGraphElements();
		
		Object[] resA = MyInputHelper.getInput(
							"<html>" +
												"Change the coloring of nodes or edges depending on<br>" +
												"the value of any of the available numeric attributes.<br>" +
												"At first, the desired numeric attribute is selected.<br>" +
												"In the next step, the coloring and the target color<br>" +
												"attribute is defined.<br>" +
												"",
							getName(),
							new Object[] {
												"Source Attribute", possibleNumericAttributes,
				});
		if (resA == null)
			return;
		int i = 0;
		attributeA = (AttributePathNameSearchType) resA[i++];
		double minValue = Double.MAX_VALUE;
		double maxValue = Double.NEGATIVE_INFINITY;
		
		for (GraphElement ge : graphElements) {
			double val = attributeA.getAttributeValue(ge, Double.NaN);
			if (!Double.isNaN(val)) {
				if (val < minValue)
					minValue = val;
				if (val > maxValue)
					maxValue = val;
			}
		}
		
		Object[] res = MyInputHelper.getInput(
							"<html>" +
												"Change the coloring of nodes or edges depending on<br>" +
												"the value of any of the available numeric attributes.<br><br>" +
												"",
							getName(),
							new Object[] {
												"Minimum Value", minValue,
												"Minimum Color", minimumColor,
												"Between Value", (minValue + maxValue) / 2,
												"Between Color", betweenColor,
												"Maxmimum Value", maxValue,
												"Maximum Color", maximumColor,
												"Gamma (1 = disabled)", gamma,
												"Targe Attribute", possibleColorAttributes
				});
		if (res == null)
			return;
		i = 0;
		
		minValue = (Double) res[i++];
		minimumColor = (Color) res[i++];
		
		double betweenValue = (Double) res[i++];
		betweenColor = (Color) res[i++];
		
		maxValue = (Double) res[i++];
		maximumColor = (Color) res[i++];
		
		gamma = (Double) res[i++];
		attributeB = (AttributePathNameSearchType) res[i++];
		
		for (GraphElement ge : graphElements) {
			double val = attributeA.getAttributeValue(ge, Double.NaN);
			if (!Double.isNaN(val)) {
				Color c = get3Color(minValue, maxValue, betweenValue, val, gamma, minimumColor, betweenColor, maximumColor);
				attributeB.setAttributeValue(ge, c);
			}
		}
	}
	
	public static Color get3Color(double minValue, double maxValue,
						double betweenValue, double val, double gamma,
						Color minimumColor, Color betweenColor, Color maximumColor) {
		Color res;
		if (val < minValue)
			val = minValue;
		if (val > maxValue)
			val = maxValue;
		if (minValue == maxValue) {
			res = betweenColor;
		} else {
			if (val < betweenValue) {
				Color tc = getColor(
									(float) ((betweenValue - val) / (betweenValue - minValue)),
									gamma,
									betweenColor,
									minimumColor);
				res = tc;
			} else {
				Color tc = getColor(
									(float) ((val - betweenValue) / (maxValue - betweenValue)),
									gamma,
									betweenColor,
									maximumColor);
				res = tc;
			}
		}
		return res;
	}
	
	public static Color getColor(float maxOrMinR, double gamma,
						Color col__1, Color col_1) {
		Color col1 = col__1;
		Color col2 = col_1;
		maxOrMinR = Math.abs(maxOrMinR);
		maxOrMinR = (float) Math.pow(maxOrMinR, gamma);
		float red = (col2.getRed() - col1.getRed()) * maxOrMinR + col1.getRed();
		float green = (col2.getGreen() - col1.getGreen()) * maxOrMinR + col1.getGreen();
		float blue = (col2.getBlue() - col1.getBlue()) * maxOrMinR + col1.getBlue();
		float alpha = (col2.getAlpha() - col1.getAlpha()) * maxOrMinR + col1.getAlpha();
		return new Color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}
}
