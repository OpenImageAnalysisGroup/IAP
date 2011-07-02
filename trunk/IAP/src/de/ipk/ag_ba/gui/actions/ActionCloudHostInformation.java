/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.actions;

import java.net.InetAddress;
import java.util.ArrayList;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudHost;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

/**
 * @author klukas
 */
public class ActionCloudHostInformation extends AbstractNavigationAction {
	
	private CloudHost ip;
	private BackgroundTaskStatusProvider hostStatus;
	private NavigationButton src;
	private MongoDB m;
	String niceHostName;
	
	public ActionCloudHostInformation(final MongoDB m, final CloudHost ip) {
		super("<html>Compute Node: " + ip.getHostName() + " <br>realized speed: " + ip.getPipelinesPerHour() + "  (pipelines/h)");
		this.ip = ip;
		this.m = m;
		
		String hostInfo = ip.getHostName();
		if (hostInfo != null && hostInfo.contains("_")) {
			try {
				String[] hhh = hostInfo.split("_", 2);
				String host = hhh[0];
				try {
					InetAddress addr = InetAddress.getByName(host);
					String hostname = addr.getHostName();
					host = hostname;
				} catch (Exception errr) {
					// ignore if hostname is unknown
				}
				hostInfo = host + "<br>(up " + SystemAnalysisExt.getCurrentTime(Long.parseLong(hhh[1])) + ")";
				
			} catch (Exception err) {
				// ignore unknown formatting
			}
		}
		niceHostName = hostInfo;
		
		this.hostStatus = new BackgroundTaskStatusProvider() {
			private String hostInfo;
			private double lastStatus = -1;
			
			@Override
			public int getCurrentStatusValue() {
				return (int) lastStatus;
			}
			
			@Override
			public void setCurrentStatusValue(int value) {
			}
			
			@Override
			public double getCurrentStatusValueFine() {
				return lastStatus;
			}
			
			@Override
			public String getCurrentStatusMessage1() {
				CloudHost ch;
				try {
					ch = m.batchGetUpdatedHostInfo(ip);
					if (ch != null) {
						ActionCloudHostInformation.this.ip = ch;
						lastStatus = ch.getTaskProgress();
						hostInfo = ch.getHostInfo();
						String rA = "";
						if (ch.getBlocksExecutedWithinLastMinute() > 0 || ch.getTasksWithinLastMinute() > 0)
							rA = ch.getBlocksExecutedWithinLastMinute() + " bpm, ";
						else
							return "idle, ";
						return rA + "t_p=" + ch.getLastPipelineTime() + " s, " +
								ch.getPipelineExecutedWithinCurrentHour() + " p.e.";
					} else
						return "N/A";
				} catch (Exception e) {
					// empty
					return "unavailable";
				}
			}
			
			@Override
			public String getCurrentStatusMessage2() {
				return hostInfo;
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
		if (ip != null) {
			if (ip.getOperatingSystem() != null) {
				if (ip.getOperatingSystem().toUpperCase().contains("WINDOWS"))
					return "img/ext/windows-pc.png";
				if (ip.getOperatingSystem().toUpperCase().contains("MAC"))
					return "img/ext/macpro_side.png";
				if (ip.getOperatingSystem().toUpperCase().contains("LINUX"))
					return "img/ext/dellR810.png";
				return "img/ext/network-server-status.png";
			}
		}
		return "img/ext/network-server.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return niceHostName;
	}
	
	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		return hostStatus;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public boolean getProvidesActions() {
		return true;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		try {
			for (BatchCmd b : m.batchGetAllCommands()) {
				if (b.getOwner() != null && b.getOwner().equals(ip.getHostName())) {
					NavigationButton n;
					n = new NavigationButton(new BatchInformationAction(b, m), src.getGUIsetting());
					n.setProcessing(true);
					// n.setRightAligned(true);
					res.add(n);
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return res;
	}
	
}
