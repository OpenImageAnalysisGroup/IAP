/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import org.BackgroundTaskStatusProvider;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.rmi_server.task_management.BatchCmd;
import de.ipk.ag_ba.rmi_server.task_management.CloudAnalysisStatus;

/**
 * @author klukas
 */
public class MongoJobStatusProvider implements BackgroundTaskStatusProvider {
	
	private BatchCmd cmd;
	private final MongoDB m;
	
	public MongoJobStatusProvider(BatchCmd cmd, MongoDB m) {
		this.cmd = cmd;
		this.m = m;
	}
	
	@Override
	public int getCurrentStatusValue() {
		return (int) getCurrentStatusValueFine();
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		// empty
	}
	
	@Override
	public double getCurrentStatusValueFine() {
		cmd = m.batchGetCommand(cmd);
		try {
			if (cmd.getRunStatus() == CloudAnalysisStatus.SCHEDULED)
				return -1;
			return cmd.getCurrentStatusValueFine();
		} catch (NullPointerException npe) {
			return -1;
		}
	}
	
	@Override
	public String getCurrentStatusMessage1() {
		try {
			return "<html>Owner: " + (cmd.getOwner() != null ? cmd.getOwner() : "(unclaimed)") + "<br>" +
					"Status: " + cmd.getRunStatus().toString() + "" +
					"<br>" + cmd.getCurrentStatusMessage1();
		} catch (NullPointerException npe) {
			return "";
		}
	}
	
	@Override
	public String getCurrentStatusMessage2() {
		try {
			return cmd.getCurrentStatusMessage2();
		} catch (NullPointerException npe) {
			return "";
		}
	}
	
	@Override
	public void pleaseStop() {
		// empty
	}
	
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	@Override
	public void pleaseContinueRun() {
		// empty
	}
	
}
