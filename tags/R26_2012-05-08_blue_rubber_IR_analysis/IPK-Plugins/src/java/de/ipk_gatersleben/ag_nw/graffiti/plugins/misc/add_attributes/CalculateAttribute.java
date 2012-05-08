/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.add_attributes;

import java.util.ArrayList;
import java.util.Collection;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

public class CalculateAttribute extends AbstractAlgorithm {
	
	AttributePathNameSearchType attributeA, attributeB;
	AttributeCalculation operation = AttributeCalculation.A;
	String attributePath = "properties";
	String attributeName = "M0";
	ModeOfAttributeOperation modeOfOperation = ModeOfAttributeOperation.createAttribute;
	
	public void execute() {
		if (operation == null) {
			MainFrame.showMessageDialog("Invalid Operation Selected!", "Error");
			return;
		}
		try {
			graph.getListenerManager().transactionStarted(this);
			if (operation.transformsDataList()) {
				ArrayList<Double> values = new ArrayList<Double>();
				Collection<GraphElement> rrr = getSelectedOrAllGraphElements();
				for (GraphElement ge : rrr) {
					double a = Double.NaN;
					if (attributeA != null)
						a = attributeA.getAttributeValue(ge, Double.NaN);
					if (!Double.isNaN(a))
						values.add(a);
				}
				ArrayList<Double> processedMultipleData = operation.performMultipleAandSingleAoperation(values);
				int i = 0;
				for (GraphElement ge : rrr) {
					double a = Double.NaN;
					if (attributeA != null)
						a = attributeA.getAttributeValue(ge, Double.NaN);
					if (!Double.isNaN(a)) {
						if (modeOfOperation == ModeOfAttributeOperation.replaceLabel)
							AttributeHelper.setLabel(ge, "" + ommitZero(processedMultipleData.get(i)));
						else
							if (modeOfOperation == ModeOfAttributeOperation.appendLabel)
								AttributeHelper.setLabel(ge, AttributeHelper.getLabel(ge, "") + ommitZero(processedMultipleData.get(i)));
							else
								AttributeHelper.setAttribute(ge, attributePath, attributeName, processedMultipleData.get(i));
					}
					i++;
				}
			} else
				if (operation.requiresMultipleA()) {
					ArrayList<Double> values = new ArrayList<Double>();
					for (GraphElement ge : getSelectedOrAllGraphElements()) {
						double a = Double.NaN;
						if (attributeA != null)
							a = attributeA.getAttributeValue(ge, Double.NaN);
						if (!Double.isNaN(a))
							values.add(a);
					}
					double aa_op = operation.performOperation(values);
					if (!Double.isNaN(aa_op)) {
						for (GraphElement ge : getSelectedOrAllGraphElements()) {
							double a = Double.NaN;
							if (attributeA != null)
								a = attributeA.getAttributeValue(ge, Double.NaN);
							if (!Double.isNaN(a)) {
								if (modeOfOperation == ModeOfAttributeOperation.replaceLabel)
									AttributeHelper.setLabel(ge, ommitZero(aa_op));
								else
									if (modeOfOperation == ModeOfAttributeOperation.appendLabel)
										AttributeHelper.setLabel(ge, AttributeHelper.getLabel(ge, "") + ommitZero(aa_op));
									else
										AttributeHelper.setAttribute(ge, attributePath, attributeName, aa_op);
							}
						}
					}
				} else {
					for (GraphElement ge : getSelectedOrAllGraphElements()) {
						double a = Double.NaN;
						double b = Double.NaN;
						if (attributeA != null)
							a = attributeA.getAttributeValue(ge, Double.NaN);
						if (attributeB != null)
							b = attributeB.getAttributeValue(ge, Double.NaN);
						double result = operation.performOperation(a, b);
						if (!Double.isNaN(result)) {
							if (modeOfOperation == ModeOfAttributeOperation.replaceLabel)
								AttributeHelper.setLabel(ge, ommitZero(result));
							else
								if (modeOfOperation == ModeOfAttributeOperation.appendLabel)
									AttributeHelper.setLabel(ge, AttributeHelper.getLabel(ge, "") + ommitZero(result));
								else
									AttributeHelper.setAttribute(ge, attributePath, attributeName, result);
						}
					}
				}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	private String ommitZero(double result) {
		String v = "" + result;
		if (v.endsWith(".0"))
			return v.substring(0, v.length() - ".0".length());
		else
			return v;
	}
	
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Perform Calculation";
		else
			return null;
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"With this command you may add new attributes to the selected graph<br>" +
							"elements (or all elements, if nothing is selected). The values of these<br>" +
							"attributes depend on the parameters you choose, below.<br><br>" +
							"<small>Example use-case: Use the menu command &quot;Mapping/Evaluate Properties&quot;,<br>" +
							"to determine the maximum and minimum sample value for each graph element. Then use this<br>" +
							"command to add a additional attribute, which shows the ratio or the span for these values.<br><br>" +
							"The attribute group and name should start with and contain only ASCII letters and optionally<br>" +
							"numbers at the end. GML file format loading may otherwise fail. Use GRAPHML file format<br>" +
							"if you would like to use special characters in attribute names.<br><br>";
	}
	
	@Override
	public Parameter[] getParameters() {
		ArrayList<ModeOfAttributeOperation> possibleCommands = new ArrayList<ModeOfAttributeOperation>();
		for (ModeOfAttributeOperation mo : ModeOfAttributeOperation.values())
			possibleCommands.add(mo);
		ArrayList<AttributePathNameSearchType> possibleAttributes = new ArrayList<AttributePathNameSearchType>();
		SearchAndSelecAlgorithm.enumerateAllAttributes(possibleAttributes, graph, SearchType.getSetOfNumericSearchTypes());
		ArrayList<AttributeCalculation> possibleOperations = new ArrayList<AttributeCalculation>();
		for (AttributeCalculation ac : AttributeCalculation.values())
			possibleOperations.add(ac);
		return new Parameter[] {
							new ObjectListParameter(null, "Attibute A", "Select the desired attribute", possibleAttributes),
							new ObjectListParameter(null, "Attibute B", "Select the desired attribute", possibleAttributes),
							new ObjectListParameter(operation, "Calculation", "Select the desired operation", possibleOperations),
							new ObjectListParameter(modeOfOperation, "Target", "Mode of operation", possibleCommands),
							new StringParameter(attributePath, "Attribute Group", "Please specify the desired target folder or leave this field blank"),
							new StringParameter(attributeName, "Attribute Name", "Please specify the desired attribute name"),

		};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		attributeA = (AttributePathNameSearchType) ((ObjectListParameter) params[i++]).getValue();
		attributeB = (AttributePathNameSearchType) ((ObjectListParameter) params[i++]).getValue();
		operation = (AttributeCalculation) ((ObjectListParameter) params[i++]).getValue();
		modeOfOperation = (ModeOfAttributeOperation) ((ObjectListParameter) params[i++]).getValue();
		attributePath = ((StringParameter) params[i++]).getString();
		attributeName = ((StringParameter) params[i++]).getString();
	}
	
	@Override
	public String getCategory() {
		return null;// "Elements"; // "menu.edit";
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
