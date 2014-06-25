package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.StringManipulationTools;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionFilterConditions extends AbstractNavigationAction {
	
	private ArrayList<ThreadSafeOptions> metaDataColumns;
	private ExperimentReference experimentReference;
	private int ngroups = -1;
	private String oldPattern = "", oldPattern2 = "";
	public ArrayList<ThreadSafeOptions> experimentFactorSelection;
	private NavigationButton src;
	private final HashSet<String> groupStrings = new HashSet<String>();
	private ThreadSafeOptions metaDataColumnsReady;
	
	public ActionFilterConditions(String tooltip) {
		super(tooltip);
	}
	
	public ActionFilterConditions(ThreadSafeOptions metaDataColumnsReady, ArrayList<ThreadSafeOptions> metaDataColumns,
			ArrayList<ThreadSafeOptions> groupSelection,
			ExperimentReference experimentReference) {
		this("Filter experiment groups");
		this.metaDataColumnsReady = metaDataColumnsReady;
		this.metaDataColumns = metaDataColumns;
		this.experimentFactorSelection = groupSelection;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		experimentReference.getData(getStatusProvider());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(new NavigationButton(new ActionBooleanInverter("Invert selection", experimentFactorSelection), src.getGUIsetting()));
		for (ThreadSafeOptions tso : experimentFactorSelection)
			res.add(new NavigationButton(new ActionThreadSafeOptionsBooleanEditor(tso), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		StringBuilder settings = new StringBuilder();
		for (ThreadSafeOptions tso : metaDataColumns) {
			settings.append("/" + tso.getBval(0, false));
		}
		StringBuilder settings2 = new StringBuilder();
		for (ThreadSafeOptions tso : experimentFactorSelection)
			settings2.append("/" + tso.getBval(0, false));
		
		String pattern = settings.toString();
		if (!pattern.equals(oldPattern)) {
			ngroups = -1;
		}
		
		if (experimentReference.getExperimentPeek() != null) {
			String pattern2 = settings2.toString();
			if (!pattern2.equals(oldPattern2)) {
				updateGroupList();
				oldPattern2 = pattern2;
			}
			
			if (ngroups < 0)
				try {
					oldPattern = pattern;
					ngroups = countAndInitGroups();
				} catch (Exception e) {
					ngroups = -2;
				}
			int sel = 0;
			for (ThreadSafeOptions tso : experimentFactorSelection)
				if (tso.getBval(0, false))
					sel++;
			return "Filter Groups<br><font color='gray'><small>" + sel + "/" + experimentFactorSelection.size()
					+ " selected &rarr; " +
					(groupStrings.size() == 0 ? "<font color='red'>no data</font>" : groupStrings.size() + " group" + (groupStrings.size() > 1 ? "s" : ""))
					+ "</small></font>";
		} else
			return "Filter Groups<br><font color='gray'><small>(data is beeing loaded)</small></font>";
	}
	
	private void updateGroupList() {
		groupStrings.clear();
		HashMap<ConditionInfo, HashSet<String>> invalidValues = new HashMap<ConditionInfo, HashSet<String>>();
		for (ThreadSafeOptions tso : experimentFactorSelection) {
			if (!tso.getBval(0, false)) {
				ConditionInfo ci = (ConditionInfo) tso.getParam(11, null);
				String val = (String) tso.getParam(12, null);
				if (!invalidValues.containsKey(ci))
					invalidValues.put(ci, new HashSet<String>());
				invalidValues.get(ci).add(val);
			}
		}
		boolean searchForPlantIDs = false;
		for (ThreadSafeOptions tsoMD : metaDataColumns) {
			if (!tsoMD.getBval(0, false))
				continue;
			ConditionInfo ciToBeChecked = (ConditionInfo) tsoMD.getParam(1, null);
			if (ciToBeChecked == ConditionInfo.IGNORED_FIELD)
				searchForPlantIDs = true;
		}
		for (SubstanceInterface si : experimentReference.getExperimentPeek()) {
			for (ConditionInterface c : si) {
				boolean valid = metaDataColumns.size() > 0;
				StringBuilder sb = new StringBuilder();
				boolean foundOneTrue = false;
				for (ThreadSafeOptions tsoMD : metaDataColumns) {
					if (!tsoMD.getBval(0, false))
						continue;
					foundOneTrue = true;
					ConditionInfo ciToBeChecked = (ConditionInfo) tsoMD.getParam(1, null);
					if (ciToBeChecked == ConditionInfo.IGNORED_FIELD)
						continue;
					String v = c.getField(ciToBeChecked);
					if (v == null || v.isEmpty())
						v = "(not specified)";
					if (invalidValues.containsKey(ciToBeChecked) && invalidValues.get(ciToBeChecked).contains(v)) {
						valid = false;
						break;
					}
					sb.append("/" + ciToBeChecked + ":" + v);
				}
				if (valid && foundOneTrue && sb.length() > 0) {
					if (!searchForPlantIDs)
						groupStrings.add(sb.toString());
					else {
						HashSet<String> plantIDs = new HashSet<String>();
						for (SampleInterface sai : c) {
							for (NumericMeasurementInterface nmi : sai) {
								String plantID = nmi.getQualityAnnotation();
								if (plantID != null && !plantID.isEmpty())
									plantIDs.add(plantID);
							}
						}
						String s = sb.toString();
						for (String p : plantIDs) {
							groupStrings.add(s + "/" + ConditionInfo.IGNORED_FIELD + ":" + p);
						}
					}
				}
			}
		}
		
	}
	
	private int countAndInitGroups() throws Exception {
		metaDataColumnsReady.waitForBoolean(0);
		ExperimentInterface ei = experimentReference.getData();
		groupStrings.clear();
		for (SubstanceInterface si : ei) {
			for (ConditionInterface c : si) {
				StringBuilder sb = new StringBuilder();
				for (ThreadSafeOptions tso : metaDataColumns) {
					if (!tso.getBval(0, false))
						continue;
					
					ConditionInfo ci = (ConditionInfo) tso.getParam(1, ConditionInfo.IGNORED_FIELD);
					String v = c.getField(ci);
					if (v == null || v.isEmpty())
						v = "(not set)";
					sb.append("/" + ci + ":" + v);
				}
				groupStrings.add(sb.toString());
			}
		}
		
		TreeMap<ConditionInfo, TreeSet<String>> ci2vs = new TreeMap<ConditionInfo, TreeSet<String>>();
		for (ThreadSafeOptions tso : metaDataColumns) {
			if (!tso.getBval(0, false))
				continue;
			ConditionInfo ci = (ConditionInfo) tso.getParam(1, ConditionInfo.IGNORED_FIELD);
			if (ci == ConditionInfo.IGNORED_FIELD)
				continue;
			if (!ci2vs.containsKey(ci))
				ci2vs.put(ci, new TreeSet<String>());
			for (SubstanceInterface si : ei) {
				for (ConditionInterface c : si) {
					String v = c.getField(ci);
					if (v == null || v.isEmpty())
						v = "(not set)";
					ci2vs.get(ci).add(v);
				}
			}
			
		}
		experimentFactorSelection.clear();
		for (ConditionInfo ci : ci2vs.keySet()) {
			for (String v : ci2vs.get(ci)) {
				experimentFactorSelection.add(
						new ThreadSafeOptions()
								.setParam(0, ci.toString())
								.setParam(1, v)
								.setParam(10, ci + "<br>" +
										"<small><font color='gray'>" + StringManipulationTools.getWordWrap(v, 25) + "</font></small>")
								.setParam(11, ci)
								.setParam(12, v)
								.setInt(Integer.MAX_VALUE).setBval(0, true));
			}
		}
		return experimentFactorSelection.size();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> remarks = new ArrayList<String>();
		remarks.add("<b>Remarks</b> &rarr;");
		remarks.add(StringManipulationTools.getWordWrap(
				"From all possible groups, specific experiment factors may now be unselected, to ignore " +
						"connected data during script processing (data is filtered out during export).", 80));
		remarks
				.add(
				StringManipulationTools
						.getWordWrap(
								"The displayed group-count (below the 'Filter Groups' icon) is a upper-bound, which may be reduced during export in case outliers are defined.",
								80));
		remarks.add(
				StringManipulationTools.getWordWrap(
						"If 'Plant-IDs'-meta data is used for grouping, specific plant-IDs can't be unselected at this place. " +
								"If specific plant-IDs should not be included in the processing, you need to define them as outliers in " +
								"the experiment header outlier field (see IAP system documentation for details).", 80));
		return new MainPanelComponent(remarks);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Sort-Selection-64 Filter.png";
	}
}
