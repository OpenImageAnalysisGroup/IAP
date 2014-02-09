/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;
import java.util.HashMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ObjectRef;
import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
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
	private final ObjectRef postFix = new ObjectRef("", "");
	private boolean requestRefresh = false;
	boolean onceExecuted = false;
	
	public ActionCloudClusterHostInformation(final MongoDB m) {
		super("Compute Cluster");
		this.m = m;
		
		this.hostStatus = new BackgroundTaskStatusProviderSupportingExternalCall() {
			@SuppressWarnings("unused")
			private String hostInfo, status3;
			private double lastStatus = -1;
			
			private int initCnt = -1;
			
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
					ArrayList<CloudHost> hl = m.batch().getAvailableHosts(90 * 1000);// 5 * 60 * 1000);
					int blocksExecutedWithinLastMinute = 0;
					int tasksWithinLastMinute = 0;
					@SuppressWarnings("unused")
					int pipelinesPerHour = 0;
					int lastPipelineTimeMin = -1;
					int lastPipelineTimeMax = -1;
					int speed = 0;
					int procCnt = 0;
					HashMap<String, ArrayList<CloudHost>> hl_filtered = new HashMap<String, ArrayList<CloudHost>>();
					for (CloudHost ch : hl) {
						if (ch != null && ch.isClusterExecutionMode()) {
							String ip = ch.getHostName();
							if (ip == null || ip.isEmpty())
								continue;
							if (ip.contains("_"))
								ip = ip.substring(0, ip.indexOf("_"));
							if (!hl_filtered.containsKey(ip))
								hl_filtered.put(ip, new ArrayList<CloudHost>());
							hl_filtered.get(ip).add(ch);
							procCnt++;
						}
					}
					if (initCnt < 0)
						initCnt = procCnt;
					if (procCnt < initCnt)
						initCnt = procCnt;
					if (procCnt > initCnt) {
						initCnt = procCnt;
						if (onceExecuted)
							ActionCloudClusterHostInformation.this.requestRefresh = true;
					}
					hostInfo = hl_filtered.size() + " nodes, " + procCnt + " instances";
					if (hl_filtered.size() > 0 && hl_filtered.size() < 4)
						hostInfo = hostInfo + "<br><small><font color='gray'>[" + StringManipulationTools.getMaxStringList(hl_filtered.keySet(), ", ", 2, "...")
								+ "]</font></small>";
					if (hl_filtered.size() > 0)
						postFix.setString(":<br><small>" + StringManipulationTools.getStringList("- ", hl_filtered.keySet(), "<br>", 1, "") + "</small>");
					else
						postFix.setString("");
					
					for (ArrayList<CloudHost> al : hl_filtered.values()) {
						for (CloudHost ch : al) {
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
				//
				
			}
			
			@Override
			public boolean wantsToStop() {
				return false;
			}
			
			@Override
			public void setCurrentStatusText1(String status) {
				
			}
			
			@Override
			public void setCurrentStatusText2(String status) {
				
			}
			
			@Override
			public void setCurrentStatusValueFineAdd(double smallProgressStep) {
				
			}
			
			@Override
			public String getCurrentStatusMessage3() {
				return null;
			}
			
			@Override
			public void setPrefix1(String prefix1) {
				
			}
		};
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/grid.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Compute Grid";
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
		return hostStatus;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		requestRefresh = false;
		onceExecuted = true;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public String getDefaultTooltip() {
		return "<html>" + super.getDefaultTooltip() + postFix;
	}
	
	@Override
	public boolean isProvidingActions() {
		return true;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public boolean requestRefresh() {
		if (requestRefresh) {
			requestRefresh = false;
			return true;
		} else
			return false;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		ArrayList<CloudHost> hl;
		try {
			hl = m.batch().getAvailableHosts(5 * 60 * 1000);
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
