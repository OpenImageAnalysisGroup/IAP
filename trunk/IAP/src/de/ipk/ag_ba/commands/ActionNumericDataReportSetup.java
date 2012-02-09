/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

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
