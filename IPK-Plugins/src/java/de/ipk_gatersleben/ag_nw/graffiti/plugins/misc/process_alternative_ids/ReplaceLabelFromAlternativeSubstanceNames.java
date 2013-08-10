/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 03.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.process_alternative_ids;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class ReplaceLabelFromAlternativeSubstanceNames extends AbstractAlgorithm {
	
	private final ArrayList<Integer> parametersToBeConsidered = new ArrayList<Integer>();
	
	private SetLabelModeOfOperation modeOfOperation = SetLabelModeOfOperation.setLabelAsSingleUniqueListDivideByDivider;
	
	private String modeOfOperationDividerForMappings = "<br>";
	private String modeOfOperationDividerForSingleMapping = ", ";
	
	private boolean modeOfOperationIncludeEmptyValues = false;
	private boolean modeOfOperationSetLabel = true;
	private boolean modeOfOperationSetClusterID = false;
	
	public String getName() {
		return "Set Label/Cluster ID";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
		}
		int maxID = 0;
		HashMap<Integer, String> exampleValues = new HashMap<Integer, String>();
		maxID = enumerateExistingAlternativeSubstanceIDsAndTheirExamples(getSelectedOrAllGraphElements(), maxID,
							exampleValues);
		if (maxID < 0) {
			errors.add("No data mapping with defined alternative identifiers found!");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "If only a single data annotation index is selected, the main ID<br>"
							+ "for each mapping is updated with this information.<br>"
							+ "Additionally the node label or cluster ID may be modified with<br>"
							+ "this command. Please specify the modes of operation and select<br>"
							+ "the data annotation idices to be processed:";
	}
	
	@Override
	public Parameter[] getParameters() {
		int maxID = 0;
		HashMap<Integer, String> exampleValues = new HashMap<Integer, String>();
		maxID = enumerateExistingAlternativeSubstanceIDsAndTheirExamples(getSelectedOrAllGraphElements(), maxID,
							exampleValues);
		ArrayList<String> selvals = new ArrayList<String>();
		for (int i = 0; i <= maxID; i++) {
			String s = "" + i;
			String example = exampleValues.get(new Integer(i));
			if (example != null)
				s += " (e.g. " + example + ")";
			selvals.add(s);
		}
		return getParameters(new ObjectListParameter(modeOfOperation, "<html><b>Mode of Operation", null,
							SetLabelModeOfOperation.values()), new StringParameter(modeOfOperationDividerForMappings,
							"<html><b>Divider for multiple mappings", null), new StringParameter(
								modeOfOperationDividerForSingleMapping, "<html><b>Divider for annotation list", null),
								new BooleanParameter(modeOfOperationIncludeEmptyValues, "<html><b>Include empty values", null),
								new BooleanParameter(modeOfOperationSetLabel, "<html><b>Result: Set Label", null), new BooleanParameter(
													modeOfOperationSetClusterID, "<html><b>Result: Set Cluster ID", null), selvals);
	}
	
	private Parameter[] getParameters(Parameter p1, Parameter p2, Parameter p3, Parameter p4, Parameter p5,
						Parameter p6, ArrayList<String> selvals) {
		ArrayList<Parameter> result = new ArrayList<Parameter>();
		result.add(p1);
		result.add(p2);
		result.add(p3);
		result.add(p4);
		result.add(p5);
		result.add(p6);
		for (String s : selvals) {
			BooleanParameter bp = new BooleanParameter(false, s, null);
			result.add(bp);
		}
		return result.toArray(new Parameter[] {});
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		modeOfOperation = (SetLabelModeOfOperation) ((ObjectListParameter) params[i++]).getValue();
		modeOfOperationDividerForMappings = (String) ((StringParameter) params[i++]).getValue();
		modeOfOperationDividerForSingleMapping = (String) ((StringParameter) params[i++]).getValue();
		
		modeOfOperationIncludeEmptyValues = ((BooleanParameter) params[i++]).getBoolean();
		modeOfOperationSetLabel = ((BooleanParameter) params[i++]).getBoolean();
		modeOfOperationSetClusterID = ((BooleanParameter) params[i++]).getBoolean();
		
		parametersToBeConsidered.clear();
		for (int i2 = i; i2 < params.length; i2++) {
			if (!((BooleanParameter) params[i2]).getBoolean())
				continue;
			String number = params[i2].getName();
			if (number.contains(" ")) {
				number = number.substring(0, number.indexOf(" "));
				number = number.trim();
			}
			int idx = Integer.parseInt(number);
			parametersToBeConsidered.add(idx);
		}
	}
	
	public void execute() {
		if (parametersToBeConsidered.size() <= 0) {
			MainFrame.showMessageDialog("No alternative identifier index has been selected in the parameter dialog.",
								"Information");
		}
		int workCnt = 0;
		graph.getListenerManager().transactionStarted(this);
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			ArrayList<ArrayList<String>> mappingStrings = new ArrayList<ArrayList<String>>();
			GraphElementHelper nh = new GraphElementHelper(ge);
			boolean hasAltIDs = false;
			for (SubstanceInterface md : nh.getDataMappings()) {
				String oMain = md.getName();
				ArrayList<String> allNames = new ArrayList<String>();
				for (int i : parametersToBeConsidered) {
					String oAlternative = md.getSynonyme(i);
					if (oMain != null && oAlternative != null) {
						hasAltIDs = true;
						workCnt++;
						String otherName = oAlternative;
						if (!modeOfOperationIncludeEmptyValues && (otherName == null || otherName.length() <= 0))
							; // ignore empty value
						else
							allNames.add(otherName);
					}
				}
				if (allNames.size() == 1)
					md.setName(allNames.get(0));
				else
					if (allNames.size() == 0 && !modeOfOperationIncludeEmptyValues)
						md.setName("");
				mappingStrings.add(allNames);
			}
			if (hasAltIDs) {
				String newName = getResultLabel(nh.getLabel(), mappingStrings);
				if (modeOfOperationSetClusterID)
					nh.setCluster(newName);
				if (modeOfOperationSetLabel)
					nh.setLabel(newName);
			}
		}
		graph.getListenerManager().transactionFinished(this);
	}
	
	private String getResultLabel(String currentLabel, ArrayList<ArrayList<String>> mappingStrings) {
		boolean htmlLabel = false;
		if (currentLabel != null && currentLabel.trim().startsWith("<html>")) {
			currentLabel = currentLabel.trim().substring("<html>".length());
		}
		StringBuilder result = new StringBuilder();
		if (modeOfOperation == SetLabelModeOfOperation.setLabelAsSingleUniqueListDivideByDivider) {
			// process
			LinkedHashSet<String> uniqueValues = new LinkedHashSet<String>();
			for (ArrayList<String> values : mappingStrings)
				for (String v : values)
					if (v.length() > 0)
						uniqueValues.add(v);
			String divider = modeOfOperationDividerForSingleMapping;
			if (divider.contains("<") && divider.contains(">"))
				htmlLabel = true;
			result.append(getListValue(uniqueValues, divider));
		} else {
			String divider1 = modeOfOperationDividerForMappings;
			if (divider1.contains("<") && divider1.contains(">"))
				htmlLabel = true;
			String divider2 = modeOfOperationDividerForSingleMapping;
			if (divider2.contains("<") && divider2.contains(">"))
				htmlLabel = true;
			if (modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableCreateColumns
								|| modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableCreateRows
								|| modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableUseExistingLabelAsTableHeaderCreateColumns
								|| modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableUseExistingLabelAsTableHeaderCreateRows) {
				htmlLabel = true;
				result.append("<table border=\"1\">");
				if (modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableUseExistingLabelAsTableHeaderCreateColumns
									|| modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableUseExistingLabelAsTableHeaderCreateRows)
					result.append("<tr><th colspan=\"" + mappingStrings.size() + "\">" + currentLabel + "</th></tr>");
				if (modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableCreateColumns
									|| modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableUseExistingLabelAsTableHeaderCreateColumns) {
					// add information as table columns
					result.append("<tr>");
					result.append(getMultipleListValues("<td>", "</td>", mappingStrings, divider2));
					result.append("</tr>");
				}
				if (modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableCreateRows
									|| modeOfOperation == SetLabelModeOfOperation.setLabelCreateTableUseExistingLabelAsTableHeaderCreateRows) {
					// add information as table rows
					result.append(getMultipleListValues("<tr><td>", "</td></tr>", mappingStrings, divider2));
				}
				result.append("</table>");
			} else {
				// use divider1 information to split data for multiple mappings
				ArrayList<String> listValues = new ArrayList<String>();
				for (ArrayList<String> v : mappingStrings)
					listValues.add(getListValue(v, divider2));
				result.append(getListValue(listValues, divider1));
			}
		}
		
		if (htmlLabel)
			return "<html>" + result.toString();
		else
			return result.toString();
	}
	
	private String getMultipleListValues(String preMap, String postMap, ArrayList<ArrayList<String>> mappingStrings,
						String dividerInList) {
		StringBuilder result = new StringBuilder();
		for (ArrayList<String> v : mappingStrings) {
			result.append(preMap);
			result.append(getListValue(v, dividerInList));
			result.append(postMap);
		}
		return result.toString();
	}
	
	private String getListValue(Collection<String> values, String divider) {
		ArrayList<String> v = new ArrayList<String>(values);
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < v.size(); i++) {
			res.append(v.get(i));
			if (i < v.size() - 1)
				res.append(divider);
		}
		return res.toString();
	}
	
	public static int enumerateExistingAlternativeSubstanceIDsAndTheirExamples(Collection<GraphElement> graphElements,
						int maxID, HashMap<Integer, String> exampleValues) {
		for (GraphElement ge : graphElements) {
			GraphElementHelper geh = new GraphElementHelper(ge);
			for (SubstanceInterface md : geh.getDataMappings()) {
				if (md.getSynonymMap() != null) {
					for (Entry<Integer, String> e : md.getSynonymMap().entrySet()) {
						if (!exampleValues.containsKey(e.getKey())) {
							String value = e.getValue();
							if (value != null && value.length() > 0)
								exampleValues.put(e.getKey(), value);
						}
						if (e.getKey() > maxID) {
							maxID = e.getKey();
						}
					}
				}
			}
		}
		return maxID;
	}
}