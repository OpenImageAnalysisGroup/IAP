/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 25, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.postgresql;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class CommandLineBackgroundTaskStatusProvider extends BackgroundTaskStatusProviderSupportingExternalCallImpl {
	
	private boolean enabled;
	
	public CommandLineBackgroundTaskStatusProvider(String status1, String status2) {
		super(status1, status2);
	}
	
	public CommandLineBackgroundTaskStatusProvider(boolean enabled) {
		this("", "");
		this.enabled = enabled;
	}
	
	@Override
	public void setCurrentStatusText1(String status) {
		super.setCurrentStatusText1(status);
		if (enabled)
			System.out.println(status);
	}
	
	@Override
	public void setCurrentStatusText2(String status) {
		super.setCurrentStatusText2(status);
		if (enabled)
			System.out.println(status);
	}
	
}
