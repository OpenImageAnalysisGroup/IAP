/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;

/**
 * @author klukas
 * 
 */
public class EnableOrDisableServerModeAction extends AbstractNavigationAction {

	private final String login;
	private final String pass;

	public EnableOrDisableServerModeAction(String login, String pass) {
		super("Start or stop server mode (cloud computing host)");
		this.login = login;
		this.pass = pass;
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
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return new CloundManagerNavigationAction(login, pass).getResultNewActionSet();
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		return currentSet;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		CloudComputingService.getInstance().switchStatus(login, pass);
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(new CloudTaskAndServerOverviewComponent(), true);
	}
}
