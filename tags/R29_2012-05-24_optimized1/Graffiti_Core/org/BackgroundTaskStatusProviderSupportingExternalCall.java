/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package org;

/**
 * @author klukas
 */
public interface BackgroundTaskStatusProviderSupportingExternalCall
					extends BackgroundTaskStatusProvider {
	public abstract void setCurrentStatusValueFine(double value);
	
	public boolean wantsToStop();
	
	public abstract void setCurrentStatusText1(String status);
	
	public abstract void setCurrentStatusText2(String status);
	
	public abstract void setCurrentStatusValueFineAdd(double smallProgressStep);
}