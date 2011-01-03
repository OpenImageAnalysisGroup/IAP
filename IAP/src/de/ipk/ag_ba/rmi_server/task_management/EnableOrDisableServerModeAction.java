/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class EnableOrDisableServerModeAction extends AbstractNavigationAction {
	
	private NavigationButton src;
	private final MongoDB m;
	
	public EnableOrDisableServerModeAction(MongoDB m) {
		super("Start or stop server mode (cloud computing host)");
		this.m = m;
	}
	
	@Override
	public String getDefaultImage() {
		return CloudComputingService.getInstance().getStatusImageName();
	}
	
	@Override
	public String getDefaultTitle() {
		return CloudComputingService.getInstance().getTaskNameEnableOrDisableActionText();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		NavigationAction cmna = new CloundManagerNavigationAction(m);
		try {
			cmna.performActionCalculateResults(src);
			return cmna.getResultNewActionSet();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		CloudComputingService.getInstance().switchStatus(m);
	}
}
