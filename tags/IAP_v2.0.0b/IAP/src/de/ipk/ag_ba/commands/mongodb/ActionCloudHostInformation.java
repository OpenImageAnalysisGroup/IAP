/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.mongodb;

import java.net.InetAddress;
import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudHost;

/**
 * @author klukas
 */
public class ActionCloudHostInformation extends AbstractNavigationAction {
	
	private CloudHost ip;
	private BackgroundTaskStatusProviderSupportingExternalCall hostStatus;
	private NavigationButton src;
	private final MongoDB m;
	String niceHostName;
	
	double lastLoad = -1;
	int cpuCnt;
	
	public ActionCloudHostInformation(final MongoDB m, final CloudHost ip) {
		super("<html>Compute Node: " + ip.getHostName());
		this.ip = ip;
		this.m = m;
		
		final String hostInfo = ip.getHostName();
		if (hostInfo != null && hostInfo.contains("_")) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String[] hhh = hostInfo.split("_", 2);
						String host = hhh[0];
						niceHostName = "" + host + "<br>(up " + SystemAnalysis.getCurrentTime(Long.parseLong(hhh[1])) + ")";
						try {
							InetAddress addr = InetAddress.getByName(host);
							String hostname = addr.getHostName();
							host = hostname;
						} catch (Exception errr) {
							niceHostName = host + "<br>(up " + SystemAnalysis.getCurrentTime(Long.parseLong(hhh[1])) + ")";
						}
						niceHostName = host + "<br>(up " + SystemAnalysis.getCurrentTime(Long.parseLong(hhh[1])) + ")";
					} catch (Exception err) {
						// ignore unknown formatting
					}
				}
			});
			t.setName("DNS Lookup for compute host " + hostInfo);
			t.start();
		}
		niceHostName = hostInfo;
		
		this.hostStatus = new BackgroundTaskStatusProviderSupportingExternalCall() {
			
			private String hostInfo, status3;
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
					ch = m.batch().getUpdatedHostInfo(ip);
					if (ch != null) {
						ActionCloudHostInformation.this.ip = ch;
						lastStatus = ch.getTaskProgress();
						hostInfo = ch.getHostInfo();
						status3 = ch.getStatus3();
						lastLoad = ch.getLoad();
						cpuCnt = ch.getRealCPUcount();
						if (System.currentTimeMillis() - ch.getLastUpdateTime() >= 60 * 1000) {
							status3 += "<br>(finished, removing info in "
									+ SystemAnalysis.getWaitTime(120 * 1000 - (System.currentTimeMillis() - ch.getLastUpdateTime())) + ")";
						} else
							if (System.currentTimeMillis() - ch.getLastUpdateTime() >= 30 * 1000) {
								status3 += "<br>(no status update since " + SystemAnalysis.getWaitTime(System.currentTimeMillis() - ch.getLastUpdateTime()) + ")";
							}
						// if (System.currentTimeMillis() - ip.getLastUpdateTime() > 5 * 60 * 1000)
						// status3 += "<br>[REMOVE FROM UPDATE]";
						String rA = ch.getBlocksExecutedWithinLastMinute() + " bpm, ";
						return ch.getPipelinesPerHour() + " p.e./h, " + rA + "t_p="
								+ ch.getLastPipelineTime() + " s, " +
								ch.getPipelineExecutedWithinCurrentHour() + " p.e.";
					} else
						return "[REMOVE FROM UPDATE]";
				} catch (Exception e) {
					// empty
					return "[REMOVE FROM UPDATE]";
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
			
			@Override
			public void setCurrentStatusValueFine(double value) {
				//
			}
			
			@Override
			public boolean wantsToStop() {
				return false;
			}
			
			@Override
			public void setCurrentStatusText1(String status) {
				//
			}
			
			@Override
			public void setCurrentStatusText2(String status) {
				//
			}
			
			@Override
			public void setCurrentStatusValueFineAdd(double smallProgressStep) {
				//
			}
			
			@Override
			public String getCurrentStatusMessage3() {
				return status3;
			}
			
			@Override
			public void setPrefix1(String prefix1) {
				//
			}
		};
	}
	
	@Override
	public String getDefaultImage() {
		if (ip != null) {
			if (ip.isClusterExecutionMode())
				return IAPimages.getCloudComputer();
			
			String os = ip.getOperatingSystem();
			if (os != null) {
				if (os.toUpperCase().contains("WINDOWS"))
					return "img/ext/windows-pc.png";
				if (os.toUpperCase().contains("MAC"))
					return "img/ext/macpro_side.png";
				if (os.toUpperCase().contains("LINUX"))
					return "img/ext/dellR810.png";
				return "img/ext/network-server-status.png";
			}
		}
		return "img/ext/network-server.png";
	}
	
	@Override
	public String getDefaultTitle() {
		String res = niceHostName;
		if (lastLoad > 0) {
			if (res.indexOf(")") > 0)
				res = StringManipulationTools.stringReplace(res, ")", ", load " + StringManipulationTools.formatNumber(lastLoad, 1) + ", " + cpuCnt + " CPUs)");
		}
		return res;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
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
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		try {
			for (BatchCmd b : m.batch().getAll()) {
				if (b.getOwner() != null && b.getOwner().equals(ip.getHostName())) {
					NavigationButton n;
					n = new NavigationButton(new BatchInformationAction(b, m), src.getGUIsetting());
					n.setProcessing(true);
					// n.setRightAligned(true);
					res.add(n);
				}
			}
		} catch (Exception e) {
			MongoDB.saveSystemErrorMessage("CloudHostInformation Error", e);
		}
		return res;
	}
	
}
