/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.database_tools;

import java.util.ArrayList;
import java.util.HashSet;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.SplitResult;
import de.ipk.ag_ba.server.task_management.TempDataSetDescription;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class ActionMergeAnalysisResults extends AbstractNavigationAction {
	
	private final MongoDB m;
	private final ArrayList<NavigationButton> mergedDocuments = new ArrayList<NavigationButton>();
	private String splitCount;
	private NavigationButton src;
	private int nSplit = -1;
	private int nTemps = -1;
	
	public ActionMergeAnalysisResults(final MongoDB m) {
		super("Merges available split analysis results, creates new analysis jobs for missing data");
		this.m = m;
		BackgroundTaskHelper.issueSimpleTask(
				"Count split results",
				"Determine number of split result datasets",
				new Runnable() {
					@Override
					public void run() {
						SplitResult sr = m.processSplitResults();
						HashSet<TempDataSetDescription> availableTempResultSets = sr.getSplitResultExperimentSets();
						ActionMergeAnalysisResults.this.nSplit = availableTempResultSets.size();
						ArrayList<ExperimentHeaderInterface> availTempDatasets = sr.getAvailableTempDatasets();
						ActionMergeAnalysisResults.this.nTemps = availTempDatasets.size();
					}
				}, null);
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getThreeDocuments();
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>" +
				"Merge Split Analysis Results" +
				(nSplit >= 0 ? "<br><font color='gray'><small>(" + nTemps + " results for " + nSplit + " experiments)</small></font>" : "") +
				"</center></html>";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return mergedDocuments;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(splitCount + " temporary split datasets have been merged and removed. " +
				mergedDocuments.size() + " result datasets have been saved.");
	}
	
	@Override
	public boolean isProvidingActions() {
		return true;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		mergedDocuments.clear();
		m.processSplitResults().merge(true, getStatusProvider());
	}
}
