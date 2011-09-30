/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.actions;

import java.util.ArrayList;
import java.util.HashMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.CloudHost;

/**
 * @author klukas
 */
public class ActionCloudClusterHostInformation extends AbstractNavigationAction {
	
	private BackgroundTaskStatusProviderSupportingExternalCall hostStatus;
	private NavigationButton src;
	private MongoDB m;
	
	public ActionCloudClusterHostInformation(final MongoDB m) {
		super("Compute Cluster");
		this.m = m;
		
		this.hostStatus = new BackgroundTaskStatusProviderSupportingExternalCall() {
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
				try {
					ArrayList<CloudHost> hl = m.batchGetAvailableHosts(5 * 60 * 1000);
					int blocksExecutedWithinLastMinute = 0;
					int tasksWithinLastMinute = 0;
					int pipelinesPerHour = 0;
					int lastPipelineTimeMin = -1;
					int lastPipelineTimeMax = -1;
					int speed = 0;
					HashMap<String, CloudHost> hl_filtered = new HashMap<String, CloudHost>();
					for (CloudHost ch : hl) {
						if (ch != null && ch.isClusterExecutionMode()) {
							String ip = ch.getHostName();
							if (ip == null || ip.isEmpty())
								continue;
							if (ip.contains("_"))
								ip = ip.substring(0, ip.indexOf("_"));
							hl_filtered.put(ip, ch);
						}
					}
					hostInfo = hl_filtered.size() + " nodes";
					for (CloudHost ch : hl_filtered.values()) {
						if (ch != null && ch.isClusterExecutionMode()) {
							lastStatus = ch.getTaskProgress();
							int be = ch.getBlocksExecutedWithinLastMinute();
							if (be >= 0)
								blocksExecutedWithinLastMinute += be;
							
							int te = ch.getTasksWithinLastMinute();
							if (te >= 0)
								tasksWithinLastMinute += te;
							int realizedSpeed = ch.getPipelinesPerHour();
							if (realizedSpeed >= 0)
								speed += realizedSpeed;
							int pph = ch.getPipelineExecutedWithinCurrentHour();
							if (pph >= 0)
								pipelinesPerHour += pph;
							int pt = ch.getLastPipelineTime();
							if (pt >= 0) {
								if (pt > lastPipelineTimeMax || lastPipelineTimeMax < 0)
									lastPipelineTimeMax = pt;
								if (pt < lastPipelineTimeMin || lastPipelineTimeMin < 0)
									lastPipelineTimeMin = pt;
							}
						}
					}
					String rA = "";
					if (blocksExecutedWithinLastMinute > 0 || tasksWithinLastMinute > 0)
						rA = blocksExecutedWithinLastMinute + " bpm, ";
					else
						return ""; // "idle, ";
					return speed + " p.e./h, " + rA + "t_p=[" + lastPipelineTimeMin + "," + lastPipelineTimeMax + "] s";
				} catch (Exception e) {
					// empty
					return e.getMessage() + "";
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean wantsToStop() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void setCurrentStatusText1(String status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCurrentStatusText2(String status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCurrentStatusValueFineAdd(double smallProgressStep) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/grid.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Compute Cluster";
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
		ArrayList<CloudHost> hl;
		try {
			hl = m.batchGetAvailableHosts(5 * 60 * 1000);
			for (CloudHost ip : hl) {
				if (ip.isClusterExecutionMode()) {
					NavigationButton n = new NavigationButton(new ActionCloudHostInformation(m, ip), src.getGUIsetting());
					res.add(n);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
}
