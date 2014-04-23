/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;
import java.util.Date;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class BatchInformationAction extends AbstractNavigationAction {
	
	private final BatchCmd cmd;
	private final RemoteCapableAnalysisAction actionProxy;
	private final BackgroundTaskStatusProviderSupportingExternalCall jobStatus;
	private final MongoDB m;
	private final String experimentName;
	
	public BatchInformationAction(final BatchCmd cmd, MongoDB m) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			InstanceCreationException {
		super("Cloud Compute Job: " + cmd.getRemoteCapableAnalysisActionClassName());
		this.cmd = cmd;
		this.m = m;
		actionProxy = (RemoteCapableAnalysisAction) InstanceLoader.createInstance(cmd.getRemoteCapableAnalysisActionClassName());
		jobStatus = new MongoJobStatusProvider(cmd, this.m);
		ExperimentHeaderInterface ehi = cmd.getExperimentHeader();
		experimentName = ehi != null ? ehi.getExperimentName() : "null";
	}
	
	@Override
	public String getDefaultImage() {
		return actionProxy.getDefaultNavigationImage();
	}
	
	@Override
	public String getDefaultTitle() {
		try {
			if (actionProxy != null)
				return actionProxy.getDefaultTitle() + " " + (getCmd().getPartIdx() + 1) + "/"
						+ getCmd().getPartCnt() + "<br><small>(for " + experimentName + ")</small>";
			else
				return "ActionProxy is NULL";
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	@Override
	public String getDefaultTooltip() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		String time = new Date(getCmd().getSubmissionTime()).toString();
		sb.append("Compute job start time: " + time + "<br>");
		sb.append("Target IPs: " + getCmd().getTargetIPs() + "<br>");
		sb.append("Partial Dataset: " + getCmd().getAvailableResultDatabaseId() + "<br>");
		sb.append("Newest available data from: " + getCmd().getAvailableResultImportDate() + "<br>");
		sb.append("Processing Experiment: " + experimentName);
		return sb.toString();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return jobStatus.getCurrentStatusValue() >= -1;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
		return jobStatus;
	}
	
	public BatchCmd getCmd() {
		return cmd;
	}
	
}
