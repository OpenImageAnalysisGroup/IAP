/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.server.task_management;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author klukas
 */
public class CloudHost extends BasicDBObject {
	private static final long serialVersionUID = 1L;
	
	public CloudHost() throws UnknownHostException {
		setHostName(SystemAnalysis.getHostName());
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
	
	public void setTasksExecutedWithinLastMinute(int tasksExecutedWithinLastMinute) {
		put("tasksPerMinute", tasksExecutedWithinLastMinute);
	}
	
	public int getTasksWithinLastMinute() {
		if (get("tasksPerMinute") != null)
			return (Integer) get("tasksPerMinute");
		else
			return 0;
	}
	
}
