/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 25, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.postgresql;

import org.SystemAnalysis;

import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class CommandLineBackgroundTaskStatusProvider extends
		BackgroundTaskStatusProviderSupportingExternalCallImpl {
	
	private final boolean enabled;
	private final boolean showOnlyProgressBar;
	
	public CommandLineBackgroundTaskStatusProvider(String status1,
			String status2) {
		super(status1, status2);
		this.enabled = true;
		this.showOnlyProgressBar = false;
	}
	
	public CommandLineBackgroundTaskStatusProvider(boolean enabled) {
		super("", "");
		this.enabled = enabled;
		this.showOnlyProgressBar = false;
	}
	
	public CommandLineBackgroundTaskStatusProvider(boolean enabled,
			boolean showOnlyProgressBar) {
		super("", "");
		this.enabled = enabled;
		this.showOnlyProgressBar = showOnlyProgressBar;
	}
	
	@Override
	public void setCurrentStatusText1(String status) {
		super.setCurrentStatusText1(status);
		if (enabled && !showOnlyProgressBar)
			System.out.println(status);
		else
			if (enabled)
				showProgress();
	}
	
	@Override
	public void setCurrentStatusText2(String status) {
		super.setCurrentStatusText2(status);
		if (enabled && !showOnlyProgressBar)
			System.out.println(status);
		else
			if (enabled)
				showProgress();
	}
	
	long lastOutput = -1;
	long lastProgress = -100;
	long firstProgressFineTime = -1;
	double firstProgressFineValue = -1;
	
	private synchronized void showProgress() {
		if (System.currentTimeMillis() - lastOutput > 10 * 1000
				&& lastProgress != getCurrentStatusValue()) {
			System.out.println();
			System.out
					.println("***********************************************************************************************");
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">INFO: Progress: " + getCurrentStatusValue() + "% // "
					+ getCurrentStatusMessage1() + " // "
					+ getCurrentStatusMessage2() + " // Load: " + ((int) (10d * SystemAnalysisExt.getRealSystemCpuLoad())) / 10
					+ " // Mem: " + SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis.getMemoryMB() + " MB");
			if (firstProgressFineValue >= 0
					&& firstProgressFineValue < getCurrentStatusValueFine()
					&& System.currentTimeMillis() - firstProgressFineTime > 0) {
				long timeForProgress = System.currentTimeMillis()
						- firstProgressFineTime;
				double progress = getCurrentStatusValueFine()
						- firstProgressFineValue;
				double speed = progress / timeForProgress;
				double remainingTime = (100 - progress) / speed;
				double fullTime = 100d / speed;
				long finishTime = (long) (System.currentTimeMillis() + remainingTime);
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">INFO: Estimated finish time     : "
						+ SystemAnalysis.getCurrentTime(finishTime));
				
				String remain = SystemAnalysis.getWaitTime((long) fullTime);
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">INFO: Estimated overall run time: "
						+ remain);
				remain = SystemAnalysis.getWaitTime((long) remainingTime);
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">INFO: Estimated remaining time  : "
						+ remain);
			}
			System.out
					.println("***********************************************************************************************");
			lastOutput = System.currentTimeMillis();
			lastProgress = getCurrentStatusValue();
			
			if (lastProgress > 0) {
				if (firstProgressFineValue < 0) {
					firstProgressFineValue = getCurrentStatusValueFine();
					firstProgressFineTime = System.currentTimeMillis();
				}
			}
		}
	}
}
