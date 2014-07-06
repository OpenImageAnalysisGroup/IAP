/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import java.util.ArrayList;

import org.ErrorMsg;
import org.ReleaseInfo;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.cloud_computing.ActionEnableOrDisableGridComuputation;
import de.ipk.ag_ba.commands.mongodb.ActionCloudClusterHostInformation;
import de.ipk.ag_ba.commands.mongodb.ActionCloudHostInformation;
import de.ipk.ag_ba.commands.mongodb.ActionJobStatus;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class CloundManagerNavigationAction extends AbstractNavigationAction {
	
	private NavigationButton src;
	private final MongoDB m;
	private final boolean showMonitoringNodes;
	
	public CloundManagerNavigationAction(MongoDB m,
			boolean showMonitoringNodes) {
		super("Task- and Server-Management for IAP V" + ReleaseInfo.IAP_VERSION_STRING);
		this.showMonitoringNodes = showMonitoringNodes;
		this.m = m != null ? m : MongoDB.getDefaultCloud();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/preferences-desktop-screensaver.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Cloud Computing (" + m.getDatabaseName() + ")";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		GUIsetting guiSetting = src.getGUIsetting();
		if (IAPmain.getRunMode() != IAPrunMode.WEB) {
			NavigationButton startOrStopServerMode = new NavigationButton(
					new ActionEnableOrDisableGridComuputation(m), guiSetting);
			res.add(startOrStopServerMode);
		}
		
		try {
			NavigationButton jobStatus = new NavigationButton(new ActionJobStatus(m), src.getGUIsetting());
			res.add(jobStatus);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		try {
			ArrayList<CloudHost> hl = m.batch().getAvailableHosts(3 * 60 * 1000);
			boolean clusterAvailable = false;
			for (CloudHost ip : hl) {
				if (!ip.isClusterExecutionMode()) {
					/*
					 * if (ip.getHostInfo() == null ||
					 * ((showMonitoringNodes && ip.getHostInfo().contains("monitoring:")) ||
					 * (!showMonitoringNodes && !ip.getHostInfo().contains("monitoring:"))
					 * )) {
					 */
					NavigationButton n = new NavigationButton(new ActionCloudHostInformation(m, ip), guiSetting);
					res.add(n);
					// }
				} else
					clusterAvailable = true;
			}
			
			String clusterStatusURL = SystemOptions.getInstance().getString("IAP", "Compute Infrastructure Info URL",
					"http://pdw-24.ipk-gatersleben.de/ganglia/?m=load_one&r=hour&s=descending&c=Brocken&h=&sh=1&hc=4");
			if (clusterAvailable) {
				res.add(
						new NavigationButton(
								new ActionCloudClusterHostInformation(m),
								guiSetting));
				if (clusterStatusURL != null && !clusterStatusURL.isEmpty()) {
					res.add(WebFolder.getURLactionButtton("Analyze Cluster Status",
							new IOurl(clusterStatusURL),
							IAPimages.getComputerConsole(), src.getGUIsetting()));
				}
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
	}
	
}
