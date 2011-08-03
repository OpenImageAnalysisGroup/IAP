/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Feb 9, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_nw.graffiti.services;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class BackgroundTaskConsoleLogger extends
					BackgroundTaskStatusProviderSupportingExternalCallImpl {
	
	private boolean enabled;
	
	public BackgroundTaskConsoleLogger(String status1, String status2, boolean enabled) {
		super(status1, status2);
		this.enabled = enabled;
		if (enabled)
			System.out.println("Task Started: " + status1 + " // " + status2);
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
