/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class SetNumericAttributeFromDataAlgorithm extends AbstractAlgorithm {
	
	private double minimumTargetValue = 1d;
	private double maximumTargetValue = 10d;
	private AttributePathNameSearchType attributeA, attributeB1, attributeB2;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Set Visual Properties dep. on Attribute Value...";
	}
	
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Change numeric visual properties such as the width of edges or node borders depending on<br>" +
							"the value of any of the available attributes.<br>" +
							"<br>";
	}
	
	@Override
	public Parameter[] getParameters() {
		ArrayList<AttributePathNameSearchType> possibleAttributes = new ArrayList<AttributePathNameSearchType>();
		SearchAndSelecAlgorithm.enumerateAllAttributes(possibleAttributes, graph, SearchType.getSetOfNumericSearchTypes());
		
		return new Parameter[] {
							new ObjectListParameter(null, "Source Attribute", "Select the source attribute", possibleAttributes),
							new DoubleParameter(minimumTargetValue, "Linear transformation: minimum",
												"The minimum source value corresponds to the the specified target value!"),
							new DoubleParameter(maximumTargetValue, "Linear transformation: maximum",
												"The maximum source value corresponds to the the specified target value!"),
							new ObjectListParameter(null, "Target Attribute 1", "Select the target attribute", possibleAttributes),
							new ObjectListParameter(null, "Target Attribute 2 (optional)", "Select the target attribute", possibleAttributes) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		attributeA = (AttributePathNameSearchType) ((ObjectListParameter) params[i++]).getValue();
		minimumTargetValue = ((DoubleParameter) params[i++]).getDouble();
		maximumTargetValue = ((DoubleParameter) params[i++]).getDouble();
		attributeB1 = (AttributePathNameSearchType) ((ObjectListParameter) params[i++]).getValue();
		attributeB2 = (AttributePathNameSearchType) ((ObjectListParameter) params[i++]).getValue();
	}
	
	public void execute() {
		Collection<GraphElement> graphElements = getSelectedOrAllGraphElements();
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
		for (GraphElement ge : graphElements) {
			try {
				double val = attributeA.getAttributeValue(ge, Double.NaN);
				if (!Double.isNaN(val)) {
					if (minValue == maxValue) {
						attributeB1.setAttributeValue(ge, (maximumTargetValue + minimumTargetValue) / 2);
						if (attributeB2 != null)
							attributeB2.setAttributeValue(ge, (maximumTargetValue + minimumTargetValue) / 2);
					} else {
						double res = (maximumTargetValue - minimumTargetValue) * (Math.abs(val) - minValue) / (maxValue - minValue) + minimumTargetValue;
						attributeB1.setAttributeValue(ge, res);
						if (attributeB2 != null)
							attributeB2.setAttributeValue(ge, res);
					}
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
