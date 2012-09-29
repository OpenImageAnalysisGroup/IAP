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

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;

/**
 * @author klukas
 */
public class BatchInformationAction extends AbstractNavigationAction {
	
	private final BatchCmd cmd;
	private final RemoteCapableAnalysisAction actionProxy;
	private final BackgroundTaskStatusProviderSupportingExternalCall jobStatus;
	private final MongoDB m;
	private final String experimentName;
	
	public BatchInformationAction(final BatchCmd cmd, MongoDB m) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super("Cloud Compute Job: " + cmd.getRemoteCapableAnalysisActionClassName());
		this.cmd = cmd;
		this.m = m;
		actionProxy = (RemoteCapableAnalysisAction) Class.forName(cmd.getRemoteCapableAnalysisActionClassName()).newInstance();
		jobStatus = new MongoJobStatusProvider(cmd, this.m);
		experimentName = cmd.getExperimentHeader().getExperimentName();
	}
	
	@Override
	public String getDefaultImage() {
		return actionProxy.getDefaultNavigationImage();
	}
	
	@Override
	public String getDefaultTitle() {
		return actionProxy.getDefaultTitle();
	}
	
	@Override
	public String getDefaultTooltip() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		String time = new Date(cmd.getSubmissionTime()).toString();
		sb.append("Compute job start time: " + time + "<br>");
		sb.append("Target IPs: " + cmd.getTargetIPs() + "<br>");
		sb.append("Partial Dataset: " + cmd.getAvailableResultDatabaseId() + "<br>");
		sb.append("Newest available data from: " + cmd.getAvailableResultImportDate() + "<br>");
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
	public boolean getProvidesActions() {
		return false;
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
	
}
