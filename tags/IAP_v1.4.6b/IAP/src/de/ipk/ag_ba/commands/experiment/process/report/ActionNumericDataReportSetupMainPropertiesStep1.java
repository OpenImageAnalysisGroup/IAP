/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.process.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.settings.ActionToggle;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_actions.SpecialCommandLineSupport;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionNumericDataReportSetupMainPropertiesStep1 extends AbstractNavigationAction implements SpecialCommandLineSupport {
	
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	private final boolean exportIndividualAngles;
	private final boolean xlsx;
	
	private boolean executed = false;
	
	private final ArrayList<NavigationButton> settings = new ArrayList<NavigationButton>();
	private final ArrayList<ThreadSafeOptions> toggles;
	private ArrayList<ThreadSafeOptions> togglesForReport;
	
	public ActionNumericDataReportSetupMainPropertiesStep1(String tooltip, boolean exportIndividualAngles,
			boolean xlsx, ArrayList<ThreadSafeOptions> toggles) {
		super(tooltip);
		this.exportIndividualAngles = exportIndividualAngles;
		this.xlsx = xlsx;
		this.toggles = toggles;
	}
	
	public ActionNumericDataReportSetupMainPropertiesStep1(
			ExperimentReference experimentReference, boolean exportIndividualAngles, boolean xlsx,
			ArrayList<ThreadSafeOptions> toggles) {
		this("Specify report options" +
				(exportIndividualAngles ? (xlsx ? " XLSX" : " CSV")
						: " PDF"),
				exportIndividualAngles,
				xlsx, toggles);
		this.experimentReference = experimentReference;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		actions.add(new NavigationButton(
				new ActionNumericDataReportSetupInterestingTraitsStep2(
						experimentReference, false, toggles, false, togglesForReport, false),
				src.getGUIsetting()));
		
		for (NavigationButton s : settings)
			actions.add(s);
		
		return actions;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return !executed;
	}
	
	@Override
	public String getDefaultTitle() {
		if (exportIndividualAngles)
			return "Export Numeric Data (" + (xlsx ? "XLSX" : "CSV") + ")";
		if (SystemAnalysis.isHeadless()) {
			return "Create Report" + (xlsx ? " (XLSX)" : "")
					+ (exportIndividualAngles ? " (side angles)" : " (avg)");
		} else {
			ArrayList<String> settings = new ArrayList<String>();
			ArrayList<ThreadSafeOptions> settingsTsos = new ArrayList<ThreadSafeOptions>();
			boolean appendix = false, clustering = false, ratio = false;
			
			Collections.sort(toggles, new Comparator<ThreadSafeOptions>() {
				@Override
				public int compare(ThreadSafeOptions o1, ThreadSafeOptions o2) {
					return ((Long) o1.getLong()).compareTo(o2.getLong());
				}
			});
			
			for (ThreadSafeOptions tso : toggles) {
				if (((String) tso.getParam(0, "")).equals("Appendix"))
					appendix = tso.getBval(0, false);
				else
					if (((String) tso.getParam(0, "")).equals("Ratio"))
						ratio = tso.getBval(0, false);
					else
						if (((String) tso.getParam(0, "")).equals("Clustering"))
							clustering = tso.getBval(0, false);
						else {
							if (tso.getBval(0, false)) {
								// if (settings.size() >= 2) {
								// settingsTsos.get(0).setBval(0, false);
								// settingsTsos.remove(0);
								// settings.remove(0);
								// }
								settings.add((String) tso.getParam(0, ""));
								settingsTsos.add(tso);
							}
						}
			}
			if (executed)
				return "<html>Group definition:<br>" +
						(settings.size() > 0 ?
								"" + StringManipulationTools.getStringList(settings, "<br>and ") + ""
								: "no (overall average)")
						+ "";
			else
				return "<html><center>About to create a<br>" + (appendix ? "full " : "short ") + "report" + (clustering ? " with clustering" : "") + "<br>"
						+ (ratio ? "(stress-response-report)<br>" : "")
						+ (exportIndividualAngles ? " (side angles)" :
								(settings.size() > 0 ?
										"- divided by " + StringManipulationTools.getStringList(settings, "<br>and ") + " -"
										: "- overall average -")
						) + "<br>(click here to continue)";
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (exportIndividualAngles)
			return IAPimages.getDownloadIcon();
		else
			return "img/ext/gpl2/Gnome-Text-X-Script-64.png";// Gnome-X-Office-Spreadsheet-64.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		executed = true;
		
		settings.clear();
		
		TreeSet<String> cs = new TreeSet<String>();
		TreeSet<String> ss = new TreeSet<String>();
		TreeSet<String> gs = new TreeSet<String>();
		TreeSet<String> vs = new TreeSet<String>();
		TreeSet<String> gc = new TreeSet<String>();
		TreeSet<String> ts = new TreeSet<String>();
		
		ExperimentInterface e = experimentReference.getData(false, status);
		for (SubstanceInterface si : e) {
			for (ConditionInterface ci : si) {
				String condition = ci.getConditionName();
				String species = ci.getSpecies();
				String genotype = ci.getGenotype();
				String variety = ci.getVariety();
				String growthcondition = ci.getGrowthconditions();
				String treatment = ci.getTreatment();
				
				if (condition != null && !condition.isEmpty())
					cs.add(condition);
				else
					cs.add("(not specified)");
				if (species != null && !species.isEmpty())
					ss.add(species);
				else
					ss.add("(not specified)");
				if (genotype != null && !genotype.isEmpty())
					gs.add(genotype);
				else
					gs.add("(not specified)");
				if (variety != null && !variety.isEmpty())
					vs.add(variety);
				else
					vs.add("(not specified)");
				if (growthcondition != null && !growthcondition.isEmpty())
					gc.add(growthcondition);
				else
					gc.add("(not specified)");
				if (treatment != null && !treatment.isEmpty())
					ts.add(treatment);
				else
					ts.add("(not specified)");
			}
		}
		
		int idx = settings.size();
		
		this.togglesForReport = new ArrayList<ThreadSafeOptions>();
		
		// ThreadSafeOptions tsoA = new ThreadSafeOptions();
		// tsoA.setParam(0, "Appendix");
		// toggles.add(tsoA);
		// settings.add(new NavigationButton(new ActionToggle("Create appendix?", "Appendix", tsoA),
		// src.getGUIsetting()));
		
		for (ThreadSafeOptions ttt : toggles) {
			String setting = (String) ttt.getParam(0, "");
			if (!ttt.getBval(0, false)) {
				System.out.println("Setting " + setting + ": NO");
				continue;
			}
			System.out.println("Setting " + setting + ": YES");
			if (setting.equalsIgnoreCase("Condition"))
				for (String c : cs) {
					ThreadSafeOptions tso = new ThreadSafeOptions();
					tso.setParam(0, setting);
					tso.setParam(1, c);
					togglesForReport.add(tso);
					settings.add(new NavigationButton(new ActionToggle("Include " + setting + " '" + c + "'?", "<html><center>" + setting + "<br>'" + c + "'", tso),
							src.getGUIsetting()));
				}
			if (setting.equalsIgnoreCase(ConditionInfo.SPECIES.toString()))
				for (String c : ss) {
					ThreadSafeOptions tso = new ThreadSafeOptions();
					tso.setParam(0, setting);
					tso.setParam(1, c);
					togglesForReport.add(tso);
					settings.add(new NavigationButton(new ActionToggle("Include " + setting + " '" + c + "'?", "<html><center>" + setting + "<br>'" + c + "'", tso),
							src.getGUIsetting()));
				}
			if (setting.equalsIgnoreCase(ConditionInfo.GENOTYPE.toString()))
				for (String c : gs) {
					ThreadSafeOptions tso = new ThreadSafeOptions();
					tso.setParam(0, setting);
					tso.setParam(1, c);
					togglesForReport.add(tso);
					settings.add(new NavigationButton(new ActionToggle("Include " + setting + " '" + c + "'?", "<html><center>" + setting + "<br>'" + c + "'", tso),
							src.getGUIsetting()));
				}
			if (setting.equalsIgnoreCase(ConditionInfo.VARIETY.toString()))
				for (String c : vs) {
					ThreadSafeOptions tso = new ThreadSafeOptions();
					tso.setParam(0, setting);
					tso.setParam(1, c);
					togglesForReport.add(tso);
					settings.add(new NavigationButton(new ActionToggle("Include " + setting + " '" + c + "'?", "<html><center>" + setting + "<br>'" + c + "'", tso),
							src.getGUIsetting()));
				}
			if (setting.equalsIgnoreCase(ConditionInfo.GROWTHCONDITIONS.toString()))
				for (String c : gc) {
					ThreadSafeOptions tso = new ThreadSafeOptions();
					tso.setParam(0, setting);
					tso.setParam(1, c);
					togglesForReport.add(tso);
					settings.add(new NavigationButton(new ActionToggle("Include " + setting + " '" + c + "'?", "<html><center>" + setting + "<br>'" + c + "'", tso),
							src.getGUIsetting()));
				}
			if (setting.equalsIgnoreCase(ConditionInfo.TREATMENT.toString()))
				for (String c : ts) {
					ThreadSafeOptions tso = new ThreadSafeOptions();
					tso.setParam(0, setting);
					tso.setParam(1, c);
					togglesForReport.add(tso);
					settings.add(new NavigationButton(new ActionToggle("Include " + setting + " '" + c + "'?", "<html><center>" + setting + "<br>'" + c + "'", tso),
							src.getGUIsetting()));
				}
		}
		if (togglesForReport.size() > 0)
			settings.add(idx, new NavigationButton(new AbstractNavigationAction("Toggle all settings") {
				@Override
				public void performActionCalculateResults(NavigationButton src) throws Exception {
					for (ThreadSafeOptions tso : togglesForReport) {
						tso.setBval(0, !tso.getBval(0, true));
					}
				}
				
				@Override
				public String getDefaultTitle() {
					return "<html><center>Toggle<br>settings";
				}
				
				@Override
				public String getDefaultImage() {
					return "img/ext/gpl2/gtcf.png";
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
					return null;
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewActionSet() {
					return null;
				}
			}, src.getGUIsetting()));
	}
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
		if (xlsx)
			return prepareCommandLineExecutionFile();
		else
			return prepareCommandLineExecutionDirectory();
	}
	
	@Override
	public void postProcessCommandLineExecution() {
		if (xlsx)
			postProcessCommandLineExecutionFile();
		else
			postProcessCommandLineExecutionDirectory();
	}
	
	public boolean prepareCommandLineExecutionFile() throws Exception {
		return true;
	}
	
	public void postProcessCommandLineExecutionFile() {
		// empty
	}
	
	public boolean prepareCommandLineExecutionDirectory() throws Exception {
		return true;
	}
	
	public void postProcessCommandLineExecutionDirectory() {
		// empty
	}
}
