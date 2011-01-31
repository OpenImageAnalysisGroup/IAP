/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import org.BackgroundTaskStatusProvider;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public interface BackgroundTaskGUIprovider {
	
	public abstract void setStatusProvider(BackgroundTaskStatusProvider statusProvider, String title, String taskMessage);
	
	public abstract BackgroundTaskStatusProvider getStatusProvider();
	
	/**
	 * Call this in case the task is finished.
	 * 
	 * @param autoClose
	 */
	public abstract void setTaskFinished(boolean autoClose, long duration);
	
	/**
	 * @return True, if the GUI that is showing the progress is still visible.
	 *         For a JDialog a implementation might call isVisible()
	 */
	public abstract boolean isProgressViewVisible();
}