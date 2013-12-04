/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.database_tools;

import java.util.ArrayList;
import java.util.Collection;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class ActionArchiveAnalysisJobs extends AbstractNavigationAction {
	
	private final MongoDB m;
	protected int nJobs = -1;
	private final boolean doArchive_TRUE_unArchive_FALSE;
	
	public ActionArchiveAnalysisJobs(final MongoDB m, final boolean doArchive_TRUE_unArchive_FALSE) {
		super("Deletes running and scheduled compute tasks");
		this.m = m;
		this.doArchive_TRUE_unArchive_FALSE = doArchive_TRUE_unArchive_FALSE;
		BackgroundTaskHelper.issueSimpleTask(
				doArchive_TRUE_unArchive_FALSE ? "Archive Analysis Jobs" : "Reactivate Analysis Jobs",
				"Determine number of compute jobs",
				new Runnable() {
					@Override
					public void run() {
						Collection<BatchCmd> availableJobs = m.batch().getAll();
						int n = 0;
						for (BatchCmd c : availableJobs) {
							if (doArchive_TRUE_unArchive_FALSE) {
								if (c.getRunStatus() == CloudAnalysisStatus.SCHEDULED)
									n++;
							} else {
								if (c.getRunStatus() == CloudAnalysisStatus.ARCHIVED)
									n++;
							}
						}
						ActionArchiveAnalysisJobs.this.nJobs = n;
					}
				}, null);
	}
	
	@Override
	public String getDefaultImage() {
		if (doArchive_TRUE_unArchive_FALSE)
			return "img/ext/gpl2/Gnome-Utilities-System-Monitor-64-still.png";
		else
			return "img/ext/gpl2/Gnome-Utilities-System-Monitor-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		if (doArchive_TRUE_unArchive_FALSE)
			return "Archive " + (nJobs >= 0 ? nJobs + "" : "") + " analysis tasks";
		else
			return "Reactivate " + (nJobs >= 0 ? nJobs + "" : "") + " analysis tasks";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (doArchive_TRUE_unArchive_FALSE)
			return new MainPanelComponent("Archived " + nJobs + " compute tasks!");
		else
			return new MainPanelComponent("Reactivated " + nJobs + " compute tasks!");
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
		Collection<BatchCmd> availableJobs = m.batch().getAll();
		for (BatchCmd c : availableJobs) {
			if (doArchive_TRUE_unArchive_FALSE) {
				if (c.getRunStatus() == CloudAnalysisStatus.SCHEDULED)
					m.batch().claim(c, CloudAnalysisStatus.ARCHIVED, false);
			} else {
				if (c.getRunStatus() == CloudAnalysisStatus.ARCHIVED)
					m.batch().claim(c, CloudAnalysisStatus.SCHEDULED, false);
			}
		}
	}
}
