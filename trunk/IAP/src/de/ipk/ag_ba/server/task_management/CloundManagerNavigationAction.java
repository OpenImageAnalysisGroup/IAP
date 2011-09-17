/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.actions.ActionCloudClusterHostInformation;
import de.ipk.ag_ba.gui.actions.ActionCloudHostInformation;
import de.ipk.ag_ba.gui.actions.ActionJobStatus;
import de.ipk.ag_ba.gui.actions.ActionMongoExperimentsNavigation;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class CloundManagerNavigationAction extends AbstractNavigationAction {
	
	private NavigationButton src;
	private final MongoDB m;
	private final ActionMongoExperimentsNavigation en;
	
	public CloundManagerNavigationAction(MongoDB m, ActionMongoExperimentsNavigation mongoExperimentsNavigationAction) {
		super("Task- and Server-Management");
		this.m = m != null ? m : MongoDB.getDefaultCloud();
		this.en = mongoExperimentsNavigationAction;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/preferences-desktop-screensaver.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Cloud Computing";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		GUIsetting guiSetting = src.getGUIsetting();
		NavigationButton startOrStopServerMode = new NavigationButton(
				new EnableOrDisableServerModeAction(m), guiSetting);
		res.add(startOrStopServerMode);
		
		try {
			NavigationButton jobStatus = new NavigationButton(new ActionJobStatus(m), src.getGUIsetting());
			res.add(jobStatus);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		if (en != null) {
			for (NavigationButton r : en.getResultNewActionSet())
				res.add(r);
		}
		
		try {
			ArrayList<CloudHost> hl = m.batchGetAvailableHosts(2 * 60 * 1000);
			boolean clusterAvailable = false;
			for (CloudHost ip : hl) {
				if (!ip.isClusterExecutionMode()) {
					NavigationButton n = new NavigationButton(new ActionCloudHostInformation(m, ip), guiSetting);
					res.add(n);
				} else
					clusterAvailable = true;
			}
			if (clusterAvailable) {
				res.add(
						new NavigationButton(
								new ActionCloudClusterHostInformation(m),
								guiSetting));
				res.add(WebFolder.getURLentity("Analyze Cluster Status",
						"http://pdw-24.ipk-gatersleben.de/ganglia/?m=load_one&r=hour&s=descending&c=Brocken&h=&sh=1&hc=4",
						IAPimages.getComputerConsole(), src.getGUIsetting()));
				
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		
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
		if (en != null) {
			en.performActionCalculateResults(src);
		}
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(new CloudTaskAndServerOverviewComponent(), true);
	}
}
