/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.actions;

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
		cmd = m.batchGetCommand(cmd);
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
	
	@Override
	public void setCurrentStatusValueFine(double value) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean wantsToStop() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setCurrentStatusText1(String status) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setCurrentStatusText2(String status) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setCurrentStatusValueFineAdd(double smallProgressStep) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		try {
			return cmd.getCurrentStatusMessage3();
		} catch (NullPointerException npe) {
			return "";
		}
	}
	
}
