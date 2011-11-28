/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 25, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.postgresql;

import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class CommandLineBackgroundTaskStatusProvider extends BackgroundTaskStatusProviderSupportingExternalCallImpl {
	
	private final boolean enabled;
	private final boolean showOnlyProgressBar;
	
	public CommandLineBackgroundTaskStatusProvider(String status1, String status2) {
		super(status1, status2);
		this.enabled = true;
		this.showOnlyProgressBar = false;
	}
	
	public CommandLineBackgroundTaskStatusProvider(boolean enabled) {
		super("", "");
		this.enabled = enabled;
		this.showOnlyProgressBar = false;
	}
	
	public CommandLineBackgroundTaskStatusProvider(boolean enabled, boolean showOnlyProgressBar) {
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
	
	private synchronized void showProgress() {
		if (System.currentTimeMillis() - lastOutput > 10 * 1000 && lastProgress != getCurrentStatusValue()) {
			System.out.println();
			System.out.println(SystemAnalysisExt.getCurrentTime() + "> INFO: Progress: " + getCurrentStatusValue() + "% // " + getCurrentStatusMessage1() + " // "
					+ getCurrentStatusMessage2());
			lastOutput = System.currentTimeMillis();
			lastProgress = getCurrentStatusValue();
		}
	}
	
}
