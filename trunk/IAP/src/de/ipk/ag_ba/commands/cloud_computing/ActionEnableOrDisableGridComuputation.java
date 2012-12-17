/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.cloud_computing;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.CloudComputingService;
import de.ipk.ag_ba.server.task_management.CloundManagerNavigationAction;

/**
 * @author klukas
 */
public class ActionEnableOrDisableGridComuputation extends AbstractNavigationAction {
	
	private NavigationButton src;
	private final MongoDB m;
	
	public ActionEnableOrDisableGridComuputation(MongoDB m) {
		super("Start or stop server mode (cloud computing host)");
		this.m = m;
	}
	
	@Override
	public String getDefaultImage() {
		return CloudComputingService.getInstance(m).getStatusImageName();
	}
	
	@Override
	public String getDefaultTitle() {
		return CloudComputingService.getInstance(m).getTaskNameEnableOrDisableActionText();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		NavigationAction cmna = new CloundManagerNavigationAction(m, false);
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
		CloudComputingService.getInstance(m).switchStatus(m);
	}
}
