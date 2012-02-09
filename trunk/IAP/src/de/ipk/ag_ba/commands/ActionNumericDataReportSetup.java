/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.TreeSet;

import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionNumericDataReportSetup extends AbstractNavigationAction implements SpecialCommandLineSupport {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	private final boolean exportIndividualAngles;
	private final String[] variant;
	private final boolean xlsx;
	
	private final ArrayList<NavigationButton> settings = new ArrayList<NavigationButton>();
	
	public ActionNumericDataReportSetup(String tooltip, boolean exportIndividualAngles, String[] variant, boolean xlsx) {
		super(tooltip);
		this.exportIndividualAngles = exportIndividualAngles;
		this.variant = variant;
		this.xlsx = xlsx;
	}
	
	public ActionNumericDataReportSetup(MongoDB m, ExperimentReference experimentReference, boolean exportIndividualAngles, String[] variant, boolean xlsx) {
		this("Specify report options" +
				(exportIndividualAngles ? (xlsx ? " XLSX" : " CSV")
						: " PDF (" + StringManipulationTools.getStringList(variant, ", ") + ")"),
				exportIndividualAngles,
				variant, xlsx);
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		actions.add(new NavigationButton(
				new ActionNumericDataReportComplete(
						m, experimentReference, false, new String[] {
								variant[0], variant[1], "TRUE" }, false), src.getGUIsetting()));
		actions.add(new NavigationButton(
				new ActionNumericDataReportComplete(m, experimentReference, false, new String[] {
						variant[0], variant[1], "FALSE" }, false), src.getGUIsetting()));
		
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
		return false;
	}
	
	@Override
	public String getDefaultTitle() {
		if (exportIndividualAngles)
			return "Save " + (xlsx ? "XLSX" : "CSV") + " Data Table";
		if (SystemAnalysis.isHeadless()) {
			return "Create Report" + (xlsx ? " (XLSX)" : "")
					+ (exportIndividualAngles ? " (side angles)" : " (avg) (" + StringManipulationTools.getStringList(variant, ", ") + ")");
		} else {
			String filter = StringManipulationTools.getStringList(variant, ", ");
			if (filter.endsWith(", TRUE"))
				filter = filter.substring(0, filter.length() - ", TRUE".length());
			if (filter.endsWith(", FALSE"))
				filter = filter.substring(0, filter.length() - ", FALSE".length());
			if (filter.endsWith(", none"))
				filter = filter.substring(0, filter.length() - ", none".length());
			filter = StringManipulationTools.stringReplace(filter, ", ", " and ");
			
			return "<html><center>Report<br>"
					+ (exportIndividualAngles ? " (side angles)" : " (divided by " + filter + ")");
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (exportIndividualAngles)
			return IAPimages.getDownloadIcon();
		else
			return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		
		settings.clear();
		
		TreeSet<String> cs = new TreeSet<String>();
		TreeSet<String> ss = new TreeSet<String>();
		TreeSet<String> gs = new TreeSet<String>();
		TreeSet<String> vs = new TreeSet<String>();
		TreeSet<String> ts = new TreeSet<String>();
		
		ExperimentInterface e = experimentReference.getData(m, false);
		for (SubstanceInterface si : e) {
			for (ConditionInterface ci : si) {
				String condition = ci.getConditionName();
				String species = ci.getSpecies();
				String genotype = ci.getGenotype();
				String variety = ci.getVariety();
				String treatment = ci.getTreatment();
				
				if (condition != null)
					cs.add(condition);
				if (species != null)
					ss.add(species);
				if (genotype != null)
					gs.add(genotype);
				if (variety != null)
					vs.add(variety);
				if (treatment != null)
					ts.add(treatment);
			}
		}
		
		for (String setting : variant) {
			if (setting.equalsIgnoreCase("Condition"))
				for (String c : cs)
					settings.add(new NavigationButton(new ActionToggle("Include " + c + "?", "condition " + c), src.getGUIsetting()));
			if (setting.equalsIgnoreCase("Species"))
				for (String c : ss)
					settings.add(new NavigationButton(new ActionToggle("Include " + c + "?", "species " + c), src.getGUIsetting()));
			if (setting.equalsIgnoreCase("Genotype"))
				for (String c : gs)
					settings.add(new NavigationButton(new ActionToggle("Include " + c + "?", "genotype " + c), src.getGUIsetting()));
			if (setting.equalsIgnoreCase("Variety"))
				for (String c : vs)
					settings.add(new NavigationButton(new ActionToggle("Include " + c + "?", "variety " + c), src.getGUIsetting()));
			if (setting.equalsIgnoreCase("Treatment"))
				for (String c : ts)
					settings.add(new NavigationButton(new ActionToggle("Include " + c + "?", "treatment " + c), src.getGUIsetting()));
		}
		
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return null;
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
