/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings;

public class PluginStatusInfo {
	static volatile private String currentMessage;
	static volatile private int currentProgress;
	
	public static synchronized void setCurrentMessage(String newStatusMessage) {
		currentMessage = newStatusMessage;
	}
	
	public static synchronized String getCurrentMessage() {
		return currentMessage;
	}
	
	public static synchronized void setCurrentProgress(int newValue) {
		currentProgress = newValue;
	}
	
	public static synchronized int getCurrentProgress() {
		return currentProgress;
	}
}