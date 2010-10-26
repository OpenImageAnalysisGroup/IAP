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

import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;

/**
 * @author klukas
 */
public class CloundManagerNavigationAction extends AbstractNavigationAction {

	private final String login;
	private final String pass;
	private NavigationGraphicalEntity src;

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
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>();
		GUIsetting guiS = src.getGUIsetting();
		NavigationGraphicalEntity startOrStopServerMode = new NavigationGraphicalEntity(
				new EnableOrDisableServerModeAction(login, pass), guiS);
		res.add(startOrStopServerMode);
		return res;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		res.add(src);
		return res;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		this.src = src;
	}
}
