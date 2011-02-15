/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.server.task_management;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;

/**
 * @author klukas
 */
public class CloudHost extends BasicDBObject {
	private static final long serialVersionUID = 1L;
	
	public CloudHost() throws UnknownHostException {
		setHostName(SystemAnalysisExt.getHostName());
		updateTime();
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
	
	public void setLastPipelineTime(int lastPipelineTime) {
		put("lastPipelineTimeInSec", lastPipelineTime);
	}
	
	public int getLastPipelineTime() {
		if (get("lastPipelineTimeInSec") != null)
			return (Integer) get("lastPipelineTimeInSec");
		else
			return 0;
	}
	
	public void setOperatingSystem(String operatingSystem) {
		put("operatingSystem", operatingSystem);
	}
	
	public String getOperatingSystem() {
		return (String) get("operatingSystem");
	}
}
