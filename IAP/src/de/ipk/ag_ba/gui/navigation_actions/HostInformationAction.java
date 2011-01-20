/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.BackgroundTaskStatusProvider;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.CloudHost;

/**
 * @author klukas
 */
public class HostInformationAction extends AbstractNavigationAction {
	
	private final CloudHost ip;
	private BackgroundTaskStatusProvider hostStatus;
	
	public HostInformationAction(final MongoDB m, final CloudHost ip) {
		super("Compute Node: " + ip.getHostName());
		this.ip = ip;
		
		this.hostStatus = new BackgroundTaskStatusProvider() {
			
			@Override
			public int getCurrentStatusValue() {
				return -1;
			}
			
			@Override
			public void setCurrentStatusValue(int value) {
			}
			
			@Override
			public double getCurrentStatusValueFine() {
				return -1;
			}
			
			@Override
			public String getCurrentStatusMessage1() {
				CloudHost ch;
				try {
					ch = m.batchGetUpdatedHostInfo(ip);
					if (ch.getBlocksExecutedWithinLastMinute() > 0 || ch.getTasksWithinLastMinute() > 0)
						return ch.getBlocksExecutedWithinLastMinute() + " bpm, " + ch.getTasksWithinLastMinute() + " tpm";
					else
						return "idle";
				} catch (Exception e) {
					// empty
					return "unavailable";
				}
			}
			
			@Override
			public String getCurrentStatusMessage2() {
				return null;
			}
			
			@Override
			public void pleaseStop() {
			}
			
			@Override
			public boolean pluginWaitsForUser() {
				return false;
			}
			
			@Override
			public void pleaseContinueRun() {
			}
		};
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/network-server.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return ip.getHostName();
	}
	
	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		return hostStatus;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean getProvidesActions() {
		return false;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		//
		return null;
	}
	
}
