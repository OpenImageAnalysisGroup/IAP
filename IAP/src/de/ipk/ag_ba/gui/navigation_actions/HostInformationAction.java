/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class HostInformationAction extends AbstractNavigationAction {
	
	private final String ip;
	
	public HostInformationAction(String ip) {
		super("Compute Node: " + ip);
		this.ip = ip;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/network-server.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return ip;
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
