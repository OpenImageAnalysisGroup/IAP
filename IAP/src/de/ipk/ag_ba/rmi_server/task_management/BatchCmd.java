/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 13, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.HashSet;
import java.util.Map.Entry;

import org.BackgroundTaskStatusProvider;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class BatchCmd extends BasicDBObject {
	private static final long serialVersionUID = 1L;
	private BackgroundTaskStatusProvider statusProvider;
	
	public HashSet<String> getTargetIPs() {
		HashSet<String> res = new HashSet<String>();
		for (Entry<String, Object> e : entrySet()) {
			if (e.getKey().startsWith("targetIP"))
				res.add((String) e.getValue());
		}
		return res;
	}
	
	public void setTargetIPs(HashSet<String> ips) {
		int idx = 1;
		for (String v : ips) {
			put("targetIP" + idx, v);
			idx++;
		}
	}
	
	public BatchCmd() {
		// empty
	}
	
	public static void enqueueBatchCmd(MongoDB m, BatchCmd cmd) throws Exception {
		m.batchEnqueue(cmd);
	}
	
	public String getRemoteCapableAnalysisActionClassName() {
		return getString("command");
	}
	
	public void setRemoteCapableAnalysisActionClassName(String command) {
		put("command", command);
	}
	
	public String getRemoteCapableAnalysisActionParams() {
		return getString("parameters");
	}
	
	public void setRemoteCapableAnalysisActionParams(String parameters) {
		put("parameters", parameters);
	}
	
	public long getSubmissionTime() {
		if (get("submission") != null)
			return getLong("submission");
		else
			return System.currentTimeMillis();
	}
	
	public void setSubmissionTime(long submission) {
		put("submission", submission);
	}
	
	public void setStatusProvider(BackgroundTaskStatusProvider statusProvider) {
		this.statusProvider = statusProvider;
	}
	
	public void updateRunningStatus(MongoDB m, CloudAnalysisStatus status) {
		if (statusProvider != null) {
			put("progress", statusProvider.getCurrentStatusValueFine());
			put("line1", statusProvider.getCurrentStatusMessage1());
			put("line2", statusProvider.getCurrentStatusMessage2());
			put("waitsForUser", statusProvider.pluginWaitsForUser());
		}
		m.batchClaim(this, status, true);
		setRunStatus(status);
		// statusProvider.pleaseStop() -->
		// statusProvider.pleaseContinueRun() -->
	}
	
	public ObjectId getExperimentMongoID() {
		if (getString("experiment") != null)
			return new ObjectId(getString("experiment"));
		else
			return null;
	}
	
	public void setExperimentMongoID(String id) {
		put("experiment", id);
	}
	
	public long getLastUpdateTime() {
		return getLong("lastupdate");
	}
	
	public CloudAnalysisStatus getRunStatus() {
		return CloudAnalysisStatus.valueOf(getString("runstatus"));
	}
	
	public void setRunStatus(CloudAnalysisStatus status) {
		put("runstatus", status.name());
	}
	
	public double getCurrentStatusValueFine() {
		try {
			Double d = getDouble("progress");
			if (d == null)
				return -1;
			else
				return d;
		} catch (NullPointerException npe) {
			return -1;
		}
	}
	
	public String getCurrentStatusMessage1() {
		return getString("line1");
	}
	
	public String getCurrentStatusMessage2() {
		return getString("line2");
	}
	
	public void setSubTaskInfo(int id, int cnt) {
		put("part_idx", id);
		put("part_cnt", cnt);
	}
	
	public int getPartIdx() {
		return getInt("part_idx", 0);
	}
	
	public int getPartCnt() {
		return getInt("part_cnt", 1);
	}
	
	public void setOwner(String hostName) {
		put("owner", hostName);
	}
	
	public String getOwner() {
		if (get("owner") != null)
			return getString("owner");
		else
			return null;
	}
}
