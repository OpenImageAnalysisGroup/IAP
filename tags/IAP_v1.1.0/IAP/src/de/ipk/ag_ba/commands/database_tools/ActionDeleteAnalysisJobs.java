/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.database_tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.SplitResult;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.TempDataSetDescription;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class ActionDeleteAnalysisJobs extends AbstractNavigationAction {
	
	private final MongoDB m;
	private long deleted;
	private int deletedTempDatasets;
	protected int nJobs = -1;
	protected int nSplit = -1;
	protected int nTemps = -1;
	
	public ActionDeleteAnalysisJobs(final MongoDB m) {
		super("Deletes running and scheduled compute tasks");
		this.m = m;
		BackgroundTaskHelper.issueSimpleTask("Count split results",
				"Determine number of split result datasets",
				new Runnable() {
					@Override
					public void run() {
						SplitResult sr = m.processSplitResults();
						Collection<BatchCmd> availableJobs = m.batch().getAll();
						ActionDeleteAnalysisJobs.this.nJobs = availableJobs.size();
						HashSet<TempDataSetDescription> availableTempResultSets = sr.getSplitResultExperimentSets();
						ActionDeleteAnalysisJobs.this.nSplit = availableTempResultSets.size();
						ArrayList<ExperimentHeaderInterface> availTempDatasets = sr.getAvailableTempDatasets();
						ActionDeleteAnalysisJobs.this.nTemps = availTempDatasets.size();
					}
				}, null);
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getCloseCross();
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Delete " + (nJobs >= 0 ? nJobs + "" : "") + " analysis tasks and<br>" +
				(nTemps >= 0 ? nTemps + "" : "") + " temporary result datasets</center>";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Removed " + deleted + " compute tasks!<br><br>" +
				"Removed " + deletedTempDatasets + " intermediate result experiment data sets.");
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		deleted = 0;
		deletedTempDatasets = 0;
		deleted = m.batch().deleteAll();
		
		for (ExperimentHeaderInterface ei : m.getExperimentList(null)) {
			if (ei.getExperimentName() == null || ei.getExperimentName().length() == 0 || ei.getExperimentName().contains("§")) {
				m.deleteExperiment(ei.getDatabaseId());
				deletedTempDatasets += 1;
			}
		}
	}
}
