/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.server.task_management;

import com.mongodb.BasicDBObject;

import de.ipk.ag_ba.gui.webstart.IAPrunMode;

/**
 * @author klukas
 */
public class CloudHost extends BasicDBObject {
	private static final long serialVersionUID = 1L;
	
	public CloudHost() {
		// empty
	}
	
	public void setHostName(String host) {
		put("host", host);
	}
	
	public String getHostName() {
		return (String) get("host");
	}
	
	public void updateTime() {
		put("ping", System.currentTimeMillis());
	}
	
	public long getLastUpdateTime() {
		return (Long) get("ping");
	}
	
	public static String getHostId() {
		return "host";
	}
	
	public void setBlocksExecutedWithinLastMinute(int blocksExecutedWithinLastMinute) {
		put("blocksPerMinute", blocksExecutedWithinLastMinute);
	}
	
	public int getBlocksExecutedWithinLastMinute() {
		if (get("blocksPerMinute") != null)
			return (Integer) get("blocksPerMinute");
		else
			return 0;
	}
	
	public void setPipelineExecutedWithinCurrentHour(int pipelineExecutedWithinCurrentHour) {
		put("pipelinesWithinCurrentHour", pipelineExecutedWithinCurrentHour);
	}
	
	public int getPipelineExecutedWithinCurrentHour() {
		if (get("pipelinesWithinCurrentHour") != null)
			return (Integer) get("pipelinesWithinCurrentHour");
		else
			return 0;
	}
	
	public void setTasksExecutedWithinLastMinute(int tasksExecutedWithinLastMinute) {
		put("tasksPerMinute", tasksExecutedWithinLastMinute);
	}
	
	public int getTasksWithinLastMinute() {
		if (get("tasksPerMinute") != null)
			return (Integer) get("tasksPerMinute");
		else
			return 0;
	}
	
	public void setHostInfo(String info) {
		put("hostInfo", info);
	}
	
	public String getHostInfo() {
		return (String) get("hostInfo");
	}
	
	public void setStatus3(String status) {
		put("status3", status);
	}
	
	public String getStatus3() {
		return (String) get("status3");
	}
	
	public void setLastPipelineTime(int lastPipelineTime) {
		put("lastPipelineTimeInSec", lastPipelineTime);
	}
	
	public int getLastPipelineTime() {
		if (get("lastPipelineTimeInSec") != null)
			return (Integer) get("lastPipelineTimeInSec");
		else
			return 0;
	}
	
	public void setLoad(double lastLoad) {
		put("load", lastLoad);
	}
	
	public double getLoad() {
		if (get("load") != null)
			return (Double) get("load");
		else
			return -1;
	}
	
	public void setRealCPUcount(int cpuCount) {
		put("cpu-cnt", cpuCount);
	}
	
	public int getRealCPUcount() {
		if (get("cpu-cnt") != null)
			return (Integer) get("cpu-cnt");
		else
			return -1;
	}
	
	public void setOperatingSystem(String operatingSystem) {
		put("operatingSystem", operatingSystem);
	}
	
	public String getOperatingSystem() {
		return (String) get("operatingSystem");
	}
	
	public int getPipelinesPerHour() {
		try {
			String h = getHostName();
			long start = Long.parseLong(h.split("_")[1]);
			long now = System.currentTimeMillis();
			long diff = now - start;
			int n = getPipelineExecutedWithinCurrentHour();
			return (int) (n / (diff / 1000d / 60d / 60d));
		} catch (Exception e) {
			return -1;
		}
	}
	
	public double getTaskProgress() {
		if (get("taskProgress") != null)
			return (Double) get("taskProgress");
		else
			return 0;
	}
	
	public void setTaskProgress(double progress) {
		put("taskProgress", progress);
	}
	
	public void setClusterExecutionMode(boolean mode) {
		put("clusterMode", mode);
	}
	
	public boolean isClusterExecutionMode() {
		String h = getHostName();
		if (h.startsWith("10."))
			return true;
		// if (true)
		// return true;
		if (get("clusterMode") != null)
			return (Boolean) get("clusterMode");
		return false;
	}
	
	public void setExecutionMode(IAPrunMode runMode) {
		put("runMode", runMode.toString());
	}
}
