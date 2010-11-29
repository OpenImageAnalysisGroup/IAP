/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class EnableOrDisableServerModeAction extends AbstractNavigationAction {

	private final String login;
	private final String pass;
	private NavigationButton src;

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
	public ArrayList<NavigationButton> getResultNewActionSet() {
		NavigationAction cmna = new CloundManagerNavigationAction(login, pass);
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
		CloudComputingService.getInstance().switchStatus(login, pass);
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(new CloudTaskAndServerOverviewComponent(), true);
	}
}
