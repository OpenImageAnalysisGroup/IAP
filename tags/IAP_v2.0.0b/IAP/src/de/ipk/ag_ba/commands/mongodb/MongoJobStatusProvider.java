/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.mongodb;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;

/**
 * @author klukas
 */
public class MongoJobStatusProvider implements BackgroundTaskStatusProviderSupportingExternalCall {
	
	private BatchCmd cmd;
	private final MongoDB m;
	private int lastStatus = -1;
	
	public MongoJobStatusProvider(BatchCmd cmd, MongoDB m) {
		this.cmd = cmd;
		this.m = m;
	}
	
	@Override
	public int getCurrentStatusValue() {
		return lastStatus;
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		// empty
	}
	
	@Override
	public double getCurrentStatusValueFine() {
		double ret;
		cmd = m.batch().getCommand(cmd);
		if (cmd == null)
			ret = -2;
		else
			if (cmd.getRunStatus() == CloudAnalysisStatus.SCHEDULED)
				ret = -1;
			else
				ret = cmd.getCurrentStatusValueFine();
		lastStatus = (int) Math.round(ret);
		return ret;
	}
	
	@Override
	public String getCurrentStatusMessage1() {
		try {
			return "<html>" + (cmd.getOwner() != null ? "Owner: " + cmd.getNiceOwner() + "<br>" : "") +
					"Status: " + cmd.getRunStatus().toNiceString()
					+ (cmd.getCurrentStatusMessage1() != null ? ", " + cmd.getCurrentStatusMessage1().toLowerCase() : "");
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
	
	@Override
	public void setCurrentStatusValueFine(double value) {
		//
		
	}
	
	@Override
	public boolean wantsToStop() {
		return false;
	}
	
	@Override
	public void setCurrentStatusText1(String status) {
		
	}
	
	@Override
	public void setCurrentStatusText2(String status) {
		
	}
	
	@Override
	public void setCurrentStatusValueFineAdd(double smallProgressStep) {
		
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		try {
			return cmd.getCurrentStatusMessage3();
		} catch (NullPointerException npe) {
			return "";
		}
	}
	
	@Override
	public void setPrefix1(String prefix1) {
		// empty
	}
	
}
