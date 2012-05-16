/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.TreeMap;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionNumericDataReportSetupInterestingTraitsStep2 extends AbstractNavigationAction {
	
	private boolean clustering;
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	ArrayList<String> lastOutput = new ArrayList<String>();
	
	private final boolean exportIndividualAngles;
	private final boolean xlsx;
	
	private ArrayList<ThreadSafeOptions> toggles;
	private final ArrayList<ThreadSafeOptions> divideDatasetBy;
	
	private boolean executed = false;
	
	private final ArrayList<NavigationButton> settingInterestingProperties = new ArrayList<NavigationButton>();
	private final ArrayList<ThreadSafeOptions> togglesForInterestingProperties = new ArrayList<ThreadSafeOptions>();
	private boolean createPDFmode;
	
	public ActionNumericDataReportSetupInterestingTraitsStep2(String tooltip,
			boolean exportIndividualAngles,
			ArrayList<ThreadSafeOptions> divideDatasetBy, boolean xlsx) {
		super(tooltip);
		this.exportIndividualAngles = exportIndividualAngles;
		this.divideDatasetBy = divideDatasetBy;
		this.xlsx = xlsx;
	}
	
	public ActionNumericDataReportSetupInterestingTraitsStep2(MongoDB m, ExperimentReference experimentReference,
			boolean exportIndividualAngles, ArrayList<ThreadSafeOptions> divideDatasetBy, boolean xlsx,
			ArrayList<ThreadSafeOptions> toggles, boolean finalStep) {
		this("Create report" +
				(exportIndividualAngles ? (xlsx ? " XLSX" : " CSV")
						: " PDF"),
				exportIndividualAngles,
				divideDatasetBy, xlsx);
		this.m = m;
		this.experimentReference = experimentReference;
		this.toggles = toggles;
		this.createPDFmode = finalStep;
	}
	
	private String[] getArrayFrom(ArrayList<ThreadSafeOptions> divideDatasetBy2) {
		ArrayList<String> res = new ArrayList<String>();
		boolean appendix = false;
		boolean ratio = false;
		for (ThreadSafeOptions tso : divideDatasetBy2) {
			String s = (String) tso.getParam(0, "");
			if (tso.getBval(0, false)) {
				if (s.equals("Appendix"))
					appendix = true;
				else
					if (s.equals("Ratio"))
						ratio = true;
					else
						if (s.equals("Clustering"))
							clustering = true;
						else
							res.add(s);
			}
		}
		while (res.size() < 2)
			res.add("none");
		while (res.size() > 2)
			res.remove(2);
		if (appendix)
			res.add("TRUE");
		else
			res.add("FALSE");
		if (ratio)
			res.add("TRUE");
		else
			res.add("FALSE");
		if (clustering)
			res.add("TRUE");
		else
			res.add("FALSE");
		
		return res.toArray(new String[] {});
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		final ThreadSafeOptions tsoBootstrapN = new ThreadSafeOptions();
		tsoBootstrapN.setInt(100);
		
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		actions.add(new NavigationButton(
				new ActionNumericDataReportCompleteFinishedStep3(
						m, experimentReference, false, divideDatasetBy, false, toggles,
						togglesForInterestingProperties, tsoBootstrapN),
				src.getGUIsetting()));
		
		if (togglesForInterestingProperties.size() > 0)
			if (clustering)
				actions.add(new NavigationButton(new AbstractNavigationAction("Bootstrapping N") {
					
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
						int n = tsoBootstrapN.getInt();
						if (n == 0)
							n = 100;
						else
							if (n == 100)
								n = 1000;
							else
								if (n == 1000)
									n = 10000;
								else
									if (n == 10000)
										n = 0;
						tsoBootstrapN.setInt(n);
					}
					
					@Override
					public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
						return null;
					}
					
					@Override
					public boolean requestTitleUpdates() {
						return true;
					}
					
					@Override
					public boolean getProvidesActions() {
						return false;
					}
					
					@Override
					public String getDefaultTitle() {
						int n = tsoBootstrapN.getInt();
						if (n > 0)
							return "<html><center>Bootstrapping-N<br>" + tsoBootstrapN.getInt() + ", using pvclust";
						else
							return "<html><center>No Bootstrapping,<br>using hclust";
					}
					
					@Override
					public NavigationImage getImageIconInactive() {
						return getImageIconActive();
					}
					
					@Override
					public NavigationImage getImageIconActive() {
						int n = tsoBootstrapN.getInt();
						if (n == 10000)
							return IAPmain.loadIcon("img/ext/gpl2/Gnome-Security-High-64.png");
						if (n == 1000)
							return IAPmain.loadIcon("img/ext/gpl2/Gnome-Security-Medium-64.png");
						if (n == 100)
							return IAPmain.loadIcon("img/ext/gpl2/Gnome-Security-Low-64.png");
						return IAPmain.loadIcon("img/ext/gpl2/gtcd.png");
					}
					
					@Override
					public String getDefaultNavigationImage() {
						int n = tsoBootstrapN.getInt();
						if (n == 10000)
							return "img/ext/gpl2/Gnome-Security-High-64.png";
						if (n == 1000)
							return "img/ext/gpl2/Gnome-Security-Medium-64.png";
						if (n == 100)
							return "img/ext/gpl2/Gnome-Security-Low-64.png";
						return "img/ext/gpl2/gtcd.png";
					}
					
					@Override
					public ArrayList<NavigationButton> getResultNewActionSet() {
						return null;
					}
				}, src.getGUIsetting()));
		
		actions.add(new NavigationButton(new AbstractNavigationAction("Toggle all settings") {
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				for (ThreadSafeOptions tso : togglesForInterestingProperties) {
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
		
		for (NavigationButton s : settingInterestingProperties)
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
		return true;
	}
	
	@Override
	public String getDefaultTitle() {
		if (!createPDFmode) {
			if (executed)
				return "Filter definition";
			if (toggles.size() > 0)
				return "<html><center>If desired, filter<br>" +
						"experiment factors out --&gt;<br>(click here to continue)";
			else
				return "<html><center>No factor selected,<br>" +
						"overall average will be plotted<br>(click here to continue)";
		}
		String add = "";
		boolean foundTrue = false;
		if (toggles == null || toggles.size() == 0)
			foundTrue = true;
		else
			for (ThreadSafeOptions tso : toggles) {
				if (tso.getBval(0, true))
					foundTrue = true;
			}
		if (!foundTrue)
			add = "<br>[NO INPUT]";
		if (exportIndividualAngles)
			return "Save " + (xlsx ? "XLSX" : "CSV") + " Data Table" + add;
		if (SystemAnalysis.isHeadless()) {
			return "Create Report" + (xlsx ? " (XLSX)" : "")
					+ (exportIndividualAngles ? " (side angles)" : " (avg) (" +
							StringManipulationTools.getStringList(
									getArrayFrom(divideDatasetBy), ", ") + ")") + add;
		} else {
			String[] arr = getArrayFrom(divideDatasetBy);
			String filter = StringManipulationTools.getStringList(
					arr, ", ");
			if (filter.endsWith(", TRUE"))
				filter = filter.substring(0, filter.length() - ", TRUE".length());
			if (filter.endsWith(", FALSE"))
				filter = filter.substring(0, filter.length() - ", FALSE".length());
			if (filter.endsWith(", none"))
				filter = filter.substring(0, filter.length() - ", none".length());
			filter = StringManipulationTools.stringReplace(filter, ", ", " and ");
			if (arr[2].equals("TRUE"))
				return "<html><center>Specify overview propeties --&gt;" + (clustering ? "<br>(used for clustering)" : "") + add + "<br>(report with appendix)";
			else
				return "<html><center>Specify overview properties --&gt;" + (clustering ? "<br>(used for clustering)" : "") + add + "<br>(click here to continue)";
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (!createPDFmode)
			return "img/ext/gpl2/Gnome-Text-X-Script-64.png";
		if (exportIndividualAngles)
			return IAPimages.getDownloadIcon();
		else
			return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		executed = true;
		settingInterestingProperties.clear();
		Experiment a = ((Experiment) experimentReference.getExperiment());
		TreeMap<String, NavigationButton> toBeAdded = new TreeMap<String, NavigationButton>();
		for (SubstanceInterface si : a) {
			String setting = si.getName();
			String niceName = IAPservice.getNiceNameForPhenotypicProperty(setting);
			if (niceName == null)
				continue;
			
			ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setParam(0, setting);
			tso.setParam(1, niceName);
			togglesForInterestingProperties.add(tso);
			
			toBeAdded.put(niceName, new NavigationButton(
					new ActionToggle("Include " + niceName + " ("
							+ setting + ")?", niceName, tso),
					src.getGUIsetting()));
			
		}
		for (NavigationButton n : toBeAdded.values())
			settingInterestingProperties.add(n);
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
}
