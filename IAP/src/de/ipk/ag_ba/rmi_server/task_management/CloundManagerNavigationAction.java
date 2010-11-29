/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class CloundManagerNavigationAction extends AbstractNavigationAction {

	private final String login;
	private final String pass;
	private NavigationButton src;

	public CloundManagerNavigationAction(String login, String pass) {
		super("Task- and Server-Management");
		this.login = login;
		this.pass = pass;
	}

	@Override
	public String getDefaultImage() {
		return "img/ext/preferences-desktop-screensaver.png";
	}

	@Override
	public String getDefaultTitle() {
		return "Task Management";
	}

	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		GUIsetting guiS = src.getGUIsetting();
		NavigationButton startOrStopServerMode = new NavigationButton(
							new EnableOrDisableServerModeAction(login, pass), guiS);
		res.add(startOrStopServerMode);
		return res;
	}

	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}

	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
}
