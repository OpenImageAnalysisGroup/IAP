/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 24.11.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

public class BackgroundTaskStatusProviderSupportingExternalCallImpl implements
					BackgroundTaskStatusProviderSupportingExternalCall {
	
	private String status1, status2;
	private double currentProgress = -1d;
	private boolean stopRequested = false;
	
	public BackgroundTaskStatusProviderSupportingExternalCallImpl(String status1, String status2) {
		this.status1 = status1;
		this.status2 = status2;
	}
	
	public synchronized void setCurrentStatusValueFine(double value) {
		currentProgress = value;
	}
	
	public boolean wantsToStop() {
		return stopRequested;
	}
	
	public void setCurrentStatusText1(String status) {
		status1 = status;
	}
	
	public void setCurrentStatusText2(String status) {
		status2 = status;
	}
	
	public synchronized int getCurrentStatusValue() {
		return (int) currentProgress;
	}
	
	public void setCurrentStatusValue(int value) {
		currentProgress = value;
	}
	
	public double getCurrentStatusValueFine() {
		return currentProgress;
	}
	
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	public void pleaseStop() {
		stopRequested = true;
	}
	
	boolean waitForUser = false;
	
	public boolean pluginWaitsForUser() {
		return waitForUser;
	}
	
	public void pleaseContinueRun() {
		waitForUser = false;
	}
	
	public void setPluginWaitsForUser(boolean wait) {
		this.waitForUser = wait;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.BackgroundTaskStatusProviderSupportingExternalCall#setCurrentStatusValueFineAdd(double)
	 */
	public synchronized void setCurrentStatusValueFineAdd(double smallProgressStep) {
		currentProgress += smallProgressStep;
	}
}
