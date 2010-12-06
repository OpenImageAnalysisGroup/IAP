/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.Date;

import org.BackgroundTaskStatusProvider;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.rmi_server.task_management.BatchCmd;
import de.ipk.ag_ba.rmi_server.task_management.RemoteCapableAnalysisAction;

/**
 * @author klukas
 */
public class BatchInformationAction extends AbstractNavigationAction {

	private final BatchCmd cmd;
	private final RemoteCapableAnalysisAction actionProxy;
	private final BackgroundTaskStatusProvider jobStatus;

	public BatchInformationAction(final BatchCmd cmd) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super("Cloud Compute Job: " + cmd.getRemoteCapableAnalysisActionClassName());
		this.cmd = cmd;
		actionProxy = (RemoteCapableAnalysisAction) Class.forName(cmd.getRemoteCapableAnalysisActionClassName()).newInstance();
		jobStatus = new MongoJobStatusProvider(cmd);
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
		sb.append("Processing Experiment: " + new MongoDB().getExperimentHeader(cmd.getExperimentMongoID()).getExperimentname());
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
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}

	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		return jobStatus;
	}

}
