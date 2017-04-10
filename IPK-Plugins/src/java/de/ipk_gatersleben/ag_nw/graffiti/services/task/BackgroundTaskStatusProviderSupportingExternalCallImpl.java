/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 24.11.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;

public class BackgroundTaskStatusProviderSupportingExternalCallImpl implements
		BackgroundTaskStatusProviderSupportingExternalCall {
	
	private String status1, status2;
	private double currentProgress = -1d;
	private boolean stopRequested = false;
	
	public BackgroundTaskStatusProviderSupportingExternalCallImpl(String status1, String status2) {
		this.status1 = status1;
		this.status2 = status2;
	}
	
	@Override
	public void reset() {
		status1 = null;
		status2 = null;
		currentProgress = -1d;
		stopRequested = false;
	}
	
	@Override
	public synchronized void setCurrentStatusValueFine(double value) {
		currentProgress = value;
	}
	
	@Override
	public boolean wantsToStop() {
		return stopRequested;
	}
	
	@Override
	public void setCurrentStatusText1(String status) {
		status1 = status;
	}
	
	@Override
	public void setCurrentStatusText2(String status) {
		status2 = status;
	}
	
	@Override
	public synchronized int getCurrentStatusValue() {
		return (int) currentProgress;
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		currentProgress = value;
	}
	
	@Override
	public double getCurrentStatusValueFine() {
		return currentProgress;
	}
	
	@Override
	public String getCurrentStatusMessage1() {
		return prefix1 != null ? prefix1 + status1 : status1;
	}
	
	@Override
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return getProgressStatus();
	}
	
	@Override
	public void pleaseStop() {
		stopRequested = true;
	}
	
	boolean waitForUser = false;
	
	@Override
	public boolean pluginWaitsForUser() {
		return waitForUser;
	}
	
	@Override
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
	@Override
	public synchronized void setCurrentStatusValueFineAdd(double smallProgressStep) {
		currentProgress += smallProgressStep;
	}
	
	long lastOutput = -1;
	long lastProgress = -100;
	long firstProgressFineTime = -1;
	double firstProgressFineValue = -1;
	double lastPro = -1;
	long lastProgressUpdateTime = -1;
	private String prefix1;
	
	private synchronized String getProgressStatus() {
		String result = "";
		double currPro = getCurrentStatusValueFine();
		if (currPro > lastPro)
			lastProgressUpdateTime = System.currentTimeMillis();
		
		if (currPro < lastPro) {
			lastOutput = -1;
			lastProgress = -100;
			firstProgressFineValue = -1;
			lastPro = -1;
			lastProgressUpdateTime = -1;
		}
		
		lastPro = currPro;
		
		if (currPro > 0.5 && firstProgressFineValue >= 0
				&& firstProgressFineValue < currPro
				&& System.currentTimeMillis() - firstProgressFineTime > 0) {
			long timeForProgress = lastProgressUpdateTime
					- firstProgressFineTime;
			double progress = currPro
					- firstProgressFineValue;
			double speed = progress / timeForProgress;
			double remainingTime = (100 - currPro) / speed;
			double fullTime = 100d / speed;
			long finishTime = (long) (lastProgressUpdateTime + remainingTime);
			
			// result = "<hr>eta: " + SystemAnalysis.getCurrentTime(finishTime) + ", runtime: "
			// + SystemAnalysis.getWaitTimeShort((long) fullTime) + ", remain: "
			// + SystemAnalysis.getWaitTimeShort((long) remainingTime);
			result = SystemAnalysis.getWaitTimeShort((long) remainingTime)
					+ " of "
					+ SystemAnalysis.getWaitTimeShort((long) fullTime) + " remain (" + SystemAnalysis.getCurrentTime(finishTime) + ")";
		} else
			if (currPro > 0.1) {
				firstProgressFineTime = System.currentTimeMillis();
				firstProgressFineValue = currPro;
			}
		
		lastOutput = System.currentTimeMillis();
		lastProgress = getCurrentStatusValue();
		
		if (lastProgress > 0.1) {
			if (firstProgressFineValue < 0) {
				firstProgressFineValue = getCurrentStatusValueFine();
				firstProgressFineTime = System.currentTimeMillis();
			}
		}
		return result;
	}
	
	@Override
	public void setPrefix1(String prefix1) {
		this.prefix1 = prefix1;
	}
}
