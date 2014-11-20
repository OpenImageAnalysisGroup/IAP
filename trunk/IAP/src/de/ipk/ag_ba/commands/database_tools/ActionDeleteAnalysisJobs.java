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

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.SplitResult;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
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
	private ArrayList<BatchCmd> commandList = null;
	private final boolean global;
	private boolean updateForCountNeeded = false;
	
	public ActionDeleteAnalysisJobs(final MongoDB m, boolean global) {
		super("Deletes running and scheduled compute tasks");
		this.m = m;
		this.global = global;
		updateN(m, global);
		
	}
	
	private void updateN(final MongoDB m, boolean global) {
		updateForCountNeeded = false;
		if (global) {
			BackgroundTaskHelper.issueSimpleTask("Count split results",
					"Determine number of split result datasets",
					new Runnable() {
						@Override
						public void run() {
							SplitResult sr = m.processSplitResults();
							Collection<BatchCmd> jl = m.batch().getAll();
							Collection<BatchCmd> availableJobs = new ArrayList<BatchCmd>();
							for (BatchCmd c : jl)
								if (c.getRunStatus() != CloudAnalysisStatus.ARCHIVED)
									availableJobs.add(c);
							ActionDeleteAnalysisJobs.this.nJobs = availableJobs.size();
							HashSet<TempDataSetDescription> availableTempResultSets = sr.getSplitResultExperimentSets(null);
							ActionDeleteAnalysisJobs.this.nSplit = availableTempResultSets.size();
							ArrayList<ExperimentHeaderInterface> availTempDatasets = sr.getAvailableTempDatasets();
							ActionDeleteAnalysisJobs.this.nTemps = availTempDatasets.size();
						}
					}, null);
		} else {
			//
		}
	}
	
	public ActionDeleteAnalysisJobs(final MongoDB m, ArrayList<BatchCmd> commandList) {
		this(m, false);
		this.commandList = commandList;
		nJobs = commandList.size();
		nTemps = 0;
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getCloseCross();
	}
	
	@Override
	public String getDefaultTitle() {
		if (updateForCountNeeded)
			updateN(m, global);
		if (global)
			return "<html><center>Delete " + (nJobs >= 0 ? nJobs + "" : "") + " analysis tasks and<br>" +
					(nTemps >= 0 ? nTemps + "" : "") + " temporary result datasets</center>";
		else
			return "Delete " + (nJobs >= 0 ? nJobs + "" : "") + " analysis tasks";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Removed " + deleted + " compute tasks!"
				+ (global ? "<br><br>" +
						"Removed " + deletedTempDatasets + " intermediate result experiment data sets." : ""));
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		deleted = 0;
		deletedTempDatasets = 0;
		if (global) {
			if (getStatusProvider() != null)
				getStatusProvider().setCurrentStatusText1("Remove all non-archived compute tasks...");
			deleted = m.batch().deleteAll(false);
			nJobs = 0;
			if (getStatusProvider() != null)
				getStatusProvider().setCurrentStatusText1("Removed non-archived compute tasks...");
			ArrayList<ExperimentHeaderInterface> ell = m.getExperimentList(null);
			int nToDo = 0;
			for (ExperimentHeaderInterface ei : ell) {
				if (ei.getExperimentName() == null || ei.getExperimentName().length() == 0 || ei.getExperimentName().contains("§")) {
					nToDo++;
				}
			}
			if (getStatusProvider() != null)
				getStatusProvider().setCurrentStatusValueFine(0);
			int n = 0;
			if (nToDo > 0)
				for (ExperimentHeaderInterface ei : ell) {
					if (ei.getExperimentName() == null || ei.getExperimentName().length() == 0 || ei.getExperimentName().contains("§")) {
						m.deleteExperiment(ei.getDatabaseId());
						deletedTempDatasets += 1;
						nTemps--;
						n++;
					}
					if (getStatusProvider() != null)
						getStatusProvider().setCurrentStatusText1("Removed temp dataset " + n + "/" + nToDo);
					if (getStatusProvider() != null && getStatusProvider().wantsToStop())
						break;
					if (getStatusProvider() != null)
						getStatusProvider().setCurrentStatusValueFine(100d * n / nToDo);
				}
			updateForCountNeeded = true;
			if (getStatusProvider() != null)
				getStatusProvider().setCurrentStatusValueFine(100);
		} else {
			if (getStatusProvider() != null)
				getStatusProvider().setCurrentStatusText1("Remove compute tasks...");
			for (BatchCmd c : commandList) {
				ThreadSafeOptions ret = new ThreadSafeOptions();
				m.batch().delete(c, ret);
				nJobs--;
				if (ret.getBval(0, false))
					deleted++;
				if (getStatusProvider() != null && getStatusProvider().wantsToStop())
					break;
				if (getStatusProvider() != null)
					getStatusProvider().setCurrentStatusText1("Removed " + deleted + " compute tasks...");
			}
			updateForCountNeeded = true;
			if (getStatusProvider() != null)
				getStatusProvider().setCurrentStatusText1("Removed compute tasks...");
		}
		if (getStatusProvider() != null)
			getStatusProvider().setCurrentStatusText1("Processing completed");
	}
}
