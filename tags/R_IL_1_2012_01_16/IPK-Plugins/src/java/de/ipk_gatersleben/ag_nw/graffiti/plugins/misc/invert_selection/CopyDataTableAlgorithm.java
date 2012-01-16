package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeMap;

import org.FeatureSet;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.actions.ClipboardService;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.DataMappingId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

public class CopyDataTableAlgorithm extends AbstractAlgorithm {
	boolean rownum = false;
	boolean label = true;
	boolean keggID = false;
	boolean cluster = false;
	boolean userURL = false;
	boolean keggURL = false;
	boolean pos = false;
	boolean size = false;
	boolean altIDs = false;
	boolean values = false;
	boolean valuesAvg = false;
	
	public void execute() {
		StringBuilder result = new StringBuilder();
		StringBuilder curRow = new StringBuilder();
		graph.numberGraphElements();
		if (rownum)
			addCol(result, curRow, "Row");
		if (label)
			addCol(result, curRow, "Label");
		if (keggID)
			addCol(result, curRow, "KEGG ID");
		if (cluster)
			addCol(result, curRow, "Cluster");
		if (userURL)
			addCol(result, curRow, "URL");
		if (keggURL)
			addCol(result, curRow, "KEGG URL");
		if (pos)
			addCol(result, curRow, "X");
		if (pos)
			addCol(result, curRow, "Y");
		if (size)
			addCol(result, curRow, "WIDTH");
		if (size)
			addCol(result, curRow, "HEIGHT");
		if (altIDs)
			addCol(result, curRow, "Alt. IDs");
		HashSet<String> dataCols = new HashSet<String>();
		ArrayList<String> dataColsArr = new ArrayList<String>();
		if (values) {
			for (Node n : getSelectedOrAllNodes()) {
				NodeHelper nh = new NodeHelper(n);
				TreeMap<DataMappingId, Stack<Double>> id2value_n1 = nh.getIdsAndValues(null);
				for (DataMappingId did : id2value_n1.keySet()) {
					String id = did.toString();
					id = StringManipulationTools.stringReplace(id, "_�_", "/");
					if (!dataCols.contains(id)) {
						dataCols.add(id);
						dataColsArr.add(id);
					}
				}
			}
			for (String id : dataColsArr)
				addCol(result, curRow, id);
		}
		ArrayList<String> dataColsArrAvg = new ArrayList<String>();
		if (valuesAvg) {
			for (Node n : getSelectedOrAllNodes()) {
				NodeHelper nh = new NodeHelper(n);
				TreeMap<DataMappingId, Stack<Double>> id2value_n1 = nh.getIdsAndAverageValues();
				for (DataMappingId did : id2value_n1.keySet()) {
					String id = did.toString();
					id = StringManipulationTools.stringReplace(id, "_�_", "/");
					if (!dataCols.contains(id)) {
						dataCols.add(id);
						dataColsArrAvg.add(id);
					}
				}
			}
			for (String id : dataColsArrAvg)
				addCol(result, curRow, id);
		}
		curRow = addRow(result);
		int row = 0;
		for (Node n : getSelectedOrAllNodes()) {
			NodeHelper nh = new NodeHelper(n);
			row++;
			if (rownum)
				addCol(result, curRow, row + "");
			if (label)
				addCol(result, curRow, nh.getLabel());
			if (keggID)
				addCol(result, curRow, KeggGmlHelper.getKeggId(n));
			if (cluster)
				addCol(result, curRow, nh.getClusterID(null));
			if (userURL)
				addCol(result, curRow, nh.getURL());
			if (keggURL)
				addCol(result, curRow, KeggGmlHelper.getKeggLinkUrl(n));
			if (pos)
				addCol(result, curRow, nh.getX() + "");
			if (pos)
				addCol(result, curRow, nh.getY() + "");
			if (size)
				addCol(result, curRow, nh.getWidth() + "");
			if (size)
				addCol(result, curRow, nh.getHeight() + "");
			if (altIDs)
				addCol(result, curRow, nh.getAlternativeIDs() + "");
			if (values) {
				TreeMap<DataMappingId, Stack<Double>> id2value_n1 = nh.getIdsAndValues(null);
				for (String col : dataColsArr) {
					Stack<Double> values = null;
					for (DataMappingId dmi : id2value_n1.keySet()) {
						String id = dmi.toString();
						id = StringManipulationTools.stringReplace(id, "_�_", "/");
						if (id.equals(col)) {
							values = id2value_n1.get(dmi);
							break;
						}
					}
					addCols(result, curRow, values);
				}
			}
			if (valuesAvg) {
				TreeMap<DataMappingId, Stack<Double>> id2value_n1 = nh.getIdsAndAverageValues();
				for (String col : dataColsArrAvg) {
					Stack<Double> values = null;
					for (DataMappingId dmi : id2value_n1.keySet()) {
						String id = dmi.toString();
						id = StringManipulationTools.stringReplace(id, "_�_", "/");
						if (id.equals(col)) {
							values = id2value_n1.get(dmi);
							break;
						}
					}
					addCols(result, curRow, values);
				}
			}
			curRow = addRow(result);
		}
		
		ClipboardService.writeToClipboardAsText(result.toString());
		MainFrame.showMessage("Information copied to clipboard!", MessageType.INFO);
	}
	
	private void addCols(StringBuilder result, StringBuilder curRow,
						Stack<Double> values2) {
		if (values2 != null) {
			ArrayList<Object> vals = new ArrayList<Object>(values2);
			addCols(result, curRow, vals);
		} else
			addCol(result, curRow, null);
	}
	
	private StringBuilder addRow(StringBuilder result) {
		result.append("\n");
		return new StringBuilder();
	}
	
	private void addCol(StringBuilder result, StringBuilder curRow, String col) {
		if (curRow.length() > 0) {
			result.append("\t");
			curRow.append("\t");
		}
		if (col == null)
			col = "";
		result.append(col);
		curRow.append(col);
	}
	
	private void addCols(StringBuilder result, StringBuilder curRow, Collection<Object> vals) {
		if (curRow.length() > 0) {
			result.append("\t");
			curRow.append("\t");
		}
		if (vals == null) {
			result.append("");
			curRow.append("");
		} else {
			int num = vals.size();
			int idx = 0;
			for (Object o : vals) {
				idx++;
				result.append(o.toString());
				curRow.append(o.toString());
				if (idx < num) {
					result.append(";");
					curRow.append(";");
				}
			}
		}
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"With this command you may transfer selected information<br>" +
							"to the clipboard. Please specify relevant columns:";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new BooleanParameter(rownum, "Row", null),
							new BooleanParameter(label, "Label", null),
							new BooleanParameter(keggID, "KEGG ID", null),
							new BooleanParameter(cluster, "Cluster ID", null),
							new BooleanParameter(userURL, "User URL", null),
							new BooleanParameter(keggURL, "KEGG Ref URL", null),
							new BooleanParameter(pos, "X/Y-Pos", null),
							new BooleanParameter(size, "Size", null),
							new BooleanParameter(altIDs, "Alternative IDs", null),
							new BooleanParameter(values, "Data mapping values", null),
							new BooleanParameter(valuesAvg, "Average data mapping values", null) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		rownum = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		label = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		keggID = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		cluster = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		userURL = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		keggURL = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		pos = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		size = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		altIDs = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		values = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		valuesAvg = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
	}
	
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			return "Copy Data Table...";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
}
